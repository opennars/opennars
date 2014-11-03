/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nars.prolog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import nars.prolog.event.ExceptionEvent;
import nars.prolog.event.ExceptionListener;
import nars.prolog.event.LibraryEvent;
import nars.prolog.event.LibraryListener;
import nars.prolog.event.OutputEvent;
import nars.prolog.event.OutputListener;
import nars.prolog.event.QueryEvent;
import nars.prolog.event.QueryListener;
import nars.prolog.event.SpyEvent;
import nars.prolog.event.SpyListener;
import nars.prolog.event.TheoryEvent;
import nars.prolog.event.TheoryListener;
import nars.prolog.event.WarningEvent;
import nars.prolog.event.WarningListener;
import nars.prolog.interfaces.IProlog;
//import alice.tuprologx.ide.ToolBar;



/**
 *
 * The Prolog class represents a tuProlog engine.
 *
 */
@SuppressWarnings("serial")
public class Prolog implements /*Castagna 06/2011*/IProlog,/**/ Serializable {


	/*  manager of current theory */
	private TheoryManager theoryManager;
	/*  component managing primitive  */
	private PrimitiveManager primitiveManager;    
	/* component managing operators */
	private OperatorManager opManager;    
	/* component managing flags */
	private FlagManager flagManager;
	/* component managing libraries */
	private LibraryManager libraryManager;
	/* component managing engine */
	private EngineManager engineManager;

	/*  spying activated ?  */
	private boolean spy;  
	/*  warning activated ?  */
	private boolean warning;
	/* listeners registrated for virtual machine output events */
	/*Castagna 06/2011*/	
	/* exception activated ? */
	private boolean exception;
	/**/
	private ArrayList<OutputListener> outputListeners;
	/* listeners registrated for virtual machine internal events */
	private ArrayList<SpyListener> spyListeners;
	/* listeners registrated for virtual machine state change events */
	private ArrayList<WarningListener> warningListeners;
	/*Castagna 06/2011*/	
	/* listeners registrated for virtual machine state exception events */
	private ArrayList<ExceptionListener> exceptionListeners;
	/**/

	/* listeners to theory events */
	private ArrayList<TheoryListener> theoryListeners;
	/* listeners to library events */
	private ArrayList<LibraryListener> libraryListeners;
	/* listeners to query events */
	private ArrayList<QueryListener> queryListeners;

    /* path history for including documents */
    private ArrayList<String> absolutePathList;
    private String lastPath;


