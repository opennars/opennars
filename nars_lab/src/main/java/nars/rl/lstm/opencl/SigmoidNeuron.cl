__kernel void kernel_activate(__global const float* values, __global float* out, int n) {
    int i = get_global_id(0);
    if (i >= n)
        return;

    out[i] = activate(values[i]);
}

__kernel void kernel_derivative(__global const float* values, __global float* out, int n) {
    int i = get_global_id(0);
    if (i >= n)
        return;

    out[i] = derivative(values[i]);
}

float derivative(float x) {
    float act = activate(x);
	return act * (1.0 - act);
}

float activate(float x) {
    return 1.0f / (1.0f + expf(-x);
}
