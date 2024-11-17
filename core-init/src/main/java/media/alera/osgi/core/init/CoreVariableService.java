package media.alera.osgi.core.init;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.annotations.GogoCommand;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;

/**
 * Variable Service used to retrieve .env variabels that are set for the application
 *
 * Do not use \@SLF4J annotation as that will cause the logger to load too early
 */
@Component(enabled = true, immediate = true, service = { CoreVariableService.class })
@GogoCommand(scope = "core", function = { "listEnvs", "listProps", "setEnv" })
public class CoreVariableService {

  public static final String CORE_APP_WORKING_FOLDER = "CORE_APP_WORKING_FOLDER";
  public static final String CORE_APP_WORKING_FOLDER_PROPERTY = "media.alera.osgi.core.working.folder";
  public static final String CORE_APP_ENV_FOLDER_PROPERTY = "media.alera.osgi.core.env.folder";
  public static final String CORE_APP_ENV_NAME_PROPERTY = "media.alera.osgi.core.env.name";

  public static final String CORE_APP_CONF_FOLDER = "CORE_APP_CONF_FOLDER";

  public static final String CORE_WEB_HTTPS_PORT = "CORE_WEB_HTTPS_PORT";
  public static final String CORE_WEB_HTTP_PORT = "CORE_WEB_HTTP_PORT";
  public static final String CORE_WEB_SECURE = "CORE_WEB_SECURE";
  public static final String CORE_WEB_KEYSTORE_FILE = "CORE_WEB_KEYSTORE_FILE";
  public static final String CORE_WEB_KEYSTORE_PASSWORD = "CORE_WEB_KEYSTORE_PASSWORD";
  public static final String CORE_WEB_KEYSTORE_TYPE = "CORE_WEB_KEYSTORE_TYPE";

  public static final String CORE_APP_KEYSTORE_PASSWORD = "CORE_APP_KEYSTORE_PASSWORD";
  public static final String CORE_APP_KEYSTORE_FILE = "CORE_APP_KEYSTORE_FILE";
  public static final String CORE_APP_KEYSTORE_TYPE = "CORE_APP_KEYSTORE_TYPE";
  public static final String CORE_APP_TRUSTSTORE_PASSWORD = "CORE_APP_TRUSTSTORE_PASSWORD";
  public static final String CORE_APP_TRUSTSTORE_FILE = "CORE_APP_TRUSTSTORE_FILE";
  public static final String CORE_APP_TRUSTSTORE_TYPE = "CORE_APP_TRUSTSTORE_TYPE";
  public static final String CORE_APP_KEY_TYPE_DEFAULT = "pkcs12";

  public static final String CORE_APP_LOGBACK_FILE = "CORE_APP_LOGBACK_FILE";

  public static final String CORE_APP_SSH_LOGIN_CONFIG = "CORE_APP_SSH_LOGIN_CONFIG";
  public static final String CORE_APP_SSH_JAAS_FILE = "CORE_APP_SSH_JAAS_FILE";
  public static final String CORE_APP_SSH_KEYSTORE = "CORE_APP_SSH_KEYSTORE";
  public static final String CORE_APP_SSH_KEYSTORE_PASSWORD = "CORE_APP_SSH_KEYSTORE_PASSWORD";
  public static final String CORE_APP_SSH_PORT = "CORE_APP_SSH_PORT";
  public static final String CORE_APP_SSH_DEFAULT_STORAGE = "CORE_APP_SSH_DEFAULT_STORAGE";
  public static final String CORE_APP_SSH_AUTH_KEYS = "CORE_APP_SSH_AUTH_KEYS";

  public static final String RUNTIME_UUID = UUID.randomUUID().toString();

  private static CoreVariableService instance = null;

  private Dotenv dotenv;

