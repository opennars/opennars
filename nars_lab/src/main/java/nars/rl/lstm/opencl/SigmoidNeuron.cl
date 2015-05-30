float activate(float x) {
    return 1.0f / (1.0f + exp(-x));
}

float derivative(float x) {
    float act = activate(x);
	return act * (1.0 - act);
}
