#pragma OPENCL EXTENSION cl_khr_fp64: enable

#define ARRAY2d(array, width, x, y) (array[(x) + (y)*(width)])

#define precisionType float

