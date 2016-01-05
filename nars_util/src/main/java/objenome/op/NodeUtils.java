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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class provides static utility methods for working with Epox nodes
 * TODO these can be replaced with iterators that avoid allocating collections
 * @since 2.0
 */
public   enum NodeUtils {
    ;

    /**
     * Returns those nodes from the given syntax that have an arity of 0. The
     * given <code>List</code> is not modified at all
     *
     * @param syntax a <code>List</code> of <code>Node</code> objects. The
     * syntax must not be <code>null</code>
     * @return a <code>List</code> of <code>Node</code> objects from the
     * <code>syntax</code> with arity of <code>0</code>
     */
    public static List<Node> terminals(Collection<Node> syntax) {
        if (syntax == null) {
            throw new IllegalArgumentException("syntax must not be null");
        }

        List<Node> terminals = new ArrayList<>(syntax.size());
        terminals.addAll(syntax.stream().filter(Node::isTerminal).collect(Collectors.toList()));

        return terminals;
    }

    /**
     * Returns those nodes from the given syntax that have an arity of greater
     * than <code>0</code>. The given <code>List</code> is not modified at all.
     *
     * @param syntax a <code>List</code> of <code>Node</code> objects. The
     * syntax must not be <code>null</code>
     * @return a <code>List</code> of <code>Node</code> objects from the
     * <code>syntax</code> with arity <code>&gt;0</code>
     */
    public static List<Node> nonTerminals(Collection<Node> syntax) {
        if (syntax == null) {
            throw new IllegalArgumentException("syntax must not be null");
        }

        List<Node> functions = new ArrayList<>(syntax.size());
        functions.addAll(syntax.stream().filter(Node::isNonTerminal).collect(Collectors.toList()));

        return functions;
    }

    /**
     * Creates a <code>List</code> of <code>Literal</code> objects with a range
     * of values. Given a <code>start</code> parameter of <code>2</code>, a
     * <code>quantity</code> of <code>4</code> and an <code>interval</code> of
     * <code>3</code>, the returned <code>List</code> will contain 4 literals
     * with the values: <code>2, 5, 8, 11</code>.
     *
     * @param start the value that should be used for the first
     * <code>Literal</code> in the range
     * @param interval the interval between each element of the range
     * @param quantity the number of elements in the range. Must be zero or
     * greater
     * @return a <code>List</code> of <code>Literals</code> with the range of
     * values given
     */
    public static List<Literal> intRange(int start, int interval, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must be 0 or greater");
        }

        List<Literal> range = new ArrayList<>(quantity);

        for (int i = 0; i < quantity; i++) {
            int value = (i * interval) + start;

            range.add(new Doubliteral(value));
        }

        return range;
    }

    /**
     * Creates a <code>List</code> of <code>Literal</code> objects with a range
     * of values. Given a <code>start</code> parameter of <code>2L</code>, a
     * <code>quantity</code> of <code>4</code> and an <code>interval</code> of
     * <code>3L</code>, the returned <code>List</code> will contain 4 literals
     * with the values: <code>2L, 5L, 8L, 11L</code>.
     *
     * @param start the value that should be used for the first
     * <code>Literal</code> in the range
     * @param interval the interval between each element of the range
     * @param quantity the number of elements in the range
     * @return a <code>List</code> of <code>Literals</code> with the range of
     * values given
     */
    public static List<Literal> longRange(long start, long interval, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must be 0 or greater");
        }

        List<Literal> range = new ArrayList<>(quantity);

        for (int i = 0; i < quantity; i++) {
            long value = (i * interval) + start;

            range.add(new Literal(value));
        }

        return range;
    }

    /**
     * Creates a <code>List</code> of <code>Literal</code> objects with a range
     * of values. Given a <code>start</code> parameter of <code>2.2</code>, a
     * <code>quantity</code> of <code>4</code> and an <code>interval</code> of
     * <code>3.2</code>, the returned <code>List</code> will contain 4 literals
     * with the values: <code>2.2, 5.4, 8.6, 11.8</code>.
     *
     * @param start the value that should be used for the first
     * <code>Literal</code> in the range
     * @param interval the interval between each element of the range
     * @param quantity the number of elements in the range
     * @return a <code>List</code> of <code>Literals</code> with the range of
     * values given
     */
    public static List<Literal> doubleRange(double start, double interval, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must be 0 or greater");
        }

        List<Literal> range = new ArrayList<>(quantity);

        for (int i = 0; i < quantity; i++) {
            double value = (i * interval) + start;

            range.add(new Doubliteral(value));
        }

        return range;
    }

    /**
     * Creates a <code>List</code> of <code>Literal</code> objects with a range
     * of values. Given a <code>start</code> parameter of <code>2.2f</code>, a
     * <code>quantity</code> of <code>4</code> and an <code>interval</code> of
     * <code>3.2f</code> , the returned <code>List</code> will contain 4
     * literals with the values: <code>2.2f, 5.4f, 8.6f, 11.8f</code>
     *
     * @param start the value that should be used for the first
     * <code>Literal</code> in the range.
     * @param interval the interval between each element of the range.
     * @param quantity the number of elements in the range.
     * @return a <code>List</code> of <code>Literals</code> with the range of
     * values given.
     */
    public static List<Literal> floatRange(float start, float interval, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must be 0 or greater");
        }

        List<Literal> range = new ArrayList<>(quantity);

        for (int i = 0; i < quantity; i++) {
            float value = (i * interval) + start;

            range.add(new Doubliteral(value));
        }

        return range;
    }

    /**
     * Creates a <code>List</code> of <code>Variable</code> objects of the given
     * data type. The number of variables created will match the number of
     * variable names provided. The variables will all have a <code>null</code>
     * value.
     *
     * @param datatype the data-type for all the variables to be created
     * @param variableNames the names to assign to each of the variables
     * @return a <code>List</code> of <code>Variable</code>s with the given
     * names
     */
    public static List<Variable> createVariables(Class<?> datatype, String... variableNames) {
        if (variableNames == null) {
            throw new IllegalArgumentException("variableNames must not be null");
        }

        List<Variable> variables = new ArrayList<>();

        for (String name : variableNames) {
            variables.add(Variable.make(name, datatype));
        }

        return variables;
    }


}
