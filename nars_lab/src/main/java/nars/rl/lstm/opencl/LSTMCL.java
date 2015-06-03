package nars.rl.lstm.opencl;

import com.jogamp.opencl.*;
import nars.rl.lstm.AgentSupervised;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static java.lang.Math.max;
import static java.lang.Math.min;


public class LSTMCL extends AgentSupervised {
    private final CLContext cl;
    private final CLCommandQueue queue;
    private final CLProgram program;
    private final CLBuffer<IntBuffer> flagIsTargetOutputAvailable;
    private final CLBuffer<FloatBuffer> batchInputs;
    private final CLBuffer<FloatBuffer> batchTargetOutputs;
    private final CLBuffer<IntBuffer> counterBarrier4;
    private final CLBuffer<IntBuffer> counterBarrier5;
    private final CLBuffer<IntBuffer> counterBarrier6;
    private final CLBuffer<IntBuffer> counterBarrier7;

    private final double init_weight_range = 0.1;
    private double learningRate;

    private final int full_input_dimension;
    private final int output_dimension;
    private final int cell_blocks;
    private final int inputDimension;


    private CLBuffer<FloatBuffer> weightsF = null;
    private CLBuffer<FloatBuffer> weightsG = null;
    private CLBuffer<FloatBuffer> weightsOut = null;

    //partials
    private CLBuffer<FloatBuffer> dSdG = null;
    private CLBuffer<FloatBuffer> dSdF = null;

    private double SCALE_OUTPUT_DELTA = 1.0;


    CLBuffer<FloatBuffer> sumF = null, sumG = null, actF, actG, actH, context;

    private CLBuffer<FloatBuffer> inputs = null;


    private CLBuffer<FloatBuffer>  full_hidden = null;
    private CLBuffer<FloatBuffer> output;
    private CLBuffer<FloatBuffer> target_outputBuffer;

    CLBuffer<FloatBuffer> deltaH = null;
    private int maxWorkGroupSize;

    private CLKernel stage1Kernel;
    private CLKernel stage2Kernel;

    private CLKernel inputToHiddenKernel;

    private  CLBuffer<IntBuffer> counterBarrier0;
    private  CLBuffer<IntBuffer> counterBarrier1;
    private  CLBuffer<IntBuffer> counterBarrier2;
    private  CLBuffer<IntBuffer> counterBarrier3;

