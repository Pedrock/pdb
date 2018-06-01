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
package com.feedzai.commons.sql.abstraction.engine.impl.db2;


import com.feedzai.commons.sql.abstraction.engine.impl.abs.AbstractEngineSchemaTest;
import com.feedzai.commons.sql.abstraction.engine.testconfig.DatabaseConfiguration;
import com.feedzai.commons.sql.abstraction.engine.testconfig.DatabaseTestUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

/**
 * @author Joao Silva (joao.silva@feedzai.com)
 * @since 2.0.0
 */
@RunWith(Parameterized.class)
public class DB2EngineSchemaTest extends AbstractEngineSchemaTest {


    @Parameterized.Parameters
    public static Collection<DatabaseConfiguration> data() throws Exception {
        return DatabaseTestUtil.loadConfigurations("db2");
    }

    protected String getDefaultSchema() {
        return config.username;
    }

    /*
     * TODO: remove after creating UDFs for DB2
     * http://newpush.com/2009/08/creating-a-user-defined-function-udf-in-java-for-ibm-db2-9-7/
     */
    @Override
    @Test
    @Ignore("Test suite doesn't have UDFs for DB2")
    public void udfGetOneTest() throws Exception {
    }

    /*
     * TODO: remove after creating UDFs for DB2
     * http://newpush.com/2009/08/creating-a-user-defined-function-udf-in-java-for-ibm-db2-9-7/
     */
    @Override
    @Test
    @Ignore("Test suite doesn't have UDFs for DB2")
    public void udfTimesTwoTest() throws Exception {
    }
}
