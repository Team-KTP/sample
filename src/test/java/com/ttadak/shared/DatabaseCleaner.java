package com.ttadak.shared;

import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** 매 테스트 전에 모든 테이블을 비운다. 테스트 DB가 H2라서 H2의 문법을 쓴다. */
public class DatabaseCleaner implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        DataSource dataSource = SpringExtension.getApplicationContext(context)
            .getBean(DataSource.class);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<String> tables = jdbcTemplate.queryForList(
            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_TYPE = 'BASE TABLE'",
            String.class
        );

        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        tables.forEach(
            table -> jdbcTemplate.execute("TRUNCATE TABLE " + table + " RESTART IDENTITY")
        );
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

}
