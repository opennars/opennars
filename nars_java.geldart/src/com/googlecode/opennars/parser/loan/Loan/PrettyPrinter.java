package com.googlecode.opennars.parser.loan.Loan;
import com.googlecode.opennars.parser.loan.Loan.Absyn.*;

public class PrettyPrinter
{
  //For certain applications increasing the initial size of the buffer may improve performance.
  private static final int INITIAL_BUFFER_SIZE = 128;
  //You may wish to change the parentheses used in precedence.
  private static final String _L_PAREN = new String("(");
  private static final String _R_PAREN = new String(")");
  
  private static boolean inAtRule = false;
  //You may wish to change render
  private static void render(String s)
  {
    if (s.equals("{"))
    {
       //buf_.append("\n");
       //indent();
       buf_.append(s);
       //_n_ = _n_ + 2;
       //buf_.append("\n");
       //indent();
    }
    else if (s.equals("(") || s.equals("["))
       buf_.append(s);
    else if (s.equals(")") || s.equals("]"))
    {
       //backup();
       trim();
       buf_.append(s);
       //buf_.append(" ");
    }
    else if (s.equals("}"))
    {
       //_n_ = _n_ - 2;
       //backup();
       //backup();
       buf_.append(s);
       //buf_.append("\n");
       //indent();
    }
    else if (s.equals(","))
    {
       //backup();
       buf_.append(s);
       buf_.append(" ");
    }
    else if (s.equals(";"))
    {
       //backup();
       buf_.append(s);
       buf_.append(" ");
       //buf_.append("\n");
       //indent();
    }
    else if (s.equals(":")) {
    	if(!inAtRule) trim();
    	buf_.append(s);
    	if(inAtRule) buf_.append(" ");
    }
    else if (s.equals(".") && inAtRule) {
    	trim();
    	inAtRule = false;
    	buf_.append(s);
    }
    else if (s.equals("")) return;
    else if (s.equals("&") || s.equals("&&") || s.equals("%") 
    		|| s.equals("|") || s.equals("||") || s.equals("-") || s.equals("~") || s.equals("/") 
    		|| s.equals("\\") || s.equals("-->") || s.equals("<->") || s.equals("}->")
    		|| s.equals("--[") || s.equals("}-[") || s.equals("==>") || s.equals("<=>")
    		|| s.equals("=/>") || s.equals("=\\>") || s.equals("=|>") || s.equals("<|>")
    		|| s.equals("</>")) {
    	trim();
    	buf_.append(" ");
    	buf_.append(s);
    	buf_.append(" ");
    }
    else if (s.equals("not") || s.equals("future") || s.equals("present") || s.equals("past")
    		 || s.equals("@budget")) {
    	buf_.append(s);
    	buf_.append(" ");
    }
    else if (s.equals("@base") || s.equals("@import") || s.equals("@prefix") || s.equals("@operator")
    		|| s.equals("@delay")) {
    	buf_.append(s);
    	buf_.append(" ");
    	inAtRule = true;
    }
    else
    {
       buf_.append(s);
       // buf_.append(" ");
    }
  }


