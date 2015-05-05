package nars.core;

import nars.nal.NALOperator;/**
 *
 * @author me
 */


public class PrintSymbols {

    public static void main(String[] args) {
        int relations = 0;
        int innates = 0;
        int symbols = 0;
        
        System.out.println("string" + "\t\t" + "rel?" + "\t\t" + "innate?" + "\t\t" + "opener?" + "\t\t" + "closer?");
        for (NALOperator i : NALOperator.values()) {
            System.out.println(i.symbol + "\t\t" + i.relation + "\t\t" + i.isNative + "\t\t" + i.opener + "\t\t" + i.closer); 
            if (i.relation) relations++;
            if (i.isNative) innates++;
            symbols++;
        }
        System.out.println("symbols=" + symbols + ", relations=" + relations + ", innates=" + innates);
    }
}
