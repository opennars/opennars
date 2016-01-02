/*
 * PreviewPanel.java                      STATUS: Vorl�ufig abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.main;

/**
 * Dieses Panel zeigt ein Vorschaubild an.
 * 
 * @author Mathias Kosch
 *
 */
public class PreviewPane extends JPanel
implements Scrollable
{
    private static final long serialVersionUID = 1L;

    /** Hintergrundfarbe, f�r nicht gezeichnete Bereiche. */
    private final static Color BACKGROUND_COLOR = Color.GRAY;
    
    /** Vorschaubild. */
    private Image image = null;
    /** Minimale Gr��e des Vorschaubildes. */
    private int minWidth = 0, minHeight = 0;
    
    
    /**
     * Erzeugt ein neues Vorschaubild.
     */
    public PreviewPane()
    {
        super(false);
    }
    
    
    /**
     * Zeigt das Vorschaubild an.
     * 
     * @param g Grafik-Kontekt, in den das Bild gezeichnet wird.
     */
    @Override
    public void paint(Graphics g)
    {
        // Gr��e des Zeichenbereichs erfragen:
        Rectangle bounds = g.getClipBounds();
           
        // Hintergrundfarbe setzen:
        g.setColor(BACKGROUND_COLOR);
        
        if (image == null)
        {
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            return;
        }

        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        int offsetX = (imageWidth < minWidth) ? (minWidth-imageWidth)/2 : 0;
        int offsetY = (imageHeight < minHeight) ? (minHeight-imageHeight)/2 : 0;
        
        // Darstellungsrechteck des Bildes berechnen:
        int imageX1 = bounds.x-offsetX;
        int imageX2 = bounds.x+bounds.width-offsetX;
        int imageY1 = bounds.y-offsetY;
        int imageY2 = bounds.y+bounds.height-offsetY;
        
        // Darstellungsrechteck auf realen Bildbereich begrenzen:
        if (imageX1 < 0)
        {
            g.fillRect(bounds.x, bounds.y, -imageX1, bounds.height);
            imageX1 = 0;
        }
        if (imageY1 < 0)
        {
            g.fillRect(bounds.x, bounds.y, bounds.width, -imageY1);
            imageY1 = 0;
        }
        if (imageX2 > imageWidth)
        {
            g.fillRect(imageWidth+offsetX, bounds.y, imageX2-imageWidth, bounds.height);
            imageX2 = imageWidth;
        }
        if (imageY2 > imageHeight)
        {
            g.fillRect(bounds.x, imageHeight+offsetY, bounds.width, imageY2-imageHeight);
            imageY2 = imageHeight;
        }
        
        // Bild darstellen:
        if ((imageX2-imageX1 > 0) && (imageY2-imageY1 > 0))
        {
            g.drawImage(image, bounds.x, bounds.y,
                    bounds.x+bounds.width, bounds.y+bounds.height,
                    bounds.x-offsetX, bounds.y-offsetY,
                    bounds.x+bounds.width-offsetX, bounds.y+bounds.height-offsetY, null);
        }
    }
    
    
    /**
     * Setzt die minimale Gr��e des Fensters.
     * 
     * @param width Minimale Breite des Fensters in Pixel.
     * @param height Minimale H�he des Fensters in Pixel.
     */
    public void setMinimumSize(int width, int height)
    {
        minWidth = width;
        minHeight = height;
    }
    
    /**
     * Setzt das darzustellende Vorschaubild.
     * 
     * @param image Darzustellendes Vorschaubild.
     */
    public void setImage(Image image)
    {
        this.image = image;
        revalidate();
        repaint();
    }
    
    
    @Override
    public Dimension getPreferredSize()
    {
        if (image == null)
            return new Dimension(0, 0);
        return new Dimension(Math.max(image.getWidth(null), minWidth),
                Math.max(image.getHeight(null), minHeight));
    }
    
    @Override
    public Dimension getPreferredScrollableViewportSize()
    {
        return getPreferredSize();
    }
    
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return 50;
    }
    
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        if (direction == SwingConstants.VERTICAL)
            return getParent().getWidth();
        if (direction == SwingConstants.HORIZONTAL)
            return getParent().getHeight();
        return 0;
    }
    
    @Override
    public boolean getScrollableTracksViewportHeight()
    {
        return false;
    }
    
    @Override
    public boolean getScrollableTracksViewportWidth()
    {
        return false;
    }    
}