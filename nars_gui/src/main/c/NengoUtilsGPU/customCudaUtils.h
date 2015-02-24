
#ifdef __cplusplus
extern "C"
{
#endif

#ifndef CUSTOM_CUDA_UTILS_H
#define CUSTOM_CUDA_UTILS_H

#include<stdio.h>
#include<cuda_runtime.h>
#include<cula_lapack_device.h>

int getGPUDeviceCount();
float findExtremeFloatArray(float* A, int size, int onDevice, char type, int* index);
void printFloatArray(float* A, int M, int N, int onDevice);
void switchFloatArrayStorage(float* A, float* B, int ldA, int ldB);
void printMatrixToFile(FILE *fp, float* A, int M, int N);
void printDeviceMatrixToFile(FILE *fp, float* devicePointer, int M, int N);

__host__ void checkStatus(culaStatus);
__host__ void checkCudaError(cudaError_t);

#endif

#ifdef __cplusplus
}
#endif

