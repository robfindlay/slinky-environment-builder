package org.slinkyframework.environment.builder.liquibase.local;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slinkyframework.environment.builder.EnvironmentBuilderException;
import org.slinkyframework.environment.builder.liquibase.LiquibaseBuildDefinition;
import org.slinkyframework.environment.builder.liquibase.drivers.DatabaseDriver;
import org.slinkyframework.environment.builder.liquibase.drivers.DatabaseDriverFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class LiquibaseSetUp {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseSetUp.class);

    private Liquibase liquibase;
    private String hostname;

    public LiquibaseSetUp(String hostname) {
        this.hostname = hostname;
    }

    public void setUp(LiquibaseBuildDefinition definition) {

        DatabaseDriver databaseDriver = null;

        try {
            databaseDriver = DatabaseDriverFactory.getInstance(definition);

            Connection con = databaseDriver.createConnection(hostname);

            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(con));
            ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();

            liquibase = new Liquibase(definition.getChangeLogFile(), resourceAccessor, database);

            LOGGER.info("Setting up database {} on {}", definition.getName(), hostname);

            liquibase.update("");

        } catch (LiquibaseException | SQLException e) {
            throw new EnvironmentBuilderException("Database setup has failed", e);
        } finally {
            databaseDriver.cleanUp();
        }
    }
}
