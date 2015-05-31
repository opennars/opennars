
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
