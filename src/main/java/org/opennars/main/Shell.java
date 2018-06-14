/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.main;

import org.opennars.io.events.TextOutputHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Run Reasoner
 * <p>
 Runs a Nar with addInput. useful for command line or batch functionality;
 TODO check duplicated code with {@link org.opennars.main.NARS}
 * <p>
 * Manage the internal working thread. Communicate with Reasoner only.
 */
public class Shell {

    private final Nar nar;

    private boolean logging;
    private PrintStream out = System.out;
    private final boolean dumpLastState = true;
    final int maxTime = 0;

    /**
     * The entry point of the standalone application.
     * <p>
     * @param args optional argument used : one addInput file
     */
    public static void main(final String[] args) throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
                
        final Shell nars = new Shell(new Nar());
        nars.nar.addInput("*volume=0");
        nars.run(args);
        
        // TODO only if single finish ( no reset in between )
        if (nars.dumpLastState) {
            System.out.println("\n==== Dump Last State ====\n"
                    + nars.nar.toString());
        }
    }

    public Shell(final Nar n) {
        this.nar = n;
    }

    private class InputThread extends Thread
    {
      private final BufferedReader bufIn;
      final Nar nar;
      InputThread(final InputStream in, final Nar nar)
      {
        this.bufIn = new BufferedReader(new InputStreamReader(in));
        this.nar=nar;
      }
      public void run()
      {
        while(true)
        {
            try {
                final String line=bufIn.readLine();
                if(line!=null) {
                    try {
                        nar.addInput(line);
                    } catch(Exception ex) {
                        if(MiscFlags.DEBUG) {
                            throw new IllegalStateException("error parsing:" +line, ex);
                        }
                        System.out.println("parsing error");
                    }
                }
                
            } catch (final IOException e) {
                throw new IllegalStateException("Could not read line.", e);
            }

            try {
                Thread.sleep(1);
            } catch (final InterruptedException e) {
                throw new IllegalStateException("Unexpectadly interrupted while sleeping.", e);
            }
        }
      }
    }
    
    /**
     * non-static equivalent to {@link #main(String[])} : finish to completion from
 an addInput file
     */
    public void run(final String[] args) {
        final TextOutputHandler output = new TextOutputHandler(nar, new PrintWriter(out, true));
        output.setErrors(true);
        output.setErrorStackTrace(true);
        final InputThread it;
        final int sleep = -1;
        final boolean noFile = false;
        
        if (args.length > 0) {
            nar.addInputFile(args[0]);
        }
        if(args.length == 0 || noFile) {   
            it=new InputThread(System.in,nar);
            it.start();
            //nar.addInput(new TextInput(new BufferedReader(new InputStreamReader(System.in))));
        }
               while (true) {
            if (logging)
                log("NARSBatch.run():"
                        + " step " + nar.time());
            
            nar.cycles(1);
            try {
                if(sleep > -1) {
                    Thread.sleep(sleep);
                }
            } catch (final InterruptedException ex) {
                Logger.getLogger(Shell.class.getName()).log(Level.SEVERE, null, ex);
            }
            //System.out.println("step");
            //System.out.println("step");
            
            
            if (logging)
                log("NARSBatch.run(): after tick"
                        + " step " + nar.time());
            
            if (maxTime > 0) {
                if (nar.time() == maxTime) {
                    break;
                }
            }
        }
               
        System.exit(0);
    }

    public void setPrintStream(final PrintStream out) {
        this.out = out;
    }

    private void log(final String mess) {
        if (logging) {
            System.out.println("/ " + mess);
        }
    }
}
