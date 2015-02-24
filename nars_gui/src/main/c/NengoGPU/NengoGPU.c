
#ifdef __cplusplus
extern "C"{
#endif

#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <pthread.h>

#include "NengoGPU.h"
#include "NengoGPU_CUDA.h"
#include "NengoGPUData.h"


int totalNumEnsembles = 0;
int totalNumNetworkArrays = 0;

// indicates which device each ensemble has been assigned to
int* deviceForEnsemble;
int* deviceForNetworkArray;

// The data for each device.
NengoGPUData** nengoDataArray;
float startTime = 0, endTime = 0;
volatile int myCVsignal = 0;
int numDevices = 0;

pthread_cond_t* cv_GPUThreads = NULL;
pthread_cond_t* cv_JNI = NULL;
pthread_mutex_t* mutex = NULL;

FILE* fp;

float* sharedInput;
int sharedInputSize;

// Actions:
// -1 - destroy
// 0 - initialize
// 1 - check
// 2 - increment
// 3 - set to value
// 4 - add value
// Keeps track of how many nodes have been processed. Implemented as a function like this for the sake of encapsulation and synchronization.
int manipulateNumDevicesFinished(int action, int value)
{
    static int numDevicesFinished;
    static pthread_mutex_t* myMutex;
    int temp = 0;

    if(action == 0)
    {
        myMutex = (pthread_mutex_t*) malloc(sizeof(pthread_mutex_t));
        
        if(!myMutex)
        {
            printf("bad malloc\n");
            exit(EXIT_FAILURE);
        }

        pthread_mutex_init(myMutex, NULL);
        numDevicesFinished = 0;
        return numDevicesFinished;
    }

    if(action == -1)
    {
        pthread_mutex_destroy(myMutex);
        free(myMutex);
        return numDevicesFinished;
    }

    pthread_mutex_lock(myMutex);

    switch(action)
    {
        case 1:
            temp = numDevicesFinished ;break;
        case 2:
            temp = ++numDevicesFinished; break;
        case 3:
            temp = numDevicesFinished = value; break;
        case 4:
            numDevicesFinished += value;
            temp = numDevicesFinished; break;
    }

    pthread_mutex_unlock(myMutex);

    return temp;
}

// -1 - initialize
// 0 - check
// 1 - kill 
// Keeps track of whether the signal to end the run has been issued.
int manipulateKill(int action)
{
    static int kill;

    switch(action)
    {
        case -1: kill = 0; break;
        case 0: break;
        case 1: kill = 1; break;
    }

    return kill;
}

// Called by the function nativeSetupRun in NengoGPU_JNI.c. By the time this is called, the NengoGPUData structure for each device should have all its static data set
// (but not yet loaded onto a device, since it should't have access to a device yet).
// This function initializes the synchronization primitives and creates a new thread for each GPU in use.
void run_start()
{
    pthread_t* current_thread; 

    NengoGPUData* currentData;

    int i = 0;

    printf("NengoGPU: RUN_START\n");

    manipulateKill(-1);
    manipulateNumDevicesFinished(0, 0);
    myCVsignal = 0;

    current_thread = (pthread_t*) malloc(sizeof(pthread_t));
    if(!current_thread)
    {
        printf("bad malloc\n");
        exit(EXIT_FAILURE);
    }

    // Initialize the mutex and condition variable. Must be done before we create the threads since the threads use them.
    mutex = (pthread_mutex_t*) malloc(sizeof(pthread_mutex_t));
    cv_GPUThreads = (pthread_cond_t*) malloc(sizeof(pthread_cond_t));
    cv_JNI = (pthread_cond_t*) malloc(sizeof(pthread_cond_t));
    if(!mutex || !cv_GPUThreads || !cv_JNI)
    {
        printf("bad malloc\n");
        exit(EXIT_FAILURE);
    }

    pthread_mutex_init(mutex, NULL);
    pthread_cond_init(cv_GPUThreads, NULL);
    pthread_cond_init(cv_JNI, NULL);

    // Start the node-processing threads. Their starting function is start_GPU_thread.
    for(;i < numDevices; i++)
    {
        currentData = nengoDataArray[i];

        pthread_create(current_thread, NULL, &start_GPU_thread, (void*)currentData);
    }

    // Wait for the threads to do their initializing (signalled by myCVSignal == numDevices), then return.
    pthread_mutex_lock(mutex);
    while(myCVsignal < numDevices)
    {
        pthread_cond_wait(cv_JNI, mutex);
    }
    myCVsignal = 0;
    pthread_cond_broadcast(cv_GPUThreads);
    pthread_mutex_unlock(mutex);
    
    free(current_thread);

    sched_yield();
}

// Called once per GPU device per simulation run. This is the entry point for each processing thread. Its input is the
// NengoGPUData structure that it is to process. The behaviour of this function is: wait until we get the signal to step
// (from nativeStep in NengoGPU_JNI.c), process the NengoGPUData structure for one step with run_NEFEnsembles, wait again.
// Eventually manipulateKill(0) will return true, meaning the run is finished and the function will break out of the loop 
// and free its resources.
void* start_GPU_thread(void* arg)
{
    NengoGPUData* nengoData = (NengoGPUData*) arg;

    int numDevicesFinished;

    printf("GPU Thread %d: about to acquire device\n", nengoData->device);
    initGPUDevice(nengoData->device);
    printf("GPU Thread %d: done acquiring device\n", nengoData->device);
    
    printf("GPU Thread %d: about to move simulation data to device\n", nengoData->device);
    moveToDeviceNengoGPUData(nengoData);
    printf("GPU Thread %d: done moving simulation data to device\n", nengoData->device);

    // signal to parent thread that initialization is complete, then wait for the other threads to finish initialization.
    pthread_mutex_lock(mutex);
    myCVsignal++;
    if(myCVsignal == numDevices)
    {
        pthread_cond_broadcast(cv_JNI);
    }
    pthread_cond_wait(cv_GPUThreads, mutex);
    pthread_mutex_unlock(mutex);
    
    // Wait for the signal to step. If that signal has already come, then myCVsignal == 1. In that case, we don't wait (if we did, we'd wait forever).
    pthread_mutex_lock(mutex);
    if(myCVsignal == 0)
    {
        pthread_cond_wait(cv_GPUThreads, mutex);
    }
    pthread_mutex_unlock(mutex);

    // The main loop for the processing threads. The thread is either processing nodes on the GPU or it is waiting for the call to step.
    while(!manipulateKill(0))
    {
        run_NEFEnsembles(nengoData, startTime, endTime);

        // signal that this device is finished processing for the step
        numDevicesFinished = manipulateNumDevicesFinished(2, 0);

        pthread_mutex_lock(mutex);
        // Wakeup the main thread if all devices are finished running
        if(numDevicesFinished == numDevices)
        {
            pthread_cond_broadcast(cv_JNI);
            manipulateNumDevicesFinished(3, 0);
        }
        // Wait for call from main thread to step
        pthread_cond_wait(cv_GPUThreads, mutex);
        pthread_mutex_unlock(mutex);
    }

    // Should only get here after run_kill has been called
    freeNengoGPUData(nengoData);
    shutdownGPUDevice();

    // if this is the last thread to finish, we wake up the main thread, it has to free some things before we finish
    pthread_mutex_lock(mutex);
    myCVsignal++;
    if(myCVsignal == numDevices)
    {
        pthread_cond_broadcast(cv_GPUThreads);
    }
    pthread_mutex_unlock(mutex);
    return NULL;
}

// Free everything - should only be called when the run is over
void run_kill()
{
    // now when the threads check kill, the answer will be yes
    manipulateKill(1);
    manipulateNumDevicesFinished(-1, 0);

    // Wakeup GPU threads so they can free their resources
    pthread_mutex_lock(mutex);
    myCVsignal = 0;
    pthread_cond_broadcast(cv_GPUThreads);
    pthread_cond_wait(cv_GPUThreads, mutex);
    pthread_mutex_unlock(mutex);

    // Once the GPU threads are done, free shared resources and return
    free(nengoDataArray);
    free(deviceForEnsemble);
    free(deviceForNetworkArray);
    free(sharedInput);

    pthread_mutex_destroy(mutex);
    pthread_cond_destroy(cv_GPUThreads);
    pthread_cond_destroy(cv_JNI);

    free(mutex);
    free(cv_GPUThreads);
    free(cv_JNI);
}

#ifdef __cplusplus
}

#endif