  //  print and show methods are defined for each category.
  public static String print(com.googlecode.opennars.parser.loan.Loan.Absyn.Document foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(com.googlecode.opennars.parser.loan.Loan.Absyn.Document foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(com.googlecode.opennars.parser.loan.Loan.Absyn.BaseRule foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(com.googlecode.opennars.parser.loan.Loan.Absyn.BaseRule foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(com.googlecode.opennars.parser.loan.Loan.Absyn.ListSentence foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(com.googlecode.opennars.parser.loan.Loan.Absyn.ListSentence foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(com.googlecode.opennars.parser.loan.Loan.Absyn.Sentence foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(com.googlecode.opennars.parser.loan.Loan.Absyn.Sentence foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(com.googlecode.opennars.parser.loan.Loan.Absyn.Budget foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(com.googlecode.opennars.parser.loan.Loan.Absyn.Budget foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(com.googlecode.opennars.parser.loan.Loan.Absyn.Stm foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(com.googlecode.opennars.parser.loan.Loan.Absyn.Stm foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(com.googlecode.opennars.parser.loan.Loan.Absyn.Term foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(com.googlecode.opennars.parser.loan.Loan.Absyn.Term foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(com.googlecode.opennars.parser.loan.Loan.Absyn.ListTerm foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(com.googlecode.opennars.parser.loan.Loan.Absyn.ListTerm foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(com.googlecode.opennars.parser.loan.Loan.Absyn.ListIdent foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(com.googlecode.opennars.parser.loan.Loan.Absyn.ListIdent foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(com.googlecode.opennars.parser.loan.Loan.Absyn.URIRef foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(com.googlecode.opennars.parser.loan.Loan.Absyn.URIRef foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(com.googlecode.opennars.parser.loan.Loan.Absyn.Literal foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(com.googlecode.opennars.parser.loan.Loan.Absyn.Literal foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String print(com.googlecode.opennars.parser.loan.Loan.Absyn.TruthValue foo)
  {
    pp(foo, 0);
    trim();
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  public static String show(com.googlecode.opennars.parser.loan.Loan.Absyn.TruthValue foo)
  {
    sh(foo);
    String temp = buf_.toString();
    buf_.delete(0,buf_.length());
    return temp;
  }
  /***   You shouldn't need to change anything beyond this point.   ***/

  private static void pp(com.googlecode.opennars.parser.loan.Loan.Absyn.Document foo, int _i_)
  {
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.DocBR)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.DocBR _docbr = (com.googlecode.opennars.parser.loan.Loan.Absyn.DocBR) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_docbr.baserule_, 0);
       pp(_docbr.listsentence_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.Doc)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.Doc _doc = (com.googlecode.opennars.parser.loan.Loan.Absyn.Doc) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_doc.listsentence_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
  }

  private static void pp(com.googlecode.opennars.parser.loan.Loan.Absyn.BaseRule foo, int _i_)
  {
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.BaseR)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.BaseR _baser = (com.googlecode.opennars.parser.loan.Loan.Absyn.BaseR) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("@base");
       pp(_baser.urilit_, 0);
       render(".");
       if (_i_ > 0) render(_R_PAREN);
    }
  }

  private static void pp(com.googlecode.opennars.parser.loan.Loan.Absyn.ListSentence foo, int _i_)
  {
     for (java.util.Iterator<Sentence> it = foo.iterator(); it.hasNext();)
     {
       pp(it.next(), 0);
       if (it.hasNext()) {
         render("");
       } else {
         render("");
       }
     }
  }

  private static void pp(com.googlecode.opennars.parser.loan.Loan.Absyn.Sentence foo, int _i_)
  {
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.SentPrefix)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.SentPrefix _sentprefix = (com.googlecode.opennars.parser.loan.Loan.Absyn.SentPrefix) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("@prefix");
       pp(_sentprefix.nsprefix_, 0);
       pp(_sentprefix.urilit_, 0);
       render(".");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.SentImport)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.SentImport _sentimport = (com.googlecode.opennars.parser.loan.Loan.Absyn.SentImport) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("@import");
       pp(_sentimport.urilit_, 0);
       render(".");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.SentDelay)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.SentDelay _sentdelay = (com.googlecode.opennars.parser.loan.Loan.Absyn.SentDelay) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("@delay");
       pp(_sentdelay.integer_, 0);
       render(".");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.SentOp)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.SentOp _sentop = (com.googlecode.opennars.parser.loan.Loan.Absyn.SentOp) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("@operator");
       pp(_sentop.uriref_, 0);
       render(".");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.SentJudge)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.SentJudge _sentjudge = (com.googlecode.opennars.parser.loan.Loan.Absyn.SentJudge) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_sentjudge.stm_, 0);
       pp(_sentjudge.truthvalue_, 0);
       pp(_sentjudge.budget_, 0);
       render(".");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.SentQuest)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.SentQuest _sentquest = (com.googlecode.opennars.parser.loan.Loan.Absyn.SentQuest) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_sentquest.stm_, 0);
       render("?");
       pp(_sentquest.budget_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.SentGoal)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.SentGoal _sentgoal = (com.googlecode.opennars.parser.loan.Loan.Absyn.SentGoal) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_sentgoal.stm_, 0);
       pp(_sentgoal.truthvalue_, 0);
       pp(_sentgoal.budget_, 0);
       render("!");
       if (_i_ > 0) render(_R_PAREN);
    }
  }

  private static void pp(com.googlecode.opennars.parser.loan.Loan.Absyn.Budget foo, int _i_)
  {
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetE)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetE _budgete = (com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetE) foo;
       if (_i_ > 0) render(_L_PAREN);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetP)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetP _budgetp = (com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetP) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("@budget");
       render("(");
       pp(_budgetp.double_, 0);
       render(")");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetPD)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetPD _budgetpd = (com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetPD) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("@budget");
       render("(");
       pp(_budgetpd.double_1, 0);
       render(";");
       pp(_budgetpd.double_2, 0);
       render(")");
       if (_i_ > 0) render(_R_PAREN);
    }
  }

  private static void pp(com.googlecode.opennars.parser.loan.Loan.Absyn.Stm foo, int _i_)
  {
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpl)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpl _stmimpl = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpl) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_stmimpl.stm_1, 0);
       render("==>");
       pp(_stmimpl.stm_2, 1);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmEquiv)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmEquiv _stmequiv = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmEquiv) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_stmequiv.stm_1, 0);
       render("<=>");
       pp(_stmequiv.stm_2, 1);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpPred)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpPred _stmimppred = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpPred) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_stmimppred.stm_1, 0);
       render("=/>");
       pp(_stmimppred.stm_2, 1);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpRet)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpRet _stmimpret = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpRet) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_stmimpret.stm_1, 0);
       render("=\\>");
       pp(_stmimpret.stm_2, 1);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpConc)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpConc _stmimpconc = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpConc) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_stmimpconc.stm_1, 0);
       render("=|>");
       pp(_stmimpconc.stm_2, 1);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvPred)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvPred _stmeqvpred = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvPred) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_stmeqvpred.stm_1, 0);
       render("</>");
       pp(_stmeqvpred.stm_2, 1);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvConc)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvConc _stmeqvconc = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvConc) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_stmeqvconc.stm_1, 0);
       render("<|>");
       pp(_stmeqvconc.stm_2, 1);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmConj)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmConj _stmconj = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmConj) foo;
       if (_i_ > 1) render(_L_PAREN);
       pp(_stmconj.stm_1, 1);
       render("&&");
       pp(_stmconj.stm_2, 2);
       if (_i_ > 1) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmDisj)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmDisj _stmdisj = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmDisj) foo;
       if (_i_ > 1) render(_L_PAREN);
       pp(_stmdisj.stm_1, 1);
       render("||");
       pp(_stmdisj.stm_2, 2);
       if (_i_ > 1) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmPar)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmPar _stmpar = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmPar) foo;
       if (_i_ > 1) render(_L_PAREN);
       pp(_stmpar.stm_1, 1);
       render(";");
       pp(_stmpar.stm_2, 2);
       if (_i_ > 1) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmSeq)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmSeq _stmseq = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmSeq) foo;
       if (_i_ > 1) render(_L_PAREN);
       pp(_stmseq.stm_1, 1);
       render(",");
       pp(_stmseq.stm_2, 2);
       if (_i_ > 1) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmNot)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmNot _stmnot = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmNot) foo;
       if (_i_ > 2) render(_L_PAREN);
       render("not");
       pp(_stmnot.stm_, 3);
       if (_i_ > 2) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmPst)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmPst _stmpst = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmPst) foo;
       if (_i_ > 2) render(_L_PAREN);
       render("past");
       pp(_stmpst.stm_, 3);
       if (_i_ > 2) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmPres)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmPres _stmpres = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmPres) foo;
       if (_i_ > 2) render(_L_PAREN);
       render("present");
       pp(_stmpres.stm_, 3);
       if (_i_ > 2) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmFut)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmFut _stmfut = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmFut) foo;
       if (_i_ > 2) render(_L_PAREN);
       render("future");
       pp(_stmfut.stm_, 3);
       if (_i_ > 2) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmInher)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmInher _stminher = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmInher) foo;
       if (_i_ > 3) render(_L_PAREN);
       pp(_stminher.term_1, 0);
       render("-->");
       pp(_stminher.term_2, 0);
       if (_i_ > 3) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmSim)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmSim _stmsim = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmSim) foo;
       if (_i_ > 3) render(_L_PAREN);
       pp(_stmsim.term_1, 0);
       render("<->");
       pp(_stmsim.term_2, 0);
       if (_i_ > 3) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmInst)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmInst _stminst = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmInst) foo;
       if (_i_ > 3) render(_L_PAREN);
       pp(_stminst.term_1, 0);
       render("}->");
       pp(_stminst.term_2, 0);
       if (_i_ > 3) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmProp)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmProp _stmprop = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmProp) foo;
       if (_i_ > 3) render(_L_PAREN);
       pp(_stmprop.term_1, 0);
       render("--[");
       pp(_stmprop.term_2, 0);
       if (_i_ > 3) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmInPp)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmInPp _stminpp = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmInPp) foo;
       if (_i_ > 3) render(_L_PAREN);
       pp(_stminpp.term_1, 0);
       render("}-[");
       pp(_stminpp.term_2, 0);
       if (_i_ > 3) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmOp)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmOp _stmop = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmOp) foo;
       if (_i_ > 3) render(_L_PAREN);
       pp(_stmop.term_, 0);
       render("(");
       pp(_stmop.listterm_, 0);
       render(")");
       if (_i_ > 3) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmTrm)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmTrm _stmtrm = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmTrm) foo;
       if (_i_ > 3) render(_L_PAREN);
       pp(_stmtrm.term_, 0);
       if (_i_ > 3) render(_R_PAREN);
    }
  }

  private static void pp(com.googlecode.opennars.parser.loan.Loan.Absyn.Term foo, int _i_)
  {
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExInt)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExInt _trmexint = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExInt) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_trmexint.term_1, 0);
       render("&");
       pp(_trmexint.term_2, 1);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInInt)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInInt _trminint = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInInt) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_trminint.term_1, 0);
       render("|");
       pp(_trminint.term_2, 1);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExDif)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExDif _trmexdif = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExDif) foo;
       if (_i_ > 1) render(_L_PAREN);
       pp(_trmexdif.term_1, 1);
       render("-");
       pp(_trmexdif.term_2, 2);
       if (_i_ > 1) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInDif)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInDif _trmindif = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInDif) foo;
       if (_i_ > 1) render(_L_PAREN);
       pp(_trmindif.term_1, 1);
       render("~");
       pp(_trmindif.term_2, 2);
       if (_i_ > 1) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExImg)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExImg _trmeximg = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExImg) foo;
       if (_i_ > 2) render(_L_PAREN);
       pp(_trmeximg.term_, 0);
       render("(");
       pp(_trmeximg.listterm_1, 0);
       render("/");
       pp(_trmeximg.listterm_2, 0);
       render(")");
       if (_i_ > 2) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInImg)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInImg _trminimg = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInImg) foo;
       if (_i_ > 2) render(_L_PAREN);
       pp(_trminimg.term_, 0);
       render("(");
       pp(_trminimg.listterm_1, 0);
       render("\\");
       pp(_trminimg.listterm_2, 0);
       render(")");
       if (_i_ > 2) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExSet)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExSet _trmexset = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExSet) foo;
       if (_i_ > 3) render(_L_PAREN);
       render("{");
       pp(_trmexset.listterm_, 0);
       render("}");
       if (_i_ > 3) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInSet)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInSet _trminset = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInSet) foo;
       if (_i_ > 3) render(_L_PAREN);
       render("[");
       pp(_trminset.listterm_, 0);
       render("]");
       if (_i_ > 3) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmProd)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmProd _trmprod = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmProd) foo;
       if (_i_ > 3) render(_L_PAREN);
       render("(");
       pp(_trmprod.listterm_, 0);
       render(")");
       if (_i_ > 3) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmLit)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmLit _trmlit = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmLit) foo;
       if (_i_ > 3) render(_L_PAREN);
       pp(_trmlit.literal_, 0);
       if (_i_ > 3) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmStm)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmStm _trmstm = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmStm) foo;
       if (_i_ > 3) render(_L_PAREN);
       render("(");
       pp(_trmstm.stm_, 0);
       render(")");
       if (_i_ > 3) render(_R_PAREN);
    }
  }

  private static void pp(com.googlecode.opennars.parser.loan.Loan.Absyn.ListTerm foo, int _i_)
  {
     for (java.util.Iterator<Term> it = foo.iterator(); it.hasNext();)
     {
       pp(it.next(), 0);
       if (it.hasNext()) {
         render(",");
       } else {
         render("");
       }
     }
  }

  private static void pp(com.googlecode.opennars.parser.loan.Loan.Absyn.ListIdent foo, int _i_)
  {
     for (java.util.Iterator<String> it = foo.iterator(); it.hasNext();)
     {
       pp(it.next(), 0);
       if (it.hasNext()) {
         render(",");
       } else {
         render("");
       }
     }
  }

  private static void pp(com.googlecode.opennars.parser.loan.Loan.Absyn.URIRef foo, int _i_)
  {
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.URIFul)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.URIFul _uriful = (com.googlecode.opennars.parser.loan.Loan.Absyn.URIFul) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_uriful.urilit_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.URICur)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.URICur _uricur = (com.googlecode.opennars.parser.loan.Loan.Absyn.URICur) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_uricur.nsprefix_, 0);
       pp(_uricur.ident_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
  }

  private static void pp(com.googlecode.opennars.parser.loan.Loan.Absyn.Literal foo, int _i_)
  {
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVar)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVar _litqvar = (com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVar) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("?");
       pp(_litqvar.ident_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVarAn)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVarAn _litqvaran = (com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVarAn) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("?");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarD)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarD _litsvard = (com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarD) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("#");
       pp(_litsvard.ident_, 0);
       render("(");
       pp(_litsvard.listident_, 0);
       render(")");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarI)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarI _litsvari = (com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarI) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("#");
       pp(_litsvari.ident_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.LitURI)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.LitURI _lituri = (com.googlecode.opennars.parser.loan.Loan.Absyn.LitURI) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_lituri.uriref_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.LitInt)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.LitInt _litint = (com.googlecode.opennars.parser.loan.Loan.Absyn.LitInt) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_litint.integer_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.LitDbl)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.LitDbl _litdbl = (com.googlecode.opennars.parser.loan.Loan.Absyn.LitDbl) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_litdbl.double_, 0);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.LitString)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.LitString _litstring = (com.googlecode.opennars.parser.loan.Loan.Absyn.LitString) foo;
       if (_i_ > 0) render(_L_PAREN);
       printQuoted(_litstring.string_);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.LitTrue)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.LitTrue _littrue = (com.googlecode.opennars.parser.loan.Loan.Absyn.LitTrue) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("true");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.LitFalse)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.LitFalse _litfalse = (com.googlecode.opennars.parser.loan.Loan.Absyn.LitFalse) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("false");
       if (_i_ > 0) render(_R_PAREN);
    }
  }

  private static void pp(com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix foo, int _i_)
  {
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix1)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix1 _nsprefix1 = (com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix1) foo;
       if (_i_ > 0) render(_L_PAREN);
       pp(_nsprefix1.ident_, 0);
       render(":");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix2)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix2 _nsprefix2 = (com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix2) foo;
       if (_i_ > 0) render(_L_PAREN);
       render(":");
       if (_i_ > 0) render(_R_PAREN);
    }
  }

  private static void pp(com.googlecode.opennars.parser.loan.Loan.Absyn.TruthValue foo, int _i_)
  {
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TruthE)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TruthE _truthe = (com.googlecode.opennars.parser.loan.Loan.Absyn.TruthE) foo;
       if (_i_ > 0) render(_L_PAREN);
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TruthF)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TruthF _truthf = (com.googlecode.opennars.parser.loan.Loan.Absyn.TruthF) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("%");
       pp(_truthf.double_, 0);
       render("%");
       if (_i_ > 0) render(_R_PAREN);
    }
    else     if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TruthFC)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TruthFC _truthfc = (com.googlecode.opennars.parser.loan.Loan.Absyn.TruthFC) foo;
       if (_i_ > 0) render(_L_PAREN);
       render("%");
       pp(_truthfc.double_1, 0);
       render(";");
       pp(_truthfc.double_2, 0);
       render("%");
       if (_i_ > 0) render(_R_PAREN);
    }
  }


  private static void sh(com.googlecode.opennars.parser.loan.Loan.Absyn.Document foo)
  {
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.DocBR)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.DocBR _docbr = (com.googlecode.opennars.parser.loan.Loan.Absyn.DocBR) foo;
       render("(");
       render("DocBR");
       sh(_docbr.baserule_);
       render("[");
       sh(_docbr.listsentence_);
       render("]");
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.Doc)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.Doc _doc = (com.googlecode.opennars.parser.loan.Loan.Absyn.Doc) foo;
       render("(");
       render("Doc");
       render("[");
       sh(_doc.listsentence_);
       render("]");
       render(")");
    }
  }

  private static void sh(com.googlecode.opennars.parser.loan.Loan.Absyn.BaseRule foo)
  {
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.BaseR)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.BaseR _baser = (com.googlecode.opennars.parser.loan.Loan.Absyn.BaseR) foo;
       render("(");
       render("BaseR");
       sh(_baser.urilit_);
       render(")");
    }
  }

  private static void sh(com.googlecode.opennars.parser.loan.Loan.Absyn.ListSentence foo)
  {
     for (java.util.Iterator<Sentence> it = foo.iterator(); it.hasNext();)
     {
       sh(it.next());
       if (it.hasNext())
         render(",");
     }
  }

  private static void sh(com.googlecode.opennars.parser.loan.Loan.Absyn.Sentence foo)
  {
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.SentPrefix)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.SentPrefix _sentprefix = (com.googlecode.opennars.parser.loan.Loan.Absyn.SentPrefix) foo;
       render("(");
       render("SentPrefix");
       sh(_sentprefix.nsprefix_);
       sh(_sentprefix.urilit_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.SentImport)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.SentImport _sentimport = (com.googlecode.opennars.parser.loan.Loan.Absyn.SentImport) foo;
       render("(");
       render("SentImport");
       sh(_sentimport.urilit_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.SentDelay)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.SentDelay _sentdelay = (com.googlecode.opennars.parser.loan.Loan.Absyn.SentDelay) foo;
       render("(");
       render("SentDelay");
       sh(_sentdelay.integer_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.SentOp)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.SentOp _sentop = (com.googlecode.opennars.parser.loan.Loan.Absyn.SentOp) foo;
       render("(");
       render("SentOp");
       sh(_sentop.uriref_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.SentJudge)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.SentJudge _sentjudge = (com.googlecode.opennars.parser.loan.Loan.Absyn.SentJudge) foo;
       render("(");
       render("SentJudge");
       sh(_sentjudge.stm_);
       sh(_sentjudge.truthvalue_);
       sh(_sentjudge.budget_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.SentQuest)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.SentQuest _sentquest = (com.googlecode.opennars.parser.loan.Loan.Absyn.SentQuest) foo;
       render("(");
       render("SentQuest");
       sh(_sentquest.stm_);
       sh(_sentquest.budget_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.SentGoal)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.SentGoal _sentgoal = (com.googlecode.opennars.parser.loan.Loan.Absyn.SentGoal) foo;
       render("(");
       render("SentGoal");
       sh(_sentgoal.stm_);
       sh(_sentgoal.truthvalue_);
       sh(_sentgoal.budget_);
       render(")");
    }
  }

  private static void sh(com.googlecode.opennars.parser.loan.Loan.Absyn.Budget foo)
  {
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetE)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetE _budgete = (com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetE) foo;
       render("BudgetE");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetP)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetP _budgetp = (com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetP) foo;
       render("(");
       render("BudgetP");
       sh(_budgetp.double_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetPD)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetPD _budgetpd = (com.googlecode.opennars.parser.loan.Loan.Absyn.BudgetPD) foo;
       render("(");
       render("BudgetPD");
       sh(_budgetpd.double_1);
       sh(_budgetpd.double_2);
       render(")");
    }
  }

  private static void sh(com.googlecode.opennars.parser.loan.Loan.Absyn.Stm foo)
  {
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpl)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpl _stmimpl = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpl) foo;
       render("(");
       render("StmImpl");
       sh(_stmimpl.stm_1);
       sh(_stmimpl.stm_2);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmEquiv)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmEquiv _stmequiv = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmEquiv) foo;
       render("(");
       render("StmEquiv");
       sh(_stmequiv.stm_1);
       sh(_stmequiv.stm_2);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpPred)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpPred _stmimppred = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpPred) foo;
       render("(");
       render("StmImpPred");
       sh(_stmimppred.stm_1);
       sh(_stmimppred.stm_2);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpRet)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpRet _stmimpret = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpRet) foo;
       render("(");
       render("StmImpRet");
       sh(_stmimpret.stm_1);
       sh(_stmimpret.stm_2);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpConc)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpConc _stmimpconc = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmImpConc) foo;
       render("(");
       render("StmImpConc");
       sh(_stmimpconc.stm_1);
       sh(_stmimpconc.stm_2);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvPred)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvPred _stmeqvpred = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvPred) foo;
       render("(");
       render("StmEqvPred");
       sh(_stmeqvpred.stm_1);
       sh(_stmeqvpred.stm_2);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvConc)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvConc _stmeqvconc = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmEqvConc) foo;
       render("(");
       render("StmEqvConc");
       sh(_stmeqvconc.stm_1);
       sh(_stmeqvconc.stm_2);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmConj)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmConj _stmconj = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmConj) foo;
       render("(");
       render("StmConj");
       sh(_stmconj.stm_1);
       sh(_stmconj.stm_2);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmDisj)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmDisj _stmdisj = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmDisj) foo;
       render("(");
       render("StmDisj");
       sh(_stmdisj.stm_1);
       sh(_stmdisj.stm_2);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmPar)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmPar _stmpar = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmPar) foo;
       render("(");
       render("StmPar");
       sh(_stmpar.stm_1);
       sh(_stmpar.stm_2);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmSeq)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmSeq _stmseq = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmSeq) foo;
       render("(");
       render("StmSeq");
       sh(_stmseq.stm_1);
       sh(_stmseq.stm_2);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmNot)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmNot _stmnot = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmNot) foo;
       render("(");
       render("StmNot");
       sh(_stmnot.stm_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmPst)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmPst _stmpst = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmPst) foo;
       render("(");
       render("StmPst");
       sh(_stmpst.stm_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmPres)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmPres _stmpres = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmPres) foo;
       render("(");
       render("StmPres");
       sh(_stmpres.stm_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmFut)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmFut _stmfut = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmFut) foo;
       render("(");
       render("StmFut");
       sh(_stmfut.stm_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmInher)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmInher _stminher = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmInher) foo;
       render("(");
       render("StmInher");
       sh(_stminher.term_1);
       sh(_stminher.term_2);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmSim)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmSim _stmsim = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmSim) foo;
       render("(");
       render("StmSim");
       sh(_stmsim.term_1);
       sh(_stmsim.term_2);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmInst)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmInst _stminst = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmInst) foo;
       render("(");
       render("StmInst");
       sh(_stminst.term_1);
       sh(_stminst.term_2);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmProp)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmProp _stmprop = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmProp) foo;
       render("(");
       render("StmProp");
       sh(_stmprop.term_1);
       sh(_stmprop.term_2);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmInPp)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmInPp _stminpp = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmInPp) foo;
       render("(");
       render("StmInPp");
       sh(_stminpp.term_1);
       sh(_stminpp.term_2);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmOp)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmOp _stmop = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmOp) foo;
       render("(");
       render("StmOp");
       sh(_stmop.term_);
       render("[");
       sh(_stmop.listterm_);
       render("]");
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.StmTrm)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.StmTrm _stmtrm = (com.googlecode.opennars.parser.loan.Loan.Absyn.StmTrm) foo;
       render("(");
       render("StmTrm");
       sh(_stmtrm.term_);
       render(")");
    }
  }

  private static void sh(com.googlecode.opennars.parser.loan.Loan.Absyn.Term foo)
  {
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExInt)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExInt _trmexint = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExInt) foo;
       render("(");
       render("TrmExInt");
       sh(_trmexint.term_1);
       sh(_trmexint.term_2);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInInt)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInInt _trminint = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInInt) foo;
       render("(");
       render("TrmInInt");
       sh(_trminint.term_1);
       sh(_trminint.term_2);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExDif)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExDif _trmexdif = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExDif) foo;
       render("(");
       render("TrmExDif");
       sh(_trmexdif.term_1);
       sh(_trmexdif.term_2);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInDif)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInDif _trmindif = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInDif) foo;
       render("(");
       render("TrmInDif");
       sh(_trmindif.term_1);
       sh(_trmindif.term_2);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExImg)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExImg _trmeximg = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExImg) foo;
       render("(");
       render("TrmExImg");
       sh(_trmeximg.term_);
       render("[");
       sh(_trmeximg.listterm_1);
       render("]");
       render("[");
       sh(_trmeximg.listterm_2);
       render("]");
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInImg)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInImg _trminimg = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInImg) foo;
       render("(");
       render("TrmInImg");
       sh(_trminimg.term_);
       render("[");
       sh(_trminimg.listterm_1);
       render("]");
       render("[");
       sh(_trminimg.listterm_2);
       render("]");
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExSet)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExSet _trmexset = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmExSet) foo;
       render("(");
       render("TrmExSet");
       render("[");
       sh(_trmexset.listterm_);
       render("]");
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInSet)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInSet _trminset = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmInSet) foo;
       render("(");
       render("TrmInSet");
       render("[");
       sh(_trminset.listterm_);
       render("]");
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmProd)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmProd _trmprod = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmProd) foo;
       render("(");
       render("TrmProd");
       render("[");
       sh(_trmprod.listterm_);
       render("]");
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmLit)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmLit _trmlit = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmLit) foo;
       render("(");
       render("TrmLit");
       sh(_trmlit.literal_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TrmStm)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TrmStm _trmstm = (com.googlecode.opennars.parser.loan.Loan.Absyn.TrmStm) foo;
       render("(");
       render("TrmStm");
       sh(_trmstm.stm_);
       render(")");
    }
  }

  private static void sh(com.googlecode.opennars.parser.loan.Loan.Absyn.ListTerm foo)
  {
     for (java.util.Iterator<Term> it = foo.iterator(); it.hasNext();)
     {
       sh(it.next());
       if (it.hasNext())
         render(",");
     }
  }

  private static void sh(com.googlecode.opennars.parser.loan.Loan.Absyn.ListIdent foo)
  {
     for (java.util.Iterator<String> it = foo.iterator(); it.hasNext();)
     {
       sh(it.next());
       if (it.hasNext())
         render(",");
     }
  }

  private static void sh(com.googlecode.opennars.parser.loan.Loan.Absyn.URIRef foo)
  {
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.URIFul)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.URIFul _uriful = (com.googlecode.opennars.parser.loan.Loan.Absyn.URIFul) foo;
       render("(");
       render("URIFul");
       sh(_uriful.urilit_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.URICur)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.URICur _uricur = (com.googlecode.opennars.parser.loan.Loan.Absyn.URICur) foo;
       render("(");
       render("URICur");
       sh(_uricur.nsprefix_);
       sh(_uricur.ident_);
       render(")");
    }
  }

  private static void sh(com.googlecode.opennars.parser.loan.Loan.Absyn.Literal foo)
  {
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVar)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVar _litqvar = (com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVar) foo;
       render("(");
       render("LitQVar");
       sh(_litqvar.ident_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVarAn)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVarAn _litqvaran = (com.googlecode.opennars.parser.loan.Loan.Absyn.LitQVarAn) foo;
       render("LitQVarAn");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarD)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarD _litsvard = (com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarD) foo;
       render("(");
       render("LitSVarD");
       sh(_litsvard.ident_);
       render("[");
       sh(_litsvard.listident_);
       render("]");
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarI)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarI _litsvari = (com.googlecode.opennars.parser.loan.Loan.Absyn.LitSVarI) foo;
       render("(");
       render("LitSVarI");
       sh(_litsvari.ident_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.LitURI)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.LitURI _lituri = (com.googlecode.opennars.parser.loan.Loan.Absyn.LitURI) foo;
       render("(");
       render("LitURI");
       sh(_lituri.uriref_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.LitInt)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.LitInt _litint = (com.googlecode.opennars.parser.loan.Loan.Absyn.LitInt) foo;
       render("(");
       render("LitInt");
       sh(_litint.integer_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.LitDbl)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.LitDbl _litdbl = (com.googlecode.opennars.parser.loan.Loan.Absyn.LitDbl) foo;
       render("(");
       render("LitDbl");
       sh(_litdbl.double_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.LitString)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.LitString _litstring = (com.googlecode.opennars.parser.loan.Loan.Absyn.LitString) foo;
       render("(");
       render("LitString");
       sh(_litstring.string_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.LitTrue)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.LitTrue _littrue = (com.googlecode.opennars.parser.loan.Loan.Absyn.LitTrue) foo;
       render("LitTrue");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.LitFalse)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.LitFalse _litfalse = (com.googlecode.opennars.parser.loan.Loan.Absyn.LitFalse) foo;
       render("LitFalse");
    }
  }

  private static void sh(com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix foo)
  {
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix1)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix1 _nsprefix1 = (com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix1) foo;
       render("(");
       render("NSPrefix1");
       sh(_nsprefix1.ident_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix2)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix2 _nsprefix2 = (com.googlecode.opennars.parser.loan.Loan.Absyn.NSPrefix2) foo;
       render("NSPrefix2");
    }
  }

  private static void sh(com.googlecode.opennars.parser.loan.Loan.Absyn.TruthValue foo)
  {
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TruthE)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TruthE _truthe = (com.googlecode.opennars.parser.loan.Loan.Absyn.TruthE) foo;
       render("TruthE");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TruthF)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TruthF _truthf = (com.googlecode.opennars.parser.loan.Loan.Absyn.TruthF) foo;
       render("(");
       render("TruthF");
       sh(_truthf.double_);
       render(")");
    }
    if (foo instanceof com.googlecode.opennars.parser.loan.Loan.Absyn.TruthFC)
    {
       com.googlecode.opennars.parser.loan.Loan.Absyn.TruthFC _truthfc = (com.googlecode.opennars.parser.loan.Loan.Absyn.TruthFC) foo;
       render("(");
       render("TruthFC");
       sh(_truthfc.double_1);
       sh(_truthfc.double_2);
       render(")");
    }
  }


  private static void pp(Integer n, int _i_) { buf_.append(n); buf_.append(" "); }
  private static void pp(Double d, int _i_) { buf_.append(d); buf_.append(" "); }
  private static void pp(String s, int _i_) { buf_.append(s); buf_.append(" "); }
  private static void pp(Character c, int _i_) { buf_.append("'" + c.toString() + "'"); buf_.append(" "); }
  private static void sh(Integer n) { render(n.toString()); }
  private static void sh(Double d) { render(d.toString()); }
  private static void sh(Character c) { render(c.toString()); }
  private static void sh(String s) { printQuoted(s); }
  private static void printQuoted(String s) { render("\"" + s + "\""); }
  private static void indent()
  {
    int n = _n_;
    while (n > 0)
    {
      buf_.append(" ");
      n--;
    }
  }
  private static void backup()
  {
     if (buf_.charAt(buf_.length() - 1) == ' ') {
      buf_.setLength(buf_.length() - 1);
    }
  }
  private static void trim()
  {
     while (buf_.length() > 0 && buf_.charAt(0) == ' ')
        buf_.deleteCharAt(0); 
    while (buf_.length() > 0 && buf_.charAt(buf_.length()-1) == ' ')
        buf_.deleteCharAt(buf_.length()-1);
  }
  private static int _n_ = 0;
  private static StringBuilder buf_ = new StringBuilder(INITIAL_BUFFER_SIZE);
}

