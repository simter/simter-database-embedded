package tech.simter.embeddeddatabase.postgres;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.file.Paths;
import java.util.List;

/**
 * See {@link org.springframework.boot.autoconfigure.jdbc.DataSourceProperties}.
 * <p>
 * See <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-kotlin.html#boot-features-kotlin-configuration-properties">'50.5 @ConfigurationProperties'</a>.
 *
 * @author RJ
 */
@Component
@ConfigurationProperties(prefix = "simter.embedded-database.postgres")
public class EmbeddedPostgresProperties {
  private String downloadUrl;  // default http://get.enterprisedb.com/postgresql/
  private String version;      // default 10.6-1
  private String username;     // default tester
  private String password;     // default password
  private String databaseName; // default testdb
  private String host;         // default localhost
  private Integer port;        // default free port
  private String dataDir;      // default target/pg-data
  private String extractedDir; // default {user-home}/.embedpostgresql/extracted/
  private List<String> additionalInitDbParams;
  private List<String> additionalPostgresParams;

  public static final String DEFAULT_DATABASE_NAME = "testdb";
  public static final String DEFAULT_USERNAME = "tester";
  public static final String DEFAULT_PASSWORD = "password";
  public static final String DEFAULT_EXTRACTED_DIR = Paths.get(System.getProperty("user.home"), ".embedpostgresql", "extracted").toString();

  public String getDownloadUrl() {
    return downloadUrl;
  }

  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public String getDataDir() {
    return dataDir;
  }

  public void setDataDir(String dataDir) {
    this.dataDir = dataDir;
  }

  public String getExtractedDir() {
    return extractedDir;
  }

  public void setExtractedDir(String extractedDir) {
    this.extractedDir = extractedDir;
  }

  public List<String> getAdditionalInitDbParams() {
    return additionalInitDbParams;
  }

  public void setAdditionalInitDbParams(List<String> additionalInitDbParams) {
    this.additionalInitDbParams = additionalInitDbParams;
  }

  public List<String> getAdditionalPostgresParams() {
    return additionalPostgresParams;
  }

  public void setAdditionalPostgresParams(List<String> additionalPostgresParams) {
    this.additionalPostgresParams = additionalPostgresParams;
  }

  @Override
  public String toString() {
    return "EmbeddedPostgresProperties{" +
      "downloadUrl='" + downloadUrl + '\'' +
      ", version='" + version + '\'' +
      ", username='" + username + '\'' +
      ", password='" + password + '\'' +
      ", databaseName='" + databaseName + '\'' +
      ", host='" + host + '\'' +
      ", port=" + port +
      ", dataDir='" + dataDir + '\'' +
      ", extractedDir='" + extractedDir + '\'' +
      ", additionalInitDbParams=" + StringUtils.collectionToDelimitedString(additionalInitDbParams, "\r\n  ") +
      ", additionalPostgresParams=" + StringUtils.collectionToDelimitedString(additionalPostgresParams, "\r\n  ") +
      '}';
  }
}