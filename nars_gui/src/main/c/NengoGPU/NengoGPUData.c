#ifdef __cplusplus
extern "C"{
#endif

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <jni.h>
#include <cuda_runtime.h>

#include "NengoGPUData.h"
#include "NengoGPU_CUDA.h"
#include "NengoGPU.h"

extern FILE* fp;


///////////////////////////////////////////////////////
// intArray and floatArray allocating and freeing
///////////////////////////////////////////////////////
intArray* newIntArray(int size, const char* name)
{
    intArray* new = (intArray*)malloc(sizeof(intArray));
    if(!new)
    {
        printf("Failed to allocate memory for intArray. name: %s, attemped size: %d\n", name, size);
        exit(EXIT_FAILURE);
    }

    new->array = (int*)malloc(size * sizeof(int));
    if(!new->array)
    {
        printf("Failed to allocate memory for intArray. name: %s, attemped size: %d\n", name, size);
        exit(EXIT_FAILURE);
    }
    
    new->size = size;
    new->name = strdup(name);
    new->onDevice = 0;

    return new;
}

void freeIntArray(intArray* a)
{
    if(!a)
        return;

    if(a->array){
        if(a->onDevice) {
            cudaFree(a->array);
        }
        else {
            free(a->array);
        }
    }
        //a->onDevice ? cudaFree(a->array) : free(a->array);
    
    if(a->name)
        free(a->name);

    free(a);
}

intArray* newIntArrayOnDevice(int size, const char* name)
{
    intArray* new = (intArray*)malloc(sizeof(intArray));

    new->array = allocateCudaIntArray(size);
    new->size = size;
    new->name = strdup(name);
    new->onDevice = 1;

    return new;
}

floatArray* newFloatArray(int size, const char* name)
{
    floatArray* new = (floatArray*)malloc(sizeof(floatArray));
    if(!new)
    {
        printf("Failed to allocate memory for floatArray. name: %s, attemped size: %d\n", name, size);
        exit(EXIT_FAILURE);
    }

    new->array = (float*)malloc(size * sizeof(float));
    if(!new->array)
    {
        printf("Failed to allocate memory for floatArray. name: %s, attemped size: %d\n", name, size);
        exit(EXIT_FAILURE);
    }

    new->size = size;
    new->name = strdup(name);
    new->onDevice = 0;

    return new;
}

void freeFloatArray(floatArray* a)
{
    //printf("freeing %s, size: %d, onDevice: %d, address: %d\n", a->name, a->size, a->onDevice, (int)a->array);

    if(!a)
        return;

    if(a->array){
        if(a->onDevice) {
            cudaFree(a->array);
        }
        else {
            free(a->array);
        }
    }
//        a->onDevice ? cudaFree(a->array) : free(a->array);
    
    if(a->name)
        free(a->name);

    free(a);
}

floatArray* newFloatArrayOnDevice(int size, const char* name)
{
    floatArray* new = (floatArray*)malloc(sizeof(floatArray));

    new->array = allocateCudaFloatArray(size);
    new->size = size;
    new->name = strdup(name);
    new->onDevice = 1;

    return new;
}
    

///////////////////////////////////////////////////////
// intArray and floatArray safe getters and setters
///////////////////////////////////////////////////////

void checkBounds(char* verb, char* name, int size, int index)
{
    if(index >= size || index < 0)
    {
        printf("%s safe array out of bounds, name: %s, size: %d, index:%d\n", verb, name, size, index);
        exit(EXIT_FAILURE);
    }
}

void checkLocation(char* verb, char* name, int onDevice, int size, int index)
{
    if(onDevice)
    {
        printf("%s safe array that is not on the host, name: %s, size: %d, index:%d\n", verb, name, size, index);
        exit(EXIT_FAILURE);
    }
}

void intArraySetElement(intArray* a, int index, int value)
{
    checkBounds("Setting", a->name, a->size, index);
    checkLocation("Setting", a->name, a->onDevice, a->size, index);

    a->array[index] = value;
}

void floatArraySetElement(floatArray* a, int index, float value)
{
    checkBounds("Setting", a->name, a->size, index);
    checkLocation("Setting", a->name, a->onDevice, a->size, index);

    a->array[index] = value;
}

int intArrayGetElement(intArray* a, int index)
{
    checkBounds("Getting", a->name, a->size, index);
    checkLocation("Getting", a->name, a->onDevice, a->size, index);

    return a->array[index];
}

float floatArrayGetElement(floatArray* a, int index)
{
    checkBounds("Getting", a->name, a->size, index);
    checkLocation("Getting", a->name, a->onDevice, a->size, index);

    return a->array[index];
}

void intArraySetData(intArray* a, int* data, int dataSize)
{
    if(dataSize > a->size)
    {
        printf("Warning: calling intArraySetData with a data set that is too large; truncating data. name: %s, size: %d, dataSize: %d", a->name, a->size, dataSize);
    }
    
    memcpy(a->array, data, dataSize * sizeof(int));
}

void floatArraySetData(floatArray* a, float* data, int dataSize)
{
    if(dataSize > a->size)
    {
        printf("Warning: calling floatArraySetData with a data set that is too large; truncating data. name: %s, size: %d, dataSize: %d", a->name, a->size, dataSize);
    }
    
    memcpy(a->array, data, dataSize * sizeof(float));
}

///////////////////////////////////////////////////////
// projection storing and printing
///////////////////////////////////////////////////////
void storeProjection(projection* proj, int* data)
{
    proj->sourceNode = data[0];
    proj->sourceOrigin = data[1];
    proj->destinationNode = data[2];
    proj->destinationTermination = data[3];
    proj->size = data[4];
    proj->sourceDevice = 0;
    proj->destDevice = 0;
}

void printProjection(projection* proj)
{
    printf("%d %d %d %d %d %d %d\n", proj->sourceNode, proj->sourceOrigin, proj->destinationNode, proj->destinationTermination, proj->size, proj->sourceDevice, proj->destDevice);
}

///////////////////////////////////////////////////////
// int_list functions 
///////////////////////////////////////////////////////
int_list* cons_int_list(int_list* list, int item)
{
    int_list* new = (int_list*)malloc(sizeof(int_list));
    new->first = item;
    new->next = list;
    return new;
}

void free_int_list(int_list* list)
{
    if(list)
    {
        int_list* temp = list->next;
        free(list);
        free_int_list(temp);
    }
}

///////////////////////////////////////////////////////
// int_queue functions
///////////////////////////////////////////////////////
int_queue* new_int_queue()
{
    int_queue* new = (int_queue*) malloc(sizeof(int_queue));

    new->size = 0;
    new->head = NULL;
    new->tail = NULL;

    return new;
}

int pop_int_queue(int_queue* queue)
{
    int_list* temp;
    if(queue )
    {
        int val;
        switch(queue->size)
        {
            case 0:
                fprintf(stderr, "Error \"int_queue\": accessing empty queue\n");
                exit(EXIT_FAILURE);
                break;
            case 1:
                val = queue->head->first;
                free_int_list(queue->head);
                queue->head = NULL;
                queue->tail = NULL;
                queue->size--;
                return val;
                break;
            default:
                val = queue->head->first;
                temp = queue->head;
                queue->head = temp->next;
                temp->next = NULL;
                free_int_list(temp);
                queue->size--;
                return val;
        }
    }
    else
    {
        fprintf(stderr, "Error \"int_queue\": accessing null queue\n");
        exit(EXIT_FAILURE);
    }
}

void add_int_queue(int_queue* queue, int val)
{
    if(queue)
    {
        queue->tail->next = cons_int_list(NULL, val);
        queue->tail = queue->tail->next;
        queue->size++;
    }
    else
    {
        fprintf(stderr, "Error \"int_queue\": accessing null queue\n");
        exit(EXIT_FAILURE);
    }
}

void free_int_queue(int_queue* queue)
{
    if(queue)
    {
        free_int_list(queue->head);
        free(queue);
    }
}

///////////////////////////////////////////////////////
// NengoGPUData functions
///////////////////////////////////////////////////////

// return a fresh NengoGPUData object with all numerical values zeroed out and all pointers set to null
NengoGPUData* getNewNengoGPUData()
{
    NengoGPUData* new = (NengoGPUData*)malloc(sizeof(NengoGPUData));

    new-> onDevice = 0;
    new-> device = 0;
    new-> maxTimeStep = 0;
     
    new-> numNetworkArrays = 0;

    new-> numNeurons = 0;
    new-> numInputs = 0;
    new-> numEnsembles = 0;
    new-> numTerminations = 0;
    new-> numDecodedTerminations = 0;
    new-> numNDterminations = 0;
    new-> numNetworkArrayOrigins = 0;
    new-> numOrigins = 0;

    new->totalInputSize = 0;
    new->totalTransformSize = 0;
    new->totalNumTransformRows = 0;
    new->totalEnsembleDimension = 0;
    new->totalEncoderSize = 0;
    new->totalDecoderSize = 0;
    new->totalOutputSize = 0;

    new->maxDecodedTerminationDimension = 0;
    new->maxNumDecodedTerminations = 0;
    new->maxDimension = 0;
    new->maxNumNeurons = 0;
    new->maxOriginDimension = 0;
    new->maxEnsembleNDTransformSize = 0;
    
    new->numNDterminations = 0;

    new->numSpikesToSendBack = 0;

    new->GPUOutputSize = 0;
    new->JavaOutputSize = 0;
    new->CPUOutputSize = 0;

    new->input = NULL;
    new->terminationOffsetInInput = NULL;
    new->networkArrayIndexInJavaArray = NULL;

    new->terminationTransforms = NULL;
    new->transformRowToInputIndexor = NULL;
    new->terminationTau = NULL;
    new->inputDimension = NULL;
    new->terminationOutput = NULL;
    new->terminationOutputIndexor = NULL;

    new->ensembleSums = NULL;
    new->encoders = NULL;
    new->decoders = NULL;
    new->encodeResult = NULL;

    new->neuronVoltage = NULL;
    new->neuronReftime = NULL;
    new->neuronBias = NULL;
    new->neuronScale = NULL;
    new->ensembleTauRC = NULL;
    new->ensembleTauRef = NULL;
    new->neuronToEnsembleIndexor = NULL;

    new->isSpikingEnsemble = NULL;

    new->ensembleNumTerminations = NULL;
    new->ensembleDimension = NULL;
    new->ensembleNumNeurons = NULL;
    new->ensembleNumOrigins = NULL;

    new->ensembleOffsetInDimensions = NULL;
    new->ensembleOffsetInNeurons = NULL;
    new->encoderRowToEnsembleIndexor = NULL;
    new->encoderRowToNeuronIndexor = NULL;
    new->decoderRowToEnsembleIndexor = NULL;

    new->encoderStride = NULL;
    new->decoderStride = NULL;

    new->ensembleOrderInEncoders = NULL;
    new->ensembleOrderInDecoders = NULL;

    new->spikes = NULL;

    new->ensembleOutput = NULL;
    new->output = NULL;
    new->outputHost = NULL;
    new->decoderRowToOutputIndexor = NULL;
    
    new->ensembleOriginDimension = NULL;

    new->ensembleOriginOffsetInOutput = NULL;
    new->networkArrayOriginOffsetInOutput = NULL; 
    new->networkArrayOriginDimension = NULL; 
    new->networkArrayNumOrigins = NULL;

    new->spikeMap = NULL;
    new->GPUTerminationToOriginMap = NULL;
    new->ensembleOutputToNetworkArrayOutputMap = NULL;
    new->ensembleIndexInJavaArray = NULL;
    
    new->NDterminationInputIndexor = NULL;
    new->NDterminationCurrents = NULL;
    new->NDterminationWeights = NULL;
    new->NDterminationEnsembleOffset = NULL;
    new->NDterminationEnsembleSums = NULL;

    new->sharedData_outputIndex = NULL;
    new->sharedData_sharedIndex = NULL;


    return new;
}


// Should only be called once the NengoGPUData's numerical values have been set. This function allocates memory of the approprate size for each pointer.
// Memory is allocated on the host. The idea is to call this before we load the data in from the JNI structures, so we have somewhere to put that data. Later, we will move most of the data to the device.
void initializeNengoGPUData(NengoGPUData* new)
{
    char* name; 

    if(new == NULL)
    {
         return;
    }

    name = "networkArrayIndexInJavaArray";
    new->networkArrayIndexInJavaArray = newIntArray(new->numNetworkArrays, name);

    name = "terminationOffsetInInput";
    new->terminationOffsetInInput = newIntArray(new->numInputs, name);

    name = "terminationTranforms";
    new->terminationTransforms = newFloatArray(new->totalTransformSize, name);
    memset(new->terminationTransforms->array, '\0', new->terminationTransforms->size * sizeof(float));

    name = "transformRowToInputIndexor";
    new->transformRowToInputIndexor = newIntArray(new->totalNumTransformRows, name);
    name = "terminationTau";
    new->terminationTau = newFloatArray(new->numInputs, name);
    name = "inputDimension";
    new->inputDimension = newIntArray(new->numInputs, name);
    name = "terminationOutputIndexor";
    new->terminationOutputIndexor = newIntArray(new->totalNumTransformRows, name);
 
    name = "encoders";
    new->encoders = newFloatArray(new->totalEncoderSize, name);
    name = "decoders";
    new->decoders = newFloatArray(new->totalDecoderSize, name);

    name = "neuronBias";
    new->neuronBias = newFloatArray(new->numNeurons, name);
    name = "neuronScale";
    new->neuronScale = newFloatArray(new->numNeurons, name);
    name = "ensembleTauRC";
    new->ensembleTauRC = newFloatArray(new->numEnsembles, name);
    name = "ensembleTauRef";
    new->ensembleTauRef = newFloatArray(new->numEnsembles, name);

    name = "isSpikingEnsemble";
    new->isSpikingEnsemble = newIntArray(new->numEnsembles, name);

    name = "ensembleNumTerminations";
    new->ensembleNumTerminations = newIntArray(new->numEnsembles, name);
    name = "ensembleDimension";
    new->ensembleDimension = newIntArray(new->numEnsembles, name);
    name = "ensembleNumNeurons";
    new->ensembleNumNeurons = newIntArray(new->numEnsembles, name);
    name = "ensembleNumOrigins";
    new->ensembleNumOrigins = newIntArray(new->numEnsembles, name);


    name = "ensembleOffsetInDimensions";
    new->ensembleOffsetInDimensions = newIntArray(new->numEnsembles, name);
    name = "ensembleOffsetInNeurons";
    new->ensembleOffsetInNeurons = newIntArray(new->numEnsembles, name);

    name = "encoderRowToEnsembleIndexor";
    new->encoderRowToEnsembleIndexor = newIntArray(new->numNeurons, name);
    name = "encoderRowToNeuronIndexor";
    new->encoderRowToNeuronIndexor = newIntArray(new->numNeurons, name);
    name = "decoderRowToEnsembleIndexor";
    new->decoderRowToEnsembleIndexor = newIntArray(new->totalOutputSize, name);
    name = "decoderRowToOutputIndexor";
    new->decoderRowToOutputIndexor = newIntArray(new->totalOutputSize, name);
    name = "neuronToEnsembleIndexor";
    new->neuronToEnsembleIndexor = newIntArray(new->numNeurons, name);

    name = "encoderStride";
    new->encoderStride = newIntArray(new->maxDimension, name);
    name = "decoderStride";
    new->decoderStride = newIntArray(new->maxNumNeurons, name);

    name = "ensembleOrderInEncoders";
    new->ensembleOrderInEncoders = newIntArray(new->numEnsembles, name);

    name = "ensembleOrderInDecoders";
    new->ensembleOrderInDecoders = newIntArray(new->numEnsembles, name); 

    name = "ensembleOriginDimension";
    new->ensembleOriginDimension = newIntArray(new->numOrigins, name);

    name = "ensembleOriginOffsetInOutput";
    new->ensembleOriginOffsetInOutput = newIntArray(new->numOrigins, name);

    name = "networkArrayOriginOffsetInOutput";
    new->networkArrayOriginOffsetInOutput = newIntArray(new->numNetworkArrayOrigins, name);

    name = "networkArrayOriginDimension";
    new->networkArrayOriginDimension = newIntArray(new->numNetworkArrayOrigins, name);

    name = "networkArrayNumOrigins";
    new->networkArrayNumOrigins = newIntArray(new->numNetworkArrays, name);

    name = "ensembleIndexInJavaArray";
    new->ensembleIndexInJavaArray = newIntArray(new->numEnsembles, name);

    name = "ensembleOutputToNetworkArrayOutputMap";
    new->ensembleOutputToNetworkArrayOutputMap = newIntArray(new->totalOutputSize, name);

    name = "NDterminationInputIndexor";
    new->NDterminationInputIndexor = newIntArray(new->numNDterminations, name);
    name = "NDterminationWeights";
    new->NDterminationWeights = newFloatArray(new->totalNonDecodedTransformSize, name);

    name = "NDterminationEnsembleOffset";
    new->NDterminationEnsembleOffset = newIntArray(new->numEnsembles, name);

}


// Called at the end of initializeNengoGPUData to determine whether any of the mallocs failed.
void checkNengoGPUData(NengoGPUData* currentData)
{
    int status = 0;

    if(status)
    {
        printf("bad NengoGPUData\n");
        exit(EXIT_FAILURE);
    }
}

// Move data that has to be on the device to the device
void moveToDeviceNengoGPUData(NengoGPUData* currentData)
{
    if(!currentData->onDevice)
    {
        // this function is in NengoGPU_CUDA.cu
        initializeDeviceInputAndOutput(currentData);

        moveToDeviceIntArray(currentData->terminationOffsetInInput);
        moveToDeviceFloatArray(currentData->terminationTransforms);
        moveToDeviceFloatArray(currentData->terminationTau);
        moveToDeviceIntArray(currentData->inputDimension);
        moveToDeviceIntArray(currentData->transformRowToInputIndexor);
        moveToDeviceIntArray(currentData->terminationOutputIndexor);

        moveToDeviceFloatArray(currentData->encoders);
        moveToDeviceFloatArray(currentData->decoders);

        moveToDeviceFloatArray(currentData->neuronBias);
        moveToDeviceFloatArray(currentData->neuronScale);
        moveToDeviceFloatArray(currentData->ensembleTauRC);
        moveToDeviceFloatArray(currentData->ensembleTauRef);
        moveToDeviceIntArray(currentData->neuronToEnsembleIndexor);

        moveToDeviceIntArray(currentData->isSpikingEnsemble);
        
        moveToDeviceIntArray(currentData->ensembleDimension);
        moveToDeviceIntArray(currentData->ensembleNumNeurons);

        moveToDeviceIntArray(currentData->ensembleOffsetInDimensions);
        moveToDeviceIntArray(currentData->ensembleOffsetInNeurons);

        moveToDeviceIntArray(currentData->encoderRowToEnsembleIndexor);
        moveToDeviceIntArray(currentData->encoderRowToNeuronIndexor);

        moveToDeviceIntArray(currentData->decoderRowToEnsembleIndexor);
        moveToDeviceIntArray(currentData->decoderRowToOutputIndexor);

        moveToDeviceIntArray(currentData->encoderStride);
        moveToDeviceIntArray(currentData->decoderStride);

        moveToDeviceIntArray(currentData->spikeMap);

        moveToDeviceIntArray(currentData->GPUTerminationToOriginMap);

        moveToDeviceIntArray(currentData->ensembleOutputToNetworkArrayOutputMap);

        moveToDeviceIntArray(currentData->NDterminationInputIndexor);
        moveToDeviceFloatArray(currentData->NDterminationWeights);
        moveToDeviceIntArray(currentData->NDterminationEnsembleOffset);

        currentData->onDevice = 1;
    }
}

// Free the NengoGPUData. Makes certain assumptions about where each array is (device or host).
void freeNengoGPUData(NengoGPUData* currentData)
{
    freeIntArray(currentData->networkArrayIndexInJavaArray);

    freeFloatArray(currentData->input);
    freeIntArray(currentData->terminationOffsetInInput);

    freeFloatArray(currentData->terminationTransforms);
    freeIntArray(currentData->transformRowToInputIndexor);
    freeFloatArray(currentData->terminationTau);
    freeIntArray(currentData->inputDimension);
    freeFloatArray(currentData->terminationOutput);
    freeIntArray(currentData->terminationOutputIndexor);

    freeFloatArray(currentData->ensembleSums);
    freeFloatArray(currentData->encoders);
    freeFloatArray(currentData->decoders);
    freeFloatArray(currentData->encodeResult);

    freeFloatArray(currentData->neuronVoltage);
    freeFloatArray(currentData->neuronReftime);
    freeFloatArray(currentData->neuronBias);
    freeFloatArray(currentData->neuronScale);
    freeFloatArray(currentData->ensembleTauRC);
    freeFloatArray(currentData->ensembleTauRef);
    freeIntArray(currentData->neuronToEnsembleIndexor);

    freeIntArray(currentData->isSpikingEnsemble);

    freeIntArray(currentData->ensembleNumTerminations);
    freeIntArray(currentData->ensembleDimension);
    freeIntArray(currentData->ensembleNumNeurons);
    freeIntArray(currentData->ensembleNumOrigins);

    freeIntArray(currentData->ensembleOffsetInDimensions);
    freeIntArray(currentData->ensembleOffsetInNeurons);
    freeIntArray(currentData->encoderRowToEnsembleIndexor);
    freeIntArray(currentData->encoderRowToNeuronIndexor);
    freeIntArray(currentData->decoderRowToEnsembleIndexor);
    freeIntArray(currentData->decoderRowToOutputIndexor);

    freeIntArray(currentData->encoderStride);
    freeIntArray(currentData->decoderStride);

    freeIntArray(currentData->ensembleOrderInEncoders);
    freeIntArray(currentData->ensembleOrderInDecoders);

    freeFloatArray(currentData->spikes);

    freeFloatArray(currentData->ensembleOutput);
    freeFloatArray(currentData->output);
    freeFloatArray(currentData->outputHost);

    freeIntArray(currentData->ensembleOriginDimension);
    freeIntArray(currentData->ensembleIndexInJavaArray);
    freeIntArray(currentData->ensembleOriginOffsetInOutput);
    freeIntArray(currentData->networkArrayOriginOffsetInOutput);
    freeIntArray(currentData->networkArrayOriginDimension);
    freeIntArray(currentData->networkArrayNumOrigins);
    freeIntArray(currentData->spikeMap);
    freeIntArray(currentData->GPUTerminationToOriginMap);
    freeIntArray(currentData->ensembleOutputToNetworkArrayOutputMap);
    freeIntArray(currentData->sharedData_outputIndex);
    freeIntArray(currentData->sharedData_sharedIndex);

    freeIntArray(currentData->NDterminationInputIndexor);
    freeFloatArray(currentData->NDterminationCurrents);
    freeFloatArray(currentData->NDterminationWeights);
    freeIntArray(currentData->NDterminationEnsembleOffset);
    freeFloatArray(currentData->NDterminationEnsembleSums);
    
    free(currentData);
};

// print the NengoGPUData. Should only be called once the data has been set.
void printNengoGPUData(NengoGPUData* currentData, int printArrays)
{
    
    printf("printing NengoGPUData:\n");
    printf("onDevice; %d\n", currentData->onDevice);
    printf("initialized; %d\n", currentData->initialized);
    printf("device; %d\n", currentData->device);
    printf("maxTimeStep; %f\n", currentData->maxTimeStep);
     
    printf("numNeurons; %d\n", currentData->numNeurons);
    printf("numEnsembles; %d", currentData->numEnsembles);
    printf("numNetworkArrays; %d\n", currentData->numNetworkArrays);
    printf("numInputs; %d\n", currentData->numInputs);
    printf("numTerminations; %d\n", currentData->numTerminations);
    printf("numNDterminations; %d\n", currentData->numNDterminations);
    printf("numDecodedTerminations; %d\n", currentData->numDecodedTerminations);
    printf("numNetworkArrayOrigins; %d\n", currentData->numNetworkArrayOrigins);
    printf("numOrigins; %d\n", currentData->numOrigins);

    printf("totalInputSize; %d\n", currentData->totalInputSize);
    printf("GPUInputSize; %d\n", currentData->GPUInputSize);
    printf("CPUInputSize; %d\n", currentData->CPUInputSize);
    printf("JavaInputSize; %d\n", currentData->JavaInputSize);
    printf("offsetInSharedInput; %d\n", currentData->offsetInSharedInput);
    printf("numSpikesToSendBack; %d\n", currentData->numSpikesToSendBack);

    printf("totalTransformSize; %d\n", currentData->totalTransformSize);
    printf("totalNumTransformRows; %d\n", currentData->totalNumTransformRows);
    printf("totalNonDecodedTransformSize; %d\n", currentData->totalNonDecodedTransformSize);
    printf("totalEnsembleDimension; %d\n", currentData->totalEnsembleDimension);
    printf("totalEncoderSize; %d\n", currentData->totalEncoderSize);
    printf("totalDecoderSize; %d\n", currentData->totalDecoderSize);
    printf("totalOutputSize; %d\n", currentData->totalOutputSize);

    printf("maxDecodedTerminationDimension; %d\n", currentData->maxDecodedTerminationDimension);
    printf("maxNumDecodedTerminations; %d\n", currentData->maxNumDecodedTerminations);
    printf("maxDimension; %d\n", currentData->maxDimension);
    printf("maxNumNeurons; %d\n", currentData->maxNumNeurons);
    printf("maxOriginDimension; %d\n", currentData->maxOriginDimension);
    printf("maxEnsembleNDTransformSize; %d\n", currentData->maxEnsembleNDTransformSize);

    printf("GPUOutputSize; %d\n", currentData->GPUOutputSize);
    printf("JavaOutputSize; %d\n", currentData->JavaOutputSize);
    printf("interGPUOutputSize; %d\n", currentData->interGPUOutputSize);
    printf("CPUOutputSize; %d\n", currentData->CPUOutputSize);
 
    if(printArrays)
    {
        printIntArray(currentData->networkArrayIndexInJavaArray, currentData->numNetworkArrays, 1);
        printIntArray(currentData->ensembleIndexInJavaArray, currentData->numEnsembles, 1);

        printFloatArray(currentData->input, currentData->totalInputSize, 1);
        printIntArray(currentData->terminationOffsetInInput, currentData->numTerminations, 1);

        printFloatArray(currentData->terminationTransforms, currentData->totalNumTransformRows, currentData->maxDecodedTerminationDimension);
        printIntArray(currentData->transformRowToInputIndexor, currentData->totalNumTransformRows, 1);
        printFloatArray(currentData->terminationTau, currentData->numTerminations, 1);
        printIntArray(currentData->inputDimension, currentData->numInputs, 1);
        printFloatArray(currentData->terminationOutput, currentData->totalNumTransformRows, 1);
        printIntArray(currentData->terminationOutputIndexor, currentData->totalNumTransformRows, 1);
        printIntArray(currentData->ensembleNumTerminations, currentData->numEnsembles, 1);

        printFloatArray(currentData->encoders, currentData->totalEncoderSize, 1);
    //    printIntArray(currentData->ensembleOrderInEncoders);
        printFloatArray(currentData->encodeResult, currentData->numNeurons, 1);
        printFloatArray(currentData->ensembleSums, currentData->totalEnsembleDimension, 1);

        printFloatArray(currentData->decoders, currentData->totalDecoderSize, 1);
    //    printIntArray(currentData->ensembleOrderInDecoders, 1);

        // data for calculating spikes
        printFloatArray(currentData->neuronVoltage, currentData->numNeurons, 1);
        printFloatArray(currentData->neuronReftime, currentData->numNeurons, 1);
        printFloatArray(currentData->neuronBias, currentData->numNeurons, 1);
        printFloatArray(currentData->neuronScale, currentData->numNeurons, 1);
        printFloatArray(currentData->ensembleTauRC, currentData->numEnsembles, 1);
        printFloatArray(currentData->ensembleTauRef, currentData->numEnsembles, 1);
        printIntArray(currentData->neuronToEnsembleIndexor, currentData->numNeurons, 1);

        printIntArray(currentData->isSpikingEnsemble, currentData->numNeurons, 1);

        // supplementary arrays for doing encoding
        printIntArray(currentData->ensembleDimension, currentData->numEnsembles, 1);
        printIntArray(currentData->ensembleOffsetInDimensions, currentData->numEnsembles, 1);
        printIntArray(currentData->encoderRowToEnsembleIndexor, currentData->numNeurons, 1); 
        printIntArray(currentData->encoderStride, currentData->maxDimension, 1);
        printIntArray(currentData->encoderRowToNeuronIndexor, currentData->numNeurons, 1);

        // supplementary arrays for doing decoding
        printIntArray(currentData->ensembleNumNeurons, currentData->numEnsembles, 1);
        printIntArray(currentData->ensembleOffsetInNeurons, currentData->numEnsembles, 1); 
        printIntArray(currentData->decoderRowToEnsembleIndexor, currentData->totalOutputSize, 1); 
        printIntArray(currentData->decoderStride, currentData->maxDimension, 1);
        printIntArray(currentData->decoderRowToOutputIndexor, currentData->totalOutputSize, 1);

        printFloatArray(currentData->spikes, currentData->numNeurons, 1);

        printFloatArray(currentData->ensembleOutput, currentData->totalOutputSize, 1);
        printFloatArray(currentData->output, currentData->totalOutputSize + currentData->numSpikesToSendBack, 1);
        printFloatArray(currentData->outputHost, currentData->CPUOutputSize + currentData->numSpikesToSendBack, 1);

        printIntArray(currentData->GPUTerminationToOriginMap , currentData->GPUInputSize, 1);
        printIntArray(currentData->spikeMap, currentData->numSpikesToSendBack, 1);

        // non decoded termination data
        printIntArray(currentData->NDterminationInputIndexor, currentData->numNDterminations, 1);
        printFloatArray(currentData->NDterminationCurrents, currentData->numNDterminations, 1);
        printFloatArray(currentData->NDterminationWeights, currentData->totalNonDecodedTransformSize, 1);
        printIntArray(currentData->NDterminationEnsembleOffset, currentData->numEnsembles, 1);
        printFloatArray(currentData->NDterminationEnsembleSums, currentData->numEnsembles, 1);

        // for organizing the output in terms of ensembles
        printIntArray(currentData->ensembleOriginOffsetInOutput, currentData->numOrigins, 1);
        printIntArray(currentData->ensembleNumOrigins , currentData->numEnsembles, 1);
        printIntArray(currentData->ensembleOriginDimension, currentData->numOrigins, 1);

        printIntArray(currentData->ensembleOutputToNetworkArrayOutputMap, currentData->totalOutputSize, 1);

        printIntArray(currentData->networkArrayOriginOffsetInOutput, currentData->numNetworkArrayOrigins, 1); 
        printIntArray(currentData->networkArrayOriginDimension, currentData->numNetworkArrayOrigins, 1); 
        printIntArray(currentData->networkArrayNumOrigins, currentData->numNetworkArrays, 1);

        printIntArray(currentData->sharedData_outputIndex, currentData->interGPUOutputSize, 1);
        printIntArray(currentData->sharedData_sharedIndex, currentData->interGPUOutputSize, 1);
    }

//    int bytesOnGPU = sizeof(float) * (8 * currentData->numEnsembles + currentData->totalInputSize + currentData->totalTransformSize + 2 * currentData->totalNumTransformRows + 2 * currentData->numTerminations + currentData->maxNumDecodedTerminations * currentData->totalEnsembleDimension + currentData->maxDimension * currentData->numNeurons + currentData->totalEnsembleDimension + currentData->numNeurons * 6 + currentData->maxNumNeurons * currentData->totalOutputSize + 2 * currentData->totalOutputSize);
 // printf("bytes on GPU: %d\n", bytesOnGPU);
 
}

void printDynamicNengoGPUData(NengoGPUData* currentData)
{
    
 /* 
    printf("input:\n");
    printFloatArrayFromDevice(NULL, currentData->input, currentData->totalInputSize, 1, 0);

//    printf("input to output map:\n");
 // printIntArrayFromDevice(NULL, currentData->GPUTerminationToOriginMap, currentData->GPUInputSize, 1, 0);

    printf("terminationOutput:\n");
    printFloatArrayFromDevice(NULL, currentData->terminationOutput, currentData->totalEnsembleDimension * currentData->maxNumDecodedTerminations, 1, 0);

    printf("ensembleSums:\n");
    printFloatArrayFromDevice(NULL, currentData->ensembleSums, currentData->totalEnsembleDimension, 1, 0);

    printf("encodeResult:\n");
    printFloatArrayFromDevice(NULL, currentData->encodeResult, currentData->numNeurons, 1, 0);
*/
    printf("neuronVoltage:\n");
    printFloatArrayFromDevice(NULL, currentData->neuronVoltage, currentData->numNeurons, 1, 1);
    /*
    //printFloatArrayFromDevice(NULL, currentData->neuronVoltage, currentData->maxNumNeurons, currentData->numEnsembles, 0);

    //printf("neuronReftime:\n");
    //printFloatArrayFromDevice(NULL, currentData->neuronReftime, currentData->numNeurons, 1, 0);


    //printf("spikes:\n");
    //printFloatArrayFromDevice(NULL, currentData->spikes, currentData->numNeurons, 1, 0);
    //printFloatArrayFromDevice(NULL, currentData->spikes, currentData->maxNumNeurons, currentData->numEnsembles, 0);


 

    printf("output:\n");
    printFloatArrayFromDevice(NULL, currentData->output, currentData->totalOutputSize, 1, 0);

    printf("outputHost:\n");
    printFloatArray(currentData->outputHost, currentData->totalOutputSize, 1);

    printf("NDterminationInputIndexor:\n");
    printIntArrayFromDevice(NULL, currentData->NDterminationInputIndexor, currentData->numNDterminations, 1, 0);

    printf("NDterminationWeights:\n");
    printFloatArrayFromDevice(NULL, currentData->NDterminationWeights, currentData->numNDterminations, 1, 0);

    printf("NDterminationEnsembleOffset:\n");
    printIntArrayFromDevice(NULL, currentData->NDterminationEnsembleOffset, currentData->numEnsembles, 1, 0);

    printf("NDterminationCurrents:\n");
    printFloatArrayFromDevice(NULL, currentData->NDterminationCurrents, currentData->numNDterminations, 1, 0);
    

    printf("NDterminationEnsembleSums:\n");
    printFloatArrayFromDevice(NULL, currentData->NDterminationEnsembleSums, currentData->numEnsembles, 1, 0);
*/    
}


void printIntArray(intArray* a, int n, int m)
{
    int i, j;

    if(!a)
        return;

    if(!a->array)
        return;

    if(a->onDevice)
    {
        printIntArrayFromDevice(NULL, a, m, n, 0);
        return;
    }

    printf("%s:\n", a->name);

    for(i = 0; i < m; i++)
    {
        for(j = 0; j < n; j++)
        {
            printf("%d ", a->array[i * n + j]);
        }
        printf("\n");
    }

    printf("\n");
}

void printFloatArray(floatArray* a, int n, int m)
{
    int i, j;

    if(!a)
        return;

    if(!a->array)
        return;

    if(a->onDevice)
    {
        printFloatArrayFromDevice(NULL, a, m, n, 0);
        return;
    }

    printf("%s:\n", a->name);

    for(i = 0; i < m; i++)
    {
        for(j = 0; j < n; j++)
        {
            printf("%f ", a->array[i * n + j]);
        }
        printf("\n");
    }

    printf("\n");
}

void moveToDeviceIntArray(intArray* a)
{
    int* result;
    cudaError_t err;
    int size;

    if(a->onDevice)
        return;

    size = a->size;

    err = cudaMalloc((void**)&result, size * sizeof(int));
    if(err)
    {
        printf("%s", cudaGetErrorString(err));
        exit(EXIT_FAILURE);
    }

    err = cudaMemcpy(result, a->array, size * sizeof(int), cudaMemcpyHostToDevice);
    if(err)
    {
        printf("%s", cudaGetErrorString(err));
        exit(EXIT_FAILURE);
    }

    free(a->array);
    a->array = result;
    a->onDevice = 1;
}
    
void moveToDeviceFloatArray(floatArray* a)
{
    float* result;
    cudaError_t err;
    int size;

    if(a->onDevice)
        return;

    size = a->size;

    err = cudaMalloc((void**)&result, size * sizeof(float));
    if(err)
    {
        printf("%s", cudaGetErrorString(err));
        exit(EXIT_FAILURE);
    }

    err = cudaMemcpy(result, a->array, size * sizeof(float), cudaMemcpyHostToDevice);
    if(err)
    {
        printf("%s", cudaGetErrorString(err));
        exit(EXIT_FAILURE);
    }

    free(a->array);
    a->array = result;
    a->onDevice = 1;
}
    
    
void moveToHostFloatArray(floatArray* a)
{
    float* result;
    int size;

    if(!a->onDevice)
        return;

    size = a->size;
    result = (float*)malloc(size * sizeof(float));
    cudaMemcpy(result, a->array, size * sizeof(float), cudaMemcpyDeviceToHost);
    cudaFree(a->array);
    a->array = result;
    a->onDevice = 0;
}

void moveToHostIntArray(intArray* a)
{
    int* result;
    int size;

    if(!a->onDevice)
        return;

    size = a->size;
    result = (int*)malloc(size * sizeof(int));
    cudaMemcpy(result, a->array, size * sizeof(int), cudaMemcpyDeviceToHost);
    cudaFree(a->array);
    a->array = result;
    a->onDevice = 0;
}

#ifdef __cplusplus
}
#endif

