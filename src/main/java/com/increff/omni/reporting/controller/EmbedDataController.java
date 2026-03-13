package com.increff.omni.reporting.controller;

import com.increff.commons.springboot.common.ApiException;
import com.increff.omni.reporting.dto.EmbedDataDto;
import com.increff.omni.reporting.model.data.EmbedTokenData;
import com.increff.omni.reporting.model.data.InputControlData;
import com.increff.omni.reporting.model.data.ReportData;
import com.increff.omni.reporting.model.data.ReportQueryData;
import com.increff.omni.reporting.model.data.TestQueryLiveData;
import com.increff.omni.reporting.model.form.EmbedTokenRequestForm;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Log4j2
@CrossOrigin
@RestController
@RequestMapping(value = "/api/embed")
@Tag(name = "Embed Integration", description = "APIs for embedded iframe integration with JWT authentication")
public class EmbedDataController {

    @Autowired
    private EmbedDataDto embedDataDto;

    @Operation(
            summary = "Generate embed token for iframe integration",
            description = "Creates a JWT token for external iframe authentication. The token embeds user and organization context for subsequent API calls."
    )
    @PostMapping(value = "/tokens")
    public EmbedTokenData generateEmbedToken(@Valid @RequestBody EmbedTokenRequestForm form) throws ApiException {
        return embedDataDto.generateEmbedToken(form);
    }

    @Operation(
            summary = "Verify JWT token and get authenticated user context",
            description = "Validates the JWT token from the Authorization header and returns the authenticated user's context including userId, orgName, orgId, email, and authentication status. This is a safe read-only operation.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping(value = "/tokens/verify")
    public Map<String, Object> verifyToken() throws ApiException {
        log.info("Verify token endpoint called");
        return embedDataDto.verifyToken();
    }

    @Operation(
            summary = "Get chart details",
            description = "Retrieves chart/report metadata by chartId. Returns configuration, visualization type, and other chart properties. This is a safe read-only operation.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping(value = "/charts/{chartId}")
    public ReportData getChartDetails(@PathVariable Integer chartId) throws ApiException {
        log.info("Get chart details endpoint called for chartId: {}", chartId);
        return embedDataDto.getChartDetails(chartId);
    }

    @Operation(
            summary = "Get chart SQL query",
            description = "Retrieves the SQL query definition for a specific chart. This is a safe read-only operation.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping(value = "/charts/{chartId}/query")
    public ReportQueryData getChartQuery(@PathVariable Integer chartId) throws ApiException {
        log.info("Get chart query endpoint called for chartId: {}", chartId);
        return embedDataDto.getChartQuery(chartId);
    }


    @Operation(
            summary = "Get chart input controls",
            description = "Retrieves input controls (filters/parameters) for a chart. Organization context is automatically extracted from JWT token. This is a safe read-only operation.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping(value = "/charts/{chartId}/controls")
    public List<InputControlData> getChartControls(@PathVariable Integer chartId) throws ApiException {
        log.info("Get chart controls endpoint called for chartId: {}", chartId);
        return embedDataDto.getChartControls(chartId);
    }

    @Operation(
            summary = "Execute chart query",
            description = "Executes a chart query with provided parameters and returns results. Organization context is automatically extracted from JWT token. POST is used to accommodate complex parameters, but this is a read-only query operation.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping(value = "/charts/query/execute")
    public TestQueryLiveData executeChartQuery(@Valid @RequestBody ReportRequestForm form) throws ApiException, IOException {
        log.info("Execute chart query endpoint called");
        return embedDataDto.executeChartQuery(form);
    }
}