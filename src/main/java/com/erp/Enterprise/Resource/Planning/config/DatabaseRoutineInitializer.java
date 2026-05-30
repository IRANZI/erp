package com.erp.Enterprise.Resource.Planning.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class DatabaseRoutineInitializer {
    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void installPostgreSqlRoutines() {
        if (!isPostgreSql()) {
            return;
        }

        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
            ClassPathResource resource = new ClassPathResource("db/postgresql-payroll-routines.sql");
            ScriptUtils.executeSqlScript(
                    connection,
                    new EncodedResource(resource, StandardCharsets.UTF_8),
                    false,
                    false,
                    "--",
                    ";;",
                    "/*",
                    "*/"
            );
            return null;
        });
    }

    private boolean isPostgreSql() {
        Boolean postgres = jdbcTemplate.execute((ConnectionCallback<Boolean>) connection ->
                connection.getMetaData().getDatabaseProductName().toLowerCase().contains("postgresql"));
        return Boolean.TRUE.equals(postgres);
    }
}
