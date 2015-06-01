
kernel void activateKernel(__global const precisionType* context, __global const precisionType* sumF, __global const precisionType* sumG, __global precisionType* actF, __global precisionType* actG, __global precisionType* actH, int cell_blocks) {
    // get index into global data array
    int i = get_global_id(0);

    // bound check (equivalent to the limit on a 'for' loop for standard/serial C code
    if (i >= cell_blocks)  {
        return;
    }

    // add the vector elements
    precisionType actfj = actF[i] = activate(sumF[i]);
    precisionType actgj = actG[i] = activate(sumG[i]);
    actH[i] = actfj * context[i] + (1 - actfj) * actgj;
}



// SPEED< full_input_dimension could be defined so it can be unrolled by the driver >


kernel void BackpropScalePartialsKernel(
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
    int cellIndex = get_global_id(0);

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

kernel void InputsToCellblocksKernel(__global precisionType* sumF, __global precisionType* sumG, __global precisionType* weightsF, __global precisionType* weightsG, __global precisionType* full_input, int full_input_dimension, int cell_blocks) {
    int cellIndex = get_global_id(0);

    if (cellIndex >= cell_blocks)  {
        return;
    }

    precisionType sumFForCell = 0.0;
    precisionType sumGForCell = 0.0;

    for (int i = 0; i < full_input_dimension; i++) {
        precisionType fi = full_input[i];

        sumFForCell = ((ARRAY2d(weightsF, cell_blocks, cellIndex, i)) * fi);
        sumGForCell = ((ARRAY2d(weightsG, cell_blocks, cellIndex, i)) * fi);
    }

    sumF[cellIndex] = sumFForCell;
    sumG[cellIndex] = sumGForCell;
}

kernel void InputToHiddenKernel(
    // read
    __global precisionType* deltaH,
    __global precisionType* dSdF,
    __global precisionType* dSdG,

    // write
    __global precisionType* weightsF,
    __global precisionType* weightsG,
    // others
    precisionType learningrate,
    int full_input_dimension, int cell_blocks
) {
    int cellIndex = get_global_id(0);

    if (cellIndex >= cell_blocks)  {
        return;
    }

    precisionType deltaHForCell = deltaH[cellIndex];

    for (int i = 0; i < full_input_dimension; i++) {
        ARRAY2d(weightsF, cell_blocks, cellIndex, i) += (deltaHForCell * ARRAY2d(dSdF, cell_blocks, cellIndex, i) * learningrate);
        ARRAY2d(weightsG, cell_blocks, cellIndex, i) += (deltaHForCell * ARRAY2d(dSdG, cell_blocks, cellIndex, i) * learningrate);
    }
}

kernel void PrepareHiddenLayerPlusBias(
    // read
    __global precisionType* actH,

    // write
    __global precisionType* full_hidden,

    // others
    int cell_blocks
) {
    int cellIndex = get_global_id(0);

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

kernel void CalculateOutputKernel(
    // read
    __global precisionType* weightsOut,
    __global precisionType* full_hidden,

    // write
    __global precisionType* output,

    // others
    int output_dimension,
    int cell_blocks
) {
    int k = get_global_id(0);

    if (k >= output_dimension)  {
        return;
    }

    precisionType s = 0;
    for (int j = 0; j < cell_blocks + 1; j++) {
        s += (ARRAY2d(weightsOut, output_dimension, k, j) * full_hidden[j]);
    }

    output[k] = s;
}
