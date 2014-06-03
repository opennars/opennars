package com.googlecode.opennars.parser.loan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.googlecode.opennars.entity.Base;
import com.googlecode.opennars.entity.BudgetValue;
import com.googlecode.opennars.entity.Sentence;
import com.googlecode.opennars.entity.Task;
import com.googlecode.opennars.entity.TruthValue;
import com.googlecode.opennars.language.BooleanLiteral;
import com.googlecode.opennars.language.CompoundTerm;
import com.googlecode.opennars.language.Conjunction;
import com.googlecode.opennars.language.ConjunctionParallel;
import com.googlecode.opennars.language.ConjunctionSequence;
import com.googlecode.opennars.language.DifferenceExt;
import com.googlecode.opennars.language.DifferenceInt;
import com.googlecode.opennars.language.Disjunction;
import com.googlecode.opennars.language.Equivalence;
import com.googlecode.opennars.language.EquivalenceAfter;
import com.googlecode.opennars.language.EquivalenceWhen;
import com.googlecode.opennars.language.ImageExt;
import com.googlecode.opennars.language.ImageInt;
import com.googlecode.opennars.language.Implication;
import com.googlecode.opennars.language.ImplicationAfter;
import com.googlecode.opennars.language.ImplicationBefore;
import com.googlecode.opennars.language.ImplicationWhen;
import com.googlecode.opennars.language.Inheritance;
import com.googlecode.opennars.language.Instance;
import com.googlecode.opennars.language.InstanceProperty;
import com.googlecode.opennars.language.IntersectionExt;
import com.googlecode.opennars.language.IntersectionInt;
import com.googlecode.opennars.language.Literal;
import com.googlecode.opennars.language.Negation;
import com.googlecode.opennars.language.NumericLiteral;
import com.googlecode.opennars.language.Product;
import com.googlecode.opennars.language.Property;
import com.googlecode.opennars.language.SetExt;
import com.googlecode.opennars.language.SetInt;
import com.googlecode.opennars.language.Similarity;
import com.googlecode.opennars.language.Statement;
import com.googlecode.opennars.language.StringLiteral;
import com.googlecode.opennars.language.TenseFuture;
import com.googlecode.opennars.language.TensePast;
import com.googlecode.opennars.language.TensePresent;
import com.googlecode.opennars.language.Term;
import com.googlecode.opennars.language.URIRef;
import com.googlecode.opennars.language.Variable;
import com.googlecode.opennars.main.Memory;
import com.googlecode.opennars.parser.InvalidInputException;
import com.googlecode.opennars.parser.Parser;
import com.googlecode.opennars.parser.Symbols;
import com.googlecode.opennars.parser.TermVisitor;
import com.googlecode.opennars.parser.loan.Loan.AbstractVisitor;
import com.googlecode.opennars.parser.loan.Loan.PrettyPrinter;
import com.googlecode.opennars.parser.loan.Loan.Yylex;
import com.googlecode.opennars.parser.loan.Loan.parser;
import com.googlecode.opennars.parser.loan.Loan.Absyn.BaseR;
import com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetE;
import com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetP;
import com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetPD;
import com.googlecode.opennars.parser.loan.Loan.Absyn.Doc;
import com.googlecode.opennars.parser.loan.Loan.Absyn.DocBR;
import com.googlecode.opennars.parser.loan.Loan.Absyn.Document;
import com.googlecode.opennars.parser.loan.Loan.Absyn.ListIdent;
import com.googlecode.opennars.parser.loan.Loan.Absyn.ListTerm;
import com.googlecode.opennars.parser.loan.Loan.Absyn.LitDbl;
import com.googlecode.opennars.parser.loan.Loan.Absyn.LitFalse;
import com.googlecode.opennars.parser.loan.Loan.Absyn.LitInt;
import com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVar;
import com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVarAn;
import com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarD;
import com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarI;
import com.googlecode.opennars.parser.loan.Loan.Absyn.LitString;
import com.googlecode.opennars.parser.loan.Loan.Absyn.LitTrue;
import com.googlecode.opennars.parser.loan.Loan.Absyn.LitURI;
import com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix1;
import com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix2;
import com.googlecode.opennars.parser.loan.Loan.Absyn.SentDelay;
import com.googlecode.opennars.parser.loan.Loan.Absyn.SentGoal;
import com.googlecode.opennars.parser.loan.Loan.Absyn.SentImport;
import com.googlecode.opennars.parser.loan.Loan.Absyn.SentJudge;
import com.googlecode.opennars.parser.loan.Loan.Absyn.SentOp;
import com.googlecode.opennars.parser.loan.Loan.Absyn.SentPrefix;
import com.googlecode.opennars.parser.loan.Loan.Absyn.SentQuest;
import com.googlecode.opennars.parser.loan.Loan.Absyn.Stm;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmConj;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmDisj;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmEquiv;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvConc;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvPred;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmFut;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpConc;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpPred;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpRet;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpl;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmInPp;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmInher;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmInst;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmNot;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmOp;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmPar;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmPres;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmProp;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmPst;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmSeq;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmSim;
import com.googlecode.opennars.parser.loan.Loan.Absyn.StmTrm;
import com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExDif;
import com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExImg;
import com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExInt;
import com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExSet;
import com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInDif;
import com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInImg;
import com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInInt;
import com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInSet;
import com.googlecode.opennars.parser.loan.Loan.Absyn.TrmLit;
import com.googlecode.opennars.parser.loan.Loan.Absyn.TrmProd;
import com.googlecode.opennars.parser.loan.Loan.Absyn.TrmStm;
import com.googlecode.opennars.parser.loan.Loan.Absyn.TruthE;
import com.googlecode.opennars.parser.loan.Loan.Absyn.TruthF;
import com.googlecode.opennars.parser.loan.Loan.Absyn.TruthFC;
import com.googlecode.opennars.parser.loan.Loan.Absyn.URICur;
import com.googlecode.opennars.parser.loan.Loan.Absyn.URIFul;

public class LoanParser extends Parser {

	public class SerialiseVisitor implements TermVisitor<Object,LoanParser> {

		/**
		 * If the provided object is a term, wrap it in a StmTrm object
		 * @param o the object to (potentially) wrap
		 * @return the wrapped object
		 */
		private Stm wrapTerm(Object o) {
			if(o instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.Term) {
				return new StmTrm((com.googlecode.opennars.parser.loan.Loan.Absyn.Term) o);
			}
			else
				return (Stm) o;
		}
		
