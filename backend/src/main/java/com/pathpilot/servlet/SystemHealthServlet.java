package com.pathpilot.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pathpilot.concurrency.FeedbackTelemetryAggregator;
import com.pathpilot.concurrency.RecommendationTaskExecutor;
import com.pathpilot.repository.OpportunityRepository;
import com.pathpilot.service.AuthSessionService;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Genuine Jakarta HttpServlet integrated alongside Spring MVC to expose live system health,
 * database readiness, recommendation engine diagnostics, and HttpSession tracking.
 * Strictly conceals credentials, JDBC URLs, raw session IDs, and stack traces.
 */
public class SystemHealthServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(SystemHealthServlet.class);
    private static final Set<String> ALLOWED_DETAILS = Set.of("summary", "engine", "session");

    private final DataSource dataSource;
    private final RecommendationTaskExecutor taskExecutor;
    private final OpportunityRepository opportunityRepository;
    private final FeedbackTelemetryAggregator telemetryAggregator;
    private final ObjectMapper objectMapper;

    private volatile Instant startupTime;
    private volatile String serviceName = "PathPilot AI";
    private final AtomicLong statusRequestCount = new AtomicLong(0);

    public SystemHealthServlet(DataSource dataSource,
                               RecommendationTaskExecutor taskExecutor,
                               OpportunityRepository opportunityRepository,
                               FeedbackTelemetryAggregator telemetryAggregator,
                               ObjectMapper objectMapper) {
        this.dataSource = dataSource;
        this.taskExecutor = taskExecutor;
        this.opportunityRepository = opportunityRepository;
        this.telemetryAggregator = telemetryAggregator;
        this.objectMapper = objectMapper;
    }

    @Override
    public void init() throws ServletException {
        this.startupTime = Instant.now();
        ServletConfig config = getServletConfig();
        if (config != null) {
            String configuredName = config.getInitParameter("serviceName");
            if (configuredName != null && !configuredName.isBlank()) {
                this.serviceName = configuredName.trim();
            }
        }
        log.info("SystemHealthServlet initialized for service '{}' at {}", serviceName, startupTime);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setHeader("Cache-Control", "no-store");

        try {
            String detailParam = req.getParameter("detail");
            String detailMode = (detailParam == null || detailParam.isBlank())
                    ? "summary"
                    : detailParam.trim().toLowerCase();

            if (!ALLOWED_DETAILS.contains(detailMode)) {
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of(
                        "status", "BAD_REQUEST",
                        "error", "Invalid 'detail' query parameter. Allowed values: summary, engine, session."
                ));
                return;
            }

            String trackParam = req.getParameter("trackSession");
            boolean trackSession = true;
            if (trackParam != null && !trackParam.isBlank()) {
                String normalizedTrack = trackParam.trim().toLowerCase();
                if ("true".equals(normalizedTrack)) {
                    trackSession = true;
                } else if ("false".equals(normalizedTrack)) {
                    trackSession = false;
                } else {
                    writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, Map.of(
                            "status", "BAD_REQUEST",
                            "error", "Invalid 'trackSession' query parameter. Allowed values: true, false."
                    ));
                    return;
                }
            }

            long requestNumber = statusRequestCount.incrementAndGet();

            DatabaseHealth dbHealth = probeDatabase();
            boolean engineHealthy = taskExecutor != null && taskExecutor.isHealthy();
            int catalogSize = 0;
            if (dbHealth.connected() && opportunityRepository != null) {
                try {
                    catalogSize = opportunityRepository.findAll().size();
                } catch (Exception ex) {
                    log.warn("Unable to read catalog count during servlet health check");
                }
            }

            Map<String, Object> sessionBlock = buildSessionHealth(req, trackSession);

            boolean overallHealthy = dbHealth.connected() && engineHealthy;
            int httpStatus = overallHealthy
                    ? HttpServletResponse.SC_OK
                    : HttpServletResponse.SC_SERVICE_UNAVAILABLE;

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("service", serviceName);
            payload.put("status", overallHealthy ? "UP" : "DEGRADED");
            payload.put("timestamp", Instant.now().toString());
            payload.put("uptimeSeconds", startupTime != null
                    ? Math.max(0L, Duration.between(startupTime, Instant.now()).getSeconds())
                    : 0L);
            payload.put("detailMode", detailMode);
            payload.put("servletRequestCount", requestNumber);

            Map<String, Object> dbBlock = new LinkedHashMap<>();
            dbBlock.put("connected", dbHealth.connected());
            dbBlock.put("productName", dbHealth.productName());
            dbBlock.put("latencyMs", dbHealth.latencyMs());
            dbBlock.put("message", dbHealth.message());
            payload.put("database", dbBlock);

            Map<String, Object> engineBlock = new LinkedHashMap<>();
            engineBlock.put("available", engineHealthy);
            engineBlock.put("status", engineHealthy ? "READY" : "UNAVAILABLE");
            if ("engine".equals(detailMode) || "summary".equals(detailMode)) {
                engineBlock.put("activeThreads", taskExecutor != null ? taskExecutor.getActiveThreadCount() : 0);
                engineBlock.put("corePoolSize", taskExecutor != null ? taskExecutor.getCorePoolSize() : 0);
                engineBlock.put("queuedTasks", taskExecutor != null ? taskExecutor.getQueueSize() : 0);
                engineBlock.put("catalogOpportunities", catalogSize);
                engineBlock.put("trackedTelemetryItems", telemetryAggregator != null ? telemetryAggregator.getSnapshot().size() : 0);
            }
            payload.put("recommendationEngine", engineBlock);
            payload.put("session", sessionBlock);

            writeJson(resp, httpStatus, payload);
        } catch (Exception ex) {
            log.error("Unexpected error while handling SystemHealthServlet GET request", ex);
            writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of(
                    "status", "ERROR",
                    "error", "An unexpected internal error occurred while evaluating system status."
            ));
        }
    }

    @Override
    public void destroy() {
        log.info("SystemHealthServlet shutting down cleanly after handling {} request(s)", statusRequestCount.get());
    }

    private DatabaseHealth probeDatabase() {
        if (dataSource == null) {
            return new DatabaseHealth(false, "Unavailable", 0L, "DataSource is not configured");
        }
        long startNanos = System.nanoTime();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1");
             ResultSet rs = ps.executeQuery()) {
            boolean rowOk = rs.next();
            boolean valid = conn.isValid(2);
            long latencyMs = Math.max(0L, (System.nanoTime() - startNanos) / 1_000_000L);
            String product = conn.getMetaData().getDatabaseProductName();
            boolean ok = rowOk && valid;
            return new DatabaseHealth(ok, product != null ? product : "Relational DB", latencyMs,
                    ok ? "Database connectivity verified" : "Database connection validation failed");
        } catch (SQLException ex) {
            long latencyMs = Math.max(0L, (System.nanoTime() - startNanos) / 1_000_000L);
            log.warn("Database connectivity check failed cleanly in servlet ({} ms)", latencyMs);
            return new DatabaseHealth(false, "Unavailable", latencyMs, "Database connectivity check failed");
        }
    }

    private Map<String, Object> buildSessionHealth(HttpServletRequest req, boolean createIfMissing) {
        HttpSession session = req.getSession(createIfMissing);
        Map<String, Object> info = new LinkedHashMap<>();

        if (session == null) {
            info.put("active", false);
            info.put("authenticated", false);
            info.put("statusCheckCount", 0);
            info.put("trackingMode", "HttpSession cookie (passive check)");
            return info;
        }

        int checks = 0;
        Object currentCount = session.getAttribute(AuthSessionService.SESSION_STATUS_CHECKS);
        if (currentCount instanceof Integer intCount) {
            checks = intCount;
        }
        if (createIfMissing) {
            checks += 1;
            session.setAttribute(AuthSessionService.SESSION_STATUS_CHECKS, checks);
            session.setAttribute(AuthSessionService.SESSION_LAST_STATUS_CHECK, Instant.now().toString());
        }

        boolean authenticated = Boolean.TRUE.equals(session.getAttribute(AuthSessionService.SESSION_AUTHENTICATED));
        Object lastCheck = session.getAttribute(AuthSessionService.SESSION_LAST_STATUS_CHECK);

        info.put("active", true);
        info.put("authenticated", authenticated);
        if (authenticated && session.getAttribute(AuthSessionService.SESSION_STUDENT_NAME) != null) {
            info.put("activeStudentName", session.getAttribute(AuthSessionService.SESSION_STUDENT_NAME));
        }
        info.put("statusCheckCount", checks);
        if (lastCheck != null) {
            info.put("lastStatusCheck", lastCheck.toString());
        }
        info.put("trackingMode", "HttpSession HttpOnly cookie");
        return info;
    }

    private void writeJson(HttpServletResponse resp, int statusCode, Object payload) throws IOException {
        resp.setStatus(statusCode);
        byte[] jsonBytes = objectMapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8);
        resp.setContentLength(jsonBytes.length);
        resp.getOutputStream().write(jsonBytes);
    }

    private record DatabaseHealth(boolean connected, String productName, long latencyMs, String message) {}
}
