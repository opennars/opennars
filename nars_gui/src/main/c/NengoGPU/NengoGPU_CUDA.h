
#ifndef NENGO_GPU_CUDA_H
#define NENGO_GPU_CUDA_H

#ifdef __cplusplus
extern "C"{
#endif

#include <cuda_runtime.h>

#include "NengoGPUData.h"

void printIntArrayFromDevice(FILE* fp, intArray* array, int n, int m, int labels);
void printFloatArrayFromDevice(FILE* fp, floatArray* array, int n, int m, int labels);

void printIntColumn(FILE* fp, int* array, int m, int n, int col);
void printFloatColumn(FILE* fp, float* array, int m, int n, int col);
void printIntRange(FILE* fp, int* array, int start, int end);
void printFloatRange(FILE* fp, float* array, int start, int end);

int getGPUDeviceCount();

void initGPUDevice(int device);

void shutdownGPUDevice();

void checkCudaErrorWithDevice(cudaError_t err, int device, char* message);
void checkCudaError(cudaError_t err, char* message);

__global__ void transform(float dt, int numTransformRows, float* input, int* inputOffset, int* transformRowToInputIndexor, float* transforms, float* tau, float* terminationOutput, int* terminationOutputIndexor, int* inputDimensions);

__global__ void sumTerminations(int totalDimensions, int maxDecodedNumTerminations, float* terminationOutput, float* ensembleSums);

__global__ void encode(int totalNumNeurons, float* encoders, float* sums, float* encodeResult, int* encoderRowToEnsembleIndexor, int* ensembleOffsetInDimension, int* ensembleDimension, int* encoderStride, int* neuronIndexor);

__global__ void integrateAfterEncode(int numNuerons, float dt, float adjusted_dt, int steps, int* neuronToEnsembleIndexor, float* encodingResult, float* neuronVoltage, float* neuronReftime, float* tau_RC, float* tauRef, float* bias, float* scale, float* spikes, float* NDterminationSums, int* isSpikingEnsemble);

__global__ void decode(int totalOutputSize, float* decoders, float* spikes, float* output, int* decoderRowToEnsembleIndexor, int* ensembleNumNeurons, int* ensembleOffsetInNeurons, int* decoderStride, int* outputIndexor);

__global__ void processNDterminations(int numEnsembles, int numNDterminations, int steps, float adjusted_dt, int* NDterminationEnsembleOffset, int* terminationOffsetInInputs, int* terminationDimensions, int* inputIndex, float* input, float* weights, float* current, float* sum, float* tau);

__global__ void moveGPUData(int size, int* map, float* to, float* from);

void run_NEFEnsembles(NengoGPUData*, float, float);

float* allocateCudaFloatArray(int size);
int* allocateCudaIntArray(int size);
long getDeviceCapacity(int device);
void initializeDeviceInputAndOutput(NengoGPUData* currentData);

#ifdef __cplusplus
}
#endif

#endif 
