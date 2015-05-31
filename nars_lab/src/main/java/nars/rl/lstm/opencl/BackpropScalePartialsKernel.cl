// SPEED< full_input_dimension could be defined so it can be unrolled by the driver >


// result
//  dSdG
//  dSdF

kernel void BackpropScalePartialsKernel(global double* actF, __global double* sumF, __global double* actG, __global double* sumG, __global double* context, __global double* dSdG, __global double* dSdF, __global double* full_input, int full_input_dimension, int cell_blocks) {
    int cellIndex = get_global_id(0);

    if (cellIndex >= cell_blocks)  {
        return;
    }

    double f = actF[cellIndex];
    double df = derivative(sumF[cellIndex]);
    double g = actG[cellIndex];
    double dg = derivative(sumG[cellIndex]);
    double h_ = context[cellIndex]; //prev value of h

    for( int i = 0; i < full_input_dimension; i++) {
        double prevdSdF = ARRAY2d(dSdF, cell_blocks, cellIndex, i);
        double prevdSdG = ARRAY2d(dSdG, cell_blocks, cellIndex, i);
        double in = full_input[i];

        ARRAY2d(dSdG, cell_blocks, cellIndex, i) = ((1.0 - f)*dg*in) + (f*prevdSdG);
        ARRAY2d(dSdF, cell_blocks, cellIndex, i) = ((h_ - g)*df*in) + (f*prevdSdF);
    }
}
