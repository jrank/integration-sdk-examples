package com.appian.sdk.csp.box;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;

public class BoxServiceTest {

  BoxService service;
  BoxIntegrationDesignerDiagnostic diagnostic;

  @Before
  public void setUp() throws Exception {
    BoxConfig boxConfig = BoxConfig.readFrom(new InputStreamReader(BoxServiceTest.class.getResourceAsStream("box_config.json")));
    BoxDeveloperEditionAPIConnection conn = BoxDeveloperEditionAPIConnection.getAppUserConnection("2246041444", boxConfig);
    diagnostic = new BoxIntegrationDesignerDiagnostic(true);
    service = new BoxService(conn, diagnostic);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void getEnterpriseUsers() throws IOException {
    service.getEnterpriseUsers();
//    for (Map.Entry<String,Object> entry : diagnostic.getResponseDiagnostics().entrySet()) {
//      System.out.println(entry.getKey() + ": " + entry.getValue());
//    }
  }
}
