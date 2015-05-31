package nars.rl.lstm.opencl;

import com.jogamp.opencl.*;
import nars.rl.lstm.IAgentSupervised;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.DoubleBuffer;
import java.util.Arrays;
import java.util.Random;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static java.lang.Math.min;
import static java.lang.System.nanoTime;


public class LSTMCL implements IAgentSupervised {

    private final CLContext cl;
    private final CLCommandQueue queue;
    private final CLProgram program;


    private double init_weight_range = 0.1;
    public double learningRate;//0.07


    private int full_input_dimension;
    private int output_dimension;
    private int cell_blocks;


    //private double [][] weightsF;
    //private double [][] weightsG;
    //private double [][] weightsOut;

    private CLBuffer<DoubleBuffer> weightsFBuffer = null;
    private CLBuffer<DoubleBuffer> weightsGBuffer = null;
    private CLBuffer<DoubleBuffer> weightsOutBuffer = null;

    //partials (Need this for each output? Need to remind myself..)
    //private double [][] dSdF;
    //private double [][] dSdG;

    private CLBuffer<DoubleBuffer> dSdGBuffer = null;
    private CLBuffer<DoubleBuffer> dSdFBuffer = null;

    private double SCALE_OUTPUT_DELTA = 1.0;


    CLBuffer<DoubleBuffer> sumFBuffer = null, sumGBuffer = null, actFBuffer, actGBuffer, actHBuffer, context;
    //private double[] sumF;
    //private double[] sumG;

    private CLBuffer<DoubleBuffer> fullInputBuffer = null;


    private CLBuffer<DoubleBuffer>  full_hidden = null;
    private CLBuffer<DoubleBuffer> output;
    private double[] deltaOutput;
    CLBuffer<DoubleBuffer> deltaH = null;
    private double[] full_input;
    private int maxWorkGroupSize;

    private CLKernel activateKernel;
    private CLKernel inputsToCellblocksKernel;
    private CLKernel backpropScalePartialsKernel;
    private CLKernel inputToHiddenKernel;
    private CLKernel prepareHiddenLayerPlusBias;
    private CLKernel calculateOutputKernel;


