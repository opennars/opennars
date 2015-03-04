/*
 * RenderUpdateEventRunner.java                     STATUS: Abgeschlossen
 * ----------------------------------------------------------------------
 * 
 */

package raytracer.cameras;

/**
 * Dieser Ereignis-Ausf�hrer f�hrt das <code>renderUpdate</code>-Ereignis aus.
 * 
 * @author Mathias Kosch
 *
 */
class RenderUpdateEventRunner implements Runnable
{
    protected final RendererEvent event;
    protected final RendererListener rl;
    
    /**
     * Erstellt ein neues <code>RenderUpdateEventRunner</code>-Objekt, das die
     * Ereignis-Behandlung aufnimmt.
     * 
     * @param event Zu sendendes Ereignis.
     * @param rl Informationen, die die Ereignisbehandlung betreffen.
     */
    public RenderUpdateEventRunner(RendererEvent event, RendererListener rl)
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
        rl.renderUpdate(event);
    }
}