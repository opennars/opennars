
#ifdef __cplusplus
extern "C"{
#endif

#include <stdlib.h>
#include <jni.h>
#include <pthread.h>
#include <string.h>
#include <limits.h>
#include <math.h>
#include <sys/timeb.h>
#include <assert.h>

#include "NEFGPUInterface_JNI.h"
#include "NengoGPU.h"
#include "NengoGPU_CUDA.h"
#include "NengoGPUData.h"

// returns not the sorted array but the indices of the values in the sorted order. So newOrder[0] is the index of 
// the largest element in values, newOrder[1] is the index of the second largest, etc.
// Order allows the user to choose between ascending and descending. 1 
int* sort(int* values, int length, int order)
{
    int i, j;
    int* newOrder = (int*) malloc( length * sizeof(int));
    int* scratch = (int*) malloc( length * sizeof(int));
    
    memset(scratch, '\0', length * sizeof(int));

    for(i = 0; i < length; i++)
    {
        j = i;
        while(j > 0 && ((order && values[i] > scratch[j - 1]) || (!order && values[i] < scratch[j-1])))
        {
            scratch[j] = scratch[j-1];
            newOrder[j] = newOrder[j-1];
            j--;
        }

        scratch[j] = values[i];
        newOrder[j] = i;
    }

    free(scratch);

    return newOrder;
}

void adjustProjections(int numProjections, projection* projections, int* networkArrayJavaIndexToDeviceIndex)
{
    int i;
    projection* p;
    for(i = 0; i < numProjections; i++)
    {
        p = (projections + i);

        p->sourceNode = networkArrayJavaIndexToDeviceIndex[p->sourceNode];
        p->destinationNode = networkArrayJavaIndexToDeviceIndex[p->destinationNode];
    }
}

// since terminations do not typically take up as much room as encoders and decoders, we are not at pains to minimize the
// space they take up
void storeTerminationData(JNIEnv* env, jobjectArray transforms_JAVA, jobjectArray tau_JAVA, jobjectArray isDecodedTermination_JAVA, NengoGPUData* currentData, int* networkArrayData)
{
    int h, i, j, k, l;

    jintArray tempIntArray_JAVA;
    jobjectArray transformsForCurrentEnsemble_JAVA;
    jobjectArray currentTransform_JAVA;
    jfloatArray currentTransformRow_JAVA;
    jfloatArray tauForCurrentEnsemble_JAVA;
    float* currentTransformRow = (float*)malloc(currentData->maxDecodedTerminationDimension * sizeof(float));

    int dimensionOfCurrentEnsemble = 0, dimensionOfCurrentTermination = 0, numTerminationsForCurrentEnsemble = 0;

    int* isDecodedTermination    = (int*)malloc(currentData->numTerminations * sizeof(int));
    
    int ensembleIndexInJavaArray = 0, NDterminationIndex = 0, transformRowIndex = 0, dimensionIndex = 0;
    int NDterminationIndexInEnsemble = 0;
    int startEnsembleIndex, endEnsembleIndex, networkArrayIndexInJavaArray, networkArrayOffsetInTerminations = 0;
    int networkArrayTerminationIndex = 0;
    int terminationOffset = 0;

    for(h = 0; h < currentData->numNetworkArrays; h++)
    {
        networkArrayIndexInJavaArray = intArrayGetElement(currentData->networkArrayIndexInJavaArray, h); 

        startEnsembleIndex = networkArrayData[networkArrayIndexInJavaArray * NENGO_NA_DATA_NUM + NENGO_NA_DATA_FIRST_INDEX];
        endEnsembleIndex = networkArrayData[networkArrayIndexInJavaArray * NENGO_NA_DATA_NUM + NENGO_NA_DATA_END_INDEX];

        for(i = startEnsembleIndex; i < endEnsembleIndex; i++)
        {
            ensembleIndexInJavaArray = intArrayGetElement(currentData->ensembleIndexInJavaArray, i);

            intArraySetElement(currentData->NDterminationEnsembleOffset, i, NDterminationIndex);

            transformsForCurrentEnsemble_JAVA = (jobjectArray) (*env)->GetObjectArrayElement(env, transforms_JAVA, ensembleIndexInJavaArray);
            numTerminationsForCurrentEnsemble = (int) (*env)->GetArrayLength(env, transformsForCurrentEnsemble_JAVA);

            tauForCurrentEnsemble_JAVA = (jfloatArray) (*env)->GetObjectArrayElement(env, tau_JAVA, ensembleIndexInJavaArray);

            if(i == startEnsembleIndex)
            {
                if(numTerminationsForCurrentEnsemble + networkArrayTerminationIndex <= currentData->numInputs)
                {
                    (*env)->GetFloatArrayRegion(env, tauForCurrentEnsemble_JAVA, 0, numTerminationsForCurrentEnsemble, currentData->terminationTau->array + networkArrayTerminationIndex);
                }
                else
                {
                    printf("error: accessing array out of bounds: terminationTau\n");
                    exit(EXIT_FAILURE);
                }
            }

            // get the array that says whether a termination is decoded for the current ensemble
            tempIntArray_JAVA = (jintArray)(*env)->GetObjectArrayElement(env, isDecodedTermination_JAVA, ensembleIndexInJavaArray);
            (*env)->GetIntArrayRegion(env, tempIntArray_JAVA, 0, numTerminationsForCurrentEnsemble, isDecodedTermination);
            (*env)->DeleteLocalRef(env, tempIntArray_JAVA);

            terminationOffset = 0;

            NDterminationIndexInEnsemble = i;

            // loop through the terminations for the current ensemble and store the relevant data
            dimensionOfCurrentEnsemble = 0;
            for(j = 0; j < numTerminationsForCurrentEnsemble; j++)
            {
                if(isDecodedTermination[j])
                {
                    // for decoded terminations
                    currentTransform_JAVA = (jobjectArray) (*env)->GetObjectArrayElement(env, transformsForCurrentEnsemble_JAVA, j);
                    dimensionOfCurrentEnsemble = (int) (*env)->GetArrayLength(env, currentTransform_JAVA);

                    for(k = 0; k < dimensionOfCurrentEnsemble; k++)
                    {
                        currentTransformRow_JAVA = (jfloatArray)(*env)->GetObjectArrayElement(env, currentTransform_JAVA, k);
                        dimensionOfCurrentTermination = (*env)->GetArrayLength(env, currentTransformRow_JAVA);

                        (*env)->GetFloatArrayRegion(env, currentTransformRow_JAVA, 0, dimensionOfCurrentTermination, currentTransformRow);

                        for(l = 0; l < dimensionOfCurrentTermination; l++)
                        {
                            floatArraySetElement(currentData->terminationTransforms, l * currentData->totalNumTransformRows + transformRowIndex, currentTransformRow[l]); 
                        }

                        intArraySetElement(currentData->terminationOutputIndexor, transformRowIndex, terminationOffset + dimensionIndex + k);
                        intArraySetElement(currentData->transformRowToInputIndexor, transformRowIndex, networkArrayOffsetInTerminations + j);

                        transformRowIndex++;

                        (*env)->DeleteLocalRef(env, currentTransformRow_JAVA);
                    }

                    terminationOffset += currentData->totalEnsembleDimension;

                    if(i == startEnsembleIndex)
                    {
                        intArraySetElement(currentData->inputDimension, networkArrayTerminationIndex, dimensionOfCurrentTermination);
                        networkArrayTerminationIndex++;
                    }

                    (*env)->DeleteLocalRef(env, currentTransform_JAVA);
                }
                else
                {
                    // for non decoded terminations
                    currentTransform_JAVA = (jobjectArray) (*env)->GetObjectArrayElement(env, transformsForCurrentEnsemble_JAVA, j);
                    currentTransformRow_JAVA = (jfloatArray)(*env)->GetObjectArrayElement(env, currentTransform_JAVA, 0);

                    dimensionOfCurrentTermination = (*env)->GetArrayLength(env, currentTransformRow_JAVA);

                    (*env)->GetFloatArrayRegion(env, currentTransformRow_JAVA, 0, dimensionOfCurrentTermination, currentTransformRow);

                    for(l = 0; l < dimensionOfCurrentTermination; l++)
                    {
                        floatArraySetElement(currentData->NDterminationWeights, NDterminationIndexInEnsemble, currentTransformRow[l]); 
                        NDterminationIndexInEnsemble += currentData->numEnsembles;
                    }

                    if(i == startEnsembleIndex)
                    {
                        intArraySetElement(currentData->inputDimension, networkArrayTerminationIndex, dimensionOfCurrentTermination);
                        networkArrayTerminationIndex++;
                    }

                    intArraySetElement(currentData->NDterminationInputIndexor, NDterminationIndex, networkArrayOffsetInTerminations + j);

                    NDterminationIndex++;

                    (*env)->DeleteLocalRef(env, currentTransform_JAVA);
                    (*env)->DeleteLocalRef(env, currentTransformRow_JAVA);
                }
                
            }

            intArraySetElement(currentData->ensembleOffsetInDimensions, i, dimensionIndex);
            intArraySetElement(currentData->ensembleDimension, i, dimensionOfCurrentEnsemble);
            intArraySetElement(currentData->ensembleNumTerminations, i, numTerminationsForCurrentEnsemble);

            dimensionIndex += dimensionOfCurrentEnsemble;

            (*env)->DeleteLocalRef(env, transformsForCurrentEnsemble_JAVA);
            (*env)->DeleteLocalRef(env, tauForCurrentEnsemble_JAVA);
        }

        networkArrayOffsetInTerminations += numTerminationsForCurrentEnsemble;
    }

    free(isDecodedTermination);
    free(currentTransformRow);
}

void storeNeuronData(JNIEnv *env, jobjectArray neuronData_JAVA, int* isSpikingEnsemble, NengoGPUData* currentData)
{
    int i, j, currentNumNeurons, neuronDataLength;

    jfloatArray neuronDataForCurrentEnsemble_JAVA;
    float* neuronDataForCurrentEnsemble;
    
    int ensembleJavaIndex = 0, neuronIndex = 0;

    for(i = 0; i < currentData->numEnsembles; i++)
    {
        ensembleJavaIndex = intArrayGetElement(currentData->ensembleIndexInJavaArray, i);
        neuronDataForCurrentEnsemble_JAVA = (jfloatArray) (*env)->GetObjectArrayElement(env, neuronData_JAVA, ensembleJavaIndex);
        neuronDataLength = (*env)->GetArrayLength(env, neuronDataForCurrentEnsemble_JAVA);
        neuronDataForCurrentEnsemble = (float*) malloc( neuronDataLength * sizeof(float));
        (*env)->GetFloatArrayRegion(env, neuronDataForCurrentEnsemble_JAVA, 0, neuronDataLength, neuronDataForCurrentEnsemble);

        (*env)->DeleteLocalRef(env, neuronDataForCurrentEnsemble_JAVA);

        currentNumNeurons = neuronDataForCurrentEnsemble[0];
        intArraySetElement(currentData->ensembleNumNeurons, i, currentNumNeurons);
        floatArraySetElement(currentData->ensembleTauRC, i, neuronDataForCurrentEnsemble[1]);
        floatArraySetElement(currentData->ensembleTauRef, i, neuronDataForCurrentEnsemble[2]);

        intArraySetElement(currentData->ensembleOffsetInNeurons, i, neuronIndex);

        for(j = 0; j < currentNumNeurons; j++)
        {
            floatArraySetElement(currentData->neuronBias, neuronIndex, neuronDataForCurrentEnsemble[5+j]);
            floatArraySetElement(currentData->neuronScale, neuronIndex, neuronDataForCurrentEnsemble[5 + currentNumNeurons + j]);
            intArraySetElement(currentData->neuronToEnsembleIndexor, neuronIndex, i);

            neuronIndex++;
        }

        intArraySetElement(currentData->isSpikingEnsemble, i, isSpikingEnsemble[ensembleJavaIndex]);

        free(neuronDataForCurrentEnsemble);
    }

}

void storeEncoders(JNIEnv *env, jobjectArray encoders_JAVA, NengoGPUData* currentData)
{
    int i, j, k;
    int ensembleIndexInJavaArray = 0;
    int numNeuronsForCurrentEnsemble;
    int dimensionOfCurrentEnsemble;

    jobjectArray encoderForCurrentEnsemble_JAVA;
    jfloatArray currentEncoderRow_JAVA;

    float* temp_encoders = (float*) malloc(currentData->totalEncoderSize * sizeof(float));
    int* temp_encoder_offset = (int*)malloc(currentData->numEnsembles * sizeof(int)), *temp;

    char* name;

    int numNeurons, offset = 0, rowOffset, neuronOffset = 0, ensembleIndex = 0, encoderRowIndex = 0, ensembleOffsetInNeurons;
    // totalNumEnsembles is a global variable denoting the number of ensembles in the entire run, not just those allocated to this device
    // here we get encoders for this device out of encoders java array, but they're not in the order we want
    for(i = 0; i < currentData->numEnsembles; i++)
    {
        temp_encoder_offset[i] = offset; 

        ensembleIndexInJavaArray = intArrayGetElement(currentData->ensembleIndexInJavaArray, i);
        encoderForCurrentEnsemble_JAVA = (jobjectArray) (*env)->GetObjectArrayElement(env, encoders_JAVA, ensembleIndexInJavaArray);
        numNeuronsForCurrentEnsemble = intArrayGetElement(currentData->ensembleNumNeurons, i);
        dimensionOfCurrentEnsemble = intArrayGetElement(currentData->ensembleDimension, i);

        for(j = 0; j < numNeuronsForCurrentEnsemble; j++)
        {
            currentEncoderRow_JAVA = (jfloatArray) (*env)->GetObjectArrayElement(env, encoderForCurrentEnsemble_JAVA, j);
            (*env)->GetFloatArrayRegion(env, currentEncoderRow_JAVA, 0, dimensionOfCurrentEnsemble, temp_encoders + offset); 
            offset += dimensionOfCurrentEnsemble;

            (*env)->DeleteLocalRef(env, currentEncoderRow_JAVA);
        }
        
        (*env)->DeleteLocalRef(env, encoderForCurrentEnsemble_JAVA);
    }

    // So now we have all the encoders in a C array, temp_encoders, but not in the order we want them in


    // order ensembles by decreasing dimension
    name = "ensembleOrderInEncoders";
    currentData->ensembleOrderInEncoders = newIntArray(currentData->numEnsembles, name);
    temp = sort(currentData->ensembleDimension->array, currentData->numEnsembles, 1);
    intArraySetData(currentData->ensembleOrderInEncoders, temp, currentData->numEnsembles);
    free(temp);


    // we have to make an array of offsets into the final encoder array so that we know how long each row is
    // what this is essentially doing is recording, for each x between 1 and the max number of dimensions,
    // how many ensembles have at least x dimensions
    for(i = 1; i <= currentData->maxDimension; i++)
    {
        numNeurons = 0;

        for(j = 0; j < currentData->numEnsembles; j++)
        {
            if(intArrayGetElement(currentData->ensembleDimension,j) >= i)
            {
                numNeurons += intArrayGetElement(currentData->ensembleNumNeurons, j);
            }
        }

        intArraySetElement(currentData->encoderStride, i - 1,    numNeurons);
    }


    // now we have the order we want the encoders to appear in the array, and the row lengths in the new array, 
    // so we just have to transform temp_encoders using this ordering
    for(i = 0; i < currentData->numEnsembles; i++)
    {
        ensembleIndex = intArrayGetElement(currentData->ensembleOrderInEncoders,i);

        offset = temp_encoder_offset[ensembleIndex];

        numNeuronsForCurrentEnsemble = intArrayGetElement(currentData->ensembleNumNeurons, ensembleIndex);
        dimensionOfCurrentEnsemble = intArrayGetElement(currentData->ensembleDimension, ensembleIndex);

        rowOffset = 0;
        for(k = 0; k < dimensionOfCurrentEnsemble; k++)
        {
            for(j = 0; j < numNeuronsForCurrentEnsemble; j++)
            {
                floatArraySetElement(currentData->encoders, rowOffset + neuronOffset + j,    temp_encoders[offset + j * dimensionOfCurrentEnsemble + k]);
            }

            rowOffset += intArrayGetElement(currentData->encoderStride,k);
        }

        neuronOffset += numNeuronsForCurrentEnsemble;
    }

    free(temp_encoder_offset);
    free(temp_encoders);



    // construct array encoderRowToEnsembleIndexor which maintains, for each row encoder row, which ensemble it belongs to
    for(i = 0; i < currentData->numEnsembles; i++)
    {
        ensembleIndex = intArrayGetElement(currentData->ensembleOrderInEncoders, i);

        numNeurons = intArrayGetElement(currentData->ensembleNumNeurons, ensembleIndex);
        ensembleOffsetInNeurons = intArrayGetElement(currentData->ensembleOffsetInNeurons, ensembleIndex);

        for(j = 0; j < numNeurons; j++)
        {
            intArraySetElement(currentData->encoderRowToEnsembleIndexor, encoderRowIndex, ensembleIndex); 
            intArraySetElement(currentData->encoderRowToNeuronIndexor, encoderRowIndex, ensembleOffsetInNeurons + j); 
            encoderRowIndex++;
        }
    }

}

void storeDecoders(JNIEnv* env, jobjectArray decoders_JAVA, NengoGPUData* currentData, int* networkArrayData)
{
    int i, j, k;

    jobjectArray decodersForCurrentEnsemble_JAVA;
    jobjectArray currentDecoder_JAVA;
    jfloatArray currentDecoderRow_JAVA;
    int decoderIndex = 0, dimensionOfCurrentDecoder, numDecodersForCurrentEnsemble, ensembleIndexInJavaArray;
    int ensembleIndex = 0, numNeuronsForCurrentEnsemble, decoderRowIndex = 0, offset = 0, rowOffset = 0, offsetInOutput;

    int* ensembleOutputSize = (int*)malloc(currentData->numEnsembles * sizeof(int)); 
    int* ensembleOffsetInOutput = (int*)malloc(currentData->numEnsembles * sizeof(int)), *temp; 

    // populate currentData->originOffsetInOutput and ensembleOutputSize
    int outputSize, originOffsetInOutput = 0, numOutputs;

    char* name;

    decoderIndex = 0;
    for(j = 0; j < currentData->numEnsembles; j++)
    {
        ensembleIndexInJavaArray = intArrayGetElement(currentData->ensembleIndexInJavaArray,j);

        decodersForCurrentEnsemble_JAVA = (jobjectArray) (*env)->GetObjectArrayElement(env, decoders_JAVA, ensembleIndexInJavaArray);
        numDecodersForCurrentEnsemble = (int) (*env)->GetArrayLength(env, decodersForCurrentEnsemble_JAVA);

        outputSize = 0;

        ensembleOffsetInOutput[j] = originOffsetInOutput;

        for(k = 0; k < numDecodersForCurrentEnsemble; k++)
        {
            currentDecoder_JAVA = (jobjectArray) (*env)->GetObjectArrayElement(env, decodersForCurrentEnsemble_JAVA, k);
            currentDecoderRow_JAVA = (jfloatArray) (*env)->GetObjectArrayElement(env, currentDecoder_JAVA, 0);

            dimensionOfCurrentDecoder = (int) (*env)->GetArrayLength(env, currentDecoderRow_JAVA);
            intArraySetElement(currentData->ensembleOriginDimension, decoderIndex, dimensionOfCurrentDecoder);

            outputSize += dimensionOfCurrentDecoder;

            intArraySetElement(currentData->ensembleOriginOffsetInOutput, decoderIndex, originOffsetInOutput);

            originOffsetInOutput += dimensionOfCurrentDecoder;
            decoderIndex++;

            (*env)->DeleteLocalRef(env, currentDecoder_JAVA);
            (*env)->DeleteLocalRef(env, currentDecoderRow_JAVA);
        }

        ensembleOutputSize[j] = outputSize;

        (*env)->DeleteLocalRef(env, decodersForCurrentEnsemble_JAVA);
    }
    
    
    // populate decoder stride
    for(i = 1; i <= currentData->maxNumNeurons; i++)
    {
        numOutputs = 0;

        for(j = 0; j < currentData->numEnsembles; j++)
        {
            if(intArrayGetElement(currentData->ensembleNumNeurons,j) >= i)
            {
                numOutputs += ensembleOutputSize[j]; 
            }
        }

        intArraySetElement(currentData->decoderStride, i - 1, numOutputs);
    }
    
    // sort the ensembles in order of decreasing number of neurons
    name = "ensembleOrderInDecoders";
    currentData->ensembleOrderInDecoders = newIntArray(currentData->numEnsembles, name); 
    temp = sort(currentData->ensembleNumNeurons->array, currentData->numEnsembles, 1);
    intArraySetData(currentData->ensembleOrderInDecoders, temp, currentData->numEnsembles);
    free(temp);

    for(i = 0; i < currentData->numEnsembles; i++)
    {
        ensembleIndex = intArrayGetElement(currentData->ensembleOrderInDecoders,i);
        ensembleIndexInJavaArray = intArrayGetElement(currentData->ensembleIndexInJavaArray, ensembleIndex);

        numNeuronsForCurrentEnsemble = intArrayGetElement(currentData->ensembleNumNeurons,ensembleIndex);

        decodersForCurrentEnsemble_JAVA = (jobjectArray) (*env)->GetObjectArrayElement(env, decoders_JAVA, ensembleIndexInJavaArray);
        numDecodersForCurrentEnsemble = (int) (*env)->GetArrayLength(env, decodersForCurrentEnsemble_JAVA);

        intArraySetElement(currentData->ensembleNumOrigins, ensembleIndex, numDecodersForCurrentEnsemble);

        rowOffset = 0;

        for(j = 0; j < numNeuronsForCurrentEnsemble; j++)
        {
            decoderRowIndex = offset;
            for(k = 0; k < numDecodersForCurrentEnsemble; k++)
            {
                currentDecoder_JAVA = (jobjectArray) (*env)->GetObjectArrayElement(env, decodersForCurrentEnsemble_JAVA, k);
                currentDecoderRow_JAVA = (jfloatArray) (*env)->GetObjectArrayElement(env, currentDecoder_JAVA, j);
                
                dimensionOfCurrentDecoder = (*env)->GetArrayLength(env, currentDecoderRow_JAVA);

                if(rowOffset + decoderRowIndex + dimensionOfCurrentDecoder <= currentData->totalDecoderSize)
                {
                    (*env)->GetFloatArrayRegion(env, currentDecoderRow_JAVA, 0, dimensionOfCurrentDecoder, currentData->decoders->array + rowOffset + decoderRowIndex);
                }
                else
                {
                    printf("error: accessing array out of bounds: decoders\n");
                    exit(EXIT_FAILURE);
                }

                decoderRowIndex += dimensionOfCurrentDecoder;
                 
                (*env)->DeleteLocalRef(env, currentDecoder_JAVA);
                (*env)->DeleteLocalRef(env, currentDecoderRow_JAVA);
            }

            rowOffset += intArrayGetElement(currentData->decoderStride,j);
        }
        
        offset = decoderRowIndex;

        (*env)->DeleteLocalRef(env, decodersForCurrentEnsemble_JAVA);
    }


    decoderRowIndex = 0;

    // set decoderRowIndexors. Tells each decoder which ensemble it belongs to and where to put its output
    for(i = 0; i < currentData->numEnsembles; i++)
    {
        ensembleIndex = intArrayGetElement(currentData->ensembleOrderInDecoders, i);

        outputSize = ensembleOutputSize[ensembleIndex]; 
        offsetInOutput = ensembleOffsetInOutput[ensembleIndex];

        for(j = 0; j < outputSize; j++)
        {
            intArraySetElement(currentData->decoderRowToEnsembleIndexor, decoderRowIndex, ensembleIndex); 
            intArraySetElement(currentData->decoderRowToOutputIndexor, decoderRowIndex, offsetInOutput + j); 
            decoderRowIndex++;
        }
    }
    
    free(ensembleOutputSize);
    free(ensembleOffsetInOutput);
}


void assignNetworkArrayToDevice(int networkArrayIndex, int* networkArrayData, int* ensembleData, int* collectSpikes, NengoGPUData* currentData)
{
    int i, startEnsembleIndex, endEnsembleIndex;

    currentData->numNetworkArrays++;
    currentData->numInputs += networkArrayData[NENGO_NA_DATA_NUM * networkArrayIndex + NENGO_NA_DATA_NUM_TERMINATIONS];
    currentData->totalInputSize += networkArrayData[NENGO_NA_DATA_NUM * networkArrayIndex + NENGO_NA_DATA_TOTAL_INPUT_SIZE];
    currentData->numNetworkArrayOrigins += networkArrayData[NENGO_NA_DATA_NUM * networkArrayIndex + NENGO_NA_DATA_NUM_ORIGINS];

    startEnsembleIndex = networkArrayData[NENGO_NA_DATA_NUM * networkArrayIndex + NENGO_NA_DATA_FIRST_INDEX];
    endEnsembleIndex = networkArrayData[NENGO_NA_DATA_NUM * networkArrayIndex + NENGO_NA_DATA_END_INDEX];

    networkArrayData[NENGO_NA_DATA_NUM * networkArrayIndex + NENGO_NA_DATA_END_INDEX] -= 
        networkArrayData[NENGO_NA_DATA_NUM * networkArrayIndex + NENGO_NA_DATA_FIRST_INDEX] - currentData->numEnsembles;

    networkArrayData[NENGO_NA_DATA_NUM * networkArrayIndex + NENGO_NA_DATA_FIRST_INDEX] = currentData->numEnsembles;

    for( i = startEnsembleIndex; i < endEnsembleIndex; i++)
    {
        currentData->numEnsembles++;
        currentData->numNeurons += ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_NUM_NEURONS];

        currentData->numDecodedTerminations += ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_NUM_DECODED_TERMINATIONS];
        currentData->totalNumTransformRows += ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_NUM_DECODED_TERMINATIONS] * ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_DIMENSION];
        
        if(ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_NUM_DECODED_TERMINATIONS] > currentData->maxNumDecodedTerminations)
        {
            currentData->maxNumDecodedTerminations = ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_NUM_DECODED_TERMINATIONS];
        }

        if(ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_MAX_TRANSFORM_DIMENSION] > currentData->maxDecodedTerminationDimension)
        {
            currentData->maxDecodedTerminationDimension = ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_MAX_TRANSFORM_DIMENSION];
        }

        if(ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_MAX_ND_TRANSFORM_SIZE] > currentData->maxEnsembleNDTransformSize)
        {
            currentData->maxEnsembleNDTransformSize = ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_MAX_ND_TRANSFORM_SIZE];
        }
        
        currentData->totalEncoderSize += ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_NUM_NEURONS] * ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_DIMENSION];

        currentData->numOrigins += ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_NUM_ORIGINS];
        currentData->totalOutputSize += ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_TOTAL_OUTPUT_SIZE];
        currentData->totalDecoderSize += ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_TOTAL_OUTPUT_SIZE] * ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_NUM_NEURONS];
        
        if(ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_MAX_DECODER_DIMENSION] > currentData->maxOriginDimension)
        {
            currentData->maxOriginDimension = ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_MAX_DECODER_DIMENSION];
        }

        currentData->totalEnsembleDimension += ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_DIMENSION];

        if(ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_DIMENSION] > currentData->maxDimension)
        {
            currentData->maxDimension = ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_DIMENSION];
        }

        if(ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_NUM_NEURONS] > currentData->maxNumNeurons)
        {
            currentData->maxNumNeurons = ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_NUM_NEURONS];
        }
        
        currentData->numNDterminations += ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_NUM_NON_DECODED_TERMINATIONS];

        if(collectSpikes[i])
        {
            currentData->numSpikesToSendBack += ensembleData[NENGO_ENSEMBLE_DATA_NUM * i + NENGO_ENSEMBLE_DATA_NUM_NEURONS];
        }
    }
}

