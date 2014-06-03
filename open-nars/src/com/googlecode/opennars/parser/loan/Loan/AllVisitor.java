package com.googlecode.opennars.parser.loan.Loan;

import com.googlecode.opennars.parser.loan.Loan.Absyn.*;

/** BNFC-Generated All Visitor */
public interface AllVisitor<R,A> extends
  com.googlecode.opennars.parser.loan.Loan.Absyn.Document.Visitor<R,A>,
  com.googlecode.opennars.parser.loan.Loan.Absyn.BaseRule.Visitor<R,A>,
  com.googlecode.opennars.parser.loan.Loan.Absyn.Sentence.Visitor<R,A>,
  com.googlecode.opennars.parser.loan.Loan.Absyn.Budget.Visitor<R,A>,
  com.googlecode.opennars.parser.loan.Loan.Absyn.Stm.Visitor<R,A>,
  com.googlecode.opennars.parser.loan.Loan.Absyn.Term.Visitor<R,A>,
  com.googlecode.opennars.parser.loan.Loan.Absyn.URIRef.Visitor<R,A>,
  com.googlecode.opennars.parser.loan.Loan.Absyn.Literal.Visitor<R,A>,
  com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix.Visitor<R,A>,
  com.googlecode.opennars.parser.loan.Loan.Absyn.TruthValue.Visitor<R,A>
{}
