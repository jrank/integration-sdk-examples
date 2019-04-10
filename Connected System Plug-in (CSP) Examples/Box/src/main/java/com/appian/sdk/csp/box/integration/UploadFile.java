package com.appian.sdk.csp.box.integration;

import java.util.Map;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.Document;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;
import com.appian.sdk.csp.box.BoxService;
import com.appian.sdk.csp.box.MultiStepIntegrationDesignerDiagnostic;

@TemplateId(name="UploadFile")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.WRITE)
public class UploadFile extends AbstractBoxIntegration {

  public static final int CHUNKED_UPLOAD_MINIMUM = 20000;

  public static final String DOCUMENT = "document";
  public static final String FILE_NAME = "fileName";
  public static final String PARENT_FOLDER_ID = "parentFolderID";

  @Override
  protected SimpleConfiguration getConfiguration(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    PropertyPath propertyPath,
    ExecutionContext executionContext) {

    SimpleConfiguration config = integrationConfiguration.setProperties(
      documentProperty(DOCUMENT)
        .label("Document")
        .instructionText("The document to upload.")
        .isRequired(true)
        .isExpressionable(true)
        .build(),
      textProperty(FILE_NAME)
        .label("Name")
        .instructionText("The desired name for the file. If blank the name of the document will be used. Box supports file names of 255 characters or less. Names cannot contain non-printable ASCII characters, \"/\" or \"\\\", names with trailing spaces, or the special names '.' and '..'.")
        .isExpressionable(true)
        .build(),
      textProperty(PARENT_FOLDER_ID)
        .label("Parent Folder ID")
        .instructionText("The ID of the parent folder. The root folder of a Box account is always represented by the ID '0'.")
        .isRequired(true)
        .isExpressionable(true)
        .build()
    );

    return config;
  }

  @Override
  protected IntegrationResponse execute(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    ExecutionContext executionContext) {

    // Get integration inputs
    Document document = integrationConfiguration.getValue(DOCUMENT);
    String fileName = integrationConfiguration.getValue(FILE_NAME);
    if (fileName == null) {
      fileName = document.getFileName();
    }
    String parentFolderId = integrationConfiguration.getValue(PARENT_FOLDER_ID);

    MultiStepIntegrationDesignerDiagnostic diagnostics = new MultiStepIntegrationDesignerDiagnostic(executionContext.isDiagnosticsEnabled());

    try {
      BoxService service = getService(connectedSystemConfiguration, executionContext, diagnostics);

      // The Pre-flight check API will verify that a file will be accepted by Box before you send all the bytes over the wire.
      service.preflightCheck(parentFolderId, fileName, Long.valueOf(document.getFileSize()));

      Map<String, Object> result;
      if (document.getFileSize() > CHUNKED_UPLOAD_MINIMUM) {
        // Use chunked upload
        result = service.uploadLargeFile(parentFolderId, document.getInputStream(), fileName, Long.valueOf(document.getFileSize()), Long.valueOf(document.getId()));
      } else {
        // Use standard upload
        result = service.uploadFile(parentFolderId, document.getInputStream(), fileName, Long.valueOf(document.getFileSize()), Long.valueOf(document.getId()));
      }

      return createSuccessResponse(result, executionContext, diagnostics);

    } catch (Exception e) {

      return createExceptionResponse(e, executionContext, diagnostics);
    }
  }
}
