/*
 * PlyObject.java                         STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.objects;

import com.jogamp.opengl.GLAutoDrawable;
import raytracer.basic.Ray;
import raytracer.basic.Transformation;
import raytracer.effects.NormalEffect;
import raytracer.exception.InvalidFormatException;
import raytracer.exception.LinearlyDependentException;
import raytracer.shader.Shader;
import raytracer.util.BoundingField;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * Erzeugt ein neues PLY-Objekt aus einer PLY-Datei.
 * 
 * @author Mathias Kosch
 *
 */
public class PlyObject extends SceneObject
{
    /** Sammlung zur Verwaltung aller Shapes. */
    SceneObjectCollection shapes = new EfficientCollection();
    
    /** Shader dieses Objets. */
    Shader shader = null;
    /** Normalen-Effekt f�r dieses Objekt. */
    NormalEffect normalEffect = null;
    
    
    /**
     * Erzeugt ein neues <code>PlyObject</code>.
     * 
     * @param fileName Dateiname der PLY-Datei.
     * @param shader Shader, mit dem das Objekt gezeichnet wird.
     * 
     * @throws java.io.IOException
     * @throws raytracer.exception.InvalidFormatException
     */
    public PlyObject(String fileName, Shader shader)
    throws IOException, InvalidFormatException
    {
        this(fileName, shader, false, null);
    }    
    
    /**
     * Erzeugt ein neues <code>PlyObject</code>.
     * 
     * @param fileName Dateiname der PLY-Datei.
     * @param shader Shader, mit dem das Objekt gezeichnet wird.
     * @param normalEffect Normalen-Effekt f�r dieses Objekt.
     * 
     * @throws java.io.IOException
     * @throws raytracer.exception.InvalidFormatException
     */
    public PlyObject(String fileName, Shader shader, NormalEffect normalEffect)
    throws IOException, InvalidFormatException
    {
        this(fileName, shader, false, normalEffect);
    }    
    
    
    /**
     * Erzeugt ein neues <code>PlyObject</code>.
     * 
     * @param fileName Dateiname der PLY-Datei.
     * @param shader Shader, mit dem das Objekt gezeichnet wird.
     * @param smoothNormals Gibt an, ob die Normalen interpoliert werden
     *        sollen, falls die PLY-Datei Informationen zu den Normalenvektoren
     *        enth�lt.
     * 
     * @throws java.io.IOException
     * @throws raytracer.exception.InvalidFormatException
     */
    public PlyObject(String fileName, Shader shader, boolean smoothNormals)
    throws IOException, InvalidFormatException
    {
        this(fileName, shader, smoothNormals, null);
    }    
    
    /**
     * Erzeugt ein neues <code>PlyObject</code>.
     * 
     * @param fileName Dateiname der PLY-Datei.
     * @param shader Shader, mit dem das Objekt gezeichnet wird.
     * @param smoothNormals Gibt an, ob die Normalen interpoliert werden
     *        sollen, falls die PLY-Datei Informationen zu den Normalenvektoren
     *        enth�lt.
     * @param normalEffect Normalen-Effekt f�r dieses Objekt.
     * 
     * @throws java.io.IOException
     * @throws raytracer.exception.InvalidFormatException
     */
    public PlyObject(String fileName, Shader shader, boolean smoothNormals,
            NormalEffect normalEffect)
    throws IOException, InvalidFormatException
    {
        this.shader = shader;
        this.normalEffect = normalEffect;
        loadPlyFile(fileName, smoothNormals);
    }    
    
    
    @Override
    public PlyObject clone()
    throws CloneNotSupportedException
    {
        PlyObject clone = (PlyObject)super.clone();
        clone.shapes = shapes.clone();
        return clone;
    }

    
    @Override
    public void getShapes(Collection<Shape> shapes)
    {
        this.shapes.getShapes(shapes);
    }
    
    @Override
    public void getBoundingPoints(Collection<Vector3d> points)
    {
        shapes.getBoundingPoints(points);
    }
    
    
    @Override
    public void transform(Transformation t)
    {
        shapes.transform(t);
    }
    
    
    @Override
    public boolean intersect(Ray ray)
    {
        return shapes.intersect(ray);
    }

