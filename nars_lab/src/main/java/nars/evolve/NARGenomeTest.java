///*
// * Here comes the text of your license
// * Each line should be prefixed with  *
// */
//package nars.evolve;
//
//import org.encog.Encog;
//import org.encog.ml.ea.train.basic.TrainEA;
//import org.encog.ml.prg.EncogProgram;
//import org.encog.ml.prg.EncogProgramContext;
//import org.encog.ml.prg.PrgCODEC;
//import org.encog.ml.prg.extension.StandardExtensions;
//import org.encog.ml.prg.generator.RampedHalfAndHalf;
//import org.encog.ml.prg.opp.ConstMutation;
//import org.encog.ml.prg.opp.SubtreeCrossover;
//import org.encog.ml.prg.opp.SubtreeMutation;
//import org.encog.ml.prg.species.PrgSpeciation;
//import org.encog.ml.prg.train.PrgPopulation;
//import org.encog.ml.prg.train.rewrite.RewriteAlgebraic;
//import org.encog.ml.prg.train.rewrite.RewriteConstants;
//
//import java.util.Random;
//
///**
// *
// * @author me
// */
//public class NARGenomeTest {
//
//    public static class NARGenomeContext extends EncogProgramContext {
//
//        public NARGenomeContext() {
//            super();
//            //loadAllFunctions();
//
//
////            System.out.println(context);
////            System.out.println(context.getDefinedVariables());
////            //System.out.println(context.getFunctions().getOpCodes());
////            System.out.println(context.getResult());
//
//        }
//
//
//    }
//    public static class NARGenome {
//
//
//
//        public NARGenome() {
//        }
//
//    }
//
//    /*@Test*/ public void testGenome() {
//        new NARGenome();
//
//    }
//
//
//	public static void main(String[] args) {
//
//		EncogProgramContext context = new NARGenomeContext();
//
//		StandardExtensions.createNumericOperators(context);
//
//		PrgPopulation pop = new PrgPopulation(context,1000);
//
//                int maxCycles = 64;
//		TrainEA genetic = new TrainEA(pop, new GeneticSearchEncog.CalculateNALTestScore(maxCycles));
//		//genetic.setValidationMode(true);
//		genetic.setCODEC(new PrgCODEC());
//		genetic.addOperation(0.5, new SubtreeCrossover());
//		genetic.addOperation(0.25, new ConstMutation(context,0.5,1.0));
//		genetic.addOperation(0.25, new SubtreeMutation(context,4));
//		//genetic.addScoreAdjuster(new ComplexityAdjustedScore(10,20,10,20.0));
//		genetic.getRules().addRewriteRule(new RewriteConstants());
//		genetic.getRules().addRewriteRule(new RewriteAlgebraic());
//		genetic.setSpeciation(new PrgSpeciation());
//
//		(new RampedHalfAndHalf(context,1, 3)).generate(new Random(), pop);
//
//		genetic.setShouldIgnoreExceptions(false);
//
//		EncogProgram best = null;
//
//		try {
//
//			for (int i = 0; i < 1000; i++) {
//				genetic.iteration();
//				best = (EncogProgram) genetic.getBestGenome();
//				System.out.println(genetic.getIteration() + ", Error: "
//						+ best.getScore() + ",Best Genome Size:" +best.size()
//						+ ",Species Count:" + pop.getSpecies().size() + ",best: " + best.dumpAsCommonExpression());
//			}
//
//			//EncogUtility.evaluate(best, trainingData);
//
//			System.out.println("Final score:" + best.getScore()
//					+ ", effective score:" + best.getAdjustedScore());
//			System.out.println(best.dumpAsCommonExpression());
//			//pop.dumpMembers(Integer.MAX_VALUE);
//			//pop.dumpMembers(10);
//
//		} catch (Throwable t) {
//			t.printStackTrace();
//		} finally {
//			genetic.finishTraining();
//			Encog.getInstance().shutdown();
//		}
//	}
// }