    public LSTMCL(Random r, int input_dimension, int output_dimension, int cell_blocks, final double initLearningRate) {

        // set up (uses default CLPlatform and creates context for all devices)
        cl = CLContext.create();
        //out.println("created " + context);

        // always make sure to release the context under all circumstances
        // not needed for this particular sample but recommented


        // select fastest device
        CLDevice device = cl.getMaxFlopsDevice();
        maxWorkGroupSize = device.getMaxWorkGroupSize();
        //out.println("using " + device);

        // create command queue on device.
        queue = device.createCommandQueue();

//            //final double actfj = actF[j] = F.Activate(sumFF[j]);
//            //final double actgj = actG[j] = G.Activate(sumGG[j]);
//            actH[j] = actfj * context[j] + (1 - actfj) * actgj;

        String[] sources = new String[6];

        sources[0] = readStringFromInputStream(LSTMCL.class.getResourceAsStream("PragmaDefinitions.cl"));
        sources[1] = readStringFromInputStream(LSTMCL.class.getResourceAsStream("SigmoidNeuron.cl"));
        sources[2] = readStringFromInputStream(LSTMCL.class.getResourceAsStream("KernelActivate.cl"));
        sources[3] = readStringFromInputStream(LSTMCL.class.getResourceAsStream("InputsToCellblocksKernel.cl"));
        sources[4] = readStringFromInputStream(LSTMCL.class.getResourceAsStream("BackpropScalePartialsKernel.cl"));
        sources[5] = readStringFromInputStream(LSTMCL.class.getResourceAsStream("Kernels.cl"));

        String concatenedSource = concatenateStringsWithNewLine(sources);

        program = cl.createProgram(concatenedSource).build();

        activateKernel = program.createCLKernel("activateKernel");
        inputsToCellblocksKernel = program.createCLKernel("InputsToCellblocksKernel");
        backpropScalePartialsKernel = program.createCLKernel("BackpropScalePartialsKernel");
        inputToHiddenKernel = program.createCLKernel("InputToHiddenKernel");
        prepareHiddenLayerPlusBias = program.createCLKernel("PrepareHiddenLayerPlusBias");
        calculateOutputKernel = program.createCLKernel("CalculateOutputKernel");

        this.learningRate = initLearningRate;
        this.output_dimension = output_dimension;
        this.cell_blocks = cell_blocks;

        context = cl.createDoubleBuffer(cell_blocks, READ_WRITE);

        full_input_dimension = input_dimension + cell_blocks + 1; //+1 for bias

        weightsFBuffer = cl.createDoubleBuffer(cell_blocks*full_input_dimension, READ_WRITE);
        weightsGBuffer = cl.createDoubleBuffer(cell_blocks*full_input_dimension, READ_WRITE);


        //dSdF = new double[cell_blocks][full_input_dimension];
        //dSdG = new double[cell_blocks][full_input_dimension];

        dSdFBuffer = cl.createDoubleBuffer(cell_blocks*full_input_dimension, READ_WRITE);
        dSdGBuffer = cl.createDoubleBuffer(cell_blocks*full_input_dimension, READ_WRITE);

        for (int i = 0; i < full_input_dimension; i++) {
            for (int cellIndex = 0; cellIndex < cell_blocks; cellIndex++) {
                writeArray2dDouble(weightsFBuffer.getBuffer(), cell_blocks, cellIndex, i, (r.nextDouble() * 2d - 1d) * init_weight_range);
                writeArray2dDouble(weightsGBuffer.getBuffer(), cell_blocks, cellIndex, i, (r.nextDouble() * 2d - 1d) * init_weight_range);
            }
        }

        queue.putWriteBuffer(dSdFBuffer, false);
        queue.putWriteBuffer(dSdGBuffer, false);

        weightsOutBuffer = cl.createDoubleBuffer(output_dimension*(cell_blocks + 1), READ_WRITE);

        for (int j = 0; j < cell_blocks + 1; j++) {
            for (int k = 0; k < output_dimension; k++)
                writeArray2dDouble(weightsOutBuffer.getBuffer(), output_dimension, k, j, (r.nextDouble() * 2d - 1d) * init_weight_range);
        }

        queue.putWriteBuffer(weightsOutBuffer, false);
    }

    public void delete() {
        if (!cl.isReleased())
            cl.release();
    }

