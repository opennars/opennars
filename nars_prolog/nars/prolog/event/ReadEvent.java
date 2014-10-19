package nars.prolog.event;

import java.util.EventObject;
import nars.prolog.lib.UserContextInputStream;

public class ReadEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private nars.prolog.lib.UserContextInputStream stream;
	
	public ReadEvent(UserContextInputStream str) {
		super(str);
		this.stream = str;
	}

	public UserContextInputStream getStream()
	{
		return this.stream;
	}

}
