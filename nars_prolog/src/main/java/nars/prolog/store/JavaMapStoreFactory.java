package nars.prolog.store;


import nars.prolog.ClauseStore;
import nars.prolog.Prolog;
import nars.prolog.Struct;
import nars.prolog.Term;
import nars.prolog.lib.JavaLibrary;

import java.util.List;

public class JavaMapStoreFactory //implements ClauseStoreFactory
{
	public ClauseStore buildClause(Prolog prolog, Term goal, List<?> varList)
	{
		if (goal instanceof Struct)
		{
			Struct s = (Struct)goal;
			if (s.getName().equals("map_entry") && s.getArity() == 3)
			{
				try
				{
					JavaLibrary jl = (JavaLibrary)prolog.getLibraryManager().getLibrary("alice.tuprolog.lib.JavaLibrary");
					Term mapTerm = s.getArg(0).getTerm();
					Object obj = jl.getRegisteredDynamicObject((Struct)mapTerm);
					if (obj instanceof java.util.Map)
						return new MapEntriesStore(prolog, (java.util.Map<?,?>)obj, s.getArg(1), s.getArg(2), varList, jl);					
				}
				catch (Exception ex) 
				{ 
					throw new RuntimeException(ex); 
				}				
			}
		}
		return null;
	}
}