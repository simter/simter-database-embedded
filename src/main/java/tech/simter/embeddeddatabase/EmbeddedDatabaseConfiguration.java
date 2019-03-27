package tech.simter.embeddeddatabase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.boot.jdbc.DataSourceInitializationMode.NEVER;

/**
 * @author RJ
 */
@Configuration("simterEmbeddedDatabaseConfiguration")
@ComponentScan
public class EmbeddedDatabaseConfiguration {
  private final static Logger logger = LoggerFactory.getLogger(EmbeddedDatabaseConfiguration.class);
  private final boolean concatSqlScript;

  @Autowired
  public EmbeddedDatabaseConfiguration(@Value("${simter.embedded-database.concat-sql-script:false}") boolean concatSqlScript) {
    this.concatSqlScript = concatSqlScript;
  }

  @PostConstruct
  public void init() {
  }

  @Bean("simterConcatSqlScript")
  public String concatSqlScript(DataSourceProperties properties) throws IOException {
    if (!concatSqlScript || properties.getInitializationMode() == null || properties.getInitializationMode() == NEVER)
      return "";
    ResourceLoader resourcePatternResolver = new PathMatchingResourcePatternResolver();

    // concat schema and data
    List<String> sqlResources = new ArrayList<>();
    if (properties.getSchema() != null) sqlResources.addAll(properties.getSchema());
    if (properties.getData() != null) sqlResources.addAll(properties.getData());
    if (sqlResources.isEmpty()) return "";
    StringBuffer sql = new StringBuffer();
    for (int i = 0; i < sqlResources.size(); i++) {
      String resourcePath = sqlResources.get(i);
      logger.debug("Load script content from {}", resourcePath);
      sql.append("-- copy from ").append(resourcePath).append("\r\n\r\n")
        .append(loadSql(resourcePath, resourcePatternResolver));
      if (i < sqlResources.size() - 1) sql.append("\r\n\r\n");
    }

    // save concatenate sql content to file
    String sqlStr = sql.toString();
    File sqlFile = new File("target/" + properties.getPlatform() + ".sql");
    logger.debug("Save concatenate SQL script to {}", sqlFile.getAbsolutePath());
    FileCopyUtils.copy(sqlStr.getBytes(StandardCharsets.UTF_8), sqlFile);

    return sqlStr;
  }

  private String loadSql(String resourcePath, ResourceLoader resourcePatternResolver) {
    try {
      return FileCopyUtils.copyToString(new InputStreamReader(
        resourcePatternResolver.getResource(resourcePath).getInputStream(), StandardCharsets.UTF_8
      ));
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}