void setupIO(int numProjections, projection* projections, NengoGPUData* currentData, int* networkArrayData, int** originRequiredByJava)
{
    int i, j, k, l;
    int currentDimension, networkArrayIndex = -1, terminationIndexInNetworkArray = -1;
    int projectionMatches, currentNumTerminations = 0, networkArrayJavaIndex; 
    int oldVal, location;

    int* flattenedProjections = (int*) malloc(2 * numProjections * sizeof(int));

    int JavaInputIndex = 0, GPUInputIndex = 0, CPUInputIndex = 0;
    
    // 0 = GPU, 1 = Java, 2 = CPU
    int* terminationLocation = (int*)malloc(currentData->numInputs * sizeof(int));

    int ensembleOriginDimension, ensembleOriginIndex = 0, naOriginIndex = 0, numOrigins, originDimension;
    int naOffsetInEnsembleOriginIndices = 0, naIndexInJavaArray, startEnsembleIndex, endEnsembleIndex;

    int interGPUFlag, interGPUOutputSize = 0, CPUOutputIndex = 0, GPUOutputIndex = 0, JavaOutputIndex = 0;
    int* originLocation = (int*)malloc(currentData->numNetworkArrayOrigins * sizeof(int));

    int indexInNetworkArrayOutput, indexInEnsembleOutput;
    int terminationIndexOnDevice, originIndexOnDevice, terminationOffsetInInput, originOffsetInOutput;

    char* name;


    // flatten the termination side of the projection array
    for(i = 0; i < currentData->numInputs; i++)
    {
        terminationIndexInNetworkArray++;
        while(terminationIndexInNetworkArray == currentNumTerminations)
        {
            networkArrayIndex++;
            networkArrayJavaIndex = intArrayGetElement(currentData->networkArrayIndexInJavaArray, networkArrayIndex);
            currentNumTerminations = networkArrayData[NENGO_NA_DATA_NUM * networkArrayJavaIndex + NENGO_NA_DATA_NUM_TERMINATIONS];
            terminationIndexInNetworkArray = 0;
        }

        currentDimension = intArrayGetElement(currentData->inputDimension, i);

        // find a GPU projection that terminates at the current termination. We can stop as soon as we find
        // one because terminations can only be involved in one projection

        j = 0;
        projectionMatches = 0;
        while(!projectionMatches && j < numProjections)
        {
            projectionMatches = projections[j].destDevice == currentData->device
                                        && projections[j].destinationNode == networkArrayIndex
                                        && projections[j].destinationTermination == terminationIndexInNetworkArray;

            if(projectionMatches)
                break;

            j++;
        }

        // if we found one then we have to see whether that projection is inter or intra device
        if(!projectionMatches)
        {
            intArraySetElement(currentData->terminationOffsetInInput, i, JavaInputIndex);
            JavaInputIndex += currentDimension;
            terminationLocation[i] = 1;
        }
        else
        {
            flattenedProjections[2 * j] = i;

            if(projections[j].sourceDevice == currentData->device)
            {
                intArraySetElement(currentData->terminationOffsetInInput, i, GPUInputIndex);
                GPUInputIndex += currentDimension;
                terminationLocation[i] = 0;
            }
            else
            {
                intArraySetElement(currentData->terminationOffsetInInput, i, CPUInputIndex);
                CPUInputIndex += currentDimension;
                terminationLocation[i] = 2;
            }
        }
    }

    currentData->GPUInputSize = GPUInputIndex;
    currentData->CPUInputSize = CPUInputIndex;
    currentData->JavaInputSize = JavaInputIndex;

    assert(GPUInputIndex + CPUInputIndex + JavaInputIndex == currentData->totalInputSize);

    // adjust the terminationOffsets to reflect the location of each termination (GPU, Java or CPU)
    // can only be done once we know the size of each location
    for(i = 0; i < currentData->numInputs; i++)
    {
        oldVal = intArrayGetElement(currentData->terminationOffsetInInput, i);
        
        location = terminationLocation[i];

        switch(location)
        {
            case 0:
                break;
            case 1:
                intArraySetElement(currentData->terminationOffsetInInput, i, oldVal + currentData->GPUInputSize);
                break;
            case 2:
                intArraySetElement(currentData->terminationOffsetInInput, i, oldVal + currentData->GPUInputSize + currentData->JavaInputSize);
                break;
        }
    }

    free(terminationLocation);


    // here goal is to set networkArrayNumOrigins and networkArrayOriginDimension;
    //
    for(i = 0; i < currentData->numNetworkArrays; i++)
    {    
        naIndexInJavaArray = intArrayGetElement(currentData->networkArrayIndexInJavaArray, i);
        
        numOrigins = networkArrayData[naIndexInJavaArray * NENGO_NA_DATA_NUM + NENGO_NA_DATA_NUM_ORIGINS];

        startEnsembleIndex = networkArrayData[naIndexInJavaArray * NENGO_NA_DATA_NUM + NENGO_NA_DATA_FIRST_INDEX];
        endEnsembleIndex = networkArrayData[naIndexInJavaArray * NENGO_NA_DATA_NUM + NENGO_NA_DATA_END_INDEX];

        intArraySetElement(currentData->networkArrayNumOrigins, i, numOrigins);

        for(j = 0; j < numOrigins; j++)
        {
            originDimension = 0;
            ensembleOriginIndex = naOffsetInEnsembleOriginIndices + j;

            for(k = startEnsembleIndex; k < endEnsembleIndex; k++)
            {
                ensembleOriginDimension = intArrayGetElement(currentData->ensembleOriginDimension, ensembleOriginIndex);
                originDimension += ensembleOriginDimension;
                ensembleOriginIndex += numOrigins;
            }

            intArraySetElement(currentData->networkArrayOriginDimension, naOriginIndex, originDimension);


            naOriginIndex++;
        }

        naOffsetInEnsembleOriginIndices += numOrigins * (endEnsembleIndex - startEnsembleIndex);
    }


    naOriginIndex = 0;

    // here my goal is JUST to set populate the origin side of the flattened projections array, populate
    // origonLocations, temporaririly populate networkArrayOriginOffsetInOutput (which will be corrected later),
    // and determine the sizes of each of the output sections
    //
    for(i = 0; i < currentData->numNetworkArrays; i++)
    {    
        naIndexInJavaArray = intArrayGetElement(currentData->networkArrayIndexInJavaArray, i);
        
        numOrigins = networkArrayData[naIndexInJavaArray * NENGO_NA_DATA_NUM + NENGO_NA_DATA_NUM_ORIGINS];

        for(j = 0; j < numOrigins; j++)
        {
            interGPUFlag = 0;

            for(k = 0; k < numProjections; k++)
            {
                projectionMatches = projections[k].sourceDevice == currentData->device
                                                && projections[k].sourceNode == i
                                                && projections[k].sourceOrigin == j;

                if(projectionMatches)
                {
                    flattenedProjections[2 * k + 1] = naOriginIndex;

                    if(projections[k].destDevice != currentData->device)
                    {
                        interGPUFlag = 1;
                    }
                }
            }

            currentDimension = intArrayGetElement(currentData->networkArrayOriginDimension, naOriginIndex);

            interGPUOutputSize += interGPUFlag ? currentDimension : 0;

            // set which section of the output array the current origin belongs in: 0 if it stays on the GPU, 1 if it 
            // has to go all the way to Java, 2 if it doesn't have to go all the way back to java but does hava to go to another GPU
            if(originRequiredByJava[naIndexInJavaArray][j])
            {
                intArraySetElement(currentData->networkArrayOriginOffsetInOutput, naOriginIndex, JavaOutputIndex);
                originLocation[naOriginIndex] = 1;
                JavaOutputIndex += currentDimension;
            }
            else if(interGPUFlag)
            {
                intArraySetElement(currentData->networkArrayOriginOffsetInOutput, naOriginIndex, CPUOutputIndex);
                originLocation[naOriginIndex] = 2;
                CPUOutputIndex += currentDimension;
            }
            else
            {
                intArraySetElement(currentData->networkArrayOriginOffsetInOutput, naOriginIndex, GPUOutputIndex);
                originLocation[naOriginIndex] = 0;
                GPUOutputIndex += currentDimension;
            }

            naOriginIndex++;
        }
    }


    currentData->GPUOutputSize = GPUOutputIndex;
    currentData->JavaOutputSize = JavaOutputIndex;
    currentData->CPUOutputSize = currentData->totalOutputSize - GPUOutputIndex;

    currentData->interGPUOutputSize = interGPUOutputSize;

    // adjust the networkArrayOriginOffsetInOutput to reflect the location of each network array Origin (GPU, Java or CPU)
    // can only be done once we know the size of each section
    //
    for(i = 0; i < currentData->numNetworkArrayOrigins; i++)
    {
        oldVal = intArrayGetElement(currentData->networkArrayOriginOffsetInOutput, i);
        
        location = originLocation[i];

        switch(location)
        {
            case 0:
                break;
            case 1:
                intArraySetElement(currentData->networkArrayOriginOffsetInOutput, i, oldVal + currentData->GPUOutputSize);
                break;
            case 2:
                intArraySetElement(currentData->networkArrayOriginOffsetInOutput, i, oldVal + currentData->GPUOutputSize + currentData->JavaOutputSize);
                break;
        }
    }

    free(originLocation);


    /*
    These are arrays whose size relies on GPUInputSize, CPUInputSize or JavaInput size, and thus cannot be made until we have those
    values. Because of this, we cannot create these arrays in the function initializeNengoGPUData like we do with all the other arrays
    */
    name = "GPUTerminationToOriginMap";
    currentData->GPUTerminationToOriginMap = newIntArray(currentData->GPUInputSize, name);

    name = "outputHost";
    currentData->outputHost = newFloatArray(currentData->CPUOutputSize + currentData->numSpikesToSendBack, name);

    name = "sharedData_outputIndex";
    currentData->sharedData_outputIndex = newIntArray(currentData->interGPUOutputSize, name);

    name = "sharedData_sharedIndex";
    currentData->sharedData_sharedIndex = newIntArray(currentData->interGPUOutputSize, name);


    // here my goal is JUST to populate the ensembleOutputToNetworkArrayOutputMap 
    naOffsetInEnsembleOriginIndices = 0;
    naOriginIndex = 0;
    
    for(i = 0; i < currentData->numNetworkArrays; i++)
    {    
        naIndexInJavaArray = intArrayGetElement(currentData->networkArrayIndexInJavaArray, i);
        
        numOrigins = networkArrayData[naIndexInJavaArray * NENGO_NA_DATA_NUM + NENGO_NA_DATA_NUM_ORIGINS];

        startEnsembleIndex = networkArrayData[naIndexInJavaArray * NENGO_NA_DATA_NUM + NENGO_NA_DATA_FIRST_INDEX];
        endEnsembleIndex = networkArrayData[naIndexInJavaArray * NENGO_NA_DATA_NUM + NENGO_NA_DATA_END_INDEX];

        for(j = 0; j < numOrigins; j++)
        {
            indexInNetworkArrayOutput = intArrayGetElement(currentData->networkArrayOriginOffsetInOutput, naOriginIndex);
            
            ensembleOriginIndex = naOffsetInEnsembleOriginIndices + j;

            for(k = startEnsembleIndex; k < endEnsembleIndex; k++)
            {
                indexInEnsembleOutput = intArrayGetElement(currentData->ensembleOriginOffsetInOutput, ensembleOriginIndex);
                ensembleOriginDimension = intArrayGetElement(currentData->ensembleOriginDimension, ensembleOriginIndex);

                for(l = 0; l < ensembleOriginDimension; l++)
                {
                    intArraySetElement(currentData->ensembleOutputToNetworkArrayOutputMap, indexInNetworkArrayOutput + l, indexInEnsembleOutput + l);
                }

                ensembleOriginIndex += numOrigins;
                indexInNetworkArrayOutput += ensembleOriginDimension;
            }

            naOriginIndex++;
        }

        naOffsetInEnsembleOriginIndices += numOrigins * (endEnsembleIndex - startEnsembleIndex);
    }


    // Use the flattened projections to create a map from the input to the output following the projections
    // This way we can launch a kernel for each projection on the GPU, have it look up where it gets its output from
    // fetch it from the output array and put it in the input array
    for(i = 0; i < numProjections; i++)
    {
        if(projections[i].sourceDevice == currentData->device && projections[i].destDevice == currentData->device)
        {
            terminationIndexOnDevice = flattenedProjections[i * 2];
            originIndexOnDevice = flattenedProjections[i * 2 + 1];

            terminationOffsetInInput = intArrayGetElement(currentData->terminationOffsetInInput, terminationIndexOnDevice);
            originOffsetInOutput = intArrayGetElement(currentData->networkArrayOriginOffsetInOutput, originIndexOnDevice);
            currentDimension = intArrayGetElement(currentData->inputDimension, terminationIndexOnDevice);

            for(j = 0; j < currentDimension; j++)
            {
                intArraySetElement(currentData->GPUTerminationToOriginMap, terminationOffsetInInput + j, originOffsetInOutput + j);
            }
        }
        else if(projections[i].sourceDevice == currentData->device && projections[i].destDevice != currentData->device)
        {
            originIndexOnDevice = flattenedProjections[i * 2 + 1];
            projections[i].offsetInSource = intArrayGetElement(currentData->networkArrayOriginOffsetInOutput, originIndexOnDevice) - currentData->GPUOutputSize;
        }
        else if(projections[i].destDevice == currentData->device && projections[i].sourceDevice != currentData->device)
        {

            terminationIndexOnDevice = flattenedProjections[i * 2];
            projections[i].offsetInDestination = intArrayGetElement(currentData->terminationOffsetInInput, terminationIndexOnDevice) - currentData->GPUInputSize + currentData->offsetInSharedInput;
        }
        
    }

    free(flattenedProjections);
}