    public LSTMCL(Random r, int input_dimension, int output_dimension, int cell_blocks, final double initLearningRate) {
        cl = CLContext.create();

        // always make sure to release the context under all circumstances
        // not needed for this particular sample but recommented


        // select fastest device
        CLDevice device = cl.getMaxFlopsDevice();
        maxWorkGroupSize = device.getMaxWorkGroupSize();
        //out.println("using " + device);

        queue = device.createCommandQueue(0);

        String[] sources = new String[3];

        sources[0] = readStringFromInputStream(LSTMCL.class.getResourceAsStream("PragmaDefinitions.cl"));
        sources[1] = readStringFromInputStream(LSTMCL.class.getResourceAsStream("SigmoidNeuron.cl"));
        sources[2] = readStringFromInputStream(LSTMCL.class.getResourceAsStream("Kernels.cl"));

        String concatenedSource = concatenateStringsWithNewLine(sources);

        program = cl.createProgram(concatenedSource).build();

        stage1Kernel = program.createCLKernel("stage1Kernel");
        stage2Kernel = program.createCLKernel("stage2Kernel");

        inputToHiddenKernel = program.createCLKernel("InputToHiddenKernel");

        this.learningRate = initLearningRate;
        this.output_dimension = output_dimension;
        this.cell_blocks = cell_blocks;

        context = cl.createFloatBuffer(cell_blocks, READ_WRITE);

        full_input_dimension = input_dimension + cell_blocks + 1; //+1 for bias
        this.inputDimension = input_dimension;

        weightsF = cl.createFloatBuffer(cell_blocks * full_input_dimension, READ_WRITE);
        weightsG = cl.createFloatBuffer(cell_blocks * full_input_dimension, READ_WRITE);

        dSdF = cl.createFloatBuffer(cell_blocks * full_input_dimension, READ_WRITE);
        dSdG = cl.createFloatBuffer(cell_blocks * full_input_dimension, READ_WRITE);

        for (int i = 0; i < full_input_dimension; i++) {
            for (int cellIndex = 0; cellIndex < cell_blocks; cellIndex++) {
                writeArray2dFloat(weightsF.getBuffer(), cell_blocks, cellIndex, i, (float)( (r.nextDouble() * 2.0f - 1.0f) * init_weight_range));
                writeArray2dFloat(weightsG.getBuffer(), cell_blocks, cellIndex, i, (float)( (r.nextDouble() * 2.0f - 1.0f) * init_weight_range));
            }
        }

        queue.putWriteBuffer(weightsF, true);
        queue.putWriteBuffer(weightsG, true);

        weightsOut = cl.createFloatBuffer(output_dimension * (cell_blocks + 1), READ_WRITE);

        for (int j = 0; j < cell_blocks + 1; j++) {
            for (int k = 0; k < output_dimension; k++)
                writeArray2dFloat(weightsOut.getBuffer(), output_dimension, k, j, (float)( (r.nextDouble() * 2.0f - 1.0f) * init_weight_range));
        }

        queue.putWriteBuffer(weightsOut, true);

        sumF = cl.createFloatBuffer(cell_blocks, READ_WRITE);
        sumG = cl.createFloatBuffer(cell_blocks, READ_WRITE);

        zero(sumF.getBuffer());
        zero(sumG.getBuffer());

        queue.putWriteBuffer(sumF, true);
        queue.putWriteBuffer(sumG, true);

        actF = cl.createFloatBuffer(cell_blocks, READ_WRITE);
        actG = cl.createFloatBuffer(cell_blocks, READ_WRITE);
        actH = cl.createFloatBuffer(cell_blocks, READ_WRITE);

        full_hidden = cl.createFloatBuffer(cell_blocks + 1, READ_WRITE);
        output = cl.createFloatBuffer(output_dimension, READ_WRITE);
        target_outputBuffer = cl.createFloatBuffer(output_dimension, READ_WRITE);

        deltaH = cl.createFloatBuffer(cell_blocks, READ_WRITE);

        inputs = cl.createFloatBuffer(full_input_dimension, READ_WRITE);

        counterBarrier0 = cl.createIntBuffer(1, READ_WRITE);
        counterBarrier1 = cl.createIntBuffer(1, READ_WRITE);
        counterBarrier2 = cl.createIntBuffer(1, READ_WRITE);
        counterBarrier3 = cl.createIntBuffer(1, READ_WRITE);
        counterBarrier4 = cl.createIntBuffer(1, READ_WRITE);
        counterBarrier5 = cl.createIntBuffer(1, READ_WRITE);
        counterBarrier6 = cl.createIntBuffer(1, READ_WRITE);
        counterBarrier7 = cl.createIntBuffer(1, READ_WRITE);


        flagIsTargetOutputAvailable = cl.createIntBuffer(1, READ_ONLY);
        batchInputs = cl.createFloatBuffer(full_input_dimension*1, READ_ONLY);
        batchTargetOutputs = cl.createFloatBuffer(output_dimension*1, READ_ONLY);
    }

    public void delete() {
        if (!cl.isReleased())
            cl.release();
    }

    public void clear() {
        zero(context.getBuffer());

        queue.putWriteBuffer(context, true);

        //reset accumulated partials
        zero(dSdF.getBuffer());
        zero(dSdG.getBuffer());

        queue.putWriteBuffer(dSdF, true);
        queue.putWriteBuffer(dSdG, true);
    }

    public double[] predict(double[] input, final boolean requireOutput) {
        return learn(input, null, requireOutput);
    }

    public static void Display() {
        System.out.println("==============================");
        System.out.println("DAGate: todo...");
        System.out.println("\n==============================");
    }

    public double[] learn(double[] input, double[] target_output, final boolean requireOutput) {
        NonResetInteraction nonResetInteraction = new NonResetInteraction();
        nonResetInteraction.observation = input;
        nonResetInteraction.target_output = target_output;
        return learnBatch(new ArrayList<>(Arrays.asList(nonResetInteraction)), requireOutput);
    }

