package tech.simter.embeddeddatabase.postgres;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
@Data
@AllArgsConstructor
@NoArgsConstructor
@Wither
@Builder(toBuilder = true)
public class EmbeddedPostgresProperties {
  private String downloadUrl;  // default http://get.enterprisedb.com/postgresql/
  private String version;      // default 10.6-1
  private String username;     // default test
  private String password;     // default password
  private String databaseName; // default testdb
  private String host;         // default localhost
  private int port;            // default free port
  private String dataDir;      // default target/pg-data
  private String extractedDir; // default {user-home}/.embedpostgresql/extracted/
  private List<String> additionalInitDbParams;
  private List<String> additionalPostgresParams;

  public static final String DEFAULT_DATABASE_NAME = "testdb";
  public static final String DEFAULT_USERNAME = "tester";
  public static final String DEFAULT_PASSWORD = "password";
  public static final String DEFAULT_EXTRACTED_DIR = Paths.get(System.getProperty("user.home"), ".embedpostgresql", "extracted").toString();
}