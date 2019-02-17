package com.appian.sdk.csp.box;

import java.util.Map;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.simplified.sdk.connectiontesting.SimpleTestableConnectedSystemTemplate;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.connectiontesting.TestConnectionResult;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.EncryptionAlgorithm;
import com.box.sdk.IAccessTokenCache;
import com.box.sdk.InMemoryLRUAccessTokenCache;
import com.box.sdk.JWTEncryptionPreferences;

@TemplateId(name="BoxPlatformConnectedSystem")
public class BoxPlatformConnectedSystem extends SimpleTestableConnectedSystemTemplate {

  public static final String CLIENT_ID = "clientID";
  public static final String CLIENT_SECRET = "clientSecret";
  public static final String PUBLIC_KEY_ID = "publicKeyID";
  public static final String PRIVATE_KEY = "privateKey";
  public static final String PRIVATE_KEY_PASSPHRASE = "passphrase";
  public static final String ENTERPRISE_ID = "enterpriseID";

  @Override
  protected SimpleConfiguration getConfiguration(
      SimpleConfiguration simpleConfiguration, ExecutionContext executionContext) {

    return simpleConfiguration.setProperties(
      textProperty(CLIENT_ID)
        .label("Client ID")
        .description("The Client ID of the application")
        .isRequired(true)
        .isImportCustomizable(true)
        .build(),
      encryptedTextProperty(CLIENT_SECRET)
        .label("Client Secret")
        .isRequired(true)
        .isImportCustomizable(true)
        .build(),
      textProperty(PUBLIC_KEY_ID)
        .label("Public Key ID")
        .isRequired(true)
        .isImportCustomizable(true)
        .build(),
      encryptedTextProperty(PRIVATE_KEY)
        .label("Private Key")
        .isRequired(true)
        .isImportCustomizable(true)
        .build(),
      encryptedTextProperty(PRIVATE_KEY_PASSPHRASE)
        .label("Private Key Passphrase")
        .isRequired(true)
        .isImportCustomizable(true)
        .build(),
      textProperty(ENTERPRISE_ID)
        .label("Enterprise ID")
        .masked(true)
        .isRequired(true)
        .isImportCustomizable(true)
        .build()
    );
  }

  @Override
  protected TestConnectionResult testConnection(
    SimpleConfiguration configuration, ExecutionContext executionContext) {

    try {
      String clientID = configuration.getValue(CLIENT_ID);
      String clientSecret = configuration.getValue(CLIENT_SECRET);
      String publicKeyID = configuration.getValue(PUBLIC_KEY_ID);
      String privateKey = configuration.getValue(PRIVATE_KEY);
      String passphrase = configuration.getValue(PRIVATE_KEY_PASSPHRASE);
      String enterpriseID = configuration.getValue(ENTERPRISE_ID);

      JWTEncryptionPreferences jwtPreferences = new JWTEncryptionPreferences();
      jwtPreferences.setPublicKeyID(publicKeyID);
      jwtPreferences.setPrivateKeyPassword(passphrase);
      jwtPreferences.setPrivateKey(privateKey);
      jwtPreferences.setEncryptionAlgorithm(EncryptionAlgorithm.RSA_SHA_256);

      BoxConfig boxConfig = new BoxConfig(clientID, clientSecret, enterpriseID, jwtPreferences);

      // Set cache info
      int MAX_CACHE_ENTRIES = 100;
      IAccessTokenCache accessTokenCache = new InMemoryLRUAccessTokenCache(MAX_CACHE_ENTRIES);

      // Create new app enterprise connection object
      BoxDeveloperEditionAPIConnection client = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(
        boxConfig, accessTokenCache);
    } catch (Exception e) {
      TestConnectionResult.error(e.getMessage());
    }

    return TestConnectionResult.success();
  }

  protected static void addRequestDiagnostics(Map<String,Object> requestDiagnostic, SimpleConfiguration connectedSystemConfiguration,
    ExecutionContext executionContext) {

    requestDiagnostic.put("Authentication Type", "OAuth 2 with JWT");
    requestDiagnostic.put("Client ID", connectedSystemConfiguration.getValue(CLIENT_ID));
    requestDiagnostic.put("Client Secret", "**********");
    requestDiagnostic.put("Public Key ID", connectedSystemConfiguration.getValue(PUBLIC_KEY_ID));
    requestDiagnostic.put("Private Key", "**********");
    requestDiagnostic.put("Private Key Passphrase", "**********");
    requestDiagnostic.put("Enterprise ID", connectedSystemConfiguration.getValue(ENTERPRISE_ID));
  }
}
