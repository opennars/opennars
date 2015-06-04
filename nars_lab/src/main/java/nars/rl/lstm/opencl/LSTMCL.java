package nars.rl.lstm.opencl;

import com.jogamp.opencl.*;
import nars.rl.lstm.AgentSupervised;
import nars.rl.lstm.Neuron;
import nars.rl.lstm.NeuronType;
import org.apache.commons.io.IOUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
    private final ValidationSimpleLSTM validation;
    private int allocatedTuplesInBatchBuffer;


    // for validation of the implementation
    public class ValidationSimpleLSTM extends AgentSupervised
    {
        private double init_weight_range = 0.1;
        public double learningRate;//0.07


        private int full_input_dimension;
        private int output_dimension;
        private int cell_blocks;
        private Neuron F;
        private Neuron G;

        public double [] context;

        public double [][] weightsF;
        public double [][] weightsG;
        public double [][] weightsOut;

        //partials (Need this for each output? Need to remind myself..)
        public double [][] dSdF;
        public double [][] dSdG;

        private NeuronType neuron_type_F = NeuronType.Sigmoid;
        private NeuronType neuron_type_G = NeuronType.Sigmoid;

        public double SCALE_OUTPUT_DELTA = 1.0;


        public double[] sumF;
        public double[] actF;
        public double[] sumG;
        public double[] actG;
        public double[] actH;
        public double[] full_hidden;
        public double[] output;
        public double[] deltaOutput;
        public double[] deltaH;
        public double[] full_input;

        public ValidationSimpleLSTM(Random r, int input_dimension, int output_dimension, int cell_blocks, final double initLearningRate)
        {
            this.learningRate = initLearningRate;
            this.output_dimension = output_dimension;
            this.cell_blocks = cell_blocks;

            context = new double[cell_blocks];

            full_input_dimension = input_dimension + cell_blocks + 1; //+1 for bias

            F = Neuron.Factory(neuron_type_F);
            G = Neuron.Factory(neuron_type_G);

            weightsF = new double[cell_blocks][full_input_dimension];
            weightsG = new double[cell_blocks][full_input_dimension];

            dSdF = new double[cell_blocks][full_input_dimension];
            dSdG = new double[cell_blocks][full_input_dimension];

            for (int i = 0; i < full_input_dimension; i++) {
                for (int j = 0; j < cell_blocks; j++) {
                    weightsF[j][i] = (r.nextDouble() * 2d - 1d) * init_weight_range;
                    weightsG[j][i] = (r.nextDouble() * 2d - 1d) * init_weight_range;
                }
            }

            weightsOut = new double[output_dimension][cell_blocks + 1];

            for (int j = 0; j < cell_blocks + 1; j++) {
                for (int k = 0; k < output_dimension; k++)
                    weightsOut[k][j] = (r.nextDouble() * 2d - 1d) * init_weight_range;
            }

            sumF = new double[cell_blocks];
            sumG = new double[cell_blocks];
        }

        public void clear()
        {

            Arrays.fill(context, 0.0);

            //reset accumulated partials
            for (int c = 0; c < cell_blocks; c++) {
                Arrays.fill(this.dSdG[c], 0.0);
                Arrays.fill(this.dSdF[c], 0.0);
            }

        }

        public double[] predict(double[] input, final boolean requireOutput)
        {
            return learn(input, null, requireOutput);
        }

        // requireOutput is unused
        public double[] learn(double[] input, double[] target_output, final boolean requireOutput) {

            final double learningRate = this.learningRate;
            final int cell_blocks = this.cell_blocks;
            final int full_input_dimension = this.full_input_dimension;

            //setup input vector


            if ((full_input == null) || (full_input.length != full_input_dimension)) {
                full_input = new double[full_input_dimension];
            }
            final double[] full_input = this.full_input;

            int loc = 0;
            for (int i = 0; i < input.length; ) {
                full_input[loc++] = input[i++];
            }
            for (int c = 0; c < context.length; ) {
                full_input[loc++] = context[c++];
            }
            full_input[loc++] = 1.0; //bias

            //cell block arrays
            if ((sumF == null) || (sumF.length!=cell_blocks)) {
                actF = new double[cell_blocks];
                actG = new double[cell_blocks];
                actH = new double[cell_blocks];
                full_hidden = new double[cell_blocks + 1];
                output = new double[output_dimension];
            }
            else {
                Arrays.fill(sumF, 0);
                Arrays.fill(actF, 0);
                Arrays.fill(sumG, 0);
                Arrays.fill(actG, 0);
                Arrays.fill(actH, 0);
            }
            final double[] full_hidden = this.full_hidden;

            //inputs to cell blocks
            inputsToCellblocksKernel();

            for (int j = 0; j < cell_blocks; j++) {
                final double actfj = actF[j] = F.Activate(sumF[j]);
                final double actgj = actG[j] = G.Activate(sumG[j]);


                actH[j] = actfj * context[j] + (1 - actfj) * actgj;
            }

            //prepare hidden layer plus bias
            Arrays.fill(full_hidden, 0);


            System.arraycopy(actH, 0, full_hidden, 0, cell_blocks);
            full_hidden[cell_blocks] = 1.0; //bias

            //calculate output

            for (int k = 0; k < output_dimension; k++)
            {
                double s = 0;
                double wk[] = weightsOut[k];
                for (int j = 0; j < cell_blocks + 1; j++)
                    s += wk[j] * full_hidden[j];

                output[k] = s;
                //output not squashed
            }

            //////////////////////////////////////////////////////////////
            //////////////////////////////////////////////////////////////
            //BACKPROP
            //////////////////////////////////////////////////////////////
            //////////////////////////////////////////////////////////////

            //scale partials
            for (int j = 0; j < cell_blocks; j++) {

                double f = actF[j];
                double df = F.Derivative(sumF[j]);
                double g = actG[j];
                double dg = G.Derivative(sumG[j]);
                double h_ = context[j]; //prev value of h

                final double[] dsg = dSdG[j];
                final double[] dsf = dSdF[j];

                for (int i = 0; i < full_input_dimension; i++) {

                    double prevdSdF = dsf[i];
                    double prevdSdG = dsg[i];
                    double in = full_input[i];

                    dsg[i] = ((1 - f)*dg*in) + (f*prevdSdG);
                    dsf[i] = ((h_- g)*df*in) + (f*prevdSdF);
                }
            }

            if (target_output != null) {

                //output to hidden

                if ((deltaOutput == null) || (deltaOutput.length!=output_dimension)) {
                    deltaOutput = new double[output_dimension];
                    deltaH = new double[cell_blocks];
                }
                else {
                    Arrays.fill(deltaOutput, 0);
                    Arrays.fill(deltaH, 0);
                }

                for (int k = 0; k < output_dimension; k++) {
                    final double dok  = deltaOutput[k] = (target_output[k] - output[k]) * SCALE_OUTPUT_DELTA;

                    final double[] wk = weightsOut[k];

                    for (int j = 0; j < cell_blocks; j++) {

                        deltaH[j] += dok * wk[j];
                        wk[j] += dok * actH[j] * learningRate;
                    }
                    //bias
                    wk[cell_blocks] += dok * 1.0 * learningRate;
                }

                //input to hidden
                for (int j = 0; j < cell_blocks; j++) {
                    final double dhj = deltaH[j];
                    final double[] dsj = dSdF[j];
                    final double[] dsd = dSdG[j];
                    final double[] wfj = weightsF[j];
                    final double[] wgj = weightsG[j];

                    for (int i = 0; i < full_input_dimension; i++) {
                        wfj[i] += dhj * dsj[i] * learningRate;
                        wgj[i] += dhj * dsd[i] * learningRate;
                    }
                }
            }

            //////////////////////////////////////////////////////////////

            //roll-over context to next time step
            System.arraycopy(actH, 0, context, 0, cell_blocks);

            //give results
            return output;
        }

        @Override
        public double[] learnBatch(List<NonResetInteraction> interactions, boolean requireOutput) throws Exception {
            throw new NotImplementedException();
        }

        public void setLearningRate(double learningRate) {
            this.learningRate = learningRate;
        }

        public void inputsToCellblocksKernel() {
            for (int i = 0; i < full_input_dimension; i++) {
                final double fi = full_input[i];

                for (int j = 0; j < cell_blocks; j++) {
                    sumF[j] += weightsF[j][i] * fi;
                    sumG[j] += weightsG[j][i] * fi;
                }
            }
        }

        // wrong transformation like in openCL
        public void inputsToCellblocksKernel2() {
            for (int j = 0; j < cell_blocks; j++) {
                double _sumF = 0.0;
                double _sumG = 0.0;

                for (int i = 0; i < full_input_dimension; i++) {
                    final double fi = full_input[i];

                    _sumF += (weightsF[j][i] * fi);
                    _sumG += (weightsG[j][i] * fi);
                }

                sumF[j] = _sumF;
                sumG[j] = _sumG;
            }
        }
    }



    private final CLContext cl;
    private final CLCommandQueue queue;
    private final CLProgram program;
    private CLBuffer<IntBuffer> flagIsTargetOutputAvailable;
    private CLBuffer<FloatBuffer> batchInputs;
    private CLBuffer<FloatBuffer> batchTargetOutputs;
    private final CLBuffer<IntBuffer> counterBarrier4;
    private final CLBuffer<IntBuffer> counterBarrier5;
    private final CLBuffer<IntBuffer> counterBarrier6;
    private final CLBuffer<IntBuffer> counterBarrier7;
    private CLBuffer<IntBuffer> counterBarrier8;
    private CLBuffer<IntBuffer> barrierResetBarrier;

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

    private CLBuffer<FloatBuffer> full_input = null;


    private CLBuffer<FloatBuffer>  full_hidden = null;
    private CLBuffer<FloatBuffer> output;
    private CLBuffer<FloatBuffer> target_outputBuffer;

    CLBuffer<FloatBuffer> deltaH = null;
    private int maxWorkGroupSize;

    private CLKernel stage1Kernel;

    private  CLBuffer<IntBuffer> counterBarrier0;
    private  CLBuffer<IntBuffer> counterBarrier1;
    private  CLBuffer<IntBuffer> counterBarrier2;
    private  CLBuffer<IntBuffer> counterBarrier3;


    public LSTMCL(Random r, int input_dimension, int output_dimension, int cell_blocks, final double initLearningRate) {
        validation = new ValidationSimpleLSTM(r, input_dimension, output_dimension, cell_blocks, initLearningRate);

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

        full_input = cl.createFloatBuffer(full_input_dimension, READ_WRITE);

        counterBarrier0 = cl.createIntBuffer(1, READ_WRITE);
        counterBarrier1 = cl.createIntBuffer(1, READ_WRITE);
        counterBarrier2 = cl.createIntBuffer(1, READ_WRITE);
        counterBarrier3 = cl.createIntBuffer(1, READ_WRITE);
        counterBarrier4 = cl.createIntBuffer(1, READ_WRITE);
        counterBarrier5 = cl.createIntBuffer(1, READ_WRITE);
        counterBarrier6 = cl.createIntBuffer(1, READ_WRITE);
        counterBarrier7 = cl.createIntBuffer(1, READ_WRITE);
        counterBarrier8 = cl.createIntBuffer(1, READ_WRITE);
        barrierResetBarrier = cl.createIntBuffer(1, READ_WRITE);


        flagIsTargetOutputAvailable = cl.createIntBuffer(1, READ_ONLY);
        batchInputs = cl.createFloatBuffer(full_input_dimension * 1, READ_ONLY);
        batchTargetOutputs = cl.createFloatBuffer(output_dimension*1, READ_ONLY);

        allocatedTuplesInBatchBuffer = 1;
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
        if( interactions.isEmpty() ) {
            return null;
        }

        // translate all interactions

        System.out.println(interactions.size());

        // reallocate buffers if necessary
        if( interactions.size() > allocatedTuplesInBatchBuffer ) {
            allocatedTuplesInBatchBuffer = interactions.size();

            flagIsTargetOutputAvailable.release();
            batchInputs.release();
            batchTargetOutputs.release();

            flagIsTargetOutputAvailable = cl.createIntBuffer(allocatedTuplesInBatchBuffer, READ_ONLY);
            batchInputs = cl.createFloatBuffer(full_input_dimension * allocatedTuplesInBatchBuffer, READ_ONLY);
            batchTargetOutputs = cl.createFloatBuffer(output_dimension*allocatedTuplesInBatchBuffer, READ_ONLY);
        }


        FloatBuffer batchInputsBuffer = batchInputs.getBuffer();
        batchInputsBuffer.rewind();

        FloatBuffer batchTargetOutputsBuffer = batchTargetOutputs.getBuffer();
        batchTargetOutputsBuffer.rewind();

        IntBuffer flagIsTargetOutputAvailableBuffer = flagIsTargetOutputAvailable.getBuffer();
        flagIsTargetOutputAvailableBuffer.rewind();

        int interactionI = 0;
        for( final NonResetInteraction iterationInteraction : interactions ) {
            for (int i = 0; i < iterationInteraction.observation.length; i++) {
                batchInputsBuffer.put(interactionI * inputDimension + i, (float) iterationInteraction.observation[i]);
            }

            if( iterationInteraction.target_output != null ) {
                for (int i = 0; i < iterationInteraction.target_output.length; i++) {
                    batchTargetOutputsBuffer.put(interactionI * output_dimension + i, (float) iterationInteraction.target_output[i]);
                }
            }

            flagIsTargetOutputAvailableBuffer.put(interactionI, booleanToInt(iterationInteraction.target_output != null));

            interactionI++;
        }







        queue.putWriteBuffer(flagIsTargetOutputAvailable, true);
        //queue.putWriteBuffer(this.inputs, true);
        queue.putWriteBuffer(batchInputs, true);
        queue.putWriteBuffer(batchTargetOutputs, true);


        int localWorkSizeForCombined = min(maxWorkGroupSize, 8);
        int globalWorkSizeForCombined = roundUp(localWorkSizeForCombined, max(output_dimension, cell_blocks + 1));






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

        IntBuffer counterBarrier8Buffer = counterBarrier8.getBuffer();
        counterBarrier8Buffer.put(0, globalWorkSizeForCombined);

        queue.putWriteBuffer(counterBarrier8, true);

        IntBuffer barrierResetBarrierBuffer = barrierResetBarrier.getBuffer();
        barrierResetBarrierBuffer.put(0, globalWorkSizeForCombined);

        queue.putWriteBuffer(barrierResetBarrier, true);


        //zero(deltaH.getBuffer());
        //queue.putWriteBuffer(deltaH, true);

        // validation







        if(false) {
            validation.context = readAndConvertFloatToDoubleArray1d(context);

            validation.weightsF = readAndConvertContentOfBuffer2d(weightsF, cell_blocks);
            validation.weightsG = readAndConvertContentOfBuffer2d(weightsG, cell_blocks);
            validation.weightsOut = readAndConvertContentOfBuffer2d(weightsOut, output_dimension);

            validation.dSdF = readAndConvertContentOfBuffer2d(dSdF, cell_blocks);
            validation.dSdG = readAndConvertContentOfBuffer2d(dSdG, cell_blocks);

            validation.sumF = readAndConvertFloatToDoubleArray1d(sumF);
            validation.actF = readAndConvertFloatToDoubleArray1d(actF);
            validation.sumG = readAndConvertFloatToDoubleArray1d(sumG);
            validation.actG = readAndConvertFloatToDoubleArray1d(actG);
            validation.actH = readAndConvertFloatToDoubleArray1d(actH);
            validation.full_hidden = readAndConvertFloatToDoubleArray1d(full_hidden);
            validation.output = readAndConvertFloatToDoubleArray1d(output);
            //public double[] deltaOutput =  = readAndConvertFloatToDoubleArray1d(delta_output);
            validation.deltaH = readAndConvertFloatToDoubleArray1d(deltaH);
            //validation.full_input=readAndConvertFloatToDoubleArray1d(full_input);
        }

        stage1Kernel.rewind();
        stage1Kernel.putArgs(context, sumF, sumG, actF, actG, actH, weightsF, weightsG, weightsOut, full_input, full_hidden, output, dSdG, dSdF);
        stage1Kernel.putArgs(flagIsTargetOutputAvailable, batchInputs, batchTargetOutputs);
        stage1Kernel.putArg(interactions.size());
        stage1Kernel.putArg(full_input_dimension).putArg(cell_blocks).putArg(output_dimension);
        stage1Kernel.putArg(inputDimension);
        stage1Kernel.putArgs(counterBarrier0, counterBarrier1, counterBarrier2, counterBarrier3, counterBarrier4, counterBarrier5, counterBarrier6, counterBarrier7, counterBarrier8, barrierResetBarrier);

        stage1Kernel.putArg((float) SCALE_OUTPUT_DELTA);
        stage1Kernel.putArgs(deltaH);
        stage1Kernel.putArg((float) learningRate);
        stage1Kernel.putArg(globalWorkSizeForCombined);

        CLEventList kernelFinished = new CLEventList(1);

        queue.put1DRangeKernel(stage1Kernel, 0, globalWorkSizeForCombined, localWorkSizeForCombined, kernelFinished);

        // wait for kernel with an event
        queue.putWaitForEvents(kernelFinished, true);

        kernelFinished.release();


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
        //queue.putCopyBuffer(actH, context);


        // validation
        if(false) {
            double[] validationResult = validation.learn(interactions.get(0).observation, interactions.get(0).target_output, requireOutput);

            validateBuffer1d(validation.actH, actH, "actH");
            validateBuffer1d(validation.context, context, "context");

            //validation.weightsF = readAndConvertContentOfBuffer2d(weightsF, cell_blocks);
            //validation.weightsG = readAndConvertContentOfBuffer2d(weightsG, cell_blocks);
            //validation.weightsOut = readAndConvertContentOfBuffer2d(weightsOut, output_dimension);

            //validation.dSdF = readAndConvertContentOfBuffer2d(dSdF, cell_blocks);
            //validation.dSdG = readAndConvertContentOfBuffer2d(dSdG, cell_blocks);

            validateBuffer1d(validation.full_input, full_input, "full_input");
            validateBuffer1d(validation.sumF, sumF, "sumF");
            validateBuffer1d(validation.actF, actF, "actF");
            validateBuffer1d(validation.sumG, sumG, "sumG");
            validateBuffer1d(validation.actG, actG, "actG");

            validateBuffer1d(validation.full_hidden, full_hidden, "full_hidden");
            validateBuffer1d(validation.output, output, "output");
            //validateBuffer1d(public double[] deltaOutput =  = readAndConvertFloatToDoubleArray1d(delta_output);
            validateBuffer1d(validation.deltaH, deltaH, "deltaH");
        }






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

    private double[] readAndConvertFloatToDoubleArray1d(CLBuffer<FloatBuffer> buffer) {
        queue.putReadBuffer(buffer, true);

        FloatBuffer bufferBuffer = buffer.getBuffer();
        bufferBuffer.rewind();

        double[] result = new double[bufferBuffer.capacity()];

        for( int i = 0; i < result.length; i++ ) {
            result[i] = bufferBuffer.get(i);
        }

        return result;
    }

    private double[][] readAndConvertContentOfBuffer2d(CLBuffer<FloatBuffer> buffer, int width) {
        queue.putReadBuffer(buffer, true);

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

        for( int i = 0; i < buffer.capacity(); i++ ) {
            buffer.put(i, 0.0f);
        }

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

    private static void validateArray1d(double[] correct, double[] comparision, String name) {
        if( correct.length != comparision.length ) {
            throw new RuntimeException("validation: unequal lengths");
        }

        for( int i = 0; i < correct.length; i++ ) {
            if( !floatEqual(correct[i], comparision[i], VALIDATION_DELTA)) {
                throw new RuntimeException("incorrect value! (" + name + ") [" + Integer.toString(i) + "] correct=" + Double.toString(correct[i]) + " compare=" + Double.toString(comparision[i]));
            }
        }
    }

    private static boolean floatEqual(double correct, double comparision, double epsilon) {
        return correct + epsilon > comparision && correct - epsilon < comparision;
    }

    private static double VALIDATION_DELTA = 0.01f;



    private void debugBuffer1d(String name, CLBuffer<FloatBuffer> buffer) {
        queue.putReadBuffer(buffer, true);

        FloatBuffer bufferBuffer = buffer.getBuffer();
        bufferBuffer.rewind();

        System.out.println("DEBUG buffer content: " + name);

        for( int i = 0; i < bufferBuffer.capacity(); i++ ) {
            System.out.println(bufferBuffer.get(i));
        }
    }

    private void validateBuffer1d(double[] reference, CLBuffer<FloatBuffer> buffer, String name) {
        double[] comparision = readAndConvertFloatToDoubleArray1d(buffer);

        validateArray1d(reference, comparision, name);


    }
}
