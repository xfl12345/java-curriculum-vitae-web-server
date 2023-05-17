package cc.xfl12345.person.cv.conf;

import cc.xfl12345.person.cv.initializer.MyDatabaseInitializer;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariDataSource dataSource(DataSourceProperties dataSourceProperties) throws Exception {
        MyDatabaseInitializer initializer = new MyDatabaseInitializer();
        initializer.setDriverClassName(dataSourceProperties.getDriverClassName());
        initializer.setUrl(dataSourceProperties.getUrl());
        initializer.setUsername(dataSourceProperties.getUsername());
        initializer.setPassword(dataSourceProperties.getPassword());
        initializer.init();

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
        dataSource.setJdbcUrl(dataSourceProperties.getUrl());
        dataSource.setUsername(dataSourceProperties.getUsername());
        dataSource.setPassword(dataSourceProperties.getPassword());
        dataSource.setPoolName(dataSourceProperties.getName());
        dataSource.setDataSourceJNDI(dataSourceProperties.getJndiName());

        return dataSource;
    }
}
