package nars.tuprolog.event;

import nars.tuprolog.lib.UserContextInputStream;

import java.util.EventObject;

public class ReadEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private nars.tuprolog.lib.UserContextInputStream stream;
	
	public ReadEvent(UserContextInputStream str) {
		super(str);
		this.stream = str;
	}

	public UserContextInputStream getStream()
	{
		return this.stream;
	}

}
