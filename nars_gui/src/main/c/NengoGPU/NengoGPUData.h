#ifndef NENGO_GPU_DATA_H
#define NENGO_GPU_DATA_H

#ifdef __cplusplus
extern "C"{
#endif

#define NEURONS_PER_GPU 100000


/*
 Used to extract ensemble data out of arrays passed in through JNI.
*/
enum EnsembleDataIndex_enum
{
  NENGO_ENSEMBLE_DATA_DIMENSION = 0,
  NENGO_ENSEMBLE_DATA_NUM_NEURONS,
  NENGO_ENSEMBLE_DATA_NUM_ORIGINS,

  NENGO_ENSEMBLE_DATA_TOTAL_INPUT_SIZE,
  NENGO_ENSEMBLE_DATA_TOTAL_OUTPUT_SIZE,
  
  NENGO_ENSEMBLE_DATA_MAX_TRANSFORM_DIMENSION,
  NENGO_ENSEMBLE_DATA_MAX_DECODER_DIMENSION,

  NENGO_ENSEMBLE_DATA_NUM_DECODED_TERMINATIONS,
  NENGO_ENSEMBLE_DATA_NUM_NON_DECODED_TERMINATIONS,

  NENGO_ENSEMBLE_DATA_MAX_ND_TRANSFORM_SIZE,
  
  NENGO_ENSEMBLE_DATA_NUM
};

typedef enum EnsembleDataIndex_enum EnsembleDataIndex;

enum NetworkArrayDataIndex_enum
{
  NENGO_NA_DATA_FIRST_INDEX = 0,
  NENGO_NA_DATA_END_INDEX,
  NENGO_NA_DATA_NUM_TERMINATIONS,
  NENGO_NA_DATA_TOTAL_INPUT_SIZE,
  NENGO_NA_DATA_NUM_ORIGINS,
  NENGO_NA_DATA_TOTAL_OUTPUT_SIZE,
  NENGO_NA_DATA_NUM_NEURONS,

  NENGO_NA_DATA_NUM
};

typedef enum NetworkArrayDataIndex_enum NetworkArrayDataIndex;


/*
  Float and int array objects that facilitate safe array accessing.
*/

struct intArray_t{
  int* array;
  int size;
  char* name;
  int onDevice;
};
typedef struct intArray_t intArray;


struct floatArray_t{
  float* array;
  int size;
  char* name;
  int onDevice;
};
typedef struct floatArray_t floatArray;

intArray* newIntArray(int size, const char* name);
void freeIntArray(intArray* a);
intArray* newIntArrayOnDevice(int size, const char* name);

floatArray* newFloatArray(int size, const char* name);
void freeFloatArray(floatArray* a);
floatArray* newFloatArrayOnDevice(int size, const char* name);

void checkBounds(char* verb, char* name, int size, int index);
void checkLocation(char* verb, char* name, int onDevice, int size, int index);

void intArraySetElement(intArray* a, int index, int value);
void floatArraySetElement(floatArray* a, int index, float value);
int intArrayGetElement(intArray* a, int index);
float floatArrayGetElement(floatArray* a, int index);

void intArraySetData(intArray* a, int* data, int dataSize);
void floatArraySetData(floatArray* a, float* data, int dataSize);

/*
  Stores a projection.
*/ 
#define PROJECTION_DATA_SIZE 6
struct projection_t{
  int sourceNode;
  int sourceOrigin;
  int destinationNode;
  int destinationTermination;
  int size;
  int destDevice;
  int sourceDevice;

  int offsetInSource;
  int offsetInDestination;
};

typedef struct projection_t projection;

void storeProjection(projection* proj, int* data);
void printProjection(projection* proj);


typedef struct int_list_t int_list;
typedef struct int_queue_t int_queue;

// implementation of a list
struct int_list_t{
  int first;
  int_list* next;
};

int_list* cons_int_list(int_list* list, int item);
int* first_int_list(int_list* list);
int_list* next_int_list(int_list* list);
void free_int_list(int_list* list);

// implementation of a queue
struct int_queue_t{
  int size;
  int_list* head;
  int_list* tail;
};

int_queue* new_int_queue();
int pop_int_queue(int_queue* queue);
void add_int_queue(int_queue* queue, int val);
void free_int_queue(int_queue* queue);



/* 
  The central data structure. One is created for each device in use. Holds nengo network data
  in arrays structured to be used by cuda kernels in NengoGPU_CUDA.cu.
*/
struct NengoGPUData_t{

  FILE *fp;
  int onDevice;
  int initialized;
  int device;
  float maxTimeStep;
   
  int numNeurons;
  int numEnsembles;
  int numNetworkArrays;
  int numInputs;
  int numTerminations;
  int numNDterminations;
  int numDecodedTerminations;
  int numNetworkArrayOrigins;
  int numOrigins;

  int totalInputSize;
  int GPUInputSize;
  int CPUInputSize;
  int JavaInputSize;
  int offsetInSharedInput;
  int numSpikesToSendBack;
  int numSpikeEnsembles;

  int totalTransformSize;
  int totalNumTransformRows;
  int totalNonDecodedTransformSize;
  int totalEnsembleDimension;
  int totalEncoderSize;
  int totalDecoderSize;
  int totalOutputSize;

  int maxDecodedTerminationDimension;
  int maxNumDecodedTerminations;
  int maxDimension;
  int maxNumNeurons;
  int maxOriginDimension;
  int maxEnsembleNDTransformSize;

  // amount of output that stays on the GPU
  int GPUOutputSize;

  // amount of output that has to go back to the java side
  int JavaOutputSize;

  // amount of output from this GPU that has to go to another GPU.
  int interGPUOutputSize;

  // totalOutputSize - GPUOutputSize
  int CPUOutputSize;

  // network-array-specific arrays
  intArray* networkArrayIndexInJavaArray;
  intArray* ensembleIndexInJavaArray;

  floatArray* input;
  intArray* terminationOffsetInInput;

  floatArray* terminationTransforms;
  intArray* transformRowToInputIndexor;
  floatArray* terminationTau;
  intArray* inputDimension;
  floatArray* terminationOutput;
  intArray* terminationOutputIndexor;
  intArray* ensembleNumTerminations;

  floatArray* encoders;
  intArray* ensembleOrderInEncoders;
  floatArray* encodeResult;
  floatArray* ensembleSums;

  floatArray* decoders;
  intArray* ensembleOrderInDecoders;

  // data for calculating spikes
  floatArray* neuronVoltage;
  floatArray* neuronReftime;
  floatArray* neuronBias;
  floatArray* neuronScale;
  floatArray* ensembleTauRC;
  floatArray* ensembleTauRef;
  intArray* neuronToEnsembleIndexor;

  intArray* isSpikingEnsemble;

  // supplementary arrays for doing encoding
  intArray* ensembleDimension;
  intArray* ensembleOffsetInDimensions;
  intArray* encoderRowToEnsembleIndexor; 
  intArray* encoderStride;
  intArray* encoderRowToNeuronIndexor;

  // supplementary arrays for doing decoding
  intArray* ensembleNumNeurons;
  intArray* ensembleOffsetInNeurons; 
  intArray* decoderRowToEnsembleIndexor; 
  intArray* decoderStride;
  intArray* decoderRowToOutputIndexor;

  floatArray* spikes;

  floatArray* ensembleOutput;
  floatArray* output;
  floatArray* outputHost;

  intArray* GPUTerminationToOriginMap;
  intArray* spikeMap;

  // non decoded termination data
  intArray* NDterminationInputIndexor;
  floatArray* NDterminationCurrents;
  floatArray* NDterminationWeights;
  intArray* NDterminationEnsembleOffset;
  floatArray* NDterminationEnsembleSums;

  // for organizing the output in terms of ensembles
  intArray* ensembleOriginOffsetInOutput;
  intArray* ensembleNumOrigins;
  intArray* ensembleOriginDimension;

  intArray* ensembleOutputToNetworkArrayOutputMap;

  intArray* networkArrayOriginOffsetInOutput; 
  intArray* networkArrayOriginDimension; 
  intArray* networkArrayNumOrigins;

  intArray* sharedData_outputIndex;
  intArray* sharedData_sharedIndex;
};

typedef struct NengoGPUData_t NengoGPUData;

NengoGPUData* getNewNengoGPUData();
void initializeNengoGPUData(NengoGPUData*);
void checkNengoGPUData(NengoGPUData*);
void moveToDeviceNengoGPUData(NengoGPUData*);
void freeNengoGPUData(NengoGPUData*);
void printNengoGPUData(NengoGPUData* currentData, int printArrays);
void printDynamicNengoGPUData(NengoGPUData* currentData);

void printIntArray(intArray* a, int n, int m);
void printFloatArray(floatArray* a, int n, int m);
void moveToDeviceIntArray(intArray* a);
void moveToDeviceFloatArray(floatArray* a);
void moveToHostFloatArray(floatArray* a);
void moveToHostIntArray(intArray* a);

#ifdef __cplusplus
}
#endif

#endif
