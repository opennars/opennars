package nars.prolog;

import nars.prolog.event.LibraryEvent;
import nars.prolog.event.PrologEventAdapter;
import nars.prolog.event.QueryEvent;
import nars.prolog.event.TheoryEvent;

public class TestPrologEventAdapter extends PrologEventAdapter {
	String firstMessage = "";
	String secondMessage = "";
    
    public void theoryChanged(TheoryEvent ev) {
    	firstMessage = ev.getOldTheory().toString();
    	secondMessage = ev.getNewTheory().toString();
    }
    
    public void newQueryResultAvailable(QueryEvent ev) {
    	firstMessage = ev.getSolveInfo().getQuery().toString();
    	secondMessage = ev.getSolveInfo().toString();
    }
    
    public void libraryLoaded(LibraryEvent ev) {
    	firstMessage = ev.getLibraryName();
    }

    public void libraryUnloaded(LibraryEvent ev) {
    	firstMessage = ev.getLibraryName();
    }
}
