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

import java.util.Arrays;
import objenome.solver.evolve.Individual;
import objenome.solver.evolve.source.SourceGenerator;

/**
 * A BrainfuckInterpreter provides the facility to execute programs in the
 * esoteric Brainfuck programming language. Memory is provided in the form of a
 * 30,000 element byte array which the programs manipulate. The
 * <code>eval</code> interpreter functions are not supported since Brainfuck
 * provides no expressions that can be evaluated. The <code>exec</code> methods
 * should be used, with the memory retrievable after execution. The
 * <code>argValues</code> given to the <code>exec</code> methods will be used to
 * populate the first elements of the memory array in sequence. The
 * <code>argNames</code> array is not used.
 *
 * <h4>Supported language syntax</h4>
 *
 * <table>
 * <tr>
 * <th>Syntax</th>
 * <th>Effect</th>
 * </tr>
 * <tr>
 * <td>&lt;</td>
 * <td>Increments the pointer. The pointer wraps around to 0 if it is to become
 * larger than the memory capacity.</td>
 * </tr>
 * <tr>
 * <td>&gt;</td>
 * <td>Decrements the pointer. The pointer wraps around to point to the last
 * memory address if it would otherwise become negative.</td>
 * </tr>
 * <tr>
 * <td>+</td>
 * <td>Increments the value of the memory location addressed by the current
 * pointer.</td>
 * </tr>
 * <tr>
 * <td>-</td>
 * <td>Decrements the value of the memory location addressed by the current
 * pointer.</td>
 * </tr>
 * <tr>
 * <td>,</td>
 * <td>Not currently supported.</td>
 * </tr>
 * <tr>
 * <td>.</td>
 * <td>Not currently supported.</td>
 * </tr>
 * <tr>
 * <td>[</td>
 * <td>If value of current memory location is 0 then jump forward to matching
 * ']'.</td>
 * </tr>
 * <tr>
 * <td>]</td>
 * <td>Jump back to just before matching '['.</td>
 * </tr>
 * </table>
 *
 * @since 2.0
 */
public class BrainfuckInterpreter<T extends Individual> implements Computer<Byte,T,Object> {

    // The indexable memory available to programs.
    private final byte[] memory;

    // Pointer to current memory address.
    private int pointer;

    private SourceGenerator<T> generator;

    /**
     * Constructs a BrainfuckInterpreter with a 30,000 element byte array for
     * memory.
     *
     * @param generator the SourceGenerator to use to convert individuals to
     * Brainfuck source code
     */
    public BrainfuckInterpreter(SourceGenerator<T> generator) {
        this(generator, 30000);
    }

    /**
     * Constructs a BrainfuckInterpreter with a byte array for memory with the
     * given capacity.
     *
     * @param generator the SourceGenerator to use to convert individuals to
     * Brainfuck source code
     * @param memorySize the size of the byte array to provide programs with for
     * memory
     */
    public BrainfuckInterpreter(SourceGenerator<T> generator, int memorySize) {
        memory = new byte[memorySize];
        pointer = 0;
    }

    /*
     * Resets the memory array to be filled with 0 bytes, and the pointer to
     * address element 0.
     */
    private void reset() {
        Arrays.fill(memory, (byte) 0);
        pointer = 0;
    }

    /**
     * Not currently supported by BrainfuckInterpreter. Calling will throw an
     * IllegalStateException.
     */
    @Override
    public Object[] eval(T program, String[] argNames, Byte[][] argValues) {
        throw new IllegalStateException("method not supported");
    }

    /**
     * Executes the given Brainfuck program upon the memory byte array. The
     * given <code>argValues</code> will be used to populate the first elements
     * of the memory array in sequence. All other elements of the memory array
     * will be set to 0 byte and the pointer will also be reset to address 0
     * before execution. The <code>argNames</code> argument is not used.
     *
     * @param program a valid Brainfuck program that is to be executed
     * @param argNames not used in this implementation
     * @param argValues an array of values which can be considered the inputs to
     * the program. They will populate the first elements of the memory array in
     * sequence before execution starts.
     */
    @Override
    public void exec(T program, String[] argNames, Byte[][] argValues) {
        int noParamSets = argValues.length;

        for (int i = 0; i < noParamSets; i++) {
            Byte[] paramSet = argValues[i];

            // Reset the environment.
            reset();

            // Set inputs as first x memory cells.
            for (int j = 0; j < paramSet.length; j++) {
                memory[i] = paramSet[j];
            }

            // Get the program source code.
            String source = generator.getSource(program);

            // Execute the source.
            execute(source);
        }
    }

    /*
     * Parses and executes the given source string as a Brainfuck program.
     */
    private void execute(final String source) {
        if (source == null) {
            return;
        }

        for (int i = 0; i < source.length(); i++) {
            final char c = source.charAt(i);

            switch (c) {
                case '>':
                    pointer = ++pointer % memory.length;
                    break;
                case '<':
                    pointer--;
                    if (pointer < 0) {
                        pointer = memory.length - 1;
                    }
                    break;
                case '+':
                    memory[pointer]++;
                    break;
                case '-':
                    memory[pointer]--;
                    break;
                case ',':
                    // Not supported.
                    break;
                case '.':
                    // Not supported.
                    // System.out.print((char) memory[pointer]);
                    break;
                case '[':
                    final int bracketIndex = findClosingBracket(source.substring(i + 1)) + (i + 1);
                    final String loopSource = source.substring((i + 1), bracketIndex);
                    while (memory[pointer] != 0) {
                        execute(loopSource);
                    }
                    i = bracketIndex;
                    break;
                case ']':
                    // Implemented as part of '['.
                    break;
                default:
                    // Ignore all other characters.
                    break;
            }
        }
    }

    /*
     * Locate the matching bracket in the given source.
     */
    private int findClosingBracket(final String source) {
        int open = 1;
        for (int i = 0; i < source.length(); i++) {
            final char c = source.charAt(i);

            if (c == '[') {
                open++;
            } else if (c == ']') {
                open--;
                if (open == 0) {
                    return i;
                }
            }
        }
        // There is no closing bracket.
        return -1;
    }

    /**
     * Returns the byte array which is providing indexed memory for the
     * programs. The array will be cleared for each execution.
     *
     * @return the program's indexed memory.
     */
    public byte[] getMemory() {
        return memory;
    }

    /**
     * Returns the source generator being used to convert individuals to source
     * code.
     *
     * @return the current source generator
     */
    public SourceGenerator<T> getSourceGenerator() {
        return generator;
    }

    /**
     * Sets the source generator to use to convert individuals to source code
     *
     * @param the source generator to set
     */
    public void setSourceGenerator(SourceGenerator<T> generator) {
        this.generator = generator;
    }
}
