package TestURLClassLoaderClasses;
public class Pippo implements IPippo {
	
	public Pippo(){ 
		System.out.println("[TestURLClassLoader] Pippo constructed!"); 
	}
	
	public void met() 
	{
		System.out.println("[TestURLClassLoader] met"); 
	}
}
