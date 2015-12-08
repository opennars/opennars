///*
// * SetInt.java
// *
// * Copyright (C) 2008  Pei Wang
// *
// * This file is part of Open-NARS.
// *
// * Open-NARS is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 2 of the License, or
// * (at your option) any later version.
// *
// * Open-NARS is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
// */
//package nars.nal.nal3;
//
//import nars.Op;
//import nars.Symbols;
//import nars.term.Term;
//import nars.term.compound.Compound;
//
//import java.io.IOException;
//
///**
// * An intensionally defined set, which contains one or more instances defining the Term.
// */
//public class SetIntN<T extends Term> extends AbstractSetN<T> implements SetInt<T> {
//
//
//    /**
//     * Constructor with partial values, called by make
//     * @param arg The component list of the term - args must be unique and sorted
//     */
//    protected SetIntN(final T... arg) {
//        super(arg);
//    }
//
//
//    @Override
//    public final Op op() {
//        return Op.SET_INT;
//    }
//
//    @Override
//    public final T[] terms() {
//        return terms.term;
//    }
//
//
//    /**
//     * Clone a SetInt
//     * @return A new object, to be casted into a SetInt
//     */
//    @Override
//    public Compound clone() {
//        return SetInt.make(terms());
//    }
//
//    @Override public Compound clone(Term[] replaced) {
//        return SetInt.make(replaced);
//    }
//
//    @Override
//    public boolean appendOperator(Appendable p) throws IOException {
//        super.appendOperator(p);
//        return false;
//    }
//
//    @Override
//    public void appendCloser(Appendable p) throws IOException {
//        p.append(Symbols.SET_INT_CLOSER);
//    }
//
//    @Override
//    public boolean appendTermOpener() {
//        return false;
//    }
//}
//
