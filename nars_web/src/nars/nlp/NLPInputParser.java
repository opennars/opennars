package nars.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import nars.io.TextInput;
import nars.io.TextInputParser;
import nars.io.Symbols;
import nars.core.NAR;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;


public class NLPInputParser implements TextInputParser {
    
    final static PythonInterpreter python = new PythonInterpreter();
    static {
        
//python.execfile(NLPInputParser.class.getResourceAsStream("corenlp/stanford_to_narsese.py"));        

        try {
            
python.execfile(NLPInputParser.class.getResourceAsStream("corenlp/stanford_to_narsese.py"));        
        }
        catch (Exception e) {
            String path = "nars_web/src/nars/nlp/corenlp/";
            python.execfile(path + "stanford_to_narsese.py");
        }
    }
    
    private final String host;
    private final int port;

    public NLPInputParser(String host, int port) {
        this.host = host;
        this.port = port;
    }

    
    protected String get_response(String input) throws IOException    {
        CoreNLPClient c = new CoreNLPClient(host, port);
        return c.parse(input);        
    }
    
    @Override
    public boolean parse(NAR nar, String input, TextInputParser lastHandler) {
        try {
            boolean explicit = true;
            char c = input.charAt(0);
            if (c == Symbols.NATURAL_LANGUAGE_MARK) {
                explicit = true;
                input = input.substring(1);
            }
            if (!(explicit || (lastHandler==null)))
                return false;
            
            String left="";
            String right="";
            String response="";
            int mode=0; //conditional mode
            if(!(" "+input).contains(" if ") && !(" "+input).contains(" If "))
            {
                response=get_response(input).trim();
            }
            else
            {
                if(input.contains("then"))
                {
                    left=get_response(input.split("then")[0]).trim();
                    right=get_response(input.split("then")[1]).trim();
                    mode=1;
                }
                else
                {
                    left=get_response(input.split("if")[0]).trim();
                    right=get_response(input.split("if")[1]).trim();
                    mode=2;
                }
            }
            
            boolean isQuestion = input.endsWith("?");
            
            //System.out.println("CoreNLP response: " + response.toString());
            
            String result="";
            python.eval("setsubj(\""+nar.data.get("subj")+"\")");
            if(mode==0)
            {
                result = python.eval("parse(\"" + response + "\", " + (isQuestion ? "True" : "False") + ")").toString().trim();
            }
            if(mode==1) //if a then b
            {
                String narsese_left =  python.eval("parse(\"" + left + "\", " + "True" + ")").toString();
                narsese_left=narsese_left.substring(0,narsese_left.length()-1);
                String narsese_right = python.eval("parse(\"" + right + "\", " + "False" + ")").toString();
                String[] splu=narsese_right.split("\n");
                for(int k=0;k<splu.length;k++)
                {
                    if(splu[k]=="")
                        continue;
                    splu[k]=splu[k].substring(0,splu[k].length()-1);
                    result+="<"+narsese_left+" ==> "+splu[k]+">"+ (isQuestion ? "?" : ".")+"\n";
                }
            }
            if(mode==2) // a if  b
            {
                String narsese_left =  python.eval("parse(\"" + left + "\", " + "False" + ")").toString();
                String narsese_right = python.eval("parse(\"" + right + "\", " + "True" + ")").toString();
                narsese_right=narsese_right.substring(0,narsese_right.length()-1);
                String[] splu=narsese_left.split("\n");
                for(int k=0;k<splu.length;k++)
                {
                    if(splu[k]=="")
                        continue;
                    splu[k]=splu[k].substring(0,splu[k].length()-1);
                    result+="<"+narsese_right+" ==> "+splu[k]+">"+ (isQuestion ? "?" : ".")+"\n";
                }
            }
            nar.data.put("subj",python.eval("getsubj()").toString());
            //System.out.println("stanford_to_narsese response: " + result);
            
            String r = result.trim();
            System.out.println(r);
            
            if (r.length() > 0) {
                new TextInput(nar, new BufferedReader( new StringReader(r)));            
                return true;
            }
        }
        catch (IOException e) {
            return false;
        }        
        return false;
    }
    
    /*
    public static void main(String[] args) {
        NAR n = new NAR();
        new NLPInputParser("host", 9100).parse(n, "This is a sentence.", null);
    }
    */
}
