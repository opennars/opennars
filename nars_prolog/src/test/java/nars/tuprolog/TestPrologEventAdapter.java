package nars.tuprolog;

import nars.tuprolog.event.LibraryEvent;
import nars.tuprolog.event.PrologEventAdapter;
import nars.tuprolog.event.QueryEvent;
import nars.tuprolog.event.TheoryEvent;

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
