package com.appian.sdk.csp.box;

import java.util.Map;

import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.simplified.sdk.connectiontesting.SimpleTestableConnectedSystemTemplate;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.Choice;
import com.appian.connectedsystems.templateframework.sdk.connectiontesting.TestConnectionResult;
import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxUser;
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
  public static final String USER_TYPE = "userType";
  public static final String USER_TYPE_SERVICE_ACCOUNT_USER = "userTypeServiceAccountUser";
  public static final String USER_TYPE_APP_USER = "userTypeAppUser";
  public static final String APP_USER_ID = "appUserId";
  public static final String DEBUG = "debug";

  private static final int MAX_CACHE_ENTRIES = 100;
  private static IAccessTokenCache accessTokenCache;

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
      textProperty(USER_TYPE)
        .label("Run As")
        .choices(
          Choice.builder().name("All integrations will run as the Service Account associated with this application").value(USER_TYPE_SERVICE_ACCOUNT_USER).build(),
          Choice.builder().name("Each integration will select an App User to run as").value(USER_TYPE_APP_USER).build()
        )
        .instructionText("See https://developer.box.com/docs/user-types and https://developer.box.com/docs/service-account for more information")
        .isRequired(true)
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
      BoxDeveloperEditionAPIConnection conn = getConnection(configuration);
      BoxFolder rootFolder = BoxFolder.getRootFolder(conn);
      rootFolder.getInfo();
    } catch (Exception e) {
      TestConnectionResult.error(e.getMessage());
    }

    return TestConnectionResult.success();
  }

  public static boolean runAsServiceAccount(SimpleConfiguration connectedSystemConfiguration) {
    String userType = connectedSystemConfiguration.getValue(BoxPlatformConnectedSystem.USER_TYPE);
    return BoxPlatformConnectedSystem.USER_TYPE_SERVICE_ACCOUNT_USER.equals(userType);
  }

  public static BoxDeveloperEditionAPIConnection getConnection(SimpleConfiguration connectedSystemConfiguration, SimpleConfiguration integrationConfiguration) {
    if (runAsServiceAccount(connectedSystemConfiguration)) {
      return getConnection(connectedSystemConfiguration);
    }

    String appUserId = integrationConfiguration.getValue(APP_USER_ID);

    return BoxDeveloperEditionAPIConnection.getAppUserConnection(appUserId, getBoxConfig(connectedSystemConfiguration), getAccessTokenCache());
  }

  public static BoxDeveloperEditionAPIConnection getConnection(SimpleConfiguration connectedSystemConfiguration) {
    return BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(getBoxConfig(connectedSystemConfiguration), getAccessTokenCache());
  }

  protected static BoxConfig getBoxConfig(SimpleConfiguration connectedSystemConfiguration) {
    String clientId = connectedSystemConfiguration.getValue(CLIENT_ID);
    String clientSecret = connectedSystemConfiguration.getValue(CLIENT_SECRET);
    String publicKeyId = connectedSystemConfiguration.getValue(PUBLIC_KEY_ID);
    String privateKey = connectedSystemConfiguration.getValue(PRIVATE_KEY);
    // The key information downloaded from Box contains '\n' which are then escaped when stored, so we need to unescape them here
    if (privateKey != null) {
      privateKey = privateKey.replaceAll("\\\\n", "\n");
    }
    String passphrase = connectedSystemConfiguration.getValue(PRIVATE_KEY_PASSPHRASE);
    String enterpriseID = connectedSystemConfiguration.getValue(ENTERPRISE_ID);

    JWTEncryptionPreferences jwtPreferences = new JWTEncryptionPreferences();
    jwtPreferences.setPublicKeyID(publicKeyId);
    jwtPreferences.setPrivateKeyPassword(passphrase);
    jwtPreferences.setPrivateKey(privateKey);
    jwtPreferences.setEncryptionAlgorithm(EncryptionAlgorithm.RSA_SHA_256);

    return new BoxConfig(clientId, clientSecret, enterpriseID, jwtPreferences);
  }

  protected static IAccessTokenCache getAccessTokenCache() {
    if (accessTokenCache == null) {
      accessTokenCache = new InMemoryLRUAccessTokenCache(MAX_CACHE_ENTRIES);
    }
    return accessTokenCache;
  }

  public static void addRequestDiagnostic(Map<String,Object> requestDiagnostic, SimpleConfiguration connectedSystemConfiguration, SimpleConfiguration integrationConfiguration) {

    boolean debug = Boolean.TRUE.equals(connectedSystemConfiguration.getValue(DEBUG));

//    requestDiagnostic.put("Authentication Type", "OAuth 2 with JWT");
//    requestDiagnostic.put("Client ID", connectedSystemConfiguration.getValue(CLIENT_ID));
//    requestDiagnostic.put("Client Secret", debug ? connectedSystemConfiguration.getValue(CLIENT_SECRET) : "**********");
//    requestDiagnostic.put("Public Key ID", connectedSystemConfiguration.getValue(PUBLIC_KEY_ID));
//    requestDiagnostic.put("Private Key", debug ? connectedSystemConfiguration.getValue(PRIVATE_KEY) : "**********");
//    requestDiagnostic.put("Private Key Passphrase", debug ? connectedSystemConfiguration.getValue(PRIVATE_KEY_PASSPHRASE) : "**********");
//    requestDiagnostic.put("Enterprise ID", connectedSystemConfiguration.getValue(ENTERPRISE_ID));
//    requestDiagnostic.put("App User ID", integrationConfiguration.getValue(APP_USER_ID));

    if (runAsServiceAccount(connectedSystemConfiguration)) {
      BoxDeveloperEditionAPIConnection conn = getConnection(connectedSystemConfiguration);
      requestDiagnostic.put("Run As", "Service Account " + BoxUser.getCurrentUser(conn).getID());
    } else {
      requestDiagnostic.put("Run As", "App User " + integrationConfiguration.getValue(APP_USER_ID));
    }
  }
}
