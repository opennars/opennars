/*
 * Created on Dec 10, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package prolog;

import nars.tuprolog.Prolog;
import nars.tuprolog.SolveInfo;
import nars.tuprolog.Theory;
import nars.tuprolog.event.SpyEvent;
import nars.tuprolog.event.SpyListener;

/**
 * @author aricci
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestBug {

    
	public static void main(String[] args) throws Exception {

	    
	    String goal = "out('"+
			"can_do(X).\n"+
			"can_do(Y).\n"+
		"').";
		
		new Prolog().solve(goal);
		
		String st =
		"p(X).				\n"+
		"test(L1,L2):-		\n"+
		"	findall(p(X),p(X),L1), \n"+
		"	append([a,b],L1,L2).	\n";
		
		
		Prolog engine = new Prolog();
		engine.addSpyListener(new SpyListener(){ 
			public void onSpy(SpyEvent e){
				System.out.println(e);
			}
		});
		//engine.setSpy(true);
		engine.setTheory(new Theory(st));
		SolveInfo info = engine.solve("test(L1,L2).");
		System.out.println(info);
		
	}
}