    public double[] learnBatch(List<NonResetInteraction> interactions, final boolean requireOutput) {

        // translate all interactions

        // TODO< reallocate buffers if necessary >


        FloatBuffer batchInputsBuffer = batchInputs.getBuffer();
        batchInputsBuffer.rewind();

        for (int i = 0; i < interactions.get(0).observation.length; i++) {
            batchInputsBuffer.put(0 * inputDimension + i, (float) interactions.get(0).observation[i++]);
        }

        IntBuffer flagIsTargetOutputAvailableBuffer = flagIsTargetOutputAvailable.getBuffer();

        flagIsTargetOutputAvailableBuffer.put(0, booleanToInt(interactions.get(0).target_output != null));


        // TODO< target output >


        queue.putWriteBuffer(flagIsTargetOutputAvailable, true);
        //queue.putWriteBuffer(this.inputs, true);
        queue.putWriteBuffer(batchInputs, true);



        int localWorkSizeForCells = min(maxWorkGroupSize, 32);  // Local work size dimensions
        int globalWorkSizeForCells = roundUp(localWorkSizeForCells, cell_blocks);   // rounded up to the nearest multiple of the localWorkSize

        int localWorkSizeForOutput = min(maxWorkGroupSize, 32);
        int globalWorkSizeForOutput = roundUp(localWorkSizeForOutput, output_dimension);


        int localWorkSizeForCombined = min(maxWorkGroupSize, 32);
        int globalWorkSizeForCombined = roundUp(localWorkSizeForOutput, max(output_dimension, cell_blocks + 1));






        queue.finish();

        // STAGE 1 KERNEL
        /////////////////


        IntBuffer counterBarrier0Buffer = counterBarrier0.getBuffer();
        counterBarrier0Buffer.put(0, globalWorkSizeForCombined);

        queue.putWriteBuffer(counterBarrier0, true);

        IntBuffer counterBarrier1Buffer = counterBarrier1.getBuffer();
        counterBarrier1Buffer.put(0, globalWorkSizeForCombined);

        queue.putWriteBuffer(counterBarrier1, true);

        IntBuffer counterBarrier2Buffer = counterBarrier2.getBuffer();
        counterBarrier2Buffer.put(0, globalWorkSizeForCombined);

        queue.putWriteBuffer(counterBarrier2, true);


        IntBuffer counterBarrier3Buffer = counterBarrier3.getBuffer();
        counterBarrier3Buffer.put(0, globalWorkSizeForCombined);

        queue.putWriteBuffer(counterBarrier3, true);

        IntBuffer counterBarrier4Buffer = counterBarrier4.getBuffer();
        counterBarrier4Buffer.put(0, globalWorkSizeForCombined);

        queue.putWriteBuffer(counterBarrier4, true);

        IntBuffer counterBarrier5Buffer = counterBarrier5.getBuffer();
        counterBarrier5Buffer.put(0, globalWorkSizeForCombined);

        queue.putWriteBuffer(counterBarrier5, true);

        IntBuffer counterBarrier6Buffer = counterBarrier6.getBuffer();
        counterBarrier6Buffer.put(0, globalWorkSizeForCombined);

        queue.putWriteBuffer(counterBarrier6, true);

        IntBuffer counterBarrier7Buffer = counterBarrier7.getBuffer();
        counterBarrier7Buffer.put(0, globalWorkSizeForCombined);

        queue.putWriteBuffer(counterBarrier7, true);


        if( interactions.get(0).target_output != null ) {
            // TODO< do this in the kernel >
            float[] floatTargetOutput = new float[interactions.get(0).target_output.length];
            for (int i = 0; i < interactions.get(0).target_output.length; i++) {
                floatTargetOutput[i] = (float) interactions.get(0).target_output[i];
            }

            target_outputBuffer.getBuffer().rewind();

            for (int i = 0; i < floatTargetOutput.length; i++) {
                target_outputBuffer.getBuffer().put(i, floatTargetOutput[i]);
            }
            queue.putWriteBuffer(target_outputBuffer, true);
        }


        zero(deltaH.getBuffer());
        queue.putWriteBuffer(deltaH, true);


        stage1Kernel.rewind();
        stage1Kernel.putArgs(context, sumF, sumG, actF, actG, actH, weightsF, weightsG, weightsOut, inputs, full_hidden, output, dSdG, dSdF);
        stage1Kernel.putArgs(flagIsTargetOutputAvailable, batchInputs, batchTargetOutputs);
        stage1Kernel.putArg(interactions.size());
        stage1Kernel.putArg(full_input_dimension).putArg(cell_blocks).putArg(output_dimension);
        stage1Kernel.putArg(inputDimension);
        stage1Kernel.putArgs(counterBarrier0, counterBarrier1, counterBarrier2, counterBarrier3, counterBarrier4, counterBarrier5, counterBarrier6, counterBarrier7);

        stage1Kernel.putArg((float) SCALE_OUTPUT_DELTA);
        stage1Kernel.putArgs(target_outputBuffer);
        stage1Kernel.putArgs(deltaH);
        stage1Kernel.putArg((float) learningRate);

        queue.put1DRangeKernel(stage1Kernel, 0, globalWorkSizeForCombined, localWorkSizeForCombined);

        queue.finish();

        //debugBuffer1d("batchInputs", batchInputs);
        //debugBuffer1d("inputs", inputs);

        int debug0 = 0;

        /*
        if( target_output != null ) {
            float[] floatTargetOutput = new float[target_output.length];
            for( int i = 0; i < target_output.length; i++ ) {
                floatTargetOutput[i] = (float)target_output[i];
            }

            target_outputBuffer.getBuffer().rewind();

            for( int i = 0; i < floatTargetOutput.length; i++ ) {
                target_outputBuffer.getBuffer().put(i, floatTargetOutput[i]);
            }
            queue.putWriteBuffer(target_outputBuffer, true);



            //output to hidden


            queue.finish();





            if(true) {
                zero(deltaH.getBuffer());
                queue.putWriteBuffer(deltaH, true);

                counterBarrier0Buffer = counterBarrier0.getBuffer();
                counterBarrier0Buffer.put(0, globalWorkSizeForCombined);

                queue.putWriteBuffer(counterBarrier0, true);

                queue.finish();

                stage2Kernel.rewind();
                stage2Kernel.putArgs(target_outputBuffer, actH, deltaH, output, weightsF, weightsG, weightsOut, dSdF, dSdG);
                stage2Kernel.putArg((float) learningRate).putArg(full_input_dimension).putArg((float) SCALE_OUTPUT_DELTA).putArg(output_dimension).putArg(cell_blocks);
                stage2Kernel.putArgs(counterBarrier0);

                queue.put1DRangeKernel(stage2Kernel, 0, globalWorkSizeForCombined, localWorkSizeForCombined);

                queue.finish();


            }
            else{
                zero(deltaH.getBuffer());
                queue.putWriteBuffer(deltaH, true);

                // we need to sync the buffer
                queue.putReadBuffer(deltaH, true);
                queue.putReadBuffer(weightsOut, true);
                queue.putReadBuffer(actH, true);
                queue.putReadBuffer(output, true);

                FloatBuffer deltaHBuffer = deltaH.getBuffer();
                FloatBuffer weightsOutBuffer = weightsOut.getBuffer();
                FloatBuffer actHBufferBuffer = actH.getBuffer();
                FloatBuffer outputBuffer = output.getBuffer();

                for (int k = 0; k < output_dimension; k++) {
                    final float dok = ((float) target_output[k] - outputBuffer.get(k)) * (float) SCALE_OUTPUT_DELTA;
                    //deltaOutput[k] = dok;

                    for (int cellIndex = 0; cellIndex < cell_blocks; cellIndex++) {
                        deltaHBuffer.put(cellIndex, deltaHBuffer.get(cellIndex) + dok * readArray2dFloat(weightsOutBuffer, output_dimension, k, cellIndex));
                        writeArray2dFloat(weightsOutBuffer, output_dimension, k, cellIndex, readArray2dFloat(weightsOutBuffer, output_dimension, k, cellIndex) + dok * actHBufferBuffer.get(cellIndex) * (float) learningRate);
                    }

                    //bias
                    writeArray2dFloat(weightsOutBuffer, output_dimension, k, cell_blocks, readArray2dFloat(weightsOutBuffer, output_dimension, k, cell_blocks) + dok * 1.0f * (float) learningRate);
                }

                queue.putWriteBuffer(weightsOut, true);
                queue.putWriteBuffer(deltaH, true);
                //HALF WORKS*
            }

            // debug
            if( false ) {
                queue.putReadBuffer(deltaH, true);

                queue.finish();

                debugBuffer1d("deltaH after stage2kernel", deltaH);
            }







            //input to hidden
            if( false) {

                inputToHiddenKernel.rewind();
                inputToHiddenKernel.putArgs(deltaH, dSdF, dSdG, weightsF, weightsG).putArg((float)learningRate).putArg(full_input_dimension).putArg(cell_blocks);

                //queue.putWriteBuffer(deltaH, true);
                queue.put1DRangeKernel(inputToHiddenKernel, 0, globalWorkSizeForCells, localWorkSizeForCells);

                queue.finish();



                //for (int cellIndex = 0; cellIndex < cell_blocks; cellIndex++) {
                //    final double deltaHForCell = deltaH[cellIndex];
                //
                //    for (int i = 0; i < full_input_dimension; i++) {
                //        weightsF[cellIndex][i] += deltaHForCell * readArray2dDouble(dSdFBufferBuffer, cell_blocks, cellIndex, i) * learningRate;
                //        weightsG[cellIndex][i] += deltaHForCell * readArray2dDouble(dSdGBufferBuffer, cell_blocks, cellIndex, i) * learningRate;
                //    }
                //}
            }




        }
        */

        //////////////////////////////////////////////////////////////

        //roll-over context to next time step
        //System.arraycopy(actH, 0, context, 0, cell_blocks);
        queue.putCopyBuffer(actH, context);


        //give results (if requested)
        if( requireOutput ) {
            queue.putReadBuffer(output, true);

            FloatBuffer outputBuffer = output.getBuffer();

            double[] outputArray = new double[output_dimension];

            for( int i = 0; i < outputArray.length; i++ ) {
                outputArray[i] = outputBuffer.get(i);
            }

            return outputArray;
        }
        else {
            return null;
        }
    }

