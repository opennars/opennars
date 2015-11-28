//import java.io.IOException;
//
//epackage jhelp.engine.io.md2;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//
//import jhelp.engine.Color4f;
//import jhelp.engine.JHelpSceneRenderer;
//import jhelp.engine.NodeWithMaterial;
//import jhelp.engine.Object3D;
//import jhelp.engine.ObjectClone;
//import jhelp.engine.Point2D;
//import jhelp.engine.Point3D;
//import jhelp.engine.Texture;
//import jhelp.engine.Vertex;
//import jhelp.engine.event.JHelpSceneRendererListener;
//import jhelp.engine.gui.JHelpFrame3D;
//import jhelp.engine.util.Tool3D;
//import jhelp.util.debug.Debug;
//import jhelp.util.debug.DebugLevel;
//import jhelp.util.io.IndexedInputStream;
//import jhelp.util.list.EnumerationIterator;
//
//public class LoaderMD2
//{
//   static class Frame
//   {
//      char[]      name;
//      int         numberOfVertices;
//      VectorMD2   scale;
//      VectorMD2[] transformed;
//      VectorMD2   translate;
//      Vertice[]   vertices;
//
//      Frame(final int numberOfVertices)
//      {
//         this.scale = new VectorMD2();
//         this.translate = new VectorMD2();
//         this.name = new char[16];
//         this.vertices = new Vertice[numberOfVertices];
//         this.transformed = new VectorMD2[numberOfVertices];
//         this.numberOfVertices = numberOfVertices;
//      }
//
//      void read(final InputStream inputStream) throws IOException
//      {
//         this.scale.read(inputStream);
//         this.translate.read(inputStream);
//         LoaderMD2.readCharArray(this.name, inputStream);
//         for(int i = 0; i < this.numberOfVertices; i++)
//         {
//            final Vertice vertice = new Vertice();
//
//            vertice.read(inputStream);
//
//            this.vertices[i] = vertice;
//         }
//      }
//
//      void transform()
//      {
//         Vertice vertice;
//
//         for(int i = 0; i < this.numberOfVertices; i++)
//         {
//            vertice = this.vertices[i];
//
//            final VectorMD2 vectorMD2 = new VectorMD2();
//
//            vectorMD2.x = ((vertice.x & 0xFF) * this.scale.x) + this.translate.x;
//            vectorMD2.y = ((vertice.y & 0xFF) * this.scale.y) + this.translate.y;
//            vectorMD2.z = ((vertice.z & 0xFF) * this.scale.z) + this.translate.z;
//
//            this.transformed[i] = vectorMD2;
//         }
//      }
//   }
//
//   static class HeaderMd2
//   {
//      /** size in bytes of a frame */
//      int framesize;
//
//      /** magic number: "IDP2" : 844121161 */
//      int ident;
//
//      /** number of frames */
//      int num_frames;
//      /** number of opengl commands */
//      int num_glcmds;
//
//      /** number of skins */
//      int num_skins;
//
//      /** number of texture coordinates */
//      int num_st;
//      /** number of triangles */
//      int num_tris;
//      /** number of vertices per frame */
//      int num_vertices;
//      /** offset end of file */
//      int offset_end;
//      /** offset frame data */
//      int offset_frames;
//      /** offset OpenGL command data */
//      int offset_glcmds;
//
//      /** offset skin data */
//      int offset_skins;
//      /** offset texture coordinate data */
//      int offset_st;
//      /** offset triangle data */
//      int offset_tris;
//      /** texture height */
//      int skinheight;
//      /** texture width */
//      int skinwidth;
//      /** version: must be 8 */
//      int version;
//
//      void read(final InputStream inputStream) throws IOException
//      {
//         this.ident = LoaderMD2.readInt(inputStream);
//         this.version = LoaderMD2.readInt(inputStream);
//
//         this.skinwidth = LoaderMD2.readInt(inputStream);
//         this.skinheight = LoaderMD2.readInt(inputStream);
//
//         this.framesize = LoaderMD2.readInt(inputStream);
//
//         this.num_skins = LoaderMD2.readInt(inputStream);
//         this.num_vertices = LoaderMD2.readInt(inputStream);
//         this.num_st = LoaderMD2.readInt(inputStream);
//         this.num_tris = LoaderMD2.readInt(inputStream);
//         this.num_glcmds = LoaderMD2.readInt(inputStream);
//         this.num_frames = LoaderMD2.readInt(inputStream);
//
//         this.offset_skins = LoaderMD2.readInt(inputStream);
//         this.offset_st = LoaderMD2.readInt(inputStream);
//         this.offset_tris = LoaderMD2.readInt(inputStream);
//         this.offset_frames = LoaderMD2.readInt(inputStream);
//         this.offset_glcmds = LoaderMD2.readInt(inputStream);
//         this.offset_end = LoaderMD2.readInt(inputStream);
//      }
//   }
//
//   static class TextureCoordinate
//   {
//      float realU;
//      float realV;
//      short u;
//      short v;
//
//      void convert(final HeaderMd2 headerMd2)
//      {
//         this.realU = (float) (this.u & 0xFFFF) / (float) (headerMd2.skinwidth);
//         this.realV = (float) (this.v & 0xFFFF) / (float) (headerMd2.skinheight);
//      }
//
//      void read(final InputStream inputStream) throws IOException
//      {
//         this.u = LoaderMD2.readShort(inputStream);
//         this.v = LoaderMD2.readShort(inputStream);
//      }
//   }
//
//   static class TextureInformation
//   {
//      char[] name;
//
//      TextureInformation()
//      {
//         this.name = new char[64];
//      }
//
//      void read(final InputStream inputStream) throws IOException
//      {
//         LoaderMD2.readCharArray(this.name, inputStream);
//      }
//   }
//
//   static class Triangle
//   {
//      short firstPointIndex;
//      short firstUVIndex;
//      short secondPointIndex;
//      short secondUVIndex;
//      short thirdPointIndex;
//      short thirdUVIndex;
//
//      void read(final InputStream inputStream) throws IOException
//      {
//         this.firstPointIndex = LoaderMD2.readShort(inputStream);
//         this.secondPointIndex = LoaderMD2.readShort(inputStream);
//         this.thirdPointIndex = LoaderMD2.readShort(inputStream);
//
//         this.firstUVIndex = LoaderMD2.readShort(inputStream);
//         this.secondUVIndex = LoaderMD2.readShort(inputStream);
//         this.thirdUVIndex = LoaderMD2.readShort(inputStream);
//      }
//   }
//
//   static class VectorMD2
//   {
//      float x;
//      float y;
//      float z;
//
//      void read(final InputStream inputStream) throws IOException
//      {
//         this.x = LoaderMD2.readFloat(inputStream);
//         this.y = LoaderMD2.readFloat(inputStream);
//         this.z = LoaderMD2.readFloat(inputStream);
//      }
//   }
//
//   static class Vertice
//   {
//      byte normalIndex;
//      byte x;
//      byte y;
//      byte z;
//
//      void read(final InputStream inputStream) throws IOException
//      {
//         this.x = LoaderMD2.readbyte(inputStream);
//         this.y = LoaderMD2.readbyte(inputStream);
//         this.z = LoaderMD2.readbyte(inputStream);
//
//         this.normalIndex = LoaderMD2.readbyte(inputStream);
//      }
//   }
//
//   static final float[][] fixedNormals =
//                                       {
//         {
//         -0.525731f, 0.000000f, 0.850651f
//         },
//         {
//         -0.442863f, 0.238856f, 0.864188f
//         },
//         {
//         -0.295242f, 0.000000f, 0.955423f
//         },
//         {
//         -0.309017f, 0.500000f, 0.809017f
//         },
//         {
//         -0.162460f, 0.262866f, 0.951056f
//         },
//         {
//         0.000000f, 0.000000f, 1.000000f
//         },
//         {
//         0.000000f, 0.850651f, 0.525731f
//         },
//         {
//         -0.147621f, 0.716567f, 0.681718f
//         },
//         {
//         0.147621f, 0.716567f, 0.681718f
//         },
//         {
//         0.000000f, 0.525731f, 0.850651f
//         },
//         {
//         0.309017f, 0.500000f, 0.809017f
//         },
//         {
//         0.525731f, 0.000000f, 0.850651f
//         },
//         {
//         0.295242f, 0.000000f, 0.955423f
//         },
//         {
//         0.442863f, 0.238856f, 0.864188f
//         },
//         {
//         0.162460f, 0.262866f, 0.951056f
//         },
//         {
//         -0.681718f, 0.147621f, 0.716567f
//         },
//         {
//         -0.809017f, 0.309017f, 0.500000f
//         },
//         {
//         -0.587785f, 0.425325f, 0.688191f
//         },
//         {
//         -0.850651f, 0.525731f, 0.000000f
//         },
//         {
//         -0.864188f, 0.442863f, 0.238856f
//         },
//         {
//         -0.716567f, 0.681718f, 0.147621f
//         },
//         {
//         -0.688191f, 0.587785f, 0.425325f
//         },
//         {
//         -0.500000f, 0.809017f, 0.309017f
//         },
//         {
//         -0.238856f, 0.864188f, 0.442863f
//         },
//         {
//         -0.425325f, 0.688191f, 0.587785f
//         },
//         {
//         -0.716567f, 0.681718f, -0.147621f
//         },
//         {
//         -0.500000f, 0.809017f, -0.309017f
//         },
//         {
//         -0.525731f, 0.850651f, 0.000000f
//         },
//         {
//         0.000000f, 0.850651f, -0.525731f
//         },
//         {
//         -0.238856f, 0.864188f, -0.442863f
//         },
//         {
//         0.000000f, 0.955423f, -0.295242f
//         },
//         {
//         -0.262866f, 0.951056f, -0.162460f
//         },
//         {
//         0.000000f, 1.000000f, 0.000000f
//         },
//         {
//         0.000000f, 0.955423f, 0.295242f
//         },
//         {
//         -0.262866f, 0.951056f, 0.162460f
//         },
//         {
//         0.238856f, 0.864188f, 0.442863f
//         },
//         {
//         0.262866f, 0.951056f, 0.162460f
//         },
//         {
//         0.500000f, 0.809017f, 0.309017f
//         },
//         {
//         0.238856f, 0.864188f, -0.442863f
//         },
//         {
//         0.262866f, 0.951056f, -0.162460f
//         },
//         {
//         0.500000f, 0.809017f, -0.309017f
//         },
//         {
//         0.850651f, 0.525731f, 0.000000f
//         },
//         {
//         0.716567f, 0.681718f, 0.147621f
//         },
//         {
//         0.716567f, 0.681718f, -0.147621f
//         },
//         {
//         0.525731f, 0.850651f, 0.000000f
//         },
//         {
//         0.425325f, 0.688191f, 0.587785f
//         },
//         {
//         0.864188f, 0.442863f, 0.238856f
//         },
//         {
//         0.688191f, 0.587785f, 0.425325f
//         },
//         {
//         0.809017f, 0.309017f, 0.500000f
//         },
//         {
//         0.681718f, 0.147621f, 0.716567f
//         },
//         {
//         0.587785f, 0.425325f, 0.688191f
//         },
//         {
//         0.955423f, 0.295242f, 0.000000f
//         },
//         {
//         1.000000f, 0.000000f, 0.000000f
//         },
//         {
//         0.951056f, 0.162460f, 0.262866f
//         },
//         {
//         0.850651f, -0.525731f, 0.000000f
//         },
//         {
//         0.955423f, -0.295242f, 0.000000f
//         },
//         {
//         0.864188f, -0.442863f, 0.238856f
//         },
//         {
//         0.951056f, -0.162460f, 0.262866f
//         },
//         {
//         0.809017f, -0.309017f, 0.500000f
//         },
//         {
//         0.681718f, -0.147621f, 0.716567f
//         },
//         {
//         0.850651f, 0.000000f, 0.525731f
//         },
//         {
//         0.864188f, 0.442863f, -0.238856f
//         },
//         {
//         0.809017f, 0.309017f, -0.500000f
//         },
//         {
//         0.951056f, 0.162460f, -0.262866f
//         },
//         {
//         0.525731f, 0.000000f, -0.850651f
//         },
//         {
//         0.681718f, 0.147621f, -0.716567f
//         },
//         {
//         0.681718f, -0.147621f, -0.716567f
//         },
//         {
//         0.850651f, 0.000000f, -0.525731f
//         },
//         {
//         0.809017f, -0.309017f, -0.500000f
//         },
//         {
//         0.864188f, -0.442863f, -0.238856f
//         },
//         {
//         0.951056f, -0.162460f, -0.262866f
//         },
//         {
//         0.147621f, 0.716567f, -0.681718f
//         },
//         {
//         0.309017f, 0.500000f, -0.809017f
//         },
//         {
//         0.425325f, 0.688191f, -0.587785f
//         },
//         {
//         0.442863f, 0.238856f, -0.864188f
//         },
//         {
//         0.587785f, 0.425325f, -0.688191f
//         },
//         {
//         0.688191f, 0.587785f, -0.425325f
//         },
//         {
//         -0.147621f, 0.716567f, -0.681718f
//         },
//         {
//         -0.309017f, 0.500000f, -0.809017f
//         },
//         {
//         0.000000f, 0.525731f, -0.850651f
//         },
//         {
//         -0.525731f, 0.000000f, -0.850651f
//         },
//         {
//         -0.442863f, 0.238856f, -0.864188f
//         },
//         {
//         -0.295242f, 0.000000f, -0.955423f
//         },
//         {
//         -0.162460f, 0.262866f, -0.951056f
//         },
//         {
//         0.000000f, 0.000000f, -1.000000f
//         },
//         {
//         0.295242f, 0.000000f, -0.955423f
//         },
//         {
//         0.162460f, 0.262866f, -0.951056f
//         },
//         {
//         -0.442863f, -0.238856f, -0.864188f
//         },
//         {
//         -0.309017f, -0.500000f, -0.809017f
//         },
//         {
//         -0.162460f, -0.262866f, -0.951056f
//         },
//         {
//         0.000000f, -0.850651f, -0.525731f
//         },
//         {
//         -0.147621f, -0.716567f, -0.681718f
//         },
//         {
//         0.147621f, -0.716567f, -0.681718f
//         },
//         {
//         0.000000f, -0.525731f, -0.850651f
//         },
//         {
//         0.309017f, -0.500000f, -0.809017f
//         },
//         {
//         0.442863f, -0.238856f, -0.864188f
//         },
//         {
//         0.162460f, -0.262866f, -0.951056f
//         },
//         {
//         0.238856f, -0.864188f, -0.442863f
//         },
//         {
//         0.500000f, -0.809017f, -0.309017f
//         },
//         {
//         0.425325f, -0.688191f, -0.587785f
//         },
//         {
//         0.716567f, -0.681718f, -0.147621f
//         },
//         {
//         0.688191f, -0.587785f, -0.425325f
//         },
//         {
//         0.587785f, -0.425325f, -0.688191f
//         },
//         {
//         0.000000f, -0.955423f, -0.295242f
//         },
//         {
//         0.000000f, -1.000000f, 0.000000f
//         },
//         {
//         0.262866f, -0.951056f, -0.162460f
//         },
//         {
//         0.000000f, -0.850651f, 0.525731f
//         },
//         {
//         0.000000f, -0.955423f, 0.295242f
//         },
//         {
//         0.238856f, -0.864188f, 0.442863f
//         },
//         {
//         0.262866f, -0.951056f, 0.162460f
//         },
//         {
//         0.500000f, -0.809017f, 0.309017f
//         },
//         {
//         0.716567f, -0.681718f, 0.147621f
//         },
//         {
//         0.525731f, -0.850651f, 0.000000f
//         },
//         {
//         -0.238856f, -0.864188f, -0.442863f
//         },
//         {
//         -0.500000f, -0.809017f, -0.309017f
//         },
//         {
//         -0.262866f, -0.951056f, -0.162460f
//         },
//         {
//         -0.850651f, -0.525731f, 0.000000f
//         },
//         {
//         -0.716567f, -0.681718f, -0.147621f
//         },
//         {
//         -0.716567f, -0.681718f, 0.147621f
//         },
//         {
//         -0.525731f, -0.850651f, 0.000000f
//         },
//         {
//         -0.500000f, -0.809017f, 0.309017f
//         },
//         {
//         -0.238856f, -0.864188f, 0.442863f
//         },
//         {
//         -0.262866f, -0.951056f, 0.162460f
//         },
//         {
//         -0.864188f, -0.442863f, 0.238856f
//         },
//         {
//         -0.809017f, -0.309017f, 0.500000f
//         },
//         {
//         -0.688191f, -0.587785f, 0.425325f
//         },
//         {
//         -0.681718f, -0.147621f, 0.716567f
//         },
//         {
//         -0.442863f, -0.238856f, 0.864188f
//         },
//         {
//         -0.587785f, -0.425325f, 0.688191f
//         },
//         {
//         -0.309017f, -0.500000f, 0.809017f
//         },
//         {
//         -0.147621f, -0.716567f, 0.681718f
//         },
//         {
//         -0.425325f, -0.688191f, 0.587785f
//         },
//         {
//         -0.162460f, -0.262866f, 0.951056f
//         },
//         {
//         0.442863f, -0.238856f, 0.864188f
//         },
//         {
//         0.162460f, -0.262866f, 0.951056f
//         },
//         {
//         0.309017f, -0.500000f, 0.809017f
//         },
//         {
//         0.147621f, -0.716567f, 0.681718f
//         },
//         {
//         0.000000f, -0.525731f, 0.850651f
//         },
//         {
//         0.425325f, -0.688191f, 0.587785f
//         },
//         {
//         0.587785f, -0.425325f, 0.688191f
//         },
//         {
//         0.688191f, -0.587785f, 0.425325f
//         },
//         {
//         -0.955423f, 0.295242f, 0.000000f
//         },
//         {
//         -0.951056f, 0.162460f, 0.262866f
//         },
//         {
//         -1.000000f, 0.000000f, 0.000000f
//         },
//         {
//         -0.850651f, 0.000000f, 0.525731f
//         },
//         {
//         -0.955423f, -0.295242f, 0.000000f
//         },
//         {
//         -0.951056f, -0.162460f, 0.262866f
//         },
//         {
//         -0.864188f, 0.442863f, -0.238856f
//         },
//         {
//         -0.951056f, 0.162460f, -0.262866f
//         },
//         {
//         -0.809017f, 0.309017f, -0.500000f
//         },
//         {
//         -0.864188f, -0.442863f, -0.238856f
//         },
//         {
//         -0.951056f, -0.162460f, -0.262866f
//         },
//         {
//         -0.809017f, -0.309017f, -0.500000f
//         },
//         {
//         -0.681718f, 0.147621f, -0.716567f
//         },
//         {
//         -0.681718f, -0.147621f, -0.716567f
//         },
//         {
//         -0.850651f, 0.000000f, -0.525731f
//         },
//         {
//         -0.688191f, 0.587785f, -0.425325f
//         },
//         {
//         -0.587785f, 0.425325f, -0.688191f
//         },
//         {
//         -0.425325f, 0.688191f, -0.587785f
//         },
//         {
//         -0.425325f, -0.688191f, -0.587785f
//         },
//         {
//         -0.587785f, -0.425325f, -0.688191f
//         },
//         {
//         -0.688191f, -0.587785f, -0.425325f
//         }
//                                       };
//
//   static byte readbyte(final InputStream inputStream) throws IOException
//   {
//      return (byte) inputStream.read();
//   }
//
//   static char readChar(final InputStream inputStream) throws IOException
//   {
//      return (char) inputStream.read();
//   }
//
//   static void readCharArray(final char[] charArray, final InputStream inputStream) throws IOException
//   {
//      for(int i = 0; i < charArray.length; i++)
//      {
//         charArray[i] = LoaderMD2.readChar(inputStream);
//      }
//   }
//
//   static float readFloat(final InputStream inputStream) throws IOException
//   {
//      return Float.intBitsToFloat(LoaderMD2.readInt(inputStream));
//   }
//
//   static int readInt(final InputStream inputStream) throws IOException
//   {
//      return inputStream.read() | (inputStream.read() << 8) | (inputStream.read() << 16) | (inputStream.read() << 24);
//   }
//
//   static short readShort(final InputStream inputStream) throws IOException
//   {
//      return (short) (inputStream.read() | (inputStream.read() << 8));
//   }
//
//   public static final void main(final String[] args)
//   {
//      final JHelpFrame3D frame3d = new JHelpFrame3D("Test");
//
//      final JHelpSceneRenderer sceneRenderer = frame3d.getSceneRenderer();
//      sceneRenderer.registerJHelpSceneRendererListener(new JHelpSceneRendererListener()
//      {
//         @Override
//         public void sceneRendererIsInitialized(final JHelpSceneRenderer sceneRenderer)
//         {
//            final File file = new File("/home/jhelp/Documents/3D/Models/tentus/sidebarrel/tris.md2");
//
//            final LoaderMD2 loaderMD2 = new LoaderMD2();
//
//            try
//            {
//               loaderMD2.load(new FileInputStream(file));
//
//               Debug.println(DebugLevel.VERBOSE, "Number of objects : ", loaderMD2.numberOfObject());
//
//               final NodeWithMaterial nodeWithMaterial = loaderMD2.obtainObject(0);
//
//               // nodeWithMaterial.getMaterial().setTwoSided(true);
//
//               try
//               {
//                  Texture texture = new Texture("skin", Texture.REFERENCE_IMAGE, new FileInputStream(new File("/home/jhelp/Documents/3D/Models/tentus/sidebarrel/skin.jpg")));
//
//                  texture.brighter(0.1f);
//
//                  texture = Tool3D.obtainBumpTexture(texture, texture, 0.75f, 0.05f, 1, 1);
//
//                  nodeWithMaterial.getMaterial().setTextureDiffuse(texture);
//                  nodeWithMaterial.getMaterial().setColorDiffuse(Color4f.WHITE);
//                  nodeWithMaterial.getMaterial().setColorEmissive(Color4f.WHITE);
//               }
//               catch(final IOException exception)
//               {
//                  // {@todo} TODO Check if print exception is enough
//                  Debug.printTodo("Check if print exception is enough");
//                  Debug.printException(exception);
//
//                  nodeWithMaterial.getMaterial().setColorDiffuse(Color4f.RED);
//               }
//
//               sceneRenderer.getScene().add(nodeWithMaterial);
//
//               for(final String name : loaderMD2.texturesList())
//               {
//                  Debug.println(DebugLevel.INFORMATION, "Texture : ", name);
//               }
//            }
//            catch(final FileNotFoundException exception)
//            {
//               // {@todo} TODO Check if print exception is enough
//               Debug.printTodo("Check if print exception is enough");
//               Debug.printException(exception);
//            }
//            catch(final IOException exception)
//            {
//               // {@todo} TODO Check if print exception is enough
//               Debug.printTodo("Check if print exception is enough");
//               Debug.printException(exception);
//            }
//
//            sceneRenderer.getScene().flush();
//
//            Debug.printMark(DebugLevel.INFORMATION, "Let's rock !");
//         }
//      });
//
//      frame3d.setVisible(true);
//
//   }
//
//   private Frame[]              frames;
//   private final HeaderMd2      headerMd2;
//   private Object3D[]           objects;
//   private int[]                openglCommands;
//   private TextureCoordinate[]  textureCoordinates;
//   private TextureInformation[] textureInformations;
//   private Triangle[]           triangles;
//
//   public LoaderMD2()
//   {
//      this.headerMd2 = new HeaderMd2();
//   }
//
//   public void load(final InputStream inputStream) throws IOException
//   {
//      final IndexedInputStream indexedInputStream = new IndexedInputStream(inputStream);
//
//      this.headerMd2.read(indexedInputStream);
//
//      this.objects = new Object3D[this.headerMd2.num_frames];
//
//      this.textureInformations = new TextureInformation[this.headerMd2.num_skins];
//      this.textureCoordinates = new TextureCoordinate[this.headerMd2.num_st];
//      this.triangles = new Triangle[this.headerMd2.num_tris];
//      this.openglCommands = new int[this.headerMd2.num_glcmds];
//      this.frames = new Frame[this.headerMd2.num_frames];
//
//      TextureInformation textureInformation;
//      TextureCoordinate textureCoordinate;
//      Triangle triangle;
//      Frame frame;
//
//      long index;
//
//      while(indexedInputStream.isFinish() == false)
//      {
//         index = indexedInputStream.getCurrentIndex();
//
//         if((index == this.headerMd2.offset_skins) && (this.headerMd2.num_skins > 0))
//         {
//            for(int i = 0; i < this.headerMd2.num_skins; i++)
//            {
//               textureInformation = new TextureInformation();
//               textureInformation.read(indexedInputStream);
//
//               this.textureInformations[i] = textureInformation;
//            }
//         }
//         else if((index == this.headerMd2.offset_st) && (this.headerMd2.num_st > 0))
//         {
//            for(int i = 0; i < this.headerMd2.num_st; i++)
//            {
//               textureCoordinate = new TextureCoordinate();
//               textureCoordinate.read(indexedInputStream);
//               textureCoordinate.convert(this.headerMd2);
//
//               this.textureCoordinates[i] = textureCoordinate;
//            }
//         }
//         else if((index == this.headerMd2.offset_tris) && (this.headerMd2.num_tris > 0))
//         {
//            for(int i = 0; i < this.headerMd2.num_tris; i++)
//            {
//               triangle = new Triangle();
//               triangle.read(indexedInputStream);
//
//               this.triangles[i] = triangle;
//            }
//         }
//         else if((index == this.headerMd2.offset_glcmds) && (this.headerMd2.num_glcmds > 0))
//         {
//            for(int i = 0; i < this.headerMd2.num_glcmds; i++)
//            {
//               this.openglCommands[i] = LoaderMD2.readInt(indexedInputStream);
//            }
//         }
//         else if((index == this.headerMd2.offset_frames) && (this.headerMd2.num_frames > 0))
//         {
//            for(int f = 0; f < this.headerMd2.num_frames; f++)
//            {
//               frame = new Frame(this.headerMd2.num_vertices);
//               frame.read(indexedInputStream);
//               frame.transform();
//
//               this.frames[f] = frame;
//            }
//         }
//         else
//         {
//            indexedInputStream.read();
//         }
//      }
//   }
//
//   public int numberOfObject()
//   {
//      return this.headerMd2.num_frames;
//   }
//
//   public NodeWithMaterial obtainObject(final int index)
//   {
//      NodeWithMaterial nodeWithMaterial = null;
//
//      Object3D object = this.objects[index];
//
//      if(object != null)
//      {
//         nodeWithMaterial = new ObjectClone(object);
//      }
//      else
//      {
//         object = new Object3D();
//         final Frame frame = this.frames[index];
//
//         Point3D position;
//         Point3D normal;
//         Point2D uv;
//         float[] normalCoor;
//         VectorMD2 vectorMD2;
//         Vertice vertice;
//         Triangle triangle;
//         TextureCoordinate textureCoordinate;
//
//         for(int t = 0; t < this.headerMd2.num_tris; t++)
//         {
//            triangle = this.triangles[t];
//
//            for(int i = 0; i < 3; i++)
//            {
//               textureCoordinate = this.textureCoordinates[triangle.firstUVIndex & 0xFFFF];
//               uv = new Point2D(textureCoordinate.realU, textureCoordinate.realV);
//
//               vertice = frame.vertices[triangle.firstPointIndex & 0xFFFF];
//               normalCoor = LoaderMD2.fixedNormals[vertice.normalIndex & 0xFF];
//               normal = new Point3D(normalCoor[0], normalCoor[1], normalCoor[2]);
//
//               vectorMD2 = frame.transformed[triangle.firstPointIndex & 0xFFFF];
//               position = new Point3D(vectorMD2.x, vectorMD2.y, vectorMD2.z);
//
//               object.addFast(new Vertex(position, uv, normal));
//
//               //
//
//               textureCoordinate = this.textureCoordinates[triangle.secondUVIndex & 0xFFFF];
//               uv = new Point2D(textureCoordinate.realU, textureCoordinate.realV);
//
//               vertice = frame.vertices[triangle.secondPointIndex & 0xFFFF];
//               normalCoor = LoaderMD2.fixedNormals[vertice.normalIndex & 0xFF];
//               normal = new Point3D(normalCoor[0], normalCoor[1], normalCoor[2]);
//
//               vectorMD2 = frame.transformed[triangle.secondPointIndex & 0xFFFF];
//               position = new Point3D(vectorMD2.x, vectorMD2.y, vectorMD2.z);
//
//               object.addFast(new Vertex(position, uv, normal));
//
//               //
//
//               textureCoordinate = this.textureCoordinates[triangle.thirdUVIndex & 0xFFFF];
//               uv = new Point2D(textureCoordinate.realU, textureCoordinate.realV);
//
//               vertice = frame.vertices[triangle.thirdPointIndex & 0xFFFF];
//               normalCoor = LoaderMD2.fixedNormals[vertice.normalIndex & 0xFF];
//               normal = new Point3D(normalCoor[0], normalCoor[1], normalCoor[2]);
//
//               vectorMD2 = frame.transformed[triangle.thirdPointIndex & 0xFFFF];
//               position = new Point3D(vectorMD2.x, vectorMD2.y, vectorMD2.z);
//
//               object.addFast(new Vertex(position, uv, normal));
//            }
//
//            if((t + 1) < this.headerMd2.num_tris)
//            {
//               object.nextFace();
//            }
//         }
//
//         object.flush();
//
//         this.objects[index] = object;
//         nodeWithMaterial = object;
//      }
//
//      return nodeWithMaterial;
//   }
//
//   public EnumerationIterator<String> texturesList()
//   {
//      final String[] list = new String[this.textureInformations.length];
//
//      for(int i = 0; i < this.textureInformations.length; i++)
//      {
//         list[i] = new String(this.textureInformations[i].name);
//      }
//
//      return new EnumerationIterator<String>(list);
//   }
//}