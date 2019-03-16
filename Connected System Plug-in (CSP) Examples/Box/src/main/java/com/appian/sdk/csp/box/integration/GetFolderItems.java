package com.appian.sdk.csp.box.integration;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

@TemplateId(name="GetFolderItems")
@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class GetFolderItems extends AbstractBoxIntegration {

  public static final String FOLDER_ID = "folderID";
  public static final String PAGING_INFO = "pagingInfo"; // Would be nice to have a paging info directly...
  public static final String START_INDEX = "startIndex";
  public static final String BATCH_SIZE = "batchSize";
  public static final String TOTAL_COUNT = "totalCount"; // Would be nice to have datasubset directly...
  public static final String ITEMS = "items";

  private static final Integer DEFAULT_START_INDEX = 1;
  private static final Integer DEFAULT_BATCH_SIZE = 100;
  private static final Integer MAX_BATCH_SIZE = 1000;

  public static final String[] FIELDS = { "name", "description", "size", "status", "owned_by", "parent" };

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
  protected String getOperationDescription() {
    return "Gets all of the files, folders, or web links contained within a folder. Returns all of the items contained in the folder. An error is returned if the folder does not exist, or if any of the parameters are invalid.";
  }

  @Override
  protected IntegrationResponse execute(
    SimpleConfiguration integrationConfiguration,
    SimpleConfiguration connectedSystemConfiguration,
    ExecutionContext executionContext) {

    BoxIntegrationDesignerDiagnostic diagnostics = new BoxIntegrationDesignerDiagnostic(executionContext.isDiagnosticsEnabled());
    BoxPlatformConnectedSystem.addRequestDiagnostics(diagnostics.getRequestDiagnostics(), connectedSystemConfiguration, executionContext);

    // Get integration inputs
    String folderId = integrationConfiguration.getValue(FOLDER_ID);
    // TODO: Should paging management go in service or in integration?
    Integer startIndex = integrationConfiguration.getValue(START_INDEX);
    Integer batchSize = integrationConfiguration.getValue(BATCH_SIZE);
    if (startIndex == null) {
      startIndex = DEFAULT_START_INDEX;
    }
    if (batchSize == null) {
      batchSize = DEFAULT_BATCH_SIZE;
    }
    if (batchSize > MAX_BATCH_SIZE) {
      batchSize = MAX_BATCH_SIZE;
    }

    try {

      // Get client from connected system
      BoxDeveloperEditionAPIConnection conn = BoxPlatformConnectedSystem.getConnection(
        connectedSystemConfiguration, executionContext);

      BoxService service = new BoxService(conn, diagnostics);

      Map<String, Object> result = service.getFolderItems(folderId, startIndex, batchSize);

      // TODO: Should paging management go in service or in integration?
      Long totalCount = Long.valueOf(result.get("total_count").toString());
      Long actualStartIndex = Long.valueOf(result.get("offset").toString());
      Long actualBatchSize = Long.valueOf(result.get("limit").toString());

      Collection<Map<String, Object>> items = (List<Map<String, Object>>)result.get("entries");

      Map<String,Object> datasubset = new HashMap<>();
      datasubset.put(START_INDEX, actualStartIndex);
      datasubset.put(BATCH_SIZE, actualBatchSize);
      datasubset.put(TOTAL_COUNT, totalCount);
      datasubset.put(ITEMS, items);

      return createSuccessResponse(datasubset, executionContext, diagnostics);

    } catch (Exception e) {

      return createExceptionResponse(e, executionContext, diagnostics);
    }
  }
}
