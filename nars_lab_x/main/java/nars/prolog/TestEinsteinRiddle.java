package prolog;

import nars.tuprolog.Agent;

public class TestEinsteinRiddle {
    
    public static void main(String[] args) {
        String[] str = new String[2];
        str[0] = "nars_prolog/src/test/java/nars/prolog/einsteinsRiddle.pl";
        str[1] = "einstein(_,X), write(X).";
        Agent.main(str);
    }
    
}