		/**
		 * If the provided object is a statement, wrap it in a TrmStm object
		 * @param o the object to (potentially) wrap
		 * @return the wrapped object
		 */
		private com.googlecode.opennars.parser.loan.Loan.Absyn.Term wrapStatement(Object o) {
			if(o instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.Stm) {
				return new TrmStm((com.googlecode.opennars.parser.loan.Loan.Absyn.Stm) o);
			}
			else
				return (com.googlecode.opennars.parser.loan.Loan.Absyn.Term) o;
		}
		
		public Object visit(Term p, LoanParser arg) {
			// TODO: Fix this awful ladder. I wish Java had a more dynamic invocation procedure like Smalltalk or Objective-C
			if(p instanceof BooleanLiteral)
				return visit((BooleanLiteral) p, arg);
			if(p instanceof Conjunction)
				return visit((Conjunction) p, arg);
			if(p instanceof ConjunctionParallel)
				return visit((ConjunctionParallel) p, arg);
			if(p instanceof ConjunctionSequence)
				return visit((ConjunctionSequence) p, arg);
			if(p instanceof DifferenceExt)
				return visit((DifferenceExt) p, arg);
			if(p instanceof DifferenceInt)
				return visit((DifferenceInt) p, arg);
			if(p instanceof Disjunction)
				return visit((Disjunction) p, arg);
			if(p instanceof Equivalence)
				return visit((Equivalence) p, arg);
			if(p instanceof EquivalenceAfter)
				return visit((EquivalenceAfter) p, arg);
			if(p instanceof EquivalenceWhen)
				return visit((EquivalenceWhen) p, arg);
			if(p instanceof ImageExt)
				return visit((ImageExt) p, arg);
			if(p instanceof ImageInt)
				return visit((ImageInt) p, arg);
			if(p instanceof Implication)
				return visit((Implication) p, arg);
			if(p instanceof ImplicationAfter)
				return visit((ImplicationAfter) p, arg);
			if(p instanceof ImplicationBefore)
				return visit((ImplicationBefore) p, arg);
			if(p instanceof ImplicationWhen)
				return visit((ImplicationWhen) p, arg);
			if(p instanceof Inheritance)
				return visit((Inheritance) p, arg);
			if(p instanceof Instance)
				return visit((Instance) p, arg);
			if(p instanceof InstanceProperty)
				return visit((InstanceProperty) p, arg);
			if(p instanceof IntersectionExt)
				return visit((IntersectionExt) p, arg);
			if(p instanceof IntersectionInt)
				return visit((IntersectionInt) p, arg);
			if(p instanceof Negation)
				return visit((Negation) p, arg);
			if(p instanceof NumericLiteral)
				return visit((NumericLiteral) p, arg);
			if(p instanceof Product)
				return visit((Product) p, arg);
			if(p instanceof Property)
				return visit((Property) p, arg);
			if(p instanceof SetExt)
				return visit((SetExt) p, arg);
			if(p instanceof SetInt)
				return visit((SetInt) p, arg);
			if(p instanceof Similarity)
				return visit((Similarity) p, arg);
			if(p instanceof TenseFuture)
				return visit((TenseFuture) p, arg);
			if(p instanceof TensePast)
				return visit((TensePast) p, arg);
			if(p instanceof TensePresent)
				return visit((TensePresent) p, arg);
			if(p instanceof URIRef)
				return visit((URIRef) p, arg);
			if(p instanceof Variable)
				return visit((Variable) p, arg);
			
			return new TrmLit(new LitString(p.getName()));
		}
		
		public Object visit(BooleanLiteral p, LoanParser arg) {
			if(p.getName().equals("true"))
				return new TrmLit(new LitTrue());
			else
				return new TrmLit(new LitFalse());
		}

		public Object visit(Conjunction p, LoanParser arg) {
			ArrayList<Term> components = p.getComponents();
			Iterator<Term> iter = components.iterator();
			Term t = iter.next();
			Stm s = wrapTerm(t.accept(this, arg));
			while(iter.hasNext()) {
				s = new StmConj(s, wrapTerm(iter.next().accept(this, arg)));
			}
			return s;
		}

		public Object visit(ConjunctionParallel p, LoanParser arg) {
			ArrayList<Term> components = p.getComponents();
			Iterator<Term> iter = components.iterator();
			Term t = iter.next();
			Stm s = wrapTerm(t.accept(this, arg));
			while(iter.hasNext()) {
				s = new StmPar(s, wrapTerm(iter.next().accept(this, arg)));
			}
			return s;
		}

		public Object visit(ConjunctionSequence p, LoanParser arg) {
			ArrayList<Term> components = p.getComponents();
			Iterator<Term> iter = components.iterator();
			Term t = iter.next();
			Stm s = wrapTerm(t.accept(this, arg));
			while(iter.hasNext()) {
				s = new StmSeq(s, wrapTerm(iter.next().accept(this, arg)));
			}
			return s;
		}

		public Object visit(DifferenceExt p, LoanParser arg) {
			ArrayList<Term> components = p.getComponents();
			Term t1 = components.get(0);
			Term t2 = components.get(1);
			return new TrmExDif(wrapStatement(t1.accept(this, arg)), wrapStatement(t2.accept(this, arg)));
		}

		public Object visit(DifferenceInt p, LoanParser arg) {
			ArrayList<Term> components = p.getComponents();
			Term t1 = components.get(0);
			Term t2 = components.get(1);
			return new TrmInDif(wrapStatement(t1.accept(this, arg)), wrapStatement(t2.accept(this, arg)));
		}

		public Object visit(Disjunction p, LoanParser arg) {
			ArrayList<Term> components = p.getComponents();
			Iterator<Term> iter = components.iterator();
			Term t = iter.next();
			Stm s = wrapTerm(t.accept(this, arg));
			while(iter.hasNext()) {
				s = new StmDisj(s, wrapTerm(iter.next().accept(this, arg)));
			}
			return s;
		}

		public Object visit(Equivalence p, LoanParser arg) {
			Stm subj = wrapTerm(p.getSubject().accept(this, arg));
			Stm pred = wrapTerm(p.getPredicate().accept(this, arg));
			return new StmEquiv(subj,pred);
		}

