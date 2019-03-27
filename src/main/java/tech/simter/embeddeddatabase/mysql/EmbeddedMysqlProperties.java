package tech.simter.embeddeddatabase.mysql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author RJ
 */
@Component
@ConfigurationProperties(prefix = "simter.embedded-database.mysql")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Wither
@Builder(toBuilder = true)
public class EmbeddedMysqlProperties {
  private String downloadUrl;  // default http://get.enterprisedb.com/postgresql/
  private String version;      // default 5.7.19
  private String username;     // default tester
  private String password;     // default password
  private String databaseName; // default testdb
  private int port;            // default free port
  private long timeout;        // default 30 seconds

  public static final String DEFAULT_DATABASE_NAME = "testdb";
  public static final String DEFAULT_USERNAME = "tester";
  public static final String DEFAULT_PASSWORD = "password";
  public static final long DEFAULT_TIMEOUT = 30L;
}