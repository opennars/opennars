package nars.rl.lstm.opencl;

import com.jogamp.opencl.*;
import nars.rl.lstm.IAgentSupervised;
import nars.rl.lstm.Neuron;
import nars.rl.lstm.NeuronType;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Random;

import static com.jogamp.opencl.CLMemory.Mem.READ_WRITE;
import static java.lang.Math.min;
import static java.lang.System.nanoTime;


public class LSTMCL implements IAgentSupervised {






    // for validation of the implementation
    public class ValidationSimpleLSTM implements IAgentSupervised
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














    private ValidationSimpleLSTM validation;

    private final CLContext cl;
    private final CLCommandQueue queue;
    private final CLProgram program;


    private double init_weight_range = 0.1;
    public double learningRate;//0.07


    private int full_input_dimension;
    private int output_dimension;
    private int cell_blocks;

    private CLBuffer<FloatBuffer> weightsF = null;
    private CLBuffer<FloatBuffer> weightsG = null;
    private CLBuffer<FloatBuffer> weightsOut = null;

    //partials
    private CLBuffer<FloatBuffer> dSdG = null;
    private CLBuffer<FloatBuffer> dSdF = null;

    private double SCALE_OUTPUT_DELTA = 1.0;


    CLBuffer<FloatBuffer> sumF = null, sumG = null, actF, actG, actH, context;

    private CLBuffer<FloatBuffer> fullInputBuffer = null;


    private CLBuffer<FloatBuffer>  full_hidden = null;
    private CLBuffer<FloatBuffer> output;
    private double[] deltaOutput;
    CLBuffer<FloatBuffer> deltaH = null;
    private float[] full_input;
    private int maxWorkGroupSize;

    private CLKernel activateKernel;
    private CLKernel inputsToCellblocksKernel;
    private CLKernel backpropScalePartialsKernel;
    private CLKernel inputToHiddenKernel;
    private CLKernel prepareHiddenLayerPlusBias;
    private CLKernel calculateOutputKernel;


