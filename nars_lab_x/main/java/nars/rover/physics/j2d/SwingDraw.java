/**
 * *****************************************************************************
 * Copyright (c) 2013, Daniel Murphy All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * ****************************************************************************
 */
package nars.rover.physics.j2d;

import nars.rover.physics.gl.JoglAbstractDraw;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.PulleyJoint;
import org.jbox2d.pooling.arrays.IntArray;
import org.jbox2d.pooling.arrays.Vec2Array;

import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;


public class SwingDraw extends DebugDraw {

    public static int circlePoints = 5;
    public static final float edgeWidth = 0.02f;

    private final TestPanelJ2D panel;
    private final boolean yFlip;
    
    private final Shape circle;
    Transform xf = new Transform();
    Color3f color = new Color3f();

    public final List<LayerDraw> layers = new ArrayList();
    private Graphics2D graphics;
    
    
    public SwingDraw(TestPanelJ2D argTestPanel, boolean yFlip) {
        super(new OBBViewportTransform());
        panel = argTestPanel;
        this.yFlip = yFlip;        
        circle = new Ellipse2D.Float(-1, -1, 2, 2);
    }

    public static interface PhyDrawable {
        public void draw(SwingDraw d);
    }
    


    public interface LayerDraw {
        public void drawGround(JoglAbstractDraw draw, World w);
        public void drawSky(JoglAbstractDraw draw, World w);
    }
    
    public void addLayer(LayerDraw l) {
        layers.add(l);
    }
    public void removeLayer(LayerDraw l) {
        layers.remove(l);
    }
    
    
    public void draw(World w) {
        
        graphics = panel.getDBGraphics();        

        
        
        
        //for (LayerDraw l : layers) l.drawGround(this, w);
        
        int flags = getFlags();
        //boolean wireframe = (flags & DebugDraw.e_wireframeDrawingBit) != 0;

        if ((flags & DebugDraw.e_shapeBit) != 0) {
            for (Body b = w.getBodyList(); b != null; b = b.getNext()) {
                drawBody(b);
            }
            //drawParticleSystem(m_particleSystem);
        }

        if ((flags & DebugDraw.e_jointBit) != 0) {

            for (Joint j = w.getJointList(); j != null; j = j.getNext()) {
                drawJoint(j);
            }
        }

//        if ((flags & DebugDraw.e_pairBit) != 0) {
//            color.set(0.3f, 0.9f, 0.9f);
//            for (Contact c = m_contactManager.m_contactList; c != null; c = c.getNext()) {
//                Fixture fixtureA = c.getFixtureA();
//                Fixture fixtureB = c.getFixtureB();
//                fixtureA.getAABB(c.getChildIndexA()).getCenterToOut(cA);
//                fixtureB.getAABB(c.getChildIndexB()).getCenterToOut(cB);
//                drawSegment(cA, cB, color);
//            }
//        }
//
//        if ((flags & DebugDraw.e_aabbBit) != 0) {
//            color.set(0.9f, 0.3f, 0.9f);
//
//            for (Body b = m_bodyList; b != null; b = b.getNext()) {
//                if (b.isActive() == false) {
//                    continue;
//                }
//
//                for (Fixture f = b.getFixtureList(); f != null; f = f.getNext()) {
//                    for (int i = 0; i < f.m_proxyCount; ++i) {
//                        FixtureProxy proxy = f.m_proxies[i];
//                        AABB aabb = m_contactManager.m_broadPhase.getFatAABB(proxy.proxyId);
//                        if (aabb != null) {
//                            Vec2[] vs = avs.get(4);
//                            vs[0].set(aabb.lowerBound.x, aabb.lowerBound.y);
//                            vs[1].set(aabb.upperBound.x, aabb.lowerBound.y);
//                            vs[2].set(aabb.upperBound.x, aabb.upperBound.y);
//                            vs[3].set(aabb.lowerBound.x, aabb.upperBound.y);
//                            drawPolygon(vs, 4, color);
//                        }
//                    }
//                }
//            }
//        }
//
//        if ((flags & DebugDraw.e_centerOfMassBit) != 0) {
//            for (Body b = m_bodyList; b != null; b = b.getNext()) {
//                xf.set(b.getTransform());
//                xf.p.set(b.getWorldCenter());
//                drawTransform(xf);
//            }
//        }
//
//        if ((flags & DebugDraw.e_dynamicTreeBit) != 0) {
//            m_contactManager.m_broadPhase.drawTree(m_debugDraw);
//        }

        //for (LayerDraw l : layers) l.drawSky(this, w);

        //flush();

    }
    public interface DrawProperty {
        public void before(Body b, SwingDraw d);
    }
    
