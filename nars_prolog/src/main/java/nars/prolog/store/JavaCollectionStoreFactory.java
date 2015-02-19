package nars.prolog.store;


import nars.prolog.ClauseStore;
import nars.prolog.Prolog;
import nars.prolog.Struct;
import nars.prolog.Term;
import nars.prolog.lib.JavaLibrary;

import java.util.List;

public class JavaCollectionStoreFactory //implements ClauseStoreFactory
{
	public ClauseStore buildClause(Prolog prolog, Term goal, List<?> varList)
	{
		if (goal instanceof Struct)
		{
			Struct s = (Struct)goal;
			if (s.getName().equals("collection_item") && s.getArity() == 2)
			{
				try
				{
					JavaLibrary jl = (JavaLibrary)prolog.getLibraryManager().getLibrary("alice.tuprolog.lib.JavaLibrary");
					Term collectionTerm = s.getArg(0).getTerm();
					Object obj = jl.getRegisteredDynamicObject((Struct)collectionTerm);
					if (obj instanceof java.util.Collection)
						return new CollectionItemsStore(prolog, 
													    (java.util.Collection<?>)obj, 
													    s.getArg(1), 
													    varList, 
													    jl);				
				}
				catch (Exception ex) { throw new RuntimeException(ex); }
			}
		}
		return null;
	}
}
