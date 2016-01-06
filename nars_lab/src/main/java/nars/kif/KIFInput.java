///*
// * Copyright (C) 2014 me
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//package nars.kif;
//
//import nars.NAR;
//import nars.io.in.PrintWriterInput;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
///**
// * http://sigmakee.cvs.sourceforge.net/viewvc/sigmakee/sigma/suo-kif.pdf
// * http://sigma-01.cim3.net:8080/sigma/Browse.jsp?kb=SUMO&lang=EnglishLanguage&flang=SUO-KIF&term=subclass
// *
// * @author me
// */
//public class KIFInput extends PrintWriterInput implements Runnable {
//
//    private final KIF kif;
//    private final Iterator<Formula> formulaIterator;
//    boolean closed = false;
//    private boolean includeSubclass;
//    private boolean includeInstance;
//    private boolean includeRelatedInternalConcept;
//    private boolean includeDisjoint;
//    private boolean includeSubrelation;
//
//    public KIFInput(NAR nar, String kifPath) throws Exception {
//        super(nar);
//
//        kif = new KIF(kifPath);
//        formulaIterator = kif.getFormulas().iterator();
//
//    }
//
//    public void setIncludeDisjoint(boolean includeDisjoint) {
//        this.includeDisjoint = includeDisjoint;
//    }
//
//    public void setIncludeRelatedInternalConcept(boolean includeRelatedInternalConcept) {
//        this.includeRelatedInternalConcept = includeRelatedInternalConcept;
//    }
//
//    public void setIncludeInstance(boolean includeInstance) {
//        this.includeInstance = includeInstance;
//    }
//
//    public void setIncludeSubclass(boolean includeSubclass) {
//        this.includeSubclass = includeSubclass;
//    }
//
//    public void setIncludeSubrelation(boolean includeSubrelation) {
//        this.includeSubrelation = includeSubrelation;
//
//    }
//
//    public void start() {
//        new Thread(this).start();
//    }
//
//    Map<String, Integer> knownOperators = new HashMap();
//    Map<String, Integer> unknownOperators = new HashMap();
//
//    protected void emit(final String statement) {
//        try {
//            out.write(statement);
//            out.write('\n');
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    @Override
//    public void run() {
//        try {
//            while (true) {
//                if (!formulaIterator.hasNext()) {
//                    break;
//                }
//
//                Formula f = formulaIterator.next();
//                if (f == null) {
//                    break;
//                }
//
//                String root = f.car(); //root operate
//
//                List<String> a = f.argumentsToArrayList(1);
//
///**
// *
// *
// * https://github.com/opencog/opencog/blob/04db8e557a2d67da9025fe455095d2cda0261ea7/opencog/python/sumo/sumo.py
// * def special_link_type(predicate):
//mapping = {
//'=>':types.ImplicationLink,
//'<=>':types.EquivalenceLink,
//'and':types.AndLink,
//'or':types.OrLink,
//'not':types.NotLink,
//'instance':types.MemberLink,
//# This might break some of the formal precision of SUMO, but who cares
//'attribute':types.InheritanceLink,
//'member':types.MemberLink,
//'subclass':types.InheritanceLink,
//'exists':types.ExistsLink,
//'forall':types.ForAllLink,
//'causes':types.PredictiveImplicationLink
//*
//                 */
//
//                switch (root) {
//                    case "subclass":
//                        if (includeSubclass) {
//                            if (a.size() != 2) {
//                                System.err.println("subclass expects 2 arguments");
//                            } else {
//                                emit("<" + a.get(0) + " --> " + a.get(1) + ">.");
//                            }
//                        }
//                        break;
//                    case "instance":
//                        if (includeInstance) {
//                            if (a.size() != 2) {
//                                System.err.println("instance expects 2 arguments");
//                            } else {
//                                emit("<" + a.get(0) + " {-- " + a.get(1) + ">.");
//                            }
//                        }
//                        break;
//                    case "relatedInternalConcept":
//                        /*(documentation relatedInternalConcept EnglishLanguage "Means that the two arguments are related concepts within the SUMO, i.e. there is a significant similarity of meaning between them. To indicate a meaning relation between a SUMO concept and a concept from another source, use the Predicate relatedExternalConcept.")            */
//                        if (includeRelatedInternalConcept) {
//                            if (a.size() != 2) {
//                                System.err.println("relatedInternalConcept expects 2 arguments");
//                            } else {
//                                emit("<" + a.get(0) + " <-> " + a.get(1) + ">.");
//                            }
//                        }
//                        break;
//                    case "disjoint":
//                        //"(||," <term> {","<term>} ")"      // disjunction
//                        if (includeDisjoint) {
//                            emit("<(||," + a.get(0) + "," + a.get(1) + ")>.");
//                        }
//                        break;
//                    case "disjointRelation":
//                        if (includeDisjoint) {
//                            emit("<(||," + a.get(0) + "," + a.get(1) + ")>.");
//                        }
//                        break;
//                    case "subrelation":
//                        //for now, use similarity+inheritance but more clear expression is possible
//                        if (includeSubrelation) {
//                            //emit("<" + a.get(0) + " <-> " + a.get(1) + ">.");
//                            emit("<" + a.get(0) + " --> " + a.get(1) + ">.");
//                        }
//                        break;
//                    default:
//                        /*System.out.println("??" + f);
//                         System.out.println();*/
//                        if (unknownOperators.containsKey(root)) {
//                            unknownOperators.put(root, unknownOperators.get(root) + 1);
//                        } else {
//                            unknownOperators.put(root, 1);
//                        }
//                        break;
//                }
//
//                if (knownOperators.containsKey(root)) {
//                    knownOperators.put(root, knownOperators.get(root) + 1);
//                } else {
//                    knownOperators.put(root, 1);
//                }
//
//                //  => Implies
//                //  <=> Equivalance
//                /*Unknown operators: {=>=466, rangeSubclass=5, inverse=1, relatedInternalConcept=7, documentation=128, range=29, exhaustiveAttribute=1, trichotomizingOn=4, subrelation=22, not=2, partition=12, contraryAttribute=1, subAttribute=2, disjoint=5, domain=102, disjointDecomposition=2, domainSubclass=9, <=>=70}*/
//            }
//            out.flush();
//            out.close();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//
//    }
//
//    //TODO handle finish(true) and interrupt/kill the thread
//    public Map<String, Integer> getKnownOperators() {
//        return knownOperators;
//    }
//
//    public Map<String, Integer> getUnknownOperators() {
//        return unknownOperators;
//    }
//
// }
