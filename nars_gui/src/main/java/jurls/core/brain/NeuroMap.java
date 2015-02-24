/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.brain;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import jurls.core.approximation.ParameterizedFunctionGenerator;
import jurls.core.approximation.ParameterizedFunction;

/**
 *
 * @author thorsten
 */
public class NeuroMap {


    




    public static class InputOutput {

        //TODO might be better as a 2D array
        public double[] input;
        public double[] output;
                
    }
    
    private final ParameterizedFunction[] functions;
    
    /** two deque's to shuffle items instead of randomly selecting an index,
     *  we can use a ring buffer which will remain within a constant size and
        have constant O(1) add/removal */
    private final List<InputOutput> memory;
    int memIndex = 0;
    
    
    private final int capacity;
    private final Random random = new Random();
    private int iterations = 0;

    public int getCapacity() {
        return capacity;
    }

    public int getDimensions(boolean input, boolean output) {
        if (memory.isEmpty()) return 0;
        InputOutput z = memory.get(0);
        if (z == null) return 0;
        
        int t = 0;
        if (z.input != null)
            if (input) t += z.input.length;
        if (z.output!=null)
            if (output) t += z.output.length;
        return t;
    }
    
    public int getIndex() {
        return memIndex;
    }

    
    public NeuroMap(
            int numInputs,
            int numOutputs,
            ParameterizedFunctionGenerator g,
            int memoryCapacity
    ) {
        this.memory = new ArrayList(memoryCapacity);
        this.capacity = memoryCapacity;
        
        functions = new ParameterizedFunction[numOutputs];
        for (int i = 0; i < numOutputs; ++i) {
            functions[i] = g.generate(numInputs);
        }

        new Thread() {

            @Override
            public void run() {
                while (true) {
                    final InputOutput io = randomMemory();

                    if ((io == null) || (io.input == null) || (io.output == null)) 
                        continue;

                    assert io.output.length == functions.length;

                    for (int i = 0; i < functions.length; ++i) {
                        functions[i].learn(io.input, io.output[i]);
                    }

                    iterations++;
                }
            }
        }.start();
    
    
    }

    public InputOutput getMemory(int y) {
        int ms = memory.size();
        if (ms == 0) return null;        
        if (y < 0) y = -y;
        return memory.get(y % ms);
    }
    
    private InputOutput randomMemory() {
        final int size = memory.size();
        
        if (size == 0) return null;
        
        int i = random.nextInt();
        
        return getMemory(i);
    }
    

    public InputOutput newMemory() {
        if (memory.size() >= capacity ){
            return memory.get((memIndex++)%capacity);
        }
        else {
            InputOutput io = new InputOutput();
            memory.add(io);
            memIndex = memory.size();
            return io;
        }
    }

    
    public void learn(double[] i, double... o) {
        InputOutput recycled = newMemory();
        recycled.input = i;
        recycled.output = o;
    }
    
    
    public double[] value(double[] input, double[] result) {
        if ((result == null) || (result.length != functions.length))
            result = new double[functions.length];
        
        for (int i = 0; i < functions.length; ++i) {
            result[i] = functions[i].value(input);
        }
        return result;        
    }
    @Deprecated public double[] value(double[] input) {
        return value(input, null);
    }

    public int getIterations() {
        return iterations;
    }
}
