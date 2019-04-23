package com.appian.sdk.csp.box;

import java.util.Map;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.simplified.sdk.connectiontesting.SimpleTestableConnectedSystemTemplate;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.connectiontesting.TestConnectionResult;
import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFolder;
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
  public static final String APP_USER_ID = "appUserID";
  public static final String DEBUG = "debug";

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
        .isRequired(true)
        .isImportCustomizable(true)
        .build(),
      textProperty(APP_USER_ID)
        .label("App User ID")
        .isRequired(true)
        .isImportCustomizable(true)
        .build(),
      booleanProperty(DEBUG)
        .label("Debug")
        .instructionText("Show additional debugging information, including credential information. DO NOT USE with production credentials!")
        .isImportCustomizable(true)
        .build()
    );
  }

  @Override
  protected TestConnectionResult testConnection(
    SimpleConfiguration configuration, ExecutionContext executionContext) {

    try {
      BoxDeveloperEditionAPIConnection conn = getConnection(configuration, executionContext);
      BoxFolder rootFolder = BoxFolder.getRootFolder(conn);
      rootFolder.getInfo();
    } catch (Exception e) {
      TestConnectionResult.error(e.getMessage());
    }

    return TestConnectionResult.success();
  }

  public static BoxDeveloperEditionAPIConnection getConnection(SimpleConfiguration configuration, ExecutionContext executionContext) {
    String clientID = configuration.getValue(CLIENT_ID);
    String clientSecret = configuration.getValue(CLIENT_SECRET);
    String publicKeyID = configuration.getValue(PUBLIC_KEY_ID);
    String privateKey = configuration.getValue(PRIVATE_KEY);
    // The key information downloaded from Box contains '\n' which are then escaped when stored, so we need to unescape them here
    if (privateKey != null) {
      privateKey = privateKey.replaceAll("\\\\n", "\n");
    }
    String passphrase = configuration.getValue(PRIVATE_KEY_PASSPHRASE);
    String enterpriseID = configuration.getValue(ENTERPRISE_ID);
    String appUserID = configuration.getValue(APP_USER_ID);

//    System.out.println("DEBUGGIN: " + privateKey);
//    System.out.println("HASH: " + privateKey.hashCode());

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
//    return BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig, accessTokenCache);
    return BoxDeveloperEditionAPIConnection.getAppUserConnection(appUserID, boxConfig, accessTokenCache);
  }

  public static void addRequestDiagnostic(Map<String,Object> requestDiagnostic, SimpleConfiguration connectedSystemConfiguration,
    ExecutionContext executionContext) {

    boolean debug = Boolean.TRUE.equals(connectedSystemConfiguration.getValue(DEBUG));

    requestDiagnostic.put("Authentication Type", "OAuth 2 with JWT");
    requestDiagnostic.put("Client ID", connectedSystemConfiguration.getValue(CLIENT_ID));
    requestDiagnostic.put("Client Secret", debug ? connectedSystemConfiguration.getValue(CLIENT_SECRET) : "**********");
    requestDiagnostic.put("Public Key ID", connectedSystemConfiguration.getValue(PUBLIC_KEY_ID));
    requestDiagnostic.put("Private Key", debug ? connectedSystemConfiguration.getValue(PRIVATE_KEY) : "**********");
    requestDiagnostic.put("Private Key Passphrase", debug ? connectedSystemConfiguration.getValue(PRIVATE_KEY_PASSPHRASE) : "**********");
    requestDiagnostic.put("Enterprise ID", connectedSystemConfiguration.getValue(ENTERPRISE_ID));
    requestDiagnostic.put("App User ID", connectedSystemConfiguration.getValue(APP_USER_ID));
  }
}
