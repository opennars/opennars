/*Castagna 06/2011*/
package nars.tuprolog.event;

import java.util.EventObject;

@SuppressWarnings("serial")
public class ExceptionEvent extends EventObject{

	private final String msg;

	public ExceptionEvent(Object source, String msg_) {
		super(source);
		msg=msg_;
	}

	public String getMsg(){
		return msg;
	}

}
/**/