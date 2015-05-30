
kernel void activateKernel(__global const double* context, __global const double* sumF, __global const double* sumG, __global double* actF, __global double* actG, __global double* actH, int cell_blocks) {
    // get index into global data array
    int i = get_global_id(0);

    // bound check (equivalent to the limit on a 'for' loop for standard/serial C code
    if (i >= cell_blocks)  {
        return;
    }

    // add the vector elements
    double actfj = actF[i] = activate(sumF[i]);
    double actgj = actG[i] = activate(sumG[i]);
    actH[i] = actfj * context[i] + (1 - actfj) * actgj;
}
