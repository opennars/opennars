/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.lab.microworld;

import java.util.HashMap;
import nars.NAR;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.gui.NARSwing;
import nars.io.Answered;
import nars.io.Narsese;

/**
 *
 * @author patrick.hammer
 */
public class SupervisedRecognition {
    
    public static void main(String[] args) {
        
        HashMap<String, Integer> map = new HashMap<>();

        //ONES:
        
        map.put("oxooo" + "\n" +
                "xxooo" + "\n" +
                "oxooo" + "\n" +
                "oxooo" + "\n" +
                "oxooo" + "\n", 
                1);
        
        map.put("oxxoo" + "\n" +
                "xoxoo" + "\n" +
                "ooxoo" + "\n" +
                "ooxoo" + "\n" +
                "ooxoo" + "\n", 
                1);
        
        map.put("oooxo" + "\n" +
                "ooxxo" + "\n" +
                "oooxo" + "\n" +
                "oooxo" + "\n" +
                "oooxo" + "\n", 
                1);
        
        map.put("oooox" + "\n" +
                "oooxx" + "\n" +
                "oooox" + "\n" +
                "oooox" + "\n" +
                "oooox" + "\n", 
                1);
        
        //ZEROS:
        
        map.put("ooooo" + "\n" +
                "xxxxx" + "\n" +
                "xooox" + "\n" +
                "xooox" + "\n" +
                "xxxxx" + "\n", 
                0);
        
        map.put("xxxxx" + "\n" +
                "xooox" + "\n" +
                "xooox" + "\n" +
                "xooox" + "\n" +
                "xxxxx" + "\n", 
                0);
        
        map.put("ooooo" + "\n" +
                "ooooo" + "\n" +
                "xxxxx" + "\n" +
                "xooox" + "\n" +
                "xxxxx" + "\n", 
                0);
        
        map.put("ooooo" + "\n" +
                "oxxxx" + "\n" +
                "oxoox" + "\n" +
                "oxoox" + "\n" +
                "oxxxx" + "\n", 
                0);
        
        map.put("ooooo" + "\n" +
                "xxxxx" + "\n" +
                "xooox" + "\n" +
                "xooox" + "\n" +
                "xxxxx" + "\n", 
                0);
        
        //training phase:
        
        NAR nar = new NAR();
        NARSwing.themeInvert();
        new NARSwing(nar);
        nar.param.noiseLevel.set(0);
        
        for(String example : map.keySet()) {
            int solution = map.get(example);
            inputExample(nar, example, solution);
            nar.step(1000);
        }
        
        //Test phase:
        
        inputExample(nar, 
                "ooxoo" + "\n" +
                "ooxoo" + "\n" +
                "ooxoo" + "\n" +
                "ooxoo" + "\n" +
                "ooxoo" + "\n", -1);
        
        try {
                nar.ask("<?what --> EXAMPLE>", new Answered() {
                    @Override
                    public void onSolution(Sentence belief) {
                        System.out.println(belief);
                    }
                    @Override
                    public void onChildSolution(Task child, Sentence belief) {}
                });
            } catch (Narsese.InvalidInputException ex) {
        }
        
        nar.step(100000);
    }

    //Inputs an example image
    private static void inputExample(NAR nar, String example, int solution) {
        String[] lines = example.split("\n");
        for(int i=0;i<lines.length;i++) {
            for(int j=0;j<lines[i].length();j++) {
                if(lines[i].charAt(j) == 'x') {
                    String inp = "<T_"+String.valueOf(i)+"_"+String.valueOf(j) + "--> on>. :|:";
                    nar.addInput(inp);
                }
            }
        }
        if(solution != -1) {
            nar.addInput("<"+ String.valueOf(solution) +" --> EXAMPLE>. :|:");    
        }
    }
    
}
