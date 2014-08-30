/*Castagna 06/2011 >*/
package nars.prolog.event;

import java.util.EventListener;

public interface ExceptionListener extends EventListener {
    public abstract void onException(ExceptionEvent e);
}
/**/