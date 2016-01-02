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
package objenome.op.compute;

import objenome.op.compute.Computer.ComputerInput;
import objenome.solver.evolve.Organism;

import java.util.function.Function;

/**
 * Computers provide a mechanism for executing program source code that is
 * being evolved by a grammar based representation or otherwise. There are two
 * approaches to execute program strings provided depending upon the nature of
 * the evolved code.
 *
 * <h4>Expression evaluation</h4>
 *
 * <p>
 * The <code>eval</code> methods are for use on program strings that represent
 * an individual expression, that will have an individual return value. A simple
 * example of this is a nested lisp expression. The result of evaluating the
 * expression will be returned from the method call.
 *
 * <h4>Statement execution</h4>
 *
 * <p>
 * The <code>exec</code> methods are for executing program strings that
 * represent one or more full program statements. No value will be returned by
 * these methods so the programs are expected to have other side effects.
 *
 * @since 2.0
 */
public interface Computer<I,P extends Organism,O> extends Function<ComputerInput<I,P>,O[]> {

    class ComputerInput<I,P> {
        public final String[] argNames;
        public final P program;
        public final I[][] argValues;

        public ComputerInput(P program, String[] argNames, I[][] argValues) {
            this.argNames = argNames;
            this.program = program;
            this.argValues = argValues;
        }
        
    }
    
    @Override
    default O[] apply(ComputerInput<I,P> input) {
        try {
            return eval(input.program, input.argNames, input.argValues);
        } catch (MalformedProgramException ex) {
            throw new RuntimeException(ex + ": " + this + " with input " + input, ex);
        }
    }
    
    /**
     * Evaluates an expression multiple times with each set of argument values
     * given. The expressions may contain the use of any argument named in the
     * <code>argNames</code> array which will be provided with the associated
     * value from the <code>argValues</code> array. The returned array will be
     * the return values from the candidate program for each of the sets of
     * argument values in order. The number of evaluations that take place
     * should be equal to the size of the argNames/argValues arrays and also
     * equal to the size of the returned Object array.
     *
     * @param expression the expression string that is to be evaluated multple
     * times.
     * @param argNames an array of arguments that each of the sets of argValues
     * should be assigned to. The array should have equal length to the
     * argValues array.
     * @param argValues argument values to be assigned to the specified argument
     * names. Each element is an array of argument values for one evaluation. As
     * such there should be argValues.length evaluations and argValues.length
     * elements in the returned Object array. The array should also have equal
     * length to the argNames array.
     * @return an array of objects which are the return values from evaluating
     * the expression with each set of argument values. The type of the returned
     * Objects may vary from program to program.
     * @throws MalformedProgramException if the given expression is not valid
     * according to the language's syntax rules.
     */
    O[] eval(P expression, String[] argNames, I[][] argValues) throws MalformedProgramException;

    /**
     * Executes a program which may consist of multiple program statements,
     * multiple times. Each time the program is executed it will be provided
     * with a different set of variable values as given by the 2-dimensional
     * <code>argValues</code> array. The program is expected to have
     * side-effects and no values will be returned.
     *
     * @param program the program source code that is to be executed.
     * @param argNames an array of arguments that each of the sets of argValues
     * should be assigned to. The array should have equal length to the
     * argValues array.
     * @param argValues argument values to be assigned to the specified argument
     * names. Each element is an array of argument values for one evaluation. As
     * such there should be argValues.length evaluations and argValues.length
     * elements in the returned Object array. The array should also have equal
     * length to the argNames array.
     * @throws MalformedProgramException if the given program is not valid
     * according to the language's syntax rules.
     */
    void exec(P program, String[] argNames, I[][] argValues) throws MalformedProgramException;
}
