/*
 * RendererEvent.java                               STATUS: Abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.cameras;

import java.util.EventObject;


/**
 * Diese Klasse implementiert ein Ereignis-Objekt f�r das Renderer-Ereignis.
 * 
 * @author Mathias Kosch
 * @see RendererListener
 *
 */
public class RendererEvent extends EventObject
{
    /**
     * Serielle Standardversions-ID:
     */
    private static final long serialVersionUID = 1L;
    
    
    private final double progress;
    
    /**
     * Erstellt ein neues <code>RendererEvent</code>-Objekt,
     * welches Informationen zu einem aufgetretenen Ereignis speichert.
     * 
     * @param source Quelle des Ereignisses.
     * @param progress Fortschritt des Ereignisses.
     */
    public RendererEvent(AsyncCamera source, double progress)
    {
        super(source);
        this.progress = progress;
    }
    
    /**
     * Liefert die Quelle des Ereignisses.<br>
     * Die Quelle ist ein Objekt auf eine <code>AsyncCamera</code>.
     * 
     * @return Quelle des Ereignisses.
     */
    @Override
    public AsyncCamera getSource()
    {
        return (AsyncCamera)source;
    }
    
    /**
     * Liefert den aktuellen fortschritt des Ereignisses.
     * 
     * @return Fortschritt des Ereignisses.
     */
    public double getProgress()
    {
        return progress;
    }
}