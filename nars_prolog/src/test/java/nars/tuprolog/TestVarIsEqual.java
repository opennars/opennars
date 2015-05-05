package nars.tuprolog;

import junit.framework.TestCase;
import nars.tuprolog.event.OutputEvent;
import nars.tuprolog.event.OutputListener;



/**
 * @author George S. Cowan
 *
 */
public class TestVarIsEqual extends TestCase {
  
  Prolog core;
  String yes = "yes.\n";
  private SysoutListener sysoutListener = new SysoutListener();
  
  protected void setUp() throws Exception {
    super.setUp();
    core = new Prolog();
    core.addOutputListener(sysoutListener);
  }
  
  private class SysoutListener implements OutputListener {
    public StringBuilder builder = new StringBuilder("");
    
    public void onOutput(OutputEvent ev) {
      builder.append(ev.getMsg());
    }
    public String getAllOutput() {
      return builder.toString();
    }
  }
  
  public void testDifferntVarsCompareEqual() throws MalformedGoalException, InvalidTheoryException {
    // theory is modified code from PTTP 
    String theory = "test :- body_for_head_literal_instrumented(d(X,Y),(not_d(X,U);d(X,Y)),Bod).    "
        + "\n" +    "                                                                 "
        + "\n" +    "body_for_head_literal_instrumented(Head,Wff,Body) :-             "
        + "\n" +    "  nl,printMeaning('body_for_head_literal input Head: '),printMeaning(Head),    "
        + "\n" +    "  nl,printMeaning('                             Wff: '),printMeaning(Wff),     "
        + "\n" +    "  false -> true ;                                                "
        + "\n" +    "  Wff = (A ; B) ->                                               "
        + "\n" +    "    nl,printMeaning('OR'),                                              "
        + "\n" +    "    body_for_head_literal_instrumented(Head,A,A1),               "
        + "\n" +    "    body_for_head_literal_instrumented(Head,B,B1),               "
        + "\n" +    "    conjoin(A1,B1,Body)                                          "
        + "\n" +    "    , nl, printMeaning('body_for_head_literal OR - Body: '),printMeaning(Body) "
        + "\n" +    "    ;                                                            "
        + "\n" +    "  Wff == Head ->                                                 "
        + "\n" +    "    Body = true;                                                 "
        + "\n" +    "  negated_literal_instrumented(Wff,Head) ->                      "
        + "\n" +    "    printMeaning(' '),                                                  "
        + "\n" +    "    Body = false;                                                "
        + "\n" +    "  %true ->                                                       "
        + "\n" +    "    nl,printMeaning('OTHERWISE'),                                       "
        + "\n" +    "    negated_literal_instrumented(Wff,Body).                      "
        + "\n" +    "                                                                 "
        + "\n" +    "negated_literal_instrumented(Lit,NotLit) :-                      "
        + "\n" +    "  nl,printMeaning('*** negated_literal in Lit:'),printMeaning(Lit),            "
        + "\n" +    "  nl,printMeaning('***                 NotLit:'),printMeaning(NotLit),                              "
        + "\n" +    "  Lit =.. [F1|L1],                                               "
        + "\n" +    "  negated_functor(F1,F2),                                        "
        + "\n" +    "  (var(NotLit) ->                                                "
        + "\n" +    "    NotLit =.. [F2|L1];                                          "
        + "\n" +    "  %true ->                                                       "
        + "\n" +    "    nl,printMeaning('                 Not var:'),printMeaning(NotLit),                            "
        + "\n" +    "    NotLit =.. [F2|L2],                                          "
        + "\n" +    "    nl,printMeaning('***              Lit array:'),printMeaning(L1),           "
        + "\n" +    "    nl,printMeaning('***           NotLit array:'),printMeaning(L2),           "
        + "\n" +    "    L1 == L2                                                     " // ERROR HAPPENS HERE
        + "\n" +    "    , nl,printMeaning('***               SUCCEEDS')                     "
        + "\n" +    "    ).                                                           "
        + "\n" +    "                                                                 "
        + "\n" +    "negated_functor(F,NotF) :-                                       "
        + "\n" +    "  atom_chars(F,L),                                               "
        + "\n" +    "  atom_chars(not_,L1),                                           "
        + "\n" +    "  (list_append(L1,L2,L) ->                                       "
        + "\n" +    "    true;                                                        "
        + "\n" +    "  %true ->                                                       "
        + "\n" +    "    list_append(L1,L,L2)),                                       "
        + "\n" +    "  atom_chars(NotF,L2).                                           "
        + "\n" +    "                                                                 "
        + "\n" +    "conjoin(A,B,C) :-                                                "
        + "\n" +    "  A == true ->                                                   "
        + "\n" +    "    C = B;                                                       "
        + "\n" +    "  B == true ->                                                   "
        + "\n" +    "    C = A;                                                       "
        + "\n" +    "  A == false ->                                                  "
        + "\n" +    "    C = false;                                                   "
        + "\n" +    "  B == false ->                                                  "
        + "\n" +    "    C = false;                                                   "
        + "\n" +    "  %true ->                                                       "
        + "\n" +    "    % nl,printMeaning('conjoin A: '),printMeaning(A),printMeaning(' B: '),printMeaning(B),   "
        + "\n" +    "    C = (A , B)                                                  "
        + "\n" +    "    % , nl,printMeaning('    out A: '),printMeaning(A),printMeaning(' B: '),printMeaning(B)  "
        + "\n" +    "    % , nl,printMeaning('        C: '),printMeaning(C)                         "
        + "\n" +    "  .                                                              "
        + "\n" +    "                                                                 "
        + "\n" +    "list_append([X|L1],L2,[X|L3]) :-                                 "
        + "\n" +    "  list_append(L1,L2,L3).                                         "
        + "\n" +    "list_append([],L,L).                                             "
        + "\n" +    "                                                                 "
        ;
    
    core.setTheory(new Theory(theory));
    
    SolveInfo info = core.solve("test. ");
    
    
    //assertTrue("Test should complete normally: " + info,info.isSuccess());
    
    /*
    String expected = ""
      + "\n" +    "body_for_head_literal input Head: d(X_e1,Y_e1)"
      + "\n" +    "                             Wff: ';'(not_d(X_e1,U_e1),d(X_e1,Y_e1))"
      + "\n" +    "OR"
      + "\n" +    "body_for_head_literal input Head: d(X_e25,Y_e25)"
      + "\n" +    "                             Wff: not_d(X_e25,U_e25)"
      + "\n" +    "*** negated_literal in Lit:not_d(X_e25,U_e25)  NotLit:d(X_e25,Y_e25)"
      + "\n" +    "***              Lit array:[X_e122,U_e86]"
      + "\n" +    "***           NotLit array:[X_e122,Y_e122]"
      + "\n" +    "OTHERWISE"
      + "\n" +    "*** negated_literal in Lit:not_d(X_e122,U_e86)  NotLit:NotLit_e136"
      + "\n" +    "body_for_head_literal input Head: d(X_e184,Y_e122)"
      + "\n" +    "                             Wff: d(X_e184,Y_e122)"
      + "\n" +    "Wff == Head"
      + "\n" +    "body_for_head_literal OR - Body: d(X_e249,U_e249)"
      + "\n" +    ""
    ;
    
  assertEquals("Var == should not succeed.", expected, sysoutListener.getAllOutput());
            */
  }

}
