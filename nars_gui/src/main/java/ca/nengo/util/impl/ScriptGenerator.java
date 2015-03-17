package ca.nengo.util.impl;

import ca.nengo.model.Network;
import ca.nengo.model.Node;
import ca.nengo.util.ScriptGenException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class ScriptGenerator extends DFSIterator {

	final HashMap<Node, String> prefixes;
	
	PrintWriter writer;
	final StringBuilder script;
	final char spaceDelimiter = '_';
	final String topLevelPrefix = "net";
    Stack<Network> parentNetwork;
    int inTemplateNetwork;
	
	public ScriptGenerator(File file) throws FileNotFoundException {
		prefixes = new HashMap<Node, String>();
		
		script = new StringBuilder(); 
		
		this.writer = new PrintWriter(file);

        parentNetwork = new Stack<Network>();
		
		writer.write("import nef\n");
		writer.write("from ca.nengo.math.impl import ConstantFunction, FourierFunction, PostfixFunction\n");
		writer.write("import math\n");
		
        inTemplateNetwork = 0;
	}

    public DFSIterator startDFS(Node node) {
        if (!(node instanceof Network)) {
            System.out.println("Cannot generate script when top level node is not a Network");
            return this;
        }

        parentNetwork.push((Network)node);
        return super.startDFS(node);
    }
	
    @SuppressWarnings("unchecked")
	protected void pre(Node node) {
        if (parentNetwork.peek().getMetaData("templates") != null &&
        	    ((ArrayList)parentNetwork.peek().getMetaData("templates")).contains(node.name()))
        {
            inTemplateNetwork++;
        }

        if (inTemplateNetwork <= 0) 
        {
            if (topLevel)
			{
				prefixes.put(node, topLevelPrefix);
			}
			
			for (Node child : node.getNodes())
			{
				String prefix;
				String nameNoSpaces = node.name().replaceAll("\\p{Blank}|\\p{Punct}", Character.toString(spaceDelimiter));
				
				if(topLevel)
					prefix = topLevelPrefix + spaceDelimiter + nameNoSpaces;
				else
					prefix = prefixes.get(node) + spaceDelimiter + nameNoSpaces ;
				
				prefixes.put(child, prefix);
			}
			
			HashMap<String, Object> toScriptArgs = new HashMap<String, Object>();
			toScriptArgs.put("prefix", prefixes.get(node) + spaceDelimiter);
			toScriptArgs.put("isSubnet", !topLevel);
			toScriptArgs.put("netName", prefixes.get(node));
			toScriptArgs.put("spaceDelim", spaceDelimiter);
	
	        
            try {
                String code = node.toScript(toScriptArgs);
                script.append(code);
            } catch(ScriptGenException e) {
                System.out.println(e.getMessage());
            } 

            if (node instanceof Network) {
                parentNetwork.push((Network)node);
            }
        }
	}
	
    @SuppressWarnings("unchecked")
	protected void post(Node node)
	{
//        if (node instanceof Network && inTemplateNetwork <= 0)
//        {
//            Network net = (Network)node;
//            parentNetwork.pop();
//
//            HashMap<String, Object> toScriptArgs = new HashMap<String, Object>();
//            toScriptArgs.put("prefix", prefixes.get(node) + spaceDelimiter);
//            toScriptArgs.put("isSubnet", !topLevel);
//            toScriptArgs.put("netName", prefixes.get(node));
//            toScriptArgs.put("spaceDelim", spaceDelimiter);
//
//            //try {
//                String code = net.toPostScript(toScriptArgs);
//                script.append(code);
//            /*} catch(ScriptGenException e) {
//                System.out.println(e.getMessage());
//            }*/
//
//            script.append("\n# ").append(node.getName()).append(" - Projections\n");
//
//            for(Projection proj : ((Network) node).getProjections())
//            {
//                HashMap templateProjections = (HashMap)((Network) node).getMetaData("templateProjections");
//                String preName = proj.getSource().getNode().getName();
//                String postName = proj.getTarget().getNode().getName();
//                if (templateProjections == null || !postName.equals(templateProjections.get(preName)))
//                {
//                    try {
//                        String code2 = proj.toScript(toScriptArgs);
//                        script.append(code2);
//                    } catch(ScriptGenException e) {
//                        System.out.println(e.getMessage());
//                    }
//                }
//            }
//
//            script.append("\n# Network ").append(node.getName()).append(" End\n\n");
//
//            if(topLevel)
//            {
//            	String nameNoSpaces = topLevelPrefix + spaceDelimiter + node.getName().replaceAll("\\p{Blank}|\\p{Punct}", Character.toString(spaceDelimiter));
//            	script.append(nameNoSpaces).append(".add_to_nengo()\n");
//            }
//        }
//
//        if (parentNetwork.peek().getMetaData("templates") != null &&
//                ((ArrayList)parentNetwork.peek().getMetaData("templates")).contains(node.getName()))
//        {
//            inTemplateNetwork--;
//        }
	}
	
	protected void finish()
	{	
		writer.write(script.toString());
		writer.close();
	}
}