  @Activate
  public void activate() {
    // Do not log anything until logback.xml value is set

    try {
      //"media.alera.osgi.core.env"
      DotenvBuilder builder = Dotenv.configure();

      boolean requireEnv = false;
      String envFolder = null;
      String envFile = null;
      if (System.getProperty(CORE_APP_ENV_FOLDER_PROPERTY) != null) {
        builder.directory(System.getProperty(CORE_APP_ENV_FOLDER_PROPERTY));
        envFolder = System.getProperty(CORE_APP_ENV_FOLDER_PROPERTY);
        requireEnv = true;
      }
      if (System.getProperty(CORE_APP_ENV_NAME_PROPERTY) != null) {
        builder.filename(System.getProperty(CORE_APP_ENV_NAME_PROPERTY));
        envFile = System.getProperty(CORE_APP_ENV_NAME_PROPERTY);
        requireEnv = true;
      }
      if (!requireEnv) {
        builder.ignoreIfMissing();
      }
      this.dotenv = builder.load();

      IPath confFolder = getConfFolder();

      // Web Server Properties
      setPropertyForEnv(this.dotenv, "org.osgi.service.http.port.secure", CORE_WEB_HTTPS_PORT, "8443");
      setPropertyForEnv(this.dotenv, "org.osgi.service.http.port", CORE_WEB_HTTP_PORT);
      setPropertyForEnv(this.dotenv, "org.apache.felix.https.enable", CORE_WEB_SECURE, "true");
      setPropertyForEnv(this.dotenv, "org.apache.felix.https.keystore", CORE_WEB_KEYSTORE_FILE);
      setPropertyForEnv(this.dotenv, "org.apache.felix.https.keystore.password", CORE_WEB_KEYSTORE_PASSWORD);
      setPropertyForEnv(this.dotenv, "org.apache.felix.https.keystore.type", CORE_WEB_KEYSTORE_TYPE, CORE_APP_KEY_TYPE_DEFAULT);

      // Application Properties
      setPropertyForEnv(this.dotenv, "javax.net.ssl.keyStorePassword", CORE_APP_KEYSTORE_PASSWORD);
      setPropertyForEnv(this.dotenv, "javax.net.ssl.keyStore", CORE_APP_KEYSTORE_FILE);
      setPropertyForEnv(this.dotenv, "javax.net.ssl.keyStoreType", CORE_APP_KEYSTORE_TYPE, CORE_APP_KEY_TYPE_DEFAULT);
      setPropertyForEnv(this.dotenv, "javax.net.ssl.trustStorePassword", CORE_APP_TRUSTSTORE_PASSWORD);
      setPropertyForEnv(this.dotenv, "javax.net.ssl.trustStore", CORE_APP_TRUSTSTORE_FILE);
      setPropertyForEnv(this.dotenv, "javax.net.ssl.trustStoreType", CORE_APP_TRUSTSTORE_TYPE, CORE_APP_KEY_TYPE_DEFAULT);

      // Set log file variable if the file exists
      String logFilePath = getAppendedPath(confFolder, "logback.xml");
      String defaultPath = null;
      if (Files.exists(new File(logFilePath).toPath())) {
        defaultPath = logFilePath;
      }

      String customPath = null;
      logFilePath = this.dotenv.get(CORE_APP_LOGBACK_FILE);
      if (logFilePath != null && !logFilePath.isBlank()) {
        if (Files.exists(new File(logFilePath).toPath())) {
          customPath = logFilePath;
        }
      }
      if (customPath != null) {
        System.setProperty("logback.configurationFile", customPath);
      } else if (defaultPath != null) {
        System.setProperty("logback.configurationFile", defaultPath);
      }

      // Secure Console Properties
      setPropertyForEnv(this.dotenv, "java.security.auth.login.config", CORE_APP_SSH_LOGIN_CONFIG, getAppendedPath(confFolder, "org.eclipse.equinox.console.authentication.config"));
      setPropertyForEnv(this.dotenv, "org.eclipse.equinox.console.jaas.file", CORE_APP_SSH_JAAS_FILE, getAppendedPath(confFolder, "store"));
      setPropertyForEnv(this.dotenv, "ssh.server.keystore", CORE_APP_SSH_KEYSTORE, getAppendedPath(confFolder, "hostkey.ser"));
      setPropertyForEnv(this.dotenv, "ssh.server.keystore.password", CORE_APP_SSH_KEYSTORE_PASSWORD);
      setPropertyForEnv(this.dotenv, "osgi.console.ssh", CORE_APP_SSH_PORT, "2222");
      setPropertyForEnv(this.dotenv, "osgi.console.ssh.useDefaultSecureStorage", CORE_APP_SSH_DEFAULT_STORAGE, "false");
      setPropertyForEnv(this.dotenv, "ssh.server.authorized_keys", CORE_APP_SSH_AUTH_KEYS, getAppendedPath(confFolder, "equinox_authorized_keys"));

      // Log information about setup below here
      if (envFolder != null) {
        LoggerFactory.getLogger(getClass()).info("'.env' folder set to: {}", envFolder);
      }
      if (envFile != null) {
        LoggerFactory.getLogger(getClass()).info("'.env' filename set to: {}", envFile);
      }
      if (customPath != null) {
        LoggerFactory.getLogger(getClass()).info("Custom logback.xml path was set to: {}", customPath);
      } else if (defaultPath != null) {
        LoggerFactory.getLogger(getClass()).info("Default logback.xml path was set to: {}", defaultPath);
      }
      if (LoggerFactory.getLogger(getClass()).isInfoEnabled()) {
        LoggerFactory.getLogger(getClass()).info("conf folder set to: {}", confFolder.toOSString());
      }

      instance = this;

      // Make sure the org.eclipse.equinox.console.ssh bundle is started
      org.eclipse.equinox.console.ssh.Activator.class.toGenericString();

    } catch (Exception ex) {
      // Log exception to logger and then throw it
      LoggerFactory.getLogger(getClass()).error("Failed to start variable service", ex);
      throw ex;
    }
  }

