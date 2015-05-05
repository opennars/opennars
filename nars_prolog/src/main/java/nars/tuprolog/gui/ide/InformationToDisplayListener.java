package nars.tuprolog.gui.ide;
import java.util.EventListener;

/**
 * Listener for information to display in the console events
 *
 * 
 */
public interface InformationToDisplayListener
    extends EventListener
{
    void onInformation(InformationToDisplayEvent e);
}