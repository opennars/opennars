/*
  Part of the G4P library for Processing 
  	http://www.lagers.org.uk/g4p/index.html
	http://sourceforge.net/projects/g4p/files/?source=navbar

  Copyright (c) 2009 Peter Lager

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
 */

package automenta.vivisect.gui;

/**
 * CLASS FOR INTERNAL USE ONLY
 * 
 * @author Peter Lager
 *
 */
class GMessenger implements GConstants, GConstantsInternal {

	/**
	 * Display an error message message
	 * 
	 * @param id message ID number
	 * @param obj 
	 * @param info
	 */
	static void message(Integer id, Object[] info){
		// Display G4P messages if required
		if(G4P.showMessages){
			switch(id){
			case MISSING:
				missingEventHandler(info);
				break;
			case USER_COL_SCHEME:
				System.out.println("USER DEFINED colour schema active");
				break;
			}
		}
		// Display all runtime errors
		switch(id){
		case NONEXISTANT:
			nonexistantEventHandler(info);
			break;
		case INVALID_TYPE:
			inavlidControlType(info);
			break;
		case INVALID_PAPPLET:
			unmatchedPApplet(info);
			break;
		case EXCP_IN_HANDLER:
			eventHandlerFailed(info);
			break;
		}
	}

	/**
	 * Error message when an exception is create inside an event handler.
	 * 
	 * info[0] event generator class
	 * info[1] event handling method name
	 * info[2] the exception thrown
	 * 
	 */
	private static void eventHandlerFailed(Object[] info) {
		String className = info[0].getClass().getSimpleName();
		String methodName = (String) info[1];
		Exception e = (Exception) info[2];
		Throwable t = e.getCause();
		StackTraceElement[] calls = t.getStackTrace();
		StringBuilder output = new StringBuilder();
		output.append("################  EXCEPTION IN EVENT HANDLER  ################");
		output.append("\nAn error occured during execution of the eventhandler:");
		output.append("\nCLASS: "+className+"   METHOD: "+methodName);
		output.append("\n\tCaused by " + t.toString());
		for(Object line : calls)
			output.append("\n\t"+ line.toString());			
		output.append("\n##############################################################\n");
		System.out.println(output);
	}

	/**
	 * Unable to find the default handler method.
	 * 
	 * info[0] event generator class
	 * info[1] event handling method name
	 * info[2] the parameter class names
	 * info[3] the parameter names (identifiers)
	 * 
	 */
	private static void missingEventHandler(Object[] info) {
		String className = info[0].getClass().getSimpleName();
		String methodName = (String) info[1];
		StringBuilder output = new StringBuilder();

		output.append("You might want to add a method to handle " + className + " events syntax is\n");
		output.append("public void " + methodName + "(");
		Class<?>[] param_classes = (Class[])(info[2]);
		String[] param_names = (String[])(info[3]);
		if(param_classes != null) {
			for(int i = 0; i < param_classes.length; i++){
				String pname = (param_classes[i]).getSimpleName();
				output.append(pname + " " + param_names[i]);
				if(i < param_classes.length - 1)
					output.append(", ");
			}
		}

		output.append(") { /* code */ }\n");
		System.out.println(output.toString());
	}

	/**
	 * Unable to find the user defined handler method.
	 * 
	 * info[0] event generator class
	 * info[1] event handling method name
	 * info[2] the parameter class names
	 * 
	 */
	private static void nonexistantEventHandler(Object[] info) {
		String className = info[0].getClass().getSimpleName();
		String methodName = (String) info[1];
		String pname;
		StringBuilder output = new StringBuilder();

		output.append("The "+className+" class cannot find this method \n");
		output.append("\tpublic void " + methodName + "(");
		Class<?>[] param_names = (Class[])(info[2]);
		for(int i = 0; i < param_names.length; i++){
			pname = (param_names[i]).getSimpleName();
			output.append(pname + " " + pname.substring(1).toLowerCase());
			if(i < param_names.length - 1)
				output.append(", ");
		}
		output.append(") { /* code */ }\n");
		System.out.println(output.toString());
	}

	/**
	 * An attempt was made to add a G4P control that does not have a visual appearance or one that 
	 * does not respond to mouse / keyboard events. 
	 * 
	 * info[0] the group class
	 * info[1] the G4P control class
	 */
	private static void inavlidControlType(Object[] info){
		String groupClassName = info[0].getClass().getSimpleName();
		String className = info[1].getClass().getSimpleName();

		System.out.println("Controls of type " + className+" cannot be added to a control group (" + groupClassName  + "\n");
	}

	/**
	 * An attempt was made to add a G4P control that does not have a visual appearance or one that 
	 * does not respond to mouse / keyboard events. 
	 * 
	 * info[0] the group class
	 * info[1] the G4P control class
	 */
	private static void unmatchedPApplet(Object[] info){
		String groupClassName = info[0].getClass().getSimpleName();
		String className = info[1].getClass().getSimpleName();
		System.out.println("The " + className + " object cannot be added to this control group (" + groupClassName  + ") because they are for different windows.\n");
	}
}
