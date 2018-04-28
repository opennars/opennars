package nars.lab.vision;

import java.util.ArrayList;
import java.util.List;
import nars.main.NAR;
import nars.control.DerivationContext;
import static nars.control.TemporalInferenceControl.proceedWithTemporalInduction;
import nars.entity.Task;
import nars.plugin.perception.SensoryChannel;

public class ConcatVisionChannel extends SensoryChannel {
    public class Position {
        public int X;
        public int Y;
    }
    
    Task[][] inputs;
    public ConcatVisionChannel(NAR nar, SensoryChannel reportResultsTo, int width, int height) {
        super(nar,reportResultsTo, width, height, width*height);
        inputs = new Task[height][width];
    }
    
    public void AddToSpatialBag(Task t) {
        int x = t.getTerm().term_indices[2];
        int y = t.getTerm().term_indices[3];
        t.incPriority((float) this.topDownPriority(t.getTerm()));
        inputs[y][x] = t;
        Position pos = new Position();
        pos.X = x;
        pos.Y = y;
        sampling.add(pos); //another vote for this position
    }
    
    List<Position> sampling = new ArrayList<>(); //TODO replace duplicates by using counter
    @Override
    public NAR addInput(Task t) {
        AddToSpatialBag(t);
        step_start(); //just input driven for now   
        return nar; //but could as well listen to nar cycle end or even spawn own thread instead
    }
    
    @Override
    public void step_start()
    {
        Position samplePos = sampling.get(0);
        Task current = inputs[samplePos.Y][samplePos.X];
        int k=0;
        for(int i=1;i<sampling.size();i++) {
            Position samplePos2 = sampling.get(i);
            Task prem2 = inputs[samplePos2.Y][samplePos2.X];
            List<Task> seq = proceedWithTemporalInduction(current.sentence, prem2.sentence, prem2, 
                                                              new DerivationContext(nar.memory), true, false, true);
            if(seq != null) {
                for(Task t : seq) {
                    if(!t.sentence.isEternal()) { //TODO improve API, this check should not be necessary
                        current = t;
                        break;
                    }
                }
            }
            k++;
        }
        System.out.println(k);
        System.out.println(current);
        this.results.add(current);//feeds results into "upper" sensory channels:
        this.step_finished(); 
    }
    
}
