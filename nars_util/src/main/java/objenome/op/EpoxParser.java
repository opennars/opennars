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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import objenome.op.compute.MalformedProgramException;
import objenome.op.bool.And;
import objenome.op.bool.IfAndOnlyIf;
import objenome.op.bool.Nand;
import objenome.op.bool.Nor;
import objenome.op.bool.Not;
import objenome.op.bool.Or;
import objenome.op.bool.Xor;
import objenome.op.lang.If;
import objenome.op.lang.Seq2;
import objenome.op.lang.Seq3;
import objenome.op.math.Absolute;
import objenome.op.math.Add;
import objenome.op.math.CoefficientPower;
import objenome.op.math.Cube;
import objenome.op.math.CubeRoot;
import objenome.op.math.DivisionProtected;
import objenome.op.math.Exp;
import objenome.op.math.Factorial;
import objenome.op.math.GreaterThan;
import objenome.op.math.InvertProtected;
import objenome.op.math.LessThan;
import objenome.op.math.Log10;
import objenome.op.math.LogNatural;
import objenome.op.math.Max2;
import objenome.op.math.Max3;
import objenome.op.math.Min2;
import objenome.op.math.Min3;
import objenome.op.math.ModuloProtected;
import objenome.op.math.Multiply;
import objenome.op.math.Power;
import objenome.op.math.Signum;
import objenome.op.math.Sqrt;
import objenome.op.math.Square;
import objenome.op.math.Subtract;
import objenome.op.trig.ArcCosecant;
import objenome.op.trig.ArcCosine;
import objenome.op.trig.ArcCotangent;
import objenome.op.trig.ArcSecant;
import objenome.op.trig.ArcSine;
import objenome.op.trig.ArcTangent;
import objenome.op.trig.AreaHyperbolicCosine;
import objenome.op.trig.AreaHyperbolicSine;
import objenome.op.trig.AreaHyperbolicTangent;
import objenome.op.trig.Cosecant;
import objenome.op.trig.Cosine;
import objenome.op.trig.Cotangent;
import objenome.op.trig.HyperbolicCosine;
import objenome.op.trig.HyperbolicSine;
import objenome.op.trig.HyperbolicTangent;
import objenome.op.trig.Secant;
import objenome.op.trig.Sine;
import objenome.op.trig.Tangent;

/**
 * This parser is for parsing valid Epox programs into a node tree. It is only
 * able to parse those node types which have been declared through the parser's
 * <code>declare</code> methods. If the constructor's <code>load</code>
 * parameter is set to <code>true</code> then all the built-in Epox node types
 * will be loaded into the parser at construction.
 *
 * @see Node
 *
 * @since 2.0
 */
public class EpoxParser {

    // The language that the parser recognises
    private Map<String, Node> nodes;

    /**
     * Constructs an <code>EpoxParser</code> with no nodes declared
     *
     * @param load whether to load the built-in Epox nodes or not
     */
    public EpoxParser(boolean load) {
        nodes = new HashMap<>();

        if (load) {
            load();
        }
    }

    /**
     * Parses an Epox program string as an executable <code>Node</code> tree.
     * The given <code>source</code> parameter must contain a valid Epox program
     * string, comprised only of nodes that have been declared. The data-types
     * of each function's inputs must correspond to valid data-types, otherwise
     * a <code>MalformedProgramException</code> will be thrown.
     *
     * @param source the program string to be parsed as an Epox program.
     * @return a <code>Node</code> which is the root of a tree which is
     * equivalent to the provided source string. A <code>null</code> value will
     * be returned if the <code>source</code> parameter is <code>null</code>.
     * @throws MalformedProgramException if the given string does not represent
     * a valid Epox program.
     */
    public Node parse(String source) throws MalformedProgramException {
        if (source == null) {
            return null;
        }

        // Locate the first bracket (straight after function name)
        int openingBracket = source.indexOf('(');

        String identifier = null;
        List<String> args = null;

        // If there is no bracket then it must be a terminal
        if (openingBracket == -1) {
            identifier = source;
            args = new ArrayList<>();
        } else {
            // Get the name of the function
            identifier = source.substring(0, openingBracket).trim();

            // Locate the closing bracket
            int closingBracket = source.lastIndexOf(')');

            // Get the comma separated arguments - without brackets
            String argumentStr = source.substring(openingBracket + 1, closingBracket);

            // Separate the arguments
            args = splitArguments(argumentStr);
        }

        // Construct the node
        Node node = nodes.get(identifier);

        // Check the arities match
        if (node == null) {
            throw new MalformedProgramException("unknown node type: " + identifier);
        } else if (node.getArity() != args.size()) {
            throw new MalformedProgramException("unexpected arity for node: " + identifier + "(expected: "
                    + node.getArity() + ", found: " + args.size() + ")");
        } else {
            node = node.newInstance();

            // Recursively parse and set each child node
            for (int i = 0; i < args.size(); i++) {
                node.setChild(i, parse(args.get(i)));
            }

            // Validate the node's input data-types
            if (node.dataType() == null) {
                if (node.isNonTerminal()) {
                    throw new MalformedProgramException("Input data-types for " + identifier + " are invalid");
                } else {
                    throw new MalformedProgramException("Data-type of terminal " + identifier + " is null");
                }
            }
        }

        return node;
    }

