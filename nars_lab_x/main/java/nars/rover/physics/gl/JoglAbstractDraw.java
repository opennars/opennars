/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package nars.rover.physics.gl;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.awt.TextRenderer;
import nars.Video;
import nars.rover.physics.PhysicsCamera;
import nars.rover.physics.j2d.SwingDraw;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.pooling.arrays.Vec2Array;

import java.util.concurrent.CopyOnWriteArrayList;

public abstract class JoglAbstractDraw extends DebugDraw {

    private JoglAbstractPanel panel;
    private final TextRenderer text;
    private static final int NUM_CIRCLE_POINTS = 13;
    public final java.util.List<SwingDraw.LayerDraw> layers = new CopyOnWriteArrayList();

    Transform xf = new Transform();


    public JoglAbstractDraw() {
        super(new OBBViewportTransform());
        text = new TextRenderer(Video.monofont);

        viewportTransform.setCamera(0, 0, 50.0f);
    }

    public void setPanel(JoglAbstractPanel panel) {

        this.panel = panel;
    }

    public void addLayer(SwingDraw.LayerDraw l) {
        layers.add(l);
    }

    public void removeLayer(SwingDraw.LayerDraw l) {
        layers.remove(l);
    }

    public void draw(World w, float time) {



        if (w == null) return;

        PhysicsCamera p = getPhysicsCamera();

        if (p != null) {
            Vec2 center = p.getTransform().getCenter();

            viewportTransform.setCenter(center);
            viewportTransform.setExtents(p.getTargetScale(), p.getTargetScale());
        }

        for (SwingDraw.LayerDraw l : layers) l.drawGround(this, w);

        int flags = getFlags();
        //boolean wireframe = (flags & DebugDraw.e_wireframeDrawingBit) != 0;

        //if ((flags & DebugDraw.e_shapeBit) != 0) {
        for (Body b = w.getBodyList(); b != null; b = b.getNext()) {
            drawBody(b, time);
        }
        //drawParticleSystem(m_particleSystem);
        //}

        //if ((flags & DebugDraw.e_jointBit) != 0) {

        for (Joint j = w.getJointList(); j != null; j = j.getNext()) {
            //drawJoint(j);
        }
        //}


        for (SwingDraw.LayerDraw l : layers) l.drawSky(this, w);

        //flush();

    }




    Color3f defaultFillColor = new Color3f(0.75f, 0.75f, 0.75f);
    Color3f defaultStrokeColor = new Color3f(1, 1, 1);
    Stroke defaultStroke = new BasicStroke(2);

    Stroke stroke = defaultStroke;
    Color3f fillColor = defaultFillColor;
    Color3f strokeColor = defaultStrokeColor;

    public void setStrokeColor(Color3f strokeColor) {
        this.strokeColor = strokeColor;
    }

    public void setFillColor(Color3f fillColor) {
        this.fillColor = fillColor;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }



    public interface DrawProperty {
        public void before(Body b, JoglAbstractDraw d, float time);
    }


    void drawBody(Body b, float time) {
        boolean wireframe = false;

        Object o = b.getUserData();
        if (o instanceof JoglAbstractDraw.DrawProperty) {
            JoglAbstractDraw.DrawProperty d = (JoglAbstractDraw.DrawProperty) o;
            d.before(b, this, time);
        } else {
            strokeColor = defaultStrokeColor;
            fillColor = defaultFillColor;
            stroke = defaultStroke;
        }


        xf.set(b.getTransform());

        for (Fixture f = b.getFixtureList(); f != null; f = f.getNext()) {
            if (b.isActive() == false) {
                drawShape(f, xf, fillColor, wireframe);
            } else if (b.getType() == BodyType.STATIC) {
                drawShape(f, xf, fillColor, wireframe);
            } else if (b.getType() == BodyType.KINEMATIC) {
                drawShape(f, xf, fillColor, wireframe);
            } else if (b.isAwake() == false) {
                drawShape(f, xf, fillColor, wireframe);
            } else {
                drawShape(f, xf, fillColor, wireframe);
            }
        }
    }

