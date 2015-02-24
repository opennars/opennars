/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import jurls.core.approximation.ApproxParameters;
import jurls.core.approximation.DiffableFunctionGenerator;
import jurls.core.approximation.DiffableFunctionMarshaller;
import jurls.core.approximation.DiffableSymbols;
import jurls.core.approximation.Generator;
import jurls.core.approximation.ParameterizedFunction;
import jurls.core.approximation.ParameterizedFunctionGenerator;
import jurls.core.approximation.UnaryDoubleFunction;
import jurls.core.utils.compile.FastExpression;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author me
 */
public class TestFastExpression {

    @Test
    public void testSimpleExpression() throws Exception {

        UnaryDoubleFunction e = new FastExpression().compile("Math.sin(x)+Math.tan(x)");
        assertEquals(Math.sin(1) + Math.tan(1), e.evaluate(1), 0.001);

    }
    
    @Test public void testRegex() {
        String i = "(-1*((((((c20))*sin(c18))*(a[83])))*(a[87])))";
        System.out.println(i);
        
        //wrong one
        System.out.println(                
                i.replaceAll("\\(\\((.+)\\)\\)", "\\($1\\)")
        );
 
        //right one
        System.out.println(
                i.replaceAll("\\(\\(([^)]*)\\)\\)", "\\($1\\)")  //remove duplicate parens
        );
    }
//
//    @Test
//    public void testDiffableFunctionExpression1() {
//        DiffableFunctionGenerator dfg = Generator.generateFourierBasis();
//        DiffableFunctionMarshaller dfm = new DiffableFunctionMarshaller(dfg, 1, 1);
//        DiffableSymbols ds;
//        String output = dfm.partialDeriveExpression(new StringBuilder(), ds = new DiffableSymbols());
//        System.out.println(output);
//        //System.out.println(ds);
//    }

}
