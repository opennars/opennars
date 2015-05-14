///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package automenta.spacegraph.physics;
//
///**
// *
// * @author seh
// */
//import com.bulletphysics.demos.opengl.IGL;
//import com.bulletphysics.util.ObjectArrayList;
//import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
//import com.bulletphysics.collision.shapes.BoxShape;
//import com.bulletphysics.collision.shapes.CollisionShape;
//import com.bulletphysics.collision.shapes.CompoundShape;
//import com.bulletphysics.collision.shapes.ConcaveShape;
//import com.bulletphysics.collision.shapes.ConvexShape;
//import com.bulletphysics.collision.shapes.CylinderShape;
//import com.bulletphysics.collision.shapes.InternalTriangleIndexCallback;
//import com.bulletphysics.collision.shapes.PolyhedralConvexShape;
//import com.bulletphysics.collision.shapes.ShapeHull;
//import com.bulletphysics.collision.shapes.SphereShape;
//import com.bulletphysics.collision.shapes.StaticPlaneShape;
//import com.bulletphysics.collision.shapes.TriangleCallback;
//import com.bulletphysics.linearmath.DebugDrawModes;
//import com.bulletphysics.linearmath.Transform;
//import com.bulletphysics.linearmath.TransformUtil;
//import com.bulletphysics.linearmath.VectorUtil;
//import com.bulletphysics.util.IntArrayList;
//import com.bulletphysics.util.ObjectPool;
//import javax.vecmath.Vector3f;
//import static com.bulletphysics.demos.opengl.IGL.*;
//
///**
// *
// * @author jezek2
// */
//public class GLObjectDrawer {
//
//    final static Vector3f defaultBodyColor = new Vector3f(0.5f, 0.5f, 0.5f);
//
//    public static void drawCoordSystem(IGL gl) {
//        gl.glBegin(GL_LINES);
//        gl.glColor3f(1, 0, 0);
//        gl.glVertex3f(0, 0, 0);
//        gl.glVertex3f(1, 0, 0);
//        gl.glColor3f(0, 1, 0);
//        gl.glVertex3f(0, 0, 0);
//        gl.glVertex3f(0, 1, 0);
//        gl.glColor3f(0, 0, 1);
//        gl.glVertex3f(0, 0, 0);
//        gl.glVertex3f(0, 0, 1);
//        gl.glEnd();
//    }
//    private static float[] glMat = new float[16];
//
//    public static void drawOpenGL(final IGL gl, final Transform trans, final CollisionShape shape, int debugMode, final BodyControl control) {
//        ObjectPool<Transform> transformsPool = ObjectPool.get(Transform.class);
//        ObjectPool<Vector3f> vectorsPool = ObjectPool.get(Vector3f.class);
//
//        //System.out.println("shape="+shape+" type="+BroadphaseNativeTypes.forValue(shape.getShapeType()));
//
//        gl.glPushMatrix();
//        trans.getOpenGLMatrix(glMat);
//        gl.glMultMatrix(glMat);
////		if (shape.getShapeType() == BroadphaseNativeTypes.UNIFORM_SCALING_SHAPE_PROXYTYPE.getValue())
////		{
////			const btUniformScalingShape* scalingShape = static_cast<const btUniformScalingShape*>(shape);
////			const btConvexShape* convexShape = scalingShape->getChildShape();
////			float	scalingFactor = (float)scalingShape->getUniformScalingFactor();
////			{
////				btScalar tmpScaling[4][4]={{scalingFactor,0,0,0},
////					{0,scalingFactor,0,0},
////					{0,0,scalingFactor,0},
////					{0,0,0,1}};
////
////				drawOpenGL( (btScalar*)tmpScaling,convexShape,color,debugMode);
////			}
////			glPopMatrix();
////			return;
////		}
//
//        if (shape.getShapeType() == BroadphaseNativeType.COMPOUND_SHAPE_PROXYTYPE) {
//            CompoundShape compoundShape = (CompoundShape) shape;
//            Transform childTrans = transformsPool.get();
//            for (int i = compoundShape.getNumChildShapes() - 1; i >= 0; i--) {
//                compoundShape.getChildTransform(i, childTrans);
//                CollisionShape colShape = compoundShape.getChildShape(i);
//                drawOpenGL(gl, childTrans, colShape, debugMode, control);
//            }
//            transformsPool.release(childTrans);
//        } else {
//            //drawCoordSystem();
//
//            //glPushMatrix();
//
//            gl.glEnable(GL_COLOR_MATERIAL);
//
//            if (control != null) {
//                gl.glColor3f(control.getSurfaceColor().x, control.getSurfaceColor().y, control.getSurfaceColor().z);
//            } else {
//                gl.glColor3f(defaultBodyColor.x, defaultBodyColor.y, defaultBodyColor.z);
//            }
//
//            boolean useWireframeFallback = true;
//
//            if ((debugMode & DebugDrawModes.DRAW_WIREFRAME) == 0) {
//                // you can comment out any of the specific cases, and use the default
//                // the benefit of 'default' is that it approximates the actual collision shape including collision margin
//
//                switch (shape.getShapeType()) {
//                    case BOX_SHAPE_PROXYTYPE: {
//                        BoxShape boxShape = (BoxShape) shape;
//                        Vector3f halfExtent = boxShape.getHalfExtentsWithMargin(vectorsPool.get());
//                        gl.glScalef(2f * halfExtent.x, 2f * halfExtent.y, 2f * halfExtent.z);
//                        gl.drawCube(1f);
//                        vectorsPool.release(halfExtent);
//                        useWireframeFallback = false;
//                        break;
//                    }
//                    case SPHERE_SHAPE_PROXYTYPE: {
//                        SphereShape sphereShape = (SphereShape) shape;
//                        float radius = sphereShape.getMargin(); // radius doesn't include the margin, so draw with margin
//                        // TODO: glutSolidSphere(radius,10,10);
//                        //sphere.draw(radius, 8, 8);
//                        gl.drawSphere(radius, 10, 10);
//                        /*
//                        glPointSize(10f);
//                        glBegin(GL_POINTS);
//                        glVertex3f(0f, 0f, 0f);
//                        glEnd();
//                        glPointSize(1f);
//                         */
//                        useWireframeFallback = false;
//                        break;
//                    }
////				case CONE_SHAPE_PROXYTYPE:
////					{
////						const btConeShape* coneShape = static_cast<const btConeShape*>(shape);
////						int upIndex = coneShape->getConeUpIndex();
////						float radius = coneShape->getRadius();//+coneShape->getMargin();
////						float height = coneShape->getHeight();//+coneShape->getMargin();
////						switch (upIndex)
////						{
////						case 0:
////							glRotatef(90.0, 0.0, 1.0, 0.0);
////							break;
////						case 1:
////							glRotatef(-90.0, 1.0, 0.0, 0.0);
////							break;
////						case 2:
////							break;
////						default:
////							{
////							}
////						};
////
////						glTranslatef(0.0, 0.0, -0.5*height);
////						glutSolidCone(radius,height,10,10);
////						useWireframeFallback = false;
////						break;
////
////					}
//
//                    case STATIC_PLANE_PROXYTYPE: {
//                        StaticPlaneShape staticPlaneShape = (StaticPlaneShape) shape;
//                        float planeConst = staticPlaneShape.getPlaneConstant();
//                        Vector3f planeNormal = staticPlaneShape.getPlaneNormal(vectorsPool.get());
//                        Vector3f planeOrigin = vectorsPool.get();
//                        planeOrigin.scale(planeConst, planeNormal);
//                        Vector3f vec0 = vectorsPool.get();
//                        Vector3f vec1 = vectorsPool.get();
//                        TransformUtil.planeSpace1(planeNormal, vec0, vec1);
//                        float vecLen = 100f;
//
//                        Vector3f pt0 = vectorsPool.get();
//                        pt0.scaleAdd(vecLen, vec0, planeOrigin);
//
//                        Vector3f pt1 = vectorsPool.get();
//                        pt1.scale(vecLen, vec0);
//                        pt1.sub(planeOrigin, pt1);
//
//                        Vector3f pt2 = vectorsPool.get();
//                        pt2.scaleAdd(vecLen, vec1, planeOrigin);
//
//                        Vector3f pt3 = vectorsPool.get();
//                        pt3.scale(vecLen, vec1);
//                        pt3.sub(planeOrigin, pt3);
//
//                        gl.glBegin(gl.GL_LINES);
//                        gl.glVertex3f(pt0.x, pt0.y, pt0.z);
//                        gl.glVertex3f(pt1.x, pt1.y, pt1.z);
//                        gl.glVertex3f(pt2.x, pt2.y, pt2.z);
//                        gl.glVertex3f(pt3.x, pt3.y, pt3.z);
//                        gl.glEnd();
//
//                        vectorsPool.release(planeNormal);
//                        vectorsPool.release(planeOrigin);
//                        vectorsPool.release(vec0);
//                        vectorsPool.release(vec1);
//                        vectorsPool.release(pt0);
//                        vectorsPool.release(pt1);
//                        vectorsPool.release(pt2);
//                        vectorsPool.release(pt3);
//
//                        break;
//                    }
//
//                    case CYLINDER_SHAPE_PROXYTYPE: {
//                        CylinderShape cylinder = (CylinderShape) shape;
//                        int upAxis = cylinder.getUpAxis();
//
//                        float radius = cylinder.getRadius();
//                        Vector3f halfVec = vectorsPool.get();
//                        float halfHeight = VectorUtil.getCoord(cylinder.getHalfExtentsWithMargin(halfVec), upAxis);
//
//                        gl.drawCylinder(radius, halfHeight, upAxis);
//
//                        vectorsPool.release(halfVec);
//
//                        break;
//                    }
//                    default: {
//                        if (shape.isConvex()) {
//                            ConvexShape convexShape = (ConvexShape) shape;
//                            if (shape.getUserPointer() == null) {
//                                // create a hull approximation
//                                ShapeHull hull = new ShapeHull(convexShape);
//
//                                // JAVA NOTE: not needed
//                                ///// cleanup memory
//                                //m_shapeHulls.push_back(hull);
//
//                                float margin = shape.getMargin();
//                                hull.buildHull(margin);
//                                convexShape.setUserPointer(hull);
//
//                                //printf("numTriangles = %d\n", hull->numTriangles ());
//                                //printf("numIndices = %d\n", hull->numIndices ());
//                                //printf("numVertices = %d\n", hull->numVertices ());
//                            }
//
//                            if (shape.getUserPointer() != null) {
//                                //glutSolidCube(1.0);
//                                ShapeHull hull = (ShapeHull) shape.getUserPointer();
//
//                                Vector3f normal = vectorsPool.get();
//                                Vector3f tmp1 = vectorsPool.get();
//                                Vector3f tmp2 = vectorsPool.get();
//
//                                if (hull.numTriangles() > 0) {
//                                    int index = 0;
//                                    IntArrayList idx = hull.getIndexPointer();
//                                    ObjectArrayList<Vector3f> vtx = hull.getVertexPointer();
//
//                                    gl.glBegin(gl.GL_TRIANGLES);
//
//                                    for (int i = 0; i < hull.numTriangles(); i++) {
//                                        int i1 = index++;
//                                        int i2 = index++;
//                                        int i3 = index++;
//                                        assert (i1 < hull.numIndices()
//                                                && i2 < hull.numIndices()
//                                                && i3 < hull.numIndices());
//
//                                        int index1 = idx.get(i1);
//                                        int index2 = idx.get(i2);
//                                        int index3 = idx.get(i3);
//                                        assert (index1 < hull.numVertices()
//                                                && index2 < hull.numVertices()
//                                                && index3 < hull.numVertices());
//
//                                        Vector3f v1 = vtx.getQuick(index1);
//                                        Vector3f v2 = vtx.getQuick(index2);
//                                        Vector3f v3 = vtx.getQuick(index3);
//                                        tmp1.sub(v3, v1);
//                                        tmp2.sub(v2, v1);
//                                        normal.cross(tmp1, tmp2);
//                                        normal.normalize();
//
//                                        gl.glNormal3f(normal.x, normal.y, normal.z);
//                                        gl.glVertex3f(v1.x, v1.y, v1.z);
//                                        gl.glVertex3f(v2.x, v2.y, v2.z);
//                                        gl.glVertex3f(v3.x, v3.y, v3.z);
//
//                                    }
//                                    gl.glEnd();
//                                }
//
//                                vectorsPool.release(normal);
//                                vectorsPool.release(tmp1);
//                                vectorsPool.release(tmp2);
//                            }
//                        } else {
//                            //						printf("unhandled drawing\n");
//                        }
//
//                    }
//
//                }
//
//            }
//
//            if (useWireframeFallback) {
//                // for polyhedral shapes
//                if (shape.isPolyhedral()) {
//                    PolyhedralConvexShape polyshape = (PolyhedralConvexShape) shape;
//
//                    gl.glBegin(GL_LINES);
//
//                    Vector3f a = vectorsPool.get(), b = vectorsPool.get();
//                    int i;
//                    for (i = 0; i < polyshape.getNumEdges(); i++) {
//                        polyshape.getEdge(i, a, b);
//
//                        gl.glVertex3f(a.x, a.y, a.z);
//                        gl.glVertex3f(b.x, b.y, b.z);
//                    }
//                    gl.glEnd();
//
//                    vectorsPool.release(a);
//                    vectorsPool.release(b);
//
////					if (debugMode==btIDebugDraw::DBG_DrawFeaturesText)
////					{
////						glRasterPos3f(0.0,  0.0,  0.0);
////						//BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),polyshape->getExtraDebugInfo());
////
////						glColor3f(1.f, 1.f, 1.f);
////						for (i=0;i<polyshape->getNumVertices();i++)
////						{
////							btPoint3 vtx;
////							polyshape->getVertex(i,vtx);
////							glRasterPos3f(vtx.x(),  vtx.y(),  vtx.z());
////							char buf[12];
////							sprintf(buf," %d",i);
////							BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
////						}
////
////						for (i=0;i<polyshape->getNumPlanes();i++)
////						{
////							btVector3 normal;
////							btPoint3 vtx;
////							polyshape->getPlane(normal,vtx,i);
////							btScalar d = vtx.dot(normal);
////
////							glRasterPos3f(normal.x()*d,  normal.y()*d, normal.z()*d);
////							char buf[12];
////							sprintf(buf," plane %d",i);
////							BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
////
////						}
////					}
//
//
//                }
//            }
//
////		#ifdef USE_DISPLAY_LISTS
////
////		if (shape->getShapeType() == TRIANGLE_MESH_SHAPE_PROXYTYPE||shape->getShapeType() == GIMPACT_SHAPE_PROXYTYPE)
////			{
////				GLuint dlist =   OGL_get_displaylist_for_shape((btCollisionShape * )shape);
////				if (dlist)
////				{
////					glCallList(dlist);
////				}
////				else
////				{
////		#else
//            if (shape.isConcave())//>getShapeType() == TRIANGLE_MESH_SHAPE_PROXYTYPE||shape->getShapeType() == GIMPACT_SHAPE_PROXYTYPE)
//            //		if (shape->getShapeType() == TRIANGLE_MESH_SHAPE_PROXYTYPE)
//            {
//                ConcaveShape concaveMesh = (ConcaveShape) shape;
//                //btVector3 aabbMax(btScalar(1e30),btScalar(1e30),btScalar(1e30));
//                //btVector3 aabbMax(100,100,100);//btScalar(1e30),btScalar(1e30),btScalar(1e30));
//
//                //todo pass camera, for some culling
//                Vector3f aabbMax = vectorsPool.get();
//                aabbMax.set(1e30f, 1e30f, 1e30f);
//                Vector3f aabbMin = vectorsPool.get();
//                aabbMin.set(-1e30f, -1e30f, -1e30f);
//
//                GlDrawcallback drawCallback = new GlDrawcallback(gl);
//                drawCallback.wireframe = (debugMode & DebugDrawModes.DRAW_WIREFRAME) != 0;
//
//                concaveMesh.processAllTriangles(drawCallback, aabbMin, aabbMax);
//
//                vectorsPool.release(aabbMax);
//                vectorsPool.release(aabbMin);
//            }
//            //#endif
//
//            //#ifdef USE_DISPLAY_LISTS
//            //		}
//            //	}
//            //#endif
//
////			if (shape->getShapeType() == CONVEX_TRIANGLEMESH_SHAPE_PROXYTYPE)
////			{
////				btConvexTriangleMeshShape* convexMesh = (btConvexTriangleMeshShape*) shape;
////
////				//todo: pass camera for some culling
////				btVector3 aabbMax(btScalar(1e30),btScalar(1e30),btScalar(1e30));
////				btVector3 aabbMin(-btScalar(1e30),-btScalar(1e30),-btScalar(1e30));
////				TriangleGlDrawcallback drawCallback;
////				convexMesh->getMeshInterface()->InternalProcessAllTriangles(&drawCallback,aabbMin,aabbMax);
////
////			}
//
//            // TODO: error in original sources GL_DEPTH_BUFFER_BIT instead of GL_DEPTH_TEST
//            //gl.glDisable(GL_DEPTH_TEST);
//            //glRasterPos3f(0, 0, 0);//mvtx.x(),  vtx.y(),  vtx.z());
//            if ((debugMode & DebugDrawModes.DRAW_TEXT) != 0) {
//                // TODO: BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),shape->getName());
//            }
//
//            if ((debugMode & DebugDrawModes.DRAW_FEATURES_TEXT) != 0) {
//                //BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),shape->getExtraDebugInfo());
//            }
//            //gl.glEnable(GL_DEPTH_TEST);
//
//            //glPopMatrix();
//        }
//
//        if (control!=null) {
//            control.draw(gl);
//        }
//
//        gl.glPopMatrix();
//    }
//
//    ////////////////////////////////////////////////////////////////////////////
//    private static class TriMeshKey {
//
//        public CollisionShape shape;
//        public int dlist; // OpenGL display list
//    }
//
//    private static class GlDisplaylistDrawcallback extends TriangleCallback {
//
//        private IGL gl;
//        private final Vector3f diff1 = new Vector3f();
//        private final Vector3f diff2 = new Vector3f();
//        private final Vector3f normal = new Vector3f();
//
//        public GlDisplaylistDrawcallback(IGL gl) {
//            this.gl = gl;
//        }
//
//        public void processTriangle(Vector3f[] triangle, int partId, int triangleIndex) {
//            diff1.sub(triangle[1], triangle[0]);
//            diff2.sub(triangle[2], triangle[0]);
//            normal.cross(diff1, diff2);
//
//            normal.normalize();
//
//            gl.glBegin(GL_TRIANGLES);
//            gl.glColor3f(0, 1, 0);
//            gl.glNormal3f(normal.x, normal.y, normal.z);
//            gl.glVertex3f(triangle[0].x, triangle[0].y, triangle[0].z);
//
//            gl.glColor3f(0, 1, 0);
//            gl.glNormal3f(normal.x, normal.y, normal.z);
//            gl.glVertex3f(triangle[1].x, triangle[1].y, triangle[1].z);
//
//            gl.glColor3f(0, 1, 0);
//            gl.glNormal3f(normal.x, normal.y, normal.z);
//            gl.glVertex3f(triangle[2].x, triangle[2].y, triangle[2].z);
//            gl.glEnd();
//
//            /*glBegin(GL_LINES);
//            glColor3f(1, 1, 0);
//            glNormal3d(normal.getX(),normal.getY(),normal.getZ());
//            glVertex3d(triangle[0].getX(), triangle[0].getY(), triangle[0].getZ());
//            glNormal3d(normal.getX(),normal.getY(),normal.getZ());
//            glVertex3d(triangle[1].getX(), triangle[1].getY(), triangle[1].getZ());
//            glColor3f(1, 1, 0);
//            glNormal3d(normal.getX(),normal.getY(),normal.getZ());
//            glVertex3d(triangle[2].getX(), triangle[2].getY(), triangle[2].getZ());
//            glNormal3d(normal.getX(),normal.getY(),normal.getZ());
//            glVertex3d(triangle[1].getX(), triangle[1].getY(), triangle[1].getZ());
//            glColor3f(1, 1, 0);
//            glNormal3d(normal.getX(),normal.getY(),normal.getZ());
//            glVertex3d(triangle[2].getX(), triangle[2].getY(), triangle[2].getZ());
//            glNormal3d(normal.getX(),normal.getY(),normal.getZ());
//            glVertex3d(triangle[0].getX(), triangle[0].getY(), triangle[0].getZ());
//            glEnd();*/
//        }
//    }
//
//    private static class GlDrawcallback extends TriangleCallback {
//
//        private IGL gl;
//        public boolean wireframe = false;
//
//        public GlDrawcallback(IGL gl) {
//            this.gl = gl;
//        }
//
//        public void processTriangle(Vector3f[] triangle, int partId, int triangleIndex) {
//            if (wireframe) {
//                gl.glBegin(GL_LINES);
//                gl.glColor3f(1, 0, 0);
//                gl.glVertex3f(triangle[0].x, triangle[0].y, triangle[0].z);
//                gl.glVertex3f(triangle[1].x, triangle[1].y, triangle[1].z);
//                gl.glColor3f(0, 1, 0);
//                gl.glVertex3f(triangle[2].x, triangle[2].y, triangle[2].z);
//                gl.glVertex3f(triangle[1].x, triangle[1].y, triangle[1].z);
//                gl.glColor3f(0, 0, 1);
//                gl.glVertex3f(triangle[2].x, triangle[2].y, triangle[2].z);
//                gl.glVertex3f(triangle[0].x, triangle[0].y, triangle[0].z);
//                gl.glEnd();
//            } else {
//                gl.glBegin(GL_TRIANGLES);
//                gl.glColor3f(1, 0, 0);
//                gl.glVertex3f(triangle[0].x, triangle[0].y, triangle[0].z);
//                gl.glColor3f(0, 1, 0);
//                gl.glVertex3f(triangle[1].x, triangle[1].y, triangle[1].z);
//                gl.glColor3f(0, 0, 1);
//                gl.glVertex3f(triangle[2].x, triangle[2].y, triangle[2].z);
//                gl.glEnd();
//            }
//        }
//    }
//
//    private static class TriangleGlDrawcallback extends InternalTriangleIndexCallback {
//
//        private IGL gl;
//
//        public TriangleGlDrawcallback(IGL gl) {
//            this.gl = gl;
//        }
//
//        public void internalProcessTriangleIndex(Vector3f[] triangle, int partId, int triangleIndex) {
//            gl.glBegin(GL_TRIANGLES);//LINES);
//            gl.glColor3f(1, 0, 0);
//            gl.glVertex3f(triangle[0].x, triangle[0].y, triangle[0].z);
//            gl.glVertex3f(triangle[1].x, triangle[1].y, triangle[1].z);
//            gl.glColor3f(0, 1, 0);
//            gl.glVertex3f(triangle[2].x, triangle[2].y, triangle[2].z);
//            gl.glVertex3f(triangle[1].x, triangle[1].y, triangle[1].z);
//            gl.glColor3f(0, 0, 1);
//            gl.glVertex3f(triangle[2].x, triangle[2].y, triangle[2].z);
//            gl.glVertex3f(triangle[0].x, triangle[0].y, triangle[0].z);
//            gl.glEnd();
//        }
//    }
//
//    /*
//    private static Map<CollisionShape,TriMeshKey> g_display_lists = new HashMap<CollisionShape,TriMeshKey>();
//
//    private static int OGL_get_displaylist_for_shape(CollisionShape shape) {
//    // JAVA NOTE: rewritten
//    TriMeshKey trimesh = g_display_lists.get(shape);
//    if (trimesh != null) {
//    return trimesh.dlist;
//    }
//
//    return 0;
//    }
//
//    private static void OGL_displaylist_clean() {
//    // JAVA NOTE: rewritten
//    for (TriMeshKey trimesh : g_display_lists.values()) {
//    glDeleteLists(trimesh.dlist, 1);
//    }
//
//    g_display_lists.clear();
//    }
//     */
//}