    private final Vec2 center = new Vec2();
    private final Vec2 axis = new Vec2();
    private final Vec2 v1 = new Vec2();
    private final Vec2 v2 = new Vec2();
    private final Vec2Array tlvertices = new Vec2Array();

    private void drawShape(Fixture fixture, Transform xf, Color3f color, boolean wireframe) {

        switch (fixture.getType()) {
            case CIRCLE: {
                CircleShape circle = (CircleShape) fixture.getShape();

                // Vec2 center = Mul(xf, circle.m_p);
                Transform.mulToOutUnsafe(xf, circle.m_p, center);
                float radius = circle.m_radius;
                xf.q.getXAxis(axis);

                /*
                 if (fixture.getUserData() != null && fixture.getUserData().equals(LIQUID_INT)) {
                 Body b = fixture.getBody();
                 liquidOffset.set(b.m_linearVelocity);
                 float linVelLength = b.m_linearVelocity.length();
                 if (averageLinearVel == -1) {
                 averageLinearVel = linVelLength;
                 } else {
                 averageLinearVel = .98f * averageLinearVel + .02f * linVelLength;
                 }
                 liquidOffset.mulLocal(liquidLength / averageLinearVel / 2);
                 circCenterMoved.set(center).addLocal(liquidOffset);
                 center.subLocal(liquidOffset);
                 drawSegment(center, circCenterMoved, liquidColor);
                 return;
                 }
                 */
                if (wireframe) {
                    drawCircle(center, radius, axis, color);
                } else {
                    drawSolidCircle(center, radius, axis, color);
                }
            }
            break;

            case POLYGON: {
                PolygonShape poly = (PolygonShape) fixture.getShape();
                int vertexCount = poly.m_count;
                assert (vertexCount <= Settings.maxPolygonVertices);
                Vec2[] vertices = tlvertices.get(Settings.maxPolygonVertices);


                for (int i = 0; i < vertexCount; ++i) {
                    // vertices[i] = Mul(xf, poly.m_vertices[i]);
                    Transform.mulToOutUnsafe(xf, poly.m_vertices[i], vertices[i]);
                }
                if (wireframe) {
                    drawPolygon(vertices, vertexCount, color);
                } else {
                    drawSolidPolygon(vertices, vertexCount, color);
                }
            }
            break;
            case EDGE: {
                EdgeShape edge = (EdgeShape) fixture.getShape();
                Transform.mulToOutUnsafe(xf, edge.m_vertex1, v1);
                Transform.mulToOutUnsafe(xf, edge.m_vertex2, v2);
                drawSegment(v1, v2, color);
            }
            break;
            case CHAIN: {
                ChainShape chain = (ChainShape) fixture.getShape();
                int count = chain.m_count;
                Vec2[] vertices = chain.m_vertices;

                Transform.mulToOutUnsafe(xf, vertices[0], v1);
                for (int i = 1; i < count; ++i) {
                    Transform.mulToOutUnsafe(xf, vertices[i], v2);
                    drawSegment(v1, v2, color);
                    drawCircle(v1, 0.05f, color);
                    v1.set(v2);
                }
            }
            break;
            default:
                break;
        }
    }


    @Override
    public IViewportTransform getViewportTranform() {
        return super.getViewportTranform();
    }

    //
    public void transformViewport(GL2 gl, Vec2 center) {

        Vec2 e = viewportTransform.getExtents();
        Vec2 vc = viewportTransform.getCenter();
        //Mat22 vt = viewportTransform.getMat22Representation();
        Vec2 ee = viewportTransform.getExtents();

//    int f = viewportTransform.isYFlip() ? -1 : 1;
//    mat[0] = exx;//vt.ex.x;
//    mat[4] = eyx; //vt.ey.x;
//    // mat[8] = 0;
//    mat[12] = e.x;
//    mat[1] = f * exy; //vt.ex.y;
//    mat[5] = f * eyy; //vt.ey.y;
//    // mat[9] = 0;
//    mat[13] = e.y;
//    // mat[2] = 0;
//    // mat[6] = 0;
//    // mat[10] = 1;
//    // mat[14] = 0;
//    // mat[3] = 0;
//    // mat[7] = 0;
//    // mat[11] = 0;
//    // mat[15] = 1;

        //gl.glMultMatrixf(mat, 0);
        gl.glLoadIdentity();

        Vec2 scale = viewportTransform.getExtents();
        Vec2 translate = new Vec2(center.x - vc.x, center.y - vc.y);

        gl.glScalef(scale.x, scale.y, 1f);
        gl.glTranslatef(translate.x, translate.y, 0);
    }

