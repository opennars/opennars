package nars.util.meter.condition;

import nars.NAR;
import nars.Narsese;
import nars.task.Task;

/**
 * Created by me on 10/14/15.
 */
public class TemporalTaskCondition extends EternalTaskCondition {

    /**
     * occurrence time (absolute) valid range
     */
    public final long occStart, occEnd;


    public TemporalTaskCondition(NAR n, long cycleStart, long cycleEnd,
                                 long occStart, long occEnd,
                                 String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax) throws Narsese.NarseseException {
        super(n, cycleStart, cycleEnd, sentenceTerm, punc, freqMin, freqMax, confMin, confMax);
        this.occStart = occStart;
        this.occEnd = occEnd;
    }

    @Override
    public String toString() {
        return super.toString() + " occurrs: (" + occStart + ',' + occEnd +
                ')';
    }

    @Override
    protected boolean occurrenceTimeMatches(Task task) {
        if (task.isEternal()) return false;

        //final long cc = task.getCreationTime();
        long oc = task.getOccurrenceTime();

        return (oc >= occStart) && (oc <= occEnd);
//
////                    long at = relativeToCondition ? getCreationTime() : task.getCreationTime();
//        final boolean tmatch = false;
////                    switch () {
////                        //TODO write time matching
//////                        case Past: tmatch = oc <= (-durationWindowNear + at); break;
//////                        case Present: tmatch = oc >= (-durationWindowFar + at) && (oc <= +durationWindowFar + at); break;
//////                        case Future: tmatch = oc > (+durationWindowNear + at); break;
////                        default:
//        throw new RuntimeException("Invalid tense for non-eternal TaskCondition: " + this);
////                    }
////                    if (!tmatch) {
////                        //beyond tense boundaries
////                        //distance += rangeError(oc, -halfDur, halfDur, true) * tenseCost;
////                        distance += 1; //tenseCost + rangeError(oc, creationTime, creationTime, true); //error distance proportional to occurence time distance
////                        match = false;
////                    }
////                    else {
////                        //System.out.println("matched time");
////                    }
//
//
//        //return true;
    }
}
