package TestURLClassLoaderClasses;
class Pluto {
	public static void method(IPippo var){ 
		System.out.println("[TestURLClassLoader] Pluto.method(IPippo) invoked.");
		var.met();
	}
	public static void method2(Pippo var){
		System.out.println("[TestURLClassLoader] Pluto.method2(Pippo) invoked.");
		var.met();
	}
}
