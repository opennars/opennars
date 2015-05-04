/*
 * Sample.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.operate.io;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import nars.Events;
import nars.Memory;
import nars.NAR;
import nars.event.AbstractReaction;
import nars.nal.Task;
import nars.nal.nal1.Inheritance;
import nars.nal.nal3.SetExt;
import nars.nal.nal4.Product;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.nal.term.Atom;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import org.apache.commons.math3.stat.Frequency;

import java.util.*;

/**
 */
public class Say extends Operator {
    private Memory memory;
    private Operation currentOperation;
    private AbstractReaction reaction;

//    boolean rejectEmpty = true;
//    boolean rejectHasVariables = true;    
    
    public Say() {
        super("^say");
    }

    final int MAX_WORD_LENGTH = 16;
    final Term WORD = get("WORD");
    final Term QUIET = get("QUIET");
    final Term INCOHERENT = get("INCOHERENT");
    final Term SAID = get("SAID");
//NOISY

    long lastEmit = 0;
    long emitPeriod = 50; //cycles between which buffer is auto-flush

    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        if (!enabled) {
            if (reaction!=null) {
                reaction.off();
                reaction = null;
            }
        }

        return super.setEnabled(n, enabled);
    }

    @Override
    protected synchronized List<Task> execute(Operation operation, Term[] args) {

        this.memory = memory;
        this.currentOperation = operation;

        if (this.reaction == null) {
            reaction = new AbstractReaction(memory, Events.CycleEnd.class) {

                @Override
                public void event(Class event, Object[] args) {
                    long now = nar.time();
                    if (buffer.getUniqueCount() > 0 && now - lastEmit > emitPeriod) {
                        nar.input("say()!");
                        lastEmit = now;
                    }
                }
            };
        }
//        if (rejectEmpty && args.length == 1) {
//            //SELF argument by itself is not worth speaking
//            throw NegativeFeedback.ignore("Said nothing");
//        }
//                
//        if (rejectEmpty && Terms.containsVariables(args)) {
//            throw NegativeFeedback.ignore("Said variables");
//        }
                
//        List<Term> spoken = Lists.newArrayList(args).subList(0, args.length-1);
//        List<Term> spoke2=new ArrayList<Term>();
//        for(Term t: spoken) {
//            if(t instanceof Product) {
//                CompoundTerm cn=(CompoundTerm) t;
//                for(Term k : cn) {
//                    String s=k.toString();
//                    if(s.startsWith("word-")) {
//                        spoke2.add(new Term(s.replace("word-", "")));
//                    } else {
//                        spoke2.add(k);
//                    }
//                }
//
//            } else {
//                return null;
//            }
//        }

        int argsLength = args.length - 1; //ignore ending SELF

        if (argsLength == 1) {
            Term wIn = args[0];
            Term w = asWord(wIn);
            if (w!=null) {
                buffer(w, operation.getTask().getPriority());
                return isWord(w, true);
            }
            else {
                return isWord(wIn, false);
            }
        }
        else if (argsLength == 0) {
            return flush();
            //measure coherence, silence, etc
        }
        else {
            List<Term> aa = Arrays.asList(args).subList(0, args.length - 1);
            return isWord(SetExt.make(aa), false);
        }

//        List<Term> spoke2 = Flat.collect(args, new ArrayList());
//        if (spoke2.size() > 0)
//            memory.emit(Say.class, spoke2);
//
//        return null;
    }



    private List<Task> isWord(Term t, boolean b) {
        return Arrays.asList(memory.newTask(Inheritance.make(t, WORD)).judgment().
                truth(b, 0.9f).present().get());
    }

    private Compound asWord(Term t) {
//        if (t.operator() == NALOperator.SET_EXT && ((SetExt)t).size() == 1) {
//            t = ((SetExt)t).term[0];
//        }

        if (t instanceof Atom) {
            String s = t.toString();
            if (s.startsWith("\"") && s.endsWith("\"") && (s.length()-2) < MAX_WORD_LENGTH) {
                return SetExt.make(t);
            }
        }

        return null;
    }

    private List<Task> flush() {
        lastEmit = memory.time();



        int num = buffer.getUniqueCount();
        double thresh = 1.0 / num;
        Iterator<Map.Entry<Comparable<?>, Long>> eie = buffer.entrySetIterator();
        if (eie.hasNext()) {
            Map.Entry<Comparable<?>, Long> ei = eie.next();
            Term t = (Term) ei.getKey();
            double p = buffer.getPct(t);
            if (num >1 && p <= thresh) {
                //no clear winner

                List<Object> ic = new ArrayList(num);
                Iterators.addAll(ic, buffer.valuesIterator());

                buffer.clear();
                return isIncoherent(ic);
            }
            else {
                //the clear winner
                buffer.clear();
                return isSpoken(t);
            }

        }

        return isQuiet();
    }

    private List<Task> isQuiet() {
        return Arrays.asList( memory.newTask(
                Inheritance.make(memory.getSelf(), QUIET)
        ).judgment().present().truth(1.0f, 0.9f).get());
    }

    private List<Task> isSpoken(Term t) {
        memory.emit(Say.class, t);
        return Arrays.asList( memory.newTask(
                Inheritance.make(
                        Product.make(t, memory.getSelf()),
                        SAID)
        ).judgment().present().truth(1.0f, 0.9f).get());
    }

    private List<Task> isIncoherent(List<Object> ic) {
        List<Term> c = Lists.transform(ic, new Function<Object, Term>() {
            @Override public Term apply(Object input) {
                return Inheritance.make( (Term)input, WORD ) ;
            }
        });
        return Arrays.asList( memory.newTask(
                Inheritance.make(
                        Product.make(SetExt.make(c), memory.getSelf()),
                        INCOHERENT)
        ).judgment().present().truth(1.0f, 0.9f).get());

    }

    final Frequency buffer = new Frequency();


    void buffer(Term word, float priority) {
        buffer.incrementValue(word, (long)(priority*1000));
    }

}