// this function should be called before setupIO, but after setupNeuronData.
void setupSpikes(int* collectSpikes, NengoGPUData* currentData)
{
    int i, j, indexInJavaArray, spikeIndex = 0, currentNumNeurons, currentOffsetInNeurons;

    //create an array which can be used by the GPU to extract the spikes we want to send back form the main spike array 
    currentData->spikeMap = newIntArray(currentData->numSpikesToSendBack, "spikeMap");

    //populate these arrays
    for(i = 0; i < currentData->numEnsembles; i++)
    {
        indexInJavaArray = intArrayGetElement(currentData->ensembleIndexInJavaArray, i);

        if(collectSpikes[indexInJavaArray])
        {
            currentOffsetInNeurons = intArrayGetElement(currentData->ensembleOffsetInNeurons, i);
            currentNumNeurons = intArrayGetElement(currentData->ensembleNumNeurons, i);

            for(j = 0; j < currentNumNeurons; j++)
            {
                intArraySetElement(currentData->spikeMap, spikeIndex, currentOffsetInNeurons + j); 
                spikeIndex++;
            }
        }
    }
}

// this sets the shared memory maps (maps for getting data out of the output arrays and into
// the shared input arrays which is the means of inter-gpu communication). relies on the offsetInSource
// and offsetInDestination fields of the projection structure being set properly (happens in setupIO).
void createSharedMemoryMaps(int numProjections, projection* projections)
{
    int i, j, sharedIndex = 0, indexInProjection;
    projection* p;
    NengoGPUData* currentData;

    for(j = 0; j < numDevices; j++)
    {
        currentData = nengoDataArray[j];
        sharedIndex = 0;
        for(i = 0; i < numProjections; i++)
        {
            p = projections + i;

            if(p->sourceDevice == currentData->device && p->destDevice != currentData->device)
            {
                for(indexInProjection = 0; indexInProjection < p->size; indexInProjection++)
                {
                    intArraySetElement(currentData->sharedData_outputIndex, sharedIndex, p->offsetInSource + indexInProjection);
                    intArraySetElement(currentData->sharedData_sharedIndex, sharedIndex, p->offsetInDestination + indexInProjection);
                    sharedIndex++;
                }
            }
        }
    }
}