    @Override
    public void drawPoint(Vec2 argPoint, float argRadiusOnScreen, Color3f argColor) {
        Vec2 vec = getWorldToScreen(argPoint);
        GL2 gl = panel.getGL().getGL2();
        gl.glPointSize(argRadiusOnScreen);
        gl.glBegin(GL2.GL_POINTS);
        gl.glVertex2f(vec.x, vec.y);
        gl.glEnd();
    }

    public static final Vec2 zero = new Vec2();

    @Override
    public void drawPolygon(Vec2[] vertices, int vertexCount, Color3f color) {
        GL2 gl = panel.getGL().getGL2();
        gl.glPushMatrix();
        transformViewport(gl, zero);
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glColor4f(color.x, color.y, color.z, 1f);
        for (int i = 0; i < vertexCount; i++) {
            Vec2 v = vertices[i];
            gl.glVertex2f(v.x, v.y);
        }
        gl.glEnd();
        gl.glPopMatrix();
    }

    public void drawSolidRect(float px, float py, float w, float h, float r, float G, float b) {
        //saveState(g);

//        getWorldToScreenToOut(px, py, temp);
//        int ipx = (int)temp.x;  int ipy = (int)temp.y;
//        getWorldToScreenToOut(px+w, py+h, temp);
//
//        int jpx = (int)temp.x;  int jpy = (int)temp.y;
//        int iw = Math.abs(jpx - ipx);
//        int ih = Math.abs(jpy - ipy);

//        if ((ipy/2 > g.getDeviceConfiguration().getBounds().getHeight()) ||
//                (ipx/2 > g.getDeviceConfiguration().getBounds().getWidth()))
//                    return;

//        g.setColor(new Color(r, G, b));
//        g.fillRect(ipx-iw/2, ipy-ih/2, iw, ih);

        //if (g.getDeviceConfiguration().getBounds().intersects(ipx-iw/2, ipy-ih/2, iw, ih)) {
        //}

        Vec2[] vert = new Vec2[4];
        //float ulx = ipx-iw/2, uly = ipy-ih/2;
        float ulx = px, uly = py;
        w/=2;
        h/=2;
        vert[0] = new Vec2(ulx - w, uly - h);
        vert[1] = new Vec2(ulx + w, uly - h);
        vert[2] = new Vec2(ulx + w, uly + h);
        vert[3] = new Vec2(ulx - w, uly + h);
        drawSolidPolygon(vert, 4, new Color3f(r, G, b));
    }

    @Override
    public void drawSolidPolygon(Vec2[] vertices, int vertexCount, Color3f color) {
        GL2 gl = panel.getGL().getGL2();
        gl.glPushMatrix();
        transformViewport(gl, zero);
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glColor4f(color.x, color.y, color.z, .8f);
        for (int i = 0; i < vertexCount; i++) {
            Vec2 v = vertices[i];
            gl.glVertex2f(v.x, v.y);
        }
        gl.glEnd();

        //OUTLINE
        /*
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glColor4f(color.x, color.y, color.z, 1f);
        for (int i = 0; i < vertexCount; i++) {
            Vec2 v = vertices[i];
            gl.glVertex2f(v.x, v.y);
        }
        gl.glEnd();
        gl.glPopMatrix();
        */
    }

