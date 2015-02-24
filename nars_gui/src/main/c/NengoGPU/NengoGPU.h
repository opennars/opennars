#ifndef NENGO_GPU_H
#define NENGO_GPU_H

#ifdef __cplusplus
extern "C"{
#endif

#include <stdio.h>
#include <pthread.h>
#include "NengoGPUData.h"

extern int totalNumEnsembles;
extern int* deviceForEnsemble;
extern int totalNumNetworkArrays;
extern int* deviceForNetworkArray;
extern NengoGPUData** nengoDataArray;
extern int numDevices;
extern float startTime;
extern float endTime;
extern volatile int myCVsignal;
extern pthread_mutex_t* mutex;
extern pthread_cond_t* cv_GPUThreads;
extern pthread_cond_t* cv_JNI;
extern FILE* fp;

extern float* sharedInput;
extern int sharedInputSize;

int manipulateNumNodesProcessed(int action, int value);
int manipulateKill(int action);

void* start_GPU_thread(void* arg);
void run_start();
void run_kill();

#ifdef __cplusplus
}
#endif

#endif
