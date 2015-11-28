///*
// * Portions Copyright (C) 2003 Sun Microsystems, Inc.
// * All rights reserved.
// */
//
///*
// *
// * COPYRIGHT NVIDIA CORPORATION 2003. ALL RIGHTS RESERVED.
// * BY ACCESSING OR USING THIS SOFTWARE, YOU AGREE TO:
// *
// *  1) ACKNOWLEDGE NVIDIA'S EXCLUSIVE OWNERSHIP OF ALL RIGHTS
// *     IN AND TO THE SOFTWARE;
// *
// *  2) NOT MAKE OR DISTRIBUTE COPIES OF THE SOFTWARE WITHOUT
// *     INCLUDING THIS NOTICE AND AGREEMENT;
// *
// *  3) ACKNOWLEDGE THAT TO THE MAXIMUM EXTENT PERMITTED BY
// *     APPLICABLE LAW, THIS SOFTWARE IS PROVIDED *AS IS* AND
// *     THAT NVIDIA AND ITS SUPPLIERS DISCLAIM ALL WARRANTIES,
// *     EITHER EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED
// *     TO, IMPLIED WARRANTIES OF MERCHANTABILITY  AND FITNESS
// *     FOR A PARTICULAR PURPOSE.
// *
// * IN NO EVENT SHALL NVIDIA OR ITS SUPPLIERS BE LIABLE FOR ANY
// * SPECIAL, INCIDENTAL, INDIRECT, OR CONSEQUENTIAL DAMAGES
// * WHATSOEVER (INCLUDING, WITHOUT LIMITATION, DAMAGES FOR LOSS
// * OF BUSINESS PROFITS, BUSINESS INTERRUPTION, LOSS OF BUSINESS
// * INFORMATION, OR ANY OTHER PECUNIARY LOSS), INCLUDING ATTORNEYS'
// * FEES, RELATING TO THE USE OF OR INABILITY TO USE THIS SOFTWARE,
// * EVEN IF NVIDIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
// *
// */
//
//package automenta.spacegraph.demo.spacegraph.old;
//
//import automenta.spacegraph.math.linalg.Mat4f;
//import automenta.spacegraph.math.linalg.Rotf;
//import com.jogamp.opengl.*;
//import com.jogamp.opengl.glu.GLU;
//import com.jogamp.opengl.util.texture.TextureData;
//import com.jogamp.opengl.util.texture.TextureIO;
//import org.encog.util.file.FileUtil;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//
//
///**
// * Auxiliary Water simulation class used by ProceduralTexturePhysics
// * main loop. Demonstration by NVidia Corporation.
// *
// * <P>
// *
// * Ported to Java and ARB_fragment_program by Kenneth Russell
// */
//
//public class Water {
//  // Note: this class is organized differently than most of the demos
//  // due to the fact that it is used for two purposes: when the
//  // pbuffer's context is current it is used to update the cellular
//  // automata, and when the parent drawable's context is current it is
//  // used to render the water geometry (with the parent drawable's GL
//  // object).
//
//  private GLU glu = new GLU();
//
//  // Rendering modes
//  public static final int CA_FULLSCREEN_REFLECT   = 0;
//  public static final int CA_FULLSCREEN_FORCE     = 1;
//  public static final int CA_FULLSCREEN_HEIGHT    = 2;
//  public static final int CA_FULLSCREEN_NORMALMAP = 3;
//  public static final int CA_TILED_THREE_WINDOWS  = 4;
//  public static final int CA_DO_NOT_RENDER        = 5;
//
//  private int[] initialMapDimensions = new int[2];
//  private TextureData initialMapData;
//
//  private String tmpSpinFilename;
//  private String tmpDropletFilename;
//  private String tmpCubeMapFilenamePrefix;
//  private String tmpCubeMapFilenameSuffix;
//
//  private GLPbuffer pbuffer;
//  private Rotf cameraOrientation = new Rotf();
//
//  // Dynamic texture names
//  private static final int CA_TEXTURE_FORCE_INTERMEDIATE = 0;
//  private static final int CA_TEXTURE_FORCE_TARGET       = 1;
//  private static final int CA_TEXTURE_VELOCITY_SOURCE    = 2;
//  private static final int CA_TEXTURE_VELOCITY_TARGET    = 3;
//  private static final int CA_TEXTURE_HEIGHT_SOURCE      = 4;
//  private static final int CA_TEXTURE_HEIGHT_TARGET      = 5;
//  private static final int CA_TEXTURE_NORMAL_MAP         = 6;
//  private static final int CA_NUM_DYNAMIC_TEXTURES       = 7;
//
//  // List names
//  private static final int CA_FRAGMENT_PROGRAM_EQ_WEIGHT_COMBINE     = 0;
//  private static final int CA_FRAGMENT_PROGRAM_NEIGHBOR_FORCE_CALC_1 = 1;
//  private static final int CA_FRAGMENT_PROGRAM_NEIGHBOR_FORCE_CALC_2 = 2;
//  private static final int CA_FRAGMENT_PROGRAM_APPLY_FORCE           = 3;
//  private static final int CA_FRAGMENT_PROGRAM_APPLY_VELOCITY        = 4;
//  private static final int CA_FRAGMENT_PROGRAM_CREATE_NORMAL_MAP     = 5;
//  private static final int CA_FRAGMENT_PROGRAM_REFLECT               = 6;
//  private static final int CA_DRAW_SCREEN_QUAD                       = 7;
//  private static final int CA_NUM_LISTS                              = 8;
//
//  // Static textures
//  private Texture initialMapTex;
//  private Texture spinTex;
//  private Texture dropletTex;
//  private Texture cubemap;
//
//  private Texture[] dynamicTextures = new Texture[CA_NUM_DYNAMIC_TEXTURES];
//
//  private int       texHeightInput;                 // current input height texture ID.
//  private int       texHeightOutput;                // current output height texture ID.
//  private int       texVelocityInput;               // current input velocity texture ID.
//  private int       texVelocityOutput;              // current output velocity texture ID.
//  private int       texForceStepOne;                // intermediate force computation result texture ID.
//  private int       texForceOutput;                 // current output force texture ID.
//
//  private int[]     displayListIDs = new int[CA_NUM_LISTS];
//
//  private int       vertexProgramID;                // one vertex program is used to choose the texcoord offset
//
//  private int       flipState;                      // used to flip target texture configurations.
//
//  private boolean   wrap;                           // CA can either wrap its borders, or clamp (clamp by default)
//  private boolean   reset = true;                   // are we resetting this frame? (user hit reset).
//  private boolean   singleStep;                     // animation step on keypress.
//  private boolean   animate = true;                 // continuous animation.
//  private boolean   slow = true;                    // run slow.
//  private boolean   wireframe;                      // render in wireframe mode
//  private boolean   applyInteriorBoundaries = true; // enable / disable "boundary" image drawing.
//  private boolean   spinLogo = true;                // draw spinning logo.
//  private boolean   createNormalMap = true;         // enable / disable normal map creation.
//
//  private float     perTexelWidth;                  // width of a texel (percentage of texture)
//  private float     perTexelHeight;                 // height of a texel
//
//  private float     blurDist = 0.5f;                // distance over which to blur.
//  private boolean   mustUpdateBlurOffsets;          // flag indicating blurDist was set last tick
//
//  private float     normalSTScale = 0.8f;           // scale of normals in normal map.
//  private float     bumpScale = 0.25f;              // scale of bumps in water.
//
//  private float     dropletFrequency = 0.175f;      // frequency at which droplets are drawn in water...
//
//  private int       slowDelay = 1;                  // amount (milliseconds) to delay when running slow.
//  private int       skipInterval;                   // frames to skip simulation.
//  private int       skipCount;                      // frame count for skipping rendering
//
//  private int       angle;                          // angle in degrees for spinning logo
//
//  private List/*<Droplet>*/ droplets = new ArrayList/*<Droplet>*/();             // array of droplets
//
//  private int       renderMode;
//
//  // Constant memory locations
//  private static final int CV_UV_OFFSET_TO_USE =  0;
//
//  private static final int CV_UV_T0_NO_OFFSET  =  1;
//  private static final int CV_UV_T0_TYPE1      =  2;
//  private static final int CV_UV_T0_TYPE2      =  3;
//  private static final int CV_UV_T0_TYPE3      =  4;
//  private static final int CV_UV_T0_TYPE4      =  5;
//
//  private static final int CV_UV_T1_NO_OFFSET  =  6;
//  private static final int CV_UV_T1_TYPE1      =  7;
//  private static final int CV_UV_T1_TYPE2      =  8;
//  private static final int CV_UV_T1_TYPE3      =  9;
//  private static final int CV_UV_T1_TYPE4      = 10;
//
//  private static final int CV_UV_T2_NO_OFFSET  = 11;
//  private static final int CV_UV_T2_TYPE1      = 12;
//  private static final int CV_UV_T2_TYPE2      = 13;
//  private static final int CV_UV_T2_TYPE3      = 14;
//  private static final int CV_UV_T2_TYPE4      = 15;
//
//  private static final int CV_UV_T3_NO_OFFSET  = 16;
//  private static final int CV_UV_T3_TYPE1      = 17;
//  private static final int CV_UV_T3_TYPE2      = 18;
//  private static final int CV_UV_T3_TYPE3      = 19;
//  private static final int CV_UV_T3_TYPE4      = 20;
//
//  private static final int CV_CONSTS_1         = 21;
//
//  public void initialize(String initialMapFilename,
//                         String spinFilename,
//                         String dropletFilename,
//                         String cubeMapFilenamePrefix,
//                         String cubeMapFilenameSuffix,
//                         GLAutoDrawable parentWindow) {
//    loadInitialTexture(initialMapFilename);
//    tmpSpinFilename           = spinFilename;
//    tmpDropletFilename        = dropletFilename;
//    tmpCubeMapFilenamePrefix  = cubeMapFilenamePrefix;
//    tmpCubeMapFilenameSuffix  = cubeMapFilenameSuffix;
//
//    // create the pbuffer.  Will use this as an offscreen rendering buffer.
//    // it allows rendering a texture larger than our window.
//    GLCapabilities caps = parentWindow.getChosenGLCapabilities();
//    caps.setDoubleBuffered(false);
//    if (!GLDrawableFactory.getFactory(caps.getGLProfile()).canCreateGLPbuffer()) {
//      throw new GLException("Pbuffers not supported with this graphics card");
//    }
//    pbuffer = GLDrawableFactory.getFactory(caps.getGLProfile()).createGLPbuffer(caps,
//                                                             null,
//                                                             initialMapDimensions[0],
//                                                             initialMapDimensions[1],
//                                                             parentWindow.getContext());
//    pbuffer.addGLEventListener(new Listener());
//  }
//
//  public void destroy() {
//    if (pbuffer != null) {
//      pbuffer.destroy();
//      pbuffer = null;
//    }
//    reset = true;
//  }
//
//  public void tick() {
//    pbuffer.display();
//  }
//
//  public void draw(GL2 gl, Rotf cameraOrientation) {
//    this.cameraOrientation.set(cameraOrientation);
//
//    if (skipCount >= skipInterval && renderMode != CA_DO_NOT_RENDER) {
//      skipCount = 0;
//      // Display the results of the rendering to texture
//      if (wireframe) {
//        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
//
//        // chances are the texture will be all dark, so lets not use a texture
//        gl.glDisable(GL2.GL_TEXTURE_2D);
//      } else {
//        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
//
//        gl.glActiveTexture(GL2.GL_TEXTURE0);
//        gl.glEnable(GL2.GL_TEXTURE_2D);
//      }
//
//      switch (renderMode) {
//        case CA_FULLSCREEN_REFLECT: {
//          // include bump scale...
//          Mat4f bscale = new Mat4f();
//          bscale.makeIdent();
//          bscale.set(0, 0, bumpScale);
//          bscale.set(1, 1, bumpScale);
//          Mat4f rot = new Mat4f();
//          rot.makeIdent();
//          rot.setRotation(cameraOrientation);
//          Mat4f matRot = rot.mul(bscale);
//
//          gl.glCallList(displayListIDs[CA_FRAGMENT_PROGRAM_REFLECT]);
//
//          // Draw quad over full display
//          gl.glActiveTexture(GL2.GL_TEXTURE0);
//          dynamicTextures[CA_TEXTURE_NORMAL_MAP].bind();
//          dynamicTextures[CA_TEXTURE_NORMAL_MAP].disable();
//          gl.glActiveTexture(GL2.GL_TEXTURE3);
//          cubemap.bind();
//          cubemap.enable();
//
//          gl.glColor4f(1, 1, 1, 1);
//          gl.glBegin(GL2.GL_QUADS);
//
//          gl.glMultiTexCoord2f(GL2.GL_TEXTURE0, 0,0);
//          gl.glMultiTexCoord4f(GL2.GL_TEXTURE1, matRot.get(0,0), matRot.get(0,1), matRot.get(0,2),  1);
//          gl.glMultiTexCoord4f(GL2.GL_TEXTURE2, matRot.get(1,0), matRot.get(1,1), matRot.get(1,2),  1);
//          gl.glMultiTexCoord4f(GL2.GL_TEXTURE3, matRot.get(2,0), matRot.get(2,1), matRot.get(2,2),  1);
//          gl.glVertex2f(-1,-1);
//
//          gl.glMultiTexCoord2f(GL2.GL_TEXTURE0, 1,0);
//          gl.glMultiTexCoord4f(GL2.GL_TEXTURE1, matRot.get(0,0), matRot.get(0,1), matRot.get(0,2), -1);
//          gl.glMultiTexCoord4f(GL2.GL_TEXTURE2, matRot.get(1,0), matRot.get(1,1), matRot.get(1,2),  1);
//          gl.glMultiTexCoord4f(GL2.GL_TEXTURE3, matRot.get(2,0), matRot.get(2,1), matRot.get(2,2),  1);
//          gl.glVertex2f( 1,-1);
//
//          gl.glMultiTexCoord2f(GL2.GL_TEXTURE0, 1,1);
//          gl.glMultiTexCoord4f(GL2.GL_TEXTURE1, matRot.get(0,0), matRot.get(0,1), matRot.get(0,2), -1);
//          gl.glMultiTexCoord4f(GL2.GL_TEXTURE2, matRot.get(1,0), matRot.get(1,1), matRot.get(1,2), -1);
//          gl.glMultiTexCoord4f(GL2.GL_TEXTURE3, matRot.get(2,0), matRot.get(2,1), matRot.get(2,2),  1);
//          gl.glVertex2f( 1, 1);
//
//          gl.glMultiTexCoord2f(GL2.GL_TEXTURE0, 0,1);
//          gl.glMultiTexCoord4f(GL2.GL_TEXTURE1, matRot.get(0,0), matRot.get(0,1), matRot.get(0,2),  1);
//          gl.glMultiTexCoord4f(GL2.GL_TEXTURE2, matRot.get(1,0), matRot.get(1,1), matRot.get(1,2), -1);
//          gl.glMultiTexCoord4f(GL2.GL_TEXTURE3, matRot.get(2,0), matRot.get(2,1), matRot.get(2,2),  1);
//          gl.glVertex2f(-1, 1);
//
//          gl.glEnd();
//
//          cubemap.disable();
//          gl.glDisable(GL2.GL_FRAGMENT_PROGRAM_ARB);
//
//          break;
//        }
//
//        case CA_FULLSCREEN_NORMALMAP: {
//          // Draw quad over full display
//          gl.glActiveTexture(GL2.GL_TEXTURE0);
//          dynamicTextures[CA_TEXTURE_NORMAL_MAP].bind();
//
//          gl.glCallList(displayListIDs[CA_DRAW_SCREEN_QUAD]);
//          break;
//        }
//
//        case CA_FULLSCREEN_HEIGHT: {
//          // Draw quad over full display
//          gl.glActiveTexture(GL2.GL_TEXTURE0);
//          gl.glBindTexture(GL2.GL_TEXTURE_2D, texHeightOutput);
//
//          gl.glCallList(displayListIDs[CA_DRAW_SCREEN_QUAD]);
//          break;
//        }
//
//        case CA_FULLSCREEN_FORCE: {
//          // Draw quad over full display
//          gl.glActiveTexture(GL2.GL_TEXTURE0);
//          dynamicTextures[CA_TEXTURE_FORCE_TARGET].bind();
//
//          gl.glCallList(displayListIDs[CA_DRAW_SCREEN_QUAD]);
//          break;
//        }
//
//        case CA_TILED_THREE_WINDOWS: {
//          // Draw quad over full display
//          // lower left
//          gl.glActiveTexture(GL2.GL_TEXTURE0);
//          dynamicTextures[CA_TEXTURE_FORCE_TARGET].bind();
//          gl.glMatrixMode(GL2.GL_MODELVIEW);
//          gl.glPushMatrix();
//
//          gl.glTranslatef(-0.5f, -0.5f, 0);
//          gl.glScalef(0.5f, 0.5f, 1);
//          gl.glCallList(displayListIDs[CA_DRAW_SCREEN_QUAD]);
//          gl.glPopMatrix();
//
//          // lower right
//          gl.glBindTexture(GL2.GL_TEXTURE_2D, texVelocityOutput);
//          gl.glPushMatrix();
//
//          gl.glTranslatef(0.5f, -0.5f, 0);
//          gl.glScalef(0.5f, 0.5f, 1);
//          gl.glCallList(displayListIDs[CA_DRAW_SCREEN_QUAD]);
//          gl.glPopMatrix();
//
//          // upper left
//          dynamicTextures[CA_TEXTURE_NORMAL_MAP].bind();
//          gl.glMatrixMode(GL2.GL_MODELVIEW);
//          gl.glPushMatrix();
//
//          gl.glTranslatef(-0.5f, 0.5f, 0);
//          gl.glScalef(0.5f, 0.5f, 1);
//          gl.glCallList(displayListIDs[CA_DRAW_SCREEN_QUAD]);
//          gl.glPopMatrix();
//
//          // upper right
//          gl.glBindTexture(GL2.GL_TEXTURE_2D, texHeightOutput);
//          gl.glMatrixMode(GL2.GL_MODELVIEW);
//          gl.glPushMatrix();
//
//          gl.glTranslatef(0.5f, 0.5f, 0);
//          gl.glScalef(0.5f, 0.5f, 1);
//          gl.glCallList(displayListIDs[CA_DRAW_SCREEN_QUAD]);
//          gl.glPopMatrix();
//
//          break;
//        }
//      }
//    } else {
//      // skip rendering this frame
//      skipCount++;
//    }
//  }
//
//  public void singleStep()                               { singleStep  = true;                 }
//  public void enableAnimation(boolean enable)            { animate     = enable;               }
//  public void enableSlowAnimation(boolean enable)        { slow        = enable;               }
//  public void reset()                                    { reset       = true;                 }
//  public void setRenderMode(int mode)                    { renderMode  = mode;                 }
//
//  public void enableWireframe(boolean enable)            { wireframe   = enable;               }
//  public void enableBorderWrapping(boolean enable)       { wrap        = enable;               }
//
//  public void enableBoundaryApplication(boolean enable)  { applyInteriorBoundaries = enable;   }
//  public void enableSpinningLogo(boolean enable)         { spinLogo    = enable;               }
//
//  public void  setBlurDistance(float distance)           { blurDist    = distance;
//                                                           mustUpdateBlurOffsets = true;       }
//  public float getBlurDistance()                         { return blurDist;                    }
//
//  public void  setBumpScale(float scale)                 { bumpScale   = scale;                }
//  public float getBumpScale()                            { return bumpScale;                   }
//
//  public void  setDropFrequency(float frequency)         { dropletFrequency = frequency;       }
//  public float getDropFrequency()                        { return dropletFrequency;            }
//
//  public static class Droplet {
//    private float rX;
//    private float rY;
//    private float rScale;
//
//    Droplet(float rX, float rY, float rScale) {
//      this.rX     = rX;
//      this.rY     = rY;
//      this.rScale = rScale;
//    }
//
//    float rX()     { return rX;     }
//    float rY()     { return rY;     }
//    float rScale() { return rScale; }
//  }
//
//  public synchronized void addDroplet(Droplet drop) {
//    droplets.add(drop);
//  }
//
//  //----------------------------------------------------------------------
//  // Internals only below this point
//  //
//
//  class Listener implements GLEventListener {
//
//    public void init(GLAutoDrawable drawable) {
//      GL2 gl = drawable.getGL().getGL2();
//
//      initOpenGL(gl);
//    }
//
//    public void dispose(GLAutoDrawable drawable) {
//    }
//
//    public void display(GLAutoDrawable drawable) {
//
//      GL2 gl = drawable.getGL().getGL2();
//      if (mustUpdateBlurOffsets) {
//        updateBlurVertOffset(gl);
//        mustUpdateBlurOffsets = false;
//      }
//
//      // Take a single step in the cellular automaton
//
//      // Disable culling
//      gl.glDisable(GL2.GL_CULL_FACE);
//
//      if (reset) {
//        reset = false;
//        flipState = 0;
//      }
//
//      if (animate) {
//        // Update the textures for one step of the simulation
//        doSingleTimeStep(gl);
//      } else if (singleStep) {
//        doSingleTimeStep(gl);
//        singleStep = false;
//      }
//
//      // Force rendering to pbuffer to complete
//      gl.glFlush();
//
//      if (slow && (slowDelay > 0) ) {
//        try {
//          Thread.sleep(slowDelay);
//        } catch (InterruptedException e) {
//        }
//      }
//    }
//
//    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
//
//    // Unused routines
//    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
//  }
//
//  // We need to load the initial texture file early to get the width
//  // and height for the pbuffer
//  private void loadInitialTexture(String initialMapFilename) {
//    try {
//      initialMapData = TextureIO.newTextureData(getClass().getClassLoader().getResourceAsStream(initialMapFilename),
//              false,
//              FileUtil.getFileSuffix(initialMapFilename));
//    } catch (IOException e) {
//      throw new GLException(e);
//    }
//    initialMapDimensions[0] = initialMapData.getWidth();
//    initialMapDimensions[1] = initialMapData.getHeight();
//  }
//
//  private void initOpenGL(GL2 gl) {
//    try {
//      loadTextures(gl, tmpSpinFilename, tmpDropletFilename, tmpCubeMapFilenamePrefix, tmpCubeMapFilenameSuffix);
//    } catch (IOException e) {
//      throw new GLException(e);
//    }
//    tmpSpinFilename           = null;
//    tmpDropletFilename        = null;
//    tmpCubeMapFilenamePrefix  = null;
//    tmpCubeMapFilenameSuffix  = null;
//
//    gl.glMatrixMode(GL2.GL_MODELVIEW);
//    gl.glLoadIdentity();
//    gl.glMatrixMode(GL2.GL_PROJECTION);
//    gl.glLoadIdentity();
//    glu.gluOrtho2D(-1, 1, -1, 1);
//
//    gl.glClearColor(0, 0, 0, 0);
//    gl.glDisable(GL2.GL_LIGHTING);
//    gl.glDisable(GL2.GL_DEPTH_TEST);
//
//    createAndWriteUVOffsets(gl, initialMapDimensions[0], initialMapDimensions[1]);
//
//    checkExtension(gl, "GL_ARB_vertex_program");
//    checkExtension(gl, "GL_ARB_fragment_program");
//    checkExtension(gl, "GL_ARB_multitexture");
//
//    ///////////////////////////////////////////////////////////////////////////
//    // UV Offset Vertex Program
//    ///////////////////////////////////////////////////////////////////////////
//
//    int[] tmpInt = new int[1];
//    gl.glGenProgramsARB(1, tmpInt, 0);
//    vertexProgramID = tmpInt[0];
//    gl.glBindProgramARB(GL2.GL_VERTEX_PROGRAM_ARB, vertexProgramID);
//
//    String programBuffer =
//"!!ARBvp1.0\n" +
//"# Constant memory location declarations (must match those in Java sources)\n" +
//"# CV_UV_OFFSET_TO_USE = 0\n" +
//"\n" +
//"# CV_UV_T0_NO_OFFSET  = 1\n" +
//"# CV_UV_T0_TYPE1      = 2\n" +
//"# CV_UV_T0_TYPE2      = 3\n" +
//"# CV_UV_T0_TYPE3      = 4\n" +
//"# CV_UV_T0_TYPE4      = 5\n" +
//"\n" +
//"# CV_UV_T1_NO_OFFSET  = 6\n" +
//"# CV_UV_T1_TYPE1      = 7\n" +
//"# CV_UV_T1_TYPE2      = 8\n" +
//"# CV_UV_T1_TYPE3      = 9\n" +
//"# CV_UV_T1_TYPE4      = 10\n" +
//"\n" +
//"# CV_UV_T2_NO_OFFSET  = 11\n" +
//"# CV_UV_T2_TYPE1      = 12\n" +
//"# CV_UV_T2_TYPE2      = 13\n" +
//"# CV_UV_T2_TYPE3      = 14\n" +
//"# CV_UV_T2_TYPE4      = 15\n" +
//"\n" +
//"# CV_UV_T3_NO_OFFSET  = 16\n" +
//"# CV_UV_T3_TYPE1      = 17\n" +
//"# CV_UV_T3_TYPE2      = 18\n" +
//"# CV_UV_T3_TYPE3      = 19\n" +
//"# CV_UV_T3_TYPE4      = 20\n" +
//"\n" +
//"# CV_CONSTS_1         = 21\n" +
//"\n" +
//"# Parameters\n" +
//"PARAM mvp [4]       = { state.matrix.mvp };     # modelview projection matrix\n" +
//"PARAM uvOffsetToUse = program.env[0];\n" +
//"PARAM uvOffsets[20] = { program.env[1..20] };\n" +
//"\n" +
//"# Addresses\n" +
//"ADDRESS addr;\n" +
//"\n" +
//"# Per vertex inputs\n" +
//"ATTRIB iPos         = vertex.position;          #position\n" +
//"\n" +
//"# Outputs\n" +
//"OUTPUT oPos         = result.position;          #position\n" +
//"\n" +
//"# Transform vertex-position to clip-space\n" +
//"DP4 oPos.x, iPos, mvp[0];\n" +
//"DP4 oPos.y, iPos, mvp[1];\n" +
//"DP4 oPos.z, iPos, mvp[2];\n" +
//"DP4 oPos.w, iPos, mvp[3];\n" +
//"\n" +
//"# Read which set of offsets to use\n" +
//"ARL addr.x, uvOffsetToUse.x;\n" +
//"\n" +
//"#    c[CV_CONSTS_1] = c[28]\n" +
//"#    x = 0\n" +
//"#    y = 0.5\n" +
//"#    z = 1\n" +
//"#    w = 2.0f\n" +
//"\n" +
//"#    Put a scale factor into r0 so the sample points\n" +
//"#    can be moved farther from the texel being written\n" +
//"#    MOV R0, c[28].z;\n" +
//"\n" +
//"# Add the offsets to the input texture\n" +
//"# coordinate, creating 4 sets of independent\n" +
//"# texture coordinates.\n" +
//"ADD result.texcoord[0], uvOffsets[addr.x     ], vertex.texcoord[0];\n" +
//"ADD result.texcoord[1], uvOffsets[addr.x + 5 ], vertex.texcoord[0];\n" +
//"ADD result.texcoord[2], uvOffsets[addr.x + 10], vertex.texcoord[0];\n" +
//"ADD result.texcoord[3], uvOffsets[addr.x + 15], vertex.texcoord[0];\n" +
//"\n" +
//"END\n";
//
//    // set up constants (not currently used in the vertex program, though)
//    float[] rCVConsts = new float[] { 0, 0.5f, 1.0f, 2.0f };
//    gl.glProgramEnvParameter4fvARB(GL2.GL_VERTEX_PROGRAM_ARB, CV_CONSTS_1, rCVConsts, 0);
//
//    loadProgram(gl, GL2.GL_VERTEX_PROGRAM_ARB, programBuffer);
//
//    ///////////////////////////////////////////////////////////////////////////
//    // fragment program setup for equal weight combination of texels
//    ///////////////////////////////////////////////////////////////////////////
//    displayListIDs[CA_FRAGMENT_PROGRAM_EQ_WEIGHT_COMBINE] = gl.glGenLists(1);
//    initEqWeightCombine_PostMult(gl, displayListIDs[CA_FRAGMENT_PROGRAM_EQ_WEIGHT_COMBINE]);
//
//    ///////////////////////////////////////////////////////////////////////////
//    // fragment program setup for computing force from neighbors (step 1)
//    ///////////////////////////////////////////////////////////////////////////
//    displayListIDs[CA_FRAGMENT_PROGRAM_NEIGHBOR_FORCE_CALC_1] = gl.glGenLists(1);
//    initNeighborForceCalcStep1(gl, displayListIDs[CA_FRAGMENT_PROGRAM_NEIGHBOR_FORCE_CALC_1]);
//
//    ///////////////////////////////////////////////////////////////////////////
//    // fragment program setup for computing force from neighbors (step 2)
//    ///////////////////////////////////////////////////////////////////////////
//    displayListIDs[CA_FRAGMENT_PROGRAM_NEIGHBOR_FORCE_CALC_2] = gl.glGenLists(1);
//    initNeighborForceCalcStep2(gl, displayListIDs[CA_FRAGMENT_PROGRAM_NEIGHBOR_FORCE_CALC_2]);
//
//    ///////////////////////////////////////////////////////////////////////////
//    // fragment program setup to apply force
//    ///////////////////////////////////////////////////////////////////////////
//    displayListIDs[CA_FRAGMENT_PROGRAM_APPLY_FORCE] = gl.glGenLists(1);
//    initApplyForce(gl, displayListIDs[CA_FRAGMENT_PROGRAM_APPLY_FORCE]);
//
//    ///////////////////////////////////////////////////////////////////////////
//    // fragment program setup to apply velocity
//    ///////////////////////////////////////////////////////////////////////////
//    displayListIDs[CA_FRAGMENT_PROGRAM_APPLY_VELOCITY] = gl.glGenLists(1);
//    initApplyVelocity(gl, displayListIDs[CA_FRAGMENT_PROGRAM_APPLY_VELOCITY]);
//
//    ///////////////////////////////////////////////////////////////////////////
//    // fragment program setup to create a normal map
//    ///////////////////////////////////////////////////////////////////////////
//    displayListIDs[CA_FRAGMENT_PROGRAM_CREATE_NORMAL_MAP] = gl.glGenLists(1);
//    initCreateNormalMap(gl, displayListIDs[CA_FRAGMENT_PROGRAM_CREATE_NORMAL_MAP]);
//
//    ///////////////////////////////////////////////////////////////////////////
//    // fragment program setup for dot product reflection
//    ///////////////////////////////////////////////////////////////////////////
//    displayListIDs[CA_FRAGMENT_PROGRAM_REFLECT] = gl.glGenLists(1);
//    initDotProductReflect(gl, displayListIDs[CA_FRAGMENT_PROGRAM_REFLECT]);
//
//    ///////////////////////////////////////////////////////////////////////////
//    // display list to render a single screen space quad.
//    ///////////////////////////////////////////////////////////////////////////
//    displayListIDs[CA_DRAW_SCREEN_QUAD] = gl.glGenLists(1);
//    gl.glNewList(displayListIDs[CA_DRAW_SCREEN_QUAD], GL2.GL_COMPILE);
//    gl.glColor4f(1, 1, 1, 1);
//    gl.glBegin(GL2.GL_TRIANGLE_STRIP);
//    gl.glTexCoord2f(0, 1); gl.glVertex2f(-1,  1);
//    gl.glTexCoord2f(0, 0); gl.glVertex2f(-1, -1);
//    gl.glTexCoord2f(1, 1); gl.glVertex2f( 1,  1);
//    gl.glTexCoord2f(1, 0); gl.glVertex2f( 1, -1);
//    gl.glEnd();
//    gl.glEndList();
//  }
//
//  private void checkExtension(GL gl, String extensionName) {
//    if (!gl.isExtensionAvailable(extensionName)) {
//      throw new GLException("Unable to initialize " + extensionName + " OpenGL extension");
//    }
//  }
//
//  private void doSingleTimeStep(GL2 gl) {
//    int temp;
//
//    // Swap texture source & target indices & pointers
//    //  0 = start from initial loaded texture
//    //  1/2 = flip flop back and forth between targets & sources
//
//    switch (flipState) {
//    case 0:
//      texHeightInput    = dynamicTextures[CA_TEXTURE_HEIGHT_SOURCE].getTextureObject();    // initial height map.
//      texHeightOutput   = dynamicTextures[CA_TEXTURE_HEIGHT_TARGET].getTextureObject();    // next height map.
//
//      texVelocityInput  = dynamicTextures[CA_TEXTURE_VELOCITY_SOURCE].getTextureObject();  // initial velocity.
//      texVelocityOutput = dynamicTextures[CA_TEXTURE_VELOCITY_TARGET].getTextureObject();  // next velocity.
//
//      // Clear initial velocity texture to 0x80 == gray
//      gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
//      gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
//
//      // Now we need to copy the resulting pixels into the intermediate force field texture
//      gl.glActiveTexture(GL2.GL_TEXTURE0);
//      gl.glBindTexture(GL2.GL_TEXTURE_2D, texVelocityInput);
//
//      // use CopyTexSubImage for speed (even though we copy all of it) since we pre-allocated the texture
//      gl.glCopyTexSubImage2D(GL2.GL_TEXTURE_2D, 0, 0, 0, 0, 0, initialMapDimensions[0], initialMapDimensions[1]);
//
//      break;
//
//    case 1:
//      temp              = texHeightInput;
//      texHeightInput    = texHeightOutput;
//      texHeightOutput   = temp;
//
//      temp              = texVelocityInput;
//      texVelocityInput  = texVelocityOutput;
//      texVelocityOutput = temp;
//
//      break;
//
//    case 2:
//      temp              = texHeightInput;
//      texHeightInput    = texHeightOutput;
//      texHeightOutput   = temp;
//
//      temp              = texVelocityInput;
//      texVelocityInput  = texVelocityOutput;
//      texVelocityOutput = temp;
//      break;
//    }
//
//    // even if wireframe mode, render to texture as solid
//    gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
//
//    /////////////////////////////////////////////////////////////
//    //  Render first 3 components of force from three neighbors
//    //  Offsets selected are 1 center texel for center height
//    //    and 3 of the 4 nearest neighbors.  Texture selected
//    //    is same for all stages as we're turning height difference
//    //    of nearest neightbor texels into a force value.
//
//    gl.glCallList(displayListIDs[CA_FRAGMENT_PROGRAM_NEIGHBOR_FORCE_CALC_1]);
//
//    // set current source texture for stage 0 texture
//    for (int i = 0; i < 4; i++)
//      {
//        gl.glActiveTexture(GL2.GL_TEXTURE0 + i);
//        gl.glBindTexture(GL2.GL_TEXTURE_2D, texHeightInput);
//        gl.glEnable(GL2.GL_TEXTURE_2D);
//      }
//
//    int wrapMode = wrap ? GL2.GL_REPEAT : GL2.GL_CLAMP_TO_EDGE;
//    gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, wrapMode);
//    gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, wrapMode);
//
//    // disable blending
//    gl.glDisable(GL2.GL_BLEND);
//
//    // render using offset 1 (type 1 -- center + 3 of 4 nearest neighbors).
//    gl.glProgramEnvParameter4fARB(GL2.GL_VERTEX_PROGRAM_ARB, CV_UV_OFFSET_TO_USE, 1, 0, 0, 0);
//
//    // bind the vertex program to be used for this step and the next one.
//    gl.glBindProgramARB(GL2.GL_VERTEX_PROGRAM_ARB, vertexProgramID);
//    gl.glEnable(GL2.GL_VERTEX_PROGRAM_ARB);
//
//    // render a screen quad. with texture coords doing difference of nearby texels for force calc.
//    gl.glCallList(displayListIDs[CA_DRAW_SCREEN_QUAD]);
//
//    gl.glDisable(GL2.GL_FRAGMENT_PROGRAM_ARB);
//
//    // Now we need to copy the resulting pixels into the intermediate force field texture
//    gl.glActiveTexture(GL2.GL_TEXTURE2);
//    dynamicTextures[CA_TEXTURE_FORCE_INTERMEDIATE].bind();
//
//    // use CopyTexSubImage for speed (even though we copy all of it) since we pre-allocated the texture
//    gl.glCopyTexSubImage2D(GL2.GL_TEXTURE_2D, 0, 0, 0, 0, 0, initialMapDimensions[0], initialMapDimensions[1]);
//
//    ////////////////////////////////////////////////////////////////
//    // Now add in last component of force for the 4th neighbor
//    //  that we didn't have enough texture lookups to do in the
//    //  first pass
//
//    gl.glCallList(displayListIDs[CA_FRAGMENT_PROGRAM_NEIGHBOR_FORCE_CALC_2]);
//
//    // Cannot use additive blending as the force contribution might
//    //   be negative and would have to subtract from the dest.
//    // We must instead use an additional texture as target and read
//    //   the previous partial 3-neighbor result into the pixel shader
//    //   for possible subtraction
//
//    // Alphablend must be false
//
//    //; t0 = center  (same as last phase)
//    //; t1 = 2nd axis final point (same as last phase)
//    //; t2 = previous partial result texture sampled at center (result of last phase copied to texture)
//    //; t3 = not used (disable now)
//
//    gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, wrapMode);
//    gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, wrapMode);
//
//    gl.glActiveTexture(GL2.GL_TEXTURE3);
//    gl.glDisable(GL2.GL_TEXTURE_2D);
//
//    // vertex program already bound.
//    // render using offset 2 (type 2 -- final nearest neighbor plus center of previous result).
//    gl.glProgramEnvParameter4fARB(GL2.GL_VERTEX_PROGRAM_ARB, CV_UV_OFFSET_TO_USE, 2, 0, 0, 0);
//
//    // render a screen quad
//    gl.glCallList(displayListIDs[CA_DRAW_SCREEN_QUAD]);
//
//    gl.glDisable(GL2.GL_FRAGMENT_PROGRAM_ARB);
//
//    // Now we need to copy the resulting pixels into the intermediate force field texture
//    gl.glActiveTexture(GL2.GL_TEXTURE1);
//    dynamicTextures[CA_TEXTURE_FORCE_TARGET].bind();
//
//    // use CopyTexSubImage for speed (even though we copy all of it) since we pre-allocated the texture
//    gl.glCopyTexSubImage2D(GL2.GL_TEXTURE_2D, 0, 0, 0, 0, 0, initialMapDimensions[0], initialMapDimensions[1]);
//
//    /////////////////////////////////////////////////////////////////
//    // Apply the force with a scale factor to reduce it's magnitude.
//    // Add this to the current texture representing the water height.
//
//    gl.glCallList(displayListIDs[CA_FRAGMENT_PROGRAM_APPLY_FORCE]);
//
//    // use offsets of zero
//    gl.glProgramEnvParameter4fARB(GL2.GL_VERTEX_PROGRAM_ARB, CV_UV_OFFSET_TO_USE, 0, 0, 0, 0);
//
//    // bind the vertex program to be used for this step and the next one.
//
//    gl.glActiveTexture(GL2.GL_TEXTURE0);
//    gl.glBindTexture(GL2.GL_TEXTURE_2D, texVelocityInput);
//    gl.glActiveTexture(GL2.GL_TEXTURE1);
//    dynamicTextures[CA_TEXTURE_FORCE_TARGET].bind();
//    gl.glActiveTexture(GL2.GL_TEXTURE2);
//    gl.glDisable(GL2.GL_TEXTURE_2D);
//    gl.glActiveTexture(GL2.GL_TEXTURE3);
//    gl.glDisable(GL2.GL_TEXTURE_2D);
//
//    // Draw the quad to add in force.
//    gl.glCallList(displayListIDs[CA_DRAW_SCREEN_QUAD]);
//
//    gl.glDisable(GL2.GL_FRAGMENT_PROGRAM_ARB);
//
//    ///////////////////////////////////////////////////////////////////
//    // With velocity texture selected, render new excitation droplets
//    //   at random freq.
//
//    float randomFrequency = (float) Math.random();
//
//    if (dropletFrequency > randomFrequency) {
//      // a drop falls - decide where
//      Droplet drop = new Droplet(2 * ((float)Math.random() - 0.5f),
//                                 2 * ((float)Math.random() - 0.5f),
//                                 0.02f +  0.1f * ((float)Math.random()));
//      addDroplet(drop);
//    }
//
//    //  Now draw the droplets:
//    if (!droplets.isEmpty()) {
//      drawDroplets(gl);
//      droplets.clear();
//    }
//
//    // Now we need to copy the resulting pixels into the velocity texture
//    gl.glActiveTexture(GL2.GL_TEXTURE1);
//    gl.glBindTexture(GL2.GL_TEXTURE_2D, texVelocityOutput);
//
//    // use CopyTexSubImage for speed (even though we copy all of it) since we pre-allocated the texture
//    gl.glCopyTexSubImage2D(GL2.GL_TEXTURE_2D, 0, 0, 0, 0, 0, initialMapDimensions[0], initialMapDimensions[1]);
//
//    //////////////////////////////////////////////////////////////////////
//    // Apply velocity to position
//    gl.glCallList(displayListIDs[CA_FRAGMENT_PROGRAM_APPLY_VELOCITY]);
//    gl.glEnable(GL2.GL_VERTEX_PROGRAM_ARB);
//
//    gl.glActiveTexture(GL2.GL_TEXTURE0);
//    gl.glBindTexture(GL2.GL_TEXTURE_2D, texHeightInput);
//    gl.glActiveTexture(GL2.GL_TEXTURE1); // velocity output already bound
//    gl.glEnable(GL2.GL_TEXTURE_2D);
//
//    // use offsets of zero
//    gl.glProgramEnvParameter4fARB(GL2.GL_VERTEX_PROGRAM_ARB, CV_UV_OFFSET_TO_USE, 0, 0, 0, 0);
//
//    // Draw the quad to add in force.
//    gl.glCallList(displayListIDs[CA_DRAW_SCREEN_QUAD]);
//
//    gl.glDisable(GL2.GL_FRAGMENT_PROGRAM_ARB);
//
//    // Now we need to copy the resulting pixels into the input height texture
//    gl.glActiveTexture(GL2.GL_TEXTURE0);
//    gl.glBindTexture(GL2.GL_TEXTURE_2D, texHeightInput);
//
//    // use CopyTexSubImage for speed (even though we copy all of it) since we pre-allocated the texture
//    gl.glCopyTexSubImage2D(GL2.GL_TEXTURE_2D, 0, 0, 0, 0, 0, initialMapDimensions[0], initialMapDimensions[1]);
//
//    ///////////////////////////////////////////////////////////////////
//    //  blur positions to smooth noise & generaly dampen things
//    //  degree of blur is controlled by magnitude of 4 neighbor texel
//    //   offsets with bilinear on
//
//    for (int i = 1; i < 4; i++) {
//      gl.glActiveTexture(GL2.GL_TEXTURE0 + i);
//      gl.glBindTexture(GL2.GL_TEXTURE_2D, texHeightInput);
//      gl.glEnable(GL2.GL_TEXTURE_2D);
//    }
//
//    // use offsets of 3
//    gl.glProgramEnvParameter4fARB(GL2.GL_VERTEX_PROGRAM_ARB, CV_UV_OFFSET_TO_USE, 3, 0, 0, 0);
//
//    gl.glCallList(displayListIDs[CA_FRAGMENT_PROGRAM_EQ_WEIGHT_COMBINE]);
//
//    gl.glCallList(displayListIDs[CA_DRAW_SCREEN_QUAD]);
//    gl.glDisable(GL2.GL_FRAGMENT_PROGRAM_ARB);
//
//    // Draw the logo in the water.
//    if (applyInteriorBoundaries) {
//      gl.glDisable(GL2.GL_VERTEX_PROGRAM_ARB);
//      drawInteriorBoundaryObjects(gl);
//    }
//
//    // Now we need to copy the resulting pixels into the velocity texture
//    gl.glActiveTexture(GL2.GL_TEXTURE0);
//    gl.glBindTexture(GL2.GL_TEXTURE_2D, texHeightOutput);
//
//    // use CopyTexSubImage for speed (even though we copy all of it) since we pre-allocated the texture
//    gl.glCopyTexSubImage2D(GL2.GL_TEXTURE_2D, 0, 0, 0, 0, 0, initialMapDimensions[0], initialMapDimensions[1]);
//
//    ///////////////////////////////////////////////////////////////////
//    // If selected, create a normal map from the height
//
//    if (createNormalMap) {
//      createNormalMap(gl);
//    }
//
//    ///////////////////////////////////////////////////////////
//    // Flip the state variable for the next round of rendering
//    switch (flipState) {
//    case 0:
//      flipState = 1;
//      break;
//    case 1:
//      flipState = 2;
//      break;
//    case 2:
//      flipState = 1;
//      break;
//    }
//  }
//
//  private void createNormalMap(GL2 gl) {
//    // use the height output on all four texture stages
//    for (int i = 0; i < 4; i++) {
//      gl.glActiveTexture(GL2.GL_TEXTURE0 + i);
//      gl.glBindTexture(GL2.GL_TEXTURE_2D, texHeightOutput);
//      gl.glEnable(GL2.GL_TEXTURE_2D);
//    }
//
//    // Set constants for red & green scale factors (also essential color masks)
//    // Red mask first
//    float[] pixMasks = new float[] { normalSTScale, 0.0f, 0.0f, 0.0f };
//
//    gl.glProgramEnvParameter4fvARB(GL2.GL_FRAGMENT_PROGRAM_ARB, 0, pixMasks, 0);
//
//    // Now green mask & scale:
//    pixMasks[0] = 0.0f;
//    pixMasks[1] = normalSTScale;
//    gl.glProgramEnvParameter4fvARB(GL2.GL_FRAGMENT_PROGRAM_ARB, 1, pixMasks, 0);
//
//    gl.glCallList(displayListIDs[CA_FRAGMENT_PROGRAM_CREATE_NORMAL_MAP]);
//
//    // set vp offsets to nearest neighbors
//    gl.glProgramEnvParameter4fARB(GL2.GL_VERTEX_PROGRAM_ARB, CV_UV_OFFSET_TO_USE, 4, 0, 0, 0);
//    gl.glEnable(GL2.GL_VERTEX_PROGRAM_ARB);
//
//    gl.glCallList(displayListIDs[CA_DRAW_SCREEN_QUAD]);
//
//    gl.glDisable(GL2.GL_FRAGMENT_PROGRAM_ARB);
//
//    // Now we need to copy the resulting pixels into the normal map
//    gl.glActiveTexture(GL2.GL_TEXTURE0);
//    dynamicTextures[CA_TEXTURE_NORMAL_MAP].bind();
//
//    // use CopyTexSubImage for speed (even though we copy all of it) since we pre-allocated the texture
//    gl.glCopyTexSubImage2D(GL2.GL_TEXTURE_2D, 0, 0, 0, 0, 0, initialMapDimensions[0], initialMapDimensions[1]);
//  }
//
//  private void drawInteriorBoundaryObjects(GL2 gl) {
//
//    gl.glActiveTexture(GL2.GL_TEXTURE0);
//    initialMapTex.bind();
//    initialMapTex.enable();
//
//    gl.glEnable(GL2.GL_ALPHA_TEST);
//
//    // disable other texture units.
//    for (int i = 1; i < 4; i++) {
//      gl.glActiveTexture(GL2.GL_TEXTURE0 + i);
//      gl.glDisable(GL2.GL_TEXTURE_2D);
//    }
//
//    gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
//    gl.glEnable(GL2.GL_BLEND);
//
//    gl.glCallList(displayListIDs[CA_DRAW_SCREEN_QUAD]);
//
//    if (spinLogo) {
//      gl.glActiveTexture(GL2.GL_TEXTURE0);
//      spinTex.bind();
//      gl.glMatrixMode(GL2.GL_MODELVIEW);
//      gl.glPushMatrix();
//      gl.glRotatef(angle, 0, 0, 1);
//      angle += 1;
//
//      gl.glCallList(displayListIDs[CA_DRAW_SCREEN_QUAD]);
//
//      gl.glPopMatrix();
//    }
//
//    gl.glDisable(GL2.GL_ALPHA_TEST);
//    gl.glDisable(GL2.GL_BLEND);
//  }
//
//  private void loadTextures(GL gl,
//                            String spinFilename,
//                            String dropletFilename,
//                            String cubeMapFilenamePrefix,
//                            String cubeMapFilenameSuffix) throws IOException {
//    if (initialMapData == null) {
//      throw new GLException("Must call loadInitialTexture ahead of time");
//    }
//
//    initialMapTex = TextureIO.newTexture(initialMapData);
//    spinTex       = TextureIO.newTexture(getClass().getClassLoader().getResourceAsStream(spinFilename), false,
//                                         FileUtil.getFileSuffix(spinFilename));
//    dropletTex    = TextureIO.newTexture(getClass().getClassLoader().getResourceAsStream(dropletFilename), false,
//                                         FileUtil.getFileSuffix(dropletFilename));
//
//    // load the cubemap texture
//    cubemap = Cubemap.loadFromStreams(getClass().getClassLoader(),
//                                      cubeMapFilenamePrefix,
//                                      cubeMapFilenameSuffix,
//                                      true);
//
//    // now create dummy intermediate textures from the initial map texture
//    for (int i = 0; i < CA_NUM_DYNAMIC_TEXTURES; i++) {
//      dynamicTextures[i] = TextureIO.newTexture(initialMapData);
//    }
//
//    initialMapData = null;
//
//    texHeightInput    = initialMapTex.getTextureObject();                               // initial height map.
//    texHeightOutput   = dynamicTextures[CA_TEXTURE_HEIGHT_TARGET].getTextureObject();   // next height map.
//
//    texVelocityInput  = dynamicTextures[CA_TEXTURE_VELOCITY_SOURCE].getTextureObject(); // initial velocity.
//    texVelocityOutput = dynamicTextures[CA_TEXTURE_VELOCITY_TARGET].getTextureObject(); // next velocity.
//  }
//
//  private void createAndWriteUVOffsets(GL2 gl, int width, int height) {
//    // This sets vertex shader constants used to displace the
//    //  source texture over several additive samples.  This is
//    //  used to accumulate neighboring texel information that we
//    //  need to run the game - the 8 surrounding texels, and the
//    //  single source texel which will either spawn or die in the
//    //  next generation.
//    // Label the texels as follows, for a source texel "e" that
//    //  we want to compute for the next generation:
//    //
//    //          abc
//    //          def
//    //          ghi:
//
//    // first the easy one: no offsets for sampling center
//    //  occupied or unoccupied
//    // Use index offset value 0.0 to access these in the
//    //  vertex shader.
//
//    perTexelWidth  = 1.0f / width;
//    perTexelHeight = 1.0f / height;
//
//    // Offset set 0 : center texel sampling
//    float[] noOffsetX = new float[] { 0, 0, 0, 0 };
//    float[] noOffsetY = new float[] { 0, 0, 0, 0 };
//
//    // Offset set 1:  For use with neighbor force pixel shader 1
//    //  samples center with 0, +u, -u, and +v,
//    //  ie the 'e','d', 'f', and 'h' texels
//    float dist = 1.5f;
//    float[] type1OffsetX = new float[] { 0.0f, -dist * perTexelWidth,  dist * perTexelWidth,   dist * perTexelWidth  };
//    float[] type1OffsetY = new float[] { 0.0f,  dist * perTexelHeight, dist * perTexelHeight, -dist * perTexelHeight };
//
//    // Offset set 2:  for use with neighbor force pixel shader 2
//    //  samples center with 0, and -v texels
//    //  ie the 'e' and 'b' texels
//    // This completes a pattern of sampling center texel and it's
//    //   4 nearest neighbors to run the height-based water simulation
//    // 3rd must be 0 0 to sample texel center from partial result
//    //   texture.
//
//    float[] type2OffsetX = new float[] { 0.0f, -dist * perTexelWidth,  0.0f, 0.0f   };
//    float[] type2OffsetY = new float[] { 0.0f, -dist * perTexelHeight, 0.0f, 0.0f   };
//
//    // type 3 offsets
//    updateBlurVertOffset(gl);
//
//    /////////////////////////////////////////////////////////////
//    // Nearest neighbor offsets:
//
//    float[] type4OffsetX = new float[] { -perTexelWidth,   perTexelWidth,   0.0f,              0.0f   };
//    float[] type4OffsetY = new float[] { 0.0f,             0.0f,            -perTexelHeight,   perTexelHeight };
//
//    // write all these offsets to constant memory
//    for (int i = 0; i < 4; ++i) {
//      float noOffset[]    = { noOffsetX[i],    noOffsetY[i],    0.0f, 0.0f };
//      float type1Offset[] = { type1OffsetX[i], type1OffsetY[i], 0.0f, 0.0f };
//      float type2Offset[] = { type2OffsetX[i], type2OffsetY[i], 0.0f, 0.0f };
//      float type4Offset[] = { type4OffsetX[i], type4OffsetY[i], 0.0f, 0.0f };
//
//      gl.glProgramEnvParameter4fvARB(GL2.GL_VERTEX_PROGRAM_ARB, CV_UV_T0_NO_OFFSET + 5 * i, noOffset, 0);
//      gl.glProgramEnvParameter4fvARB(GL2.GL_VERTEX_PROGRAM_ARB, CV_UV_T0_TYPE1     + 5 * i, type1Offset, 0);
//      gl.glProgramEnvParameter4fvARB(GL2.GL_VERTEX_PROGRAM_ARB, CV_UV_T0_TYPE2     + 5 * i, type2Offset, 0);
//      gl.glProgramEnvParameter4fvARB(GL2.GL_VERTEX_PROGRAM_ARB, CV_UV_T0_TYPE4     + 5 * i, type4Offset, 0);
//    }
//  }
//
//  private void updateBlurVertOffset(GL2 gl) {
//    float[] type3OffsetX = new float[] { -perTexelWidth * 0.5f,
//                                         perTexelWidth,
//                                         perTexelWidth * 0.5f,
//                                         -perTexelWidth
//    };
//    float[] type3OffsetY = new float[] { perTexelHeight,
//                                         perTexelHeight * 0.5f,
//                                         -perTexelHeight,
//                                         -perTexelHeight * 0.5f
//    };
//    float[] offsets = new float[] { 0, 0, 0, 0 };
//
//    for (int i = 0; i < 4; ++i) {
//      offsets[0] = blurDist * ( type3OffsetX[i]);
//      offsets[1] = blurDist * ( type3OffsetY[i]);
//      gl.glProgramEnvParameter4fvARB(GL2.GL_VERTEX_PROGRAM_ARB, CV_UV_T0_TYPE3 + 5 * i, offsets, 0);
//    }
//  }
//
//  private synchronized void drawDroplets(GL2 gl) {
//    gl.glDisable(GL2.GL_FRAGMENT_PROGRAM_ARB);
//    gl.glDisable(GL2.GL_VERTEX_PROGRAM_ARB);
//
//    gl.glActiveTexture(GL2.GL_TEXTURE0);
//    dropletTex.bind();
//    dropletTex.enable();
//
//    gl.glActiveTexture(GL2.GL_TEXTURE1);
//    gl.glDisable(GL2.GL_TEXTURE_2D);
//
//    gl.glBlendFunc(GL2.GL_ONE, GL2.GL_ONE);
//    gl.glEnable(GL2.GL_BLEND);
//
//    gl.glBegin(GL2.GL_QUADS);
//    gl.glColor4f(1, 1, 1, 1);
//    for (Iterator iter = droplets.iterator(); iter.hasNext(); ) {
//      Droplet droplet = (Droplet) iter.next();
//      // coords in [-1,1] range
//
//      // Draw a single quad to the texture render target
//      // The quad is textured with the initial droplet texture, and
//      //   covers some small portion of the render target
//      // Draw the droplet
//
//      gl.glTexCoord2f(0, 0); gl.glVertex2f(droplet.rX() - droplet.rScale(), droplet.rY() - droplet.rScale());
//      gl.glTexCoord2f(1, 0); gl.glVertex2f(droplet.rX() + droplet.rScale(), droplet.rY() - droplet.rScale());
//      gl.glTexCoord2f(1, 1); gl.glVertex2f(droplet.rX() + droplet.rScale(), droplet.rY() + droplet.rScale());
//      gl.glTexCoord2f(0, 1); gl.glVertex2f(droplet.rX() - droplet.rScale(), droplet.rY() + droplet.rScale());
//    }
//    gl.glEnd();
//
//    gl.glDisable(GL2.GL_BLEND);
//  }
//
//  //----------------------------------------------------------------------
//  // Inlined register combiner and texture shader programs
//  // (don't want to port nvparse as it's a dead-end; we'll focus on Cg instead)
//
//  private void initEqWeightCombine_PostMult(GL2 gl, int displayListID) {
//    // Take samples of all four texture inputs and average them,
//    // adding on a bias
//    //
//    // Original register combiner program:
//    //
//    // Stage 0
//    // rgb
//    // {
//    //   discard = half_bias(tex0);
//    //   discard = half_bias(tex1);
//    //   spare0 = sum();
//    //   scale_by_one_half();
//    // }
//    // Stage 1
//    // rgb
//    // {
//    //   discard = half_bias(tex2);
//    //   discard = half_bias(tex3);
//    //   spare1 = sum();
//    //   scale_by_one_half();
//    // }
//    // Stage 2
//    // rgb
//    // {
//    //   discard = spare0;
//    //   discard = spare1;
//    //   spare0 = sum();
//    //   scale_by_one_half();
//    // }
//    // Stage 3
//    // rgb
//    // {
//    //   discard = const0;
//    //   discard = spare0;
//    //   spare0 = sum();
//    // }
//
//    float[] const0 = new float[] { 0.5f, 0.5f, 0.5f, 1.0f };
//
//    int[] tmpInt = new int[1];
//    gl.glGenProgramsARB(1, tmpInt, 0);
//    int fragProg = tmpInt[0];
//    gl.glBindProgramARB(GL2.GL_FRAGMENT_PROGRAM_ARB, fragProg);
//
//    String program =
//"!!ARBfp1.0\n" +
//"PARAM const0  = program.env[0];\n" +
//"PARAM oneQtr  = { 0.25, 0.25, 0.25, 0.25 };\n" +
//"PARAM two     = { 2.0, 2.0, 2.0, 2.0 };\n" +
//"TEMP texSamp0, texSamp1, texSamp2, texSamp3;\n" +
//"TEMP spare0, spare1;\n" +
//"\n" +
//"TEX texSamp0, fragment.texcoord[0], texture[0], 2D;\n" +
//"TEX texSamp1, fragment.texcoord[1], texture[1], 2D;\n" +
//"TEX texSamp2, fragment.texcoord[2], texture[2], 2D;\n" +
//"TEX texSamp3, fragment.texcoord[3], texture[3], 2D;\n" +
//"ADD spare0, texSamp0, texSamp1;\n" +
//"ADD spare1, texSamp2, texSamp3;\n" +
//"ADD spare0, spare0, spare1;\n" +
//"SUB spare0, spare0, two;\n" +
//"MAD result.color, oneQtr, spare0, const0;\n" +
//"\n" +
//"END\n";
//
//    loadProgram(gl, GL2.GL_FRAGMENT_PROGRAM_ARB, program);
//
//    gl.glNewList(displayListID, GL2.GL_COMPILE);
//    gl.glProgramEnvParameter4fvARB(GL2.GL_FRAGMENT_PROGRAM_ARB, 0, const0, 0);
//    gl.glBindProgramARB(GL2.GL_FRAGMENT_PROGRAM_ARB, fragProg);
//    gl.glEnable(GL2.GL_FRAGMENT_PROGRAM_ARB);
//    gl.glEndList();
//  }
//
//  private void initNeighborForceCalcStep1(GL2 gl, int displayListID) {
//    // Step one in the nearest-neighbor force calculation for height-based water
//    // simulation.  NeighborForceCalc2 is the second step.
//    //
//    // This step takes the center point and three neighboring points, and computes
//    // the texel difference as the "force" acting to pull the center texel.
//    //
//    // The amount to which the computed force is applied to the texel is controlled
//    // in a separate shader.
//
//    //  get colors from all 4 texture stages
//    //  tex0 = center texel
//    //  tex1 = 1st neighbor
//    //  tex2 = 2nd neighbor - same axis as 1st neighbor point
//    //       so force for that axis == t1 - t0 + t2 - t0
//    //  tex3 = 3rd neighbor on other axis
//
//    // Original register combiner program:
//    //
//    // Stage 0
//    // rgb
//    // {
//    //   //s0 = t1 - t0;
//    //   discard = -tex0;
//    //   discard = tex1;
//    //   spare0 = sum();
//    // }
//    // Stage 1
//    // rgb
//    // {
//    //   //s1 = t2 - t0;
//    //   discard = -tex0;
//    //   discard = tex2;
//    //   spare1 = sum();
//    // }
//    // Stage 2
//    // // 'force' for 1st axis
//    // rgb
//    // {
//    //   //s0 = s0 + s1 = t1 - t0 + t2 - t0;
//    //   discard = spare0;
//    //   discard = spare1;
//    //   spare0 = sum();
//    // }
//    // Stage 3
//    // // one more point for 2nd axis
//    // rgb
//    // {
//    //   //s1 = t3 - t0;
//    //   discard = -tex0;
//    //   discard = tex3;
//    //   spare1 = sum();
//    // }
//    // Stage 4
//    // rgb
//    // {
//    //   //s0 = s0 + s1 = t3 - t0 + t2 - t0 + t1 - t0;
//    //   discard = spare0;
//    //   discard = spare1;
//    //   spare0 = sum();
//    // }
//    // Stage 5
//    // // Now add in a force to gently pull the center texel's
//    // //  value to 0.5.  The strength of this is controlled by
//    // //  the PCN_EQ_REST_FAC  - restoration factor
//    // // Without this, the simulation will fade to zero or fly
//    // //  away to saturate at 1.0
//    // rgb
//    // {
//    //   //s1 = 0.5 - t0;
//    //   discard = -tex0;
//    //   discard = const0;
//    //   spare1 = sum();
//    // }
//    // Stage 6
//    // {
//    //   rgb
//    //   {
//    //     discard = spare1 * const0;
//    //     discard = spare0;
//    //     spare0 = sum();
//    //   }
//    // }
//    // Stage 7
//    // rgb
//    // {
//    //   discard = spare0;
//    //   discard = const0;
//    //   spare0 = sum();
//    // }
//
//    float[] const0 = new float[] { 0.5f, 0.5f, 0.5f, 1.0f };
//
//    int[] tmpInt = new int[1];
//    gl.glGenProgramsARB(1, tmpInt, 0);
//    int fragProg = tmpInt[0];
//    gl.glBindProgramARB(GL2.GL_FRAGMENT_PROGRAM_ARB, fragProg);
//
//    String program =
//"!!ARBfp1.0\n" +
//"PARAM const0 = program.env[0];\n" +
//"PARAM three                 = {  3,     3,     3,    1.0 };\n" +
//"TEMP texSamp0, texSamp1, texSamp2, texSamp3;\n" +
//"TEMP spare0, spare1;\n" +
//"\n" +
//"TEX texSamp0, fragment.texcoord[0], texture[0], 2D;\n" +
//"TEX texSamp1, fragment.texcoord[1], texture[1], 2D;\n" +
//"TEX texSamp2, fragment.texcoord[2], texture[2], 2D;\n" +
//"TEX texSamp3, fragment.texcoord[3], texture[3], 2D;\n" +
//"ADD spare0, texSamp1, texSamp2;\n" +
//"MAD spare1, const0, const0, const0;\n" +
//"ADD spare0, texSamp3, spare0;\n" +
//"ADD spare0, spare1, spare0;\n" +
//"ADD spare1, three, const0;\n" +
//"MAD result.color, -spare1, texSamp0, spare0;\n" +
//
//// Faster version which hardcodes in value of const0:
////"ADD spare0, texSamp1, texSamp2;\n" +
////"ADD spare1, texSamp3, pointSevenFive;\n" +
////"ADD spare0, spare0, spare1;\n" +
////"MAD result.color, minusThreePointFive, texSamp0, spare0;\n" +
//
//// Straightforward port:
////"SUB spare0, texSamp1, texSamp0;\n" +
////"SUB spare1, texSamp2, texSamp0;\n" +
////"ADD spare0, spare0, spare1;\n" +
////"SUB spare1, texSamp3, texSamp0;\n" +
////"ADD spare0, spare0, spare1;\n" +
////"SUB spare1, const0, texSamp0;\n" +
////"MAD spare0, const0, spare1, spare0;\n" +
////"ADD result.color, spare0, const0;\n" +
//
//"\n" +
//"END\n";
//
//    loadProgram(gl, GL2.GL_FRAGMENT_PROGRAM_ARB, program);
//
//    gl.glNewList(displayListID, GL2.GL_COMPILE);
//    gl.glProgramEnvParameter4fvARB(GL2.GL_FRAGMENT_PROGRAM_ARB, 0, const0, 0);
//    gl.glBindProgramARB(GL2.GL_FRAGMENT_PROGRAM_ARB, fragProg);
//    gl.glEnable(GL2.GL_FRAGMENT_PROGRAM_ARB);
//    gl.glEndList();
//  }
//
//  private void initNeighborForceCalcStep2(GL2 gl, int displayListID) {
//    // 2nd step of force calc for render-to-texture
//    // water simulation.
//    //
//    // Adds the 4th & final neighbor point to the
//    // force calc..
//    //
//    // Bias and scale the values so 0 force is 0.5,
//    // full negative force is 0.0, and full pos is
//    // 1.0
//    //
//    // tex0    Center texel
//    // tex1    2nd axis neighbor point
//    // tex2    previous partial force amount
//    // Result from t1 - t0 is added to this t2
//    //  partial result & output
//
//    // Original register combiner program:
//    //
//    // Stage 0
//    // last element of neighbor force
//    // rgb
//    // {
//    //   discard = -tex0;
//    //   discard = tex1;
//    //   spare0 = sum();
//    // }
//    // Stage 1
//    // add with previous partial force amount
//    // rgb
//    // {
//    //   discard = spare0;
//    //   discard = tex2;
//    //   spare0 = sum();
//    // }
//
//    int[] tmpInt = new int[1];
//    gl.glGenProgramsARB(1, tmpInt, 0);
//    int fragProg = tmpInt[0];
//    gl.glBindProgramARB(GL2.GL_FRAGMENT_PROGRAM_ARB, fragProg);
//
//    String program =
//"!!ARBfp1.0\n" +
//"PARAM const0 = program.env[0];\n" +
//"TEMP texSamp0, texSamp1, texSamp2;\n" +
//"TEMP spare0;\n" +
//"\n" +
//"TEX texSamp0, fragment.texcoord[0], texture[0], 2D;\n" +
//"TEX texSamp1, fragment.texcoord[1], texture[1], 2D;\n" +
//"TEX texSamp2, fragment.texcoord[2], texture[2], 2D;\n" +
//"SUB spare0, texSamp1, texSamp0;\n" +
//"ADD result.color, spare0, texSamp2;\n" +
//"\n" +
//"END\n";
//
//    loadProgram(gl, GL2.GL_FRAGMENT_PROGRAM_ARB, program);
//
//    gl.glNewList(displayListID, GL2.GL_COMPILE);
//    gl.glBindProgramARB(GL2.GL_FRAGMENT_PROGRAM_ARB, fragProg);
//    gl.glEnable(GL2.GL_FRAGMENT_PROGRAM_ARB);
//    gl.glEndList();
//  }
//
//  private void initApplyForce(GL2 gl, int displayListID) {
//    // This shader samples t1, biases its value to a signed number, and applies this
//    // value multiplied by a scale factor to the t0 sample.
//    //
//    // This is used to apply a "force" texture value to a "velocity" state texture
//    // for nearest-neighbor height-based water simulations.  The output pixel is
//    // the new "velocity" value to replace the t0 sample in rendering to a new
//    // texture which will replace the texture selected into t0.
//    //
//    // A nearly identical shader using a different scaling constant is used to
//    // apply the "velocity" value to a "height" texture at each texel.
//    //
//    // t1 comes in the range [0,1] but needs to hold signed values, so a value of
//    // 0.5 in t1 represents zero force.  This is biased to a signed value in
//    // computing the new velocity.
//    //
//    // tex0 = previous velocity
//    // tex1 = force
//    //
//    // Bias the force so that 0.5 input = no change in t0 value
//    //  and 0.0 input means -0.5 * scale change in t0 value
//    //
//    // New velocity = force * scale + previous velocity
//
//    // Original register combiner program:
//    //
//    // Stage 0
//    // rgb
//    // {
//    //   discard = expand(tex1) * const0;
//    //   discard = expand(tex0);
//    //   spare0 = sum();
//    //   scale_by_one_half();
//    // }
//    // Stage 1
//    // rgb
//    // {
//    //   discard = spare0;
//    //   discard = const1;
//    //   spare0 = sum();
//    // }
//
//    float[] const0 = new float[] { 0.25f, 0.25f, 0.25f, 1.0f };
//    float[] const1 = new float[] { 0.5f,  0.5f,  0.5f,  1.0f };
//
//    int[] tmpInt = new int[1];
//    gl.glGenProgramsARB(1, tmpInt, 0);
//    int fragProg = tmpInt[0];
//    gl.glBindProgramARB(GL2.GL_FRAGMENT_PROGRAM_ARB, fragProg);
//
//    String program =
//"!!ARBfp1.0\n" +
//"PARAM const0 = program.env[0];\n" +
//"PARAM const1 = program.env[1];\n" +
//"PARAM one     = { 1.0, 1.0, 1.0, 0.0 };\n" +
//"PARAM oneHalf = { 0.5, 0.5, 0.5, 1.0 };\n" +
//"PARAM two     = { 2.0, 2.0, 2.0, 1.0 };\n" +
//"TEMP texSamp0, texSamp1;\n" +
//"TEMP spare0, spare1;\n" +
//"\n" +
//"TEX texSamp0, fragment.texcoord[0], texture[0], 2D;\n" +
//"TEX texSamp1, fragment.texcoord[1], texture[1], 2D;\n" +
//"MAD spare0, two, texSamp1, -one;\n" +
//"MAD spare1, two, texSamp0, -one;\n" +
//"MAD spare0, spare0, const0, spare1;\n" +
//"MAD result.color, oneHalf, spare0, const1;\n" +
//"\n" +
//"END\n";
//
//    loadProgram(gl, GL2.GL_FRAGMENT_PROGRAM_ARB, program);
//
//    gl.glNewList(displayListID, GL2.GL_COMPILE);
//    gl.glProgramEnvParameter4fvARB(GL2.GL_FRAGMENT_PROGRAM_ARB, 0, const0, 0);
//    gl.glProgramEnvParameter4fvARB(GL2.GL_FRAGMENT_PROGRAM_ARB, 1, const1, 0);
//    gl.glBindProgramARB(GL2.GL_FRAGMENT_PROGRAM_ARB, fragProg);
//    gl.glEnable(GL2.GL_FRAGMENT_PROGRAM_ARB);
//    gl.glEndList();
//  }
//
//  private void initApplyVelocity(GL2 gl, int displayListID) {
//    // This shader samples t1, biases its value to a signed number, and applies this
//    // value multiplied by a scale factor to the t0 sample.
//    //
//    // This is used to apply a "velocity" texture value to a "height" state texture
//    // for nearest-neighbor height-based water simulations.  The output pixel is
//    // the new "height" value to replace the t0 sample in rendering to a new
//    // texture which will replace the texture selected into t0.
//    //
//    // A nearly identical shader using a different scaling constant is used to
//    // apply the "force" value to the "velocity" texture at each texel.
//    //
//    // t1 comes in the range [0,1] but needs to hold signed values, so a value of
//    // 0.5 in t1 represents zero velocity.  This is biased to a signed value in
//    // computing the new position.
//    //
//    // tex0 = height field
//    // tex1 = velocity
//    //
//    // Bias the force/velocity to a signed value so we can subtract from
//    //   the t0 position sample.
//    //
//    // New height = velocity * scale factor + old height
//
//    // Original register combiner program:
//    //
//    // Stage 0
//    // rgb
//    // {
//    //   discard = expand(tex1) * const0;
//    //   discard = expand(tex0);
//    //   spare0 = sum();
//    //   scale_by_one_half();
//    // }
//    // Stage 1
//    // rgb
//    // {
//    //   discard = spare0;
//    //   discard = const0;
//    //   spare0 = sum();
//    // }
//    // }
//
//    float[] const0 = new float[] { 0.5f,  0.5f,  0.5f,  1.0f };
//
//    int[] tmpInt = new int[1];
//    gl.glGenProgramsARB(1, tmpInt, 0);
//    int fragProg = tmpInt[0];
//    gl.glBindProgramARB(GL2.GL_FRAGMENT_PROGRAM_ARB, fragProg);
//
//    String program =
//"!!ARBfp1.0\n" +
//"PARAM const0 = program.env[0];\n" +
//"PARAM one     = { 1.0, 1.0, 1.0, 0.0 };\n" +
//"PARAM oneHalf = { 0.5, 0.5, 0.5, 1.0 };\n" +
//"PARAM two     = { 2.0, 2.0, 2.0, 1.0 };\n" +
//"TEMP texSamp0, texSamp1;\n" +
//"TEMP spare0, spare1;\n" +
//"\n" +
//"TEX texSamp0, fragment.texcoord[0], texture[0], 2D;\n" +
//"TEX texSamp1, fragment.texcoord[1], texture[1], 2D;\n" +
//"MAD spare0, two, texSamp1, -one;\n" +
//"MAD spare1, two, texSamp0, -one;\n" +
//"MAD spare0, spare0, const0, spare1;\n" +
//"MAD result.color, oneHalf, spare0, const0;\n" +
//"\n" +
//"END\n";
//
//    loadProgram(gl, GL2.GL_FRAGMENT_PROGRAM_ARB, program);
//
//    gl.glNewList(displayListID, GL2.GL_COMPILE);
//    gl.glProgramEnvParameter4fvARB(GL2.GL_FRAGMENT_PROGRAM_ARB, 0, const0, 0);
//    gl.glBindProgramARB(GL2.GL_FRAGMENT_PROGRAM_ARB, fragProg);
//    gl.glEnable(GL2.GL_FRAGMENT_PROGRAM_ARB);
//    gl.glEndList();
//  }
//
//  private void initCreateNormalMap(GL2 gl, int displayListID) {
//    // Neighbor-differencing for RGB normal map creation.  Scale factors for s and t
//    // axis components are set in program code.
//    // This does a crude 1-s^2-t^2 calculation for the blue component in order to
//    // approximately normalize the RGB normal map vector.  For s^2+t^2 close to 1.0,
//    // this is a close approximation to blue = sqrt(1 - s^2 - t^2) which would give a
//    // normalized vector.
//    // An additional pass with a dependent texture lookup (alpha-red or green-blue)
//    // could be used to produce an exactly normalized normal.
//
//    // colors from all 4 texture stages
//    // tex0 = -s,  0
//    // tex1 = +s,  0
//    // tex2 =  0, +t
//    // tex3 =  0, -t
//
//    // Original register combiner program:
//    //
//    // Stage 0
//    // rgb
//    // {
//    //   // (t0 - t1)*4  : 4 for higher scale
//    //   discard = -tex1;
//    //   discard = tex0;
//    //   spare0 = sum();
//    //   scale_by_four();
//    // }
//    // Stage 1
//    // rgb
//    // {
//    //   // (t3 - t2)*4 : 4 for higher scale
//    //   discard = -tex2;
//    //   discard = tex3;
//    //   spare1 = sum();
//    //   scale_by_four();
//    // }
//    // Stage 2
//    // Define const0 in the third general combiner as RGBA = (scale, 0, 0, 0)
//    //  Where scale [0,1] is applied to reduce the magnitude
//    //  of the s axis component of the normal.
//    // Define const1 in the third combiner similarly to affect the t axis component
//    // define these by "ramboing" them in the C++ code that uses this combiner script.
//    // Note: these variables have been renamed to "redMask" and "greenMask" in
//    // the fragment program below.
//    // rgb
//    // {
//    //   // see comment about consts above!
//    //   // t0 = s result in red only
//    //   discard = spare0 * const0;
//    //   discard = spare1 * const1;
//    //   spare0 = sum();
//    // }
//    // Stage 3
//    // rgb
//    // {
//    //   tex1 = spare0 * spare0;
//    //   scale_by_two();
//    // }
//    // Stage 4
//    // const0 = (1, 1, 0, 0);
//    // rgb
//    // {
//    //   spare1 = unsigned_invert(tex1) . const0;
//    //   scale_by_one_half();
//    // }
//    // Stage 5
//    // const0 = (0.5, 0.5, 0, 0);
//    // rgb
//    // {
//    //   discard = spare0;
//    //   discard = const0;
//    //   spare0 = sum();
//    // }
//    // Stage 6
//    // const0 = (0, 0, 1, 1);
//    // rgb
//    // {
//    //   discard = spare1 * const0;
//    //   discard = spare0;
//    //   spare0 = sum();
//    // }
//
//
//    float[] const0 = new float[] { 0.5f,  0.5f,  0.5f,  1.0f };
//
//    int[] tmpInt = new int[1];
//    gl.glGenProgramsARB(1, tmpInt, 0);
//    int fragProg = tmpInt[0];
//    gl.glBindProgramARB(GL2.GL_FRAGMENT_PROGRAM_ARB, fragProg);
//
//    String program =
//"!!ARBfp1.0\n" +
//"PARAM redMask   = program.env[0];\n" +
//"PARAM greenMask = program.env[1];\n" +
//"PARAM const0    = { 1.0, 1.0, 0.0, 0.0 };\n" +
//"PARAM const1    = { 0.5, 0.5, 0.0, 0.0 };\n" +
//"PARAM const2    = { 0.0, 0.0, 1.0, 1.0 };\n" +
//"PARAM one     = { 1.0, 1.0, 1.0, 0.0 };\n" +
//"PARAM oneHalf = { 0.5, 0.5, 0.5, 1.0 };\n" +
//"PARAM two     = { 2.0, 2.0, 2.0, 1.0 };\n" +
//"PARAM four    = { 4.0, 4.0, 4.0, 1.0 };\n" +
//"TEMP texSamp0, texSamp1, texSamp2, texSamp3;\n" +
//"TEMP spare0, spare1, spare2;\n" +
//"\n" +
//"TEX texSamp0, fragment.texcoord[0], texture[0], 2D;\n" +
//"TEX texSamp1, fragment.texcoord[1], texture[1], 2D;\n" +
//"TEX texSamp2, fragment.texcoord[2], texture[2], 2D;\n" +
//"TEX texSamp3, fragment.texcoord[3], texture[3], 2D;\n" +
//"SUB spare0, texSamp0, texSamp1;\n" +
//"MUL spare0, spare0, four;\n" +
//"SUB spare1, texSamp3, texSamp2;\n" +
//"MUL spare1, spare1, four;\n" +
//"MUL spare0, spare0, redMask;\n" +
//"MAD spare0, greenMask, spare1, spare0;\n" +
//"MUL_SAT spare2, spare0, spare0;\n" +
//"SUB spare2, one, spare2;\n" +
//"DP3 spare1, spare2, const0;\n" +
//"ADD spare0, spare0, const1;\n" +
//"MAD result.color, const2, spare1, spare0;\n" +
//"\n" +
//"END\n";
//
//    loadProgram(gl, GL2.GL_FRAGMENT_PROGRAM_ARB, program);
//
//    gl.glNewList(displayListID, GL2.GL_COMPILE);
//    gl.glBindProgramARB(GL2.GL_FRAGMENT_PROGRAM_ARB, fragProg);
//    gl.glEnable(GL2.GL_FRAGMENT_PROGRAM_ARB);
//    gl.glEndList();
//  }
//
//  private void initDotProductReflect(GL2 gl, int displayListID) {
//    // Pseudocode for this operation, derived from the NVidia
//    // texture_shader.txt documentation at
//    // http://oss.sgi.com/projects/ogl-sample/registry/NV/texture_shader.txt
//
//    // TEX texSamp0, fragment.texcoord[0], texture[0], 2D;
//    // MAD texSamp0, two, texSamp0, minusOne;
//    // TEMP dotPP = texSamp0 . texcoord[1];
//    // TEMP dotP  = texSamp0 . texcoord[2];
//    // TEMP dotC  = texSamp0 . texcoord[3];
//    // TEMP R, N, E;
//    // N = [dotPP, dotP, dotC];
//    // ooNLength = N dot N;
//    // RCP ooNLength, ooNLength;
//    // E = [texcoord[1].w, texcoord[2].w, texcoord[3].w];
//    // nDotE = N dot E;
//    // MUL R, nDotE, N;
//    // MUL R, R, two;
//    // MUL R, R, ooNLength;
//    // SUB R, R, E;
//    // TEX result.color, R, texture[3], CUBE;
//
//    // This fragment program is pretty length-sensitive; making it too
//    // big causes the frame rate to be cut in half on my machine
//    // (Quadro FX Go700) due to sync-to-vertical-refresh. The program
//    // below is more optimized in its use of temporaries. Some of the
//    // scaling operations on the first component of the normal vector
//    // (before subtracting off the E vector) don't appear to make much
//    // of a visual difference so they are skipped as well.
//
//    int[] tmpInt = new int[1];
//    gl.glGenProgramsARB(1, tmpInt, 0);
//    int fragProg = tmpInt[0];
//    gl.glBindProgramARB(GL2.GL_FRAGMENT_PROGRAM_ARB, fragProg);
//
//    String program =
//"!!ARBfp1.0\n" +
//"PARAM minusOne = { -1.0, -1.0, -1.0, 0.0 };\n" +
//"PARAM two      = {  2.0,  2.0,  2.0, 0.0 };\n" +
//"TEMP texSamp0, R, N, E;\n" +
//"\n" +
//"TEX texSamp0, fragment.texcoord[0], texture[0], 2D;\n" +
//"MAD texSamp0, two, texSamp0, minusOne;\n" +
//"DP3 N.x,   texSamp0, fragment.texcoord[1];\n" +
//"DP3 N.y,   texSamp0, fragment.texcoord[2];\n" +
//"DP3 N.z,   texSamp0, fragment.texcoord[3];\n" +
//"MOV E.x, fragment.texcoord[1].w;\n" +
//"MOV E.y, fragment.texcoord[2].w;\n" +
//"MOV E.z, fragment.texcoord[3].w;\n" +
//"MUL N, N, two;\n" +
//"SUB R, N, E;\n" +
//"TEX result.color, R, texture[3], CUBE;\n" +
//"\n" +
//"END";
//
//    loadProgram(gl, GL2.GL_FRAGMENT_PROGRAM_ARB, program);
//
//    gl.glNewList(displayListID, GL2.GL_COMPILE);
//    gl.glBindProgramARB(GL2.GL_FRAGMENT_PROGRAM_ARB, fragProg);
//    gl.glEnable(GL2.GL_FRAGMENT_PROGRAM_ARB);
//    gl.glEndList();
//  }
//
//  private void loadProgram(GL2 gl,
//                           int target,
//                           String programBuffer) {
//
//    gl.glProgramStringARB(target, GL2.GL_PROGRAM_FORMAT_ASCII_ARB, programBuffer.length(), programBuffer);
//
//    int[] errPos = new int[1];
//    gl.glGetIntegerv(GL2.GL_PROGRAM_ERROR_POSITION_ARB, errPos, 0);
//    if (errPos[0] >= 0) {
//      String kind = "Program";
//      if (target == GL2.GL_VERTEX_PROGRAM_ARB) {
//        kind = "Vertex program";
//      } else if (target == GL2.GL_FRAGMENT_PROGRAM_ARB) {
//        kind = "Fragment program";
//      }
//      System.out.println(kind + " failed to load:");
//      String errMsg = gl.glGetString(GL2.GL_PROGRAM_ERROR_STRING_ARB);
//      if (errMsg == null) {
//        System.out.println("[No error message available]");
//      } else {
//        System.out.println("Error message: \"" + errMsg + "\"");
//      }
//      System.out.println("Error occurred at position " + errPos[0] + " in program:");
//      int endPos = errPos[0];
//      while (endPos < programBuffer.length() && programBuffer.charAt(endPos) != '\n') {
//        ++endPos;
//      }
//      System.out.println(programBuffer.substring(errPos[0], endPos));
//      throw new GLException("Error loading " + kind);
//    } else {
//      if (target == GL2.GL_FRAGMENT_PROGRAM_ARB) {
//        int[] isNative = new int[1];
//        gl.glGetProgramivARB(GL2.GL_FRAGMENT_PROGRAM_ARB,
//                             GL2.GL_PROGRAM_UNDER_NATIVE_LIMITS_ARB,
//                             isNative, 0);
//        if (isNative[0] != 1) {
//          System.out.println("WARNING: fragment program is over native resource limits");
//          Thread.dumpStack();
//        }
//      }
//    }
//  }
//}
