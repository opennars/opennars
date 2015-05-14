uniform vec2 lightLocation;
uniform vec3 lightColor;
uniform float screenHeight;

void main() {
	float distance = length(lightLocation - gl_FragCoord.xy)*scale;
	float attenuation = 0.75 / distance;
	//vec4 color = vec4(attenuation, attenuation, attenuation, 0.25 + attentuation*attentuation) * vec4(lightColor, 1);
	vec4 color = vec4(lightcolor, attenuation)

	gl_FragColor = color;
}