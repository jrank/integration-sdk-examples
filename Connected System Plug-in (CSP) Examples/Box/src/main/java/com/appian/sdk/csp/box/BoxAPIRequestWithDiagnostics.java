package com.appian.sdk.csp.box;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIRequest;
import com.box.sdk.BoxAPIResponse;
import com.box.sdk.BoxJSONRequest;
import com.box.sdk.BoxJSONResponse;

public class BoxAPIRequestWithDiagnostics extends BoxAPIRequest {

  public BoxAPIRequestWithDiagnostics(BoxAPIConnection api, URL url, String method) {
    super(api, url, method);
  }

  @Override
  public List<RequestHeader> getHeaders() {
    return super.getHeaders();
  }
}
