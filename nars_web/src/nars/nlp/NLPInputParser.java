package nars.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import nars.io.ExperienceReader;
import nars.io.InputParser;
import nars.io.Symbols;
import nars.main_nogui.NAR;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;


public class NLPInputParser implements InputParser {
    
    final static PythonInterpreter python = new PythonInterpreter();
    static {
        python.execfile(NLPInputParser.class.getResourceAsStream("corenlp/stanford_to_narsese.py"));        
    }
    
    private final String host;
    private final int port;

    public NLPInputParser(String host, int port) {
        this.host = host;
        this.port = port;
    }

    
    @Override
    public boolean parse(NAR nar, String input, InputParser lastHandler) {
        try {
            boolean explicit = true;
            char c = input.charAt(0);
            if (c == Symbols.NATURAL_LANGUAGE_MARK) {
                explicit = true;
                input = input.substring(1);
            }
            if (!(explicit || (lastHandler==null)))
                return false;
            
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

            boolean isQuestion = input.endsWith("?");
            
            //System.out.println("CoreNLP response: " + response.toString());
            
            PyObject result = python.eval("parse(\"" + response.toString() + "\", " + (isQuestion ? "True" : "False") + ")");
            
            //System.out.println("stanford_to_narsese response: " + result);
            
            String r = result.toString().trim();
            
            if (r.length() > 0) {
                new ExperienceReader(nar, new BufferedReader( new StringReader(r)));            
                return true;
            }
        }
        catch (IOException e) {
            return false;
        }        
        return false;
    }
    
}
