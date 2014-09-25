package nars.race1d;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.io.Output;
import nars.operator.NullOperator;

/**
 *
 * @author me
 */
public class Race1D {

    
    public final boolean grid[][];
    private final int width;
    private final int length;
    
    /** width = # of vertical states, length = total distance of the race */
    public Race1D(int width, int length) {
        this.length = length;
        this.width = width;
        grid = new boolean[length][];
        for (int i = 0; i < length; i++)
            grid[i] = new boolean[width];
        
        
    }
    
    public void draw() {
        for (int w = -1; w <= width; w++) {
            
            for (int i = 0; i < length; i++) {
                if ((w == -1) || (w == width)) {
                    System.out.print('*');
                }
                else {
                    System.out.print(grid[i][w] ? '=' : ' ');
                }                
            }
            
            System.out.println();
        }
    }
    
    /** generates a single path that is guaranteed to be solvable */
    public int[] randomSinglePath() {
        int[] actions = new int[length];
        int opening = 0;
        for (int l = 0; l < length; l++) {
            
            for (int w = 0;  w < width; w++)
                grid[l][w] = (w==opening) ? false : true;
            
            boolean left = Math.random() < 0.5;
            opening = opening + (left ? -1 : 1);
            if (opening < 0) opening = 0;
            if (opening == width) opening = width-1;
         
            
            actions[l] = left ? -1 : 1;            
        }
        return actions;        
    }
    
    private static List<String> narsese(int[] solution) {
        List<String> s = new ArrayList();
        for (int i : solution) {
            if (i == -1) 
                s.add("(^left,x)!");
            else if (i == 1)
                s.add("(^right,x)!");
        }
        return s;
    } 
    
    public int n(int w) {
        return Math.min(width-1, Math.max(0, w));
    }

    public String getInput(int i, int position) {
        String input = "";
        int aheadLeft = grid[i+1][n(position-1)] ? 1 : 0;
        int aheadCenter = grid[i+1][n(position)] ? 1 : 0;
        int aheadRight = grid[i+1][n(position+1)] ? 1 : 0;
        input += "<" + aheadLeft + " --> aheadLeft>. :|: \n";
        input += "<" + aheadCenter + " --> aheadCenter>. :|: \n";
        input += "<" + aheadRight + " --> aheadRight>. :|: \n";
        return input;        
    }
    
    public void train(NAR n) {        
        int sol[] = randomSinglePath();        
        List<String> solution = narsese( sol );

        int position = 0;
        
        for (int i = 0; i < length-1; i++) {
            //provide input as a vector, then action
//            String input = "<(*,";
//            for (int j = 0; j < width; j++) {
//                input += grid[i][j] ? "1" : "0";
//                if (j < width-1)
//                    input += ",";
//            }
//            input += ") --> see>. :|:";
            
            
        
            String input = getInput(i, position);
            input += solution.get(i);
            
            n.addInput(input);
            n.finish(1);
            
            
            if (sol[i] == -1)
                position--;
            else if (sol[i] == 1)
                position++;
            position = n(position);
        }
        
    }
    
    public void evaluate(NAR n) {
        int sol[] = randomSinglePath();

        int position = 0;
        
        AtomicInteger nextOut = new AtomicInteger(0);
        Output o = new Output() {

            @Override public void output(Class channel, Object signal) {
                if (channel != IN.class) {
                    String x = signal.toString();
                    if (x.contains("!"))
                        System.out.println(x);
                    if (x.contains("(^left,x)!")) {
                        nextOut.set(-1);
                    }
                    else if (x.contains("(^right,x)!")) {
                        nextOut.set(1);
                    }
                }
            }            
        };
        n.addOutput(o);
        n.cycle(1);
        
        
        for (int i = 0; i < length-1; i++) {
            String s = getInput(i, position);
            n.addInput(s);
            n.finish(1);
            
            final int UNKNOWN = -100;
            int deadline = 100;
            int time = 0;
            nextOut.set(UNKNOWN);
            //wait for answer
            while ((time < deadline) && (nextOut.get() == UNKNOWN)) {
                n.cycle(1);
                time++;
            }
            if (nextOut.get()!=UNKNOWN) {
                System.out.println(nextOut.get() + " == " + sol[i] + " ? @" + time );
            }
            else {
                System.out.println("fail");
            }

            
            if (sol[i] == -1)
                position--;
            else if (sol[i] == 1)
                position++;
            position = n(position);
        }
        n.removeOutput(o);
    }

    private void addOperations(NAR n) {
        n.memory.addOperator(new NullOperator("^left") {
            
        });
        n.memory.addOperator(new NullOperator("^right") {
            
        });
    }

    
    public static void main(String[] args) {
        Race1D r = new Race1D(5, 16);
        
        
        NAR n = new DefaultNARBuilder().build();
        r.addOperations(n);
        
        int trainingCycles = 10;
        for (int i = 0; i < trainingCycles; i++) {
            r.train(n);
        }

        r.evaluate(n);
        
        n.cycle(1);
        n.finish(1);
        r.draw();
    }


}
