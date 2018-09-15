/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.main;

import org.opennars.io.events.TextOutputHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import org.opennars.language.Term;

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
        Nar nar = null;
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
    
    public static void argInfo() {
        System.out.println("expected arguments: none, or: narOrConfigFileOrNull idOrNull nalFileOrNull cyclesToRunOrNull");
        System.out.println("or for UDP networking support:");
        //args length check, it has to be 5+5*k, with k in N0
        System.out.println("narOrConfigFileOrNull idOrNull nalFileOrNull cyclesToRunOrNull listenPort targetIP1 targetPort1 prioThres1 mustContainTermOrNull1 sendInput1 ... targetIPN targetPortN prioThresN mustContainTermOrNullN sendInputN");
        System.out.println("Here, OrNull means they can be null too, example: null null null null 64001 127.0.0.1 64002 0.5 null True");
    }
    
    /**
     * logging
     *
     */
    static void log(String message) {
        // l for log
        System.out.println("[l]: " + message);
    }

    /**
     * The entry point of the standalone application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, 
            ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException, InterruptedException {
        if(args.length == 0) { //in that case just run the instance
            args = new String[] { "null", "null", "null", "null"};
        }
        if(args.length != 4 && ((args.length-5) % 5 != 0 || args.length < 5)) { //args length check
            argInfo();
            System.exit(0);
        }
        
        log("creating Nar...");
        Nar nar = Shell.createNar(args);
        
        if(args.length > 4) {
            log("attaching NarNode networking features to Nar...");
            int nar1port = Integer.parseInt(args[4]);
            NarNode nar1 = new NarNode(nar, nar1port);
            for(int i=5; i<args.length; i+=5) {
                Term T = args[i+3].toLowerCase().equals("null") ? null : new Term(args[i+3]);
                nar1.addRedirectionTo(args[i], Integer.parseInt(args[i+1]), Float.parseFloat(args[i+2]), T, Boolean.parseBoolean(args[i+4]));
            }
        }
        
        log("attaching Shell to Nar...");
        new Shell(nar).run(args);
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