    private static int booleanToInt(boolean value) {
        if( value ) {
            return 1;
        }
        else {
            return 0;
        }
    }

    private double[] convertFloatToDoubleArray1d(float[] array) {
        double[] result = new double[array.length];

        for( int i = 0; i < result.length; i++ ) {
            result[i] = array[i];
        }

        return result;
    }

    private double[][] getAndConvertContentOfBuffer2d(CLBuffer<FloatBuffer> buffer, int width) {
        FloatBuffer bufferBuffer = buffer.getBuffer();
        bufferBuffer.rewind();

        final int height = bufferBuffer.capacity() / width;

        double[][] result = new double[width][height];

        for( int y = 0; y < height; y++ ) {
            for( int x = 0; x < width; x++ ) {
                result[x][y] = readArray2dFloat(bufferBuffer, width, x, y);
            }
        }

        return result;
    }

    private double[] getAndConvertContentOfBuffer1d(CLBuffer<FloatBuffer> buffer) {
        FloatBuffer bufferBuffer = buffer.getBuffer();
        //bufferBuffer.rewind();

        double[] contentAsDouble = new double[bufferBuffer.capacity()];

        for( int i = 0; i < contentAsDouble.length; i++ ) {
            contentAsDouble[i] = bufferBuffer.get(i);
        }

        return contentAsDouble;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    private static void zero(FloatBuffer buffer) {
        buffer.rewind();
        while (buffer.remaining() != 0)
            buffer.put(0);
        buffer.rewind();
    }

    private static int roundUp(int groupSize, int globalSize) {
        int r = globalSize % groupSize;
        if (r == 0) {
            return globalSize;
        } else {
            return globalSize + groupSize - r;
        }
    }

    private static String readStringFromInputStream(InputStream inputStream) {
        try {
            return IOUtils.toString(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Can't read stream");
        }
    }

    private static String concatenateStringsWithNewLine(String[] sources) {
        String result = "";

        for( String iterationSource : sources ) {
            result += iterationSource + "\n";
        }

        return result;
    }

    // must be analogous to the opencl 2d array macro
    private static void writeArray2dFloat(FloatBuffer buffer, int width, int x, int y, float value) {
        buffer.put(x + y*width, value);
    }

    private static float readArray2dFloat(FloatBuffer buffer, int width, int x, int y) {
        return buffer.get(x + y*width);
    }

    private static void validateArray1d(double[] correct, double[] comparision) {
        if( correct.length != comparision.length ) {
            throw new RuntimeException("validation: unequal lengths");
        }

        for( int i = 0; i < correct.length; i++ ) {
            if( !floatEqual(correct[i], comparision[i], VALIDATION_DELTA)) {
                throw new RuntimeException("incorrect value! [" + Integer.toString(i) + "] correct=" + Double.toString(correct[i]) + " compare=" + Double.toString(comparision[i]));
            }
        }
    }

    private static boolean floatEqual(double correct, double comparision, double epsilon) {
        return correct + epsilon > comparision && correct - epsilon < comparision;
    }

    private static double VALIDATION_DELTA = 0.1f;



    private void debugBuffer1d(String name, CLBuffer<FloatBuffer> buffer) {
        queue.putReadBuffer(buffer, true);

        FloatBuffer bufferBuffer = buffer.getBuffer();
        bufferBuffer.rewind();

        System.out.println("DEBUG buffer content: " + name);

        for( int i = 0; i < bufferBuffer.capacity(); i++ ) {
            System.out.println(bufferBuffer.get(i));
        }
    }
}
