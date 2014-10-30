/*
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;
import javarepl.ExpressionReader;
import javarepl.Main;
import static javarepl.Main.printColors;
import static javarepl.Result.result;
import javarepl.ResultPrinter;
import javarepl.client.EvaluationResult;
import javarepl.client.JavaREPLClient;
import javarepl.console.ConsoleConfig;
import static javarepl.console.ConsoleConfig.consoleConfig;
import javarepl.console.SimpleConsole;
import javarepl.console.commands.EvaluateFromHistory;
import javarepl.console.commands.ListValues;
import javarepl.console.commands.SearchHistory;
import javarepl.console.commands.ShowHistory;
import javarepl.console.rest.RestConsole;
import javarepl.internal.totallylazy.Option;
import static javarepl.internal.totallylazy.Option.none;
import nars.core.build.DefaultNARBuilder.CommandLineNARBuilder;
import nars.io.TextInput;
import nars.io.TextOutput;



/**
 * Run Reasoner
 * <p>
 Runs a NAR with addInput. useful for command line or batch functionality; 
 TODO check duplicated code with {@link nars.main.NARS}
 * <p>
 * Manage the internal working thread. Communicate with Reasoner only.
 */
public class NARRun {

    private final NAR nar;

    private boolean logging;
    private PrintStream out = System.out;
    private final boolean dumpLastState = true;
    int maxTime = 0;



    public static void main(String... args) throws Exception {
        Main.process = none();
        Main.console = new ResultPrinter(printColors(args));

        ConsoleConfig config = consoleConfig()                
                //.historyFile(new File(getProperty("user.home"), ".javarepl-embedded.history"))
                .commands(
                        ListValues.class,
                        ShowHistory.class,
                        EvaluateFromHistory.class,
                        SearchHistory.class)                
                .results(
                        result("date", new Date()),
                        result("num", 42));

        int port = 8001;
        new RestConsole(new SimpleConsole(config), port);
        
        JavaREPLClient client = new JavaREPLClient("localhost", 8001);
                
        //JavaREPLClient client = clientFor(hostname(args), port(args));
        ExpressionReader expressionReader = Main.expressionReaderFor("nars", client);

        Option<String> expression = none();
        Option<EvaluationResult> result = none();
        while (expression.isEmpty() || !result.isEmpty()) {
            expression = expressionReader.readExpression();

            if (!expression.isEmpty()) {
                result = client.execute(expression.get());
                if (!result.isEmpty())
                    Main.console.printEvaluationResult(result.get());
            }
        }
    }

    public static void startReplWeb() throws Exception {
        ConsoleConfig config = consoleConfig()                
                //.historyFile(new File(getProperty("user.home"), ".javarepl-embedded.history"))
                .commands(
                        ListValues.class,
                        ShowHistory.class,
                        EvaluateFromHistory.class,
                        SearchHistory.class)                
                .results(
                        result("date", new Date()),
                        result("num", 42));

        int port = 8001;
        new RestConsole(new SimpleConsole(config), port);
        
        
    }
    
    /**
     * The entry point of the standalone application.
     * <p>
     * @param args optional argument used : one addInput file
     */
    public static void main1(String args[]) {
                
        NARRun nars = new NARRun(new CommandLineNARBuilder(args).build());        
        nars.runInference(args);
        
        // TODO only if single finish ( no reset in between )
        if (nars.dumpLastState) {
            System.out.println("\n==== Dump Last State ====\n"
                    + nars.nar.toString());
        }
    }

    public NARRun(NAR n) {
        this.nar = n;
    }

    /**
     * non-static equivalent to {@link #main(String[])} : finish to completion from
 an addInput file
     */
    public void runInference(String args[]) {
        TextOutput output = new TextOutput(nar, new PrintWriter(out, true));
        output.setErrors(true);
        output.setErrorStackTrace(true);
        
        if (args.length > 0) {
            try {
                nar.addInput(new TextInput(new File(args[0])));
            } catch (FileNotFoundException ex) {
                System.err.println("NARRun.init: " + ex);
            }
        }
        else {            
            nar.addInput(new TextInput(new BufferedReader(new InputStreamReader(System.in))));
        }
        
               while (true) {
            if (logging)
                log("NARSBatch.run():"
                        + " step " + nar.time()
                        + " " + nar.inputChannels.size());
            

            nar.step(1);
            
            
            if (logging)
                log("NARSBatch.run(): after tick"
                        + " step " + nar.time()
                        + " " + nar.inputChannels.size());
            
            if (maxTime > 0) {
                if ((nar.inputChannels.isEmpty()) || nar.time() == maxTime) {
                    break;
                }
            }
        }
               
        System.exit(0);
    }

    public void setPrintStream(PrintStream out) {
        this.out = out;
    }

    private void log(String mess) {
        if (logging) {
            System.out.println("/ " + mess);
        }
    }
    
    
    
    
    


}
