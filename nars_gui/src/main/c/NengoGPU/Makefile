CUDA_INC_PATH=/usr/local/cuda/include
CUDA_LIB_PATH=/usr/local/cuda/lib64
JAVA_INC=/usr/lib/jvm/java-6-sun/include
JAVA_INC_LINUX=/usr/lib/jvm/java-6-sun/include/linux

CUDA_INCLUDES= -I${CUDA_INC_PATH} 
#-I${HOME}/NVIDIA_GPU_Computing_SDK/C/common/inc
JAVA_INCLUDES=-I${JAVA_INC} -I${JAVA_INC_LINUX}
LIBPATH=-L${CUDA_LIB_PATH}

LIBS=-lcudart -lm -lc

GCC_PATH=/usr/bin/gcc-4.4

CC=gcc-4.4
CUDA_CC=nvcc
CFLAGS=-fPIC -c -m64 -Wall -I. ${CUDA_INCLUDES} ${JAVA_INCLUDES}
CUDAFLAGS= -ccbin ${GCC_PATH} --ptxas-options=--verbose --compiler-options '-fPIC' -c

#the order of the object files here is important, get unsatisfied link error if it gets mixed up
OBJ=NengoGPU_JNI.o NengoGPU.o NengoGPU_CUDA.o NengoGPUData.o

all: CFLAGS+= -O3
all: libNengoGPU.so 

debug: CFLAGS+= -g -O0
debug: CUDAFLAGS+= -g -G
debug: SO_DEBUG=-g
debug: libNengoGPU.so

libNengoGPU.so: ${OBJ} 
	${CC} ${SO_DEBUG} -shared -Wl,-soname,libNengoGPU.so -o libNengoGPU.so.1.0 ${OBJ} ${LIBPATH} ${LIBS}

NengoGPUData.o: NengoGPUData.h
NengoGPU.o : NengoGPU.h NengoGPU_CUDA.h
NengoGPU_JNI.o : NengoGPU.h NEFGPUInterface_JNI.h NengoGPU_CUDA.h

NengoGPU_CUDA.o: NengoGPU_CUDA.cu NengoGPU_CUDA.h NengoGPU.h
	${CUDA_CC} ${CUDAFLAGS} -o NengoGPU_CUDA.o NengoGPU_CUDA.cu 

clean:
	rm -rf libNengoGPU.so.1.0 *.o
