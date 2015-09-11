/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.op.mental;

import com.gs.collections.impl.list.mutable.primitive.LongArrayList;
import nars.Events;
import nars.Global;
import nars.NAR;
import nars.concept.Concept;
import nars.event.NARReaction;
import nars.nal.UtilityFunctions;
import nars.nal.nal5.Conjunction;
import nars.nal.nal7.AbstractInterval;
import nars.nal.nal7.CyclesInterval;
import nars.nal.nal7.Sequence;
import nars.process.NAL;
import nars.task.Task;
import nars.task.stamp.Stamp;
import nars.term.Term;
import nars.truth.Truth;
import nars.truth.TruthFunctions;
import nars.util.event.DefaultObserved;

import java.util.Arrays;
import java.util.List;

import static nars.nal.nal7.TemporalRules.ORDER_CONCURRENT;
import static nars.nal.nal7.TemporalRules.ORDER_FORWARD;

/**
 * @author tc
 */
public class PerceptionAccel extends NARReaction {

    public final static int PERCEPTION_DECISION_ACCEL_SAMPLES = 1; //new inference rule accelerating decision making: https://groups.google.com/forum/#!topic/open-nars/B8veE-WDd8Q
    public final static int ConjunctionMemorySize = 100;
    private final DefaultObserved.DefaultObservableRegistration onConceptActive;
    private final DefaultObserved.DefaultObservableRegistration onConceptForget;

    //mostly only makes sense if perception plugin is loaded
    //keep track of how many conjunctions with related amount of component terms there are:


    int[] sv = new int[ConjunctionMemorySize]; //use static array, should suffice for now
    float partConceptsPrioThreshold = 0.1f;
    final List<Task> eventbuffer = Global.newArrayList();
    int cur_maxlen = 1;
    final LongArrayList evBase = new LongArrayList();


    public PerceptionAccel(NAR n) {
        super(n, Events.InduceSucceedingEvent.class);

        this.onConceptActive = n.memory.eventConceptActive.on(c -> {
            Term t = c.getTerm();
            if (t instanceof Sequence)
                handleConjunctionSequence((Sequence)t, true);
        });
        this.onConceptForget = n.memory.eventConceptForget.on(c -> {
            Term t = c.getTerm();
            if (t instanceof Sequence)
                handleConjunctionSequence((Sequence)t, false);
        });
    }

    @Override
    public Class[] getEvents() {
        return super.getEvents();
    }

    @Override
    public void event(Class event, Object[] args) {
        if (event == Events.InduceSucceedingEvent.class) { //todo misleading event name, it is for a new incoming event
            Task newEvent = (Task) args[0];
            if (/*newEvent.isInput() &&*/ newEvent.isJudgment() && !newEvent.isEternal()) {

                NAL nal = (NAL) args[1];

                while (eventbuffer.size() + 1 > cur_maxlen + 1) {
                    eventbuffer.remove(0);
                }
                eventbuffer.add(newEvent);

                perceive(newEvent, nal);
            }
        }
    }


    public void setPartConceptsPrioThreshold(float value) {
        partConceptsPrioThreshold = value;
    }

    public double getPartConceptsPrioThreshold() {
        return partConceptsPrioThreshold;
    }