    @Override
    public void drawCircle(Vec2 center, float radius, Color3f color) {
        GL2 gl = panel.getGL().getGL2();
        gl.glPushMatrix();
        transformViewport(gl, zero);
        float theta = 2 * MathUtils.PI / NUM_CIRCLE_POINTS;
        float c = MathUtils.cos(theta);
        float s = MathUtils.sin(theta);
        float x = radius;
        float y = 0;
        float cx = center.x;
        float cy = center.y;
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glColor4f(color.x, color.y, color.z, 1);
        for (int i = 0; i < NUM_CIRCLE_POINTS; i++) {
            gl.glVertex3f(x + cx, y + cy, 0);
            // apply the rotation matrix
            float temp = x;
            x = c * x - s * y;
            y = s * temp + c * y;
        }
        gl.glEnd();
        gl.glPopMatrix();
    }


    public void drawCircle(Vec2 center, float radius, Vec2 axis, Color3f color) {
        GL2 gl = panel.getGL().getGL2();
        gl.glPushMatrix();
        transformViewport(gl, zero);
        float theta = 2 * MathUtils.PI / NUM_CIRCLE_POINTS;
        float c = MathUtils.cos(theta);
        float s = MathUtils.sin(theta);
        float x = radius;
        float y = 0;
        float cx = center.x;
        float cy = center.y;
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glColor4f(color.x, color.y, color.z, 1);
        for (int i = 0; i < NUM_CIRCLE_POINTS; i++) {
            gl.glVertex3f(x + cx, y + cy, 0);
            // apply the rotation matrix
            float temp = x;
            x = c * x - s * y;
            y = s * temp + c * y;
        }
        gl.glEnd();
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(cx, cy, 0);
        gl.glVertex3f(cx + axis.x * radius, cy + axis.y * radius, 0);
        gl.glEnd();
        gl.glPopMatrix();
    }

    @Override
    public void drawSolidCircle(Vec2 center, float radius, Vec2 axis, Color3f color) {
        GL2 gl = panel.getGL().getGL2();
        gl.glPushMatrix();
        transformViewport(gl, zero);
        float theta = 2 * MathUtils.PI / NUM_CIRCLE_POINTS;
        float c = MathUtils.cos(theta);
        float s = MathUtils.sin(theta);
        float x = radius;
        float y = 0;
        float cx = center.x;
        float cy = center.y;
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glColor4f(color.x, color.y, color.z, .4f);
        for (int i = 0; i < NUM_CIRCLE_POINTS; i++) {
            gl.glVertex3f(x + cx, y + cy, 0);
            // apply the rotation matrix
            float temp = x;
            x = c * x - s * y;
            y = s * temp + c * y;
        }
        gl.glEnd();
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glColor4f(color.x, color.y, color.z, 1);
        for (int i = 0; i < NUM_CIRCLE_POINTS; i++) {
            gl.glVertex3f(x + cx, y + cy, 0);
            // apply the rotation matrix
            float temp = x;
            x = c * x - s * y;
            y = s * temp + c * y;
        }
        gl.glEnd();
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(cx, cy, 0);
        gl.glVertex3f(cx + axis.x * radius, cy + axis.y * radius, 0);
        gl.glEnd();
        gl.glPopMatrix();
    }

