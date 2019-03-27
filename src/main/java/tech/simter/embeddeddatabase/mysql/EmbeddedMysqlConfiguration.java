package tech.simter.embeddeddatabase.mysql;

import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.config.DownloadConfig;
import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.config.SchemaConfig;
import com.wix.mysql.distribution.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.wix.mysql.EmbeddedMysql.anEmbeddedMysql;
import static com.wix.mysql.config.Charset.UTF8;
import static com.wix.mysql.config.DownloadConfig.aDownloadConfig;
import static com.wix.mysql.config.MysqldConfig.aMysqldConfig;
import static com.wix.mysql.distribution.Version.v5_7_19;
import static java.lang.String.format;
import static org.springframework.util.StringUtils.isEmpty;
import static tech.simter.embeddeddatabase.mysql.EmbeddedMysqlProperties.*;

/**
 * @author RJ
 */
@Configuration
@ConditionalOnClass(name = "com.wix.mysql.EmbeddedMysql")
public class EmbeddedMysqlConfiguration {
  private final static Logger logger = LoggerFactory.getLogger(EmbeddedMysqlConfiguration.class);

  @Autowired
  public EmbeddedMysqlConfiguration() {
  }

  /**
   * @param config the MysqldConfig configuration which will be used to get the needed host, port..
   * @return the created DB datasource
   */
  @Bean
  @DependsOn("embeddedMysql")
  @ConditionalOnExpression("!${simter.embedded-database.disabled-datasource:false}")
  public DataSource dataSource(MysqldConfig config, EmbeddedMysqlProperties properties) {
    String dbName = isEmpty(properties.getDatabaseName()) ? DEFAULT_DATABASE_NAME : properties.getDatabaseName();
    DriverManagerDataSource ds = new DriverManagerDataSource();
    ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
    // https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-jdbc-url-format.html
    // https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-reference-url-format.html
    // dbc:MySQL://$host:$port/$database?k1=v1&k2=v2
    ds.setUrl(format("jdbc:mysql://%s:%s/%s", "localhost", config.getPort(), dbName));
    ds.setUsername(config.getUsername());
    ds.setPassword(config.getPassword());
    logger.debug("Create a datasource instance for mysql");
    return ds;
  }

  /**
   * @return MysqldConfig that contains embedded db configuration like user name, password
   */
  @Bean
  public MysqldConfig msqldConfig(EmbeddedMysqlProperties properties) throws IOException {
    // custom version
    Version version;
    if (isEmpty(properties.getVersion())) version = v5_7_19;
    else version = createVersion(properties.getVersion());

    // common
    MysqldConfig.Builder configBuilder = aMysqldConfig(version)
      .withCharset(UTF8)
      .withTempDir("target/mysql/");

    // custom port
    if (properties.getPort() > 0) configBuilder.withPort(properties.getPort());
    else configBuilder.withFreePort();

    // custom username, password
    configBuilder.withUser(
      isEmpty(properties.getUsername()) ? DEFAULT_USERNAME : properties.getUsername(),
      isEmpty(properties.getPassword()) ? DEFAULT_PASSWORD : properties.getPassword()
    );

    // custom timeout
    if (properties.getTimeout() > 0) configBuilder.withTimeout(properties.getTimeout(), TimeUnit.SECONDS);
    else configBuilder.withTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

    //.withTimeZone("Europe/Vilnius")
    //.withServerVariable("max_connect_errors", 666)

    return configBuilder.build();
  }

  // convert X.Y.Z to vX_Y_Z
  private Version createVersion(String xyz) {
    String raw = xyz.replace(".", "_");
    return Enum.valueOf(Version.class, raw.startsWith("v") ? raw : "v" + raw);
  }

  /**
   * @param config     the MysqldConfig configuration to use to start mysql db process
   * @param properties the properties configuration
   * @return the started db process
   */
  @Bean(destroyMethod = "stop")
  public EmbeddedMysql embeddedMysql(MysqldConfig config, EmbeddedMysqlProperties properties) {
    logger.debug("embedded mysql properties={}", properties);
    DownloadConfig.Builder downloadConfigBuilder = aDownloadConfig();

    // custom download url
    if (!isEmpty(properties.getDownloadUrl())) {
      logger.debug("embedded mysql download-url={}", properties.getDownloadUrl());
      // default baseUrl="https://dev.mysql.com/get/Downloads/", can use local protocol "file:///C:/Users/simter/download"
      downloadConfigBuilder.withBaseUrl(properties.getDownloadUrl());
      //.withProxy(aHttpProxy("127.0.0.1", 8888))
      //.withCacheDir(Paths.get(System.getProperty("user.home"), ".embedmysql").toString())
    }

    String dbName = isEmpty(properties.getDatabaseName()) ? DEFAULT_DATABASE_NAME : properties.getDatabaseName();
    EmbeddedMysql.Builder mysqld = anEmbeddedMysql(config, downloadConfigBuilder.build())
      .addSchema(SchemaConfig.aSchemaConfig(dbName).build());
    if (logger.isInfoEnabled())
      logger.info("Starting embedded database: url='jdbc:mysql://{}:{}/{}', username='{}', version={}.{}",
        "localhost", config.getPort(), dbName, config.getUsername(),
        config.getVersion().getMajorVersion(), config.getVersion().getMinorVersion());

    return mysqld.start();
  }
}