package nars.rl.lstm.opencl;

import com.jogamp.opencl.*;
import nars.rl.lstm.IAgentSupervised;
import nars.rl.lstm.Neuron;
import nars.rl.lstm.NeuronType;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.DoubleBuffer;
import java.util.Arrays;
import java.util.Random;

import static com.jogamp.opencl.CLMemory.Mem.*;
import static java.lang.Math.min;
import static java.lang.System.nanoTime;


public class LSTMCL implements IAgentSupervised
{

    private final CLContext cl;
    private final CLCommandQueue queue;
    private final CLProgram program;


    private double init_weight_range = 0.1;
    public double learningRate;//0.07


    private int full_input_dimension;
    private int output_dimension;
    private int cell_blocks;
    private Neuron F;
    private Neuron G;



    private double [][] weightsF;
    private double [][] weightsG;
    private double [][] weightsOut;

    private CLBuffer<DoubleBuffer> weightsFBuffer = null;
    private CLBuffer<DoubleBuffer> weightsGBuffer = null;
    private CLBuffer<DoubleBuffer> weightsOutBuffer = null;

    //partials (Need this for each output? Need to remind myself..)
    private double [][] dSdF;
    private double [][] dSdG;

    private CLBuffer<DoubleBuffer> dSdGBuffer = null;
    private CLBuffer<DoubleBuffer> dSdFBuffer = null;

    private NeuronType neuron_type_F = NeuronType.Sigmoid;
    private NeuronType neuron_type_G = NeuronType.Sigmoid;

    private double SCALE_OUTPUT_DELTA = 1.0;


    CLBuffer<DoubleBuffer> sumFBuffer = null, sumGBuffer = null, actFBuffer, actGBuffer, actHBuffer, contextBuffer;
    //private double[] sumF;
    //private double[] sumG;

    private CLBuffer<DoubleBuffer> fullInputBuffer = null;


    private double[] full_hidden;
    private double[] output;
    private double[] deltaOutput;
    private double[] deltaH;
    private double[] full_input;
    private int maxWorkGroupSize;

    private CLKernel activateKernel;
    private CLKernel inputsToCellblocksKernel;


    public LSTMCL(Random r, int input_dimension, int output_dimension, int cell_blocks, final double initLearningRate)
    {

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

        String[] sources = new String[4];

        sources[0] = readStringFromInputStream(LSTMCL.class.getResourceAsStream("PragmaDefinitions.cl"));
        sources[1] = readStringFromInputStream(LSTMCL.class.getResourceAsStream("SigmoidNeuron.cl"));
        sources[2] = readStringFromInputStream(LSTMCL.class.getResourceAsStream("KernelActivate.cl"));
        sources[3] = readStringFromInputStream(LSTMCL.class.getResourceAsStream("InputsToCellblocksKernel.cl"));


        String concatenedSource = concatenateStringsWithNewLine(sources);

        program = cl.createProgram(concatenedSource).build();

        activateKernel = program.createCLKernel("activateKernel");
        inputsToCellblocksKernel = program.createCLKernel("InputsToCellblocksKernel");

        this.learningRate = initLearningRate;
        this.output_dimension = output_dimension;
        this.cell_blocks = cell_blocks;

        contextBuffer = cl.createDoubleBuffer(cell_blocks, READ_WRITE);

        full_input_dimension = input_dimension + cell_blocks + 1; //+1 for bias

        F = Neuron.Factory(neuron_type_F);
        G = Neuron.Factory(neuron_type_G);

        weightsF = new double[cell_blocks][full_input_dimension];
        weightsG = new double[cell_blocks][full_input_dimension];

        weightsFBuffer = cl.createDoubleBuffer(cell_blocks*full_input_dimension, READ_WRITE);
        weightsGBuffer = cl.createDoubleBuffer(cell_blocks*full_input_dimension, READ_WRITE);


        dSdF = new double[cell_blocks][full_input_dimension];
        dSdG = new double[cell_blocks][full_input_dimension];

        dSdFBuffer = cl.createDoubleBuffer(cell_blocks*full_input_dimension, READ_WRITE);
        dSdGBuffer = cl.createDoubleBuffer(cell_blocks*full_input_dimension, READ_WRITE);

        for (int i = 0; i < full_input_dimension; i++) {
            for (int j = 0; j < cell_blocks; j++) {
                weightsF[j][i] = (r.nextDouble() * 2d - 1d) * init_weight_range;
                weightsG[j][i] = (r.nextDouble() * 2d - 1d) * init_weight_range;
            }
        }

        weightsOut = new double[output_dimension][cell_blocks + 1];

        weightsOutBuffer = cl.createDoubleBuffer(output_dimension*(cell_blocks + 1), READ_WRITE);

        for (int j = 0; j < cell_blocks + 1; j++) {
            for (int k = 0; k < output_dimension; k++)
                weightsOut[k][j] = (r.nextDouble() * 2d - 1d) * init_weight_range;
        }
    }

