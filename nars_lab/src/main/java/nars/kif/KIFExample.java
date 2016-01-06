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
//
//package nars.kif;
//
//import nars.NAR;
//import nars.nar.Default;
//
///**
// *
// * @author me
// */
//public class KIFExample {
//
//
//    public static void main(String[] args) throws Exception {
//
//
//        NAR n = new Default();
//
//
//
//
//        KIFInput k = new KIFInput(n, "/home/me/sigma/KBs/Merge.kif");
//        k.setIncludeSubclass(true);
//        k.setIncludeInstance(true);
//        k.setIncludeSubrelation(true);
//        k.setIncludeRelatedInternalConcept(true);
//        //k.setIncludeDisjoint(true);
//
//
//        //start before adding input to begin filling buffer
//        k.start();
//        n.input(k);
//
//        n.frame(1);
//
//        /*
//        TextOutput t = new TextOutput(n, System.out);
//        t.setErrors(true);
//        t.setErrorStackTrace(true);
//        */
//
//
//        //t.stop();
//
//        //new NARSwing(n);
//
//
//        /*
//        new TextInput(n, "$0.99;0.99$ <Human --> ?x>?");
//        new TextInput(n, "$0.99;0.99$ <Human--> {?x}>?");
//        new TextInput(n, "$0.99;0.99$ <?x --> Human>?");*/
//
//
//    }
// }