		public Object visit(EquivalenceAfter p, LoanParser arg) {
			Stm subj = wrapTerm(p.getSubject().accept(this, arg));
			Stm pred = wrapTerm(p.getPredicate().accept(this, arg));
			return new StmEqvPred(subj,pred);
		}

		public Object visit(EquivalenceWhen p, LoanParser arg) {
			Stm subj = wrapTerm(p.getSubject().accept(this, arg));
			Stm pred = wrapTerm(p.getPredicate().accept(this, arg));
			return new StmEqvConc(subj,pred);
		}

		public Object visit(ImageExt p, LoanParser arg) {
			List<Term> pre = p.getComponents().subList(0, p.getRelationIndex());
			Term reln = p.getComponents().get(p.getRelationIndex());
			List<Term> post = p.getComponents().subList(p.getRelationIndex() + 1, p.size());
			
			com.googlecode.opennars.parser.loan.Loan.Absyn.Term relnT = wrapStatement(reln.accept(this, arg));
			
			ListTerm preList = new ListTerm();
			Iterator<Term> iter = pre.iterator();
			while(iter.hasNext()) {
				preList.add(wrapStatement(iter.next().accept(this, arg)));
			}
			
			ListTerm postList = new ListTerm();
			iter = post.iterator();
			while(iter.hasNext()) {
				postList.add(wrapStatement(iter.next().accept(this, arg)));
			}
			
			return new TrmExImg(relnT, preList, postList);
		}

		public Object visit(ImageInt p, LoanParser arg) {
			List<Term> pre = p.getComponents().subList(0, p.getRelationIndex());
			Term reln = p.getComponents().get(p.getRelationIndex());
			List<Term> post = p.getComponents().subList(p.getRelationIndex() + 1, p.size());
			
			com.googlecode.opennars.parser.loan.Loan.Absyn.Term relnT = wrapStatement(reln.accept(this, arg));
			
			ListTerm preList = new ListTerm();
			Iterator<Term> iter = pre.iterator();
			while(iter.hasNext()) {
				preList.add(wrapStatement(iter.next().accept(this, arg)));
			}
			
			ListTerm postList = new ListTerm();
			iter = post.iterator();
			while(iter.hasNext()) {
				postList.add(wrapStatement(iter.next().accept(this, arg)));
			}
			
			return new TrmInImg(relnT, preList, postList);
		}

		public Object visit(Implication p, LoanParser arg) {
			Stm subj = wrapTerm(p.getSubject().accept(this, arg));
			Stm pred = wrapTerm(p.getPredicate().accept(this, arg));
			return new StmImpl(subj,pred);
		}

		public Object visit(ImplicationAfter p, LoanParser arg) {
			Stm subj = wrapTerm(p.getSubject().accept(this, arg));
			Stm pred = wrapTerm(p.getPredicate().accept(this, arg));
			return new StmImpPred(subj,pred);
		}

		public Object visit(ImplicationBefore p, LoanParser arg) {
			Stm subj = wrapTerm(p.getSubject().accept(this, arg));
			Stm pred = wrapTerm(p.getPredicate().accept(this, arg));
			return new StmImpRet(subj,pred);
		}
		
		public Object visit(ImplicationWhen p, LoanParser arg) {
			Stm subj = wrapTerm(p.getSubject().accept(this, arg));
			Stm pred = wrapTerm(p.getPredicate().accept(this, arg));
			return new StmImpConc(subj,pred);
		}

		public Object visit(Inheritance p, LoanParser arg) {
			com.googlecode.opennars.parser.loan.Loan.Absyn.Term subj = wrapStatement(p.getSubject().accept(this, arg));
			com.googlecode.opennars.parser.loan.Loan.Absyn.Term pred = wrapStatement(p.getPredicate().accept(this, arg));
			return new StmInher(subj,pred);
		}

		public Object visit(Instance p, LoanParser arg) {
			com.googlecode.opennars.parser.loan.Loan.Absyn.Term subj = wrapStatement(p.getSubject().accept(this, arg));
			com.googlecode.opennars.parser.loan.Loan.Absyn.Term pred = wrapStatement(p.getPredicate().accept(this, arg));
			return new StmInst(subj,pred);
		}

		public Object visit(InstanceProperty p, LoanParser arg) {
			com.googlecode.opennars.parser.loan.Loan.Absyn.Term subj = wrapStatement(p.getSubject().accept(this, arg));
			com.googlecode.opennars.parser.loan.Loan.Absyn.Term pred = wrapStatement(p.getPredicate().accept(this, arg));
			return new StmInPp(subj,pred);
		}

		public Object visit(IntersectionExt p, LoanParser arg) {
			ArrayList<Term> components = p.getComponents();
			Iterator<Term> iter = components.iterator();
			Term t = iter.next();
			com.googlecode.opennars.parser.loan.Loan.Absyn.Term s = wrapStatement(t.accept(this, arg));
			while(iter.hasNext()) {
				s = new TrmExInt(s, wrapStatement(iter.next().accept(this, arg)));
			}
			return s;
		}

		public Object visit(IntersectionInt p, LoanParser arg) {
			ArrayList<Term> components = p.getComponents();
			Iterator<Term> iter = components.iterator();
			Term t = iter.next();
			com.googlecode.opennars.parser.loan.Loan.Absyn.Term s = wrapStatement(t.accept(this, arg));
			while(iter.hasNext()) {
				s = new TrmInInt(s, wrapStatement(iter.next().accept(this, arg)));
			}
			return s;
		}

		public Object visit(Negation p, LoanParser arg) {
			Stm s = wrapTerm(p.getComponents().get(0).accept(this, arg));
			return new StmNot(s);
		}

		public Object visit(NumericLiteral p, LoanParser arg) {
			if(p.getType() == NumericLiteral.TYPE.INTEGER)
				return new TrmLit(new LitInt(Integer.parseInt(p.getName())));
			else if(p.getType() == NumericLiteral.TYPE.DOUBLE)
				return new TrmLit(new LitDbl(Double.parseDouble(p.getName())));
			else
				return null;
		}

		public Object visit(Product p, LoanParser arg) {
			Iterator<Term> iter = p.getComponents().iterator();
			ListTerm ts = new ListTerm();
			while(iter.hasNext()) {
				ts.add(wrapStatement(iter.next().accept(this, arg)));
			}
			return new TrmProd(ts);
		}