    public void delete() {
        if (!cl.isReleased())
            cl.release();
    }

    public void clear()
    {

        zero(contextBuffer.getBuffer());

        //reset accumulated partials
        for (int c = 0; c < cell_blocks; c++) {
            Arrays.fill(this.dSdG[c], 0.0);
            Arrays.fill(this.dSdF[c], 0.0);
        }

    }

    public double[] predict(double[] input)
    {
        return learn(input, null);
    }

    public static void Display()
    {
        System.out.println("==============================");
        System.out.println("DAGate: todo...");
        System.out.println("\n==============================");
    }

    public double[] learn(double[] input, double[] target_output) {

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

        DoubleBuffer cb = contextBuffer.getBuffer();
        for (int c = 0; c < cell_blocks; ) {
            full_input[loc++] = cb.get(c++);
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

            full_hidden = new double[cell_blocks + 1];
            output = new double[output_dimension];
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

        int localWorkSize = min(maxWorkGroupSize, 256);  // Local work size dimensions
        int globalWorkSize = roundUp(localWorkSize, cell_blocks);   // rounded up to the nearest multiple of the localWorkSize

        // copy weights to gpu

        DoubleBuffer weightsFBufferBuffer = weightsFBuffer.getBuffer();
        DoubleBuffer weightsGBufferBuffer = weightsGBuffer.getBuffer();

        // double[cell_blocks][full_input_dimension];

        for( int y = 0; y < full_input_dimension; y++ ) {
            for( int x = 0; x < cell_blocks; x++ ) {
                writeArray2dDouble(weightsFBufferBuffer, cell_blocks, x, y, weightsF[x][y]);
            }
        }

        for( int y = 0; y < full_input_dimension; y++ ) {
            for( int x = 0; x < cell_blocks; x++ ) {
                writeArray2dDouble(weightsGBufferBuffer, cell_blocks, x, y, weightsG[x][y]);
            }
        }



        final double[] full_hidden = this.full_hidden;

        inputsToCellblocksKernel.rewind();
        inputsToCellblocksKernel.putArgs(sumFBuffer, sumGBuffer, weightsFBuffer, weightsGBuffer, fullInputBuffer).putArg(full_input_dimension).putArg(cell_blocks);

        DoubleBuffer sumFBufferBuffer = sumFBuffer.getBuffer();
        DoubleBuffer sumGBufferBuffer = sumGBuffer.getBuffer();

        ////inputs to cell blocks
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
        activateKernel.putArgs(contextBuffer, sumFBuffer, sumGBuffer, actFBuffer, actGBuffer, actHBuffer).putArg(cell_blocks);


        // asynchronous write of data to GPU device,
        // followed by blocking read to get the computed results back.
        long time = nanoTime();
        queue
                .putWriteBuffer(weightsFBuffer, false)
                .putWriteBuffer(weightsGBuffer, false)
                .putWriteBuffer(fullInputBuffer, false)
                .put1DRangeKernel(inputsToCellblocksKernel, 0, globalWorkSize, localWorkSize)

                .putWriteBuffer(contextBuffer, false)
                .put1DRangeKernel(activateKernel, 0, globalWorkSize, localWorkSize)
                .putReadBuffer(actFBuffer, true)
                .putReadBuffer(actGBuffer, true)
                .putReadBuffer(actHBuffer, true);
        time = nanoTime() - time;

        //System.out.println(time);

        //prepare hidden layer plus bias
        Arrays.fill(full_hidden, 0);

        DoubleBuffer ah = actHBuffer.getBuffer();
        for (int i = 0; i < cell_blocks; i++)
            full_hidden[i] = ah.get(i);
        //System.arraycopy(actH, 0, full_hidden, 0, cell_blocks);

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

        DoubleBuffer af = actFBuffer.getBuffer();
        DoubleBuffer ag = actGBuffer.getBuffer();
        for (int j = 0; j < cell_blocks; j++) {

            double f = af.get(j);
            double df = F.Derivative(sumFBufferBuffer.get(j));
            double g = ag.get(j);
            double dg = G.Derivative(sumGBufferBuffer.get(j));
            double h_ = cb.get(j); //prev value of h

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
                    wk[j] += dok * ah.get(j) * learningRate;
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
        //System.arraycopy(actH, 0, context, 0, cell_blocks);
        //TODO DoubleBuffer copy
        for (int i = 0; i < cell_blocks; i++)
            cb.put(i, ah.get(i));

        //give results
        return output;
    }

    public void setLearningRate(double learningRate) {
        learningRate = learningRate;
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
}
