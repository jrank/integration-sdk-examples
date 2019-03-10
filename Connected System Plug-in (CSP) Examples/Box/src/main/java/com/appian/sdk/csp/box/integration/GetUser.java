package com.appian.sdk.csp.box.integration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appian.sdk.csp.box.BoxPlatformConnectedSystem;
import com.appian.sdk.csp.box.objects.User;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxUser;

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
  protected IntegrationResponse execute(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    ExecutionContext executionContext) {

    // Setup request diagnostics
    Map<String,Object> requestDiagnostic = new LinkedHashMap<>();
    Map<String,Object> responseDiagnostic = new LinkedHashMap<>();
    BoxPlatformConnectedSystem.addRequestDiagnostics(requestDiagnostic, connectedSystemConfiguration, executionContext);

    // Get integration inputs
    String userId = integrationConfiguration.getValue(USER_ID);
    requestDiagnostic.put("User ID", userId);

    Long executeStart = System.currentTimeMillis();

    try {

      // Get client from connected system
      BoxDeveloperEditionAPIConnection conn = BoxPlatformConnectedSystem.getConnection(
        connectedSystemConfiguration, executionContext);

      // Get user
      BoxUser user = new BoxUser(conn, userId);

      // Map results
      Map<String,Object> result = new HashMap<>();
      result.put(USER, User.toMap(user.getInfo()));

      long executeEnd = System.currentTimeMillis();

      // TODO change to a better representation
      responseDiagnostic.put("User", user.toString());

      IntegrationDesignerDiagnostic diagnostic = IntegrationDesignerDiagnostic.builder()
        .addExecutionTimeDiagnostic(executeEnd - executeStart)
        .addRequestDiagnostic(requestDiagnostic)
        .addResponseDiagnostic(responseDiagnostic)
        .build();

      return IntegrationResponse
        .forSuccess(result)
        .withDiagnostic(diagnostic)
        .build();

    } catch (Exception e) {

      Long executeEnd = System.currentTimeMillis();

      return createExceptionResponse(e, executionContext, executeEnd - executeStart, requestDiagnostic, responseDiagnostic);

    }
  }
}
