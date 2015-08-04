/** Ben F Rayfield offers this software opensource GNU GPL 2+ */
package smartblob.commonfuncs;
//import jselfmodify.JSelfModify;

/** Use the following line at the top of each source code file:
import static commonfuncs.CommonFuncs.*;
<br><br>
This is more like functional programming than object oriented.
*/
public class CommonFuncs{
	private CommonFuncs(){}
	
	public static void log(String line){
		if(line.trim().equals("")){
			System.out.println("empty log line");
		}
		//JSelfModify.log(line);
		System.out.println(line);
	}
	
	public static void logToUser(String line){
		//JSelfModify.logToUser(line);
		log(line);
	}
	
	/** Since JSelfModify doesnt have a User system yet, just room for expansion,
	this function uses JSelfModify.rootUser as is normally done.
	WARNING: This kind of thing will need to be redesigned securely when we start having usernames.
	*
	public static Object jsmGet(String path){
		try{
			return JSelfModify.root.get(JSelfModify.rootUser, path);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}*/
	
	/** See WARNING in jsmGet *
	public static void jsmPut(String path, Object value){
		try{
			JSelfModify.root.put(JSelfModify.rootUser, path, value);
		}catch(Exception e){
			throw new RuntimeException(e);
		}	
	}*/
	
	/** See WARNING in jsmGet *
	public static boolean jsmExist(String path){
		try{
			return JSelfModify.root.exist(JSelfModify.rootUser, path);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}*/


}