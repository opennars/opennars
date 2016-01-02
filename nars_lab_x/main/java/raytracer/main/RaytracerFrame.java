/*
 * RaytracerFrame.java                             STATUS: In Bearbeitung
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.main;

import raytracer.basic.*;
import raytracer.cameras.*;
import raytracer.effects.RoughNormalEffect;
import raytracer.exception.InvalidFormatException;
import raytracer.lights.AreaLight;
import raytracer.lights.DirectionalLight;
import raytracer.lights.PointLight;
import raytracer.lights.SphereLight;
import raytracer.objects.*;
import raytracer.shader.*;

import javax.imageio.ImageIO;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Dieses Fenster startet den Raytracing-Prozess zu einer Kamera und zeigt
 * dabei asynchron das Vorschaubild an.
 * 
 * @author Mathias Kosch
 * @author Sassan Torabi-Goudarzi
 */
public class RaytracerFrame extends JFrame
implements RendererListener, WindowListener, ComponentListener, ActionListener
{
    private static final long serialVersionUID = 1L;
    
    /** Horizontale Aufl�sung der Kamera. */
    protected int resX = 640;//256;
    /** Vertikale Aufl�sung der Kamera. */
    protected int resY = 480;//180;

    /** ID der aktuellen Szene. */
    protected int sceneId = 0;//15;
    /** ID der aktuellen Kamera. */
    protected int cameraId = 0;//13;
    
    
    private final static String TITLE = "Vorschau";
    private final static int WIDTH = 800;
    private final static int HEIGHT = 600;
    
    private final static String IMAGE_FILE_NAME = "vorschau";
    
    private final static int VIEW_ZOOM = 0;
    private final static int VIEW_ZOOM_WINDOW = 1;
    
    
    /** Kamera, die das Vorschaubild erzeugt. */
    private AsyncCamera camera = null;
    /** Fortschritt des Render-Vorgangs. */
    private double progress = 0.0;
    
    /** Panel, das das Vorschaubild anzeigt. */
    private PreviewPane preview = null;
    private JScrollPane scroller = null;
    
    /** Ansichtsmodus. */
    private int view = VIEW_ZOOM_WINDOW;
    /** Zoom-Faktor, falls Variabel gew�hlt. */
    private float zoomFactor;
    /** Aktuelles Vorschaubild. */
    private BufferedImage renderedImage = null;
    
    
    private long startTime;
    
    private boolean windowStartet = false;
    
    
    /**
     * Erzeugt ein neues Vorschaufenster und startet den Raytracing-Prozess.
     */
    public RaytracerFrame()
    {
        super(TITLE);
        init();
        changeCamera(cameraId);
        changeScene(sceneId);
    }
    

    private void init()
    {
        // Fenster initialisieren:
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        
        // Ereignisbehandlung initialisieren:
        addWindowListener(this);

        // Men�leiste initialisieren:
        initMenu();
        
        // Komponenten initialisieren:
        initComponents();
    }
    
    private void startCamera()
    {
        if (!windowStartet)
            return;
        
        if (camera != null)
        {
            // Ereignisbehandlung initialisieren:
            camera.addRendererListener(this);

            // Starzeit ermitteln:
            startTime = System.currentTimeMillis();
            
            // Szene rendern:
            camera.render();
        }
        
        // Bild zur�cksetzen:
        //renderedImage = null;
        
        // Titel aktualisieren:
        SwingUtilities.invokeLater(this::updateTitle);
    }
    
    private void stopCamera()
    {
        // Rendervorgang abbrechen:
        if (camera != null)
            camera.stop();

        SwingUtilities.invokeLater(this::updateTitle);
    }
    
    private void initMenu()
    {
        JMenuBar menuBar = new JMenuBar();
        
        // Menu "Datei" hinzuf�gen:
        menuBar.add(createFileMenu());
        
        // Menu "Szene" hinzuf�gen:
        menuBar.add(createSceneMenu());
        
        // Menu "Kamera" hinzuf�gen:
        menuBar.add(createCameraMenu());
        
        // Menu "Ansicht" hinzuf�gen:
        menuBar.add(createViewMenu());
       
        setJMenuBar(menuBar);
    }
    
    private void initComponents()
    {
        preview = new PreviewPane();
        scroller = new JScrollPane(preview);
        scroller.addComponentListener(this);
        add(scroller);

        scroller.addMouseMotionListener(mouseHandler);
        scroller.addMouseListener(mouseHandler);

    }

    final MouseAdapter mouseHandler = new MouseAdapter() {
        int dragStartX = -1, dragStartY = -1;

        @Override
        public void mousePressed(MouseEvent e) {
            dragStartX = e.getX(); dragStartY = e.getY();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (camera!=null) {
                if (camera instanceof FirstCamera) {
                    FirstCamera f = (FirstCamera)camera;

                    Vector3d delta = new Vector3d(e.getX() - dragStartX, e.getY() - dragStartY, 0);
                    delta.scale(0.0005);
                    FirstCamera.org.add(delta);

                    dragStartX = e.getX();
                    dragStartY = e.getY();

                    SwingUtilities.invokeLater(RaytracerFrame.this::restartCamera);

                    //camera.fpsRotate(e.getX() - dragStartX, e.getY() - dragStartY);
                }

            }
        }
    };

    public void restartCamera() {
        stopCamera();
        startCamera();
    }
    
    /**
     * Men� erstellen: "Datei"
     * 
     * @return Erstelltes Men�.
     */
    private JMenu createFileMenu()
    {
        JMenu menu = new JMenu("Datei");

        menu.add(createFileResolutionMenu());
        
        menu.addSeparator();

        JMenuItem menuItem = new JMenuItem("Bild als PNG speichern...");
        menuItem.setActionCommand("SaveAs:png");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Bild als BMP speichern...");
        menuItem.setActionCommand("SaveAs:bmp");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Beenden");
        menuItem.setActionCommand("Exit");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        return menu;
    }
    
    /**
     * Men� erstellen: "Datei" -> "Aufl�sung �ndern"
     * 
     * @return Erstelltes Men�.
     */
    private JMenu createFileResolutionMenu()
    {
        JMenu menu = new JMenu("Aufl�sung �ndern");

        JMenuItem menuItem = new JMenuItem("256 x 256");
        menuItem.setActionCommand("Resolution:256:256");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("512 x 512");
        menuItem.setActionCommand("Resolution:512:512");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("1024 x 1024");
        menuItem.setActionCommand("Resolution:1024:1024");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("400 x 300");
        menuItem.setActionCommand("Resolution:400:300");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("640 x 480");
        menuItem.setActionCommand("Resolution:640:480");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("800 x 600");
        menuItem.setActionCommand("Resolution:800:600");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("1024 x 768");
        menuItem.setActionCommand("Resolution:1024:768");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("1280 x 1024");
        menuItem.setActionCommand("Resolution:1280:1024");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("1600 x 1200");
        menuItem.setActionCommand("Resolution:1600:1200");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("300 x 400");
        menuItem.setActionCommand("Resolution:300:400");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("480 x 640");
        menuItem.setActionCommand("Resolution:480:640");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("600 x 800");
        menuItem.setActionCommand("Resolution:600:800");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("768 x 1024");
        menuItem.setActionCommand("Resolution:768:1024");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("1024 x 1280");
        menuItem.setActionCommand("Resolution:1024:1280");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("1200 x 1600");
        menuItem.setActionCommand("Resolution:1200:1600");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Benutzerdefiniert...");
        menuItem.setActionCommand("Resolution");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        return menu;
    }
    
    /**
     * Men� erstellen: "Szene"
     * 
     * @return Erstelltes Men�.
     */
    private JMenu createSceneMenu()
    {
        JMenu menu = new JMenu("Szene");

        JMenuItem menuItem = new JMenuItem("Bowling-Szene");
        menuItem.setActionCommand("Scene:15");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Bowling-Kugel");
        menuItem.setActionCommand("Scene:16");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("2 Kugeln + Diamant (FirstPhongShader)");
        menuItem.setActionCommand("Scene:9");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("2 Kugeln + Diamant");
        menuItem.setActionCommand("Scene:10");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("2 Kugeln + Venus");
        menuItem.setActionCommand("Scene:11");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Hase");
        menuItem.setActionCommand("Scene:12");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Soft-Shadows");
        menuItem.setActionCommand("Scene:13");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Lichtbrechungs-Test");
        menuItem.setActionCommand("Scene:14");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("3 Kugeln - FlatShader");
        menuItem.setActionCommand("Scene:0");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("3 Kugeln - EyeLightShader");
        menuItem.setActionCommand("Scene:1");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("3 Kugeln - FirstPhongShader");
        menuItem.setActionCommand("Scene:2");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("3 Kugeln - Dreieck");
        menuItem.setActionCommand("Scene:3");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("3 Kugeln - DirectionalLight");
        menuItem.setActionCommand("Scene:4");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("3 Kugeln - PointLight");
        menuItem.setActionCommand("Scene:5");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("3 Kugeln - Shadow");
        menuItem.setActionCommand("Scene:6");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("3 Kugeln - MirrorShader");
        menuItem.setActionCommand("Scene:7");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("3 Kugeln - ReflectiveShader");
        menuItem.setActionCommand("Scene:8");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        return menu;
    }
    
    /**
     * Men� erstellen: "Kamera"
     * 
     * @return Erstelltes Men�.
     */
    private JMenu createCameraMenu()
    {
        JMenu menu = new JMenu("Kamera");

        menu.add(createCameraCompassMenu());
        menu.add(createCameraTestMenu());
        
        menu.addSeparator();

        JMenuItem menuItem = new JMenuItem("Gesamtsicht - Links");
        menuItem.setActionCommand("Camera:22");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Gesamtsicht - Rechts");
        menuItem.setActionCommand("Camera:23");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Aktive Bahnen");
        menuItem.setActionCommand("Camera:24");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Kegel-H�hle 1");
        menuItem.setActionCommand("Camera:25");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Kegel-H�hle 2");
        menuItem.setActionCommand("Camera:26");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Kegel-H�hle 3");
        menuItem.setActionCommand("Camera:27");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Kugel auf Kegel");
        menuItem.setActionCommand("Camera:30");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("R�cklaufbahn");
        menuItem.setActionCommand("Camera:28");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Blick nach Innen");
        menuItem.setActionCommand("Camera:29");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Blick auf die T�r");
        menuItem.setActionCommand("Camera:31");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Bowlingbahn 1");
        menuItem.setActionCommand("Camera:14");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Bowlingbahn 2");
        menuItem.setActionCommand("Camera:15");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Bowlingbahn 3");
        menuItem.setActionCommand("Camera:16");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Bowlingbahn 4");
        menuItem.setActionCommand("Camera:17");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Bowlingbahn 5");
        menuItem.setActionCommand("Camera:18");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Bowlingbahn 6");
        menuItem.setActionCommand("Camera:19");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Bowlingbahn 7");
        menuItem.setActionCommand("Camera:20");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Bowlingbahn: T�r");
        menuItem.setActionCommand("Camera:20");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        return menu;
    }
    
    /**
     * Men� erstellen: "Kamera" -> "Kompass"
     * 
     * @return Erstelltes Men�.
     */
    private JMenu createCameraCompassMenu()
    {
        JMenu menu = new JMenu("Kompass");

        JMenuItem menuItem = new JMenuItem("Nach Norden");
        menuItem.setActionCommand("Camera:5");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Nach S�den");
        menuItem.setActionCommand("Camera:6");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Nach Osten");
        menuItem.setActionCommand("Camera:7");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Nach Westen");
        menuItem.setActionCommand("Camera:8");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Nach Nord-Osten");
        menuItem.setActionCommand("Camera:9");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Nach Nord-Westen");
        menuItem.setActionCommand("Camera:10");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Nach S�d-Osten");
        menuItem.setActionCommand("Camera:11");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Nach S�d-Westen");
        menuItem.setActionCommand("Camera:12");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Lichtbrechungstest");
        menuItem.setActionCommand("Camera:13");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        return menu;
    }

    /**
     * Men� erstellen: "Kamera" -> "Test-Szene"
     * 
     * @return Erstelltes Men�.
     */
    private JMenu createCameraTestMenu()
    {
        JMenu menu = new JMenu("Test-Szene");

        JMenuItem menuItem = new JMenuItem("Ansicht von Oben");
        menuItem.setActionCommand("Camera:4");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Kamera f�r Lichtbrechungs-Test");
        menuItem.setActionCommand("Camera:13");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("FirstCamera");
        menuItem.setActionCommand("Camera:0");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("PerspectiveCamera - Position 1");
        menuItem.setActionCommand("Camera:1");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("PerspectiveCamera - Position 2");
        menuItem.setActionCommand("Camera:2");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("PerspectiveCamera - Position 3");
        menuItem.setActionCommand("Camera:3");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        return menu;
    }

    /**
     * Men� erstellen: "Ansicht"
     * 
     * @return Erstelltes Men�.
     */
    private JMenu createViewMenu()
    {
        JMenu menu = new JMenu("Ansicht");

        JMenuItem menuItem = new JMenuItem("Ans Fenster anpassen");
        menuItem.setActionCommand("ViewZoomWindow");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("800 %");
        menuItem.setActionCommand("ViewZoom:8.0");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("400 %");
        menuItem.setActionCommand("ViewZoom:4.0");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("200 %");
        menuItem.setActionCommand("ViewZoom:2.0");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("150 %");
        menuItem.setActionCommand("ViewZoom:1.5");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("100 %");
        menuItem.setActionCommand("ViewZoom:1.0");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("50 %");
        menuItem.setActionCommand("ViewZoom:0.5");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        return menu;
    }
    
    
    /**
     * Aktualisiert den Fenstertitel.
     */
    private void updateTitle()
    {
        long time = System.currentTimeMillis()-startTime;
        time = (long)((double) time /progress)-time;

        String title = TITLE;
        if (camera != null)
        {
            if (camera.isRendering())
                title += String.format(" (%1$.3f%% - %2$s)", progress* 100.0,
                        timetoString(time));
            else if (camera.isRendered())
                title += " (Fertig!)";
            else
                title += " (Unvollst�ndig.)";
            title += " - " + camera.getResX() + 'x' + camera.getResY();
        }
        setTitle(title);
    }
    
    private String timetoString(long time)
    {
        time /= 1000L;
        byte seconds = (byte)(time % 60L);
        time /= 60L;
        byte minutes = (byte)(time % 60L);
        time /= 60L;
        byte hours = (byte)(time % 24L);
        time /= 24L;
        
        String text = String.format("%1$02d:%2$02d:%3$02d", hours, minutes, seconds);
        return (time != 0) ? time + " Tage, " + text : text;
    }
    
    
    /**
     * Aktualisiert das Vorschaubild.
     */
    private void updateImage()
    {
        if (renderedImage == null)
            return;
        
        int width, height;
        int imageWidth = renderedImage.getWidth();
        int imageHeight = renderedImage.getHeight();
        
        switch (view)
        {
        case VIEW_ZOOM:
            // Scrollleisten aktivieren:
            scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            
            // Bildgr��e ermitteln:
            preview.setMinimumSize(scroller.getViewport().getWidth(), scroller.getViewport().getHeight());
            width = Math.round(zoomFactor* (float) imageWidth);
            height = Math.round(zoomFactor* (float) imageHeight);
            break;
        
        default:
        case VIEW_ZOOM_WINDOW:
            // Scrollleisten deaktivieren:
            scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

            Insets insets = scroller.getInsets();
            width = scroller.getWidth()-insets.left-insets.right;
            height = scroller.getHeight()-insets.top-insets.bottom;
            
            // Bildgr��e ermitteln:
            preview.setMinimumSize(width, height);
            if (height*imageWidth < width*imageHeight)
                width = height*imageWidth/imageHeight;
            else
                height = width*imageHeight/imageWidth;
            break;
        }
        
        // Bild setzen:
        if ((width <= 0) || (height <= 0))
            return;
        if ((width == imageWidth) && (height == imageHeight))
            preview.setImage(renderedImage);
        else
            preview.setImage(renderedImage.getScaledInstance(width, height, Image.SCALE_FAST));
    }
    
    
    @Override
    public void renderUpdate(RendererEvent event)
    {
        progress = event.getProgress();
        if (renderedImage == null)
            renderedImage = event.getSource().getImage();
        else
            event.getSource().getImage(renderedImage);
        updateTitle();
        updateImage();

        if (this.camera.getScene().update(System.currentTimeMillis())) {
            SwingUtilities.invokeLater(this::restartCamera);
        }
    }
    
    @Override
    public void renderFinished(RendererEvent event) {
        renderUpdate(event);
    }
    
    
    @Override
    public void windowOpened(WindowEvent e)
    {
        // Kamera initialisieren:
        windowStartet = true;
        startCamera();

    }
    
    @Override
    public void windowClosed(WindowEvent e)
    {
        // Kamera stoppen:
        stopCamera();
    }
    
    
    @Override
    public void componentResized(ComponentEvent e)
    {
        updateImage();
    }
    
    
    @Override
    public void actionPerformed(ActionEvent event)
    {
        String command = event.getActionCommand();
        String split[] = command.split(":");
        
        // Aktionen des Men�s: "Datei"
        if (command.equals("Resolution"))
            changeResolution();
        else if (split[0].equals("Resolution"))
            changeResolution(Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        else if (split[0].equals("SaveAs"))
            imageSaveAs(split[1]);
        else if (command.equals("Exit"))
            fileExit();
        
        // Aktionen des Men�s: "Szene"
        else if (split[0].equals("Scene"))
            changeScene(Integer.parseInt(split[1]));
        
        // Aktionen des Men�s: "Szene"
        else if (split[0].equals("Camera"))
            changeCamera(Integer.parseInt(split[1]));
        
        // Aktionen des Men�s: "Ansicht"
        else if (split[0].equals("ViewZoom"))
            viewZoom(Float.parseFloat(split[1]));
        else if (command.equals("ViewZoomWindow"))
            viewZoomWindow();
    }
    
    
    /**
     * �ffnet einen Dateiauswahl-Dialog und speichert das Vorschaubild unter
     * dem vom Benutzer angegebenen Dateinamen.
     * 
     * @param imageType Bezeichner f�r das Bildformat.
     */
    public void imageSaveAs(String imageType)
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Bild speichern unter...");
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileHidingEnabled(false);
        chooser.setFileFilter(new ImageFileFilter('.' + imageType));
        chooser.setSelectedFile(new File(IMAGE_FILE_NAME + '_' + System.currentTimeMillis() + '.' + imageType));
        
        for (;;)
        {
            // Dateiauswahl-Dialog anzeigen:
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

            // Pr�fen, ob gew�hlte Datei bereits existiert:
            if (chooser.getSelectedFile().exists())
            {
                // Die Datei existiert bereits. 
                // Benutzer fragen, ob sie �berschrieben werden soll:
                if (JOptionPane.showConfirmDialog(this, "Die gew�hlte Datei existiert bereits.\n" +
                    "Soll die Datei �berschrieben werden?", "Datei existiert",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
                    continue;
                }
            break;
        }

        // Bild speichern:
        try
        {
            if (!ImageIO.write(renderedImage, imageType, chooser.getSelectedFile()))
                throw new NullPointerException();
        }
        catch (Exception e)
        {
            // Das Thumbnail konnte nicht gespeichert werden.
            // Fehlerdialog anzeigen:
            JOptionPane.showMessageDialog(this, "Das Thumbnail konnte nicht gespeichert werden.",
                "Fehler beim Speichern", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Beendet das Programm.
     */
    public void fileExit()
    {
        dispose();
    }
    
    /**
     * Fragt dem Benutzer nach einer neuen Aufl�sung und �ndert diese
     * entsprechend.
     */
    public void changeResolution()
    {
        int width, height;

        for (;;)
        {
            // Benutzer nach der neuen Bildgr��e fragen:
            String input = JOptionPane.showInputDialog(this, "Neue Gr��e des gerenderten Bildes:\n(Beispiel: \"640 x 480\")",
                    "Bildgr��e �ndern", JOptionPane.QUESTION_MESSAGE);
            if (input == null)
            return;

            try
            {
                // Eingabe in Zahlen Umwandeln:
                String[] inputs = input.split("[^0-9]+", 2);
                width = Integer.parseInt(inputs[0]);
                height = Integer.parseInt(inputs[1]);
                
                // Pr�fen, ob die Dimensionen g�ltig sind:
                if ((width <= 0) || (height <= 0))
                    throw new InvalidFormatException();
            }
            catch (Exception e)
            {
                // Die eingegebenen Dimensionen werden nicht akzeptiert.
                // Fehlermeldung anzeigen:
                JOptionPane.showMessageDialog(this, "Die eingegebene Bildgr��e wird nicht akzeptiert.",
                    "Fehlerhafte Eingabe", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            break;
        }

        // Bild auf die gew�nschten Dimensionen vergr��ern:
        changeResolution(width, height);
    }
    
    /**
     * �ndert die Aufl�sung der Kamera.
     * 
     * @param resX Neue Horizontale Aufl�sung.
     * @param resY Neue Vertikale Aufl�sung.
     */
    public void changeResolution(int resX, int resY)
    {
        this.resX = resX;
        this.resY = resY;
        changeCamera(cameraId);
    }

    /**
     * �ndert die Szene.
     * 
     * @param sceneId ID der neuen Szene.
     */
    public void changeScene(int sceneId)
    {
        Scene scene;

        switch (sceneId)
        {
        case 0:     // FlatShader
            final Sphere s = new Sphere(new Vector3d(-2.0, 1.7, 0.0), 2.0, new ColorShader(ColorEx.RED));
            scene = new EfficientScene() {

                final double start = System.currentTimeMillis();

                @Override
                public boolean update(double t) {
                    //doesnt work yet:
                    /*
                    double c = (t - start) / 1000.0 ;
                    s.getCenter().set( 10*Math.sin(c), 10*Math.cos(c), 0.5f);
                    s.setShader(new ColorShader(new ColorEx(Color.getHSBColor((float)Math.random(), 0.5f, 0.5f))));
                    */
                    //remove(s);
                    //add(s);
                    return false;
                }
            };
            scene.add(s);
            scene.add(new Sphere(new Vector3d(1.0, -1.0, -1.0), 2.2, new ColorShader(ColorEx.GREEN)));
            scene.add(new Sphere(new Vector3d(3.0, 0.8, 2.0), 2.0, new ColorShader(ColorEx.BLUE)));
            scene.add(new InfinitePlane(new Vector3d(0.0, -1.0, 0.0), new Vector3d(0.0, 1.0, 0.0), new ColorShader(ColorEx.YELLOW)));
            //scene.add(new InfinitePlane(new Vector3d(0.0, -1.0, 0.0), new Vector3d(0.0, 1.0, 0.0), new WallShader(ColorEx.WHITE)));
            break;
            
        case 1:     // EyeLightShader
            scene = new EfficientScene();
            scene.add(new Sphere(new Vector3d(-2.0, 1.7, 0.0), 2.0, new EyeLightShader(new ColorShader(ColorEx.RED))));
            scene.add(new Sphere(new Vector3d(1.0, -1.0, -1.0), 2.2, new EyeLightShader(new ColorShader(ColorEx.GREEN))));
            scene.add(new Sphere(new Vector3d(3.0, 0.8, 2.0), 2.0, new EyeLightShader(new ColorShader(ColorEx.BLUE))));
            scene.add(new InfinitePlane(new Vector3d(0.0, -1.0, 0.0), new Vector3d(0.0, 1.0, 0.0), new EyeLightShader(new ColorShader(ColorEx.YELLOW))));
            break;
            
        case 2:     // FirstPhongShader
            scene = new EfficientScene();
            scene.add(new Sphere(new Vector3d(-2.0, 1.7, 0.0), 2.0, new FirstPhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.5f, 0.0f, 0.0f), new Vector3f(0.7f, 0.0f, 0.0f), new Vector3f(0.5f, 0.2f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Sphere(new Vector3d(1.0, -1.0, -1.0), 2.2, new FirstPhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.3f, 0.0f), new Vector3f(0.0f, 0.9f, 0.0f), new Vector3f(0.0f, 0.4f, 0.2f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Sphere(new Vector3d(3.0, 0.8, 2.0), 2.0, new FirstPhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.3f), new Vector3f(0.0f, 0.0f, 0.5f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new InfinitePlane(new Vector3d(0.0, -1.0, 0.0), new Vector3d(0.0, 1.0, 0.0), new FirstPhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
            break;
            
        case 3:     // Dreiecke
            scene = new EfficientScene();
            scene.add(new Sphere(new Vector3d(-2.0, 1.7, 0.0), 2.0, new FirstPhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.5f, 0.0f, 0.0f), new Vector3f(0.7f, 0.0f, 0.0f), new Vector3f(0.5f, 0.2f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Sphere(new Vector3d(1.0, -1.0, -1.0), 2.2, new FirstPhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.3f, 0.0f), new Vector3f(0.0f, 0.9f, 0.0f), new Vector3f(0.0f, 0.4f, 0.2f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Sphere(new Vector3d(3.0, 0.8, 2.0), 2.0, new FirstPhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.3f), new Vector3f(0.0f, 0.0f, 0.5f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Triangle(new Vector3d(-7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, -7.0), new FirstPhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Triangle(new Vector3d(7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, 7.0), new FirstPhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
            break;
            
        case 4:     // DirectionalLight
            scene = new EfficientScene();
            scene.add(new Sphere(new Vector3d(-2.0, 1.7, 0.0), 2.0, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.5f, 0.0f, 0.0f), new Vector3f(0.7f, 0.0f, 0.0f), new Vector3f(0.5f, 0.2f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Sphere(new Vector3d(1.0, -1.0, -1.0), 2.2, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.3f, 0.0f), new Vector3f(0.0f, 0.9f, 0.0f), new Vector3f(0.0f, 0.4f, 0.2f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Sphere(new Vector3d(3.0, 0.8, 2.0), 2.0, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.3f), new Vector3f(0.0f, 0.0f, 0.5f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Triangle(new Vector3d(-7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, -7.0), new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Triangle(new Vector3d(7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, 7.0), new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f))));

            scene.setAmbientLight(new ColorEx(0.2f, 0.2f, 0.2f));
            scene.add(new DirectionalLight(new Vector3d(1.0, -1.0, 0.0), new ColorEx(0.8f, 0.8f, 0.8f)));
            break;
            
        case 5:     // PointLight
            scene = new EfficientScene();
            scene.add(new Sphere(new Vector3d(-2.0, 1.7, 0.0), 2.0, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.5f, 0.0f, 0.0f), new Vector3f(0.7f, 0.0f, 0.0f), new Vector3f(0.5f, 0.2f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Sphere(new Vector3d(1.0, -1.0, -1.0), 2.2, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.3f, 0.0f), new Vector3f(0.0f, 0.9f, 0.0f), new Vector3f(0.0f, 0.4f, 0.2f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Sphere(new Vector3d(3.0, 0.8, 2.0), 2.0, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.3f), new Vector3f(0.0f, 0.0f, 0.5f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Triangle(new Vector3d(-7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, -7.0), new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Triangle(new Vector3d(7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, 7.0), new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f))));

            scene.setAmbientLight(new ColorEx(0.2f, 0.2f, 0.2f));
            scene.add(new PointLight(new Vector3d(-7.0, 4.0, -7.0), new ColorEx(20.0f, 20.0f, 20.0f)));
            break;
            
        case 6:     // Shadow
            scene = new EfficientScene();
            scene.add(new Sphere(new Vector3d(-2.0, 1.7, 0.0), 2.0, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.5f, 0.0f, 0.0f), new Vector3f(0.7f, 0.0f, 0.0f), new Vector3f(0.5f, 0.2f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Sphere(new Vector3d(1.0, -1.0, -1.0), 2.2, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.3f, 0.0f), new Vector3f(0.0f, 0.9f, 0.0f), new Vector3f(0.0f, 0.4f, 0.2f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Sphere(new Vector3d(3.0, 0.8, 2.0), 2.0, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.3f), new Vector3f(0.0f, 0.0f, 0.5f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Triangle(new Vector3d(-7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, -7.0), new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Triangle(new Vector3d(7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, 7.0), new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f))));

            scene.setAmbientLight(new ColorEx(0.2f, 0.2f, 0.2f));
            scene.add(new DirectionalLight(new Vector3d(1.0, -1.0, 0.0), new ColorEx(0.8f, 0.8f, 0.8f)));
            scene.add(new PointLight(new Vector3d(-7.0, 4.0, -7.0), new ColorEx(20.0f, 20.0f, 20.0f)));
            break;
            
        case 7:     // MirrorShader
            scene = new EfficientScene();
            scene.add(new Sphere(new Vector3d(-2.0, 1.7, 0.0), 2.0, new MirrorShader()));
            scene.add(new Sphere(new Vector3d(1.0, -1.0, -1.0), 2.2, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.3f, 0.0f), new Vector3f(0.0f, 0.9f, 0.0f), new Vector3f(0.0f, 0.4f, 0.2f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Sphere(new Vector3d(3.0, 0.8, 2.0), 2.0, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.3f), new Vector3f(0.0f, 0.0f, 0.5f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Triangle(new Vector3d(-7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, -7.0), new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
            scene.add(new Triangle(new Vector3d(7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, 7.0), new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f))));

            scene.setAmbientLight(new ColorEx(0.2f, 0.2f, 0.2f));
            scene.add(new DirectionalLight(new Vector3d(1.0, -1.0, 0.0), new ColorEx(0.8f, 0.8f, 0.8f)));
            scene.add(new PointLight(new Vector3d(-7.0, 4.0, -7.0), new ColorEx(20.0f, 20.0f, 20.0f)));
            break;
            
        case 8:     // ReflectiveShader
            scene = new EfficientScene();
            scene.add(new Sphere(new Vector3d(-2.0, 1.7, 0.0), 2.0, new ReflectiveShader(0.2f, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.5f, 0.0f, 0.0f), new Vector3f(0.7f, 0.0f, 0.0f), new Vector3f(0.5f, 0.2f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f)))));
            scene.add(new Sphere(new Vector3d(1.0, -1.0, -1.0), 2.2, new ReflectiveShader(0.2f, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.3f, 0.0f), new Vector3f(0.0f, 0.9f, 0.0f), new Vector3f(0.0f, 0.4f, 0.2f), new Vector3f(0.05f, 0.05f, 0.05f)))));
            scene.add(new Sphere(new Vector3d(3.0, 0.8, 2.0), 2.0, new ReflectiveShader(0.2f, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.3f), new Vector3f(0.0f, 0.0f, 0.5f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.05f, 0.05f, 0.05f)))));
            scene.add(new Triangle(new Vector3d(-7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, -7.0), new ReflectiveShader(0.2f, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f)))));
            scene.add(new Triangle(new Vector3d(7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, 7.0), new ReflectiveShader(0.2f, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f)))));

            scene.setAmbientLight(new ColorEx(0.2f, 0.2f, 0.2f));
            scene.add(new DirectionalLight(new Vector3d(1.0, -1.0, 0.0), new ColorEx(0.8f, 0.8f, 0.8f)));
            scene.add(new PointLight(new Vector3d(-7.0, 4.0, -7.0), new ColorEx(20.0f, 20.0f, 20.0f)));
            break;
            
        case 9:     // Diamant (FirstPhongShader)
            try
            {
                Transformation t = new Transformation();
                t.scale(0.9, 0.9, 0.9);
                t.move(1.0, 1.0, -3.0);
                
                OffObject off = new OffObject("off/dodecahedron.off", new ConcatShader(new FirstPhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.3f, 0.0f), new Vector3f(0.3f, 0.7f, 0.4f), new Vector3f(0.4f, 1.0f, 0.5f), new Vector3f(0.0f, 0.0f, 0.0f)), 0.7f, new RefractionShader(RefractionShader.INDEX_DIAMOND, new ColorEx(0.85f, 0.99f, 0.85f))));
                off.transform(t);
                
                scene = new EfficientScene();
                scene.add(off);
                scene.add(new Sphere(new Vector3d(-2.0, 1.7, 0.0), 2.0, new ReflectionShader(new FirstPhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.5f, 0.0f, 0.0f), new Vector3f(0.7f, 0.0f, 0.0f), new Vector3f(0.5f, 0.2f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f)))));
                scene.add(new Sphere(new Vector3d(3.0, 0.8, 2.0), 2.0, new FirstPhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.3f), new Vector3f(0.0f, 0.0f, 0.5f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
                scene.add(new Triangle(new Vector3d(-7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, -7.0), new ReflectionShader(new FirstPhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f)))));
                scene.add(new Triangle(new Vector3d(7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, 7.0), new ReflectionShader(new FirstPhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f)))));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            break;
            
        case 10:     // Diamant
            try
            {
                Transformation t = new Transformation();
                t.scale(0.9, 0.9, 0.9);
                t.move(1.0, 1.0, -3.0);
                
                OffObject off = new OffObject("off/dodecahedron.off", new ConcatShader(new FirstPhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.3f, 0.0f), new Vector3f(0.3f, 0.7f, 0.4f), new Vector3f(0.4f, 1.0f, 0.5f), new Vector3f(0.0f, 0.0f, 0.0f)), 0.7f, new RefractionShader(RefractionShader.INDEX_DIAMOND, new ColorEx(0.85f, 0.99f, 0.85f))));
                off.transform(t);
                
                scene = new EfficientScene();
                scene.add(off);
                scene.add(new Sphere(new Vector3d(-2.0, 1.7, 0.0), 2.0, new ReflectionShader(new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.5f, 0.0f, 0.0f), new Vector3f(0.7f, 0.0f, 0.0f), new Vector3f(0.5f, 0.2f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f)))));
                scene.add(new Sphere(new Vector3d(3.0, 0.8, 2.0), 2.0, new ReflectionShader(new PhongShader(new TextureShader("texture/earth.jpg"), new Vector3f(0.2f, 0.2f, 0.2f), new Vector3f(0.6f, 0.6f, 0.6f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 0.0f)))));
                scene.add(new Triangle(new Vector3d(-7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, -7.0), new ReflectionShader(new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f)))));
                scene.add(new Triangle(new Vector3d(7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, 7.0), new ReflectionShader(new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f)))));

                scene.setAmbientLight(new ColorEx(0.2f, 0.2f, 0.2f));
                scene.add(new DirectionalLight(new Vector3d(1.0,-1.0,0.0), new ColorEx(0.8f, 0.8f, 0.8f)));
                scene.add(new PointLight(new Vector3d(-7.0,4.0,-7.0), new ColorEx(20.0f, 20.0f, 20.0f)));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            break;
            
        case 11:     // Venus
            try
            {
                Transformation t = new Transformation();
                t.rotate(90.0, 0.0, 1.0, 0.0);
                t.scale(2.0, 2.0, 2.0);
                t.move(2.0, 2.0, -2.0);
                
                OffObject off = new OffObject("off/venus.off", new ConcatShader(new FirstPhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.3f, 0.0f), new Vector3f(0.3f, 0.7f, 0.4f), new Vector3f(0.4f, 1.0f, 0.5f), new Vector3f(0.0f, 0.0f, 0.0f)), 0.7f, new RefractionShader(RefractionShader.INDEX_DIAMOND, new ColorEx(0.85f, 0.99f, 0.85f))));
                off.transform(t);
                
                scene = new EfficientScene();
                scene.add(off);
                scene.add(new Sphere(new Vector3d(-2.0, 1.7, 0.0), 2.0, new ReflectionShader(new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.5f, 0.0f, 0.0f), new Vector3f(0.7f, 0.0f, 0.0f), new Vector3f(0.5f, 0.2f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f)))));
                scene.add(new Sphere(new Vector3d(3.0, 0.8, 2.0), 2.0, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.3f), new Vector3f(0.0f, 0.0f, 0.5f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
                scene.add(new Triangle(new Vector3d(-7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, -7.0), new ReflectionShader(new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f)))));
                scene.add(new Triangle(new Vector3d(7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, 7.0), new ReflectionShader(new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.05f, 0.05f, 0.05f)))));

                scene.setAmbientLight(new ColorEx(0.2f, 0.2f, 0.2f));
                scene.add(new DirectionalLight(new Vector3d(1.0,-1.0,0.0), new ColorEx(0.8f, 0.8f, 0.8f)));
                scene.add(new PointLight(new Vector3d(-7.0,4.0,-7.0), new ColorEx(20.0f, 20.0f, 20.0f)));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            break;
            
        case 12:     // Hase
            try
            {
                Transformation t = new Transformation();
                
                String texture = "texture/synth11.jpg";
                Triangle t1 = new Triangle(new Vector3d(-7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, -7.0), new ReflectionShader(new PhongShader(new TextureShader(texture), new Vector3f(0.1f, 0.1f, 0.1f), new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 0.0f))));
                Triangle t2 = new Triangle(new Vector3d(7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, 7.0), new ReflectionShader(new PhongShader(new TextureShader(texture), new Vector3f(0.1f, 0.1f, 0.1f), new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 0.0f))));
                
                t1.setTextureCoords(new Vector2d(1.0, 1.0), new Vector2d(1.0, 0.0), new Vector2d(0.0, 0.0));
                t2.setTextureCoords(new Vector2d(0.0, 0.0), new Vector2d(0.0, 1.0), new Vector2d(1.0, 1.0));
                t.reset();
                t.scale(1.5, 1.5, 0.0);
                t1.transformTexture(t);
                t2.transformTexture(t);

                PlyObject ply = new PlyObject("ply/bunny.ply", new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.1f, 0.0f), new Vector3f(0.0f, 0.4f, 0.0f), new Vector3f(0.0f, 0.7f, 0.2f), new Vector3f(0.0f, 0.0f, 0.0f)));
                ply.normalize();
                t.reset();
                t.scale(3.0, 3.0, 3.0);
                t.move(2.0, 1.3, -3.5);
                ply.transform(t);
                
                scene = new EfficientScene();
                scene.add(new Sphere(new Vector3d(), 30.0, new TextureShader("texture/univ02.jpg")));
                scene.add(ply);
                scene.add(new Sphere(new Vector3d(-2.0, 1.7, 0.0), 2.0, new ConcatShader(new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.1f, 0.0f, 0.0f), new Vector3f(0.5f, 0.0f, 0.0f), new Vector3f(1.0f, 0.4f, 0.2f), new Vector3f(0.0f, 0.0f, 0.0f)), 0.8f, new RefractionShader(RefractionShader.INDEX_GLASS, new ColorEx(0.95f, 0.85f, 0.85f)))));
                scene.add(new Sphere(new Vector3d(3.0, 0.8, 2.0), 2.0, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.3f), new Vector3f(0.0f, 0.0f, 0.5f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.05f, 0.05f, 0.05f))));
                scene.add(t1);
                scene.add(t2);

                scene.setAmbientLight(new ColorEx(0.2f, 0.2f, 0.2f));
                //scene.add(new DirectionalLight(new Vector3d(1.0, -1.0, 0.0), new ColorEx(0.8f, 0.8f, 0.8f)));
                scene.add(new PointLight(new Vector3d(-7.0, 4.0, -7.0), new ColorEx(10.0f, 10.0f, 10.0f)));
                scene.add(new PointLight(new Vector3d(-7.0, 4.0, 7.0), new ColorEx(6.0f, 6.0f, 6.0f)));
                scene.add(new PointLight(new Vector3d(7.0, 4.0, 7.0), new ColorEx(8.0f, 8.0f, 8.0f)));
                scene.add(new PointLight(new Vector3d(7.0, 4.0, -7.0), new ColorEx(11.0f, 11.0f, 11.0f)));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            break;
            
        case 13:     // Soft-Shadows 
            try 
            { 
                Transformation t = new Transformation();
                t.scale(0.9, 0.9, 0.9); 
                t.move(1.0, 1.0, -3.0); 
                 
                //OffObject off = new OffObject("off/cube.off", new ConcatShader(new FirstPhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(1.0f, 1.0f, 1.0f)), 1.0f, new RefractionShader(RefractionShader.INDEX_DIAMOND, new ColorEx(ColorEx.WHITE)))); 
                OffObject off = new OffObject("off/cube.off", new ConcatShader(new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.3f, 0.0f), new Vector3f(0.3f, 0.7f, 0.4f), new Vector3f(0.4f, 0.4f, 0.4f), new Vector3f(0.0f, 0.0f, 0.0f), 5.0F), 0.0f, new RefractionShader(RefractionShader.INDEX_DIAMOND, new ColorEx(ColorEx.WHITE))));
                //OffObject off = new OffObject("off/dodecahedron.off", new ConcatShader(new FirstPhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.3f, 0.0f), new Vector3f(0.3f, 0.7f, 0.4f), new Vector3f(0.4f, 1.0f, 0.5f), new Vector3f(0.0f, 0.0f, 0.0f)), 0.7f, new RefractionShader(RefractionShader.SOL_DIAMOND, new ColorEx(0.85f, 0.99f, 0.85f)))); 
                off.transform(t);
                
                scene = new EfficientScene();
                //scene.add(off);
                scene.add(new Sphere(new Vector3d(-2.0, 1.7, 0.0), 2.0, new ReflectionShader(new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.5f, 0.0f, 0.0f), new Vector3f(0.7f, 0.0f, 0.0f), new Vector3f(0.7f, 0.7f, 0.7f), new Vector3f(0.05f, 0.05f, 0.05f), 10.0F))));
                scene.add(new Sphere(new Vector3d(3.0, 0.8, 2.0), 2.0, new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.3f), new Vector3f(0.0f, 0.0f, 0.5f), new Vector3f(0.7f, 0.7f, 0.7f), new Vector3f(0.05f, 0.05f, 0.05f), 10.0F)));
                scene.add(new Triangle(new Vector3d(-7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, -7.0), new ReflectionShader(new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.4f, 0.4f, 0.4f), new Vector3f(0.05f, 0.05f, 0.05f), 5.0F))));
                scene.add(new Triangle(new Vector3d(7.0, -1.0, -7.0), new Vector3d(7.0, -1.0, 7.0), new Vector3d(-7.0, -1.0, 7.0), new ReflectionShader(new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.9f, 0.9f, 0.0f), new Vector3f(0.4f, 0.4f, 0.4f), new Vector3f(0.05f, 0.05f, 0.05f), 5.0F))));
     
                scene.setAmbientLight(new ColorEx(0.2f, 0.2f, 0.2f));
                // scene.add(new DirectionalLight(new Vector3d(1.0,-1.0,0.0), new ColorEx(0.1f, 0.1f, 0.1f))); 
                // scene.add(new PointLight(new Vector3d(-7.0,4.0,-7.0), new ColorEx(10.0f, 10.0f, 10.0f))); 
                // scene.add(new AreaLight(new Vector3d(0.0,8.0,0.0),new Vector3d(1.5,0.0,0.0),new Vector3d(0.0,0.0,1.5), new ColorEx(0.8f, 0.8f, 0.8f))); 
                // scene.add(new Sphere(new Vector3d(1.0,2.0,0.0), 0.25 , new LightSourceShader(new ColorEx(1.0f, 1.0f, 1.0f))));
                
                
                //scene.add(new AreaLight(new Vector3d(1.0, 3.0, 0.0), new Vector3d(0.5, 0.0, 0.0), new Vector3d(0.0, 0.0, 3.0), new ColorEx(12.0f, 12.0f, 12.0f)));
                
                scene.add(new SphereLight(new Vector3d(1.0, 3.0, 0.0), 0.7,  new ColorEx(12.0f, 12.0f, 12.0f)));
            } 
            catch (Exception e) 
            { 
                throw new RuntimeException(e); 
            }
            break;
            
        case 14:     // Lichtbrechungs-Test
            try
            {
                scene = new EfficientScene();

                Shader wallRed = new PhongShader(new ColorShader(ColorEx.RED), new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.7f, 0.7f, 0.7f));
                Shader wallBlue = new PhongShader(new ColorShader(ColorEx.BLUE), new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.7f, 0.7f, 0.7f));
                Shader wallGray = new PhongShader(new ColorShader(ColorEx.GRAY), new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.7f, 0.7f, 0.7f));
                
                
                // Linke Wand:
                scene.add(new Triangle(new Vector3d(-1.0, -1.0, 1.0), new Vector3d(-1.0, -1.0, -1.0), new Vector3d(-1.0, 1.0, 1.0), wallRed));
                scene.add(new Triangle(new Vector3d(-1.0, 1.0, 1.0), new Vector3d(-1.0, -1.0, -1.0), new Vector3d(-1.0, 1.0, -1.0), wallRed));
                
                // Rechte Wand:
                scene.add(new Triangle(new Vector3d(1.0, -1.0, -1.0), new Vector3d(1.0, -1.0, 1.0), new Vector3d(1.0, 1.0, 1.0), wallBlue));
                scene.add(new Triangle(new Vector3d(1.0, 1.0, 1.0), new Vector3d(1.0, 1.0, -1.0), new Vector3d(1.0, -1.0, -1.0), wallBlue));

                // Hintere Wand:
                scene.add(new Triangle(new Vector3d(-1.0, -1.0, -1.0), new Vector3d(1.0, -1.0, -1.0), new Vector3d(-1.0, 1.0, -1.0), wallGray));
                scene.add(new Triangle(new Vector3d(-1.0, 1.0, -1.0), new Vector3d(1.0, -1.0, -1.0), new Vector3d(1.0, 1.0, -1.0), wallGray));

                // Obere Wand:
                scene.add(new Triangle(new Vector3d(-1.0, 1.0, 1.0), new Vector3d(-1.0, 1.0, -1.0), new Vector3d(1.0, 1.0, 1.0), wallGray));
                scene.add(new Triangle(new Vector3d(1.0, 1.0, 1.0), new Vector3d(-1.0, 1.0, -1.0), new Vector3d(1.0, 1.0, -1.0), wallGray));

                // Untere Wand:
                scene.add(new Triangle(new Vector3d(-1.0, -1.0, 1.0), new Vector3d(1.0, -1.0, 1.0), new Vector3d(-1.0, -1.0, -1.0), wallGray));
                scene.add(new Triangle(new Vector3d(-1.0, -1.0, -1.0), new Vector3d(1.0, -1.0, 1.0), new Vector3d(1.0, -1.0, -1.0), wallGray));

                // Licht:
                scene.add(new AreaLight(new Vector3d(0.0, 0.999999, 0.0), new Vector3d(0.2, 0.0, 0.0), new Vector3d(0.0, 0.0, 0.2), new ColorEx(3.0f, 3.0f, 3.0f)));
                
                // Kugel mit Spiegelung:
                Sphere rs = new Sphere(new Vector3d(-0.5, -0.65, -0.5), 0.35, new MirrorShader());
                rs.setNormalEffect(new RoughNormalEffect(0.03f));
                scene.add(rs);
                
                // Kugel mit Lichtbrechung:
                rs = new Sphere(new Vector3d(0.5, -0.65, 0.0), 0.35, new RefractionShader(RefractionShader.INDEX_GLASS, new ColorEx(0.9f, 0.9f, 0.9f)));
                rs.setNormalEffect(new RoughNormalEffect(0.1f));
                scene.add(rs);
                
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            break;
            
        case 15:     // Bowling-Szene
            scene = new BowlingScene();
            break;
            
        case 16:     // Bowling-Kugel
            try
            {
                Transformation t = new Transformation();
                t.scale(3.0, 3.0, 3.0);
                t.rotate(190.0, 1.0, 0.0, 0.0);
                
                PlyObject ply = new PlyObject("ply/bowl.ply", new PhongShader(new ColorShader(ColorEx.WHITE), new Vector3f(0.1f, 0.1f, 0.1f), new Vector3f(0.4f, 0.4f, 0.4f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.0f, 0.0f, 0.0f)), true, new RoughNormalEffect(0.2f));
                ply.center();
                ply.transform(t);
                
                scene = new EfficientScene();
                scene.add(ply);

                scene.setAmbientLight(new ColorEx(0.2f, 0.2f, 0.2f));
                //scene.add(new DirectionalLight(new Vector3d(1.0, -1.0, 0.0), new ColorEx(0.8f, 0.8f, 0.8f)));
                scene.add(new PointLight(new Vector3d(-7.0, 1.0, -7.0), new ColorEx(5.0f, 5.0f, 5.0f)));
                scene.add(new PointLight(new Vector3d(-7.0, 1.0, 7.0), new ColorEx(3.0f, 3.0f, 3.0f)));
                scene.add(new PointLight(new Vector3d(7.0, 1.0, 7.0), new ColorEx(4.0f, 4.0f, 4.0f)));
                scene.add(new PointLight(new Vector3d(7.0, 1.0, -7.0), new ColorEx(5.5f, 5.5f, 5.5f)));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            break;
            
        default:
            throw new IllegalArgumentException();
        }
        
        // Szene setzen:
        setCameraScene(this.camera, scene);
        this.sceneId = sceneId;
    }
    
    /**
     * �ndert die Kamera.
     * 
     * @param cameraId ID der neuen Kamera.
     */
    public void changeCamera(int cameraId)
    {
        AsyncCamera camera;

        switch (cameraId)
        {
        case 0:     // FirstCamera
            camera = new FirstCamera(resX, resY);
            break;
            
        case 1:     // PerspectiveCamera - 1
            camera = new PerspectiveCamera(resX, resY, 60.0,
                    new Vector3d(0.0, 0.0, -10.0),
                    new Vector3d(0.0, 0.0, 1.0),
                    new Vector3d(0.0, 1.0, 0.0)
                );
            break;
            
        case 2:     // PerspectiveCamera - 2
            camera = new PerspectiveCamera(resX, resY, 60.0,
                    new Vector3d(-8.0, 3.0, -8.0),
                    new Vector3d(1.0, -0.1, 1.0),
                    new Vector3d(0.0, 1.0, 0.0)
                );
            break;
            
        case 3:     // PerspectiveCamera - 3
            camera = new PerspectiveCamera(resX, resY, 60.0,
                    new Vector3d(-8.0, 3.0, -8.0),
                    new Vector3d(1.0, -0.1, 1.0),
                    new Vector3d(1.0, 1.0, 0.0)
                );
            break;
            
        case 4:     // Ansicht von Oben
            camera = new PerspectiveCamera(resX, resY, 10.0, 30.0,
                    new Vector3d(0.0, 10.0, 0.0),
                    new Vector3d(0.0, -1.0, 0.0),
                    new Vector3d(0.5, 0.0, 1.0)
                );
            break;
            
        case 5:     // Kompass: Nach N
            camera = new PerspectiveCamera(resX, resY, 60.0,
                    new Vector3d(0.0, 3.0, 11.0),
                    new Vector3d(0.0, -0.1, -1.0),
                    new Vector3d(0.0, 1.0, 0.0)
                );
            break;
            
        case 6:     // Kompass: Nach S
            camera = new PerspectiveCamera(resX, resY, 60.0,
                    new Vector3d(0.0, 3.0, -11.0),
                    new Vector3d(0.0, -0.1, 1.0),
                    new Vector3d(0.0, 1.0, 0.0)
                );
            break;
            
        case 7:     // Kompass: Nach O
            camera = new PerspectiveCamera(resX, resY, 60.0,
                    new Vector3d(-11.0, 3.0, 0.0),
                    new Vector3d(1.0, -0.1, 0.0),
                    new Vector3d(0.0, 1.0, 0.0)
                );
            break;
            
        case 8:     // Kompass: Nach W
            camera = new PerspectiveCamera(resX, resY, 60.0,
                    new Vector3d(11.0, 3.0, 0.0),
                    new Vector3d(-1.0, -0.1, 0.0),
                    new Vector3d(0.0, 1.0, 0.0)
                );
            break;
            
        case 9:     // Kompass: Nach NO
            camera = new PerspectiveCamera(resX, resY, 60.0,
                    new Vector3d(-8.0, 3.0, 8.0),
                    new Vector3d(1.0, -0.1, -1.0),
                    new Vector3d(0.0, 1.0, 0.0)
                );
            break;
            
        case 10:     // Kompass: Nach NW
            camera = new PerspectiveCamera(resX, resY, 60.0,
                    new Vector3d(8.0, 3.0, 8.0),
                    new Vector3d(-1.0, -0.1, -1.0),
                    new Vector3d(0.0, 1.0, 0.0)
                );
            break;
            
        case 11:     // Kompass: Nach SO
            camera = new PerspectiveCamera(resX, resY, 60.0,
                    new Vector3d(-8.0, 3.0, -8.0),
                    new Vector3d(1.0, -0.1, 1.0),
                    new Vector3d(0.0, 1.0, 0.0)
                );
            break;
            
        case 12:     // Kompass: Nach SW
            camera = new PerspectiveCamera(resX, resY, 60.0,
                    new Vector3d(8.0, 3.0, -8.0),
                    new Vector3d(-1.0, -0.1, 1.0),
                    new Vector3d(0.0, 1.0, 0.0)
                );
            break;
            
        case 13:     // Kamera f�r Lichtbrechungs-Test
            camera = new PerspectiveCamera(resX, resY, 60.0,
                    new Vector3d(0.0, 0.0, 2.3),
                    new Vector3d(0.0, 0.0, -1.0),
                    new Vector3d(0.0, 1.0, 0.0)
                );
            break;
            
        case 14:     // Bowlingbahn Cam 1
        	camera = new PerspectiveCamera(resX, resY, 48.0,
			new Vector3d(6.5, -3.0, 5.0),
			new Vector3d(-1.0, -0.4, -1.0),
			new Vector3d(0.0, 1.0, 0.0)
            );
        	break;
            
        case 15:     // Bowlingbahn Cam 2
        	camera = new PerspectiveCamera(resX, resY, 48.0,
        			new Vector3d(6.5, -3.0, 5.0),
        			new Vector3d(-1.0, -0.4, 0.0),
        			new Vector3d(0.0, 1.0, 0.0)
        	);
        	break;
            
        case 16:     // Bowlingbahn Cam 3 
            camera = new PerspectiveCamera(resX, resY, 48.0, 
                      new Vector3d(-3.0, -5.0, -1.0),
                      new Vector3d(1.0, 0.3, 1.0), 
                      new Vector3d(0.0, 1.0, 0.0) 
            ); 
        	break;
            
        case 17:     // Bowlingbahn Cam 4 
            camera = new PerspectiveCamera(resX, resY, 48.0, 
                      new Vector3d(0.0, -3.0, 4.0),
                      new Vector3d(0.0, -0.3, -1.0), 
                      new Vector3d(0.0, 1.0, 0.0) 
            ); 
            break;
            
        case 18:     // Bowlingbahn Cam 5 
        	camera = new PerspectiveCamera(resX, resY, 48.0,
        			new Vector3d(-1.7, -4.5, -1.5),
        			new Vector3d(0.0, -0.2, -1.0),
        			new Vector3d(0.0, 1.0, 0.0)
        	);
        	break;
        	
        case 19:     // Bowlingbahn Cam 6
            camera = new PerspectiveCamera(resX, resY, 48.0, 
                      new Vector3d(-1.7, -3.0, 5.0),
                      new Vector3d(0.4, -0.3, -1.0), 
                      new Vector3d(0.0, 1.0, 0.0) 
            ); 
        	break;
            
        case 20:     // Bowlingbahn Cam 7 
            camera = new PerspectiveCamera(resX, resY, 48.0,
                new Vector3d(6.5, -5.0, 5.0),
                new Vector3d(-1.0, 0.0, -1.0),
                new Vector3d(0.0, 1.0, 0.0)
            );
            break;

        case 21:     // Bowlingbahn: T�r
            camera = new PerspectiveCamera(resX, resY, 48.0,
                    new Vector3d(5.5, -4.45, 3.0),
                    new Vector3d(0.0, 0.0, 1.0),
                    new Vector3d(0.0, 1.0, 0.0)
            );
            break;
            
        case 22:     // Bowlingbahn: Gesamtsicht - Links
            camera = new PerspectiveCamera(resX, resY, 90.0,
                    new Vector3d(0.48407353831851324, -3.018759713273284, 7.824136399028278),
                    new Vector3d(0.2806038610573204, -0.06880636875619817, -0.9573542483210515),
                    new Vector3d(0.016031995929637704, 0.9976240590274125, -0.06700158174435003)
            );
            break;

        case 23:     // Bowlingbahn: Gesamtsicht - Rechts
            camera = new PerspectiveCamera(resX, resY, 90.0,
                    new Vector3d(3.3415185643027585, -2.491147420136292, 7.76233135809683),
                    new Vector3d(-0.1613922052704986, -0.1435433723086555, -0.9763953381414523),
                    new Vector3d(-0.0032002921789601223, 0.9894364993202709, -0.14493161126188117)
            );
            break;

        case 24:     // Aktive Bahnen
            camera = new PerspectiveCamera(resX, resY, 40.0,
                    new Vector3d(1.5413462747371491, -2.991910952084204, 7.467307261538312),
                    new Vector3d(-0.14682095498853334, -0.35785700613144567, -0.9221615749633957),
                    new Vector3d(-0.03337822838551267, 0.9335258922036488, -0.3569528014392296)
            );
            break;

        case 25:     // Kegelh�hle 1
            camera = new PerspectiveCamera(resX, resY, 60.0,
                    new Vector3d(2.287794290473877, -4.961842907704207, -4.983976265535724),
                    new Vector3d(-0.06992685270634139, -0.091858046747063, 0.9933138147224554),
                    new Vector3d(-0.00650235031485027, 0.9957721107667741, 0.09162763152846623)
            );
            break;

        case 26:     // Kegelh�hle 2
            camera = new PerspectiveCamera(resX, resY, 60.0,
                    new Vector3d(2.2108868356139415, -5.17254733310003, -4.555834092500915),
                    new Vector3d(0.042023092823442486, -0.08196841148673485, 0.9957485823178154),
                    new Vector3d(1.7127103451264575E-4, 0.9966295411617966, 0.08203370252465271)
            );
            break;

        case 27:     // Kegelh�hle 3
            camera = new PerspectiveCamera(resX, resY, 100.0,
                    new Vector3d(-1.7373856956200784, -4.404969797366187, -3.8345626302168077),
                    new Vector3d(0.022946872129845395, -0.9226012335361213, 0.38507194774124304),
                    new Vector3d(-0.02616175040147755, 0.3844873065066896, 0.9227594886812676)
            );
            break;

        case 28:    // R�cklaufbahn
            camera = new PerspectiveCamera(resX, resY, 60.0,
                    new Vector3d(-0.017770551068791232, -4.6357380415993505, -2.6108089048414507),
                    new Vector3d(0.2074487052869382, 0.003854990788496639, 0.9782383010907354),
                    new Vector3d(-0.029360239110590207, 0.9995662784849537, 0.002287198110993075)
            );
            break;

        case 29:    // Blich nach Innen
            camera = new PerspectiveCamera(resX, resY, 50.0,
                    new Vector3d(6.358395206522472, -4.060900500976465, 10.30033525354931),
                    new Vector3d(-0.2654053758067943, -0.12922957803579257, -0.9554369171503306),
                    new Vector3d(-0.04180645080624365, 0.9915868675199432, -0.12250593795051956)
            );
            break;

        case 30:    // Kugel auf Kegel
            camera = new PerspectiveCamera(resX, resY, 30.0,
                    new Vector3d(-1.1000401672362226, -5.087014697874249, -1.7290776544906388),
                    new Vector3d(-0.24468540658034604, -0.04019086411753281, -0.9687691914218264),
                    new Vector3d(0.020058011513229602, 0.9987169172374077, -0.04649941287745099)
            );
            break;
            
        case 31:    // Blick auf die T�r
            camera = new PerspectiveCamera(resX, resY, 70.0,
                    new Vector3d(-0.3332293474357643, -3.298876452112479, 2.2210001610458563),
                    new Vector3d(0.49936848376860654, -0.26712210351156884, 0.8241825642624807),
                    new Vector3d(0.16071457758041097, 0.963333898784952, 0.2148455817674904)
            );
            break;
            
        default:
            throw new IllegalArgumentException();
        }
        
        // Kamera setzen:
        setCameraScene(camera, (this.camera == null) ? null : this.camera.getScene());
        this.cameraId = cameraId;
    }
    
    /**
     * �ndert die Kamera und die Szene.
     * 
     * @param camera Neue Kamera.
     * @param scene Neue Szene.
     */
    private void setCameraScene(AsyncCamera camera, Scene scene)
    {
        stopCamera();
        camera.setScene(scene);
        this.camera = camera;
        startCamera();
    }
    
    /**
     * Zeigt das Vorschaubild mit einem variablen Zoom-Faktor an.
     * 
     * @param zoomFactor Zoom-Faktor f�r das Vorschaubild.
     */
    public void viewZoom(float zoomFactor)
    {
        view = VIEW_ZOOM;
        this.zoomFactor = zoomFactor;
        updateImage();
    }

    /**
     * Zeigt das Vorschaubild so an, dass es komplett in das Fenster passt.
     */
    public void viewZoomWindow()
    {
        view = VIEW_ZOOM_WINDOW;
        updateImage();
    }
    

    @Override
    public void windowClosing(WindowEvent e) {}
    @Override
    public void windowActivated(WindowEvent e) {}
    @Override
    public void windowDeactivated(WindowEvent e) {}
    @Override
    public void windowDeiconified(WindowEvent e) {}
    @Override
    public void windowIconified(WindowEvent e) {}
    @Override
    public void componentHidden(ComponentEvent e) {}
    @Override
    public void componentMoved(ComponentEvent e) {}
    @Override
    public void componentShown(ComponentEvent e) {}
    
    
    protected static class ImageFileFilter extends javax.swing.filechooser.FileFilter
    {
        protected final String fileType;
        
        
        /**
         * Erzeugt einen neuen <code>ImageFileFilter</code>.
         * 
         * @param fileType Nur Dateien dieses Typs werden angezeigt.
         */
        public ImageFileFilter(String fileType)
        {
            this.fileType = fileType;
        }
        
        @Override
        public boolean accept(File file)
        {
            // Verzeichnisse immer anzeigen:
            if (file.isDirectory())
                return true;
            
            // Nur Dateien mit der erw�nschten Erweiterung anzeigen:
            String fileName = file.getName();
            int index = fileName.lastIndexOf('.');
            if ((index < 0) || (index >= fileName.length()))
                return false;
            return fileName.substring(index).equalsIgnoreCase(fileType);
        }
        
        @Override
        public String getDescription()
        {
            return "Bild-Dateien (*" + fileType + ')';
        }
    }
}