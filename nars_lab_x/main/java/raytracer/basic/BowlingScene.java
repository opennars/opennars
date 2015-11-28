package raytracer.basic;

import raytracer.effects.NormalEffect;
import raytracer.effects.RoughNormalEffect;
import raytracer.lights.AreaLight;
import raytracer.lights.SphereLight;
import raytracer.objects.PlyObject;
import raytracer.objects.Sphere;
import raytracer.shader.*;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/** 
 * Diese Szene stellt eine Bowlingbahn dar. 
 *  
 * @author Jonas Stahl 
 * @author Mathias Kosch 
 * @author Sassan Torabi-Goudarzi 
 * 
 */ 
public class BowlingScene extends EfficientScene 
{ 
    protected final Shader pinShader = new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.3f, 0.3f, 0.3f), new Vector3f(0.9f, 0.9f, 0.9f), new Vector3f(0.4f, 0.4f, 0.4f));

    //protected ColorEx holeLightLeft = new ColorEx(2.0f, 0.8f, 0.8f);
    //protected ColorEx holeLightRight = new ColorEx(0.8f, 0.8f, 2.0f);
    protected final ColorEx holeLightLeft = new ColorEx(1.0f, 1.0f, 1.0f);
    protected final ColorEx holeLightRight = new ColorEx(1.0f, 1.0f, 1.0f);
    protected final ColorEx sphereLight = new ColorEx(2.5f, 2.5f, 2.0f);
    protected final ColorEx areaLight = new ColorEx(1.3f, 1.3f, 1.3f);

    
    /** 
     * Erzeugt eine neue Szenemit einer Bowlingbahn. 
     */ 
    public BowlingScene() 
    { 
        try
        {
            // Ambientes Licht der Szene setzen:
            setAmbientLight(new ColorEx(0.3f, 0.3f, 0.3f));
            
            createEnvironment();
            createSphereLights();
            createAreaLights();
            createHoleLights();
            
            createHouse();
            createRoom();
            
            createBowls();
            createLeftPins();
            createMiddlePins();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    

    protected void createEnvironment()
    throws Exception
    {
        // Umgebungstextur hinzuf�gen:
        add(new Sphere(new Vector3d(0.0, -6.0, 0.0), 1000000.0, new TextureShader("texture/sky.jpg")));
        
        // Sonne hinzuf�gen:
        add(new SphereLight(new Vector3d(-25000.0, 100000.0, 50000.0), 2000.0, new ColorEx(10000000000.0f, 9200000000.0f, 7000000000.0f)));
    }
    
    protected void createHoleLights() {
        //Lichter linke Bahn
        add(new AreaLight(new Vector3d(-2.6,-4.3,-3.5),new Vector3d(0.1,0.0,0.0),new Vector3d(0.0,0.0,0.65), holeLightLeft)); 
        add(new AreaLight(new Vector3d(-0.8,-4.3,-3.5),new Vector3d(0.1,0.0,0.0),new Vector3d(0.0,0.0,0.65), holeLightRight)); 
        
        //Lichter mittlere Bahn
        add(new AreaLight(new Vector3d(1.4,-4.3,-3.5),new Vector3d(0.1,0.0,0.0),new Vector3d(0.0,0.0,0.65), holeLightLeft)); 
        add(new AreaLight(new Vector3d(3.059,-4.3,-3.5),new Vector3d(0.1,0.0,0.0),new Vector3d(0.0,0.0,0.65), holeLightRight)); 
    }
    
    protected void createSphereLights()
    throws Exception
    {
        Transformation t = new Transformation();

        // Lampe mit Gl�hbirne hinzuf�gen:
        add(new SphereLight(new Vector3d(0.0,-1.75,4.65),0.05, sphereLight));
        t.reset();
        PlyObject ply = new PlyObject("ply/lamp2.ply",
                new ReflectiveShader(0.2f, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.3f, 0.3f, 0.3f), new Vector3f(0.4f, 0.4f, 0.4f), new Vector3f(0.7f, 0.7f, 0.7f))));
        t.scale(0.15,0.15,0.15);
        t.move(0.0,-1.1,4.65);
        ply.center();
        ply.transform(t);
        add(ply);
        
        // Lampe mit Gl�hbirne hinzuf�gen:
        add(new SphereLight(new Vector3d(4.2,-1.75,4.65),0.05, sphereLight)); 
        t.reset();
        ply = new PlyObject("ply/lamp2.ply", 
                new ReflectiveShader(0.2f, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.3f, 0.3f, 0.3f), new Vector3f(0.4f, 0.4f, 0.4f), new Vector3f(0.7f, 0.7f, 0.7f))));
        t.scale(0.15,0.15,0.15);
        t.move(4.2,-1.1,4.65);
        ply.center();
        ply.transform(t);
        add(ply);
        /*
        // Lampe mit Gl�hbirne hinzuf�gen:
        add(new SphereLight(new Vector3d(0,-1.75,-0.2),0.1, sphereLight));
        t.reset();
        ply = new PlyObject("ply/lamp2.ply", 
                new ReflectiveShader(0.2f, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.3f, 0.3f, 0.3f), new Vector3f(0.4f, 0.4f, 0.4f), new Vector3f(0.7f, 0.7f, 0.7f))));
        t.scale(0.15,0.15,0.15);
        t.move(0,-1.1,-0.2);
        ply.center();
        ply.transform(t);
        add(ply);
        
        // Lampe mit Gl�hbirne hinzuf�gen:
        add(new SphereLight(new Vector3d(4.2,-1.75,-0.2),0.1, sphereLight)); 
        t.reset();
        ply = new PlyObject("ply/lamp2.ply", 
                new ReflectiveShader(0.2f, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.3f, 0.3f, 0.3f), new Vector3f(0.4f, 0.4f, 0.4f), new Vector3f(0.7f, 0.7f, 0.7f))));
        t.scale(0.15,0.15,0.15);
        t.move(4.2,-1.1,-0.2);
        ply.center();
        ply.transform(t);
        add(ply);*/
    }
    
    protected void createAreaLights()
    throws Exception
    {
        Transformation t = new Transformation();

        // Fl�chige Lampe hinzuf�gen:
        add(new AreaLight(new Vector3d(-1.5,-0.8,2.5),new Vector3d(0.07,0.0,0.0),new Vector3d(0.0,0.0,0.7), areaLight));  
        t.reset();
        PlyObject ply = new PlyObject("ply/lamp3.ply",
                new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.3f, 0.3f, 0.3f), new Vector3f(0.4f, 0.4f, 0.4f), new Vector3f(0.7f, 0.7f, 0.7f)));
        t.rotate(-90.0, 0.0, -1.0, 0.0);
        t.scale(0.1,0.1,0.1); 
        t.move(-1.5,-0.3,2.5); 
        ply.center(); 
        ply.transform(t); 
        add(ply); 
                 
        // Fl�chige Lampe hinzuf�gen:
        add(new AreaLight(new Vector3d(5.9,-0.8,2.5),new Vector3d(0.07,0.0,0.0),new Vector3d(0.0,0.0,0.7), areaLight));  
        t.reset(); 
        ply = new PlyObject("ply/lamp3.ply",  
                  new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.3f, 0.3f, 0.3f), new Vector3f(0.4f, 0.4f, 0.4f), new Vector3f(0.7f, 0.7f, 0.7f))); 
        t.rotate(-90.0, 0.0, -1.0, 0.0);
        t.scale(0.1,0.1,0.1); 
        t.move(5.9,-0.3,2.5); 
        ply.center(); 
        ply.transform(t); 
        add(ply);        
        
        
        // Fl�chige Lampe hinzuf�gen:
        add(new AreaLight(new Vector3d(5.9,-0.8,6.8),new Vector3d(0.07,0.0,0.0),new Vector3d(0.0,0.0,0.7), areaLight));  
        t.reset(); 
        ply = new PlyObject("ply/lamp3.ply",  
                  new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.3f, 0.3f, 0.3f), new Vector3f(0.4f, 0.4f, 0.4f), new Vector3f(0.7f, 0.7f, 0.7f))); 
        t.rotate(-90.0, 0.0, -1.0, 0.0);
        t.scale(0.1,0.1,0.1); 
        t.move(5.9,-0.3,6.8); 
        ply.center(); 
        ply.transform(t); 
        add(ply); 
                  
        // Fl�chige Lampe hinzuf�gen:
        add(new AreaLight(new Vector3d(-1.5,-0.8,6.8),new Vector3d(0.07,0.0,0.0),new Vector3d(0.0,0.0,0.7), areaLight));  
        t.reset(); 
        ply = new PlyObject("ply/lamp3.ply",  
                  new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.3f, 0.3f, 0.3f), new Vector3f(0.4f, 0.4f, 0.4f), new Vector3f(0.7f, 0.7f, 0.7f))); 
        t.rotate(-90.0, 0.0, -1.0, 0.0);
        t.scale(0.1,0.1,0.1); 
        t.move(-1.5,-0.3,6.8); 
        ply.center(); 
        ply.transform(t); 
        add(ply);
    }
    
    protected void createHouse()
    throws Exception
    {
        Transformation t = new Transformation();

        // Haus erzeugen: 
        t.reset();
        PlyObject ply = new PlyObject("ply/BowlingHouse.ply",
                new PhongShader(new WallShader(new ColorEx(1.0f, 1.0f, 0.9f)), new Vector3f(0.6f, 0.6f, 0.6f), new Vector3f(1.0f, 0.9f, 0.9f), new Vector3f(0.7f, 0.7f, 0.6f)),
                new RoughNormalEffect(0.2f));
        t.rotate(-90.0, 1.0, 0.0, 0.0);
        ply.transform(t);
        add(ply);
        
        // R�ckwand erzeugen: 
        t.reset(); 
        ply = new PlyObject("ply/BowlingHoleWall.ply", 
                new PhongShader(new TextureShader("texture/HoleWall.jpg"), new Vector3f(0.4f, 0.4f, 0.4f), new Vector3f(0.7f, 0.7f, 0.8f), new Vector3f(0.5f, 0.5f, 0.6f)));
        t.move(4.175, -5.32, -0.55);
        ply.transform(t);
        t.reset();
        t.scale(3.0, 3.0, 0.0);
        ply.transformTexture(t);
        add(ply);
        
        //T�r erzeugen:
        t.reset();
        ply = new PlyObject("ply/BowlingDoor.ply", 
                new PhongShader(new TextureShader("texture/Door.jpg"), new Vector3f(0.3f, 0.3f, 0.3f), new Vector3f(0.4f, 0.4f, 0.4f), new Vector3f(0.7f, 0.7f, 0.7f)));
        t.move(5.5, -4.45, 8.0);
        ply.center();
        ply.transform(t);
        add(ply);
    }

    protected void createRoom() 
    { 
        try 
        { 
            Transformation t = new Transformation();

            Shader roadShader = new ReflectiveShader(0.15f, new ReflectionShader(1.4f, new PhongShader(new TextureShader("texture/bowlRoad.jpg"), new Vector3f(0.3f, 0.3f, 0.3f), new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0.8f, 0.8f, 0.8f))));
            Shader backRoadShader = new ReflectionShader(1.2f, new PhongShader(new TextureShader("texture/BackRoad.jpg"), new Vector3f(0.4f, 0.4f, 0.4f), new Vector3f(0.7f, 0.7f, 0.7f), new Vector3f(0.5f, 0.5f, 0.5f)));
            Shader sideRoadShader = new PhongShader(new TextureShader("texture/sideRoad.jpg"), new Vector3f(0.3f, 0.3f, 0.3f), new Vector3f(0.4f, 0.4f, 0.4f), new Vector3f(0.7f, 0.7f, 0.7f));
            Shader bowlingHolderShader = new ReflectiveShader(0.2f, new PhongShader(new TextureShader("texture/BowlHolder.jpg"), new Vector3f(0.3f, 0.3f, 0.2f), new Vector3f(0.6f, 0.6f, 0.5f), new Vector3f(0.95f, 0.95f, 0.8f)));
            Shader rampShader = new ReflectiveShader(0.1f, new PhongShader(new ColorShader(ColorEx.DARK_GRAY), new Vector3f(0.3f, 0.3f, 0.3f), new Vector3f(0.7f, 0.7f, 0.7f), new Vector3f(0.5f, 0.5f, 0.5f)));
            
            //linke Bahn
            t.reset();
            PlyObject ply = new PlyObject("ply/BowlingRoad.ply", roadShader);
            t.scale(1.0,1.0,1.0);
            t.move(-1.7,-5.44, 0.0);
            ply.center();
            ply.transform(t);
            t.reset();
            t.rotate(10.0, 0.0, 0.0, 1.0);
            ply.transformTexture(t);
            add(ply);
            
            
            //mittler Bahn
            t.reset();
            ply = new PlyObject("ply/BowlingRoad.ply", roadShader);
            t.scale(1.0,1.0,1.0);
            t.move(2.301, -5.44, 0.0);
            ply.center();
            ply.transform(t);
            t.reset();
            t.rotate(-3.0, 0.0, 0.0, 1.0);
            t.move(0.3, 0.2, 0.0);
            ply.transformTexture(t);
            add(ply);
            
            //rechte Bahn
            t.reset();
            ply = new PlyObject("ply/BowlingRoad.ply", roadShader);
            t.scale(1.0,1.0,1.0);
            t.move(6.16, -5.44, 0.0);
            ply.center();
            ply.transform(t);
            t.reset();
            t.rotate(-15.0, 0.0, 0.0, 1.0);
            t.move(0.1, 0.7, 0.0);
            ply.transformTexture(t);
            add(ply);
           
            
            //Back Road links
            t.reset();
            ply = new PlyObject("ply/BowlingBackRoad.ply", backRoadShader, true, new RoughNormalEffect(0.1f));
            t.scale(0.47,0.47,0.47);
            t.move(0.29, -5.43, -0.14);
            ply.center();
            ply.transform(t);
            t.reset();
            t.scale(6.0, 6.0, 0.0);
            ply.transformTexture(t);
            add(ply);
            
            //Back Road Rechts
            t.reset();
            ply = new PlyObject("ply/BowlingBackRoad.ply", backRoadShader, true, new RoughNormalEffect(0.1f));
            t.scale(0.45,0.45,0.45);
            t.move(4.225, -5.43, 0.1);
            ply.center();
            ply.transform(t);
            t.reset();
            t.scale(6.0, 6.0, 0.0);
            t.rotate(100.0, 0.0, 0.0, 1.0);
            ply.transformTexture(t);
            add(ply);
            
            //Sideroad links
            t.reset();
            ply = new PlyObject("ply/BowlingSideRoad.ply", sideRoadShader);
            
            t.scale(1.1,1.1,1.1);
            t.move(-2.95, -5.44, -0.3);
            ply.center();
            ply.transform(t);
            add(ply);
            
            //Sideroad rechts
            t.reset();
            ply = new PlyObject("ply/BowlingSideRoad.ply", sideRoadShader);

            t.scale(1.1,1.1,1.1);
            t.move(7.4, -5.43, -0.3);
            ply.center();
            ply.transform(t);
            add(ply);



            //Rampe
            t.reset();
            ply = new PlyObject("ply/BowlingRampe.ply", rampShader, new RoughNormalEffect(0.5f));
            ply.normalize();
            t.scale(5.5,5.5,5.5);
            t.move(2.2, -5.46, 4.65);
            ply.center();
            ply.transform(t);
            add(ply);

            //Holder links  
            t.reset();
            ply = new PlyObject("ply/BowlingHolder.ply", bowlingHolderShader);
            t.move(0.33, -5.1, 4.65);
            ply.center();
            ply.transform(t);
            add(ply);
            
            //Holder rechts
            t.reset();
            ply = new PlyObject("ply/BowlingHolder.ply", bowlingHolderShader);
            t.move(4.27, -5.1, 4.65);
            ply.center();
            ply.transform(t);
            add(ply);
        } 
        catch (Exception e) 
        { 
            throw new RuntimeException(e); 
        } 
    } 
    
    protected void createBowls()
    throws Exception
    {
        // Kugel linke Bahn:
        addBowl(new Vector3d(-1.475,-5.305, -3.0), new ConcatShader(new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.3f), new Vector3f(0.1f, 0.1f, 0.6f), new Vector3f(0.7f, 0.7f, 1.0f)), 0.85f, new RefractionShader(RefractionShader.INDEX_GLASS, new ColorEx(0.4f, 0.4f, 0.9f))),
                new RoughNormalEffect(0.15f),
                30.0, -1.0, 2.0, -0.5);
        
        // Kugel mittlere Bahn
        addBowl(new Vector3d(0.53,-5.24, 2.0), new PhongShader(new ColorShader(new ColorEx(0.9f, 0.9f, 0.9f)), new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0.6f, 0.6f, 0.6f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 0.0f)),
                new RoughNormalEffect(0.2f),
                70.0, 0.0, 1.0, 1.0);
        
        //Kugeln auf Haltern
        addBowl(new Vector3d(0.67, -4.75, 4.8), new PhongShader(new ColorShader(new ColorEx(1.0f, 1.0f, 0.3f)), new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.6f, 0.6f, 0.6f)),
                new RoughNormalEffect(0.2f),
                80.0, -1.0, 2.0, 0.0);
        addBowl(new Vector3d(0.67, -4.75, 4.57), new PhongShader(new ColorShader(new ColorEx(0.85f, 0.0f, 0.1f)), new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.6f, 0.6f, 0.6f)),
                new RoughNormalEffect(0.2f),
                -60.0, 1.0, 1.0, 0.0);
        addBowl(new Vector3d(0.0, -4.75, 4.69), new PhongShader(new ColorShader(new ColorEx(0.7f, 0.8f, 0.0f)), new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.6f, 0.6f, 0.6f)),
                new RoughNormalEffect(0.2f),
                150.0, 0.0, 1.0, 0.0);
    }
    
    protected void addBowl(Vector3d center, Shader shader, NormalEffect normalEffect,
            double angle, double x, double y, double z)
    throws Exception
    {
        //if (mitKugel)
        {
            Transformation t = new Transformation();
            PlyObject ply = new PlyObject("ply/bowl.ply", shader, true, normalEffect);
            t.rotate(angle, x, y, z);
            t.scale(0.075,0.075,0.075);
            t.move(center.x, center.y, center.z);
            ply.center();
            ply.transform(t);
            add(ply);
        }
        /*else
        {
            Sphere sphere = new Sphere(center, 0.106, shader);
            sphere.setNormalEffect(normalEffect);
            add(sphere);
        }*/
    }
    
    
    protected void createLeftPins()
    throws Exception
    {
        Transformation t = new Transformation();

        /** !!! Linke Bahn !!!  */
        // Kegel
        // Hintere Reihe
        t.reset();
        PlyObject ply = new PlyObject("ply/pin.ply", pinShader, true);
        t.rotate(-90.0, -1.0, 0.0, 0.0);
        t.scale(0.033,0.033,0.033);
        t.move(-2.20,-5.205,-4.1);
        ply.center();
        ply.transform(t);
        add(ply);
        
        //
        t.reset();
        ply = new PlyObject("ply/pin.ply", pinShader, true);
        t.rotate(-90.0, -1.0, 0.0, 0.0);
        t.scale(0.033,0.033,0.033);
        t.move(-1.85,-5.205,-4.1);
        ply.center();
        ply.transform(t);
        add(ply);
        
        //
        t.reset();
        ply = new PlyObject("ply/pin.ply", pinShader, true);
        t.rotate(-90.0, -1.0, 0.0, 0.0);
        t.scale(0.033,0.033,0.033);
        t.move(-1.5,-5.205,-4.1);
        ply.center();
        ply.transform(t);
        add(ply);
        
        //
        t.reset();
        ply = new PlyObject("ply/pin.ply", pinShader, true);
        t.rotate(-90.0, -1.0, 0.0, 0.0);
        t.scale(0.033,0.033,0.033);
        t.move(-1.15,-5.205,-4.1);
        ply.center();
        ply.transform(t);
        add(ply);
        
        // 2te von hinten
        t.reset();
        ply = new PlyObject("ply/pin.ply", pinShader, true);
        t.rotate(-90.0, -1.0, 0.0, 0.0);
        t.scale(0.033,0.033,0.033);
        t.move(-1.325,-5.205,-3.81);
        ply.center();
        ply.transform(t);
        add(ply);
        
        //
        t.reset();
        ply = new PlyObject("ply/pin.ply", pinShader, true);
        t.rotate(-90.0, -1.0, 0.0, 0.0);
        t.scale(0.033,0.033,0.033);
        t.move(-1.675,-5.205,-3.81);
        ply.center();
        ply.transform(t);
        add(ply);
        
        //
        t.reset();
        ply = new PlyObject("ply/pin.ply", pinShader, true);
        t.rotate(-90.0, -1.0, 0.0, 0.0);
        t.scale(0.033,0.033,0.033);
        t.move(-2.025,-5.205,-3.81);
        ply.center();
        ply.transform(t);
        add(ply);
        
        // 3te von hinten
        t.reset();
        ply = new PlyObject("ply/pin.ply", pinShader, true);
        t.rotate(-90.0, -1.0, 0.0, 0.0);
        t.scale(0.033,0.033,0.033);
        t.move(-1.85,-5.205,-3.52);
        ply.center();
        ply.transform(t);
        add(ply);
        
        //
        t.reset();
        ply = new PlyObject("ply/pin.ply", pinShader, true);
        t.rotate(-90.0, -1.0, 0.0, 0.0);
        t.scale(0.033,0.033,0.033);
        t.move(-1.5,-5.205,-3.52);
        ply.center();
        ply.transform(t);
        add(ply);
        
        // vorne
        t.reset();
        ply = new PlyObject("ply/pin.ply", pinShader, true);
        t.rotate(-90.0, -1.0, 0.0, 0.0);
        t.scale(0.033,0.033,0.033);
        t.move(-1.675,-5.205,-3.23);
        ply.center();
        ply.transform(t);
        add(ply);
    }
    
    protected void createMiddlePins()
    throws Exception
    {
        Transformation t = new Transformation();

        /** Mittler Bahn
         * 
         */
        
        // Kegel
        // Hintere Reihe
        t.reset();
        PlyObject ply = new PlyObject("ply/pin.ply", pinShader, true);
        t.rotate(-90.0, -1.0, 0.0, 0.0);
        t.scale(0.033,0.033,0.033);
        t.move(1.659,-5.205,-4.1);
        ply.center();
        ply.transform(t);
        add(ply);
                  
        //
        t.reset();
        ply = new PlyObject("ply/pin.ply", pinShader, true);
        t.rotate(-90.0, -1.0, 0.0, 0.0);
        t.scale(0.033,0.033,0.033);
        t.move(2.709,-5.205,-4.1);
        ply.center();
        ply.transform(t);
        add(ply);
        
        // 2te von hinten
        t.reset();
        ply = new PlyObject("ply/pin.ply", pinShader, true);
        t.rotate(-90.0, -1.0, 0.0, 0.0);
        t.scale(0.033,0.033,0.033);
        t.move(2.534,-5.205,-3.81);
        ply.center();
        ply.transform(t);
        add(ply);
        
        
        // Umgefallen
        t.reset();
        ply = new PlyObject("ply/pin.ply", pinShader, true);
        t.rotate(180.0, 1.0, 0.0, 0.0);
        t.rotate(-90.0, 0.0, 0.0, -1.0);
        t.rotate(-34.0, 0.0, -1.0, 0.0);
        t.scale(0.033,0.033,0.033);
        t.move(2.234,-5.36,-3.71);
        ply.center();
        ply.transform(t);
        add(ply);
        
        // Umgefallen
        t.reset();
        ply = new PlyObject("ply/pin.ply", pinShader, true);
        t.rotate(80.0, 1.0, 0.0, 0.0);
        t.rotate(-90.0, 0.0, 0.0, -1.0);
        t.rotate(-56.0, 0.0, -1.0, 0.0);
        t.scale(0.033,0.033,0.033);
        t.move(2.0,-5.36,-4.1);
        ply.center();
        ply.transform(t);
        add(ply);        
        
        
        // Umgefallen
        t.reset();
        ply = new PlyObject("ply/pin.ply", pinShader, true);
        t.rotate(-70.0, 1.0, 0.0, 0.0);
        t.rotate(-90.0, 0.0, 0.0, -1.0);
        t.rotate(-56.0, 0.0, -1.0, 0.0);
        t.scale(0.033,0.033,0.033);
        t.move(2.5,-5.36,-4.3);
        ply.center();
        ply.transform(t);
        add(ply);        
    }
}