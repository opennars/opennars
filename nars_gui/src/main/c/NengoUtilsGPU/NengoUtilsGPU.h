
#ifdef __cplusplus
extern "C"
{
#endif

#ifndef NENGO_GPU_FUNCTIONS_H
#define NENGO_GPU_FUNCTIONS_H

#include<cuda_runtime.h>

// pseudo inverse functions
float* pseudoInverse(float* A, int M, int N, float minSV, int numSV, int inputOnDevice, int outputOnDevice);
__global__ void invertS(float*, int, int, float, int);
__global__ void undoLowerTriangularStorage(float* Sd, unsigned int numElements, int stride);

// find gamma functions
float* findGamma(float* A_transpose, int numNeurons, int numEvalPoints, int inputOnDevice, int outputOnDevice);

#endif

#ifdef __cplusplus
}
#endif