    public LSTMCL(Random r, int input_dimension, int output_dimension, int cell_blocks, final double initLearningRate) {
        validation = new ValidationSimpleLSTM(r, input_dimension, output_dimension, cell_blocks, initLearningRate);


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
        queue = device.createCommandQueue(0);

//            //final double actfj = actF[j] = F.Activate(sumFF[j]);
//            //final double actgj = actG[j] = G.Activate(sumGG[j]);
//            actH[j] = actfj * context[j] + (1 - actfj) * actgj;

        String[] sources = new String[3];

        sources[0] = readStringFromInputStream(LSTMCL.class.getResourceAsStream("PragmaDefinitions.cl"));
        sources[1] = readStringFromInputStream(LSTMCL.class.getResourceAsStream("SigmoidNeuron.cl"));
        sources[2] = readStringFromInputStream(LSTMCL.class.getResourceAsStream("Kernels.cl"));

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

        context = cl.createFloatBuffer(cell_blocks, READ_WRITE);

        full_input_dimension = input_dimension + cell_blocks + 1; //+1 for bias

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
        //setup input vector


        if ((full_input == null) || (full_input.length != full_input_dimension)) {
            full_input = new float[full_input_dimension];

            fullInputBuffer = cl.createFloatBuffer(full_input_dimension, READ_WRITE);
        }

        int loc = 0;
        for (int i = 0; i < input.length; ) {
            full_input[loc++] = (float)input[i++];
        }

        queue.putReadBuffer(context, true);

        FloatBuffer contextBuffer = context.getBuffer();
        contextBuffer.rewind();
        for (int c = 0; c < cell_blocks; ) {
            full_input[loc++] = contextBuffer.get(c++);
        }
        full_input[loc++] = (float)1.0; //bias

        // copy full input
        FloatBuffer fullInputBufferBuffer = fullInputBuffer.getBuffer();
        fullInputBufferBuffer.rewind();

        for( int i = 0; i < full_input_dimension; i++ ) {
            fullInputBufferBuffer.put(i, full_input[i]);
        }

        queue.putWriteBuffer(fullInputBuffer, true);


        //cell block arrays
        if ((sumF == null)) {

            //actF = cl.createFloatBuffer(cell_blocks, READ_WRITE);
            //actG = cl.createFloatBuffer(cell_blocks, READ_WRITE);
            //actH = cl.createFloatBuffer(cell_blocks, READ_WRITE);
            //actF = new double[cell_blocks];
            //actG = new double[cell_blocks];
            //actH = new double[cell_blocks];

            //full_hidden = cl.createFloatBuffer(cell_blocks + 1, READ_WRITE);
            //output = cl.createFloatBuffer(output_dimension, READ_WRITE);
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

        int localWorkSizeForCells = min(maxWorkGroupSize, 32);  // Local work size dimensions
        int globalWorkSizeForCells = roundUp(localWorkSizeForCells, cell_blocks);   // rounded up to the nearest multiple of the localWorkSize

        int localWorkSizeForOutput = min(maxWorkGroupSize, 32);
        int globalWorkSizeForOutput = roundUp(localWorkSizeForOutput, output_dimension);





        // asynchronous write of data to GPU device,
        // followed by blocking read to get the computed results back.
        long time = nanoTime();




        queue.finish();

        ///queue.putWriteBuffer(weightsF, true); // for validation
        ///queue.putWriteBuffer(weightsG, true); // for validation
        //queue.putReadBuffer(fullInputBuffer, true); // for validation

        double[][] weightsFBeforeOpenCLkernel = getAndConvertContentOfBuffer2d(weightsF, cell_blocks); // for validation
        double[][] weightsGBeforeOpenCLkernel = getAndConvertContentOfBuffer2d(weightsG, cell_blocks); // for validation
        //double[] fullInputBeforeOpenCLkernel = getAndConvertContentOfBuffer1d(fullInputBuffer); // for validation


        queue.finish();

        inputsToCellblocksKernel.rewind();
        inputsToCellblocksKernel.putArgs(sumF, sumG, weightsF, weightsG, fullInputBuffer).putArg(full_input_dimension).putArg(cell_blocks);

        queue.put1DRangeKernel(inputsToCellblocksKernel, 0, globalWorkSizeForCells, localWorkSizeForCells);

        //queue.finish();

        queue.putReadBuffer(sumF, true); // for validation
        queue.putReadBuffer(sumG, true); // for validation

        queue.finish();

        double[] sumFAfterOpenCLkernel = getAndConvertContentOfBuffer1d(sumF);
        double[] sumGAfterOpenCLkernel = getAndConvertContentOfBuffer1d(sumG);

        validation.weightsF = weightsFBeforeOpenCLkernel;
        validation.weightsG = weightsGBeforeOpenCLkernel;
        validation.full_input = convertFloatToDoubleArray1d(full_input);

        validation.inputsToCellblocksKernel2();

        validateArray1d(validation.sumF, sumFAfterOpenCLkernel); // validation
        validateArray1d(validation.sumG, sumGAfterOpenCLkernel); // validation



        activateKernel.rewind();
        activateKernel.putArgs(context, sumF, sumG, actF, actG, actH).putArg(cell_blocks);

        queue.put1DRangeKernel(activateKernel, 0, globalWorkSizeForCells, localWorkSizeForCells);




        //prepare hidden layer plus bias
        prepareHiddenLayerPlusBias.rewind();
        prepareHiddenLayerPlusBias.putArgs(actH, full_hidden).putArg(cell_blocks);

        queue.put1DRangeKernel(prepareHiddenLayerPlusBias, 0, globalWorkSizeForCells, localWorkSizeForCells);


        //Arrays.fill(full_hidden, 0);
        //
        //FloatBuffer actHBufferBuffer = actHBuffer.getBuffer();
        //for (int i = 0; i < cell_blocks; i++)
        //    full_hidden[i] = actHBufferBuffer.get(i);
        ////System.arraycopy(actH, 0, full_hidden, 0, cell_blocks);
        //
        //full_hidden[cell_blocks] = 1.0; //bias

        //calculate output


        calculateOutputKernel.rewind();
        calculateOutputKernel.putArgs(weightsOut, full_hidden, output).putArg(output_dimension).putArg(cell_blocks);

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
        backpropScalePartialsKernel.putArgs(actF, sumF, actG, sumG, context, dSdG, dSdF, fullInputBuffer).putArg(full_input_dimension).putArg(cell_blocks);

        queue
                .put1DRangeKernel(backpropScalePartialsKernel, 0, globalWorkSizeForCells, localWorkSizeForCells);
                //.putReadBuffer(dSdFBuffer, true)
                //.putReadBuffer(dSdGBuffer, true);

        /*
        FloatBuffer actFBufferBuffer = actFBuffer.getBuffer();
        FloatBuffer actGBufferBuffer = actGBuffer.getBuffer();
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

        //FloatBuffer dSdFBufferBuffer = dSdFBuffer.getBuffer();
        //FloatBuffer dSdGBufferBuffer = dSdGBuffer.getBuffer();

        if( target_output != null ) {

            //output to hidden

            if ((deltaOutput == null) || (deltaOutput.length!=output_dimension)) {
                deltaOutput = new double[output_dimension];
                deltaH = cl.createFloatBuffer(cell_blocks, READ_WRITE);
            }
            else {
                Arrays.fill(deltaOutput, 0);
                zero(deltaH.getBuffer());
            }

            // we need to sync the buffer
            queue.putReadBuffer(deltaH, true);
            queue.putReadBuffer(weightsOut, true);
            queue.putReadBuffer(actH, true);
            queue.putReadBuffer(output, true);

            FloatBuffer deltaHBuffer = deltaH.getBuffer();
            FloatBuffer weightsOutBufferBuffer = weightsOut.getBuffer();
            FloatBuffer actHBufferBuffer = actH.getBuffer();
            FloatBuffer outputBuffer = output.getBuffer();

            for (int k = 0; k < output_dimension; k++) {
                final float dok  = ((float)target_output[k] - outputBuffer.get(k)) * (float)SCALE_OUTPUT_DELTA;
                deltaOutput[k] = dok;

                for (int cellIndex = 0; cellIndex < cell_blocks; cellIndex++) {
                    deltaHBuffer.put(cellIndex, deltaHBuffer.get(cellIndex) + dok * readArray2dFloat(weightsOutBufferBuffer, output_dimension, k, cellIndex));
                    writeArray2dFloat(weightsOutBufferBuffer, output_dimension, k, cellIndex, readArray2dFloat(weightsOutBufferBuffer, output_dimension, k, cellIndex) + dok * actHBufferBuffer.get(cellIndex) * (float) learningRate);
                }
                //bias

                writeArray2dFloat(weightsOutBufferBuffer, output_dimension, k, cell_blocks, readArray2dFloat(weightsOutBufferBuffer, output_dimension, k, cell_blocks) + dok * 1.0f * (float) learningRate);
            }

            queue.putWriteBuffer(weightsOut, true);
            queue.putWriteBuffer(deltaH, true);


            //input to hidden

            inputToHiddenKernel.rewind();
            inputToHiddenKernel.putArgs(deltaH, dSdF, dSdG, weightsF, weightsG).putArg((float)learningRate).putArg(full_input_dimension).putArg(cell_blocks);

            queue
                    .putWriteBuffer(deltaH, true)
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
}
