void inputsToCellblocksFiber(int fiberId, __global precisionType* sumF, __global precisionType* sumG, __global precisionType* weightsF, __global precisionType* weightsG, __global precisionType* full_input, int full_input_dimension, int cell_blocks) {
    int cellIndex = fiberId;

    if (cellIndex >= cell_blocks)  {
        return;
    }

    precisionType sumFForCell = 0.0;
    precisionType sumGForCell = 0.0;

    for (int i = 0; i < full_input_dimension; i++) {
        precisionType fi = full_input[i];

        sumFForCell += (ARRAY2d(weightsF, cell_blocks, cellIndex, i) * fi);
        sumGForCell += (ARRAY2d(weightsG, cell_blocks, cellIndex, i) * fi);
    }

    sumF[cellIndex] = sumFForCell;
    sumG[cellIndex] = sumGForCell;
}

void activateFiber(
    int fiberId,

    // read
    __global precisionType* context,
    __global precisionType* sumF,
    __global precisionType* sumG,

    // write
    __global precisionType* actF,
    __global precisionType* actG,
    __global precisionType* actH,

    // other
    int cell_blocks
) {
    int cellIndex = fiberId;

    if (cellIndex >= cell_blocks)  {
        return;
    }

    // add the vector elements
    precisionType actfj = activate(sumF[cellIndex]);
    precisionType actgj = activate(sumG[cellIndex]);

    actF[cellIndex] = actfj;
    actG[cellIndex] = actgj;

    actH[cellIndex] = actfj * context[cellIndex] + (1.0 - actfj) * actgj;
}



void PrepareHiddenLayerPlusBiasFiber(
    int fiberId,

    // read
    __global precisionType* actH,

    // write
    __global precisionType* full_hidden,

    // others
    int cell_blocks
) {
    int cellIndex = fiberId;

    // + 1 because see orginal java code
    if (cellIndex >= cell_blocks + 1)  {
        return;
    }

    if( cellIndex == cell_blocks ) {
        full_hidden[cell_blocks] = 1.0;
    }
    else {
        full_hidden[cellIndex] = actH[cellIndex];
    }
}


kernel void CalculateOutputFiber(
    int fiberId,

    // read
    __global precisionType* weightsOut,
    __global precisionType* full_hidden,

    // write
    __global precisionType* output,

    // others
    int output_dimension,
    int cell_blocks
) {
    int k = fiberId;

    if (k >= output_dimension)  {
        return;
    }

    precisionType s = 0;
    for (int j = 0; j < cell_blocks + 1; j++) {
        s += (ARRAY2d(weightsOut, output_dimension, k, j) * full_hidden[j]);
    }

    output[k] = s;
}


// SPEED< full_input_dimension could be defined so it can be unrolled by the driver >

kernel void BackpropScalePartialsFiber(
    int fiberId,

    __global precisionType* actF,
    __global precisionType* sumF,
    __global precisionType* actG,
    __global precisionType* sumG,
    __global precisionType* context,
    __global precisionType* dSdG, // result
    __global precisionType* dSdF, // result

    __global precisionType* full_input,
    int full_input_dimension,
    int cell_blocks
) {
    int cellIndex = fiberId;

    if (cellIndex >= cell_blocks)  {
        return;
    }

    precisionType f = actF[cellIndex];
    precisionType df = derivative(sumF[cellIndex]);
    precisionType g = actG[cellIndex];
    precisionType dg = derivative(sumG[cellIndex]);
    precisionType h_ = context[cellIndex]; //prev value of h

    for( int i = 0; i < full_input_dimension; i++) {
        precisionType prevdSdF = ARRAY2d(dSdF, cell_blocks, cellIndex, i);
        precisionType prevdSdG = ARRAY2d(dSdG, cell_blocks, cellIndex, i);
        precisionType in = full_input[i];

        ARRAY2d(dSdG, cell_blocks, cellIndex, i) = ((1.0 - f)*dg*in) + (f*prevdSdG);
        ARRAY2d(dSdF, cell_blocks, cellIndex, i) = ((h_ - g)*df*in) + (f*prevdSdF);
    }
}


/////////////////////////////////////////////////////////////////


