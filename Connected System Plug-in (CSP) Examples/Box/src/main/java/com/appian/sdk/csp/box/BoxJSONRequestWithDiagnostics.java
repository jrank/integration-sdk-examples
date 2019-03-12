package com.appian.sdk.csp.box;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIResponse;
import com.box.sdk.BoxJSONRequest;
import com.box.sdk.BoxJSONResponse;

public class BoxJSONRequestWithDiagnostics extends BoxJSONRequest {

  private BoxIntegrationDesignerDiagnostic diagnostic;

  // TODO: To support multiple requests, add a diagnostic prefix
  public BoxJSONRequestWithDiagnostics(BoxAPIConnection api, URL url, String method, BoxIntegrationDesignerDiagnostic diagnostic) {
    super(api, url, method);

    this.diagnostic = diagnostic;
  }

  @Override
  public BoxAPIResponse send() {
    Long executionStartTime = System.currentTimeMillis();

    try {

      addRequestDiagnostics();

      BoxJSONResponse response = (BoxJSONResponse)super.send();

      addResponseDiagnostics(response);

      return response;

    } finally {

      this.diagnostic.addExecutionTime(System.currentTimeMillis() - executionStartTime);

    }
  }

  private static final String URL = "URL";
  private static final String METHOD = "Method";
  private static final String BODY = "Body";
  private static final String HEADERS = "Headers";
  private static final String STATUS_CODE = "URL";

  protected void addRequestDiagnostics() {
    Map<String, Object> requestDiagnostics = this.diagnostic.getRequestDiagnostics();

    requestDiagnostics.put(URL, this.getUrl().toString());
    requestDiagnostics.put(METHOD, this.getMethod());
    requestDiagnostics.put(HEADERS, getHeadersAsStrings(this.getHeaders()));
    if (this.getBodyAsJsonValue() != null) {
      requestDiagnostics.put(BODY, this.bodyToString());
    }
  }

  protected void addResponseDiagnostics(BoxJSONResponse response) {
    Map<String, Object> responseDiagnostics = this.diagnostic.getResponseDiagnostics();

    responseDiagnostics.put(STATUS_CODE, response.getResponseCode());
    responseDiagnostics.put(HEADERS, response.getHeaders());
    responseDiagnostics.put(BODY, response.getJSON());
  }

  private Map<String, String> getHeadersAsStrings(List<RequestHeader> headers) {
    Map<String, String> stringHeaders = new LinkedHashMap<>();
    for (RequestHeader header : headers) {
      stringHeaders.put(header.getKey(), header.getValue());
    }
    return stringHeaders;
  }
}
