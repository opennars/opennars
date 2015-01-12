/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.jwam;

import nars.jwam.WAMProlog.Answer;
import nars.jwam.WAMProlog.Answering;
import nars.jwam.WAMProlog.Query;
import nars.jwam.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author me
 */
public class WAMPrologREPL {

    public static void main(String[] args) throws IOException {
        WAMProlog w = WAMProlog.newSmall();
        BufferedReader b = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = b.readLine()) !=null) {
            try {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                
                System.out.println("inputtnig: " + line);
                Query q = w.query(line);
                
                q.getAnswers(new Answering() {

                    @Override
                    public boolean onNextAnswer(Query q, Answer a) {
                        return false;
                    }
                    
                }, 0);
                
            } catch (ParseException ex) {
                System.err.println(ex);
            }
            
        }
    }
}
