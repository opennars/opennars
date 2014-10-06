package nars.core.build;

/**
 *
 * @author me
 */
//public class RealTimeNARBuilder extends DefaultNARBuilder {
//    private final boolean randomRemoval;
//
//    public RealTimeNARBuilder() {
//        this(true);
//    }
//    public RealTimeNARBuilder(boolean randomRemoval) {
//        super();
//        this.randomRemoval = randomRemoval;
//    }
//
//    @Override
//    public AbstractBag<Task> newNovelTaskBag(Param p) {
//        return new ContinuousBag<>(getTaskBufferSize(), p.newTaskForgetDurations, randomRemoval);
//    }
//
//    @Override
//    public AbstractBag<Concept> newConceptBag(Param p) {
//        return new ContinuousBag<>(getConceptBagSize(), p.conceptForgetDurations, randomRemoval);
//    }
//    
//    @Override
//    public Concept newConcept(final Term t, final Memory m) {        
//        //NOT USED, remove this abstract method because it doesnt apply to all types
//        return null; 
//    }
//
//    @Override
//    public ConceptProcessor newConceptProcessor(Param p, ConceptBuilder c) {
//        return new RealTimeFloodCycle();
//    }
//    
//    
//}