package nars.tuprolog.store;


import nars.nal.term.Term;
import nars.tuprolog.ClauseStore;
import nars.tuprolog.PTerm;
import nars.tuprolog.Prolog;
import nars.tuprolog.Struct;
import nars.tuprolog.lib.JavaLibrary;

import java.util.List;

public class JavaMapStoreFactory //implements ClauseStoreFactory
{
	public ClauseStore buildClause(Prolog prolog, Struct goal, List<?> varList)
	{
		/*if (goal instanceof Struct)
		{*/
			Struct s = (Struct)goal;
			if (s.getName().equals("map_entry") && s.size() == 3)
			{
				try
				{
					JavaLibrary jl = (JavaLibrary)prolog.getLibraries().getLibrary("alice.tuprolog.lib.JavaLibrary");
					Term mapTerm = s.getTermX(0).getTerm();
					Object obj = jl.getRegisteredDynamicObject((Struct)mapTerm);
					Term x1 = s.getTermX(1);
					Term x2 = s.getTermX(2);

					if ((x1 instanceof PTerm) && (x2 instanceof PTerm) && obj instanceof java.util.Map)
						return new MapEntriesStore(prolog, (java.util.Map)obj, (PTerm)x1, (PTerm)x2, varList, jl);
				}
				catch (Exception ex) 
				{ 
					throw new RuntimeException(ex); 
				}				
			}

		return null;
	}
}