///*
// * Copyright (C) 2014 peiwang
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
//package nars.op.mental;
//
//import nars.concept.Concept;
//import nars.nal.nal8.operator.SyncOperator;
//import nars.task.Task;
//import nars.term.Term;
//
//import java.util.List;
//
///**
// * Operator that activates a concept
// */
//public class hesitate extends SyncOperator implements Mental {
//
//    /**
//     * To activate a concept as if a question has been asked about it
//     *
//     * @param args Arguments, a Statement followed by an optional tense
//     * @return Immediate results as Tasks
//     */
//    @Override
//    public List<Task> apply(Task operation) {
//        Term term = operation.getTerm().arg(0);
//        Concept concept = nar.conceptualize(term, operation.getBudget());
//        concept.discountGoalConfidence();
//        return null;
//    }
//
//}
