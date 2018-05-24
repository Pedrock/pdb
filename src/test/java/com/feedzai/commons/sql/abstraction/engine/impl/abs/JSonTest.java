/*
 * Copyright 2014 Feedzai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feedzai.commons.sql.abstraction.engine.impl.abs;

import com.feedzai.commons.sql.abstraction.ddl.DbColumnType;
import com.feedzai.commons.sql.abstraction.ddl.DbEntity;
import com.feedzai.commons.sql.abstraction.dml.Expression;
import com.feedzai.commons.sql.abstraction.dml.Update;
import com.feedzai.commons.sql.abstraction.dml.result.ResultColumn;
import com.feedzai.commons.sql.abstraction.engine.ConnectionResetException;
import com.feedzai.commons.sql.abstraction.engine.DatabaseEngine;
import com.feedzai.commons.sql.abstraction.engine.DatabaseEngineException;
import com.feedzai.commons.sql.abstraction.engine.DatabaseFactory;
import com.feedzai.commons.sql.abstraction.engine.NameAlreadyExistsException;
import com.feedzai.commons.sql.abstraction.engine.impl.PostgreSqlEngine;
import com.feedzai.commons.sql.abstraction.engine.testconfig.DatabaseConfiguration;
import com.feedzai.commons.sql.abstraction.engine.testconfig.DatabaseTestUtil;
import com.feedzai.commons.sql.abstraction.entry.EntityEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.feedzai.commons.sql.abstraction.dml.dialect.SqlBuilder.all;
import static com.feedzai.commons.sql.abstraction.dml.dialect.SqlBuilder.column;
import static com.feedzai.commons.sql.abstraction.dml.dialect.SqlBuilder.entry;
import static com.feedzai.commons.sql.abstraction.dml.dialect.SqlBuilder.eq;
import static com.feedzai.commons.sql.abstraction.dml.dialect.SqlBuilder.lit;
import static com.feedzai.commons.sql.abstraction.dml.dialect.SqlBuilder.select;
import static com.feedzai.commons.sql.abstraction.dml.dialect.SqlBuilder.table;
import static com.feedzai.commons.sql.abstraction.dml.dialect.SqlBuilder.update;
import static com.feedzai.commons.sql.abstraction.engine.configuration.PdbProperties.ENGINE;
import static com.feedzai.commons.sql.abstraction.engine.configuration.PdbProperties.JDBC;
import static com.feedzai.commons.sql.abstraction.engine.configuration.PdbProperties.PASSWORD;
import static com.feedzai.commons.sql.abstraction.engine.configuration.PdbProperties.SCHEMA_POLICY;
import static com.feedzai.commons.sql.abstraction.engine.configuration.PdbProperties.USERNAME;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for JSON columns.
 *
 * @author Paulo Leitao (paulo.leitao@feedzai.com)
 * @since 2.1.6
 */
@RunWith(Parameterized.class)
public class JSonTest {

    /*
     * Test table properties, a table with a PK and a json column.
     */
    private static final String TEST_TABLE = "TEST_TBL";
    private static final String PK_COL = "PK_COL";
    private static final String JSON_COL = "JSON_COL";

    // The json value that will be used in most of the tests
    private static final long PK_VALUE = 1;
    private static final String JSON_PART_1 = "\"foo\": \"bar\"";
    private static final String JSON_PART_2 = "\"baz\": 1";
    private static final String JSON_PART_3 = "\"baz\": 2";
    private static final String JSON_VALUE = "{" + JSON_PART_1 + ", " + JSON_PART_2 + "}";

    private DatabaseEngine dbEngine;

    /**
     * Configurations the test will run with, set them in connection.properties and ensure
     * they are included in the -Dinstances VM args.
     *
     * @return  The configurations under which the test runs.
     */
    @Parameterized.Parameters
    public static Collection<DatabaseConfiguration> data() throws Exception {
        return DatabaseTestUtil.loadConfigurations();
    }

    @Parameterized.Parameter
    public DatabaseConfiguration config;