void outputToHiddenFiber(
    int fiberId,

    int currentBatchElement,

    __global precisionType* batchTargetOutputs, // input

    __global precisionType* actH,
    __global precisionType* deltaH,
    __global precisionType* output,
    __global precisionType* weightsOut,


    precisionType SCALE_OUTPUT_DELTA,
    precisionType learningRate,

    int output_dimension,
    int cell_blocks
) {
    int j = fiberId;

    if (j >= cell_blocks)  {
        return;
    }


    precisionType deltaHSum = 0.0f;

    for (int k = 0; k < output_dimension; k++) {
        precisionType dok = (batchTargetOutputs[output_dimension*currentBatchElement + k] - output[k]) * SCALE_OUTPUT_DELTA;

        deltaHSum += (dok * ARRAY2d(weightsOut, output_dimension, k, j));
        ARRAY2d(weightsOut, output_dimension, k, j) += (dok * actH[j] * learningRate);

        //bias
        ARRAY2d(weightsOut, output_dimension, k, cell_blocks) += (dok * 1.0f * learningRate);
    }

    deltaH[j] = deltaHSum;
}


kernel void InputToHiddenFiber(
    int fiberId,

    // read
    __global precisionType* deltaH,
    __global precisionType* dSdF,
    __global precisionType* dSdG,

    // write
    __global precisionType* weightsF,
    __global precisionType* weightsG,
    // others
    precisionType learningrate,
    int full_input_dimension,
    int cell_blocks
) {
    int cellIndex = fiberId;

    if (cellIndex >= cell_blocks)  {
        return;
    }

    precisionType deltaHForCell = deltaH[cellIndex];

    for (int i = 0; i < full_input_dimension; i++) {
        ARRAY2d(weightsF, cell_blocks, cellIndex, i) += (deltaHForCell * ARRAY2d(dSdF, cell_blocks, cellIndex, i) * learningrate);
        ARRAY2d(weightsG, cell_blocks, cellIndex, i) += (deltaHForCell * ARRAY2d(dSdG, cell_blocks, cellIndex, i) * learningrate);
    }
}













kernel void InputsToCellblocksKernel(__global precisionType* sumF, __global precisionType* sumG, __global precisionType* weightsF, __global precisionType* weightsG, __global precisionType* full_input, int full_input_dimension, int cell_blocks) {
    int cellIndex = get_global_id(0);

    inputsToCellblocksFiber(cellIndex, sumF, sumG, weightsF, weightsG, full_input, full_input_dimension, cell_blocks);
}



