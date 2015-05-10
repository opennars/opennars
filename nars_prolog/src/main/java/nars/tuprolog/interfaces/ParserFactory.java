package nars.tuprolog.interfaces;

import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import nars.tuprolog.Operators;
import nars.tuprolog.Parser;
import nars.tuprolog.PTerm;

public class ParserFactory {
	
	/**
     * Creating a parser with default operate interpretation
     */
	public static IParser createParser(String theory) {
		return new Parser(theory);
	}
	
	/**
     * creating a parser with default operate interpretation
     */
    public static IParser createParser(String theory, ObjectIntHashMap<PTerm> mapping) {
    	return new Parser(theory, mapping);
    }    
	
	/**
     * creating a Parser specifing how to handle operators
     * and what text to parse
     */
    public static IParser createParser(IOperators op, String theory) {
    	return new Parser((Operators)op, theory);
    }
    
    /**
     * creating a Parser specifing how to handle operators
     * and what text to parse
     */
    public static IParser createParser(IOperators op, String theory, ObjectIntHashMap<PTerm>  mapping) {
    	return new Parser((Operators)op, theory, mapping);
    }

}
