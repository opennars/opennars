/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.brain;

import jurls.core.approximation.ParameterizedFunction;
import jurls.core.approximation.ParameterizedFunctionGenerator;

import java.util.Random;

/**
 *
 * @author thorsten
 */
public class NeuroMap {


    public int getDimensions(boolean includeInput, boolean includeOutput) {
        int t = 0;
        if (includeInput) t += memory[0].input.length;
        if (includeOutput) t += memory[0].output.length;
        return t;
    }

    public static class InputOutput {

        public double[] input;
        public double[] output;
    }

    public final ParameterizedFunction[] functions;
    public final InputOutput[] memory;
    private int numElements = 0;
    private final Random random = new Random();
    private int iterations = 0;
    private boolean running = true;
    private int iterationsPerSecondCounter = 0;
    private int iterationsPerSecond = 0;
    private long t0 = System.currentTimeMillis();

    public int getCapacity() { return memory.length; }

    public NeuroMap(
            int numInputs,
            int numOutputs,
            ParameterizedFunctionGenerator g,
            int memoryCapacity
    ) {
        memory = new InputOutput[memoryCapacity];

        functions = new ParameterizedFunction[numOutputs];
        for (int i = 0; i < numOutputs; ++i) {
            functions[i] = g.generate(numInputs);
        }

        new Thread() {

            @Override
            public void run() {
                while (running) {
                    if (numElements == 0) {
                        continue;
                    }

                    InputOutput io = randomMemory();

                    assert io.output.length == functions.length;

                    for (int i = 0; i < functions.length; ++i) {
                        functions[i].learn(io.input, io.output[i]);
                    }

                    iterations++;
                    iterationsPerSecondCounter++;

                    long t1 = System.currentTimeMillis();
                    if (t1 - t0 > 1000) {
                        t0 = t1;
                        iterationsPerSecond = iterationsPerSecondCounter;
                        iterationsPerSecondCounter = 0;
                    }
                }
            }
        }.start();

    }

    public void stop() {
        running = false;
    }

    private InputOutput randomMemory() {
        return memory[random.nextInt(numElements)];
    }

    public void learn(double[] i, double[] o) {
        if (numElements >= memory.length) {
            InputOutput io = randomMemory();
            io.input = i;
            io.output = o;
        } else {
            memory[numElements] = new InputOutput();
            memory[numElements].input = i;
            memory[numElements].output = o;
            numElements++;
        }
    }

    public void value(double[] output,double[] input) {
        for (int i = 0; i < functions.length; ++i) {
            output[i] = functions[i].value(input);
        }
    }

    @SuppressWarnings("HardcodedFileSeparator")
    public String getDebugString() {
        return "iter : " + iterations
                + " (" + iterationsPerSecond + "/s); "
                + "elem : " + numElements;
    }
    public int getIndex() {
        return numElements;
    }

}
