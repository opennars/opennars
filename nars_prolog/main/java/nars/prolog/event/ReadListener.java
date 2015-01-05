package nars.prolog.event;

import java.util.EventListener;

public interface ReadListener extends EventListener{

	public void readCalled(ReadEvent event);
}
