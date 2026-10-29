package com.pulsepass.pulsepass.integration;

import static org.assertj.core.api.Assertions.*;

import java.sql.*;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
class SecurityMigrationTest {
  @Container static PostgreSQLContainer db = new PostgreSQLContainer("postgres:18-alpine");

  @Test
  void freshAndUpgradePreserveLegacy() throws Exception {
    for (String schema : new String[] {"fresh_security", "upgrade_security"}) {
      var config =
          Flyway.configure()
              .dataSource(db.getJdbcUrl(), db.getUsername(), db.getPassword())
              .schemas(schema)
              .defaultSchema(schema)
              .locations("classpath:db/migration");
      if (schema.startsWith("upgrade")) {
        config.target("4").load().migrate();
        try (var c =
                DriverManager.getConnection(db.getJdbcUrl(), db.getUsername(), db.getPassword());
            var st = c.createStatement()) {
          st.execute(
              "INSERT INTO "
                  + schema
                  + ".users(username,email,active) VALUES"
                  + " ('legacy-security','legacy-security@test.invalid',true)");
        }
      }
      config.target("latest").load().migrate();
      config.load().validate();
      try (var c =
              DriverManager.getConnection(db.getJdbcUrl(), db.getUsername(), db.getPassword());
          var st = c.createStatement()) {
        var rs = st.executeQuery("select count(*) from " + schema + ".user_credentials");
        rs.next();
        assertThat(rs.getInt(1)).isZero();
        if (schema.startsWith("upgrade")) {
          rs =
              st.executeQuery(
                  "select count(*) from " + schema + ".users where username='legacy-security'");
          rs.next();
          assertThat(rs.getInt(1)).isEqualTo(1);
        }
      }
    }
  }
}
