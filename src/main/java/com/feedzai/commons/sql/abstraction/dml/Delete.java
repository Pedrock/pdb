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
package com.feedzai.commons.sql.abstraction.dml;

/**
 * Represents the DELETE operator.
 *
 * @author Rui Vilao (rui.vilao@feedzai.com)
 * @since 2.0.0
 */
public class Delete extends Expression {
    /**
     * The WHERE expression.
     */
    private Expression where;
    /**
     * The table.
     */
    private final Expression table;

    /**
     * Creates a new instance of {@link Delete}.
     *
     * @param table The table.
     */
    public Delete(final Expression table) {
        this.table = table;
    }

    /**
     * Sets the where expression.
     *
     * @param where The where expression.
     * @return This object.
     */
    public Delete where(final Expression where) {
        this.where = where;

        return this;
    }

    /**
     * Gets the WHERE expression.
     *
     * @return The WHERE expression.
     */
    public Expression getWhere() {
        return where;
    }

    /**
     * Gets the TABLE expression.
     *
     * @return The TABLE expression.
     */
    public Expression getTable() {
        return table;
    }

    @Override
    public String translate() {
        return translator.translate(this);
    }
}
