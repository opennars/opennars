///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package automenta.spacegraph.physics;
//
//import automenta.spacegraph.math.linalg.Vec4f;
//import com.bulletphysics.demos.opengl.IGL;
//import com.bulletphysics.dynamics.RigidBody;
//import com.sun.opengl.util.awt.TextRenderer;
//import java.awt.Font;
//import java.awt.geom.Rectangle2D;
//import java.util.LinkedList;
//import java.util.List;
//import javax.media.opengl.GL2;
//import javax.vecmath.Vector3f;
//
///**
// *
// * @author seh
// */
//public interface BodyControl {
//
//    public interface BodyReaction {
//
//        public void onPress();
//    }
//
//    public Vector3f getSurfaceColor();
//
//    public void update(RigidBody r, double dt);
//
//    public void draw(IGL gl);
//
//    public List<BodyReaction> getReactions();
//
//    public static class AbstractBodyControl implements BodyControl {
//
//        private Vector3f surfaceColor;
//        private final List<BodyReaction> reactions = new LinkedList();
//
//        public AbstractBodyControl(Vector3f surfaceColor) {
//            super();
//            setColor(surfaceColor);
//        }
//
//        public void setColor(Vector3f color) {
//            this.surfaceColor = color;
//        }
//
//        @Override
//        public Vector3f getSurfaceColor() {
//            return surfaceColor;
//        }
//
//        @Override
//        public void update(RigidBody r, double dt) {
//        }
//
//        @Override
//        public void draw(IGL gl) {
//        }
//
//        @Override
//        public List<BodyReaction> getReactions() {
//            return reactions;
//        }
//    }
//
//    /** draws text on the front of a shape */
//    public static class TextScreen extends AbstractBodyControl {
//
//        private TextRenderer textRenderer;
//        private float textScaleFactor;
//        private String text;
//        private boolean useVertexArrays = false;
//        private Vec4f textColor = new Vec4f(1f, 1f, 1f, 1f);
//
//        public TextScreen(Vector3f backgroundColor, Vector3f textColor, String initialText) {
//            super(backgroundColor);
//
//            setText(initialText);
//
//            if (textRenderer == null) {
//                textRenderer = new TextRenderer(new Font("Arial", Font.PLAIN, 72));
//            }
//
//        }
//
//        public void setText(String text) {
//            this.text = text;
//        }
//
//        @Override
//        public void draw(IGL gl) {
//            super.draw(gl);
//
//
//            textRenderer.setSmoothing(false);
//            textRenderer.setUseVertexArrays(useVertexArrays);
//
//            // Compute the scale factor of the largest string which will make
//            // them all fit on the faces of the cube
//            Rectangle2D bounds = textRenderer.getBounds("Bottom");
//            float w = (float) bounds.getWidth();
//            float h = (float) bounds.getHeight();
//            textScaleFactor = 1.0f / (w * 1.1f);
//
//
//            gl.glPushMatrix();
//            gl.glTranslatef(0f, 0f, 0.6f);
//            //transform(gl);
//
//            // Now draw the overlaid text. In this setting, we don't want the
//            // text on the backward-facing faces to be visible, so we enable
//            // back-face culling; and since we're drawing the text over other
//            // geometry, to avoid z-fighting we disable the depth test. We
//            // could plausibly also use glPolygonOffset but this is simpler.
//            // Note that because the TextRenderer pushes the enable state
//            // internally we don't have to reset the depth test or cull face
//            // bits after we're done.
//            textRenderer.begin3DRendering();
//            gl.glEnable(GL2.GL_DEPTH_TEST);
//            gl.glEnable(GL2.GL_CULL_FACE);
//
//            // Note that the defaults for glCullFace and glFrontFace are
//            // GL_BACK and GL_CCW, which match the TextRenderer's definition
//            // of front-facing text.
//            bounds = textRenderer.getBounds(text);
//            w = (float) bounds.getWidth();
//            h = (float) bounds.getHeight();
//            textRenderer.setColor(textColor.x(), textColor.y(), textColor.z(), 1f);
//            textRenderer.draw3D(text,
//                    w / -2.0f * textScaleFactor,
//                    h / -2.0f * textScaleFactor,
//                    0.1f,
//                    textScaleFactor);
//            textRenderer.end3DRendering();
//
//            gl.glPopMatrix();
//
//        }
//
//        public static TextRenderer newTextRenderer(Font font) {
//            TextRenderer textRenderer = new TextRenderer(font);
//            return textRenderer;
//        }
//    }
//
//    /** draws a 2D matrix on the front of a shape */
//    public static class MatrixScreen extends AbstractBodyControl {
//
//        private Vector3f[][] bitmap;
//        private float pixelRatio;
//
//        public MatrixScreen(Vector3f color, Vector3f[][] bitmap) {
//            super(color);
//            setPixelRatio(1.0f);
//            setBitmap(bitmap);
//        }
//
//        public synchronized void setBitmap(Vector3f[][] bitmap) {
//            this.bitmap = bitmap;
//        }
//
//        public void setPixelRatio(float pixelRatio) {
//            this.pixelRatio = Math.max(0.0f, Math.min(pixelRatio, 1.0f));
//        }
//
//        public float getPixelRatio() {
//            return pixelRatio;
//        }
//        final float zMargin = 0.1f;
//
//        @Override
//        public void draw(IGL gl) {
//            super.draw(gl);
//
//            float px;
//
//            gl.glTranslatef(0, 0, 0.5f + zMargin);
//
//            int height = bitmap.length;
//
//            final float h = 0.5f / ((float) height);
//            float py = -0.5f + h / 2.0f;
//
//            final float pr = getPixelRatio();
//
//            for (int y = 0; y < height; y++) {
//
//                final int width = bitmap[y].length;
//                final float w = 0.5f / ((float) width);
//
//                px = -0.5f + w / 2.0f;
//
//                final float aw = w * pr;
//                final float ah = h * pr;
//
//                for (int x = 0; x < width; x++) {
//
//                    final Vector3f bc = bitmap[x][y];
//                    gl.glColor3f(bc.x, bc.y, bc.z);
//                    gl.glBegin(GL2.GL_QUADS);
//                    {
//                        //Front
//                        //gl.glNormal3f(0, 0, 1); {
//                        gl.glVertex3f(-aw + px, -ah + py, 0);
//                        gl.glVertex3f(aw + px, -ah + py, 0);
//                        gl.glVertex3f(aw + px, ah + py, 0);
//                        gl.glVertex3f(-aw + px, ah + py, 0);
//                        //}
//                    }
//                    gl.glEnd();
//
//                    px += w * 2.0f;
//
//                }
//
//                py += h * 2.0f;
//            }
//
//
//            drawFront(gl);
//
//        }
//
//        /** draw within -1..+1 for x, y */
//        protected void drawFront(IGL gl) {
//        }
//    }
//
//    //GridRetina(int pixelwidth, int pixelheight, float minUpdateInterval, float minDisplayInterval, float pixelProbability, floatDisplayScale)
//        //minUpdateInterval = 0, don't update
//        //minDisplayInterval = 0, don't display
//
//        //displayScale = 0: dont display
//        //displayScale = 1.0 : display in full resolution (using MatrixScreen)
//        // 0 < x < 1.0 : display at scaled resolution (rounded to integer measurement)
//
//        //setVisibleDistance(min, max, curve) - adjusts range window: min=black, max=white
//            //curve = ( linear | logarithmic | exponential )
//
//}
