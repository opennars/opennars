/*
 * Sample.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.nal.nal8.operator;

import nars.nal.nal8.Execution;

/**
 *  A class used as a template for Operator definition.
 *  Can also be extended for purely procedural operators.
 */
public class NullOperator extends SyncOperator {

    public NullOperator(String name) {
        super(name);
    }

    /** uses the implementation class's simpleName as the term */
    public NullOperator() {
    }

    @Override
    public void execute(Execution e) {

    }
}

