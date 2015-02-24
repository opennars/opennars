
#ifndef NENGO_GPU_JNI_H
#define NENGO_GPU_JNI_H

#ifdef __cplusplus
extern "C"{
#endif

#include <stdio.h>
#include <jni.h>

int* sort(int* values, int length, int order);

void storeTerminationData(JNIEnv* env, jobjectArray transforms_JAVA, jobjectArray tau_JAVA, jobjectArray isDecodedTermination_JAVA, NengoGPUData* currentData, int* networkArrayData);

void storeNeuronData(JNIEnv* env, jobjectArray neuronData_JAVA, int* isSpikingEnsemble, NengoGPUData* currentData );

void storeEncoders(JNIEnv* env, jobjectArray encoders_JAVA, NengoGPUData* currentData);

void storeDecoders(JNIEnv* env, jobjectArray decoders_JAVA, NengoGPUData* currentData, int* networkArrayData);

void assignNetworkArrayToDevice(int networkArrayIndex, int* networkArrayData, int* ensembleData, int* collectSpikes, NengoGPUData* currentData);

void setupIO(int numProjections, projection* projections, NengoGPUData* currentData, int* networkArrayData, int** originRequiredByJava);

void setupSpikes(int* collectSpikes, NengoGPUData* currentData);

#ifdef __cplusplus
}
#endif

#endif
