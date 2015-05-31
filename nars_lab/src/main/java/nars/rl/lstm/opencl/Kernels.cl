
kernel void InputToHiddenKernel(
    // read
    __global double* deltaH,
    __global double* dSdF,
    __global double* dSdG,

    // write
    __global double* weightsF,
    __global double* weightsG,
    // others
    double learningrate,
    int full_input_dimension, int cell_blocks
) {
    int cellIndex = get_global_id(0);

    if (cellIndex >= cell_blocks)  {
        return;
    }

    double deltaHForCell = deltaH[cellIndex];

    for (int i = 0; i < full_input_dimension; i++) {
        ARRAY2d(weightsF, cell_blocks, cellIndex, i) += deltaHForCell * ARRAY2d(dSdF, cell_blocks, cellIndex, i) * learningrate;
        ARRAY2d(weightsG, cell_blocks, cellIndex, i) += deltaHForCell * ARRAY2d(dSdG, cell_blocks, cellIndex, i) * learningrate;
    }
}

kernel void PrepareHiddenLayerPlusBias(
    // read
    __global double* actH,

    // write
    __global double* full_hidden,

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
    __global double* weightsOut,
    __global double* full_hidden,

    // write
    __global double* output,

    // others
    int output_dimension,
    int cell_blocks
) {
    int k = get_global_id(0);

    if (k >= output_dimension)  {
        return;
    }

    double s = 0;
    for (int j = 0; j < cell_blocks + 1; j++) {
        s += ARRAY2d(weightsOut, output_dimension, k, j) * full_hidden[j];
    }

    output[k] = s;
}