    @Before
    public void createTestTable() throws Exception {
        // Connect to db
        Properties dbProps = new Properties() {
            {
                setProperty(JDBC, config.jdbc);
                setProperty(USERNAME, config.username);
                setProperty(PASSWORD, config.password);
                setProperty(ENGINE, config.engine);
                setProperty(SCHEMA_POLICY, "create-drop");
            }
        };
        dbEngine = DatabaseFactory.getConnection(dbProps);

        // Create table
        DbEntity testEntity = new DbEntity.Builder()
                .name(TEST_TABLE)
                .addColumn(PK_COL, DbColumnType.LONG)
                .addColumn(JSON_COL, DbColumnType.JSON)
                .pkFields(PK_COL)
                .build();
        dbEngine.addEntity(testEntity);
    }

    /**
     * Scenario for an insert of a correct json value using persist().
     */
    @Test
    public void normalInsertTest() throws DatabaseEngineException {
        dbEngine.beginTransaction();
        EntityEntry jsonTestEntry = getTestEntry();
        dbEngine.persist(TEST_TABLE, jsonTestEntry, false);
        dbEngine.commit();
        checkInsertedValue(JSON_PART_1, JSON_PART_2);
    }

    /**
     * Scenario for an insert of an invalid json using persist(). If should fail on
     * engines that support JSON natively.
     */
    @Test
    public void badValueInsertTest() {
        boolean exceptionThrown = false;
        try {
            dbEngine.beginTransaction();
            EntityEntry jsonTestEntry = entry()
                    .set(PK_COL, PK_VALUE)
                    .set(JSON_COL, "bad json")
                    .build();
            dbEngine.persist(TEST_TABLE, jsonTestEntry, false);
            dbEngine.commit();
        } catch (final DatabaseEngineException e) {
            dbEngine.rollback();
            exceptionThrown = true;
        }
        if (dbEngine instanceof PostgreSqlEngine && !exceptionThrown) {
            fail("An error is expected when inserting an incorrect json value in a db engine that supports this type");
        }
    }

    /**
     * Scenario for inserts in batch updates.
     */
    @Test
    public void batchInsertTest() throws DatabaseEngineException {
        dbEngine.beginTransaction();
        dbEngine.addBatch(TEST_TABLE, getTestEntry());
        dbEngine.flush();
        dbEngine.commit();
        checkInsertedValue(JSON_PART_1, JSON_PART_2);
    }

    /**
     * Scenario for an update of a JSON field using prepared statements
     */
    @Test
    public void prepStmtUpdateTest() throws DatabaseEngineException, NameAlreadyExistsException, ConnectionResetException {
        // Insert entry first
        normalInsertTest();

        // Update JSON column
        dbEngine.beginTransaction();
        String PS_NAME = "MyPS";
        Update upd = update(table(TEST_TABLE)).set(eq(column(JSON_COL), lit("?"))).where(eq(column(PK_COL), lit("?")));
        dbEngine.createPreparedStatement(PS_NAME, upd);
        dbEngine.clearParameters(PS_NAME);
        dbEngine.setParameter(PS_NAME, 1, "{" + JSON_PART_1 + ", " + JSON_PART_3 + "}", DbColumnType.JSON);
        dbEngine.setParameter(PS_NAME, 2, PK_VALUE);
        dbEngine.executePS(PS_NAME);
        dbEngine.commit();
        checkInsertedValue(JSON_PART_1, JSON_PART_3);

    }

    /**
     * Creates the test entry value used in most tests.
     *
     * @return the created test entry.
     */
    private EntityEntry getTestEntry() {
        return entry()
                .set(PK_COL, PK_VALUE)
                .set(JSON_COL, JSON_VALUE)
                .build();
    }

    /**
     * Checks that the test table has a single entry and with the values as expected.
     */
    private void checkInsertedValue(String jsonPart1, String jsonPart2) throws DatabaseEngineException {
        Expression query = select(all()).from(table(TEST_TABLE));
        List<Map<String, ResultColumn>> results = dbEngine.query(query);
        assertEquals("One value inserted", 1, results.size());
        Map<String, ResultColumn> firstRow = results.get(0);
        assertNotNull("Inserted row is not null", firstRow);
        assertThat("JSon value is as expected", firstRow.get(JSON_COL).toString().replaceAll("\\s+", ""), anyOf(
                is(("{" + jsonPart1 + "," + jsonPart2 + "}").replaceAll("\\s+", "")),
                is(("{" + jsonPart2 + "," + jsonPart1 + "}").replaceAll("\\s+",""))
        ));
    }

    @After
    public void cleanup() {
        dbEngine.close();
    }
}
