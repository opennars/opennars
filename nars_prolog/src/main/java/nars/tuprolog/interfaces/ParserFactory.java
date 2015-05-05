package nars.tuprolog.interfaces;

import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import nars.tuprolog.OperatorManager;
import nars.tuprolog.Parser;
import nars.tuprolog.Term;

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
    public static IParser createParser(String theory, ObjectIntHashMap<Term> mapping) {
    	return new Parser(theory, mapping);
    }    
	
	/**
     * creating a Parser specifing how to handle operators
     * and what text to parse
     */
    public static IParser createParser(IOperatorManager op, String theory) {
    	return new Parser((OperatorManager)op, theory);
    }
    
    /**
     * creating a Parser specifing how to handle operators
     * and what text to parse
     */
    public static IParser createParser(IOperatorManager op, String theory, ObjectIntHashMap<Term>  mapping) {
    	return new Parser((OperatorManager)op, theory, mapping);
    }

}