    @Override
    public boolean occlude(Ray ray)
    {
        return shapes.occlude(ray);
    }
    
    
    @Override
    public void display(GLAutoDrawable drawable)
    {
        shapes.display(drawable);
    }


    /**
     * Transformiert dieses Objekt so, dass die kleinste umschlie�ende Kugel
     * im Ursprung liegt.
     */
    public void center()
    {
        BoundingField.Sphere sphere = smallestEnclosingSphere();
        Transformation t = new Transformation();
        t.move(-sphere.center.x, -sphere.center.y, -sphere.center.z);
        transform(t);
    }
    
    /**
     * Transformiert dieses Objekt so, dass die kleinste umschlie�ende Kugel
     * im Ursprung mit Radius 1 liegt.
     */
    public void normalize()
    {
        BoundingField.Sphere sphere = smallestEnclosingSphere();
        Transformation t = new Transformation();
        t.move(-sphere.center.x, -sphere.center.y, -sphere.center.z);
        t.scale(1.0/sphere.radius, 1.0/sphere.radius, 1.0/sphere.radius);
        transform(t);
    }
    
    
    /**
     * Wendet eine Transformation auf die Texturkoordinaten dieses Objekts an.
     * 
     * @param t Transformation, die auf die Texturkoordinaten angewendet wird.
     */
    public void transformTexture(Transformation t)
    {
        for (Shape shape : shapes.getShapes()) shape.transformTexture(t);
    }
    
    
    /**
     * Liest die Informationen aus einer PLY-Datei und erzeugt daraus
     * Dreiecke.
     * 
     * @param fileName Dateiname der OFF-Datei.
     * @param smoothNormals Gibt an, ob die Normalen interpoliert werden
     *        sollen, falls die PLY-Datei Informationen zu den Normalenvektoren
     *        enth�lt.
     * 
     * @throws java.io.IOException
     * @throws raytracer.exception.InvalidFormatException
     */
    private void loadPlyFile(String fileName, boolean smoothNormals)
    throws InvalidFormatException
    {
        // Datei �ffnen:
        Scanner scanner = new Scanner(new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(fileName)));
        scanner.useLocale(Locale.US);
            
        Tokenizer tokenizer = new Tokenizer(scanner);


