///*
// * Copyright (c) 2003-2005 Sun Microsystems, Inc. All Rights Reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are
// * met:
// *
// * - Redistribution of source code must retain the above copyright
// *   notice, this list of conditions and the following disclaimer.
// *
// * - Redistribution in binary form must reproduce the above copyright
// *   notice, this list of conditions and the following disclaimer in the
// *   documentation and/or other materials provided with the distribution.
// *
// * Neither the name of Sun Microsystems, Inc. or the names of
// * contributors may be used to endorse or promote products derived from
// * this software without specific prior written permission.
// *
// * This software is provided "AS IS," without a warranty of any kind. ALL
// * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
// * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
// * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
// * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
// * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
// * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
// * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
// * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
// * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
// * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
// * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
// *
// * You acknowledge that this software is not designed or intended for use
// * in the design, construction, operation or maintenance of any nuclear
// * facility.
// *
// * Sun gratefully acknowledges that this software was originally authored
// * and developed by Kenneth Bradley Russell and Christopher John Kline.
// */
//
//package automenta.spacegraph.demo.spacegraph.old;
//
//
//import com.jogamp.opengl.GL;
//import com.jogamp.opengl.GLException;
//import com.jogamp.opengl.util.texture.Texture;
//import com.jogamp.opengl.util.texture.TextureData;
//import com.jogamp.opengl.util.texture.TextureIO;
//import org.encog.util.file.FileUtil;
//
//import java.io.IOException;
//
///** Helper class for loading cubemaps from a set of textures. */
//
//public class Cubemap {
//
//  private static final String[] suffixes = { "posx", "negx", "posy", "negy", "posz", "negz" };
//  private static final int[] targets = { GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X,
//                                         GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
//                                         GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
//                                         GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
//                                         GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
//                                         GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z };
//
//  public static Texture loadFromStreams(ClassLoader scope,
//                                        String basename,
//                                        String suffix,
//                                        boolean mipmapped) throws IOException, GLException {
//    Texture cubemap = TextureIO.newTexture(GL.GL_TEXTURE_CUBE_MAP);
//
//    for (int i = 0; i < suffixes.length; i++) {
//      String resourceName = basename + suffixes[i] + "." + suffix;
//      TextureData data = TextureIO.newTextureData(scope.getResourceAsStream(resourceName),
//                                                  mipmapped,
//                                                  FileUtil.getFileSuffix(resourceName));
//      if (data == null) {
//        throw new IOException("Unable to load texture " + resourceName);
//      }
//      cubemap.updateImage(data, targets[i]);
//    }
//
//    return cubemap;
//  }
//}
