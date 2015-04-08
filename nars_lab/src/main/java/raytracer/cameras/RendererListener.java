/*
 * RendererListener.java                            STATUS: Abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.cameras;

import java.util.EventListener;

/**
 * Dieses Interface implementiert Methoden zur Ereignisbehandlung eines
 * Render-Vorgangs.
 * 
 * @author Mathias Kosch
 * @see raytracer.cameras.RendererEvent
 * 
 */
public interface RendererListener extends EventListener
{
    /**
     * Wird aufgerufen, sobald ein neues Pixel gerendert wurde.
     * 
     * @param event <code>RendererEvent</code>-Objekt zum aufgetretenen Ereignis.
     */
    void renderUpdate(RendererEvent event);
    
    /**
     * Wird aufgerufen, sobald das Rendern abgeschlossen ist.
     * 
     * @param event <code>RendererEvent</code>-Objekt zum aufgetretenen Ereignis.
     */
    void renderFinished(RendererEvent event);
}