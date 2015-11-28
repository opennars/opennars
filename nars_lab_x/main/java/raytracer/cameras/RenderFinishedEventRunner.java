/*
 * RenderFinishedEventRunner.java                   STATUS: Abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.cameras;

/**
 * Dieser Ereignis-Ausf�hrer f�hrt das <code>renderFinished</code>-Ereignis aus.
 * 
 * @author Mathias Kosch
 *
 */
class RenderFinishedEventRunner implements Runnable
{
    protected final RendererEvent event;
    protected final RendererListener rl;
    
    /**
     * Erstellt ein neues <code>RenderFinishedEventRunner</code>-Objekt, das die
     * Ereignis-Behandlung aufnimmt.
     * 
     * @param event Zu sendendes Ereignis.
     * @param rl Informationen, die die Ereignisbehandlung betreffen.
     */
    public RenderFinishedEventRunner(RendererEvent event, RendererListener rl)
    {
        this.event = event;
        this.rl = rl;
    }
    
    /**
     * F�hrt die Ereignisbehandlungsroutine aus.
     */
    @Override
    public void run()
    {
        rl.renderFinished(event);
    }
}