package nars.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 * Connects to a CoreNLP server for NLP parsing, which returns raw parse data.
 * This then gets processed by a Python script via Jython to generate Narsese statements.
 * The result can then be used as input to a NAR
 */
public class ParseCoreNLP {
    
    final static PythonInterpreter python = new PythonInterpreter();
    static {
        python.execfile(ParseCoreNLP.class.getResourceAsStream("corenlp/stanford_to_narsese.py"));
        
    }
    
    private final String host;
    private final int port;
  
    public ParseCoreNLP(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    public String parse(String input) throws IOException {        
        Socket s = new Socket(host, port);
        s.setSoLinger(true, 1);
        
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        
        PrintWriter out = new PrintWriter(s.getOutputStream(), false);        
        out.write(input + "\n");
        out.flush();        
                        
        String x = null;
        StringBuffer response = new StringBuffer();
        while ((x = in.readLine()) != null) {            
            response.append( x + "|");
        }
        
        System.out.println("response: " + response.toString());
        PyObject result = python.eval("parse(\"" + response.toString() + "\")");
        System.out.println(result);
        
        return "";
    }
    
    public static void main(String[] args) throws Exception {
        ParseCoreNLP p = new ParseCoreNLP("91.203.212.130", 9100);
        System.out.println(p.parse("this is a sentence."));
        System.out.println(p.parse("this is a question?"));
    }
}