JNIEXPORT jint JNICALL Java_ca_nengo_util_impl_NEFGPUInterface_nativeGetNumDevices
    (JNIEnv *env, jclass class)
{
    jint numGPU = (jint) getGPUDeviceCount();

    return numGPU;
}

// This function takes as arguments all the information required by the ensembles to run that won't change from step to step: decoders, encoders, transformations.
// This is called only once, at the beginning of a run (specifically, when the GPUNodeThreadPool is created). 
JNIEXPORT void JNICALL Java_ca_nengo_util_impl_NEFGPUInterface_nativeSetupRun
    (JNIEnv *env, jclass class, jobjectArray terminationTransforms_JAVA, jobjectArray isDecodedTermination_JAVA, 
    jobjectArray terminationTau_JAVA, jobjectArray encoders_JAVA, jobjectArray decoders_JAVA, 
    jobjectArray neuronData_JAVA, jobjectArray projections_JAVA, jobjectArray networkArrayData_JAVA, jobjectArray ensembleData_JAVA, 
    jintArray isSpikingEnsemble_JAVA, jintArray collectSpikes_JAVA, jobjectArray originRequiredByJava_JAVA, jfloat maxTimeStep, jintArray deviceForNetworkArrays_JAVA, jint numDevicesRequested)
{
    int i, j, k, endIndex, originNodeIndex, termNodeIndex;
    int numAvailableDevices = getGPUDeviceCount();
    
    int numProjections, *currentProjection, *networkArrayData, *ensembleData, numNeurons;
    int *isSpikingEnsemble, *collectSpikes, **originRequiredByJava, *networkArrayJavaIndexToDeviceIndex;
    projection* projections;

    jintArray tempIntArray_JAVA;
    jintArray dataRow_JAVA;

    NengoGPUData* currentData;

    // Setup NengoGPU
    printf("NengoGPU: SETUP\n"); 

    numDevices = numDevicesRequested;

    printf("Using %d devices. %d available\n", numDevices, numAvailableDevices);

    nengoDataArray = (NengoGPUData**) malloc(sizeof(NengoGPUData*) * numDevices);

    totalNumEnsembles = (int) (*env)->GetArrayLength(env, neuronData_JAVA);
    totalNumNetworkArrays = (int)(*env)->GetArrayLength(env, networkArrayData_JAVA);

    // Create the NengoGPUData structs, one per device.
    for(i = 0; i < numDevices; i++)
    {
        nengoDataArray[i] = getNewNengoGPUData();
        nengoDataArray[i]->maxTimeStep = (float)maxTimeStep;
    }

    // sort out the projections
    numProjections = (int) (*env)->GetArrayLength(env, projections_JAVA);
    currentProjection = (int*)malloc(PROJECTION_DATA_SIZE * sizeof(int));
    projections = (projection*) malloc( numProjections * sizeof(projection));
    for(i=0; i < numProjections; i++)
    {
        tempIntArray_JAVA = (jintArray)(*env)->GetObjectArrayElement(env, projections_JAVA, i);
        (*env)->GetIntArrayRegion(env, tempIntArray_JAVA, 0, PROJECTION_DATA_SIZE, currentProjection);
        storeProjection(projections + i, currentProjection);

        (*env)->DeleteLocalRef(env, tempIntArray_JAVA);
    }

    free(currentProjection);
    (*env)->DeleteLocalRef(env, projections_JAVA);

    networkArrayData = (int*)malloc(totalNumNetworkArrays * NENGO_NA_DATA_NUM * sizeof(int));
    ensembleData = (int*)malloc(totalNumEnsembles * NENGO_ENSEMBLE_DATA_NUM * sizeof(int));

    // store Network Array Data, and in a separate array store the number of neurons for each
    // networkArray to be used in creating the device configuration
    for(i = 0; i < totalNumNetworkArrays; i++)
    {
        dataRow_JAVA = (jintArray) (*env)->GetObjectArrayElement(env, networkArrayData_JAVA, i);
        (*env)->GetIntArrayRegion(env, dataRow_JAVA, 0, NENGO_NA_DATA_NUM, networkArrayData + i * NENGO_NA_DATA_NUM);
        
        numNeurons = networkArrayData[i * NENGO_NA_DATA_NUM + NENGO_NA_DATA_NUM_NEURONS];

        (*env)->DeleteLocalRef(env, dataRow_JAVA);
    }

    (*env)->DeleteLocalRef(env, networkArrayData_JAVA);

    // store ensemble data
    for(i = 0; i < totalNumEnsembles; i++)
    {
        dataRow_JAVA = (jintArray) (*env)->GetObjectArrayElement(env, ensembleData_JAVA, i);
        (*env)->GetIntArrayRegion(env, dataRow_JAVA, 0, NENGO_ENSEMBLE_DATA_NUM, ensembleData + i * NENGO_ENSEMBLE_DATA_NUM);
        (*env)->DeleteLocalRef(env, dataRow_JAVA);
    }

    (*env)->DeleteLocalRef(env, ensembleData_JAVA);
 
    // Distribute the ensembles to the devices. Tries to minimize communication required between GPUs.
    //generateNengoGPUDeviceConfiguration(totalNumNeurons, networkArrayNumNeurons, numProjections, projections, adjacencyMatrix, deviceForNetworkArray);
    
    deviceForNetworkArray = (int*) malloc(totalNumNetworkArrays * sizeof(int));
    deviceForEnsemble = (int*) malloc(totalNumEnsembles * sizeof(int));

    (*env)->GetIntArrayRegion(env, deviceForNetworkArrays_JAVA, 0, totalNumNetworkArrays, deviceForNetworkArray); 
    (*env)->DeleteLocalRef(env, deviceForNetworkArrays_JAVA);

    // Store which device each ensemble belongs to based on which device each network array belongs to
    for(i = 0; i < totalNumNetworkArrays; i++)
    {
        j = networkArrayData[i * NENGO_NA_DATA_NUM + NENGO_NA_DATA_FIRST_INDEX];
        endIndex = networkArrayData[i * NENGO_NA_DATA_NUM + NENGO_NA_DATA_END_INDEX];

        for(; j < endIndex; j++)
        {
            deviceForEnsemble[j] = deviceForNetworkArray[i];
        }
    }

    for(i = 0; i < numProjections; i++)
    {
        originNodeIndex = projections[i].sourceNode;
        termNodeIndex = projections[i].destinationNode;

        projections[i].sourceDevice = deviceForNetworkArray[originNodeIndex];
        projections[i].destDevice = deviceForNetworkArray[termNodeIndex];
    }

    isSpikingEnsemble = (int*) malloc(totalNumEnsembles * sizeof(int));
    (*env)->GetIntArrayRegion(env, isSpikingEnsemble_JAVA, 0, totalNumEnsembles, isSpikingEnsemble); 
    (*env)->DeleteLocalRef(env, isSpikingEnsemble_JAVA);

    collectSpikes = (int*) malloc(totalNumEnsembles * sizeof(int));
    (*env)->GetIntArrayRegion(env, collectSpikes_JAVA, 0, totalNumEnsembles, collectSpikes); 
    (*env)->DeleteLocalRef(env, collectSpikes_JAVA);

    originRequiredByJava = (int**) malloc(totalNumNetworkArrays * sizeof(int*));
    for(i = 0; i < totalNumNetworkArrays; i++)
    {
        tempIntArray_JAVA = (jintArray) (*env)->GetObjectArrayElement(env, originRequiredByJava_JAVA, i); 
        j = (*env)->GetArrayLength(env, tempIntArray_JAVA);

        originRequiredByJava[i] = (int*) malloc(j * sizeof(int));
        (*env)->GetIntArrayRegion(env, tempIntArray_JAVA, 0, j, originRequiredByJava[i]); 
        
        (*env)->DeleteLocalRef(env, tempIntArray_JAVA);
    }

    (*env)->DeleteLocalRef(env, originRequiredByJava_JAVA);

    // set the number fields in the NengoGPUData structs so that it knows how big to make its internal arrays
    networkArrayJavaIndexToDeviceIndex = (int*)malloc(totalNumEnsembles * sizeof(int));
    for(i = 0; i < totalNumNetworkArrays; i++)
    {
        currentData = nengoDataArray[deviceForNetworkArray[i]];

        networkArrayJavaIndexToDeviceIndex[i] = currentData->numNetworkArrays;

        assignNetworkArrayToDevice(i, networkArrayData, ensembleData, collectSpikes, currentData); 
    }

    // Adjust projections to reflect the distribution of ensembles to devices.
    adjustProjections(numProjections, projections, networkArrayJavaIndexToDeviceIndex);
    free( networkArrayJavaIndexToDeviceIndex );

    sharedInputSize = 0;

    // Now we start to load the data into the NengoGPUData struct for each device. 
    // (though the data doesn't get put on the actual device just yet).
    // Because of the CUDA architecture, we have to do some weird things to get a good speedup. 
    // These arrays that store the transforms, decoders, are setup in a non-intuitive way so 
    // that memory accesses can be parallelized in CUDA kernels. For more information, see the NengoGPU user manual.
    for(i = 0; i < numDevices; i++)
    {
        currentData = nengoDataArray[i];
        
        currentData->device = i;

        currentData->offsetInSharedInput = sharedInputSize;

        currentData->numTerminations = currentData->numDecodedTerminations + currentData->numNDterminations;
        currentData->totalTransformSize = currentData->maxDecodedTerminationDimension * currentData->totalNumTransformRows;
        currentData->totalNonDecodedTransformSize = currentData->maxEnsembleNDTransformSize * currentData->numEnsembles;

        initializeNengoGPUData(currentData);
        

        // set networkArrayIndexInJavaArray
        j = 0;
        for(k = 0; k < currentData->numNetworkArrays; k++)
        {
            while(deviceForNetworkArray[j] != currentData->device)
            {
                j++;
            }

            intArraySetElement(currentData->networkArrayIndexInJavaArray, k, j);
            j++;
        }

        // set ensembleIndexInJavaArray
        j = 0;
        for(k = 0; k < currentData->numEnsembles; k++)
        {
            while(deviceForEnsemble[j] != currentData->device)
            {
                j++;
            }

            intArraySetElement(currentData->ensembleIndexInJavaArray, k, j);
            j++;
        }
 
        storeTerminationData(env, terminationTransforms_JAVA, terminationTau_JAVA, isDecodedTermination_JAVA, currentData, networkArrayData);

        storeNeuronData(env, neuronData_JAVA, isSpikingEnsemble, currentData);

        storeEncoders(env, encoders_JAVA, currentData);

        storeDecoders(env, decoders_JAVA, currentData, networkArrayData);

        setupSpikes(collectSpikes, currentData);

        setupIO(numProjections, projections, currentData, networkArrayData, originRequiredByJava);

        sharedInputSize += currentData->JavaInputSize + currentData->CPUInputSize;
    }

    (*env)->DeleteLocalRef(env, terminationTransforms_JAVA);
    (*env)->DeleteLocalRef(env, terminationTau_JAVA);
    (*env)->DeleteLocalRef(env, isDecodedTermination_JAVA);
    (*env)->DeleteLocalRef(env, neuronData_JAVA);
    (*env)->DeleteLocalRef(env, encoders_JAVA);
    (*env)->DeleteLocalRef(env, decoders_JAVA);

    
    for(i = 0; i < totalNumNetworkArrays; i++)
    {
        free(originRequiredByJava[i]);
    }

    free(originRequiredByJava);

    free(isSpikingEnsemble);
    free(collectSpikes);
    free(ensembleData);
    free(networkArrayData);

    // set the shared memory maps for the device. We can't do this until
    // all the intra-device projections have had their offsets set, which means each device has to have
    // had setupIO called on it.
    createSharedMemoryMaps(numProjections, projections);

    // Allocate and initialize the shared array
    sharedInput = (float*)malloc(sharedInputSize * sizeof(float));
    for(i = 0; i < sharedInputSize; i++)
    {
        sharedInput[i] = 0.0;
    }

    free(projections);


    // we have all the data we need, now start the worker threads which control the GPU's directly.
    run_start();
}


