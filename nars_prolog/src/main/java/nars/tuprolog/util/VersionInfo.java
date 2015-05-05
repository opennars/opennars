package nars.tuprolog.util;

public class VersionInfo 
{
	private static final String ENGINE_VERSION = "2.9++";
	private static final String JAVA_SPECIFIC_VERSION = "0";
	private static final String NET_SPECIFIC_VERSION = "0";
	
	public static String getEngineVersion()
	{
		return ENGINE_VERSION;
	}
	
	public static String getPlatform()
	{
		String vmName = System.getProperty("java.vm.name");
		if(vmName.contains("Java")) //"Java HotSpot(TM) Client VM"
			return "Java";
		else if(vmName.equals("IKVM.NET"))
			return ".NET";
		else 
			throw new RuntimeException();
	}
	
	public static String getSpecificVersion()
	{
		String vmName = System.getProperty("java.vm.name");
		if(vmName.contains("Java")) //"Java HotSpot(TM) Client VM"
			return JAVA_SPECIFIC_VERSION;
		else if(vmName.equals("IKVM.NET"))
			return NET_SPECIFIC_VERSION;
		else
			throw new RuntimeException();
	}
	
	public static String getCompleteVersion()
	{
		return getEngineVersion() + '.' + getSpecificVersion();
	}
}
