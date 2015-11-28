package automenta.spacegraph.physics.light;

import automenta.spacegraph.video.ShaderControl;
import com.jogamp.opengl.GL2;
import org.jbox2d.common.IViewportTransform;
import org.jbox2d.common.Vec2;

import java.util.ArrayList;

import static com.jogamp.opengl.GL2.*;

public class LightEngine {

	public final float width = 300;
	public final float height = 300;

	public ArrayList<Light> lights = new ArrayList<Light>();
	public ArrayList<Block> blocks = new ArrayList<Block>();

	private int fragmentShader;
	private int shaderProgram;
	private ShaderControl shader;


	public void render(final GL2 gl, IViewportTransform vt) {


		//gl.glClear(gl.GL_COLOR_BUFFER_BIT);
		for (Light light : lights) {
			gl.glColorMask(false, false, false, false);
			gl.glStencilFunc(GL_ALWAYS, 1, 1);
			gl.glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);

			for (Block block : blocks) {
				Vec2[] vertices = block.getVertices();
				for (int i = 0; i < vertices.length; i++) {
					Vec2 currentVertex = vertices[i];
					Vec2 nextVertex = vertices[(i + 1) % vertices.length];
					Vec2 edge = nextVertex.sub(currentVertex);
					Vec2 normal = new Vec2(edge.y, -edge.x);
					Vec2 lightToCurrent = currentVertex.sub(light.location);
					if (Vec2.dot(normal, lightToCurrent) > 0) {
						Vec2 point1 = currentVertex.add((Vec2) currentVertex.sub(light.location).mulLocal(800));
						Vec2 point2 = nextVertex.add((Vec2) nextVertex.sub(light.location).mulLocal(800));
						gl.glBegin(GL_QUADS); {
							gl.glVertex2f(currentVertex.x, currentVertex.y);
							gl.glVertex2f(point1.x, point1.y);
							gl.glVertex2f(point2.x, point2.y);
							gl.glVertex2f(nextVertex.x, nextVertex.y);
						} gl.glEnd();
					}
				}
			}

			float cx, cy;
			float scale = vt.getExtents().x;

			cx = -vt.getCenter().x * scale;
			cy = -vt.getCenter().y * scale;


			gl.glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
			gl.glStencilFunc(GL_EQUAL, 0, 1);
			gl.glColorMask(true, true, true, true);

			gl.glUseProgram(shaderProgram);
			gl.glUniform1f(gl.glGetUniformLocation(shaderProgram, "scale"), 1);
			gl.glUniform2f(gl.glGetUniformLocation(shaderProgram, "lightLocation"), cx+ light.location.x / scale, cy + light.location.y);
			gl.glUniform3f(gl.glGetUniformLocation(shaderProgram, "lightColor"), light.red, light.green, light.blue);
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL_ONE, GL_ONE);
			//gl.glBlendFunc(GL_ONE, GL_DST_ALPHA);

			gl.glBegin(GL_QUADS); {
				gl.glVertex2f(-width/2, -height/2);
				gl.glVertex2f(-width/2, height/2);
				gl.glVertex2f(width/2, height/2);
				gl.glVertex2f(width/2, -height/2);
			}gl.glEnd();

			gl.glDisable(GL_BLEND);
			gl.glUseProgram(0);
			gl.glClear(GL_STENCIL_BUFFER_BIT);
		}
		gl.glColor3f(0, 0, 0);
		for (Block block : blocks) {
			gl.glBegin(GL_QUADS); {
				for (Vec2 vertex : block.getVertices()) {
					gl.glVertex2f(vertex.x, vertex.y);
				}
			} gl.glEnd();
		}

	}

	public void random() {
		int lightCount = 4 + (int) (Math.random() * 1);
		int blockCount = 0; //2 + (int) (Math.random() * 1);

		for (int i = 1; i <= lightCount; i++) {
			Vec2 location = new Vec2((float) Math.random() * width, (float) Math.random() * height);
			lights.add(new Light(location, (float) Math.random() * 10, (float) Math.random() * 10, (float) Math.random() * 10));
		}

		for (int i = 1; i <= blockCount; i++) {
			int width = 50;
			int height = 50;
			int x = (int) (Math.random() * (this.width - width));
			int y = (int) (Math.random() * (this.height - height));
			blocks.add(new Block(x, y, width, height));
		}
	}

	public void init(GL2 gl) {

		String fs = "uniform vec2 lightLocation;\n" +
				"uniform vec3 lightColor;\n" +
				"uniform float screenHeight;\n" +
				"\n" +
				"void main() {\n" +
				"\tfloat distance = length(lightLocation - gl_FragCoord.xy);\n" +
				"\tfloat attenuation = 1.0 / distance;\n" +
				"\tvec4 color = vec4(attenuation, attenuation, attenuation, pow(attenuation, 3)) * vec4(lightColor, 1);\n" +
				"\n" +
				"\tgl_FragColor = color;\n" +
				"}";
		shader = new ShaderControl(fs);
		shader.init(gl);

		this.shaderProgram = shader.shaderprogram;
		this.fragmentShader = shader.fragmentShaderProgram;

		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(0, width, height, 0, 1, -1);
		gl.glMatrixMode(GL_MODELVIEW);

		gl.glEnable(GL_STENCIL_TEST);
		gl.glClearColor(0, 0, 0, 0);
	}

	public void delete(GL2 gl) {
		gl.glDeleteShader(fragmentShader);
		gl.glDeleteProgram(shaderProgram);
	}

}
