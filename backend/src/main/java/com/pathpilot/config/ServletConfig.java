package com.pathpilot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pathpilot.concurrency.FeedbackTelemetryAggregator;
import com.pathpilot.concurrency.RecommendationTaskExecutor;
import com.pathpilot.repository.OpportunityRepository;
import com.pathpilot.servlet.SystemHealthServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Explicitly registers the custom Jakarta HttpServlet alongside Spring MVC endpoints.
 */
@Configuration
public class ServletConfig {

    @Bean
    public SystemHealthServlet systemHealthServlet(
            DataSource dataSource,
            RecommendationTaskExecutor taskExecutor,
            OpportunityRepository opportunityRepository,
            FeedbackTelemetryAggregator telemetryAggregator,
            ObjectMapper objectMapper) {
        return new SystemHealthServlet(dataSource, taskExecutor, opportunityRepository, telemetryAggregator, objectMapper);
    }

    @Bean
    public ServletRegistrationBean<SystemHealthServlet> systemHealthServletRegistration(SystemHealthServlet servlet) {
        ServletRegistrationBean<SystemHealthServlet> registration =
                new ServletRegistrationBean<>(servlet, "/servlet/system-health");
        registration.setName("SystemHealthServlet");
        registration.setLoadOnStartup(1);
        registration.addInitParameter("serviceName", "PathPilot AI");
        return registration;
    }
}
