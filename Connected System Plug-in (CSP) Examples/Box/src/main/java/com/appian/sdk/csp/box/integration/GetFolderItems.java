package com.appian.sdk.csp.box.integration;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIRequest;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxJSONResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    Long executeStart = System.currentTimeMillis();

    try {

      // Get client from connected system
      BoxDeveloperEditionAPIConnection conn = BoxPlatformConnectedSystem.getConnection(
        connectedSystemConfiguration, executionContext);

      // Get folder item list as JSON
      BoxJSONResponse response = getFolderItemsPaging(conn, folderId, startIndex, batchSize);

      long executeEnd = System.currentTimeMillis();

      String body = response.getJSON();

      responseDiagnostic.put("Status Code", response.getResponseCode());
      responseDiagnostic.put("Headers", response.getHeaders());
      responseDiagnostic.put("Body", body);

      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> map = mapper.readValue(body, new TypeReference<Map<String, Object>>(){});

      Long totalCount = Long.valueOf(map.get("total_count").toString());
      Long actualStartIndex = Long.valueOf(map.get("offset").toString());
      Long actualBatchSize = Long.valueOf(map.get("limit").toString());

      Collection<Map<String, Object>> items = (List<Map<String, Object>>)map.get("entries");

      // Map results
      Map<String,Object> result = new HashMap<>();
      result.put(START_INDEX, actualStartIndex);
      result.put(BATCH_SIZE, actualBatchSize);
      result.put(TOTAL_COUNT, totalCount);
      result.put(ITEMS, items);

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

  public BoxJSONResponse getFolderItemsPaging(BoxAPIConnection conn, String folderId, Integer startIndex, Integer batchSize) {
    if (startIndex == null) {
      startIndex = DEFAULT_START_INDEX;
    }
    if (batchSize == null) {
      batchSize = DEFAULT_BATCH_SIZE;
    }
    if (batchSize > MAX_BATCH_SIZE) {
      batchSize = MAX_BATCH_SIZE;
    }
    String query = "?offset=" + startIndex + "&limit=" + batchSize;

    URL url = BoxFolder.GET_ITEMS_URL.buildWithQuery(conn.getBaseURL(), query, folderId);
    BoxAPIRequest request = new BoxAPIRequest(conn, url, "GET");
    return (BoxJSONResponse) request.send();
  }

  public void addResponseDiagnostics(Map<String,Object> diagnostics, BoxJSONResponse response) {

  }
}