	/**
	 * Builds a prolog engine with default libraries loaded.
	 *
	 * The default libraries are BasicLibrary, ISOLibrary,
	 * IOLibrary, and  JavaLibrary
	 */
	public Prolog() {
		this(false,true);
		try {
			loadLibrary("nars.prolog.lib.BasicLibrary");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			loadLibrary("nars.prolog.lib.ISOLibrary");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			loadLibrary("nars.prolog.lib.IOLibrary");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			if (System.getProperty("java.vm.name").equals("IKVM.NET"))
				loadLibrary("OOLibrary.OOLibrary, OOLibrary");
			else
				loadLibrary("nars.prolog.lib.JavaLibrary");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	/**
	 * Builds a tuProlog engine with loaded
	 * the specified libraries
	 *
	 * @param libs the (class) name of the libraries to be loaded
	 */
	public Prolog(String[] libs) throws InvalidLibraryException {
		this(false,true);
		if (libs != null) {
			for (int i = 0; i < libs.length; i++) {
				loadLibrary(libs[i]);
			}
		}
	}


	/**
	 * Initialize basic engine structures.
	 * 
	 * @param spy spying activated
	 * @param warning warning activated
	 */
	private Prolog(boolean spy, boolean warning) {
		outputListeners = new ArrayList<>();
		spyListeners = new ArrayList<>();
		warningListeners = new ArrayList<>();
		/*Castagna 06/2011*/		
		exceptionListeners = new ArrayList<>();
		/**/
		this.spy = spy;
		this.warning = warning;
		/*Castagna 06/2011*/
		exception = true;
		/**/
		theoryListeners = new ArrayList<>();
		queryListeners = new ArrayList<>();
		libraryListeners = new ArrayList<>();
        absolutePathList = new ArrayList<>();
		initializeManagers();
	}


	private void initializeManagers() {
		flagManager      = new FlagManager();
		libraryManager   = new LibraryManager();        
		opManager        = new OperatorManager();
		theoryManager    = new TheoryManager();
		primitiveManager = new PrimitiveManager();
		engineManager    = new EngineManager();
		//config managers
		theoryManager.initialize(this);
		libraryManager.initialize(this);
		flagManager.initialize(this);
		primitiveManager.initialize(this);
		engineManager.initialize(this);
	}


	/**
	 * Gets the component managing flags
	 */
	public FlagManager getFlagManager() {
		return flagManager;
	}

	/**
	 * Gets the component managing theory
	 */
	public TheoryManager getTheoryManager() {
		return theoryManager;
	}

	/**
	 * Gets the component managing primitives
	 */
	public PrimitiveManager getPrimitiveManager() {
		return primitiveManager;
	}

	/**
	 * Gets the component managing libraries
	 */
	public LibraryManager getLibraryManager() {
		return libraryManager;
	}

	/** Gets the component managing operators */
	public OperatorManager getOperatorManager() {
		return opManager; 
	}

	/**
	 * Gets the component managing engine
	 */
	public EngineManager getEngineManager() {
		return engineManager; 
	}


	/**
	 * Gets the current version of the tuProlog system
	 */
	public static String getVersion() {
		return nars.prolog.util.VersionInfo.getEngineVersion();
	}

    /**
     * Gets the last Element of the path list
     */
    public String getCurrentDirectory() {
        String directory = "";
        if(absolutePathList.isEmpty()) {
        	if(this.lastPath!=null)
        	{
        		directory = this.lastPath;
        	}
        	else
        	{
        		directory = System.getProperty("user.dir");
        	}
        } else {
            directory = absolutePathList.get(absolutePathList.size()-1);
        }

        return directory;
    }

    /**
     * Sets the last Element of the path list
     */
    public void setCurrentDirectory(String s) {
        this.lastPath=s;    
    }

    
    
    
	// theory management interface

	/**
	 * Sets a new theory
	 *
	 * @param th is the new theory
	 * @throws InvalidTheoryException if the new theory is not valid
	 * @see Theory
	 */
	public void setTheory(Theory th) throws InvalidTheoryException {	//no syn
		theoryManager.clear();
		addTheory(th);
	}


	/**
	 * Adds (appends) a theory
	 *
	 * @param th is the theory to be added
	 * @throws InvalidTheoryException if the new theory is not valid
	 * @see Theory
	 */
        @Override
	public SolveInfo addTheory(final PrologTermIterator th) throws InvalidTheoryException {	//no syn 
            return addTheory(th.iterator(this));
        }
        
	public SolveInfo addTheory(final Iterator<? extends Term> i) throws InvalidTheoryException {	//no syn
		Theory oldTh = getTheory();
		theoryManager.consult(i, true, null);
		SolveInfo theoryGoal = theoryManager.solveTheoryGoal();
		Theory newTh = getTheory();
		TheoryEvent ev = new TheoryEvent(this, oldTh, newTh);    
		this.notifyChangedTheory(ev);
                return theoryGoal;
	}
        
	public SolveInfo addTheory(final Struct s) throws InvalidTheoryException {	//no syn
		Theory oldTh = getTheory();
		theoryManager.consult(s, true, null);
		SolveInfo theoryGoal = theoryManager.solveTheoryGoal();
		Theory newTh = getTheory();
		TheoryEvent ev = new TheoryEvent(this, oldTh, newTh);    
		this.notifyChangedTheory(ev);
                return theoryGoal;
	}        

	/**
	 * Gets current theory
	 *
	 * @return current(dynamic) theory
	 */
	public Theory getTheory() {	//no syn
		try {
			return new Theory(theoryManager.getTheory(true));
		} catch (Exception ex){
			return null;
		}
	}

//
//	/**
//	 * Gets last consulted theory, with the original textual format
//	 *  
//	 * @return theory
//	 */
//	public Theory getLastConsultedTheory() {	//no syn
//		return theoryManager.getLastConsultedTheory();
//	}


	/**
	 * Clears current theory
	 */
	public void clearTheory() {	//no syn
		try {
			setTheory(new Theory());
		} catch (InvalidTheoryException e) {
			// this should never happen
		}
	}


	// libraries management interface

	/**
	 * Loads a library.
	 *
	 * If a library with the same name is already present,
	 * a warning event is notified and the request is ignored.
	 *
	 * @param className name of the Java class containing the library to be loaded
	 * @return the reference to the Library just loaded
	 * @throws InvalidLibraryException if name is not a valid library
	 */
	public Library loadLibrary(String className) throws InvalidLibraryException {	//no syn
		return libraryManager.loadLibrary(className);
	}
	
	/**
	 * Loads a library.
	 *
	 * If a library with the same name is already present,
	 * a warning event is notified and the request is ignored.
	 *
	 * @param className name of the Java class containing the library to be loaded
	 * @param paths The path where is contained the library.
	 * @return the reference to the Library just loaded
	 * @throws InvalidLibraryException if name is not a valid library
	 */
	public Library loadLibrary(String className, String[] paths) throws InvalidLibraryException {	//no syn
		return libraryManager.loadLibrary(className, paths);
	}


	/**
	 * Loads a specific instance of a library
	 *
	 * If a library with the same name is already present,
	 * a warning event is notified 
	 * 
	 * @param lib the (Java class) name of the library to be loaded
	 * @throws InvalidLibraryException if name is not a valid library
	 */
	public void loadLibrary(Library lib) throws InvalidLibraryException {	//no syn
		libraryManager.loadLibrary(lib);
	}


	/**
	 * Gets the list of current libraries loaded
	 *
	 * @return the list of the library names
	 */
	public String[] getCurrentLibraries() {		//no syn
		return libraryManager.getCurrentLibraries();
	}


	/**
	 * Unloads a previously loaded library
	 *
	 * @param name of the library to be unloaded
	 * @throws InvalidLibraryException if name is not a valid loaded library
	 */
	public void unloadLibrary(String name) throws InvalidLibraryException {		//no syn
		libraryManager.unloadLibrary(name);
	}


	/**
	 * Gets the reference to a loaded library
	 *
	 * @param name the name of the library already loaded
	 * @return the reference to the library loaded, null if the library is
	 *         not found
	 */
	public Library getLibrary(String name) {	//no syn
		return libraryManager.getLibrary(name);
	}


	protected Library getLibraryPredicate(String name, int nArgs) {		//no syn
		return primitiveManager.getLibraryPredicate(name,nArgs);
	}


	protected Library getLibraryFunctor(String name, int nArgs) {		//no syn
		return primitiveManager.getLibraryFunctor(name,nArgs);
	}



	// operators management

	/**
	 *  Gets the list of the operators currently defined
	 *
	 *  @return the list of the operators
	 */
	public java.util.List<Operator> getCurrentOperatorList() {	//no syn
		return opManager.getOperators();
	}


	// solve interface

	/**
	 *  Solves a query
	 *
	 * @param g the term representing the goal to be demonstrated
	 * @return the result of the demonstration
	 * @see SolveInfo
	 **/
	public SolveInfo solve(Term g, double maxTimeSeconds) {
		//System.out.println("ENGINE SOLVE #0: "+g);
		if (g == null) return null; 
		
		SolveInfo sinfo = engineManager.solve(g, maxTimeSeconds);
		
		QueryEvent ev = new QueryEvent(this,sinfo);
		notifyNewQueryResultAvailable(ev);

		return sinfo;

	}
        public SolveInfo solve(Term g) {
            return solve(g, 0);
        }

	/**
	 * Solves a query
	 *
	 * @param st the string representing the goal to be demonstrated
	 * @return the result of the demonstration
	 * @see SolveInfo
	 **/
	public SolveInfo solve(String st) throws MalformedGoalException {
		try {
			Parser p = new Parser(opManager, st);
			Term t = p.nextTerm(true);
			return solve(t);
		} catch (InvalidTermException ex) {
			throw new MalformedGoalException();
		}
	}
	
	/**
	 * Gets next solution
	 *
	 * @return the result of the demonstration
	 * @throws NoMoreSolutionException if no more solutions are present
	 * @see SolveInfo
	 **/
	public SolveInfo solveNext(double maxTimeSec) throws NoMoreSolutionException {
		if (hasOpenAlternatives()) {
			SolveInfo sinfo = engineManager.solveNext(maxTimeSec);
			QueryEvent ev = new QueryEvent(this,sinfo);
			notifyNewQueryResultAvailable(ev);
			return sinfo;
		} else
			throw new NoMoreSolutionException();
	}
        public SolveInfo solveNext() throws NoMoreSolutionException {
            return solveNext(0);
        }

	/**
	 * Halts current solve computation
	 */
	public void solveHalt() {
		engineManager.solveHalt();
	}

	/**
	 * Accepts current solution
	 */
	public void solveEnd() {	//no syn
		engineManager.solveEnd();
	}


	/**
	 * Asks for the presence of open alternatives to be explored
	 * in current demostration process.
	 *
	 * @return true if open alternatives are present
	 */
	public boolean hasOpenAlternatives() {		//no syn
		boolean b =  engineManager.hasOpenAlternatives();
		return b;
	}

	/**
	 * Checks if the demonstration process was stopped by an halt command.
	 * 
	 * @return true if the demonstration was stopped
	 */
	public boolean isHalted() {		//no syn
		return engineManager.isHalted();
	}

	/**
	 * Unifies two terms using current demonstration context.
	 *
	 * @param t0 first term to be unified
	 * @param t1 second term to be unified
	 * @return true if the unification was successful
	 */
	public boolean match(Term t0, Term t1) {	//no syn
		return t0.match(t1);
	}

	/**
	 * Unifies two terms using current demonstration context.
	 *
	 * @param t0 first term to be unified
	 * @param t1 second term to be unified
	 * @return true if the unification was successful
	 */
	public boolean unify(Term t0, Term t1) {	//no syn
		return t0.unify(this,t1);
	}

	/**
	 * Identify functors
	 * 
	 * @param term term to identify
	 */
	public void identifyFunctor(Term term) {	//no syn
		primitiveManager.identifyFunctor(term);
	}


	/**
	 * Gets a term from a string, using the operators currently
	 * defined by the engine
	 *
	 * @param st the string representing a term
	 * @return the term parsed from the string
	 * @throws InvalidTermException if the string does not represent a valid term
	 */
	public Term toTerm(String st) throws InvalidTermException {	//no syn
		return Parser.parseSingleTerm(st, opManager);
	}

	/**
	 * Gets the string representation of a term, using operators
	 * currently defined by engine
	 *
	 * @param term      the term to be represented as a string
	 * @return the string representing the term
	 */
	public String toString(Term term) {		//no syn
		return (term.toStringAsArgY(opManager, OperatorManager.OP_HIGH));
	}


	/**
	 * Defines a new flag
	 */
	public boolean defineFlag(String name, Struct valueList, Term defValue, boolean modifiable, String libName) {
		return flagManager.defineFlag(name,valueList,defValue,modifiable,libName);
	}


	// spy interface ----------------------------------------------------------

	/**
	 * Switches on/off the notification of spy information events
	 * @param state  - true for enabling the notification of spy event
	 */
	public synchronized void setSpy(boolean state) {
		spy = state;
	}

	/**
	 * Checks the spy state of the engine
	 * @return  true if the engine emits spy information
	 */
	public synchronized boolean isSpy() {
		return spy;
	}


	/**
	 * Notifies a spy information event
	 */
	protected synchronized void spy(final String s) {
		if (spy) {
			notifySpy(new SpyEvent(this, s));
		}
	}

	/**
	 * Notifies a spy information event
	 * @param s TODO
	 */
	protected synchronized void spy(String s, Engine e) {
		//System.out.println("spy: "+i+"  "+s+"  "+g);
		if (spy) {
			ExecutionContext ctx = e.currentContext;
			int i=0;
			String g = "-";
			if (ctx.fatherCtx != null){
				i = ctx.depth-1;
				g = ctx.fatherCtx.currentGoal.toString();
			}
			notifySpy(new SpyEvent(this, e, "spy: " + i + "  " + s + "  " + g));
		}
	}


	/**
	 * Switches on/off the notification of warning information events
	 * @param state  - true for enabling warning information notification
	 */
	public synchronized void setWarning(boolean state) {
		warning = state;
	}

	/**
	 * Checks if warning information are notified
	 * @return  true if the engine emits warning information
	 */
	public synchronized boolean isWarning() {
		return warning;
	}

	/**
	 * Notifies a warn information event
	 *
	 *
	 * @param m the warning message
	 */
	public void warn(String m) {
		if (warning){
			notifyWarning(new WarningEvent(this, m));
			//log.warn(m);
		}
	}

	/*Castagna 06/2011*/
	/**
	 * Notifies a exception information event
	 *
	 *
	 * @param m the exception message
	 */
	public void exception(String m) {
		if (exception){
			notifyException(new ExceptionEvent(this, m));
		}
	}
	/**/

	/*Castagna 06/2011*/
	/**
	 * Checks if exception information are notified
	 * @return  true if the engine emits exception information
	 */
	public synchronized boolean isException() {
		return exception;
	}
	/**/

	/*Castagna 06/2011*/
	/**
	 * Switches on/off the notification of exception information events
	 * @param state  - true for enabling exception information notification
	 */
	public synchronized void setException(boolean state) {
		exception = state;
	}
	/**/

	/**
	 * Produces an output information event
	 *
	 * @param m the output string
	 */
	public synchronized void stdOutput(String m) {
		notifyOutput(new OutputEvent(this, m));
	}

	// event listeners management

	/**
	 * Adds a listener to ouput events
	 *
	 * @param l the listener
	 */
	public synchronized void addOutputListener(OutputListener l) {
		outputListeners.add(l);
	}


	/**
	 * Adds a listener to theory events
	 *
	 * @param l the listener
	 */
	public synchronized void addTheoryListener(TheoryListener l) {
		theoryListeners.add(l);
	}

	/**
	 * Adds a listener to library events
	 *
	 * @param l the listener
	 */
	public synchronized void addLibraryListener(LibraryListener l) {
		libraryListeners.add(l);
	}

	/**
	 * Adds a listener to theory events
	 *
	 * @param l the listener
	 */
	public synchronized void addQueryListener(QueryListener l) {
		queryListeners.add(l);
	}

	/**
	 * Adds a listener to spy events
	 *
	 * @param l the listener
	 */
	public synchronized void addSpyListener(SpyListener l) {
		spyListeners.add(l);
	}

	/**
	 * Adds a listener to warning events
	 *
	 * @param l the listener
	 */
	public synchronized void addWarningListener(WarningListener l) {
		warningListeners.add(l);
	}

	/*Castagna 06/2011*/	
	/**
	 * Adds a listener to exception events
	 *
	 * @param l the listener
	 */
	public synchronized void addExceptionListener(ExceptionListener l) {
		exceptionListeners.add(l);
	}
	/**/

	/**
	 * Removes a listener to ouput events
	 *
	 * @param l the listener
	 */
	public synchronized void removeOutputListener(OutputListener l) {
		outputListeners.remove(l);
	}

	/**
	 * Removes all output event listeners
	 */
	public synchronized void removeAllOutputListeners() {
		outputListeners.clear();
	}

	/**
	 * Removes a listener to theory events
	 *
	 * @param l the listener
	 */
	public synchronized void removeTheoryListener(TheoryListener l) {
		theoryListeners.remove(l);
	}

	/**
	 * Removes a listener to library events
	 *
	 * @param l the listener
	 */
	public synchronized void removeLibraryListener(LibraryListener l) {
		libraryListeners.remove(l);
	}

	/**
	 * Removes a listener to query events
	 *
	 * @param l the listener
	 */
	public synchronized void removeQueryListener(QueryListener l) {
		queryListeners.remove(l);
	}


	/**
	 * Removes a listener to spy events
	 *
	 * @param l the listener
	 */
	public synchronized void removeSpyListener(SpyListener l) {
		spyListeners.remove(l);
	}

	/**
	 * Removes all spy event listeners
	 */
	public synchronized void removeAllSpyListeners() {
		spyListeners.clear();
	}

	/**
	 * Removes a listener to warning events
	 *
	 * @param l the listener
	 */
	public synchronized void removeWarningListener(WarningListener l) {
		warningListeners.remove(l);
	}

	/**
	 * Removes all warning event listeners
	 */
	public synchronized void removeAllWarningListeners() {
		warningListeners.clear();
	}

	/* Castagna 06/2011*/	
	/**
	 * Removes a listener to exception events
	 *
	 * @param l the listener
	 */
	public synchronized void removeExceptionListener(ExceptionListener l) {
		exceptionListeners.remove(l);
	}
	/**/	

	/*Castagna 06/2011*/	
	/**
	 * Removes all exception event listeners
	 */
	public synchronized void removeAllExceptionListeners() {
		exceptionListeners.clear();
	}
	/**/

	/**
	 * Gets a copy of current listener list to output events
	 */
	public synchronized List<OutputListener> getOutputListenerList() {
		return new ArrayList<>(outputListeners);
	}

	/**
	 * Gets a copy of current listener list to warning events
	 *
	 */
	public synchronized List<WarningListener> getWarningListenerList() {
		return new ArrayList<>(warningListeners);
	}

	/*Castagna 06/2011*/	
	/**
	 * Gets a copy of current listener list to exception events
	 *
	 */
	public synchronized List<ExceptionListener> getExceptionListenerList() {
		return new ArrayList<>(exceptionListeners);
	}
	/**/
	
	/**
	 * Gets a copy of current listener list to spy events
	 *
	 */
	public synchronized List<SpyListener> getSpyListenerList() {
		return new ArrayList<>(spyListeners);
	}

	/**
	 * Gets a copy of current listener list to theory events
	 * 
	 */
	public synchronized List<TheoryListener> getTheoryListenerList() {
		return new ArrayList<>(theoryListeners);
	}

	/**
	 * Gets a copy of current listener list to library events
	 *
	 */
	public synchronized List<LibraryListener> getLibraryListenerList() {
		return new ArrayList<>(libraryListeners);
	}

	/**
	 * Gets a copy of current listener list to query events
	 *
	 */
	public synchronized List<QueryListener> getQueryListenerList() {
		return new ArrayList<>(queryListeners);
	}

	// notification

	/**
	 * Notifies an ouput information event
	 *
	 * @param e the event
	 */
	protected void notifyOutput(OutputEvent e) {
		for(OutputListener ol:outputListeners){
			ol.onOutput(e);
		}
	}

	/**
	 * Notifies a spy information event
	 *
	 * @param e the event
	 */
	protected void notifySpy(SpyEvent e) {
		for(SpyListener sl:spyListeners){
			sl.onSpy(e);
		}
	}

	/**
	 * Notifies a warning information event
	 *
	 * @param e the event
	 */
	protected void notifyWarning(WarningEvent e) {
		for(WarningListener wl:warningListeners){
			wl.onWarning(e);
		}
	}

	/*Castagna 06/2011*/	
	/**
	 * Notifies a exception information event
	 *
	 * @param e the event
	 */
	protected void notifyException(ExceptionEvent e) {
		for(ExceptionListener el:exceptionListeners){
			el.onException(e);
		}
	}
	/**/
	
	//

	/**
	 * Notifies a new theory set or updated event
	 * 
	 * @param e the event
	 */
	protected void notifyChangedTheory(TheoryEvent e) {
		for (TheoryListener tl : theoryListeners) {
			tl.theoryChanged(e);
		}
	}

	/**
	 * Notifies a library loaded event
	 * 
	 * @param e the event
	 */
	protected void notifyLoadedLibrary(LibraryEvent e) {
		for(LibraryListener ll:libraryListeners){
			ll.libraryLoaded(e);
		}
	}

	/**
	 * Notifies a library unloaded event
	 * 
	 * @param e the event
	 */
	protected void notifyUnloadedLibrary(LibraryEvent e) {
		for(LibraryListener ll:libraryListeners){
			ll.libraryUnloaded(e);
		}
	}

	/**
	 * Notifies a library loaded event
	 * 
	 * @param e the event
	 */
	protected void notifyNewQueryResultAvailable(QueryEvent e) {
		for(QueryListener ql:queryListeners){
			ql.newQueryResultAvailable(e);
		}
	}


    /**
     * Append a new path to directory list
     *
     */
    public void pushDirectoryToList(String path) {
        absolutePathList.add(path);
    }

    /**
     *
     * Retract an element from directory list
     */
    public void popDirectoryFromList() {
        if(!absolutePathList.isEmpty()) {
            absolutePathList.remove(absolutePathList.size()-1);
        }
    }

     /**
       *
       * Reset directory list
      */
    public void resetDirectoryList(String path) {
        absolutePathList = new ArrayList<>();
        absolutePathList.add(path);
    }
    
    public Term termSolve(String st){
		try{
			Parser p = new Parser(opManager, st);
			Term t = p.nextTerm(true);
			return t;
		}catch(InvalidTermException e)
		{
			String s = "null";
			Term t = Term.createTerm(s);
			return t;
		}
	}

}