// Called once per step from the Java code. Puts the representedInputValues in the proper form for processing, then tells each GPU thread
// to take a step. Once they've finished the step, this function puts the representedOutputValues and spikes in the appropriate Java
// arrays so that they can be read on the Java side when this call returns.
JNIEXPORT void JNICALL Java_ca_nengo_util_impl_NEFGPUInterface_nativeStep
    (JNIEnv *env, jclass class, jobjectArray input, jobjectArray output, jobjectArray spikes, jfloat startTime_JAVA, jfloat endTime_JAVA)
{
    jobjectArray currentInputs_JAVA;
    jfloatArray currentInput_JAVA;
    jobjectArray currentOutputs_JAVA;
    jfloatArray currentOutput_JAVA;
    jfloatArray currentSpikes_JAVA;
    
    NengoGPUData* currentData;

    int i, j, k, l;
    int naIndexInJavaArray, inputIndex, numInputs, inputDimension;

    //store represented output in java array
    int numOutputs, outputDimension, outputIndex, spikeIndex, sharedIndex, ensembleNumNeurons;
    int ensembleIndexInJavaArray;

    startTime = (float) startTime_JAVA;
    endTime = (float) endTime_JAVA;

    for( i = 0; i < numDevices; i++)
    {
        currentData = nengoDataArray[i];

        inputIndex = 0;

        for( j = 0; j < currentData->numNetworkArrays; j++)
        {
            naIndexInJavaArray = intArrayGetElement(currentData->networkArrayIndexInJavaArray,j);
                
            currentInputs_JAVA = (jobjectArray) (*env)->GetObjectArrayElement(env, input, naIndexInJavaArray);
            
            if(currentInputs_JAVA != NULL)
            {
                numInputs = (*env)->GetArrayLength(env, currentInputs_JAVA);

                for(k = 0; k < numInputs; k++)
                {
                    currentInput_JAVA = (jfloatArray)(*env)->GetObjectArrayElement(env, currentInputs_JAVA, k);
                    if(currentInput_JAVA != NULL)
                    {
                        inputDimension = (*env)->GetArrayLength(env, currentInput_JAVA);

                        if(inputIndex    + inputDimension <= currentData->JavaInputSize)
                        {
                            (*env)->GetFloatArrayRegion(env, currentInput_JAVA, 0, inputDimension, sharedInput + currentData->offsetInSharedInput + inputIndex);
                        }
                        else
                        {
                            printf("error: accessing sharedInput out of bounds. size: %d, index: %d\n", sharedInputSize, currentData->offsetInSharedInput + inputIndex + inputDimension);
                            exit(EXIT_FAILURE);
                        }

                        inputIndex += inputDimension;
                    }
                        
                    (*env)->DeleteLocalRef(env, currentInput_JAVA);
                }
            }

            (*env)->DeleteLocalRef(env, currentInputs_JAVA);
        }
    }

    (*env)->DeleteLocalRef(env, input);

    // tell the runner threads to run and then wait for them to finish. The last of them to finish running will wake this thread up. 
    pthread_mutex_lock(mutex);
    myCVsignal = 1;
    pthread_cond_broadcast(cv_GPUThreads);
    pthread_cond_wait(cv_JNI, mutex);
    pthread_mutex_unlock(mutex);

    //store represented output in java array
    for(i = 0; i < numDevices; i++)
    {
        currentData = nengoDataArray[i];

        outputIndex = 0;

        for(k = 0; k < currentData->numNetworkArrays; k++)
        {
            naIndexInJavaArray = intArrayGetElement(currentData->networkArrayIndexInJavaArray,k);

            currentOutputs_JAVA = (jobjectArray)(*env)->GetObjectArrayElement(env, output, naIndexInJavaArray);
            numOutputs = (*env)->GetArrayLength(env, currentOutputs_JAVA); 

            for(l = 0; l < numOutputs; l++)
            {
                currentOutput_JAVA = (jfloatArray) (*env)->GetObjectArrayElement(env, currentOutputs_JAVA, l);

                if(currentOutput_JAVA != NULL)
                {
                    outputDimension = (*env)->GetArrayLength(env, currentOutput_JAVA);

                    if(outputIndex + outputDimension <= currentData->JavaOutputSize)
                    {
                         (*env)->SetFloatArrayRegion(env, currentOutput_JAVA, 0, outputDimension, currentData->outputHost->array + outputIndex);
                    }
                    else
                    {
                        printf("error: accessing outputHost for java out of bounds. size: %d, index: %d, dim: %d, device: %d, na: %d out of %d\n", currentData->JavaOutputSize, outputIndex, outputDimension, currentData->device, k, currentData->numNetworkArrays);
                        exit(EXIT_FAILURE);
                    }

                    outputIndex += outputDimension;
                    (*env)->DeleteLocalRef(env, currentOutput_JAVA);
                }
            }
            
            (*env)->DeleteLocalRef(env, currentOutputs_JAVA);
        }

        spikeIndex = 0;

        for(k = 0; k < currentData->numEnsembles; k++)
        {
            ensembleIndexInJavaArray = intArrayGetElement(currentData->ensembleIndexInJavaArray,k);

            currentSpikes_JAVA = (*env)->GetObjectArrayElement(env, spikes, ensembleIndexInJavaArray);

            if(currentSpikes_JAVA != NULL)
            {
                ensembleNumNeurons = (int) (*env)->GetArrayLength(env, currentSpikes_JAVA);

                (*env)->SetFloatArrayRegion(env, currentSpikes_JAVA, 0, ensembleNumNeurons, currentData->outputHost->array + currentData->CPUOutputSize + spikeIndex);

                spikeIndex += ensembleNumNeurons;

                (*env)->DeleteLocalRef(env, currentSpikes_JAVA);
            }
        }

        // write from the output array of the current device to the shared data array
        for( k = 0; k < currentData->interGPUOutputSize; k++)
        {
            outputIndex = intArrayGetElement(currentData->sharedData_outputIndex, k);
            sharedIndex = intArrayGetElement(currentData->sharedData_sharedIndex, k);

            sharedInput[sharedIndex] = floatArrayGetElement(currentData->outputHost, outputIndex);
        }
    }


    (*env)->DeleteLocalRef(env, output);
    (*env)->DeleteLocalRef(env, spikes);
}

JNIEXPORT void JNICALL Java_ca_nengo_util_impl_NEFGPUInterface_nativeKill
(JNIEnv *env, jclass class)
{
    printf("NengoGPU: KILL\n");
    run_kill();
}

#ifdef __cplusplus
}
#endif
