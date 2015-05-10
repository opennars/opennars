package nars.tuprolog.store;

import nars.nal.term.Term;
import nars.tuprolog.*;
import nars.tuprolog.lib.JavaLibrary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;



public class CollectionItemsStore extends ClauseStore
{
	private Struct collection;
	private Iterator iter;
	private PTerm current;
	private JavaLibrary lib;
	private Prolog engine;
	
	void nextCompatible()
	{
		current = null;
		while (current == null && iter.hasNext())
		{
			current = lib.registerDynamic(iter.next());
			List v1 = new ArrayList();
			List v2 = new ArrayList();
			if (!goal.unify( v1, v2, current))
				current = null;
			Var.free(v1);
			Var.free(v2);
		}
	}
	
	public CollectionItemsStore(Prolog engine, Collection C, PTerm goal, List vars, JavaLibrary lib)	{
        super(goal, vars);
		iter = C.iterator();
		this.lib = lib;

		this.collection = lib.registerDynamic(C);
		this.engine = engine;		
		nextCompatible();
	}
	
	public void close() { /* nothing to do here */ }
	
	public Clause fetch()
	{
		if (current == null)
			return null;
		Var.free(vars);
		CollectionItemClause result = new CollectionItemClause(new Struct(
				"collection_item", new PTerm[] {collection, current} ));
		nextCompatible();
		return result;
	}

	public boolean hasCompatibleClause()
	{
		return current != null;
	}

	public boolean haveAlternatives()
	{
		return current != null;
	}
}