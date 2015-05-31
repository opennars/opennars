kernel void InputsToCellblocksKernel(__global double* sumF, __global double* sumG, __global const double* weightsF, __global const double* weightsG, __global const double* full_input, int full_input_dimension, int cell_blocks) {
    int cellIndex = get_global_id(0);

    if (cellIndex >= cell_blocks)  {
        return;
    }

    double sumFForCell = 0.0;
    double sumGForCell = 0.0;

    for (int i = 0; i < full_input_dimension; i++) {
        double fi = full_input[i];

        sumFForCell += ARRAY2d(weightsF, cell_blocks, cellIndex, i) * fi;
        sumGForCell += ARRAY2d(weightsG, cell_blocks, cellIndex, i) * fi;
    }

    sumG[cellIndex] = sumGForCell;
    sumF[cellIndex] = sumFForCell;
}