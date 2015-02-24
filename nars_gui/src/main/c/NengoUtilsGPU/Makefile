CUDA_INC_PATH=/usr/local/cuda/include
CUDA_LIB_PATH=/usr/local/cuda/lib64

CULA_INC_PATH=/usr/local/cula/include
CULA_LIB_PATH=/usr/local/cula/lib64

JAVA_INC=/usr/lib/jvm/java-6-sun/include
JAVA_INC_LINUX=/usr/lib/jvm/java-6-sun/include/linux

CUDA_INCLUDES=-I${CULA_INC_PATH} -I${CUDA_INC_PATH}
JAVA_INCLUDES=-I${JAVA_INC} -I${JAVA_INC_LINUX}
LIBPATH=-L${CUDA_LIB_PATH} -L${CULA_LIB_PATH}

LIBS=-lcula_core -lcula_lapack -lcublas -lcudart -lm -lc

GCC_PATH=/usr/bin/gcc-4.4

CC=gcc-4.4
CUDA_CC=nvcc
CFLAGS=-fPIC -c -m64 -Wall -I. ${JAVA_INCLUDES} ${CUDA_INCLUDES}
CUDAFLAGS= -ccbin ${GCC_PATH} --ptxas-options=--verbose --compiler-options '-fPIC' -c 

OBJ=customCudaUtils.o weightedCostApproximatorGPU.o NengoUtilsGPU_JNI.o

all: CFLAGS+= -O3
all: libNengoUtilsGPU.so

debug: CFLAGS+= -g -O0
debug: libNengoUtilsGPU.so

libNengoUtilsGPU.so: ${OBJ} 
	${CC} -shared -Wl,-soname,libNengoUtilsGPU.so -o libNengoUtilsGPU.so.1.0 ${OBJ} ${LIBPATH} ${LIBS}

customCudaUtils.o : customCudaUtils.h
NengoUtilsGPU_JNI.o : NengoUtilsGPU.h weightedCostApproximator_JNI.h

weightedCostApproximatorGPU.o : weightedCostApproximatorGPU.cu NengoUtilsGPU.h
	${CUDA_CC} ${CUDAFLAGS} -I${CULA_INC_PATH} -o weightedCostApproximatorGPU.o  weightedCostApproximatorGPU.cu 

clean:
	rm -rf libNengoUtilsGPU.so.1.0 *.o
