package com.appian.sdk.csp.box.integration;

import java.util.Map;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appian.sdk.csp.box.BoxService;
import com.appian.sdk.csp.box.MultiStepIntegrationDesignerDiagnostic;

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

    // Get integration inputs
    String userId = integrationConfiguration.getValue(USER_ID);

    MultiStepIntegrationDesignerDiagnostic diagnostics = new MultiStepIntegrationDesignerDiagnostic(executionContext.isDiagnosticsEnabled());

    try {
      BoxService service = getService(connectedSystemConfiguration, executionContext, diagnostics);

      Map<String, Object> result = service.getUser(userId);

      return createSuccessResponse(result, executionContext, diagnostics);

    } catch (Exception e) {

      return createExceptionResponse(e, executionContext, diagnostics);
    }
  }
}
