package prolog;

import nars.prolog.Agent;

public class TestEinsteinRiddle {
    
    public static void main(String[] args) {
        String[] str = new String[2];
        str[0] = "./alice/tuprolog/einsteinsRiddle.pl";
        str[1] = "einstein(_,X), write(X).";
        Agent.main(str);
    }
    
}
