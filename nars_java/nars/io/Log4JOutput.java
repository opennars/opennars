package nars.io;

import nars.core.NAR;
import nars.gui.InferenceLogger;
import nars.gui.InferenceLogger.LogOutput;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.net.SocketAppender;

/**
 * Appends to Log4J logging system, which can output to console, file, socket, etc..
 */
public class Log4JOutput implements Output, LogOutput {
    private boolean traceActive;

    public Log4JOutput(NAR nar, boolean trace) {
        SocketAppender sa = new SocketAppender("localhost", 4445);
        Logger.getRootLogger().addAppender(sa);
        
        ConsoleAppender console = new ConsoleAppender();
        //String PATTERN = "%d [%p|%c|%C{1}] %m%n";
        
        //http://logging.apache.org/log4j/2.x/manual/layouts.html#HTMLLayout
        String PATTERN = "%c{1} %m%n";
        
        console.setLayout(new PatternLayout(PATTERN)); 
        
        console.setThreshold(Level.ALL);
        console.activateOptions();        
  
        Logger.getRootLogger().addAppender(console);
        
        nar.addOutput(this);
        
        if (trace) {
            this.traceActive = true;
            
            nar.memory.setRecorder(new InferenceLogger(this));
        }
    }
    
    @Override public final void output(final Class channel, final Object signal) {
        Logger l;
        if (channel == IN.class)
            l = Logger.getLogger("nars.io.In");
        else if (channel == OUT.class)
            l = Logger.getLogger("nars.io.Out");        
        else if (channel == ERR.class)
            l = Logger.getLogger("nars.io.Err");
        else if (channel == ECHO.class)
            l = Logger.getLogger("nars.io.Echo");        
        else if (channel == EXE.class)
            l = Logger.getLogger("nars.io.Exe");
        else 
            l = Logger.getLogger(channel);
        
        l.info(signal);
    }
    
    @Override public void traceAppend(final String channel, final String s) {
        Logger.getLogger("nars.inference." + channel).info(s);
        
    }
    

    
}
