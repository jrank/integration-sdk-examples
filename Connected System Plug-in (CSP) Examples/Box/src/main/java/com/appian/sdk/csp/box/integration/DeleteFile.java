package com.appian.sdk.csp.box.integration;

import java.util.LinkedHashMap;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appian.sdk.csp.box.BoxIntegrationDesignerDiagnostic;
import com.appian.sdk.csp.box.BoxPlatformConnectedSystem;
import com.appian.sdk.csp.box.BoxService;
import com.box.sdk.BoxDeveloperEditionAPIConnection;

@TemplateId(name="DeleteFile")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.WRITE)
public class DeleteFile extends AbstractBoxIntegration {

  public static final String FILE_ID = "fileId";

  @Override
  protected SimpleConfiguration getConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {

    SimpleConfiguration config = integrationConfiguration.setProperties(
      // SDK: Operation description should be shown somewhere by default, even better in the create dialog!
      textProperty(OPERATION_DESCRIPTION)
        .isReadOnly(true)
        .build(),
      textProperty(FILE_ID)
        .label("File ID")
        .instructionText("The file ID to delete")
        .isRequired(true)
        .isExpressionable(true)
        .build()
    );

    // SDK: Would like to set this fixed, default value when creating the property
    config.setValue(OPERATION_DESCRIPTION, getOperationDescription());

    return config;
  }

  @Override
  protected String getOperationDescription() {
    return "Discards a file to the trash. Depending on the enterprise settings for this user, the item will either be actually deleted from Box or moved to the trash.";
  }

  @Override
  protected IntegrationResponse execute(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    ExecutionContext executionContext) {

    // TODO: Move to abstract base class?
    BoxIntegrationDesignerDiagnostic diagnostics = new BoxIntegrationDesignerDiagnostic(executionContext.isDiagnosticsEnabled());
    BoxPlatformConnectedSystem.addRequestDiagnostics(diagnostics.getRequestDiagnostics(), connectedSystemConfiguration, executionContext);

    // Get integration inputs
    String fileId = integrationConfiguration.getValue(FILE_ID);

    try {

      // Get client from connected system
      BoxDeveloperEditionAPIConnection conn = BoxPlatformConnectedSystem.getConnection(
        connectedSystemConfiguration, executionContext);

      BoxService service = new BoxService(conn, diagnostics);

      service.deleteFile(fileId);

      return createSuccessResponse(new LinkedHashMap<>(), executionContext, diagnostics);

    } catch (Exception e) {

      return createExceptionResponse(e, executionContext, diagnostics);
    }
  }
}
