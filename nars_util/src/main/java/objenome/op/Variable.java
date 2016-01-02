/*
 * Copyright 2007-2013
 * Licensed under GNU Lesser General Public License
 * 
 * This file is part of EpochX: genetic programming software for research
 * 
 * EpochX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EpochX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with EpochX. If not, see <http://www.gnu.org/licenses/>.
 * 
 * The latest version is available from: http://www.epochx.org
 */
package objenome.op;

/**
 * Instances of <code>Variable</code> are named values for use in a program
 * tree. Variables are <b>not</b> nodes, so they must be wrapped in a
 * <code>VariableNode</code> to be used in a program. The data-type of a
 * variable is determined at construction and must not then be changed.
 *
 * @see VariableNode
 *
 * @since 2.0
 */
public abstract class Variable<X> {

    public final Class datatype;
    public final String name;


    @Override
    public Variable<X> clone() {
        return Variable.make(name, datatype);
    }

    public static Variable make(String name, Class datatype) {
        if (datatype == Double.class) {
            return new DoubleVariable(name);
        }

        return new ObjectVariable(name, datatype);
    }
    public static <X> Variable<X> make(String name, X value) {
        return new ObjectVariable(name, value);
    }

    public void set(double valueForDoubleVariable) {
        setValue((X)((Double)valueForDoubleVariable));
        //throw new RuntimeException("this is not a DoubleVariable");
    }

    public static class ObjectVariable<X> extends Variable<X> {
        protected X value;

        ObjectVariable(String name, Class datatype) {
            super(name, datatype);

            if (datatype == Double.class) {
                throw new RuntimeException("use DoubleVariable");
            }
        }

        /**
         * Constructs a new variable with the given value. The variable's name is
         * provided but the data-type is determined by the type of the given value.
         * The given <code>name</code> and <code>value</code> must be
         * non-<code>null</code>. If the value is unknown then use the alternative
         * constructor to provide the data-type instead of a value.
         *
         * @param name a name for the variable
         * @param value the initial value of the variable
         */
        ObjectVariable(String name, X value) {
            this(name, value.getClass());

            this.value = value;
        }

        @Override
        public void setValue(X value) {
            if (value != null && !datatype.isAssignableFrom(value.getClass())) {
                throw new IllegalArgumentException("variables may not change data-type");
            }

            this.value = value;
        }

        @Override
        public X getValue() {
            return value;
        }
    }


    /**
     * Constructs a new variable with a <code>null</code> value. The variable's
     * name and data-type must be provided. The given <code>name</code> and
     * <code>datatype</code> must be non-<code>null</code>.
     *
     * @param name the name of the variable
     * @param datatype the widest data-type of the values to be assigned to this
     * variable
     */
    public Variable(String name, Class datatype) {
        if (name == null || datatype == null) {
            throw new IllegalArgumentException("identifier and data-type must be non-null");
        }

        this.name = name;
        this.datatype = datatype;
    }


    /** does not compare value */
    @Override public boolean equals(Object obj) {
        if (obj instanceof Variable) {
            return name.equals(((Variable)obj).name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Sets the value of this variable. The data-type of a variable cannot be
     * changed after construction, and only values which are instances of the
     * class or subclasses of the original data-type may be used. A
     * <code>null</code> value is considered valid for a variable of any
     * data-type.
     *
     * @param value the value to set for the variable
     */
    public abstract void setValue(X value);

    /**
     * Returns the data-type of this variable
     *
     * @return this variable's data-type
     */
    public Class getDataType() {
        return datatype;
    }

    /**
     * Returns this variable's value
     *
     * @return this variable's value
     */
    public abstract X getValue();


    /**
     * Returns the name of this variable
     *
     * @return the name of this variable
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a string representation of this variable
     *
     * @return a string representation of this variable
     */
    @Override
    public String toString() {
        return name;
    }

}
