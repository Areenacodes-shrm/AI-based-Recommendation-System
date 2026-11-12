package com.pathpilot.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pathpilot.concurrency.FeedbackTelemetryAggregator;
import com.pathpilot.concurrency.RecommendationTaskExecutor;
import com.pathpilot.repository.OpportunityRepository;
import com.pathpilot.service.AuthSessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("SystemHealthServlet Registration, Lifecycle & HttpSession Integration Tests")
class SystemHealthServletTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RecommendationTaskExecutor taskExecutor;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private FeedbackTelemetryAggregator telemetryAggregator;

    private String servletUrl(String query) {
        return "http://localhost:" + port + "/servlet/system-health" + (query != null && !query.isEmpty() ? "?" + query : "");
    }

    @Test
    @DisplayName("Should return 200 UP with structured non-sensitive JSON and increment HttpSession counter across Tomcat requests")
    void shouldReturnHealthyStatusAndTrackSession() throws Exception {
        CookieManager cookies = new CookieManager();
        HttpClient client = HttpClient.newBuilder().cookieHandler(cookies).build();

        HttpRequest firstReq = HttpRequest.newBuilder(URI.create(servletUrl("detail=engine"))).GET().build();
        HttpResponse<String> firstRes = client.send(firstReq, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, firstRes.statusCode());
        String contentType = firstRes.headers().firstValue("Content-Type").orElse("").toLowerCase();
        assertTrue(contentType.contains("application/json"), "Expected JSON content type, got: " + contentType);
        assertTrue(contentType.contains("utf-8"), "Expected UTF-8 encoding, got: " + contentType);
        assertEquals("no-store", firstRes.headers().firstValue("Cache-Control").orElse(""));

        String body = firstRes.body();
        JsonNode root = objectMapper.readTree(body);

        assertEquals("PathPilot AI", root.get("service").asText());
        assertEquals("UP", root.get("status").asText());
        assertEquals("engine", root.get("detailMode").asText());
        assertTrue(root.get("database").get("connected").asBoolean());
        assertTrue(root.get("recommendationEngine").get("available").asBoolean());
        assertEquals("READY", root.get("recommendationEngine").get("status").asText());
        assertTrue(root.get("recommendationEngine").get("catalogOpportunities").asInt() >= 14);
        assertTrue(root.get("session").get("active").asBoolean());
        assertEquals(1, root.get("session").get("statusCheckCount").asInt());

        // Ensure internal connection strings or credentials never appear in servlet output
        assertFalse(body.contains("jdbc:"));
        assertFalse(body.contains("passwordHash"));
        assertFalse(body.contains("SQLException"));

        HttpRequest secondReq = HttpRequest.newBuilder(URI.create(servletUrl("detail=session"))).GET().build();
        HttpResponse<String> secondRes = client.send(secondReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, secondRes.statusCode());

        JsonNode secondRoot = objectMapper.readTree(secondRes.body());
        assertEquals("session", secondRoot.get("detailMode").asText());
        assertTrue(secondRoot.get("session").get("active").asBoolean());
        assertEquals(2, secondRoot.get("session").get("statusCheckCount").asInt());
    }

    @Test
    @DisplayName("Should return 400 Bad Request for invalid query parameters")
    void shouldRejectInvalidQueryParameters() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> invalidDetailRes = client.send(
                HttpRequest.newBuilder(URI.create(servletUrl("detail=invalid_mode"))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(400, invalidDetailRes.statusCode());
        String contentType = invalidDetailRes.headers().firstValue("Content-Type").orElse("").toLowerCase();
        assertTrue(contentType.contains("application/json"));
        assertEquals("BAD_REQUEST", objectMapper.readTree(invalidDetailRes.body()).get("status").asText());

        HttpResponse<String> invalidTrackRes = client.send(
                HttpRequest.newBuilder(URI.create(servletUrl("trackSession=not_a_boolean"))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(400, invalidTrackRes.statusCode());
        assertEquals("BAD_REQUEST", objectMapper.readTree(invalidTrackRes.body()).get("status").asText());
    }

    @Test
    @DisplayName("Should honor passive session check when trackSession=false")
    void shouldSupportPassiveSessionCheck() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> res = client.send(
                HttpRequest.newBuilder(URI.create(servletUrl("trackSession=false"))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, res.statusCode());

        JsonNode root = objectMapper.readTree(res.body());
        assertFalse(root.get("session").get("active").asBoolean());
        assertEquals(0, root.get("session").get("statusCheckCount").asInt());
    }

    @Test
    @DisplayName("Should return 503 Service Unavailable cleanly on database outage and execute init/destroy lifecycle")
    void shouldReturn503WhenDatabaseUnavailableWithoutLeakingInternals() throws Exception {
        DataSource failingDataSource = new DataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                throw new SQLException("Secret internal JDBC host db-prod-01:3306 refused connection");
            }
            @Override
            public Connection getConnection(String u, String p) throws SQLException { return getConnection(); }
            @Override public PrintWriter getLogWriter() { return null; }
            @Override public void setLogWriter(PrintWriter out) {}
            @Override public void setLoginTimeout(int seconds) {}
            @Override public int getLoginTimeout() { return 0; }
            @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException { throw new SQLFeatureNotSupportedException(); }
            @Override public <T> T unwrap(Class<T> iface) throws SQLException { throw new SQLException(); }
            @Override public boolean isWrapperFor(Class<?> iface) { return false; }
        };

        SystemHealthServlet servlet = new SystemHealthServlet(
                failingDataSource, taskExecutor, opportunityRepository, telemetryAggregator, objectMapper
        );
        MockServletConfig config = new MockServletConfig("SystemHealthServlet");
        config.addInitParameter("serviceName", "PathPilot Test Harness");
        servlet.init(config);

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/servlet/system-health");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        req.getSession(true).setAttribute(AuthSessionService.SESSION_AUTHENTICATED, Boolean.FALSE);

        servlet.doGet(req, resp);
        servlet.destroy();

        assertEquals(503, resp.getStatus());
        String content = resp.getContentAsString();
        JsonNode root = objectMapper.readTree(content);
        assertEquals("DEGRADED", root.get("status").asText());
        assertEquals("PathPilot Test Harness", root.get("service").asText());
        assertFalse(root.get("database").get("connected").asBoolean());
        assertFalse(content.contains("db-prod-01"));
        assertFalse(content.contains("SQLException"));
    }
}
