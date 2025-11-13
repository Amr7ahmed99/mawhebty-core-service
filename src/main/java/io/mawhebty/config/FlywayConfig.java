package io.mawhebty.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.flywaydb.core.Flyway;
import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class FlywayConfig {

    private final DataSource dataSource;

    @Bean
    public CommandLineRunner flywayRunner() {
        return args -> {
            try {
                Flyway flyway = Flyway.configure()
                        .dataSource(dataSource)
                        .locations("classpath:db/migration")
                        .baselineOnMigrate(true)
                        .validateOnMigrate(false)
                        .outOfOrder(true)
                        .table("flyway_schema_history")
                        .load();

                flyway.migrate();
                System.out.println("✅ Flyway migration completed successfully");

            } catch (Exception e) {
                System.err.println("❌ Flyway migration failed: " + e.getMessage());
                throw e;
            }
        };
    }
}