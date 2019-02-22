package com.appian.sdk.csp.box.integration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationError;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appian.sdk.csp.box.BoxPlatformConnectedSystem;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFolder;

@TemplateId(name="GetFolderInfo")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class GetFolderInfo extends SimpleIntegrationTemplate {

  public static final String FOLDER_ID = "folderID";
  public static final String FOLDER = "folder";

  @Override
  protected SimpleConfiguration getConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {

    return integrationConfiguration.setProperties(
      textProperty(FOLDER_ID)
        .label("Folder ID")
        .instructionText("The root folder of a Box account is always represented by the ID '0'.")
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
    String folderId = integrationConfiguration.getValue(FOLDER_ID);
    requestDiagnostic.put("Folder ID", folderId);

    long executeStart = System.currentTimeMillis();

    try {

      // Get client from connected system
      BoxDeveloperEditionAPIConnection conn = BoxPlatformConnectedSystem.getConnection(
        connectedSystemConfiguration, executionContext);

      // Get folder info
      BoxFolder folder = new BoxFolder(conn, folderId);
      BoxFolder.Info folderInfo = folder.getInfo();

      // Map results
      Map<String,Object> result = new HashMap<>();
      result.put(FOLDER, folderInfo);
      // Fails because BoxFolder.Info isn't a supported type: https://tech.appian.com/docs/appian/19.1-maint/supported-response-types.html

      long executeEnd = System.currentTimeMillis();

      responseDiagnostic.put("Folder", folder.toString());
      responseDiagnostic.put("Folder Info", folderInfo);

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

      long executeEnd = System.currentTimeMillis();

      IntegrationError error;

      if (e instanceof BoxAPIException) {

        BoxAPIException ex = (BoxAPIException)e;
        error = IntegrationError.builder()
          .title("Box returned an error")
          .message(e.getMessage())
          //          .detail(e.getCause() != null ? "Cause: " + e.getCause().getMessage() : null)
          .detail("See the Response tab for more details.")
          .build();

        responseDiagnostic.put("Box Response Code", ex.getResponseCode());
        responseDiagnostic.put("Box Response", ex.getResponse());

      } else {

        error = IntegrationError.builder()
          .title("An unexpected error occurred")
          .message(e.getMessage())
//          .detail(e.getCause() != null ? "Cause: " + e.getCause().getMessage() : null)
          .detail("See the Response tab for more details.")
          .build();

      }

      // Add the exception stacktrace to the response diagnostics
      StringWriter stackTrace = new StringWriter();
      e.printStackTrace(new PrintWriter(stackTrace));
      responseDiagnostic.put("Exception", stackTrace.toString());

      IntegrationDesignerDiagnostic diagnostic = IntegrationDesignerDiagnostic.builder()
        .addExecutionTimeDiagnostic(executeEnd - executeStart)
        .addRequestDiagnostic(requestDiagnostic)
        .addResponseDiagnostic(responseDiagnostic)
        .build();

      return IntegrationResponse
        .forError(error)
        .withDiagnostic(diagnostic)
        .build();
    }
  }
}
