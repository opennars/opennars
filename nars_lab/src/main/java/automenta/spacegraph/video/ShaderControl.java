package automenta.spacegraph.video;

/**
 * Created by me on 5/14/15.
 */

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.glsl.ShaderProgram;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

// The shader control class.
// loads and starts/stops shaders.
//http://www.guyford.co.uk/showpage.php?id=50&page=How_to_setup_and_load_GLSL_Shaders_in_JOGL
public class ShaderControl extends ShaderProgram {
    public int vertexShaderProgram;
    public int fragmentShaderProgram;
    public  int shaderprogram;
    public String[] vsrc;
    public String[] fsrc;

    public ShaderControl(String vertexShader, String fragmentShader) {
        this.vsrc = new String[] { vertexShader };
        this.fsrc = new String[] { fragmentShader };
    }

    public ShaderControl(String fragmentShader) {
        this.vsrc = null;
        this.fsrc = new String[] { fragmentShader };
    }

    // this will attach the shaders
    public void init( GL2 gl )
    {
        try
        {
            attachShaders(gl);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // loads the shaders
    // in this example we assume that the shader is a file located in the applications JAR file.
    //
    public String[] loadShader( String name )
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            InputStream is = getClass().getResourceAsStream(name);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
                sb.append('\n');
            }
            is.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("Shader is " + sb.toString());
        return new String[]
                { sb.toString() };
    }

    // This compiles and loads the shader to the video card.
    // if there is a problem with the source the program will exit at this point.
    //
    private void attachShaders( GL2 gl ) throws Exception
    {
        shaderprogram = gl.glCreateProgram();

        if (vsrc!=null) {
            vertexShaderProgram = gl.glCreateShader(GL2.GL_VERTEX_SHADER);
            gl.glShaderSource(vertexShaderProgram, 1, vsrc, null, 0);
            gl.glCompileShader(vertexShaderProgram);
            gl.glAttachShader(shaderprogram, vertexShaderProgram);
        }

        if (fsrc!=null) {
            fragmentShaderProgram = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);
            gl.glShaderSource(fragmentShaderProgram, 1, fsrc, null, 0);
            gl.glCompileShader(fragmentShaderProgram);
            gl.glAttachShader(shaderprogram, fragmentShaderProgram);
        }
        //
        gl.glLinkProgram(shaderprogram);
        gl.glValidateProgram(shaderprogram);
        IntBuffer intBuffer = IntBuffer.allocate(1);
        gl.glGetProgramiv(shaderprogram, GL2.GL_LINK_STATUS, intBuffer);

        if (intBuffer.get(0) != 1)
        {
            gl.glGetProgramiv(shaderprogram, GL2.GL_INFO_LOG_LENGTH, intBuffer);
            int size = intBuffer.get(0);
            System.err.println("Program link error: ");
            if (size > 0)
            {
                ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                gl.glGetProgramInfoLog(shaderprogram, size, intBuffer, byteBuffer);
                for (byte b : byteBuffer.array())
                {
                    System.err.print((char) b);
                }
            }
            else
            {
                System.out.println("Unknown");
            }
            System.exit(1);
        }
    }

    // this function is called when you want to activate the shader.
    // Once activated, it will be applied to anything that you draw from here on
    // until you call the dontUseShader(GL) function.
    public int useShader( GL2 gl )
    {
        gl.glUseProgram(shaderprogram);
        return shaderprogram;
    }

    // when you have finished drawing everything that you want using the shaders,
    // call this to stop further shader interactions.
    public void dontUseShader( GL2 gl )
    {
        gl.glUseProgram(0);
    }
}
