package nars.prolog.interfaces;

import nars.prolog.Prolog;

public class PrologFactory {
	
	/**
	 * Builds a prolog engine with default libraries loaded.
	 *
	 * The default libraries are BasicLibrary, ISOLibrary,
	 * IOLibrary, and  JavaLibrary
	 */
	public static IProlog createProlog() {
		return new Prolog();
	}

}
