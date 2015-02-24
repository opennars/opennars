 
#ifdef __cplusplus
extern "C"
{
#endif

#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <cuda_runtime.h>

#include "weightedCostApproximator_JNI.h"
#include "customCudaUtils.h"
#include "NengoUtilsGPU.h"

JNIEXPORT jboolean JNICALL Java_ca_nengo_math_impl_WeightedCostApproximator_hasGPU
  (JNIEnv *env, jclass class)
{
  jboolean hasGPU = (jboolean) (getGPUDeviceCount() > 0);

  return hasGPU;
}

/* 
  Must be called from Java code.
  Takes a matrix as input and returns its pseudoInverse. Used to pseudo invert gamma.
*/
JNIEXPORT jobjectArray JNICALL Java_ca_nengo_math_impl_WeightedCostApproximator_nativePseudoInverse
  (JNIEnv* env, jclass class, jobjectArray java_matrix, jfloat minSV, jint numSV)
{
    int i = 0;

    jsize M = (*env)->GetArrayLength(env, java_matrix);
    jfloatArray temp_array = (jfloatArray) (*env)->GetObjectArrayElement(env, java_matrix, 0);
    jsize N = (*env)->GetArrayLength(env, temp_array);

    float* A = NULL;
    float* A_row_major = NULL;
    A_row_major = (float*)malloc(N*M*sizeof(float));
    A = (float*)malloc(N*M*sizeof(float));


	if(!A || !A_row_major)
        exit(EXIT_FAILURE);

    // move the data in to a C array
    for(;i < M; i++)
    {
       temp_array = (jfloatArray)(*env)->GetObjectArrayElement(env, java_matrix, i);
       (*env)->GetFloatArrayRegion(env, temp_array, 0, N, (A_row_major + N*i));
    }

    switchFloatArrayStorage(A_row_major, A, N, M);

    A = pseudoInverse(A, (int)M, (int)N, (float)minSV, (int)numSV, 0, 0);

    switchFloatArrayStorage(A, A_row_major, N, M);

    java_matrix = (jobjectArray) (*env)->NewObjectArray(env, N, (*env)->GetObjectClass(env, temp_array), 0);

    for(i=0;i < N; i++)
    {
       temp_array = (jfloatArray) (*env)->NewFloatArray(env, M);
       (*env)->SetFloatArrayRegion(env, temp_array, 0, M, (A_row_major + i * M));
       (*env)->SetObjectArrayElement(env, java_matrix, i, temp_array);
    }

    free(A);
    free(A_row_major);
    
    return java_matrix;
}

/* 
  Must be called from Java code.
  Takes A as input and returns gamma = A * A_transpose 
*/
JNIEXPORT jobjectArray JNICALL Java_ca_nengo_math_impl_WeightedCostApproximator_nativeFindGamma
  (JNIEnv *env, jclass class, jobjectArray noisy_values_JAVA)
{
  int numNeurons = (int) (*env)->GetArrayLength(env, noisy_values_JAVA);
  jfloatArray temp_array = (jfloatArray) (*env)->GetObjectArrayElement(env, noisy_values_JAVA, 0);
  int numEvalPoints = (int) (*env)->GetArrayLength(env, temp_array);

  int i;

  // The java array noisy_values_JAVA is just A, not A_transpose, but when we move it to a C array, 
  // we get it in row major format the CUDA routines expect column major format, so we really have A_transpose
  float* A_transpose = (float*)malloc(numNeurons * numEvalPoints * sizeof(float)), *gamma;
  jobjectArray gamma_JAVA;

  if(!A_transpose)
    exit(EXIT_FAILURE);

  // move the data in to a C array
  for(i=0 ;i < numNeurons; i++)
  {
     temp_array = (jfloatArray)(*env)->GetObjectArrayElement(env, noisy_values_JAVA, i);
     (*env)->GetFloatArrayRegion(env, temp_array, 0, numEvalPoints, (A_transpose + numEvalPoints*i));
  }

  gamma = findGamma(A_transpose, numNeurons, numEvalPoints, 0, 0);

  // gamma is stored in lower triangular format since its symmetric
  gamma_JAVA = (jobjectArray) (*env)->NewObjectArray(env, numNeurons, (*env)->GetObjectClass(env, temp_array), 0);
  for(i=0;i < numNeurons; i++)
  {
     temp_array = (jfloatArray) (*env)->NewFloatArray(env, numNeurons);
     (*env)->SetFloatArrayRegion(env, temp_array, 0, numNeurons, (gamma + i * numNeurons));
     (*env)->SetObjectArrayElement(env, gamma_JAVA, i, temp_array);
  }

  free(gamma);

  return gamma_JAVA;
}

/* 
  Must be called from Java code.
  Takes A as an input value, finds gamma = (A * A_tranpose) and then finds pseudoInverse(gamma).
*/
JNIEXPORT jobjectArray JNICALL Java_ca_nengo_math_impl_WeightedCostApproximator_nativeFindGammaPseudoInverse
  (JNIEnv *env, jclass class, jobjectArray noisy_values_JAVA, jfloat minSV, jint numSV)
{
  int numNeurons = (int) (*env)->GetArrayLength(env, noisy_values_JAVA);
  jfloatArray temp_array = (jfloatArray) (*env)->GetObjectArrayElement(env, noisy_values_JAVA, 0);
  int numEvalPoints = (int) (*env)->GetArrayLength(env, temp_array);

  int i;

  float* A_transpose, *gamma, *gamma_inv;
  jobjectArray result;

  printf("Using GPU to find gamma and its pseudoInverse. %d neurons, %d sample points\n", numNeurons, numEvalPoints);

  // The java array noisy_values_JAVA is just A, not A_transpose, but when we move it to a C array, 
  // we get it in row major format the CUDA routines expect column major format, so we really have A_transpose
  A_transpose = (float*)malloc(numNeurons * numEvalPoints * sizeof(float));

  if(!A_transpose)
    exit(EXIT_FAILURE);

  // move the data in to a C array
  for(i=0 ;i < numNeurons; i++)
  {
     temp_array = (jfloatArray)(*env)->GetObjectArrayElement(env, noisy_values_JAVA, i);
     (*env)->GetFloatArrayRegion(env, temp_array, 0, numEvalPoints, (A_transpose + numEvalPoints*i));
  }

  gamma = findGamma(A_transpose, numNeurons, numEvalPoints, 0, 1);
  gamma_inv = pseudoInverse(gamma, numNeurons, numNeurons, (float)minSV, (int)numSV, 1, 0);

  result = (jobjectArray) (*env)->NewObjectArray(env, numNeurons, (*env)->GetObjectClass(env, temp_array), 0);

  // move the result in to a Java array
  for(i=0;i < numNeurons; i++)
  {
     temp_array = (jfloatArray) (*env)->NewFloatArray(env, numNeurons);
     (*env)->SetFloatArrayRegion(env, temp_array, 0, numNeurons, (gamma_inv + i * numNeurons));
     (*env)->SetObjectArrayElement(env, result, i, temp_array);
  }

  free(gamma_inv);
  
  return result;
}



#ifdef __cplusplus
}
#endif

