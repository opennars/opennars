package com.googlecode.opennars.tools.actuary;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import com.googlecode.opennars.entity.Sentence;
import com.googlecode.opennars.entity.Task;
import com.googlecode.opennars.main.Reasoner;
import com.googlecode.opennars.parser.InvalidInputException;
import com.googlecode.opennars.parser.Parser;
import com.googlecode.opennars.parser.loan.LoanParser;
import com.googlecode.opennars.parser.loan.Loan.PrettyPrinter;
import com.googlecode.opennars.parser.loan.Loan.Absyn.BaseR;
import com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix1;
import com.googlecode.opennars.parser.loan.Loan.Absyn.SentPrefix;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.UnflaggedOption;


public class Actuary implements Observer {

	private SimpleJSAP jsap;
	private Parser parser;
	private Reasoner reasoner;
	private int verbosity;
	
	public Actuary() {
		try {
			jsap = new SimpleJSAP(
						"actuary",
					    "Reads in LOAN ontologies and performs inferences upon them.",
					    new Parameter[] {
								//new Switch("displayhelp", 'h', "help", "Display this help message."),
						    new FlaggedOption("verbosity", JSAP.INTEGER_PARSER, "0", true, 'v',"verbosity", "The higher the verbosity, the more the system will output"),
								new UnflaggedOption("files", JSAP.STRING_PARSER, null, true, true, "The files to load.")
						}
					);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void update(final Observable o, final Object arg) {
		Sentence sent = (Sentence) arg;
		System.out.println(parser.serialiseSentence(sent, reasoner.getMemory()));
	}
	
	public void run(final String[] args) {
		
		if(jsap == null)
			return;
		
		JSAPResult config = jsap.parse(args);
		if(!config.success()) {
			System.err.println();
			System.err.println("Usage: java " + Actuary.class.getName());
			
			System.err.println("                " + jsap.getUsage());
			System.err.println();
			System.err.println(jsap.getHelp());
			System.exit(1);
		}
		
		// Configure the parameters
		verbosity = config.getInt("verbosity");
		
		// Create ourselves a parser and reasoner
		parser = new LoanParser();
		reasoner = new Reasoner(parser);
		
		reasoner.addObserver(this);
		reasoner.getParameters().SILENT_LEVEL = (verbosity >= 0 && verbosity <= 100 ? 100 - verbosity : 100);
		
		// Get the list of files to load
		String[] files = config.getStringArray("files");
		for(int i = 0; i < files.length; i++) {
			String content = "";
			// Try treating them as URLs first
			try {
				URL ctx = new File(".").toURL();
				URL u = new URL(ctx, files[i]);
				((LoanParser) parser).setBaseURI(u.toURI());
				BufferedReader input = new BufferedReader(new InputStreamReader(u.openStream()));
				StringBuilder builder = new StringBuilder();
				String line;
				while((line = input.readLine()) != null)
					builder.append(line);
				content = builder.toString();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				Iterator<Task> tasks = parser.parseTasks(content, reasoner.getMemory()).iterator();
				while(tasks.hasNext()) {
					reasoner.tellTask(tasks.next());
				}
			} catch (InvalidInputException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		// Write the header of the file
		BaseR br = new BaseR("<" + ((LoanParser) parser).getBaseURI().toString() + ">");
		System.out.println(PrettyPrinter.print(br));
		Map<String, URI> ns = ((LoanParser) parser).getNamespaces();
		Iterator<String> iter = ns.keySet().iterator();
		while(iter.hasNext()) {
			String pre = iter.next();
			SentPrefix n = new SentPrefix(new NSPrefix1(pre), "<" + ns.get(pre).toString() + ">");
			System.out.println(PrettyPrinter.print(n));
		}
		
		System.out.println();
		
		// Start reasoning
		reasoner.start();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Actuary c = new Actuary();
		c.run(args);
	}

}
