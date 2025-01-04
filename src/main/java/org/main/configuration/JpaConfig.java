package org.main.configuration;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "org.main.models")
@EnableJpaRepositories(basePackages = "org.main.controllers.repository")
public class JpaConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(JpaConfig.class);
    
    @Primary
    @Bean
    public DataSource dataSource() {
        // Configura il tuo DataSource, ad esempio con HikariCP
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/reviewhub_db?useSSL=true&requireSSL=true&allowPublicKeyRetrieval=true");
        dataSource.setUsername("admin");
        dataSource.setPassword("admin");
        return dataSource;
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder, DataSource dataSource) {
        logger.info("Configuring EntityManagerFactory...");
        return builder
                .dataSource(dataSource)
                .packages("org.main.models") // Pacchetto contenente le entità JPA
                .persistenceUnit("default")
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