		public Object visit(Property p, LoanParser arg) {
			com.googlecode.opennars.parser.loan.Loan.Absyn.Term subj = wrapStatement(p.getSubject().accept(this, arg));
			com.googlecode.opennars.parser.loan.Loan.Absyn.Term pred = wrapStatement(p.getPredicate().accept(this, arg));
			return new StmProp(subj,pred);
		}

		public Object visit(SetExt p, LoanParser arg) {
			Iterator<Term> iter = p.getComponents().iterator();
			ListTerm ts = new ListTerm();
			while(iter.hasNext()) {
				ts.add(wrapStatement(iter.next().accept(this, arg)));
			}
			return new TrmExSet(ts);
		}

		public Object visit(SetInt p, LoanParser arg) {
			Iterator<Term> iter = p.getComponents().iterator();
			ListTerm ts = new ListTerm();
			while(iter.hasNext()) {
				ts.add(wrapStatement(iter.next().accept(this, arg)));
			}
			return new TrmInSet(ts);
		}

		public Object visit(Similarity p, LoanParser arg) {
			com.googlecode.opennars.parser.loan.Loan.Absyn.Term subj = wrapStatement(p.getSubject().accept(this, arg));
			com.googlecode.opennars.parser.loan.Loan.Absyn.Term pred = wrapStatement(p.getPredicate().accept(this, arg));
			return new StmSim(subj,pred);
		}

		public Object visit(TenseFuture p, LoanParser arg) {
			Stm s = wrapTerm(p.getComponents().get(0).accept(this, arg));
			return new StmFut(s);
		}

		public Object visit(TensePast p, LoanParser arg) {
			Stm s = wrapTerm(p.getComponents().get(0).accept(this, arg));
			return new StmPst(s);
		}

		public Object visit(TensePresent p, LoanParser arg) {
			Stm s = wrapTerm(p.getComponents().get(0).accept(this, arg));
			return new StmPres(s);
		}

		public Object visit(StringLiteral p, LoanParser arg) {
			return new TrmLit(new LitString(p.getName()));
		}

		public Object visit(URIRef p, LoanParser arg) {
			String uriref = p.getName();
			// Try namespaces first
			Iterator<String> iter = arg.getNamespaces().keySet().iterator();
			while(iter.hasNext()) {
				String pre = iter.next();
				URI u = arg.getNamespaces().get(pre);
				// Can we match the start of the URI to any known sequence?
				if(uriref.startsWith(u.toString())) {
					// Blank prefix is dealt with separately in the abstract syntax for parsing reasons
					if(u.equals(""))
						return new TrmLit(new LitURI(new URICur(new NSPrefix2(), uriref.substring(u.toString().length()))));
					else
						return new TrmLit(new LitURI(new URICur(new NSPrefix1(pre), uriref.substring(u.toString().length()))));
				}
			}
			
			try {
				// Try resolving against the base
				URI b = arg.getBaseURI();
				URI r = b.relativize(new URI(uriref));
				return new TrmLit(new LitURI(new URIFul("<" + r.toString() + ">")));
			}
			catch (URISyntaxException e) {
				e.printStackTrace();
			}
			return new TrmLit(new LitURI(new URIFul("<" + p.getName() + ">")));
		}

		public Object visit(Variable p, LoanParser arg) {
			if(p.getType() == Variable.VarType.QUERY)
				return new TrmLit(new LitQVar(p.getName().substring(1)));
			else if(p.getType() == Variable.VarType.INDEPENDENT)
				return new TrmLit(new LitSVarI(p.getName().substring(1)));
			else if(p.getType() == Variable.VarType.DEPENDENT)
				return new TrmLit(new LitSVarD(p.getName().substring(1), new ListIdent()));
			else if(p.getType() == Variable.VarType.ANONYMOUS)
				return new TrmLit(new LitQVarAn());
			else
				return null;
		}

	}