    /**
     * Declares a node so that the parser is able to parse nodes of this type.
     * The identifier of the node is used to identify nodes of this type in the
     * source.
     *
     * @param node an instance of the node type to declare
     */
    public void declare(Node node) {
        nodes.put(node.getIdentifier(), node);
    }

    /**
     * Removes a node so that the parser will no longer parse nodes of this
     * type. The identifier of the node must match the identifier from when it
     * was declared.
     *
     * @param node an instance of a node type that should no longer be parsed
     */
    public void undeclare(Node node) {
        nodes.remove(node.getIdentifier());
    }

    /*
     * Splits a string that represents zero or more arguments to a function,
     * separated by either commas or spaces, into the separate arguments. Each
     * argument may contain nested method calls, which themselves have multiple
     * arguments - this method splits just the top level of arguments so that
     * they can themselves be parsed individually.
     */
    private List<String> splitArguments(String argStr) {
        int depth = 0;

        List<String> args = new ArrayList<>(5);
        StringBuilder buffer = new StringBuilder();

        argStr = argStr.trim();

        for (int i = 0; i < argStr.length(); i++) {
            char c = argStr.charAt(i);

            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            }
            if (((c == ' ') || (c == ',')) && (depth == 0)) {
                args.add(buffer.toString());
                buffer = new StringBuilder();
                while (argStr.charAt(i + 1) == ' ') {
                    i++;
                }
            } else {
                buffer.append(c);
            }
        }

        if (buffer.length() > 0) {
            args.add(buffer.toString());
        }

        return args;
    }

    /**
     * Loads the built-in Epox node types
     */
    protected void load() {
        // Insert the Boolean functions.
        Node[] toDeclare = new Node[]{
            new And(),
            new IfAndOnlyIf(),
            new Nand(),
            new Nor(),
            new Not(),
            new Or(),
            new Xor(),
            new ArcCosecant(),
            new ArcCosine(),
            new ArcCotangent(),
            new ArcSecant(),
            new ArcSine(),
            new ArcTangent(),
            new AreaHyperbolicCosine(),
            new AreaHyperbolicSine(),
            new AreaHyperbolicTangent(),
            new Cosecant(),
            new Cosine(),
            new Cotangent(),
            new HyperbolicCosine(),
            new HyperbolicSine(),
            new HyperbolicTangent(),
            new Secant(),
            new Sine(),
            new Tangent(),
            new Absolute(),
            new Add(),
            new CoefficientPower(),
            new Cube(),
            new CubeRoot(),
            new Exp(),
            new Factorial(),
            new GreaterThan(),
            new InvertProtected(),
            new Log10(),
            new LogNatural(),
            new LessThan(),
            new Max2(),
            new Min2(),
            new Max3(),
            new Min3(),
            new ModuloProtected(),
            new Multiply(),
            new Power(),
            new DivisionProtected(),
            new Signum(),
            new Square(),
            new Sqrt(),
            new Subtract(),
            new If(),
            new Seq2(),
            new Seq3()
        };

        for (Node node : toDeclare) {
            declare(node);
        }
    }
}
