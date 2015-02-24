#ifdef __cplusplus
extern "C"
{
#endif

#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <cula_lapack_device.h>
#include <cuda_runtime.h>
#include <cublas.h>

#include "customCudaUtils.h"
#include "NengoUtilsGPU.h"

/*
  Inverts a square diagonal matrix in place. Launch n kernels where the 
  n is the length of one side of the matrix to be inverted.
*/
__global__ void invertS(float* Sd, int minDim, int leadingDim, float minSV, int numSV)
{
  int i = threadIdx.x + (blockDim.x * threadIdx.y) + (blockIdx.x + (gridDim.x * blockIdx.y)) * blockDim.x * blockDim.y;
  float valueInS;
  if(i < minDim)
  { 
     valueInS = Sd[i];
     Sd[i] = 0;

     if(valueInS < minSV || (numSV != -1 && i > numSV))
     {
          Sd[leadingDim*i + i] = 0;
     }
     else if(valueInS != 0)
     {
          Sd[leadingDim*i + i] = 1/valueInS;
     }
  }

  return;
}

/*
  Takes a square symmetrix matrix in lower triangular form (only the values below the main diagonal
  are present, the rest are probably NANs) and expands it to normal storage form.
  Launch n^2 kernels where n is the length of one side of the matrix. 
*/
__global__ void undoLowerTriangularStorage(float* Sd, unsigned int numElements, int stride)
{
  unsigned int index = threadIdx.x + (blockDim.x * threadIdx.y) + (blockIdx.x + (gridDim.x * blockIdx.y)) * blockDim.x * blockDim.y;

  int i = index / stride;
  int j = index % stride;

  if(i < j)
  {
    Sd[j + stride * i] = Sd[i + stride * j];
  }
}

/*
Takes in a matrix with dimension M x N stored in column major format and returns its pseudoInverse
*/
float* pseudoInverse(float* A, int M, int N, float minSV, int numSV, int inputOnDevice, int outputOnDevice)
{
    char jobu = 'S';
    char jobv = 'S';

    int minDim = min(M,N);
    int maxDim = max(M,N);
    
    float* Ad = NULL;
    float* Sd = NULL;
    float* VTd = NULL;
    float* Ud = NULL;

    cudaError_t err;
    culaStatus status;

    if(inputOnDevice)
    {
      Ad = A;
    }else{
      err = cudaMalloc((void**)&Ad, M*N*sizeof(float));
      checkCudaError(err);

      err = cudaMemcpy(Ad, A, M*N*sizeof(float), cudaMemcpyHostToDevice);
      checkCudaError(err);

      free(A);
    }

    err = cudaMalloc((void**)&Sd, M*N*sizeof(float));
    checkCudaError(err);
    err = cudaMemset((void*)Sd, 0, M*N*sizeof(float));
    checkCudaError(err);

    err = cudaMalloc((void**)&Ud, minDim*M*sizeof(float));
    checkCudaError(err);

    err = cudaMalloc((void**)&VTd, minDim*N*sizeof(float));
    checkCudaError(err);

    status = culaInitialize();
    checkStatus(status);

    status = culaDeviceSgesvd(jobu, jobv, M, N, Ad, M, Sd, Ud, M, VTd, minDim);  
    checkStatus(status);
   
   
    // we need at least minDim blocks...its ok to have more, but they will not be used
    dim3 dimBlock(16, 16);
    dim3 dimGrid(1, minDim / (dimBlock.x * dimBlock.y) + 1);
    // Now U should be in Ad, S in Sd and VT in VTd
    invertS<<<dimGrid,dimBlock>>>(Sd, minDim, minDim, minSV, numSV);
    checkCudaError(err);


    cublasInit();
   
    // compute S^-1 * UT and store in Ad
    cublasSgemm('N', 'T', minDim, M, minDim, 1, Sd, minDim, Ud, M, 0, Ad, minDim);

    // compute V * S^-1 * UT and store in Sd
    cublasSgemm('T', 'N', N, M, minDim, 1, VTd, minDim, Ad, minDim, 0, Sd, N);
    
    //Shut everything down
    cublasShutdown();

    err = cudaFree(Ad);
    checkCudaError(err);

    err = cudaFree(VTd);
    checkCudaError(err);

    err = cudaFree(Ud);
    checkCudaError(err);

    culaFreeBuffers();

    if(outputOnDevice)
    {
      return Sd;
    }else{
      float* S = (float*) malloc(M*N*sizeof(float));

      err = cudaMemcpy(S, Sd, M*N*sizeof(float), cudaMemcpyDeviceToHost);
      checkCudaError(err);

      cudaFree(Sd);

      return S;
    }
}



/*
Takes in A_transpose stored in column major format and returns A * A_transpose
*/
float* findGamma(float* A_transpose, int numNeurons, int numEvalPoints, int inputOnDevice, int outputOnDevice)
{
    cudaError_t err;

    char uplo = 'U';
    char trans = 'T';

    float alpha = 1.0f / ((float) numEvalPoints);

    float* A_transpose_d;

    if(inputOnDevice)
    {
      A_transpose_d = A_transpose;
    }else{
      err = cudaMalloc((void**)&A_transpose_d, numNeurons*numEvalPoints*sizeof(float));
      checkCudaError(err);

      err = cudaMemcpy(A_transpose_d, A_transpose, numNeurons*numEvalPoints*sizeof(float), cudaMemcpyHostToDevice);
      checkCudaError(err);

      free(A_transpose);
    }

    float* gamma_d = NULL;
    err = cudaMalloc((void**)&gamma_d, numNeurons*numNeurons*sizeof(float));
    checkCudaError(err);

    cublasInit();

    // A times A transpose stored in matrix gamma_d
    cublasSsyrk(uplo, trans, numNeurons, numEvalPoints, alpha, A_transpose_d, numEvalPoints, 0, gamma_d, numNeurons);

    cudaFree(A_transpose_d);

    dim3 dimBlock(16, 16);
    dim3 dimGrid(numNeurons / dimBlock.x + 1, numNeurons / dimBlock.y + 1);

    undoLowerTriangularStorage<<<dimGrid, dimBlock>>>(gamma_d, numNeurons * numNeurons, numNeurons);
    err = cudaGetLastError();
    checkCudaError(err);

    if(outputOnDevice)
    {
      return gamma_d;
    }else{
      float* gamma = (float*) malloc(numNeurons * numNeurons * sizeof(float));

      err = cudaMemcpy(gamma, gamma_d, numNeurons*numNeurons*sizeof(float), cudaMemcpyDeviceToHost);
      checkCudaError(err);
      
      err = cudaFree(gamma_d);
      checkCudaError(err);

      return gamma;
    }
}

#ifdef __cplusplus
}
#endif
