package nars.tuprolog.store;

import nars.nal.term.Term;
import nars.tuprolog.*;
import nars.tuprolog.lib.JavaLibrary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



public class MapEntriesStore extends ClauseStore
{
	private Struct map;
	private Iterator<?> iter;
	private JavaLibrary jl;
	private Prolog engine;
	private PTerm key, value;
	private List varList;
	private Term currentKey = null, currentValue = null;
	
	void nextCompatible()
	{
		currentKey = currentValue = null;
		while (currentKey == null && iter.hasNext())
		{
			Map.Entry<?,?> e = (Map.Entry<?,?>)iter.next();
			currentKey = jl.registerDynamic(e.getKey());
			currentValue = jl.registerDynamic(e.getValue());
			List v1 = new ArrayList();
			List v2 = new ArrayList();
			if (!key.unify(v1, v2, currentKey) || !value.unify(v1, v2, currentValue))
				currentKey = currentValue = null;
			Var.free(v1);
			Var.free(v2);
		}
	}
	
	public MapEntriesStore(Prolog engine, Map map, PTerm key, PTerm value, List varList, JavaLibrary lib)
	{
        super(null, varList);
		iter = map.entrySet().iterator();
		this.map = lib.registerDynamic(map);
		this.key = key;
		this.value = value;
		this.varList = varList;
		this.jl = lib;
		this.engine = engine;	
		
		nextCompatible();
	}
	
	public void close() { /* nothing to do here */ }
	
	public Clause fetch()
	{	
		if (currentValue == null)
			return null;
		Var.free(varList);
		CollectionItemClause result = new CollectionItemClause(new Struct(
				"map_entry", new Term[] {map, currentKey, currentValue} ));
		nextCompatible();
		return result;
	}

	public boolean hasCompatibleClause()
	{
		return currentValue != null;
	}

	public boolean haveAlternatives()
	{
		return currentValue != null;	
	}
}
