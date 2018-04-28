package nars.lab.vision;

import java.util.ArrayList;
import java.util.List;
import nars.main.NAR;
import nars.control.DerivationContext;
import static nars.control.TemporalInferenceControl.proceedWithTemporalInduction;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.plugin.perception.SensoryChannel;
import nars.language.Term;
import nars.storage.LevelBag;

public class SpatialSamplingVisionChannel extends SensoryChannel {
    public class Position {
        public int X;
        public int Y;
    }
    
    LevelBag<Task<Term>,Sentence<Term>>[][] spatialbag;
    public SpatialSamplingVisionChannel(NAR nar, SensoryChannel reportResultsTo, int width, int height) {
        super(nar,reportResultsTo, width, height, width*height);
        spatialbag = new LevelBag[height][width];
    }
    
    public void AddToSpatialBag(Task t) {
        int x = t.getTerm().term_indices[2];
        int y = t.getTerm().term_indices[3];
        if(spatialbag[y][x] == null) {
            spatialbag[y][x] = new LevelBag(100, 100);
        }
        t.incPriority((float) this.topDownPriority(t.getTerm()));
        spatialbag[y][x].putIn(t);
        Position pos = new Position();
        pos.X = x;
        pos.Y = y;
        sampling.add(pos); //another vote for this position
    }
    
    List<Position> sampling = new ArrayList<>(); //TODO replace duplicates by using counter
    @Override
    public NAR addInput(Task t) {
        int[] test = t.getTerm().term_indices;
        AddToSpatialBag(t);
        for(int i=0;i<100000;i++) {
            step_start(); //just input driven for now   
        }
        return nar; //but could as well listen to nar cycle end or even spawn own thread instead
    }
    
    @Override
    public void step_start()
    {
        int ind = nar.memory.randomNumber.nextInt(sampling.size());
        Position samplePos = sampling.get(ind);
        Task sampled = spatialbag[samplePos.Y][samplePos.X].takeNext();
        //Todo improve API, channel should not need to know where in the array x and y size is
        
        //spatial biased random sampling: 
        int ind2 = nar.memory.randomNumber.nextInt(sampling.size());
        int s2posY = sampling.get(ind2).Y;
        int s2posX = sampling.get(ind2).X;
        if(spatialbag[s2posY][s2posX] != null) {
            Task sampled2 = spatialbag[s2posY][s2posX].takeNext();
            if(sampled2 != null) {
                //improve API, carrying out temporal inference should be easier..
                List<Task> seq = proceedWithTemporalInduction(sampled.sentence, sampled2.sentence, sampled2, 
                                                              new DerivationContext(nar.memory), true, false, true);
                if(seq != null) {
                    for(Task t : seq) {
                        if(!t.sentence.isEternal()) { //TODO improve API, this check should not be necessary
                            AddToSpatialBag(t);
                            this.results.add(t);
                        }
                    }
                }
                //todo improve API, putting an element bag should be easy
                spatialbag[s2posY][s2posX].putBack(sampled2, nar.memory.cycles(nar.memory.param.conceptForgetDurations), nar.memory);
            }
        }
        spatialbag[samplePos.Y][samplePos.X].putBack(sampled, nar.memory.cycles(nar.memory.param.conceptForgetDurations), nar.memory);
        //feeds results into "upper" sensory channels:
        this.step_finished(); 
    }
    
}
