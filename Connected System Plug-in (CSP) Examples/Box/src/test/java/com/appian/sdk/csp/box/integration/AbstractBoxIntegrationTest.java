package com.appian.sdk.csp.box.integration;

import java.util.Locale;
import java.util.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.ProxyConfigurationData;
import com.appian.sdk.csp.box.MultiStepIntegrationDesignerDiagnostic;
import com.box.sdk.BoxAPIException;

public class AbstractBoxIntegrationTest {

  TestIntegration integration;

  @Before
  public void setUp() throws Exception {
    this.integration = new TestIntegration();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void createExceptionError_item_name_in_use() {
    String responseBody = "{\n" + "  \"type\" : \"error\",\n" + "  \"status\" : 409,\n" +
      "  \"code\" : \"item_name_in_use\",\n" + "  \"context_info\" : {\n" + "    \"conflicts\" : {\n" +
      "      \"type\" : \"file\",\n" + "      \"id\" : \"430081003668\",\n" + "      \"file_version\" : {\n" +
      "        \"type\" : \"file_version\",\n" + "        \"id\" : \"454701178068\",\n" +
      "        \"sha1\" : \"514fd5edb57a71e7904da1b7ebb391c8e34b1123\"\n" + "      },\n" +
      "      \"sequence_id\" : \"0\",\n" + "      \"etag\" : \"0\",\n" +
      "      \"sha1\" : \"514fd5edb57a71e7904da1b7ebb391c8e34b1123\",\n" +
      "      \"name\" : \"emailFooter.html\"\n" + "    }\n" + "  },\n" +
      "  \"help_url\" : \"http://developers.box.com/docs/#errors\",\n" +
      "  \"message\" : \"Item with the same name already exists\",\n" +
      "  \"request_id\" : \"butdhxg1k04nqao1\"\n" + "}\n";

    BoxAPIException exception = new BoxAPIException("", 409, responseBody);

    MultiStepIntegrationDesignerDiagnostic diagnostic = new MultiStepIntegrationDesignerDiagnostic(true);

    IntegrationResponse response = this.integration.createExceptionResponse(exception, getExecutionContext(false), diagnostic);

    Assert.assertEquals("Item with the same name already exists", response.getError().getMessage());
  }

  @Test
  public void createExceptionError_item_name_invalid() {
    String responseBody = "{\n" + "  \"type\" : \"error\",\n" + "  \"status\" : 400,\n" +
      "  \"code\" : \"item_name_invalid\",\n" +
      "  \"context_info\" : \"Names cannot contain non-printable ASCII, / or \\\\, leading or trailing whitespace. The special names \\\".\\\" or \\\"..\\\" are also unsupported.\",\n" +
      "  \"help_url\" : \"http://developers.box.com/docs/#errors\",\n" +
      "  \"message\" : \"Item name invalid\",\n" + "  \"request_id\" : \"i1alexg1k70az80m\"\n" + "}\n";

    BoxAPIException exception = new BoxAPIException("", 400, responseBody);

    MultiStepIntegrationDesignerDiagnostic diagnostic = new MultiStepIntegrationDesignerDiagnostic(true);

    IntegrationResponse response = this.integration.createExceptionResponse(exception, getExecutionContext(false), diagnostic);

    Assert.assertEquals("Names cannot contain non-printable ASCII, / or \\, leading or trailing whitespace. The special names \".\" or \"..\" are also unsupported.", response.getError().getMessage());
  }

  @Test
  public void createExceptionError_not_found_invalid_parameter() {
    String responseBody = "{\n" + "  \"type\" : \"error\",\n" + "  \"status\" : 404,\n" +
      "  \"code\" : \"not_found\",\n" + "  \"context_info\" : {\n" + "    \"errors\" : [ {\n" +
      "      \"reason\" : \"invalid_parameter\",\n" + "      \"name\" : \"parent\",\n" +
      "      \"message\" : \"Invalid value '2'. 'parent' with value '2' not found\"\n" + "    } ]\n" +
      "  },\n" + "  \"help_url\" : \"http://developers.box.com/docs/#errors\",\n" +
      "  \"message\" : \"Not Found\",\n" + "  \"request_id\" : \"wtzpr8g1k71mq2xp\"\n" + "}\n";

    BoxAPIException exception = new BoxAPIException("", 409, responseBody);

    MultiStepIntegrationDesignerDiagnostic diagnostic = new MultiStepIntegrationDesignerDiagnostic(true);

    IntegrationResponse response = this.integration.createExceptionResponse(exception, getExecutionContext(false), diagnostic);

    Assert.assertEquals("Invalid value '2'. 'parent' with value '2' not found", response.getError().getMessage());
  }

  private ExecutionContext getExecutionContext(boolean isEnabled) {
    return new ExecutionContext() {
      @Override
      public Locale getDesignerLocale() {
        return Locale.getDefault();
      }

      @Override
      public Locale getExecutionLocale() {
        return Locale.getDefault();
      }

      @Override
      public boolean isDiagnosticsEnabled() {
        return isEnabled;
      }

      @Override
      public boolean hasAccessToConnectedSystem() {
        return false;
      }

      @Override
      public ProxyConfigurationData getProxyConfigurationData() {
        return null;
      }

      @Override
      public Optional<String> getAccessToken() {
        return Optional.empty();
      }

      @Override
      public int getAttemptNumber() {
        return 0;
      }
    };
  }

  class TestIntegration extends AbstractBoxIntegration {

    @Override
    protected String getOperationDescription() {
      return null;
    }

    @Override
    protected IntegrationResponse execute(
      SimpleConfiguration integrationConfiguration,
      SimpleConfiguration connectedSystemConfiguration,
      ExecutionContext executionContext) {
      return null;
    }
  }
}