    public void perceive(final Task task, NAL nal) { //implement Peis idea here now
        //we start with length 2 compounds, and search for patterns which are one longer than the longest observed one

        boolean longest_result_derived_already = false;


        for (int Len = cur_maxlen + 1; Len >= 2; Len--) {
            //ok, this is the length we have to collect, measured from the end of event buffer
            Term[] relterms = new Term[2 * Len - 1]; //there is a interval term for every event
            //measuring its distance to the next event, but for the last event this is obsolete
            //thus it are 2*Len-1] terms

            Task newEvent = eventbuffer.get(eventbuffer.size() - 1);
            Truth truth = newEvent.getTruth();

            evBase.clear();


            int k = 0;
            for (int i = 0; i < Len; i++) {
                int j = eventbuffer.size() - 1 - (Len - 1) + i; //we go till to the end of the event buffer
                if (j < 0) { //event buffer is not filled up enough to support this one, happens at the beginning where event buffer has no elements
                    //but the mechanism already looks for length 2 patterns on the occurence of the first event
                    break;
                }
                Task current = eventbuffer.get(j);
                evBase.addAll(current.getEvidence());

                relterms[k++] = current.getTerm();
                if (i != Len - 1) { //if its not the last one, then there is a next one for which we have to put an interval
                    truth = TruthFunctions.deduction(truth, current.getTruth());
                    Task next = eventbuffer.get(j + 1);

                    long dt = next.getOccurrenceTime() - current.getOccurrenceTime();
                    relterms[k++] = nal.newInterval(dt);
                }
            }


            boolean eventBufferDidNotHaveSoMuchEvents = false;
            for (int i = 0; i < relterms.length; i++) {
                if (relterms[i] == null) {
                    eventBufferDidNotHaveSoMuchEvents = true;
                }
            }
            if (eventBufferDidNotHaveSoMuchEvents) {
                continue;
            }

            //if (k < 2) continue;

            relterms = Arrays.copyOf(relterms, k); //remove extra null's from end

            //decide on the tense of &/ by looking if the first event happens parallel with the last one
            //Todo refine in 1.6.3 if we want to allow input of difference occurence time
            boolean after = newEvent.after(eventbuffer.get(eventbuffer.size() - 1 - (Len - 1)), nal.nar.memory().duration.get());

            //critical part: (not checked for correctness yet):
            //we now have to look at if the first half + the second half already exists as concept, before we add it
            Term[] firstHalf;
            Term[] secondHalf;
            if (relterms[Len - 1] instanceof AbstractInterval) {
                //the middle can be a interval, for example in case of a,+1,b , in which case we dont use it

                firstHalf = new Term[Len - 1]; //so we skip the middle here
                secondHalf = new Term[Len - 1]; //as well as here
                int h = 0; //make index mapping easier by counting
                for (int i = 0; i < Len - 1; i++) {
                    firstHalf[i] = relterms[h];
                    h++;
                }
                h += 1; //we have to overjump the middle element this is why
                for (int i = 0; i < Len - 1; i++) {
                    secondHalf[i] = relterms[h];
                    h++;
                }
            } else { //it is a event so its fine
                firstHalf = new Term[Len]; //2*Len-1 in total
                secondHalf = new Term[Len]; //but the middle is also used in the second one
                int h = 0; //make index mapping easier by counting
                for (int i = 0; i < Len; i++) {
                    firstHalf[i] = relterms[h];
                    h++;
                }
                h--; //we have to use the middle twice this is why
                for (int i = 0; i < Len; i++) {
                    secondHalf[i] = relterms[h];
                    h++;
                }
            }
            Term firstC = Conjunction.make(firstHalf, after ? ORDER_FORWARD : ORDER_CONCURRENT);
            Term secondC = Conjunction.make(secondHalf, after ? ORDER_FORWARD : ORDER_CONCURRENT);
            Concept C1 = nal.nar.concept(firstC);
            Concept C2 = nal.nar.concept(secondC);

            if (after && (C1 == null || C2 == null)) { //everything which happens now we can summarize for now on
//                if (debugMechanism) {                  //but only if the atomic events are also existing as concepts
//                    System.out.println("one didn't exist: " + firstC + " or " + secondC); //and are above partConcepts priority threshold
//                }
                continue; //the components were not observed, so don't allow creating this compound
            }



            if (C1 == null || C2 == null || C1.getPriority() < partConceptsPrioThreshold || C2.getPriority() < partConceptsPrioThreshold) {
                continue; //too less priority
            }

            relterms = CyclesInterval.removeZeros(relterms);
            if (relterms.length < 2) continue;

            Term[] relterms2=new Term[relterms.length];
            int u=0;
            for(int i=0; i<relterms.length; i++) {
                if(!(relterms[i] instanceof AbstractInterval)) {
                    //ok it is not an interval, so it has to exist as concept else it needs to be eliminated
                    Concept C=nal.nar.concept(relterms[i]);
                    if(C !=null && C.getPriority() >= partConceptsPrioThreshold) {
                        relterms2[u] = relterms[i];
                        u++;
                    }
                }
                else {
                    relterms2[u] = relterms[i];
                    u++;
                }
            }

            Term[] relterms2_real=new Term[u];
            System.arraycopy(relterms2, 0, relterms2_real, 0, u);

            Term C0 = Conjunction.make(relterms2_real, after ? ORDER_FORWARD : ORDER_CONCURRENT);
            if (!(C0 instanceof Conjunction)) {
                continue;
            }
            Conjunction C = (Conjunction)C0;


//            if (debugMechanism) {
//                nal.emit(Events.DEBUG.class, this, "Success", S);
//            }


            longest_result_derived_already = true;
            //Stamper st = nal.newStamp(task.getSentence(), nal.time(), evBase.toArray());

            //lets make the new event the parent task, and derive it
            Task T = nal.deriveDouble(nal.newTask(C).judgment().truth(truth)
                            .budget(UtilityFunctions.or(C1.getPriority(), C2.getPriority()), Global.DEFAULT_JUDGMENT_DURABILITY)
                            .parent(task, newEvent)
                            .occurrNow()
                            .setEvidence(Stamp.toSetArray(evBase.toArray()))
                            .setTemporalInducting(!longest_result_derived_already));



        }
    }


    public void handleConjunctionSequence(Sequence c, boolean add) {
//
//        if (debugMechanism) {
//            System.out.println(this + " handleConjunctionSequence with " + c.toString() + " " + String.valueOf(add));
//            //nal.emit(Events.DEBUG.class, this, "handleConjunctionSequence", c, add);
//        }

        if (add) { //manage concept counter
            sv[c.length()]++;
        } else {
            sv[c.length()]--;
        }
        //determine cur_maxlen 
        //by finding the first complexity which exists
        cur_maxlen = 1; //minimum size is 1 (the events itself), in which case only chaining of two will happen
        for (int i = sv.length - 1; i >= 2; i--) { //>=2 because a conjunction with size=1 doesnt exist
            if (sv[i] > 0) {
                cur_maxlen = i; //dont using the index 0 in sv makes it easier here
                break;
            }
        }
//
//        if (debugMechanism) {
//            System.out.println("determined max len is " + String.valueOf(cur_maxlen));
//        }
    }

}