    Color defaultFillColor =  new Color(0.75f, 0.75f, 0.75f);
    Color defaultStrokeColor =  new Color(1,1,1);
    Stroke defaultStroke = new BasicStroke(2);
    
    Stroke stroke = defaultStroke;    
    Color fillColor = defaultFillColor;
    Color strokeColor = defaultStrokeColor;

    public void setStrokeColor(Color strokeColor) {
        this.strokeColor = strokeColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }
    
    

    void drawBody(Body b) {
        boolean wireframe = false;
        
        Object o = b.getUserData();
        if (o instanceof DrawProperty) {
            DrawProperty d = (DrawProperty)o;
            d.before(b, this);
            
            if ((fillColor == null) && (stroke == null))
                return;
        }
        else {
            strokeColor = defaultStrokeColor;
            fillColor = defaultFillColor;
            stroke = defaultStroke;
        }
        
        
        
        xf.set(b.getTransform());
        
        for (Fixture f = b.getFixtureList(); f != null; f = f.getNext()) {
            if (b.isActive() == false) {
                color.set(0.5f, 0.5f, 0.3f);
                drawShape(f, xf, color, wireframe);
            } else if (b.getType() == BodyType.STATIC) {
                color.set(0.5f, 0.9f, 0.3f);
                drawShape(f, xf, color, wireframe);
            } else if (b.getType() == BodyType.KINEMATIC) {
                color.set(0.5f, 0.5f, 0.9f);
                drawShape(f, xf, color, wireframe);
            } else if (b.isAwake() == false) {
                color.set(0.5f, 0.5f, 0.5f);
                drawShape(f, xf, color, wireframe);
            } else {
                color.set(0.9f, 0.7f, 0.7f);
                drawShape(f, xf, color, wireframe);
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

    Vec2 p1 = new Vec2(); //pool.popVec2();
    Vec2 p2 = new Vec2(); //pool.popVec2();

    private void drawJoint(Joint joint) {
        Body bodyA = joint.getBodyA();
        Body bodyB = joint.getBodyB();
        Transform xf1 = bodyA.getTransform();
        Transform xf2 = bodyB.getTransform();
        Vec2 x1 = xf1.p;
        Vec2 x2 = xf2.p;
        joint.getAnchorA(p1);
        joint.getAnchorB(p2);

        color.set(0.5f, 0.8f, 0.8f);

        switch (joint.getType()) {
            // TODO djm write after writing joints
            case DISTANCE:
                drawSegment(p1, p2, color);
                break;

            case PULLEY: {
                PulleyJoint pulley = (PulleyJoint) joint;
                Vec2 s1 = pulley.getGroundAnchorA();
                Vec2 s2 = pulley.getGroundAnchorB();
                drawSegment(s1, p1, color);
                drawSegment(s2, p2, color);
                drawSegment(s1, s2, color);
            }
            break;
            case CONSTANT_VOLUME:
            case MOUSE:
                // don't draw this
                break;
            default:
                drawSegment(x1, p1, color);
                drawSegment(p1, p2, color);
                drawSegment(x2, p2, color);
        }
        //pool.pushVec2(2);
    }

    /*@Override
    public void setViewportTransform(IViewportTransform viewportTransform) {
        super.setViewportTransform(viewportTransform);
        viewportTransform.setYFlip(yFlip);
    }*/

    private final Vec2Array vec2Array = new Vec2Array();

    @Override
    public void drawPoint(Vec2 argPoint, float argRadiusOnScreen, Color3f argColor) {
        getWorldToScreenToOut(argPoint, sp1);
        Graphics2D g = getGraphics();

        Color c = new Color(argColor.x, argColor.y, argColor.z);
        g.setColor(c);
        sp1.x -= argRadiusOnScreen;
        sp1.y -= argRadiusOnScreen;
        g.fillOval((int) sp1.x, (int) sp1.y, (int) argRadiusOnScreen * 2, (int) argRadiusOnScreen * 2);
    }

    private final Vec2 sp1 = new Vec2();
    private final Vec2 sp2 = new Vec2();

    @Override public void drawSegment(Vec2 p1, Vec2 p2, Color3f color) {
        getWorldToScreenToOut(p1, sp1);
        getWorldToScreenToOut(p2, sp2);

        Color c = new Color(color.x, color.y, color.z);
        Graphics2D g = getGraphics();
        g.setColor(c);

        g.setStroke(stroke);
        g.drawLine((int) sp1.x, (int) sp1.y, (int) sp2.x, (int) sp2.y);
    }

    public void drawSegment(Vec2 p1, Vec2 p2, Color3f color, float alpha) {
        getWorldToScreenToOut(p1, sp1);
        getWorldToScreenToOut(p2, sp2);

        
        Color c = new Color(color.x, color.y, color.z, alpha);
        Graphics2D g = getGraphics();        
        g.setColor(c);
        g.setStroke(stroke);
        g.drawLine((int) sp1.x, (int) sp1.y, (int) sp2.x, (int) sp2.y);
    }
    
    public void drawAABB(AABB argAABB, Color3f color) {
        Vec2 vecs[] = vec2Array.get(4);
        argAABB.getVertices(vecs);
        drawPolygon(vecs, 4, color);
    }

    private final AffineTransform tr = new AffineTransform();
    private AffineTransform oldTrans = new AffineTransform();
    private Stroke oldStroke;

    private void saveState(Graphics2D g) {
        oldTrans = g.getTransform();
        oldStroke = g.getStroke();
    }

    private void restoreState(Graphics2D g) {
        g.setTransform(oldTrans);
        g.setStroke(oldStroke);
    }

    private void transformGraphics(Graphics2D g, Vec2 center) {
        Vec2 e = viewportTransform.getExtents();
        Vec2 vc = viewportTransform.getCenter();
        Mat22 vt = new Mat22(e, vc);//viewportTransform.getMat22Representation();

        int flip = yFlip ? -1 : 1;
        tr.setTransform(vt.ex.x, flip * vt.ex.y, vt.ey.x, flip * vt.ey.y, e.x, e.y);
        tr.translate(-vc.x, -vc.y);
        tr.translate(center.x, center.y);
        g.transform(tr);
    }

    @Override
    public void drawCircle(Vec2 center, float radius, Color3f color) {
        Graphics2D g = getGraphics();
        Color s = new Color(color.x, color.y, color.z, 1f);
        saveState(g);
        transformGraphics(g, center);
        g.setStroke(stroke);
        g.scale(radius, radius);
        g.setColor(s);
        g.drawOval(-1, -1, 2, 2);
        restoreState(g);
    }

    public void drawCircle(Vec2 center, float radius, Vec2 axis, Color3f color) {
        Graphics2D g = getGraphics();
        saveState(g);
        transformGraphics(g, center);
        g.setStroke(stroke);
        Color s = new Color(color.x, color.y, color.z, 1f);
        g.scale(radius, radius);
        g.setColor(s);
        g.draw(circle);
        g.rotate(MathUtils.atan2(axis.y, axis.x));
        if (axis != null) {
            g.drawLine(0, 0, 1, 0);
        }
        restoreState(g);
    }

    @Override
    public void drawSolidCircle(Vec2 center, float radius, Vec2 axis, Color3f color) {
        Graphics2D g = getGraphics();
        saveState(g);
        transformGraphics(g, center);
        g.setStroke(stroke);
        Color f = new Color(color.x, color.y, color.z, .4f);
        Color s = new Color(color.x, color.y, color.z, 1f);
        g.scale(radius, radius);
        g.setColor(f);
        g.fill(circle);
        g.setColor(s);
        g.draw(circle);
        g.rotate(MathUtils.atan2(axis.y, axis.x));
        if (axis != null) {
            g.drawLine(0, 0, 1, 0);
        }
        restoreState(g);
    }

    private final Vec2 zero = new Vec2();
    private final Color pcolorA = new Color(1f, 1f, 1f, .4f);

//    @Override
//    public void drawParticles(final Vec2[] centers, final float radius, ParticleColor[] colors, final int count) {
//        Graphics2D g = getGraphics();
//        saveState(g);
//        transformGraphics(g, zero);
//        g.setStroke(stroke);
//        for (int i = 0; i < count; i++) {
//            Vec2 center = centers[i];
//            Color color;
//            if (colors == null) {
//                color = pcolorA;
//            } else {
//                ParticleColor c = colors[i];
//                color = new Color(c.r * 1f / 127, c.g * 1f / 127, c.b * 1f / 127, c.a * 1f / 127);
//            }
//            AffineTransform old = g.getTransform();
//            g.translate(center.x, center.y);
//            g.scale(radius, radius);
//            g.setColor(color);
//            g.fill(circle);
//            g.setTransform(old);
//        }
//        restoreState(g);
//    }

    private final Color pcolor = new Color(1f, 1f, 1f, 1f);

//    @Override
//    public void drawParticlesWireframe(Vec2[] centers, float radius, ParticleColor[] colors, int count) {
//        Graphics2D g = getGraphics();
//        saveState(g);
//        transformGraphics(g, zero);
//        g.setStroke(stroke);
//        for (int i = 0; i < count; i++) {
//            Vec2 center = centers[i];
//            Color color;
//            // No alpha channel, it slows everything down way too much.
//            if (colors == null) {
//                color = pcolor;
//            } else {
//                ParticleColor c = colors[i];
//                color = new Color(c.r * 1f / 127, c.g * 1f / 127, c.b * 1f / 127, 1);
//            }
//            AffineTransform old = g.getTransform();
//            g.translate(center.x, center.y);
//            g.scale(radius, radius);
//            g.setColor(color);
//            g.draw(circle);
//            g.setTransform(old);
//        }
//        restoreState(g);
//    }

    private final Vec2 temp = new Vec2();
    private final static IntArray xIntsPool = new IntArray();
    private final static IntArray yIntsPool = new IntArray();

    public void drawSolidRect(float px, float py, float w, float h, float r, float G, float b) {
        Graphics2D g = getGraphics();
        //saveState(g);
                
        getWorldToScreenToOut(px, py, temp);
        int ipx = (int)temp.x;  int ipy = (int)temp.y;
        getWorldToScreenToOut(px+w, py+h, temp);
        
        int jpx = (int)temp.x;  int jpy = (int)temp.y;
        int iw = Math.abs(jpx - ipx);
        int ih = Math.abs(jpy - ipy);
          
//        if ((ipy/2 > g.getDeviceConfiguration().getBounds().getHeight()) ||
//                (ipx/2 > g.getDeviceConfiguration().getBounds().getWidth()))
//                    return;
        
        g.setColor(new Color(r, G, b));
        g.fillRect(ipx-iw/2, ipy-ih/2, iw, ih);

        //if (g.getDeviceConfiguration().getBounds().intersects(ipx-iw/2, ipy-ih/2, iw, ih)) {
        //}        
        
        //restoreState(g);        
    }

    @Override
    public void drawSolidPolygon(Vec2[] vertices, int vertexCount, Color3f color) {
        Color s = strokeColor;
        Color f = fillColor;
        Graphics2D g = getGraphics();
        //saveState(g);
        int[] xInts = xIntsPool.get(vertexCount);
        int[] yInts = yIntsPool.get(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            getWorldToScreenToOut(vertices[i], temp);
            xInts[i] = (int) temp.x;
            yInts[i] = (int) temp.y;
        }
        g.setStroke(stroke);        
        g.setColor(f);
        g.fillPolygon(xInts, yInts, vertexCount);
        g.setColor(s);
        g.drawPolygon(xInts, yInts, vertexCount);
        //restoreState(g);
    }

    @Override
    public void drawPolygon(Vec2[] vertices, int vertexCount, Color3f color) {
        Color s = strokeColor; //new Color(color.x, color.y, color.z, 1f);
        Graphics2D g = getGraphics();
        
        //saveState(g);
        int[] xInts = xIntsPool.get(vertexCount);
        int[] yInts = yIntsPool.get(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            getWorldToScreenToOut(vertices[i], temp);
            xInts[i] = (int) temp.x;
            yInts[i] = (int) temp.y;
        }
        g.setStroke(stroke);
        g.setColor(s);
        g.drawPolygon(xInts, yInts, vertexCount);
        //restoreState(g);
    }

    @Override
    public void drawString(float x, float y, String s, Color3f color) {
        Graphics2D g = getGraphics();
        if (g == null) {
            return;
        }
        Color c = new Color(color.x, color.y, color.z);
        g.setColor(c);
        g.drawString(s, x, y);
    }

    public Graphics2D getGraphics() {        
        return graphics;
    }

    private final Vec2 temp2 = new Vec2();

    @Override
    public void drawTransform(Transform xf) {
        Graphics2D g = getGraphics();
        getWorldToScreenToOut(xf.p, temp);
        temp2.setZero();
        float k_axisScale = 0.4f;

        Color c = new Color(1, 0, 0);
        g.setColor(c);

        temp2.x = xf.p.x + k_axisScale * xf.q.c;
        temp2.y = xf.p.y + k_axisScale * xf.q.s;
        getWorldToScreenToOut(temp2, temp2);
        g.drawLine((int) temp.x, (int) temp.y, (int) temp2.x, (int) temp2.y);

        c = new Color(0, 1, 0);
        g.setColor(c);
        temp2.x = xf.p.x + -k_axisScale * xf.q.s;
        temp2.y = xf.p.y + k_axisScale * xf.q.c;
        getWorldToScreenToOut(temp2, temp2);
        g.drawLine((int) temp.x, (int) temp.y, (int) temp2.x, (int) temp2.y);
    }
}
