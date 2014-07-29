package nars.test.core;

import nars.io.Symbols;
import nars.io.Symbols.InnateOperator;

/**
 *
 * @author me
 */


public class PrintSymbols {

    public static void main(String[] args) {
        int relations = 0;
        int innates = 0;
        int symbols = 0;
        
        System.out.println("string" + "\t\t" + "rel?" + "\t\t" + "innate?" + "\t\t" + "opener?" + "\t\t" + "closer?");
        for (InnateOperator i : Symbols.InnateOperator.values()) {
            System.out.println(i.string + "\t\t" + i.relation + "\t\t" + i.innate + "\t\t" + i.opener + "\t\t" + i.closer); 
            if (i.relation) relations++;
            if (i.innate) innates++;
            symbols++;
        }
        System.out.println("symbols=" + symbols + ", relations=" + relations + ", innates=" + innates);
    }
}
