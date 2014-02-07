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
package com.feedzai.commons.sql.abstraction.dml.result;

/**
 * The H2 column result implementation.
 *
 * @author Diogo Guerra (diogo.guerra@feedzai.com)
 * @since 2.0.0
 */
public class H2ResultColumn extends ResultColumn {
    /**
     * Creates a new instance of {@link ResultColumn}.
     *
     * @param name The column name.
     * @param val  The column value.
     */
    public H2ResultColumn(final String name, final Object val) {
        super(name, val);
    }
}
