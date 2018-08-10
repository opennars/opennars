/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
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

/**
 * Run Reasoner inside a command line application for batch processing
 */
/* TODO check duplicated code with {@link org.opennars.main.Nar} */
// Manage the internal working thread. Communicate with Reasoner only.
public class Shell {

    private final Nar nar;
    private PrintStream out = System.out;
    
    public static Nar createNar(String[] args) throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, 
            ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        Nar nar = new Nar();
        Integer id = null;
        if(!args[1].toLowerCase().equals("null")) {
            id = Integer.parseInt(args[1]);
        }
        
        if(args[0].toLowerCase().equals("null")) {
            if(id == null) {
                nar = new Nar();
            } else {
                nar = new Nar(id);
            }
        } else if(args[0].endsWith(".xml")) {
            if(id == null) {
                nar = new Nar(args[0]);
            } else {
                nar = new Nar(id, args[0]);
            }
        }
        else {
            if(id != null) {
                System.out.println("Identity of loaded nar can not be changed, set idOrNull to null if Nar from file should be used!");
                System.exit(1);
            }
            nar = Nar.LoadFromFile(args[0]);
        }
        return nar;
    }

    /**
     * The entry point of the standalone application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, 
            ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException, InterruptedException {
        if(args.length > 4) {
            NarNode.main(args);
        }
        if(args.length == 0) { //in that case just run the instance
            args = new String[] { "null", "null", "null", "null"};
        }
        if(args.length != 4) { //args length check
            System.out.println("expected arguments: none, or: narOrConfigFileOrNull idOrNull nalFileOrNull cyclesToRunOrNull");
            System.out.println("Here, OrNull means they can be null too, example: null null 64001 127.0.0.1 64002 0.5 null True");
            System.exit(0);
        }
        Nar nar = Shell.createNar(args);
        final Shell nars = new Shell(nar);
        nars.run(args);
    }

    public Shell(final Nar n) {
        this.nar = n;
    }

    private class InputThread extends Thread {
        private final BufferedReader bufIn;
        final Nar nar;

        InputThread(final InputStream in, final Nar nar) {
            this.bufIn = new BufferedReader(new InputStreamReader(in));
            this.nar = nar;
        }

        public void run() {
            while (true) {
                try {
                    final String line = bufIn.readLine();
                    if (line != null) {
                        try {
                            nar.addInput(line);
                        } catch (Exception ex) {
                            if (MiscFlags.DEBUG) {
                                throw new IllegalStateException("error parsing:" + line, ex);
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
     * non-static equivalent to {@link #main(String[])} : finish to completion from an addInput file
     */
    public void run(final String[] args) {
        final TextOutputHandler output = new TextOutputHandler(nar, new PrintWriter(out, true));
        output.setErrors(true);
        output.setErrorStackTrace(true);
        final InputThread it;

        final boolean hasInputFile = !args[2].toLowerCase().equals("null");
        final boolean hasNumberOfSteps = !args[3].toLowerCase().equals("null");

        if (hasInputFile) {
            nar.addInputFile(args[2]);
        }
        it = new InputThread(System.in, nar);
        it.start();

        final int numberOfSteps = hasNumberOfSteps ? Integer.parseInt(args[3]) : -1;

        if (hasNumberOfSteps) {
            nar.cycles(numberOfSteps);
            System.exit(0);
        } else {
            nar.start();
        }
    }

    public void setPrintStream(final PrintStream out) {
        this.out = out;
    }
}