	public class ParserVisitor extends AbstractVisitor<Object, LoanParser> {

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetE, java.lang.Object)
		 */
		@Override
		public Object visit(BudgetE p, LoanParser arg) {
			return super.visit(p, arg);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetP, java.lang.Object)
		 */
		@Override
		public Object visit(BudgetP p, LoanParser arg) {
			// TODO Auto-generated method stub
			return super.visit(p, arg);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetPD, java.lang.Object)
		 */
		@Override
		public Object visit(BudgetPD p, LoanParser arg) {
			// TODO Auto-generated method stub
			return super.visit(p, arg);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.BaseR, java.lang.Object)
		 */
		@Override
		public Object visit(BaseR p, LoanParser arg) {
			try {
				arg.setBaseURI(new URI(p.urilit_.substring(1, p.urilit_.length()-1)));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.Doc, java.lang.Object)
		 */
		@Override
		public Object visit(Doc p, LoanParser arg) {
			List<Task> tasks = new ArrayList<Task>();
			Iterator<com.googlecode.opennars.parser.loan.Loan.Absyn.Sentence> iter = p.listsentence_.iterator();
			while(iter.hasNext()) {
				com.googlecode.opennars.parser.loan.Loan.Absyn.Sentence s = iter.next();
				Object t = s.accept(this, arg);
				if(t != null) {
					if(t instanceof List) // Should only happen with an @import
						tasks.addAll((Collection<? extends Task>) t);
					else
						tasks.add((Task) t);
				}
			}
			return tasks;
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.DocBR, java.lang.Object)
		 */
		@Override
		public Object visit(DocBR p, LoanParser arg) {
			p.baserule_.accept(this, arg);
			List<Task> tasks = new ArrayList<Task>();
			Iterator<com.googlecode.opennars.parser.loan.Loan.Absyn.Sentence> iter = p.listsentence_.iterator();
			while(iter.hasNext()) {
				com.googlecode.opennars.parser.loan.Loan.Absyn.Sentence s = iter.next();
				Task t = (Task) s.accept(this, arg);
				if(t != null)
					tasks.add(t);
			}
			return tasks;
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitDbl, java.lang.Object)
		 */
		@Override
		public Object visit(LitDbl p, LoanParser arg) {
			return new NumericLiteral(p.double_.doubleValue());
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitFalse, java.lang.Object)
		 */
		@Override
		public Object visit(LitFalse p, LoanParser arg) {
			return new BooleanLiteral(false);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitInt, java.lang.Object)
		 */
		@Override
		public Object visit(LitInt p, LoanParser arg) {
			return new NumericLiteral(p.integer_.intValue());
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVar, java.lang.Object)
		 */
		@Override
		public Object visit(LitQVar p, LoanParser arg) {
			return new Variable("" + Symbols.QUERY_VARIABLE_TAG + p.ident_);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVarAn, java.lang.Object)
		 */
		@Override
		public Object visit(LitQVarAn p, LoanParser arg) {
			return new Variable("" + Symbols.QUERY_VARIABLE_TAG);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitString, java.lang.Object)
		 */
		@Override
		public Object visit(LitString p, LoanParser arg) {
			// TODO Auto-generated method stub
			return new StringLiteral(p.string_);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarD, java.lang.Object)
		 */
		@Override
		public Object visit(LitSVarD p, LoanParser arg) {
			StringBuilder builder = new StringBuilder();
			builder.append(Symbols.VARIABLE_TAG);
			builder.append(p.ident_);
			builder.append(Symbols.COMPOUND_TERM_OPENER);
			Iterator<String> iter = p.listident_.iterator();
			while(iter.hasNext()) {
				builder.append(iter.next());
				if(iter.hasNext())
					builder.append(",");
			}
			builder.append(Symbols.COMPOUND_TERM_CLOSER);
			return new Variable(builder.toString());
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarI, java.lang.Object)
		 */
		@Override
		public Object visit(LitSVarI p, LoanParser arg) {
			return new Variable("" + Symbols.VARIABLE_TAG + p.ident_);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitTrue, java.lang.Object)
		 */
		@Override
		public Object visit(LitTrue p, LoanParser arg) {
			return new BooleanLiteral(true);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.LitURI, java.lang.Object)
		 */
		@Override
		public Object visit(LitURI p, LoanParser arg) {
			URI u = (URI) p.uriref_.accept(this, arg);
			return new URIRef(u);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix1, java.lang.Object)
		 */
		@Override
		public Object visit(NSPrefix1 p, LoanParser arg) {
			// TODO Auto-generated method stub
			return p.ident_;
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix2, java.lang.Object)
		 */
		@Override
		public Object visit(NSPrefix2 p, LoanParser arg) {
			// TODO Auto-generated method stub
			return "";
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentDelay, java.lang.Object)
		 */
		@Override
		public Object visit(SentDelay p, LoanParser arg) {
			int delay = p.integer_.intValue();
			for(int i = 0; i < delay; i++)
				arg.getMemory().cycle();
			return null;
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentGoal, java.lang.Object)
		 */
		@Override
		public Object visit(SentGoal p, LoanParser arg) {
			// Get the truth
			TruthValue truth = (TruthValue) p.truthvalue_.accept(this, arg);
			
			// We don't actually do budget values (yet) in LOAN
			float priority = memory.getParameters().DEFAULT_GOAL_PRIORITY;
            float durability = memory.getParameters().DEFAULT_GOAL_DURABILITY;
            float quality = memory.budgetfunctions.truthToQuality(truth);
            BudgetValue budget = new BudgetValue(priority, durability, quality, memory);
            
            // Process the sentence itself
            Term content = (Term) p.stm_.accept(this, arg);
            Base base = new Base();
            Sentence sentence = Sentence.make(content, Symbols.GOAL_MARK, truth, base, memory);
            sentence.setInput();
            
            return new Task(sentence, budget, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentImport, java.lang.Object)
		 */
		@Override
		public Object visit(SentImport p, LoanParser arg) {
			try {
				URL u = new URL(p.urilit_.substring(1, p.urilit_.length()-1));
				BufferedReader reader = new BufferedReader(new InputStreamReader(u.openStream()));
				StringBuilder builder = new StringBuilder();
				String line = "";
				while(line != null) {
					builder.append(line);
					builder.append("\n");
					line = reader.readLine();
				}
				
				LoanParser parser = new LoanParser();
				parser.setBaseURI(u.toURI());
				List<Task> ts = parser.parseTasks(builder.toString(), memory);
				return ts;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (InvalidInputException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentJudge, java.lang.Object)
		 */
		@Override
		public Object visit(SentJudge p, LoanParser arg) {
			// Get the truth
			TruthValue truth = (TruthValue) p.truthvalue_.accept(this, arg);
			
			// We don't actually do budget values (yet) in LOAN
			float priority = memory.getParameters().DEFAULT_JUDGMENT_PRIORITY;
            float durability = memory.getParameters().DEFAULT_JUDGMENT_DURABILITY;
            float quality = memory.budgetfunctions.truthToQuality(truth);
            BudgetValue budget = new BudgetValue(priority, durability, quality, memory);
            
            // Process the sentence itself
            Term content = (Term) p.stm_.accept(this, arg);
            Base base = new Base();
            Sentence sentence = Sentence.make(content, Symbols.JUDGMENT_MARK, truth, base, memory);
            sentence.setInput();
            
            return new Task(sentence, budget, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentOp, java.lang.Object)
		 */
		@Override
		public Object visit(SentOp p, LoanParser arg) {
			// TODO Handle the adding of operators once we can do something with them
			return null;
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentPrefix, java.lang.Object)
		 */
		@Override
		public Object visit(SentPrefix p, LoanParser arg) {
			String pre = (String) p.nsprefix_.accept(this, arg);
			try {
				URI u = new URI(p.urilit_.substring(1, p.urilit_.length()-1));
				arg.getNamespaces().put(pre, u);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.SentQuest, java.lang.Object)
		 */
		@Override
		public Object visit(SentQuest p, LoanParser arg) {
			// Questions don't have truth
			TruthValue truth = null;
			
			// We don't actually do budget values (yet) in LOAN
			float priority = memory.getParameters().DEFAULT_QUESTION_PRIORITY;
            float durability = memory.getParameters().DEFAULT_QUESTION_DURABILITY;
            float quality = 1;
            BudgetValue budget = new BudgetValue(priority, durability, quality, memory);
            
            // Process the sentence itself
            Term content = (Term) p.stm_.accept(this, arg);
            Base base = null;
            Sentence sentence = Sentence.make(content, Symbols.QUESTION_MARK, truth, base, memory);
            sentence.setInput();
            
            // ++DEBUG
            System.out.println(sentence.toString2());
            // --DEBUG
            
            return new Task(sentence, budget, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmConj, java.lang.Object)
		 */
		@Override
		public Object visit(StmConj p, LoanParser arg) {
			Term t1 = (Term) p.stm_1.accept(this, arg);
			Term t2 = (Term) p.stm_2.accept(this, arg);
			ArrayList<Term> ts = new ArrayList<Term>();
			ts.add(t1);
			ts.add(t2);
			
			return CompoundTerm.make(Symbols.CONJUNCTION_OPERATOR, ts, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmDisj, java.lang.Object)
		 */
		@Override
		public Object visit(StmDisj p, LoanParser arg) {
			Term t1 = (Term) p.stm_1.accept(this, arg);
			Term t2 = (Term) p.stm_2.accept(this, arg);
			ArrayList<Term> ts = new ArrayList<Term>();
			ts.add(t1);
			ts.add(t2);
			
			return CompoundTerm.make(Symbols.DISJUNCTION_OPERATOR, ts, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmEquiv, java.lang.Object)
		 */
		@Override
		public Object visit(StmEquiv p, LoanParser arg) {
			Term t1 = (Term) p.stm_1.accept(this, arg);
			Term t2 = (Term) p.stm_2.accept(this, arg);
			ArrayList<Term> ts = new ArrayList<Term>();
			ts.add(t1);
			ts.add(t2);
			
			return Statement.make(Symbols.EQUIVALENCE_RELATION, t1, t2, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvConc, java.lang.Object)
		 */
		@Override
		public Object visit(StmEqvConc p, LoanParser arg) {
			Term t1 = (Term) p.stm_1.accept(this, arg);
			Term t2 = (Term) p.stm_2.accept(this, arg);
			ArrayList<Term> ts = new ArrayList<Term>();
			ts.add(t1);
			ts.add(t2);
			
			return Statement.make(Symbols.EQUIVALENCE_WHEN_RELATION, t1, t2, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvPred, java.lang.Object)
		 */
		@Override
		public Object visit(StmEqvPred p, LoanParser arg) {
			Term t1 = (Term) p.stm_1.accept(this, arg);
			Term t2 = (Term) p.stm_2.accept(this, arg);
			
			return Statement.make(Symbols.EQUIVALENCE_AFTER_RELATION, t1, t2, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmFut, java.lang.Object)
		 */
		@Override
		public Object visit(StmFut p, LoanParser arg) {
			Term t1 = (Term) p.stm_.accept(this, arg);
			
			ArrayList<Term> ts = new ArrayList<Term>();
			ts.add(t1);
			
			return CompoundTerm.make(Symbols.FUTURE_OPERATOR, ts, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpConc, java.lang.Object)
		 */
		@Override
		public Object visit(StmImpConc p, LoanParser arg) {
			Term t1 = (Term) p.stm_1.accept(this, arg);
			Term t2 = (Term) p.stm_2.accept(this, arg);
			
			return Statement.make(Symbols.IMPLICATION_WHEN_RELATION, t1, t2, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpl, java.lang.Object)
		 */
		@Override
		public Object visit(StmImpl p, LoanParser arg) {
			Term t1 = (Term) p.stm_1.accept(this, arg);
			Term t2 = (Term) p.stm_2.accept(this, arg);
			
			return Statement.make(Symbols.IMPLICATION_RELATION, t1, t2, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpPred, java.lang.Object)
		 */
		@Override
		public Object visit(StmImpPred p, LoanParser arg) {
			Term t1 = (Term) p.stm_1.accept(this, arg);
			Term t2 = (Term) p.stm_2.accept(this, arg);
			
			return Statement.make(Symbols.IMPLICATION_AFTER_RELATION, t1, t2, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpRet, java.lang.Object)
		 */
		@Override
		public Object visit(StmImpRet p, LoanParser arg) {
			Term t1 = (Term) p.stm_1.accept(this, arg);
			Term t2 = (Term) p.stm_2.accept(this, arg);
			
			return Statement.make(Symbols.IMPLICATION_BEFORE_RELATION, t1, t2, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmInher, java.lang.Object)
		 */
		@Override
		public Object visit(StmInher p, LoanParser arg) {
			Term t1 = (Term) p.term_1.accept(this, arg);
			Term t2 = (Term) p.term_2.accept(this, arg);
			
			return Statement.make(Symbols.INHERITANCE_RELATION, t1, t2, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmInPp, java.lang.Object)
		 */
		@Override
		public Object visit(StmInPp p, LoanParser arg) {
			Term t1 = (Term) p.term_1.accept(this, arg);
			Term t2 = (Term) p.term_2.accept(this, arg);
			
			return Statement.make(Symbols.INSTANCE_PROPERTY_RELATION, t1, t2, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmInst, java.lang.Object)
		 */
		@Override
		public Object visit(StmInst p, LoanParser arg) {
			Term t1 = (Term) p.term_1.accept(this, arg);
			Term t2 = (Term) p.term_2.accept(this, arg);
			
			return Statement.make(Symbols.INSTANCE_RELATION, t1, t2, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmNot, java.lang.Object)
		 */
		@Override
		public Object visit(StmNot p, LoanParser arg) {
			Term t1 = (Term) p.stm_.accept(this, arg);
			
			ArrayList<Term> ts = new ArrayList<Term>();
			ts.add(t1);
			
			return CompoundTerm.make(Symbols.NEGATION_OPERATOR, ts, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmOp, java.lang.Object)
		 */
		@Override
		public Object visit(StmOp p, LoanParser arg) {
			// Get the functor
			Term t1 = (Term) p.term_.accept(this, arg);
			
			// Get the arguments
			ArrayList<Term> opargs = new ArrayList<Term>();
			Iterator<com.googlecode.opennars.parser.loan.Loan.Absyn.Term> iter = p.listterm_.iterator();
			while(iter.hasNext()) {
				Term t = (Term) iter.next().accept(this, arg);
				opargs.add(t);
			}
			
			// Make arguments into a product
			Term prod = CompoundTerm.make(Symbols.PRODUCT_OPERATOR, opargs, memory);
			
			return Statement.make(Symbols.INHERITANCE_RELATION, prod, t1, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmPar, java.lang.Object)
		 */
		@Override
		public Object visit(StmPar p, LoanParser arg) {
			Term t1 = (Term) p.stm_1.accept(this, arg);
			Term t2 = (Term) p.stm_2.accept(this, arg);
			ArrayList<Term> ts = new ArrayList<Term>();
			ts.add(t1);
			ts.add(t2);
			
			return CompoundTerm.make(Symbols.PARALLEL_OPERATOR, ts, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmPres, java.lang.Object)
		 */
		@Override
		public Object visit(StmPres p, LoanParser arg) {
			Term t1 = (Term) p.stm_.accept(this, arg);
			
			ArrayList<Term> ts = new ArrayList<Term>();
			ts.add(t1);
			
			return CompoundTerm.make(Symbols.PRESENT_OPERATOR, ts, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmProp, java.lang.Object)
		 */
		@Override
		public Object visit(StmProp p, LoanParser arg) {
			Term t1 = (Term) p.term_1.accept(this, arg);
			Term t2 = (Term) p.term_2.accept(this, arg);
			
			return Statement.make(Symbols.PROPERTY_RELATION, t1, t2, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmPst, java.lang.Object)
		 */
		@Override
		public Object visit(StmPst p, LoanParser arg) {
			Term t1 = (Term) p.stm_.accept(this, arg);
			
			ArrayList<Term> ts = new ArrayList<Term>();
			ts.add(t1);
			
			return CompoundTerm.make(Symbols.PAST_OPERATOR, ts, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmSeq, java.lang.Object)
		 */
		@Override
		public Object visit(StmSeq p, LoanParser arg) {
			Term t1 = (Term) p.stm_1.accept(this, arg);
			Term t2 = (Term) p.stm_2.accept(this, arg);
			ArrayList<Term> ts = new ArrayList<Term>();
			ts.add(t1);
			ts.add(t2);
			
			return CompoundTerm.make(Symbols.SEQUENCE_OPERATOR, ts, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmSim, java.lang.Object)
		 */
		@Override
		public Object visit(StmSim p, LoanParser arg) {
			Term t1 = (Term) p.term_1.accept(this, arg);
			Term t2 = (Term) p.term_2.accept(this, arg);
			
			return Statement.make(Symbols.SIMILARITY_RELATION, t1, t2, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.StmTrm, java.lang.Object)
		 */
		@Override
		public Object visit(StmTrm p, LoanParser arg) {
			return p.term_.accept(this, arg);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExDif, java.lang.Object)
		 */
		@Override
		public Object visit(TrmExDif p, LoanParser arg) {
			Term t1 = (Term) p.term_1.accept(this, arg);
			Term t2 = (Term) p.term_2.accept(this, arg);
			ArrayList<Term> ts = new ArrayList<Term>();
			ts.add(t1);
			ts.add(t2);
			
			return CompoundTerm.make(Symbols.DIFFERENCE_EXT_OPERATOR, ts, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExImg, java.lang.Object)
		 */
		@Override
		public Object visit(TrmExImg p, LoanParser arg) {
			Term reln = (Term) p.term_.accept(this, arg);
			ArrayList<Term> ts = new ArrayList<Term>();
			ts.add(reln);
			
			Iterator<com.googlecode.opennars.parser.loan.Loan.Absyn.Term> iter = p.listterm_1.iterator();
			while(iter.hasNext())
				ts.add((Term) iter.next().accept(this, arg));
			ts.add(new Term("" + Symbols.IMAGE_PLACE_HOLDER));
			iter = p.listterm_2.iterator();
			while(iter.hasNext())
				ts.add((Term) iter.next().accept(this, arg));
			
			return CompoundTerm.make(Symbols.IMAGE_EXT_OPERATOR, ts, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExInt, java.lang.Object)
		 */
		@Override
		public Object visit(TrmExInt p, LoanParser arg) {
			Term t1 = (Term) p.term_1.accept(this, arg);
			Term t2 = (Term) p.term_2.accept(this, arg);
			ArrayList<Term> ts = new ArrayList<Term>();
			ts.add(t1);
			ts.add(t2);
			
			return CompoundTerm.make(Symbols.INTERSECTION_EXT_OPERATOR, ts, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExSet, java.lang.Object)
		 */
		@Override
		public Object visit(TrmExSet p, LoanParser arg) {
			ArrayList<Term> ts = new ArrayList<Term>();
			Iterator<com.googlecode.opennars.parser.loan.Loan.Absyn.Term> iter = p.listterm_.iterator();
			while(iter.hasNext()) {
				Term t = (Term) iter.next().accept(this, arg);
				ts.add(t);
			}
			
			return SetExt.make(ts, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInDif, java.lang.Object)
		 */
		@Override
		public Object visit(TrmInDif p, LoanParser arg) {
			Term t1 = (Term) p.term_1.accept(this, arg);
			Term t2 = (Term) p.term_2.accept(this, arg);
			ArrayList<Term> ts = new ArrayList<Term>();
			ts.add(t1);
			ts.add(t2);
			
			return CompoundTerm.make(Symbols.DIFFERENCE_INT_OPERATOR, ts, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInImg, java.lang.Object)
		 */
		@Override
		public Object visit(TrmInImg p, LoanParser arg) {
			Term reln = (Term) p.term_.accept(this, arg);
			ArrayList<Term> ts = new ArrayList<Term>();
			ts.add(reln);
			
			Iterator<com.googlecode.opennars.parser.loan.Loan.Absyn.Term> iter = p.listterm_1.iterator();
			while(iter.hasNext())
				ts.add((Term) iter.next().accept(this, arg));
			ts.add(new Term("" + Symbols.IMAGE_PLACE_HOLDER));
			iter = p.listterm_2.iterator();
			while(iter.hasNext())
				ts.add((Term) iter.next().accept(this, arg));
			
			return CompoundTerm.make(Symbols.IMAGE_INT_OPERATOR, ts, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInInt, java.lang.Object)
		 */
		@Override
		public Object visit(TrmInInt p, LoanParser arg) {
			Term t1 = (Term) p.term_1.accept(this, arg);
			Term t2 = (Term) p.term_2.accept(this, arg);
			ArrayList<Term> ts = new ArrayList<Term>();
			ts.add(t1);
			ts.add(t2);
			
			return CompoundTerm.make(Symbols.INTERSECTION_INT_OPERATOR, ts, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInSet, java.lang.Object)
		 */
		@Override
		public Object visit(TrmInSet p, LoanParser arg) {
			ArrayList<Term> ts = new ArrayList<Term>();
			Iterator<com.googlecode.opennars.parser.loan.Loan.Absyn.Term> iter = p.listterm_.iterator();
			while(iter.hasNext()) {
				Term t = (Term) iter.next().accept(this, arg);
				ts.add(t);
			}
			
			return SetInt.make(ts, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmLit, java.lang.Object)
		 */
		@Override
		public Object visit(TrmLit p, LoanParser arg) {
			return p.literal_.accept(this, arg);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmProd, java.lang.Object)
		 */
		@Override
		public Object visit(TrmProd p, LoanParser arg) {
			ArrayList<Term> ts = new ArrayList<Term>();
			Iterator<com.googlecode.opennars.parser.loan.Loan.Absyn.Term> iter = p.listterm_.iterator();
			while(iter.hasNext()) {
				Term t = (Term) iter.next().accept(this, arg);
				ts.add(t);
			}
			
			return CompoundTerm.make(Symbols.PRODUCT_OPERATOR, ts, memory);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TrmStm, java.lang.Object)
		 */
		@Override
		public Object visit(TrmStm p, LoanParser arg) {
			return p.stm_.accept(this, arg);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TruthE, java.lang.Object)
		 */
		@Override
		public Object visit(TruthE p, LoanParser arg) {
			return new TruthValue(1, memory.getParameters().DEFAULT_JUDGMENT_CONFIDENCE);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TruthF, java.lang.Object)
		 */
		@Override
		public Object visit(TruthF p, LoanParser arg) {
			return new TruthValue(p.double_.floatValue(), memory.getParameters().DEFAULT_JUDGMENT_CONFIDENCE);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.TruthFC, java.lang.Object)
		 */
		@Override
		public Object visit(TruthFC p, LoanParser arg) {
			return new TruthValue(p.double_1.floatValue(),p.double_2.floatValue());
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.URICur, java.lang.Object)
		 */
		@Override
		public Object visit(URICur p, LoanParser arg) {
			String pre = (String) p.nsprefix_.accept(this, arg);
			URI u = arg.getNamespaces().get(pre);
			return u.resolve(p.ident_);
		}

		/* (non-Javadoc)
		 * @see com.googlecode.opennars.parser.loan.Loan.AbstractVisitor#visit(com.googlecode.opennars.parser.loan.Loan.Absyn.URIFul, java.lang.Object)
		 */
		@Override
		public Object visit(URIFul p, LoanParser arg) {
			URI base = arg.getBaseURI();
			String u = p.urilit_.substring(1, p.urilit_.length()-1);
			return base.resolve(u);
		}

	}

	private Map<String,URI> namespaces = new HashMap<String,URI>();
	private URI baseURI = null;
	private Memory memory = null;
	
	@Override
	public Task parseTask(String input, Memory memory)
			throws InvalidInputException {
		this.memory = memory;
		List<Task> tasks = null;
		Yylex l = new Yylex(new StringReader(input));
	    parser p = new parser(l);
	    try {
			Document parse_tree = p.pDocument();
			tasks = (List<Task>) parse_tree.accept(new ParserVisitor(), this);
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidInputException();
		}
	    if(tasks.size() > 0)
	    	return tasks.get(0);
	    else
	    	return null;
	}

	@Override
	public List<Task> parseTasks(String input, Memory memory)
			throws InvalidInputException {
		this.memory = memory;
		List<Task> tasks = null;
		Yylex l = new Yylex(new StringReader(input));
	    parser p = new parser(l);
	    try {
			Document parse_tree = p.pDocument();
			tasks = (List<Task>) parse_tree.accept(new ParserVisitor(), this);
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidInputException();
		}
	    
		return tasks;
	}

	@Override
	public String serialiseSentence(Sentence task, Memory memory) {
		com.googlecode.opennars.parser.loan.Loan.Absyn.ListSentence ss = new com.googlecode.opennars.parser.loan.Loan.Absyn.ListSentence();
		com.googlecode.opennars.parser.loan.Loan.Absyn.Sentence s;
		com.googlecode.opennars.parser.loan.Loan.Absyn.Stm stm = (Stm) ((CompoundTerm) task.getContent()).accept(new SerialiseVisitor(), this);
		if(task.isJudgment()) {
			com.googlecode.opennars.parser.loan.Loan.Absyn.TruthValue tv = new com.googlecode.opennars.parser.loan.Loan.Absyn.TruthFC(Double.valueOf(task.getTruth().getFrequency()), Double.valueOf(task.getTruth().getConfidence()));
			s = new com.googlecode.opennars.parser.loan.Loan.Absyn.SentJudge(stm, tv, new BudgetE());
			ss.add(s);
		}
		else if(task.isQuestion()) {
			s = new com.googlecode.opennars.parser.loan.Loan.Absyn.SentQuest(stm, new BudgetE());
			ss.add(s);
		}
		else if(task.isGoal()) {
			com.googlecode.opennars.parser.loan.Loan.Absyn.TruthValue tv = new com.googlecode.opennars.parser.loan.Loan.Absyn.TruthFC(Double.valueOf(task.getTruth().getFrequency()), Double.valueOf(task.getTruth().getConfidence()));
			s = new com.googlecode.opennars.parser.loan.Loan.Absyn.SentGoal(stm, tv, new BudgetE());
			ss.add(s);
		}
		
		com.googlecode.opennars.parser.loan.Loan.Absyn.Doc doc = new Doc(ss);
		return PrettyPrinter.print(doc).replace('\n', ' ');
	}

	@Override
	public String serialiseSentences(List<Sentence> tasks, Memory memory) {
		// TODO: Write this method
		com.googlecode.opennars.parser.loan.Loan.Absyn.BaseRule br = new com.googlecode.opennars.parser.loan.Loan.Absyn.BaseR(this.getBaseURI().toString());
		return null;
	}

	/**
	 * @return the memory
	 */
	public Memory getMemory() {
		return memory;
	}

	/**
	 * @param memory the memory to set
	 */
	public void setMemory(Memory memory) {
		this.memory = memory;
	}

	/**
	 * @return the namespaces
	 */
	public Map<String, URI> getNamespaces() {
		return namespaces;
	}

	/**
	 * @param namespaces the namespaces to set
	 */
	public void setNamespaces(Map<String, URI> namespaces) {
		this.namespaces = namespaces;
	}

	/**
	 * @return the base URI
	 */
	public URI getBaseURI() {
		return baseURI;
	}

	/**
	 * @param baseURI the base URI to set
	 */
	public void setBaseURI(URI baseURI) {
		this.baseURI = baseURI;
	}

}
