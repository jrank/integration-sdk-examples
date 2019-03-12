package com.appian.sdk.csp.box;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIResponse;
import com.box.sdk.BoxDateFormat;
import com.box.sdk.BoxJSONResponse;
import com.box.sdk.BoxMultipartRequest;

public class BoxMultipartRequestWithDiagnostics extends BoxMultipartRequest {

  private BoxIntegrationDesignerDiagnostic diagnostic;
  private Map<String, Object> parts;

  // TODO: To support multiple requests, add a diagnostic prefix
  public BoxMultipartRequestWithDiagnostics(BoxAPIConnection api, URL url, BoxIntegrationDesignerDiagnostic diagnostic) {
    super(api, url);

    this.diagnostic = diagnostic;
    this.parts = new HashMap<>();
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

  @Override
  public void putField(String key, String value) {
    this.parts.put(key, value);
    super.putField(key, value);
  }

  @Override
  public void putField(String key, Date value) {
    this.parts.put(key, BoxDateFormat.format(value));
    super.putField(key, value);
  }

  @Override
  public void setFile(InputStream inputStream, String filename) {
    // TODO: Add more detail about file part
    this.parts.put("file", filename);
    super.setFile(inputStream, filename);
  }

  @Override
  public void setFile(InputStream inputStream, String filename, long fileSize) {
    // TODO: Add more detail about file part
    this.parts.put("file", filename);
    super.setFile(inputStream, filename, fileSize);
  }

  private static final String URL = "URL";
  private static final String METHOD = "Method";
  private static final String HEADERS = "Headers";
  private static final String PARTS = "Multipart Parts";
  private static final String BODY = "Body";
  private static final String STATUS_CODE = "URL";

  protected void addRequestDiagnostics() {
    Map<String, Object> requestDiagnostics = this.diagnostic.getRequestDiagnostics();

    requestDiagnostics.put(URL, this.getUrl().toString());
    requestDiagnostics.put(METHOD, this.getMethod());
    requestDiagnostics.put(HEADERS, getHeadersAsStrings(this.getHeaders()));
    requestDiagnostics.put(PARTS, this.parts);
    // TODO: break out parts to body?
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
