package com.dyxia.nexuserp.config;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Stratégie de migration Flyway personnalisée.
 * Effectue un repair automatique avant chaque migration pour corriger les
 * checksums des scripts modifiés en développement, puis applique les migrations.
 */
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            flyway.repair(); // Corrige les checksums des migrations modifiées et supprime les migrations en échec
            flyway.migrate(); // Applique les nouvelles migrations
        };
    }
}
