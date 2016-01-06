//package nars.concept.util;
//
//import nars.concept.Concept;
//import nars.term.Term;
//import org.infinispan.commons.marshall.Externalizer;
//
//import java.io.IOException;
//import java.io.ObjectInput;
//import java.io.ObjectOutput;
//
///**
// * Created by me on 10/12/15.
// */
//public class ConceptExternalizer implements Externalizer<Concept> {
//
//    private final ConceptBuilder builder;
//
//    public ConceptExternalizer(ConceptBuilder cb) {
//        this.builder = cb;
//    }
//
//    @Override
//    public void writeObject(ObjectOutput output, Concept c) throws IOException {
//
//        output.writeObject(c.getTerm());
//
//        c.getTermLinks().writeValues(output);
//        c.getTaskLinks().writeValues(output);
//
//        //output.writeObject(c.getMeta());
//        output.writeLong(c.getCreationTime());
//
//
//        c.getBeliefs().writeValues(output);
//        c.getGoals().writeValues(output);
//        c.getQuests().writeValues(output);
//        c.getQuestions().writeValues(output);
//
//
//    }
//
//    @Override
//    public Concept readObject(ObjectInput input) throws IOException, ClassNotFoundException {
//
//        Term term = (Term) input.readObject();
//
//        Concept c = builder.apply(term);
//
//        c.getTermLinks().readValues(input);
//        c.getTaskLinks().readValues(input);
//
//        //Map meta = (Map) input.readObject();
//        // TODO apply meta
//        c.setCreationTime(input.readLong());
//
//
//        c.getBeliefs().readValues(input);
//        c.getGoals().readValues(input);
//        c.getQuests().readValues(input);
//        c.getQuestions().readValues(input);
//
//
//        return c;
//    }
// }
