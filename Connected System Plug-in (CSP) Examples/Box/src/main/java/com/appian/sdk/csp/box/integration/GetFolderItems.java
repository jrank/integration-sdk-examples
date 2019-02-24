package com.appian.sdk.csp.box.integration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
import com.appian.sdk.csp.box.objects.File;
import com.appian.sdk.csp.box.objects.Folder;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.PartialCollection;

@TemplateId(name="GetFolderItems")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class GetFolderItems extends SimpleIntegrationTemplate {

  public static final String FOLDER_ID = "folderID";
  public static final String PAGING_INFO = "pagingInfo"; // Would be nice to have a paging info directly...
  public static final String START_INDEX = "startIndex";
  public static final String BATCH_SIZE = "batchSize";
  public static final String TOTAL_COUNT = "totalCount"; // Would be nice to have datasubset directly...
  public static final String ITEMS = "items";


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
        .build(),
      integerProperty(START_INDEX)
        .label("Start Index")
        .instructionText("Index where the current page begins. Use the same value as startIndex in a PagingInfo.")
        .isRequired(false)
        .isExpressionable(true)
        .build(),
      integerProperty(BATCH_SIZE)
        .label("Batch Size")
        .instructionText("Number of items to return at a time. Default is 100, maximum is 1000. Use the same value as batchSize in a PagingInfo.")
        .isRequired(false)
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
    Integer startIndex = integrationConfiguration.getValue(START_INDEX);
    requestDiagnostic.put("Start Index", startIndex);
    Integer batchSize = integrationConfiguration.getValue(BATCH_SIZE);
    requestDiagnostic.put("Batch Size", batchSize);

    long executeStart = System.currentTimeMillis();

    try {

      // Get client from connected system
      BoxDeveloperEditionAPIConnection conn = BoxPlatformConnectedSystem.getConnection(
        connectedSystemConfiguration, executionContext);

      // Get folder
      BoxFolder folder = new BoxFolder(conn, folderId);

      // Get folder children
      PartialCollection<BoxItem.Info> children = folder.getChildrenRange(startIndex, batchSize);

      long executeEnd = System.currentTimeMillis();

      Long totalCount = children.fullSize();
      Long actualStartIndex = children.offset();
      Long actualBatchSize = children.limit();

      Collection<Map<String, Object>> items = new LinkedList<>();
      for (BoxItem.Info info : children) {
        if ("file".equals(info.getType())) {
          items.add(File.toMap(info));
        } else if ("folder".equals(info.getType())) {
          items.add(Folder.toMap(info));
        }
        // TODO handle web links, others?
      }

      // Map results
      Map<String,Object> result = new HashMap<>();
      result.put(START_INDEX, actualStartIndex);
      result.put(BATCH_SIZE, actualBatchSize);
      result.put(TOTAL_COUNT, totalCount);
      result.put(ITEMS, items);

//      responseDiagnostic.put("Folder", folder.toString());

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
          .detail("See the Response tab for more details.")
          .build();

        responseDiagnostic.put("Box Response Code", ex.getResponseCode());
        responseDiagnostic.put("Box Response", ex.getResponse());

      } else {

        error = IntegrationError.builder()
          .title("An unexpected error occurred")
          .message(e.getMessage())
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
