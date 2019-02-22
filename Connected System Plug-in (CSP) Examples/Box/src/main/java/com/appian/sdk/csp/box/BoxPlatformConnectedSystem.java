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
        .masked(true)
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
//    String privateKey = "-----BEGIN ENCRYPTED PRIVATE KEY-----\nMIIFDjBABgkqhkiG9w0BBQ0wMzAbBgkqhkiG9w0BBQwwDgQIe5iEw2hMD8MCAggA\nMBQGCCqGSIb3DQMHBAiRMtrKGmIJNgSCBMjtuX4IlPE6CycquuPIgJONYn85V2kV\n/GOTJo5ypABAQ1k295N9/1csEIu5s5U2aGy1aDvtwcxzvdC0EGDCLd4CKni5UOvk\nE2HwfLgNCzzT6nB0EZ7bw4nJ6B3OhWzA4jyBJ90YoQeRwl7PuWTmAHRCd60YKiPd\nYBIWATtwa0QjzFOoaN2M4zOKl8YVhfNyWcoMOGy0yj+2fHPRD4Xncw1aLzlz+anJ\nNXiHsUPr4Nwi0RnxNat/jAlA4dsjlUQRBPIZdxPGc/25dqr3kD6nqg5C34VlPS4a\n2dVT3FJFWW3ghDGVISVyDC61AhXKIuWQowhQhWE/dTACwVF2ZYuV1TPyC+Papyr6\nL6hcft5Vl2WTybWUB14f1ByMOri0F/AylHQbln2p6lXvt1pji8Vgmf+RlN34/3cG\nTwv1WYaYZE7/Dp8GYtmB3Q4jJONgHDjL9wIGxdkKTNdjL39iLxNY6JOfHeNgnzPf\n5B8br4+ZsOd7Ut7JwKCB3xLUsSolZvxz8TiLU5vY2BfwS/5L+mTihMlr56fJt/P0\ne4dvSjXb5Z7lruqGzAaRj9rT5qKpD6doipq2sp6nVSu5hRF5XkxTyV6MvVQH4vH8\nfKq6Npy5RMW9tl9StO7c+W7MJVXpL9CoGHwWkgGVo5+jaP78klTLP7SgPA4qCyHo\nvCX4cwwQAawkI92vANpOsFkc3vpQd8MXMCgpDgmXf5ET6MFuGRp1p4f0ffWjqzgr\nXYIUF1qR756G6/PWQOS3FzURn6u20VoGpzHe1PgLQvELGTGtXN5tT4YQKdRjXPLA\nPsA9k141uz6VmQ+K+PqEmdM/iXCJaDqykv65ryQUr3SacppN7K5U0tvtZNyZ42Kw\n3I0uVC0eGT3Fbq8a79NhjRtvgJzdNXIHuf0u1055dYLr31skfaSGzXQ16NnSlG+F\nc4kgSUvqruiyg92mE1IS8QZFa62I8SkHRaapyUazCVEAYYIzXk/XxmPWkK6tjUDo\nLNYzm8pFdJBEh4huor8MGQPgDSXaj3IZxdY3wDdZaIvxrR35VQQJE1IJTMZbMSIl\ntMlckGSB4gE268UdhqlBygS7xjdsPy57jN3sn9IubgyRw0l1uGTixa+kSDfuQ7bt\nCuVk80C/iYX58XligVK1yhmPmBzoi4xCGNnE8BX6ktY0IANyj/1JA+9fjnhVbevk\nB3dcvpgtu78T/X82mG9uS7s+hi+ShrKKM9/s5yllM5CZyAJfv/s/pvRGGKDexlBm\nQFK+Sva4YIWgawBAz3cjcLPPzLP+IiQqP/PyjQAzR4EBiBcS0ZjvUMRFA4vTxjQl\n8ogeR/XRzHV3iZNGJb4BddQHjxgeysiOoA7+SH/DWDS/Z0uGM5jWVitRi7ov9uKn\nlIbMLouWL8y+CiPpovh8o1LvB0474p0OO64T0rsiPXErYlXUO2S9jkl4owKFbi5o\nFVVzMUvxYJxUkfMRzwQXhSsR3PFpR8zugFfpHJOGjmuNK0mZMFRTZLkSGvCJRmXM\nE6HBXp19K/qgCWZQrtEujs1FiVIhV+WJuPANW48eGXTuR5dUbj6ye7z1dH19SPGU\nGCfkzkHt/QpZbA/yGVcaRybkvNnW8IwFxaj+NeMC0lY43QrtfKt5Qd6WUzoBc6QT\nPgo=\n-----END ENCRYPTED PRIVATE KEY-----\n";
    /*
-----BEGIN ENCRYPTED PRIVATE KEY-----\nMIIFDjBABgkqhkiG9w0BBQ0wMzAbBgkqhkiG9w0BBQwwDgQIe5iEw2hMD8MCAggA\nMBQGCCqGSIb3DQMHBAiRMtrKGmIJNgSCBMjtuX4IlPE6CycquuPIgJONYn85V2kV\n/GOTJo5ypABAQ1k295N9/1csEIu5s5U2aGy1aDvtwcxzvdC0EGDCLd4CKni5UOvk\nE2HwfLgNCzzT6nB0EZ7bw4nJ6B3OhWzA4jyBJ90YoQeRwl7PuWTmAHRCd60YKiPd\nYBIWATtwa0QjzFOoaN2M4zOKl8YVhfNyWcoMOGy0yj+2fHPRD4Xncw1aLzlz+anJ\nNXiHsUPr4Nwi0RnxNat/jAlA4dsjlUQRBPIZdxPGc/25dqr3kD6nqg5C34VlPS4a\n2dVT3FJFWW3ghDGVISVyDC61AhXKIuWQowhQhWE/dTACwVF2ZYuV1TPyC+Papyr6\nL6hcft5Vl2WTybWUB14f1ByMOri0F/AylHQbln2p6lXvt1pji8Vgmf+RlN34/3cG\nTwv1WYaYZE7/Dp8GYtmB3Q4jJONgHDjL9wIGxdkKTNdjL39iLxNY6JOfHeNgnzPf\n5B8br4+ZsOd7Ut7JwKCB3xLUsSolZvxz8TiLU5vY2BfwS/5L+mTihMlr56fJt/P0\ne4dvSjXb5Z7lruqGzAaRj9rT5qKpD6doipq2sp6nVSu5hRF5XkxTyV6MvVQH4vH8\nfKq6Npy5RMW9tl9StO7c+W7MJVXpL9CoGHwWkgGVo5+jaP78klTLP7SgPA4qCyHo\nvCX4cwwQAawkI92vANpOsFkc3vpQd8MXMCgpDgmXf5ET6MFuGRp1p4f0ffWjqzgr\nXYIUF1qR756G6/PWQOS3FzURn6u20VoGpzHe1PgLQvELGTGtXN5tT4YQKdRjXPLA\nPsA9k141uz6VmQ+K+PqEmdM/iXCJaDqykv65ryQUr3SacppN7K5U0tvtZNyZ42Kw\n3I0uVC0eGT3Fbq8a79NhjRtvgJzdNXIHuf0u1055dYLr31skfaSGzXQ16NnSlG+F\nc4kgSUvqruiyg92mE1IS8QZFa62I8SkHRaapyUazCVEAYYIzXk/XxmPWkK6tjUDo\nLNYzm8pFdJBEh4huor8MGQPgDSXaj3IZxdY3wDdZaIvxrR35VQQJE1IJTMZbMSIl\ntMlckGSB4gE268UdhqlBygS7xjdsPy57jN3sn9IubgyRw0l1uGTixa+kSDfuQ7bt\nCuVk80C/iYX58XligVK1yhmPmBzoi4xCGNnE8BX6ktY0IANyj/1JA+9fjnhVbevk\nB3dcvpgtu78T/X82mG9uS7s+hi+ShrKKM9/s5yllM5CZyAJfv/s/pvRGGKDexlBm\nQFK+Sva4YIWgawBAz3cjcLPPzLP+IiQqP/PyjQAzR4EBiBcS0ZjvUMRFA4vTxjQl\n8ogeR/XRzHV3iZNGJb4BddQHjxgeysiOoA7+SH/DWDS/Z0uGM5jWVitRi7ov9uKn\nlIbMLouWL8y+CiPpovh8o1LvB0474p0OO64T0rsiPXErYlXUO2S9jkl4owKFbi5o\nFVVzMUvxYJxUkfMRzwQXhSsR3PFpR8zugFfpHJOGjmuNK0mZMFRTZLkSGvCJRmXM\nE6HBXp19K/qgCWZQrtEujs1FiVIhV+WJuPANW48eGXTuR5dUbj6ye7z1dH19SPGU\nGCfkzkHt/QpZbA/yGVcaRybkvNnW8IwFxaj+NeMC0lY43QrtfKt5Qd6WUzoBc6QT\nPgo=\n-----END ENCRYPTED PRIVATE KEY-----\n

-----BEGIN ENCRYPTED PRIVATE KEY-----
MIIFDjBABgkqhkiG9w0BBQ0wMzAbBgkqhkiG9w0BBQwwDgQIe5iEw2hMD8MCAggA
MBQGCCqGSIb3DQMHBAiRMtrKGmIJNgSCBMjtuX4IlPE6CycquuPIgJONYn85V2kV
/GOTJo5ypABAQ1k295N9/1csEIu5s5U2aGy1aDvtwcxzvdC0EGDCLd4CKni5UOvk
E2HwfLgNCzzT6nB0EZ7bw4nJ6B3OhWzA4jyBJ90YoQeRwl7PuWTmAHRCd60YKiPd
YBIWATtwa0QjzFOoaN2M4zOKl8YVhfNyWcoMOGy0yj+2fHPRD4Xncw1aLzlz+anJ
NXiHsUPr4Nwi0RnxNat/jAlA4dsjlUQRBPIZdxPGc/25dqr3kD6nqg5C34VlPS4a
2dVT3FJFWW3ghDGVISVyDC61AhXKIuWQowhQhWE/dTACwVF2ZYuV1TPyC+Papyr6
L6hcft5Vl2WTybWUB14f1ByMOri0F/AylHQbln2p6lXvt1pji8Vgmf+RlN34/3cG
Twv1WYaYZE7/Dp8GYtmB3Q4jJONgHDjL9wIGxdkKTNdjL39iLxNY6JOfHeNgnzPf
5B8br4+ZsOd7Ut7JwKCB3xLUsSolZvxz8TiLU5vY2BfwS/5L+mTihMlr56fJt/P0
e4dvSjXb5Z7lruqGzAaRj9rT5qKpD6doipq2sp6nVSu5hRF5XkxTyV6MvVQH4vH8
fKq6Npy5RMW9tl9StO7c+W7MJVXpL9CoGHwWkgGVo5+jaP78klTLP7SgPA4qCyHo
vCX4cwwQAawkI92vANpOsFkc3vpQd8MXMCgpDgmXf5ET6MFuGRp1p4f0ffWjqzgr
XYIUF1qR756G6/PWQOS3FzURn6u20VoGpzHe1PgLQvELGTGtXN5tT4YQKdRjXPLA
PsA9k141uz6VmQ+K+PqEmdM/iXCJaDqykv65ryQUr3SacppN7K5U0tvtZNyZ42Kw
3I0uVC0eGT3Fbq8a79NhjRtvgJzdNXIHuf0u1055dYLr31skfaSGzXQ16NnSlG+F
c4kgSUvqruiyg92mE1IS8QZFa62I8SkHRaapyUazCVEAYYIzXk/XxmPWkK6tjUDo
LNYzm8pFdJBEh4huor8MGQPgDSXaj3IZxdY3wDdZaIvxrR35VQQJE1IJTMZbMSIl
tMlckGSB4gE268UdhqlBygS7xjdsPy57jN3sn9IubgyRw0l1uGTixa+kSDfuQ7bt
CuVk80C/iYX58XligVK1yhmPmBzoi4xCGNnE8BX6ktY0IANyj/1JA+9fjnhVbevk
B3dcvpgtu78T/X82mG9uS7s+hi+ShrKKM9/s5yllM5CZyAJfv/s/pvRGGKDexlBm
QFK+Sva4YIWgawBAz3cjcLPPzLP+IiQqP/PyjQAzR4EBiBcS0ZjvUMRFA4vTxjQl
8ogeR/XRzHV3iZNGJb4BddQHjxgeysiOoA7+SH/DWDS/Z0uGM5jWVitRi7ov9uKn
lIbMLouWL8y+CiPpovh8o1LvB0474p0OO64T0rsiPXErYlXUO2S9jkl4owKFbi5o
FVVzMUvxYJxUkfMRzwQXhSsR3PFpR8zugFfpHJOGjmuNK0mZMFRTZLkSGvCJRmXM
E6HBXp19K/qgCWZQrtEujs1FiVIhV+WJuPANW48eGXTuR5dUbj6ye7z1dH19SPGU
GCfkzkHt/QpZbA/yGVcaRybkvNnW8IwFxaj+NeMC0lY43QrtfKt5Qd6WUzoBc6QT
Pgo=
-----END ENCRYPTED PRIVATE KEY-----
     */
    String passphrase = configuration.getValue(PRIVATE_KEY_PASSPHRASE);
    String enterpriseID = configuration.getValue(ENTERPRISE_ID);

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
    return BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig, accessTokenCache);
  }

  public static void addRequestDiagnostics(Map<String,Object> requestDiagnostic, SimpleConfiguration connectedSystemConfiguration,
    ExecutionContext executionContext) {

    boolean debug = Boolean.TRUE.equals(connectedSystemConfiguration.getValue(DEBUG));

    requestDiagnostic.put("Authentication Type", "OAuth 2 with JWT");
    requestDiagnostic.put("Client ID", connectedSystemConfiguration.getValue(CLIENT_ID));
    requestDiagnostic.put("Client Secret", debug ? connectedSystemConfiguration.getValue(CLIENT_SECRET) : "**********");
    requestDiagnostic.put("Public Key ID", connectedSystemConfiguration.getValue(PUBLIC_KEY_ID));
    requestDiagnostic.put("Private Key", debug ? connectedSystemConfiguration.getValue(PRIVATE_KEY) : "**********");
    requestDiagnostic.put("Private Key Passphrase", debug ? connectedSystemConfiguration.getValue(PRIVATE_KEY_PASSPHRASE) : "**********");
    requestDiagnostic.put("Enterprise ID", connectedSystemConfiguration.getValue(ENTERPRISE_ID));
  }
}
