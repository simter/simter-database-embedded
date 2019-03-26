package tech.simter.embeddeddatabase.postgres;

import de.flapdoodle.embed.process.distribution.IVersion;
import de.flapdoodle.embed.process.io.directories.FixedPath;
import de.flapdoodle.embed.process.runtime.Network;
import de.flapdoodle.embed.process.store.PostgresArtifactStoreBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.CollectionUtils;
import ru.yandex.qatools.embed.postgresql.Command;
import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig.Credentials;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig.Net;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig.Storage;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig.Timeout;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;
import ru.yandex.qatools.embed.postgresql.config.PostgresDownloadConfigBuilder;
import ru.yandex.qatools.embed.postgresql.config.RuntimeConfigBuilder;
import ru.yandex.qatools.embed.postgresql.distribution.Version;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Paths;

import static java.lang.String.format;
import static org.springframework.util.StringUtils.isEmpty;
import static tech.simter.embeddeddatabase.postgres.EmbeddedPostgresProperties.*;

@Configuration
@ConditionalOnClass(name = "ru.yandex.qatools.embed.postgresql.EmbeddedPostgres")
public class EmbeddedPostgresConfiguration {
  private final static Logger logger = LoggerFactory.getLogger(EmbeddedPostgresConfiguration.class);

  @Autowired
  public EmbeddedPostgresConfiguration() {
  }

  /**
   * @param config the PostgresConfig configuration which will be used to get the needed host, port..
   * @return the created DB datasource
   */
  @Bean
  @DependsOn("postgresProcess")
  @ConditionalOnExpression("!${simter.embedded-database.disabled-datasource:false}")
  public DataSource dataSource(PostgresConfig config) {
    DriverManagerDataSource ds = new DriverManagerDataSource();
    ds.setDriverClassName("org.postgresql.Driver");
    ds.setUrl(format("jdbc:postgresql://%s:%s/%s", config.net().host(), config.net().port(), config.storage().dbName()));
    ds.setUsername(config.credentials().username());
    ds.setPassword(config.credentials().password());
    logger.debug("Create a datasource instance for postgres");
    return ds;
  }

  /**
   * @return PostgresConfig that contains embedded db configuration like user name , password
   */
  @Bean
  public PostgresConfig postgresConfig(EmbeddedPostgresProperties properties) throws IOException {
    IVersion version = isEmpty(properties.getVersion()) ? Version.V10_6 : (IVersion) properties::getVersion;
    String dataDir = isEmpty(properties.getDataDir()) ?
      // default ""target/pg-{version}-data""
      Paths.get("target", "pg-" + version.asInDownloadPath() + "-data").toString()
      : properties.getDataDir();
    PostgresConfig postgresConfig = new PostgresConfig(
      version,
      new Net(
        isEmpty(properties.getHost()) ? "localhost" : properties.getHost(),
        properties.getPort() < 1 ? Network.getFreeServerPort() : properties.getPort()
      ),
      new Storage(
        isEmpty(properties.getDatabaseName()) ? DEFAULT_DATABASE_NAME : properties.getDatabaseName(),
        dataDir
      ),
      new Timeout(),
      new Credentials(
        isEmpty(properties.getUsername()) ? DEFAULT_USERNAME : properties.getUsername(),
        isEmpty(properties.getPassword()) ? DEFAULT_PASSWORD : properties.getPassword()
      )
    );
    if (!CollectionUtils.isEmpty(properties.getAdditionalInitDbParams()))
      postgresConfig.getAdditionalInitDbParams().addAll(properties.getAdditionalInitDbParams());
    if (!CollectionUtils.isEmpty(properties.getAdditionalPostgresParams()))
      postgresConfig.getAdditionalPostgresParams().addAll(properties.getAdditionalPostgresParams());
    return postgresConfig;
  }

  /**
   * @param config     the PostgresConfig configuration to use to start Postgres db process
   * @param properties the properties configuration
   * @return the started db process
   */
  @Bean(destroyMethod = "stop")
  public PostgresProcess postgresProcess(PostgresConfig config, EmbeddedPostgresProperties properties) throws IOException {
    String extractedDir = isEmpty(properties.getExtractedDir()) ? DEFAULT_EXTRACTED_DIR : properties.getExtractedDir();
    logger.debug("embedded postgres properties={}", properties);
    de.flapdoodle.embed.process.config.RuntimeConfigBuilder cfg = new RuntimeConfigBuilder()
      .defaults(Command.Postgres)
      .artifactStore(
        new PostgresArtifactStoreBuilder() {
          @Override
          public PostgresArtifactStoreBuilder defaults(Command command) {
            super.defaults(command);

            // custom extracted dir
            logger.debug("embedded-postgres extracted-dir={}", extractedDir);
            this.tempDir(new FixedPath(extractedDir)); // this.tempDir().setDefault(SubdirTempDir())

            // custom download url
            if (!isEmpty(properties.getDownloadUrl())) {
              logger.debug("embedded-postgres download-url={}", properties.getDownloadUrl());
              this.download().setDefault(
                new PostgresDownloadConfigBuilder()
                  .defaultsForCommand(command)
                  .downloadPath(properties.getDownloadUrl()) // here
                  .build()
              );
            }

            return this;
          }
        }.defaults(Command.Postgres)
      );

    PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getInstance(cfg.build());
    PostgresExecutable exec = runtime.prepare(config);
    if (logger.isWarnEnabled())
      logger.info("Starting embedded database: url='jdbc:postgresql://{}:{}/{}', username='{}', version={}",
        config.net().host(), config.net().port(), config.storage().dbName(),
        config.credentials().username(), config.version().asInDownloadPath());
    return exec.start();
  }
}