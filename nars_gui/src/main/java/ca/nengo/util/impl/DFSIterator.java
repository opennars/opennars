package ca.nengo.util.impl;

import ca.nengo.model.Node;


public class DFSIterator {
	
	protected boolean topLevel;
	
	public DFSIterator()
	{
		topLevel = false;
	}
	
	protected void pre(Node node)
	{
		
	}
	
	public DFSIterator startDFS(Node node)
	{
		topLevel = true;
		DFS(node);
		finish();
		return this;
	}
	
	protected DFSIterator DFS(Node node)
	{		
		pre(node);
		
		Node[] children = node.getChildren();
		
		boolean oldTopLevel = topLevel;
		topLevel = false;
		
		for(Node n : children)
		{
			DFS(n);
		}
		
		topLevel = oldTopLevel;
		
		post(node);
		
		return this;
	}
	
	protected void post(Node node)
	{
	}
	
	protected void finish()
	{
	}
}