kernel void stage1Kernel(
    __global precisionType* context,
    __global precisionType* sumF,
    __global precisionType* sumG,
    __global precisionType* actF,
    __global precisionType* actG,
    __global precisionType* actH,
    __global precisionType* weightsF,
    __global precisionType* weightsG,
    __global precisionType* weightsOut,
    __global precisionType* full_input,
    __global precisionType* full_hidden,
    __global precisionType* output,
    __global precisionType* dSdG,
    __global precisionType* dSdF,


    __global int* flagIsTargetOutputAvailable,
    __global precisionType* batchInputs,
    __global precisionType* batchTargetOutputs,

    int countOfInputOutputTuples,




    int full_input_dimension,
    int cell_blocks,
    int output_dimension,
    int inputDimension,

    __global int *counterBarrier0,
    __global int *counterBarrier1,
    __global int *counterBarrier2,
    __global int *counterBarrier3,
    __global int *counterBarrier4,
    __global int *counterBarrier5,
    __global int *counterBarrier6,
    __global int *counterBarrier7,
    __global int *counterBarrier8,
    __global int *barrierResetBarrier,





    precisionType SCALE_OUTPUT_DELTA,

    __global precisionType* deltaH,

    precisionType learningRate,

    int syncronisationCounterResetValue
) {
    int fiberId = get_global_id(0);

    for( int inputI = 0; inputI < countOfInputOutputTuples; inputI++ ) {


        // assemle full inputs buffer from inputs and context
        if( fiberId == 0 ) {
            int location = 0;
            for( int batchInputsI = 0; batchInputsI < inputDimension; batchInputsI++ ) {
                full_input[location] = batchInputs[inputDimension*inputI + batchInputsI];
                location++;
            }

            for( int contextI = 0; contextI < cell_blocks; contextI++ ) {
                full_input[location] = context[contextI];
                location++;
            }

            full_input[location] = 1.0f; // bias
        }


        atomic_sub(counterBarrier7, 1);

        // wait until the counter hits 0
        for(;;) {
            int syncronisationValue = atomic_sub(counterBarrier7, 0);

            if( syncronisationValue <= 0 ) {
                break;
            }
        }



        // reset barrier-reset barrier

        barrierResetBarrier[0] = syncronisationCounterResetValue;



        inputsToCellblocksFiber(fiberId, sumF, sumG, weightsF, weightsG, full_input, full_input_dimension, cell_blocks);


        atomic_sub(counterBarrier0, 1);

        // wait until the counter hits 0
        for(;;) {
            int syncronisationValue = atomic_sub(counterBarrier0, 0);

            if( syncronisationValue <= 0 ) {
                break;
            }
        }

        activateFiber(fiberId, context, sumF, sumG, actF, actG, actH, cell_blocks);


        atomic_sub(counterBarrier1, 1);

        // wait until the counter hits 0
        for(;;) {
            int syncronisationValue = atomic_sub(counterBarrier1, 0);

            if( syncronisationValue <= 0 ) {
                break;
            }
        }



        PrepareHiddenLayerPlusBiasFiber(fiberId, actH, full_hidden, cell_blocks);



        atomic_sub(counterBarrier2, 1);

        // wait until the counter hits 0
        for(;;) {
            int syncronisationValue = atomic_sub(counterBarrier2, 0);

            if( syncronisationValue <= 0 ) {
                break;
            }
        }



        CalculateOutputFiber(fiberId, weightsOut, full_hidden, output, output_dimension, cell_blocks);




        atomic_sub(counterBarrier3, 1);

        // wait until the counter hits 0
        for(;;) {
            int syncronisationValue = atomic_sub(counterBarrier3, 0);

            if( syncronisationValue <= 0 ) {
                break;
            }
        }



        BackpropScalePartialsFiber(fiberId,actF,sumF,actG,sumG,context,dSdG, dSdF, full_input, full_input_dimension, cell_blocks);



        atomic_sub(counterBarrier4, 1);

        // wait until the counter hits 0
        for(;;) {
            int syncronisationValue = atomic_sub(counterBarrier4, 0);

            if( syncronisationValue <= 0 ) {
                break;
            }
        }


        if( flagIsTargetOutputAvailable[inputI] != 0 ) {
            outputToHiddenFiber(fiberId, inputI, batchTargetOutputs, actH, deltaH, output, weightsOut, SCALE_OUTPUT_DELTA, learningRate, output_dimension, cell_blocks);




            atomic_sub(counterBarrier5, 1);

            // wait until the counter hits 0
            for(;;) {
                int syncronisationValue = atomic_sub(counterBarrier5, 0);

                if( syncronisationValue <= 0 ) {
                    break;
                }
            }



            InputToHiddenFiber( fiberId, deltaH,dSdF, dSdG, weightsF, weightsG, learningRate, full_input_dimension, cell_blocks);
        }

        atomic_sub(counterBarrier6, 1);

        // wait until the counter hits 0
        for(;;) {
            int syncronisationValue = atomic_sub(counterBarrier6, 0);

            if( syncronisationValue <= 0 ) {
                break;
            }
        }

        // rollover

        for( int cellI = 0; cellI < cell_blocks; cellI++ ) {
            context[cellI] = actH[cellI];
        }

        atomic_sub(counterBarrier8, 1);

        // wait until the counter hits 0
        for(;;) {
            int syncronisationValue = atomic_sub(counterBarrier8, 0);

            if( syncronisationValue <= 0 ) {
                break;
            }
        }




        // reset most counterBarriers
        counterBarrier0[0] = syncronisationCounterResetValue;
        counterBarrier1[0] = syncronisationCounterResetValue;
        counterBarrier2[0] = syncronisationCounterResetValue;
        counterBarrier3[0] = syncronisationCounterResetValue;
        counterBarrier4[0] = syncronisationCounterResetValue;
        counterBarrier5[0] = syncronisationCounterResetValue;
        counterBarrier6[0] = syncronisationCounterResetValue;
        counterBarrier7[0] = syncronisationCounterResetValue;
        counterBarrier8[0] = syncronisationCounterResetValue;


        // barrier for the reset of all other barriers

        atomic_sub(barrierResetBarrier, 1);

        // wait until the counter hits 0
        for(;;) {
            int syncronisationValue = atomic_sub(barrierResetBarrier, 0);

            if( syncronisationValue <= 0 ) {
                break;
            }
        }
    }
}