  public static CoreVariableService getDefault() {
    return instance;
  }

  public String getEnvSystemProperty(final String name) {
    return getEnvSystemProperty(name, null);
  }

  public String getEnvSystemProperty(final String name, final String defaultValue) {
    String env = this.dotenv.get(name);
    if (env != null && !env.isBlank()) {
      return env;
    }

    String property = System.getProperty(name);
    if (property != null && !property.isBlank()) {
      return property;
    }

    return defaultValue;
  }

  public IPath getAppFolder() {
    String appFolder = this.dotenv.get(CORE_APP_WORKING_FOLDER);
    if (appFolder == null || appFolder.isBlank()) {
      appFolder = System.getProperty(CORE_APP_WORKING_FOLDER_PROPERTY, "");
    }
    return new Path(appFolder);
  }

  public IPath getConfFolder() {
    String confFolderValue = this.dotenv.get(CORE_APP_CONF_FOLDER);
    final IPath confFolder;
    if (confFolderValue == null || confFolderValue.isBlank()) {
      confFolder = getAppFolder().append("conf");
    } else {
      confFolder = new Path(confFolderValue);
    }
    return confFolder;
  }

  public String getAppendedPath(final IPath path, final String file) {
    return path.append(file).toPortableString();
  }

  public IPath getAppendedPathAsPath(final IPath path, final String file) {
    return path.append(file);
  }

  @Descriptor("List env and .env variables")
  public void listEnvs(final CommandSession session) throws Exception {
    this.dotenv.entries().stream().sorted((k, m) -> k.getKey().compareToIgnoreCase(m.getKey()))
    .map(n -> {
      if (n.getKey().toUpperCase().contains("PASSWORD")) {
        return String.format("%s = %s%n", n.getKey(), "*********");
      }
      return String.format("%s = %s%n", n.getKey(), n.getValue());
    }).forEach(k -> session.getConsole().printf(k));
  }

  @Descriptor("List env and .env variables")
  public void listEnvsText(final CommandSession session) throws Exception {
    this.dotenv.entries().stream().sorted((k, m) -> k.getKey().compareToIgnoreCase(m.getKey()))
    .map(n -> String.format("%s = %s%n", n.getKey(), n.getValue())).forEach(k -> session.getConsole().printf(k));
  }

  @Descriptor("List all system properties")
  public void listProps(final CommandSession session) throws Exception {
    System.getProperties().entrySet().stream().sorted((k,m) -> ((String)k.getKey()).compareToIgnoreCase((String)m.getKey()))
    .forEach(k -> session.getConsole().printf("%s = %s%n", k.getKey(), k.getValue()));
  }

  @Descriptor("Set an system property")
  public void setEnv(final CommandSession session, final String name, final String value) {
    System.setProperty(name, value);
    if (session != null) {
      session.getConsole().printf("Set %s = %s%n", name, value);
    }
  }

  private void setPropertyForEnv(final Dotenv dotenv, final String propName, final String envName, final String defaultValue) {
    String value = dotenv.get(envName);
    if (value != null && !value.isBlank()) {
      System.setProperty(propName, value);
    } else if (defaultValue != null && !defaultValue.isBlank()) {
      System.setProperty(propName, defaultValue);
    }
  }

  private void setPropertyForEnv(final Dotenv dotenv, final String propName, final String envName) {
    setPropertyForEnv(dotenv, propName, envName, null);
  }

  public String getRuntimeUUID() {
    return RUNTIME_UUID;
  }

  public Optional<String> getEnvSystemPropertyFileContent(final String name) {
    return getEnvSystemPropertyFileContent(name, null);
  }

  public Optional<String> getEnvSystemPropertyFileContent(final String name, final String defaultValue) {
    String fileName = getEnvSystemProperty(name, defaultValue);
    if (fileName == null) {
      return Optional.empty();
    }
    IPath confFolder = getConfFolder();
    IPath filePath = getAppendedPathAsPath(confFolder, fileName);
    java.nio.file.Path path = filePath.toPath();

    if (Files.exists(path)) {
      try {
        return Optional.of(Files.readString(path, StandardCharsets.UTF_8));
      } catch (IOException ioEx) {
        LoggerFactory.getLogger(getClass()).error("Failed to read file: {}", path, ioEx);
      }
    }
    return Optional.empty();
  }

}