    @Override
    public void drawSegment(Vec2 p1, Vec2 p2, Color3f color) {
        GL2 gl = panel.getGL().getGL2();
        gl.glPushMatrix();
        transformViewport(gl, zero);
        gl.glBegin(GL2.GL_LINES);
        gl.glColor3f(color.x, color.y, color.z);
        gl.glVertex3f(p1.x, p1.y, 0);
        gl.glVertex3f(p2.x, p2.y, 0);
        gl.glEnd();
        gl.glPopMatrix();
    }
    public void drawSegment(Vec2 p1, Vec2 p2, float r, float g, float b, float a, float width) {
        GL2 gl = panel.getGL().getGL2();
        gl.glPushMatrix();
        transformViewport(gl, zero);
        gl.glLineWidth(width);
        gl.glBegin(GL2.GL_LINES);
        gl.glColor4f(r, g, b, a);
        gl.glVertex3f(p1.x, p1.y, 0);
        gl.glVertex3f(p2.x, p2.y, 0);
        gl.glEnd();
        gl.glPopMatrix();
    }

//  @Override
//  public void drawParticles(Vec2[] centers, float radius, ParticleColor[] colors, int count) {
//    GL2 gl = panel.getGL().getGL2();
//    gl.glPushMatrix();
//    transformViewport(gl, zero);
//
//    float theta = 2 * MathUtils.PI / NUM_CIRCLE_POINTS;
//    float c = MathUtils.cos(theta);
//    float s = MathUtils.sin(theta);
//
//    float x = radius;
//    float y = 0;
//
//    for (int i = 0; i < count; i++) {
//      Vec2 center = centers[i];
//      float cx = center.x;
//      float cy = center.y;
//      gl.glBegin(GL2.GL_TRIANGLE_FAN);
//      if (colors == null) {
//        gl.glColor4f(1, 1, 1, .4f);
//      } else {
//        ParticleColor color = colors[i];
//        gl.glColor4b(color.r, color.g, color.b, color.a);
//      }
//      for (int j = 0; j < NUM_CIRCLE_POINTS; j++) {
//        gl.glVertex3f(x + cx, y + cy, 0);
//        float temp = x;
//        x = c * x - s * y;
//        y = s * temp + c * y;
//      }
//      gl.glEnd();
//    }
//    gl.glPopMatrix();
//  }


//  @Override
//  public void drawParticlesWireframe(Vec2[] centers, float radius, ParticleColor[] colors, int count) {
//    GL2 gl = panel.getGL().getGL2();
//    gl.glPushMatrix();
//    transformViewport(gl, zero);
//
//    float theta = 2 * MathUtils.PI / NUM_CIRCLE_POINTS;
//    float c = MathUtils.cos(theta);
//    float s = MathUtils.sin(theta);
//
//    float x = radius;
//    float y = 0;
//
//    for (int i = 0; i < count; i++) {
//      Vec2 center = centers[i];
//      float cx = center.x;
//      float cy = center.y;
//      gl.glBegin(GL2.GL_LINE_LOOP);
//      if (colors == null) {
//        gl.glColor4f(1, 1, 1, 1);
//      } else {
//        ParticleColor color = colors[i];
//        gl.glColor4b(color.r, color.g, color.b, (byte) 127);
//      }
//      for (int j = 0; j < NUM_CIRCLE_POINTS; j++) {
//        gl.glVertex3f(x + cx, y + cy, 0);
//        float temp = x;
//        x = c * x - s * y;
//        y = s * temp + c * y;
//      }
//      gl.glEnd();
//    }
//    gl.glPopMatrix();
//  }

    private final Vec2 temp = new Vec2();
    private final Vec2 temp2 = new Vec2();

    @Override
    public void drawTransform(Transform xf) {
        GL2 gl = panel.getGL().getGL2();
        getWorldToScreenToOut(xf.p, temp);
        temp2.setZero();
        float k_axisScale = 0.4f;

        gl.glBegin(GL2.GL_LINES);
        gl.glColor3f(1, 0, 0);

        temp2.x = xf.p.x + k_axisScale * xf.q.c;
        temp2.y = xf.p.y + k_axisScale * xf.q.s;
        getWorldToScreenToOut(temp2, temp2);
        gl.glVertex2f(temp.x, temp.y);
        gl.glVertex2f(temp2.x, temp2.y);

        gl.glColor3f(0, 1, 0);
        temp2.x = xf.p.x + -k_axisScale * xf.q.s;
        temp2.y = xf.p.y + k_axisScale * xf.q.c;
        getWorldToScreenToOut(temp2, temp2);
        gl.glVertex2f(temp.x, temp.y);
        gl.glVertex2f(temp2.x, temp2.y);
        gl.glEnd();
    }

    @Override
    public void drawString(float x, float y, String s, Color3f color) {
        text.beginRendering(panel.getWidth(), panel.getHeight());
        text.setColor(color.x, color.y, color.z, 1);
        text.draw(s, (int) x, panel.getHeight() - (int) y);
        text.endRendering();
    }

    protected abstract PhysicsCamera getPhysicsCamera();
}