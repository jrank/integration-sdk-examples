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
import com.appian.sdk.csp.box.objects.Folder;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFolder;

@TemplateId(name="GetFolderInfo")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class GetFolderInfo extends AbstractBoxIntegration {

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
    BoxPlatformConnectedSystem.addRequestDiagnostics(requestDiagnostic, connectedSystemConfiguration,
      executionContext);

    // Get integration inputs
    String folderId = integrationConfiguration.getValue(FOLDER_ID);
    requestDiagnostic.put("Folder ID", folderId);

    Long executeStart = System.currentTimeMillis();

    try {

      // Get client from connected system
      BoxDeveloperEditionAPIConnection conn = BoxPlatformConnectedSystem.getConnection(
        connectedSystemConfiguration, executionContext);

      // Get folder
      BoxFolder folder = new BoxFolder(conn, folderId);

      // Map results
      Map<String,Object> result = new HashMap<>();
      result.put(FOLDER, Folder.toMap(folder.getInfo()));

      long executeEnd = System.currentTimeMillis();

      // TODO change to a better representation
      responseDiagnostic.put("Folder", folder.toString());

      IntegrationDesignerDiagnostic diagnostic = IntegrationDesignerDiagnostic.builder()
        .addExecutionTimeDiagnostic(executeEnd - executeStart)
        .addRequestDiagnostic(requestDiagnostic)
        .addResponseDiagnostic(responseDiagnostic)
        .build();

      return IntegrationResponse.forSuccess(result).withDiagnostic(diagnostic).build();

    } catch (Exception e) {

      Long executeEnd = System.currentTimeMillis();

      return createExceptionResponse(e, executionContext, executeEnd - executeStart, requestDiagnostic,
        responseDiagnostic);

    }
  }
}
