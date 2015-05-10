package nars.tuprolog.store;


import nars.nal.term.Term;
import nars.tuprolog.ClauseStore;
import nars.tuprolog.PTerm;
import nars.tuprolog.Prolog;
import nars.tuprolog.Struct;
import nars.tuprolog.lib.JavaLibrary;

import java.util.List;

public class JavaCollectionStoreFactory //implements ClauseStoreFactory
{
	public ClauseStore buildClause(Prolog prolog, PTerm goal, List<?> varList)
	{
		if (goal instanceof Struct)
		{
			Struct s = (Struct)goal;
			if (s.getName().equals("collection_item") && s.size() == 2)
			{
				try
				{
					JavaLibrary jl = (JavaLibrary)prolog.getLibraries().getLibrary("alice.tuprolog.lib.JavaLibrary");
					Term collectionTerm = s.getTermX(0).getTerm();
					Object obj = jl.getRegisteredDynamicObject((Struct)collectionTerm);
					if (obj instanceof java.util.Collection)
						return new CollectionItemsStore(prolog, 
													    (java.util.Collection<?>)obj, 
													    s.getTermXP(1),
													    varList, 
													    jl);				
				}
				catch (Exception ex) { throw new RuntimeException(ex); }
			}
		}
		return null;
	}
}