        Vector<Element> elements = new Vector<Element>();
        
        
        try
        {
            // PLY-Kennung pr�fen:
            if (tokenizer.token() != Tokenizer.TOKEN_PLY)
                throw new InvalidFormatException();
            tokenizer.next();
            
            // Format-Kennung pr�fen:
            if (tokenizer.token() != Tokenizer.TOKEN_FORMAT)
                throw new InvalidFormatException();
            tokenizer.next();
            if (tokenizer.token() != Tokenizer.TOKEN_ASCII)
                throw new InvalidFormatException();
            tokenizer.next();
            if (tokenizer.token() != Tokenizer.TOKEN_NUMBER)
                throw new InvalidFormatException();
            if (tokenizer.doubleValue() != 1.0)
                throw new InvalidFormatException();
            tokenizer.next();
            
            // Header-Informationen der PLY-Datei auslesen:
            Property property = null;
            Element currentElement = null;
            loop:
            do
            {
                switch (tokenizer.token())
                {
                case Tokenizer.TOKEN_COMMENT:
                    tokenizer.endLine();
                    tokenizer.next();
                    continue;
                    
                case Tokenizer.TOKEN_ELEMENT:
                    tokenizer.next();
                    
                    // Neues Element erstellen:
                    currentElement = new Element();
                    elements.add(currentElement);
                    
                    // Eigenschaften des Elements lesen:
                    if (tokenizer.token() != Tokenizer.TOKEN_NAME)
                        throw new InvalidFormatException();
                    currentElement.name = tokenizer.stringValue();
                    tokenizer.next();
                    if (tokenizer.token() != Tokenizer.TOKEN_NUMBER)
                        throw new InvalidFormatException();
                    currentElement.count = tokenizer.intValue();
                    tokenizer.next();
                    break;
                    
                case Tokenizer.TOKEN_PROPERTY:
                    if (currentElement == null)
                        throw new InvalidFormatException();
                    tokenizer.next();
                    
                    // Neue Eigenschaft erstellen:
                    property = new Property();
                    currentElement.properties.add(property);
                    
                    // Eigenschaften der Eigenschaft lesen:
                    if (!Tokenizer.isType(tokenizer.token()))
                        throw new InvalidFormatException();
                    property.type = tokenizer.token();
                    tokenizer.next();
                    if (Tokenizer.isTypeList(property.type))
                    {
                        if (!Tokenizer.isTypeInt(tokenizer.token()))
                            throw new InvalidFormatException();
                        property.listCountType = tokenizer.token();
                        tokenizer.next();
                        if (!Tokenizer.isType(tokenizer.token()))
                            throw new InvalidFormatException();
                        property.listDataType = tokenizer.token();
                        tokenizer.next();
                    }
                    if (tokenizer.token() != Tokenizer.TOKEN_NAME)
                        throw new InvalidFormatException();
                    property.name = tokenizer.stringValue();
                    tokenizer.next();
                    break;
                    
                case Tokenizer.TOKEN_END_HEADER:
                    break loop;
                    
                default:
                    throw new InvalidFormatException();
                }
            }
            while (true);
            
            Vector<Vector3d> vertices = new Vector<Vector3d>();
            Vector<Vector3d> normals = new Vector<Vector3d>();
            Vector<Vector2d> textures = new Vector<Vector2d>();
            Vector<Integer> faces = new Vector<Integer>();
            
            // Daten auslesen und verarbeiten:
            for (Element element : elements) {
                currentElement = element;

                switch (currentElement.name) {
                    case "vertex":
                        readVertices(scanner, currentElement, vertices, normals, textures);
                        break;
                    case "face":
                        readFaces(scanner, currentElement, faces);
                        break;
                    default:
                        readElement(scanner, currentElement);
                        break;
                }
            }
            
            // Fl�chen der PLY-Datei triangulieren und Dreiecke daraus erzeugen:
            Iterator<Integer> itFaces = faces.iterator();
            while (itFaces.hasNext())
            {
                int count = -itFaces.next();
                if (count < 3)
                    throw new InvalidFormatException();
                
                // Ersten beiden Punkte ermitteln:
                int firstId = itFaces.next();
                int previousId = itFaces.next();

                // Aus den restlichen Punkten der Fl�che Dreiecke erzeugen:
                count -= 2;
                do
                {
                    int currentId = itFaces.next();
                    try
                    {
                        Triangle triangle = new Triangle(vertices.get(firstId), vertices.get(previousId), vertices.get(currentId), shader);
                        triangle.setNormalEffect(normalEffect);
                        
                        // Normalenvektoren setzen:
                        if (smoothNormals)
                        {
                            Vector3d normalA = normals.get(firstId);
                            Vector3d normalB = normals.get(previousId);
                            Vector3d normalC = normals.get(currentId);
                            if ((normalA != null) && (normalB != null)&& (normalC != null))
                                triangle.setNormals(normalA, normalB, normalC);
                        }
                        
                        // Texturkoordinaten setzen:
                        Vector2d textureA = textures.get(firstId);
                        Vector2d textureB = textures.get(previousId);
                        Vector2d textureC = textures.get(currentId);
                        if ((textureA != null) && (textureB != null)&& (textureC != null))
                            triangle.setTextureCoords(textureA, textureB, textureC);
                        
                        shapes.add(triangle);
                    }
                    catch (LinearlyDependentException e)
                    {
                        // Falls bei den Koordinaten lineare Abh�ngigkeit besteht,
                        // verwerfe deises Dreieck.
                    }
                    previousId = currentId;
                }
                while (--count > 0);
            }
        }
        finally
        {
            scanner.close();
        }
    }

    private static void readVertices(Scanner scanner, Element element,
                                     Vector<Vector3d> vertices, Vector<Vector3d> normals,
                                     Vector<Vector2d> textures)
    {

        // Alle Datens�tze dieses Elements auslesen:
        for (int i = 0; i < element.count; i++)
        {
            Vector3d vertex = new Vector3d();
            Vector3d normal = new Vector3d();
            Vector2d texture = new Vector2d();

            Iterator<Property> it = element.properties.iterator();
            while (it.hasNext())
            {
                Property property = it.next();

                if (Tokenizer.isTypeList(property.type))
                {
                    // Alle Elemente der Liste �berspringen:
                    int count = scanner.nextInt();
                    for (int j = 0; j < count; j++)
                        scanner.next();
                }
                else
                {
                    double doubleValue = scanner.nextDouble();

                    switch (property.name) {
                        case "x":
                            vertex.x = doubleValue;
                            break;
                        case "y":
                            vertex.y = doubleValue;
                            break;
                        case "z":
                            vertex.z = doubleValue;
                            break;
                        case "nx":
                            normal.x = doubleValue;
                            break;
                        case "ny":
                            normal.y = doubleValue;
                            break;
                        case "nz":
                            normal.z = doubleValue;
                            break;
                        case "s":
                            texture.x = doubleValue;
                            break;
                        case "t":
                            texture.y = doubleValue;
                            break;
                    }
                }
            }
            
            // Normalenvektor hinzuf�gen:
            normals.add(((normal.x == 0.0) && (normal.y == 0.0) && (normal.z == 0.0)) ?
                    null : new Vector3d(normal));
                
            // Texturkoordinate hinzuf�gen:
            textures.add(((texture.x == 0.0) && (texture.y == 0.0)) ?
                    null : new Vector2d(texture));
            
            // Daten hinzuf�gen:
            vertices.add(vertex);
        }
    }

    private static void readFaces(Scanner scanner, Element element,
                                  Vector<Integer> faces)
    {

        // Alle Datens�tze dieses Elements auslesen:
        for (int i = 0; i < element.count; i++)
        {
            Iterator<Property> it = element.properties.iterator();
            while (it.hasNext())
            {
                Property property = it.next();

                if (Tokenizer.isTypeList(property.type))
                {
                    // Alle Punkte der Fl�che hinzuf�gen:
                    int count = scanner.nextInt();
                    //System.out.print(count + " ");
                    faces.add(-count);
                    for (int j = 0; j < count; j++)
                    {
                        int data = scanner.nextInt();
                        //System.out.print(data + " ");
                        faces.add(data);
                    }
                    //System.out.println();
                }
                else
                    scanner.next();
            }
        }
    }

    private static void readElement(Scanner scanner, Element element)
    {

        // Alle Datens�tze dieses Elements auslesen:
        for (int i = 0; i < element.count; i++)
        {
            Iterator<Property> it = element.properties.iterator();
            while (it.hasNext())
            {
                Property property = it.next();

                if (Tokenizer.isTypeList(property.type))
                {
                    // Alle Elemente der Liste �berspringen:
                    int count = scanner.nextInt();
                    for (int j = 0; j < count; j++)
                        scanner.next();
                }
                else
                    scanner.next();
            }
        }
    }
    
    
    protected static class Tokenizer
    {
        public final static int TOKEN_ERROR = -1;
        
        public final static int TOKEN_PLY = 0;
        public final static int TOKEN_FORMAT = 1;
        public final static int TOKEN_ASCII = 2;
        public final static int TOKEN_BINARY_LITTLE_ENDIAN = 3;
        public final static int TOKEN_BINARY_BIG_ENDIAN = 4;
        public final static int TOKEN_COMMENT = 5;
        public final static int TOKEN_ELEMENT = 6;
        public final static int TOKEN_PROPERTY = 7;
        public final static int TOKEN_END_HEADER = 8;
        
        public final static int TOKEN_INT8 = 16;
        public final static int TOKEN_UINT8 = 17;
        public final static int TOKEN_INT16 = 18;
        public final static int TOKEN_UINT16 = 19;
        public final static int TOKEN_INT32 = 20;
        public final static int TOKEN_UINT32 = 21;
        public final static int TOKEN_FLOAT32 = 24;
        public final static int TOKEN_FLOAT64 = 25;
        
        public final static int TOKEN_LIST = 33;
        public final static int TOKEN_NAME = 34;
        public final static int TOKEN_NUMBER = 35;
        
        
        private final Scanner scanner;
        private int token = -1;
        private String tokenValue = null;
        private double tokenDouble;
        
        
        public Tokenizer(Scanner scanner)
        {
            this.scanner = scanner;
            next();
        }
        
    
        public int token()
        {
            return token;
        }
        
        public int intValue()
        {
            if (token != TOKEN_NUMBER)
                throw new NumberFormatException();
            return (int)tokenDouble;
        }
        
        public double doubleValue()
        {
            if (token != TOKEN_NUMBER)
                throw new NumberFormatException();
            return tokenDouble;
        }
        
        public String stringValue()
        {
            return tokenValue;
        }
        
        
        public void next()
        {
            // N�chsten String einlesen:
            try
            {
                tokenValue = scanner.next();
            }
            catch (NoSuchElementException e)
            {
                token = TOKEN_ERROR;
                tokenValue = null;
                return;
            }

            switch (tokenValue) {
                case "ply":
                    token = TOKEN_PLY;
                    break;
                case "format":
                    token = TOKEN_FORMAT;
                    break;
                case "ascii":
                    token = TOKEN_ASCII;
                    break;
                case "binary_little_endian":
                    token = TOKEN_BINARY_LITTLE_ENDIAN;
                    break;
                case "binary_big_endian":
                    token = TOKEN_BINARY_BIG_ENDIAN;
                    break;
                case "comment":
                    token = TOKEN_COMMENT;
                    break;
                case "element":
                    token = TOKEN_ELEMENT;
                    break;
                case "property":
                    token = TOKEN_PROPERTY;
                    break;
                case "end_header":
                    token = TOKEN_END_HEADER;
                    break;
                case "char":
                    token = TOKEN_INT8;
                    break;
                case "int8":
                    token = TOKEN_INT8;
                    break;
                case "uchar":
                    token = TOKEN_UINT8;
                    break;
                case "uint8":
                    token = TOKEN_UINT8;
                    break;
                case "short":
                    token = TOKEN_INT16;
                    break;
                case "int16":
                    token = TOKEN_INT16;
                    break;
                case "ushort":
                    token = TOKEN_UINT16;
                    break;
                case "uint16":
                    token = TOKEN_UINT16;
                    break;
                case "int":
                    token = TOKEN_INT32;
                    break;
                case "int32":
                    token = TOKEN_INT32;
                    break;
                case "uint":
                    token = TOKEN_UINT32;
                    break;
                case "uint32":
                    token = TOKEN_UINT32;
                    break;
                case "float":
                    token = TOKEN_FLOAT32;
                    break;
                case "float32":
                    token = TOKEN_FLOAT32;
                    break;
                case "double":
                    token = TOKEN_FLOAT64;
                    break;
                case "float64":
                    token = TOKEN_FLOAT64;
                    break;
                case "list":
                    token = TOKEN_LIST;
                    break;
                default:
                    try {
                        tokenDouble = Double.parseDouble(tokenValue);
                        token = TOKEN_NUMBER;
                    } catch (NumberFormatException e) {
                        token = TOKEN_NAME;
                    }
                    break;
            }
        }
        
        public void endLine()
        {
            // Bis zum Ende der Zeile lesen:
            scanner.nextLine();
        }
        
        
        public static boolean isType(int token)
        {
            return ((token & TOKEN_INT8) != 0) || (token == TOKEN_LIST);
        }
        
        public static boolean isTypeInt(int token)
        {
            return (token & TOKEN_INT8) != 0;
        }
        
        public static boolean isTypeDouble(int token)
        {
            return (token & TOKEN_FLOAT32) != 0;
        }
        
        public static boolean isTypeList(int token)
        {
            return token == TOKEN_LIST;
        }
    }
    
    
    protected static class Element
    {
        /** Name dieses Elements. */
        public String name;
        /** Anzahl der Eintr�ge f�r dieses Element. */
        public int count;
        
        /** Liste aller Attribute dieses Elements. */
        public final Vector<Property> properties = new Vector<Property>();
    }
    
    
    protected static class Property
    {
        /** Name dieser Eigenschaft. */
        public String name;
        public int type;
        
        public int listCountType;
        public int listDataType;
    }
}