    public void clear() {
        zero(context.getBuffer());

        queue.putWriteBuffer(context, false);

        //reset accumulated partials
        zero(dSdFBuffer.getBuffer());
        zero(dSdGBuffer.getBuffer());

        queue.putWriteBuffer(dSdFBuffer, false);
        queue.putWriteBuffer(dSdGBuffer, false);
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
        final double learningRate = this.learningRate;
        final int cell_blocks = this.cell_blocks;
        final int full_input_dimension = this.full_input_dimension;

        //setup input vector


        if ((full_input == null) || (full_input.length != full_input_dimension)) {
            full_input = new double[full_input_dimension];

            fullInputBuffer = cl.createDoubleBuffer(full_input_dimension, READ_ONLY);
        }
        final double[] full_input = this.full_input;



        int loc = 0;
        for (int i = 0; i < input.length; ) {
            full_input[loc++] = input[i++];
        }

        DoubleBuffer contextBuffer = context.getBuffer();
        for (int c = 0; c < cell_blocks; ) {
            full_input[loc++] = contextBuffer.get(c++);
        }
        full_input[loc++] = 1.0; //bias

        // copy full input
        DoubleBuffer fullInputBufferBuffer = fullInputBuffer.getBuffer();
        fullInputBufferBuffer.rewind();
        fullInputBufferBuffer.put(full_input);


        //cell block arrays
        if ((sumFBuffer == null)) {
            sumFBuffer = cl.createDoubleBuffer(cell_blocks, READ_WRITE);
            sumGBuffer = cl.createDoubleBuffer(cell_blocks, READ_WRITE);
            //sumF = new double[cell_blocks];
            //sumG = new double[cell_blocks];

            actFBuffer = cl.createDoubleBuffer(cell_blocks, READ_WRITE);
            actGBuffer = cl.createDoubleBuffer(cell_blocks, READ_WRITE);
            actHBuffer = cl.createDoubleBuffer(cell_blocks, READ_WRITE);
            //actF = new double[cell_blocks];
            //actG = new double[cell_blocks];
            //actH = new double[cell_blocks];

            full_hidden = cl.createDoubleBuffer(cell_blocks+1, READ_WRITE);
            output = cl.createDoubleBuffer(output_dimension, READ_WRITE);
        }
        else {
            // these will be set in the InputToCellblocks kernel, so no need to zero it here
            //zero(sumFBuffer.getBuffer());
            //zero(sumGBuffer.getBuffer());

            //thse will be set in the kernel, so no need to zero it here
            //zero(actF.getBuffer());
            //zero(actG.getBuffer());
            //zero(actH.getBuffer());
        }

        int localWorkSizeForCells = min(maxWorkGroupSize, 256);  // Local work size dimensions
        int globalWorkSizeForCells = roundUp(localWorkSizeForCells, cell_blocks);   // rounded up to the nearest multiple of the localWorkSize

        int localWorkSizeForOutput = min(maxWorkGroupSize, 256);
        int globalWorkSizeForOutput = roundUp(localWorkSizeForOutput, output_dimension);

        inputsToCellblocksKernel.rewind();
        inputsToCellblocksKernel.putArgs(sumFBuffer, sumGBuffer, weightsFBuffer, weightsGBuffer, fullInputBuffer).putArg(full_input_dimension).putArg(cell_blocks);

        DoubleBuffer sumFBufferBuffer = sumFBuffer.getBuffer();
        DoubleBuffer sumGBufferBuffer = sumGBuffer.getBuffer();

        //INPUTS TO CELL BLOCKS KERNEL
        //for (int cellIndex = 0; cellIndex < cell_blocks; cellIndex++) {
        //    for (int i = 0; i < full_input_dimension; i++)
        //    {
        //        final double fi = full_input[i];

        //        sumFBufferBuffer.put(cellIndex, sumFBufferBuffer.get(cellIndex) + weightsF[cellIndex][i] * fi);
        //        //sumFF[cellIndex] += weightsF[cellIndex][i] * fi;
        //        sumGBufferBuffer.put(cellIndex, sumGBufferBuffer.get(cellIndex) + weightsG[cellIndex][i] * fi);
        //        //sumGG[cellIndex] += weightsG[cellIndex][i] * fi;
        //    }
        //}


        //INVOKE ACTIVATION KERNEL:
        //  inputs: sumF, sumG
        //  outputs: actF, actG, actH
//        for (int j = 0; j < cell_blocks; j++) {
//            final double actfj = actF[j] = F.Activate(sf.get(j));
//            final double actgj = actG[j] = G.Activate(sg.get(j));
//            //final double actfj = actF[j] = F.Activate(sumFF[j]);
//            //final double actgj = actG[j] = G.Activate(sumGG[j]);
//
//            actH[j] = actfj * context[j] + (1 - actfj) * actgj;
//        }

        // and map the buffers to its input parameters.
        activateKernel.rewind();
        activateKernel.putArgs(context, sumFBuffer, sumGBuffer, actFBuffer, actGBuffer, actHBuffer).putArg(cell_blocks);


        // asynchronous write of data to GPU device,
        // followed by blocking read to get the computed results back.
        long time = nanoTime();
        queue
                //.putWriteBuffer(weightsFBuffer, false)
                //.putWriteBuffer(weightsGBuffer, false)
                .putWriteBuffer(fullInputBuffer, false)
                .put1DRangeKernel(inputsToCellblocksKernel, 0, globalWorkSizeForCells, localWorkSizeForCells)

                .putWriteBuffer(context, false)
                .put1DRangeKernel(activateKernel, 0, globalWorkSizeForCells, localWorkSizeForCells);
                //.putReadBuffer(actFBuffer, true)
                //.putReadBuffer(actGBuffer, true)
                ///.putReadBuffer(actHBuffer, true);
        time = nanoTime() - time;

        //System.out.println(time);

        /*
        {
            // debugging

            queue.putReadBuffer(actHBuffer, false);

            double[] actHHost = new double[cell_blocks];

            for( int i = 0; i < cell_blocks; i++ ) {
                actHHost[i] = actHBuffer.getBuffer().get(i);
            }

            int debugHere = 0;
        }
        */



        //prepare hidden layer plus bias
        prepareHiddenLayerPlusBias.rewind();
        prepareHiddenLayerPlusBias.putArgs(actHBuffer, full_hidden).putArg(cell_blocks);

        queue.put1DRangeKernel(prepareHiddenLayerPlusBias, 0, globalWorkSizeForCells, localWorkSizeForCells);


        //Arrays.fill(full_hidden, 0);
        //
        //DoubleBuffer actHBufferBuffer = actHBuffer.getBuffer();
        //for (int i = 0; i < cell_blocks; i++)
        //    full_hidden[i] = actHBufferBuffer.get(i);
        ////System.arraycopy(actH, 0, full_hidden, 0, cell_blocks);
        //
        //full_hidden[cell_blocks] = 1.0; //bias

        //calculate output


        calculateOutputKernel.rewind();
        calculateOutputKernel.putArgs(weightsOutBuffer, full_hidden, output).putArg(output_dimension).putArg(cell_blocks);

        queue.put1DRangeKernel(calculateOutputKernel, 0, globalWorkSizeForOutput, localWorkSizeForOutput);

        //for (int k = 0; k < output_dimension; k++)
        //{
        //    double s = 0;
        //    for (int j = 0; j < cell_blocks + 1; j++)
        //        s +=  weightsOut[k][j] * full_hidden[j];
        //
        //    output[k] = s;
        //    //output not squashed
        //}

        //////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////
        //BACKPROP
        //////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////

        //SCALE PARTIALS KERNEL
        backpropScalePartialsKernel.rewind();
        backpropScalePartialsKernel.putArgs(actFBuffer, sumFBuffer, actGBuffer, sumGBuffer, context, dSdGBuffer, dSdFBuffer, fullInputBuffer).putArg(full_input_dimension).putArg(cell_blocks);

        queue
                .put1DRangeKernel(backpropScalePartialsKernel, 0, globalWorkSizeForCells, localWorkSizeForCells);
                //.putReadBuffer(dSdFBuffer, true)
                //.putReadBuffer(dSdGBuffer, true);

        /*
        DoubleBuffer actFBufferBuffer = actFBuffer.getBuffer();
        DoubleBuffer actGBufferBuffer = actGBuffer.getBuffer();
        for (int cellI = 0; cellI < cell_blocks; cellI++) {

            double f = actFBufferBuffer.get(cellI);
            double df = F.Derivative(sumFBufferBuffer.get(cellI));
            double g = actGBufferBuffer.get(cellI);
            double dg = G.Derivative(sumGBufferBuffer.get(cellI));
            double h_ = cb.get(cellI); //prev value of h

            final double[] dsg = dSdG[cellI];
            final double[] dsf = dSdF[cellI];

            for (int i = 0; i < full_input_dimension; i++) {

                double prevdSdF = dsf[i];
                double prevdSdG = dsg[i];
                double in = full_input[i];

                dsg[i] = ((1 - f)*dg*in) + (f*prevdSdG);
                dsf[i] = ((h_- g)*df*in) + (f*prevdSdF);
            }
        }*/

        //DoubleBuffer dSdFBufferBuffer = dSdFBuffer.getBuffer();
        //DoubleBuffer dSdGBufferBuffer = dSdGBuffer.getBuffer();

        if( target_output != null ) {

            //output to hidden

            if ((deltaOutput == null) || (deltaOutput.length!=output_dimension)) {
                deltaOutput = new double[output_dimension];
                deltaH = cl.createDoubleBuffer(cell_blocks, READ_WRITE);
            }
            else {
                Arrays.fill(deltaOutput, 0);
                zero(deltaH.getBuffer());
            }

            // we need to sync the buffer
            queue.putReadBuffer(deltaH, false);
            queue.putReadBuffer(weightsOutBuffer, false);
            queue.putReadBuffer(actHBuffer, false);
            queue.putReadBuffer(output, false);

            DoubleBuffer deltaHBuffer = deltaH.getBuffer();
            DoubleBuffer weightsOutBufferBuffer = weightsOutBuffer.getBuffer();
            DoubleBuffer actHBufferBuffer = actHBuffer.getBuffer();
            DoubleBuffer outputBuffer = output.getBuffer();

            for (int k = 0; k < output_dimension; k++) {
                final double dok  = deltaOutput[k] = (target_output[k] - outputBuffer.get(k)) * SCALE_OUTPUT_DELTA;

                for (int cellIndex = 0; cellIndex < cell_blocks; cellIndex++) {
                    deltaHBuffer.put(cellIndex, deltaHBuffer.get(cellIndex) + dok * readArray2dDouble(weightsOutBufferBuffer, output_dimension, k, cellIndex));
                    writeArray2dDouble(weightsOutBufferBuffer, output_dimension, k, cellIndex,      readArray2dDouble(weightsOutBufferBuffer, output_dimension, k, cellIndex) + dok * actHBufferBuffer.get(cellIndex) * learningRate);
                }
                //bias

                writeArray2dDouble(weightsOutBufferBuffer, output_dimension, k, cell_blocks,     readArray2dDouble(weightsOutBufferBuffer, output_dimension, k, cell_blocks)   + dok * 1.0 * learningRate);
            }

            queue.putWriteBuffer(weightsOutBuffer, false);
            queue.putWriteBuffer(deltaH, false);


            //input to hidden

            inputToHiddenKernel.rewind();
            inputToHiddenKernel.putArgs(deltaH, dSdFBuffer, dSdGBuffer, weightsFBuffer, weightsGBuffer).putArg(learningRate).putArg(full_input_dimension).putArg(cell_blocks);

            queue
                    .putWriteBuffer(deltaH, false)
                    .put1DRangeKernel(inputToHiddenKernel, 0, globalWorkSizeForCells, localWorkSizeForCells);


            //for (int cellIndex = 0; cellIndex < cell_blocks; cellIndex++) {
            //    final double deltaHForCell = deltaH[cellIndex];
            //
            //    for (int i = 0; i < full_input_dimension; i++) {
            //        weightsF[cellIndex][i] += deltaHForCell * readArray2dDouble(dSdFBufferBuffer, cell_blocks, cellIndex, i) * learningRate;
            //        weightsG[cellIndex][i] += deltaHForCell * readArray2dDouble(dSdGBufferBuffer, cell_blocks, cellIndex, i) * learningRate;
            //    }
            //}
        }

        //////////////////////////////////////////////////////////////

        //roll-over context to next time step
        //System.arraycopy(actH, 0, context, 0, cell_blocks);
        queue.putCopyBuffer(actHBuffer, context);


        //give results (if requested)
        if( requireOutput ) {
            queue.putReadBuffer(output, false);

            DoubleBuffer outputBuffer = output.getBuffer();

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

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    private static void zero(DoubleBuffer buffer) {
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
    private static void writeArray2dDouble(DoubleBuffer buffer, int width, int x, int y, double value) {
        buffer.put(x + y*width, value);
    }

    private static double readArray2dDouble(DoubleBuffer buffer, int width, int x, int y) {
        return buffer.get(x + y*width);
    }
}
