package com.appian.sdk.csp.box.integration;

import java.net.URL;
import java.util.Map;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appian.sdk.csp.box.BoxIntegrationDesignerDiagnostic;
import com.appian.sdk.csp.box.BoxJSONRequestWithDiagnostics;
import com.appian.sdk.csp.box.BoxPlatformConnectedSystem;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxJSONResponse;
import com.box.sdk.BoxUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@TemplateId(name="GetUser")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class GetUser extends AbstractBoxIntegration {

  public static final String USER_ID = "userID";
  public static final String USER = "user";

  @Override
  protected SimpleConfiguration getConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {

    return integrationConfiguration.setProperties(
      textProperty(USER_ID)
        .label("User ID")
        .isRequired(true)
        .isExpressionable(true)
        .build()
    );
  }

  @Override
  protected String getOperationDescription() {
    return "Get information about a user.";
  }

  @Override
  protected IntegrationResponse execute(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    ExecutionContext executionContext) {

    BoxIntegrationDesignerDiagnostic diagnostics = new BoxIntegrationDesignerDiagnostic(executionContext.isDiagnosticsEnabled());
    BoxPlatformConnectedSystem.addRequestDiagnostics(diagnostics.getRequestDiagnostics(), connectedSystemConfiguration, executionContext);

    // Get integration inputs
    String userId = integrationConfiguration.getValue(USER_ID);

    try {

      // Get client from connected system
      BoxDeveloperEditionAPIConnection conn = BoxPlatformConnectedSystem.getConnection(
        connectedSystemConfiguration, executionContext);

      // Create the request
      URL url = BoxUser.USER_URL_TEMPLATE.build(conn.getBaseURL(), userId);
      BoxJSONRequestWithDiagnostics request = new BoxJSONRequestWithDiagnostics(conn, url, "GET", diagnostics);
      BoxJSONResponse response = (BoxJSONResponse)request.send();

      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> result = mapper.readValue(response.getJSON(), new TypeReference<Map<String, Object>>(){});

      return createSuccessResponse(result, executionContext, diagnostics);

    } catch (Exception e) {

      return createExceptionResponse(e, executionContext, diagnostics);
    }
  }
}
