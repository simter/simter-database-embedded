package tech.simter.embeddeddatabase.mysql;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author RJ
 */
@Component
@ConfigurationProperties(prefix = "simter.embedded-database.mysql")
public class EmbeddedMysqlProperties {
  private String downloadUrl;  // default http://get.enterprisedb.com/postgresql/
  private String version;      // default 5.7.19
  private String username;     // default tester
  private String password;     // default password
  private String databaseName; // default testdb
  private Integer port;        // default free port
  private long timeout;        // default 30 seconds

  public static final String DEFAULT_DATABASE_NAME = "testdb";
  public static final String DEFAULT_USERNAME = "tester";
  public static final String DEFAULT_PASSWORD = "password";
  public static final long DEFAULT_TIMEOUT = 30L;

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

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  @Override
  public String toString() {
    return "EmbeddedMysqlProperties{" +
      "downloadUrl='" + downloadUrl + '\'' +
      ", version='" + version + '\'' +
      ", username='" + username + '\'' +
      ", password='" + password + '\'' +
      ", databaseName='" + databaseName + '\'' +
      ", port=" + port +
      ", timeout=" + timeout +
      '}';
  }
}