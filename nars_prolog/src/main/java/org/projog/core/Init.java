package org.projog.core;

/**
 % projog-bootstrap.pl
 % This file contains Prolog syntax that is interpreted when a projog console is started.
 % This file contains code that configures the projog environment with
 % "core" built-in predicates (e.g. "true", "consult", etc.) and numerical operations (e.g. "+", "-", etc.).
 % It also defines operators in order to provide a more convenient syntax for writing terms.
 % This file is included in the projog-core.jar that contains the projog class files.
 % This file can be overridden by providing another file named "projog-bootstrap.pl"
 % in the root directory where the console is launched, or in the classpath before the projog-core.jar.
 % See http://www.projog.org/javadoc/org/projog/core/KnowledgeBaseUtils.html#bootstrap(org.projog.core.KnowledgeBase)
 */
public class Init {

    //http://i-tools.org/code#slash
    public static String init = "\'?-\'( pj_add_predicate(\'/\'(op, 3), \'org.projog.core.function.io.Op\') ).\n\'?-\'( op(1200, fx, \'?-\') ).\n?- op(400, yfx, \'/\').\n\n% boolean\n?- pj_add_predicate(true, \'org.projog.core.function.bool.True\').\n?- pj_add_predicate(fail, \'org.projog.core.function.bool.Fail\').\n\n% classify\n?- pj_add_predicate(var/1, \'org.projog.core.function.classify.IsVar\').\n?- pj_add_predicate(nonvar/1, \'org.projog.core.function.classify.IsNonVar\').\n?- pj_add_predicate(atom/1, \'org.projog.core.function.classify.IsAtom\').\n?- pj_add_predicate(number/1, \'org.projog.core.function.classify.IsNumber\').\n?- pj_add_predicate(atomic/1, \'org.projog.core.function.classify.IsAtomic\').\n?- pj_add_predicate(integer/1, \'org.projog.core.function.classify.IsInteger\').\n?- pj_add_predicate(float/1, \'org.projog.core.function.classify.IsFloat\').\n?- pj_add_predicate(compound/1, \'org.projog.core.function.classify.IsCompound\').\n?- pj_add_predicate(is_list/1, \'org.projog.core.function.classify.IsList\').\n?- pj_add_predicate(char_type/2, \'org.projog.core.function.classify.CharType\').\n\n% compare\n?- pj_add_predicate(\'=\'/2, \'org.projog.core.function.compare.Equal\').\n?- pj_add_predicate(\'==\'/2, \'org.projog.core.function.compare.StrictEquality\').\n?- pj_add_predicate(\'=:=\'/2, \'org.projog.core.function.compare.NumericEquality\').\n?- pj_add_predicate(\'=\\=\'/2, \'org.projog.core.function.compare.NumericInequality\').\n?- pj_add_predicate(\'<\'/2, \'org.projog.core.function.compare.NumericLessThan\').\n?- pj_add_predicate(\'=<\'/2, \'org.projog.core.function.compare.NumericLessThanOrEqual\').\n?- pj_add_predicate(\'>\'/2, \'org.projog.core.function.compare.NumericGreaterThan\').\n?- pj_add_predicate(\'>=\'/2, \'org.projog.core.function.compare.NumericGreaterThanOrEqual\').\n?- pj_add_predicate(\'@<\'/2, \'org.projog.core.function.compare.TermLessThan\').\n?- pj_add_predicate(\'@>\'/2, \'org.projog.core.function.compare.TermGreaterThan\').\n?- pj_add_predicate(\'@>=\'/2, \'org.projog.core.function.compare.TermGreaterThanOrEqual\').\n?- pj_add_predicate(\'@=<\'/2, \'org.projog.core.function.compare.TermLessThanOrEqual\').\n?- pj_add_predicate(\'\\=\'/2, \'org.projog.core.function.compare.NotUnifiable\').\n?- pj_add_predicate(compare/3, \'org.projog.core.function.compare.Compare\').\n?- pj_add_predicate(predsort/3, \'org.projog.core.function.compare.PredSort\').\n?- pj_add_predicate(between/3, \'org.projog.core.function.compare.Between\').\n\n% compound\n?- pj_add_predicate(\',\'/2, \'org.projog.core.function.compound.Conjunction\').\n?- pj_add_predicate(\';\'/2, \'org.projog.core.function.compound.Disjunction\').\n?- pj_add_predicate(\'/\'(\'\\+\', 1), \'org.projog.core.function.compound.Not\').\n?- pj_add_predicate(not/1, \'org.projog.core.function.compound.Not\').\n?- pj_add_predicate(call/1, \'org.projog.core.function.compound.Call\').\n?- pj_add_predicate(time/1, \'org.projog.core.function.compound.Call\').\n?- pj_add_predicate(once/1, \'org.projog.core.function.compound.Once\').\n?- pj_add_predicate(bagof/3, \'org.projog.core.function.compound.BagOf\').\n?- pj_add_predicate(findall/3, \'org.projog.core.function.compound.FindAll\').\n?- pj_add_predicate(setof/3, \'org.projog.core.function.compound.SetOf\').\n\n% construct\n?- pj_add_predicate(functor/3, \'org.projog.core.function.construct.Functor\').\n?- pj_add_predicate(arg/3, \'org.projog.core.function.construct.Arg\').\n?- pj_add_predicate(\'=..\'/2, \'org.projog.core.function.construct.Univ\').\n?- pj_add_predicate(atom_chars/2, \'org.projog.core.function.construct.AtomChars\').\n?- pj_add_predicate(number_chars/2, \'org.projog.core.function.construct.NumberChars\').\n?- pj_add_predicate(atom_concat/3, \'org.projog.core.function.construct.AtomConcat\').\n\n% debug\n?- pj_add_predicate(debugging, \'org.projog.core.function.debug.Debugging\').\n?- pj_add_predicate(nodebug, \'org.projog.core.function.debug.NoDebug\').\n?- pj_add_predicate(trace, \'org.projog.core.function.debug.Trace\').\n?- pj_add_predicate(notrace, \'org.projog.core.function.debug.NoTrace\').\n?- pj_add_predicate(spy/1, \'org.projog.core.function.debug.AlterSpyPoint/spy\').\n?- pj_add_predicate(nospy/1, \'org.projog.core.function.debug.AlterSpyPoint/noSpy\').\n\n% io\n?- pj_add_predicate(close/1, \'org.projog.core.function.io.Close\').\n?- pj_add_predicate(current_input/1, \'org.projog.core.function.io.CurrentInput\').\n?- pj_add_predicate(seeing/1, \'org.projog.core.function.io.CurrentInput\').\n?- pj_add_predicate(see/1, \'org.projog.core.function.io.See\').\n?- pj_add_predicate(seen, \'org.projog.core.function.io.Seen\').\n?- pj_add_predicate(tell/1, \'org.projog.core.function.io.Tell\').\n?- pj_add_predicate(told, \'org.projog.core.function.io.Told\').\n?- pj_add_predicate(current_output/1, \'org.projog.core.function.io.CurrentOutput\').\n?- pj_add_predicate(get_char/1, \'org.projog.core.function.io.GetChar\').\n?- pj_add_predicate(nl, \'org.projog.core.function.io.NewLine\').\n?- pj_add_predicate(open/3, \'org.projog.core.function.io.Open\').\n?- pj_add_predicate(put_char/1, \'org.projog.core.function.io.PutChar\').\n?- pj_add_predicate(read/1, \'org.projog.core.function.io.Read\').\n?- pj_add_predicate(set_input/1, \'org.projog.core.function.io.SetInput\').\n?- pj_add_predicate(set_output/1, \'org.projog.core.function.io.SetOutput\').\n?- pj_add_predicate(write/1, \'org.projog.core.function.io.Write\').\n?- pj_add_predicate(write_canonical/1, \'org.projog.core.function.io.WriteCanonical\').\n?- pj_add_predicate(writef/2, \'org.projog.core.function.io.Writef\').\n?- pj_add_predicate(writef/1, \'org.projog.core.function.io.Writef\').\n\n% kb (knowledge base)\n?- pj_add_predicate(pj_add_calculatable/2, \'org.projog.core.function.kb.AddCalculatable\').\n?- pj_add_predicate(asserta/1, \'org.projog.core.function.kb.Assert/assertA\').\n?- pj_add_predicate(assertz/1, \'org.projog.core.function.kb.Assert/assertZ\').\n?- pj_add_predicate(assert/1, \'org.projog.core.function.kb.Assert/assertZ\').\n?- pj_add_predicate(listing/1, \'org.projog.core.function.kb.Listing\').\n?- pj_add_predicate(clause/2, \'org.projog.core.function.kb.Inspect/inspectClause\').\n?- pj_add_predicate(retract/1, \'org.projog.core.function.kb.Inspect/retract\').\n?- pj_add_predicate(retractall/1, \'org.projog.core.function.kb.RetractAll\').\n?- pj_add_predicate(consult/1, \'org.projog.core.function.kb.Consult\').\n?- pj_add_predicate(ensure_loaded/1, \'org.projog.core.function.kb.EnsureLoaded\').\n?- pj_add_predicate(flag/3, \'org.projog.core.function.kb.Flag\').\n\n% db (recorded database)\n?- pj_add_predicate(erase/1, \'org.projog.core.function.db.Erase\').\n?- pj_add_predicate(recorded/2, \'org.projog.core.function.db.Recorded\').\n?- pj_add_predicate(recorded/3, \'org.projog.core.function.db.Recorded\').\n?- pj_add_predicate(recorda/2, \'org.projog.core.function.db.InsertRecord/recordA\').\n?- pj_add_predicate(recorda/3, \'org.projog.core.function.db.InsertRecord/recordA\').\n?- pj_add_predicate(recordz/2, \'org.projog.core.function.db.InsertRecord/recordZ\').\n?- pj_add_predicate(recordz/3, \'org.projog.core.function.db.InsertRecord/recordZ\').\n \n% math\n?- pj_add_predicate(is/2, \'org.projog.core.function.math.Is\').\n\n% flow control\n?- pj_add_predicate(repeat, \'org.projog.core.function.flow.RepeatInfinitely\').\n?- pj_add_predicate(repeat/1, \'org.projog.core.function.flow.RepeatSetAmount\').\n?- pj_add_predicate(\'!\', \'org.projog.core.function.flow.Cut\').\n\n% list\n?- pj_add_predicate(length/2, \'org.projog.core.function.list.Length\').\n?- pj_add_predicate(reverse/2, \'org.projog.core.function.list.Reverse\').\n?- pj_add_predicate(member/2, \'org.projog.core.function.list.Member\').\n?- pj_add_predicate(memberchk/2, \'org.projog.core.function.list.MemberCheck\').\n?- pj_add_predicate(append/3, \'org.projog.core.function.list.Append\').\n?- pj_add_predicate(subtract/3, \'org.projog.core.function.list.SubtractFromList\').\n?- pj_add_predicate(keysort/2, \'org.projog.core.function.list.KeySort\').\n?- pj_add_predicate(flatten/2, \'org.projog.core.function.list.Flatten\').\n?- pj_add_predicate(sort/2, \'org.projog.core.function.list.SortAsSet\').\n?- pj_add_predicate(msort/2, \'org.projog.core.function.list.Sort\').\n?- pj_add_predicate(delete/3, \'org.projog.core.function.list.Delete\').\n?- pj_add_predicate(subset/2, \'org.projog.core.function.list.Subset\').\n?- pj_add_predicate(select/3, \'org.projog.core.function.list.Select\').\n?- pj_add_predicate(nth0/3, \'org.projog.core.function.list.Nth/nth0\').\n?- pj_add_predicate(nth1/3, \'org.projog.core.function.list.Nth/nth1\').\n?- pj_add_predicate(nth/3, \'org.projog.core.function.list.Nth/nth1\').\n?- pj_add_predicate(maplist/2, \'org.projog.core.function.list.MapList\').\n?- pj_add_predicate(checklist/2, \'org.projog.core.function.list.MapList\').\n?- pj_add_predicate(maplist/3, \'org.projog.core.function.list.MapList\').\n?- pj_add_predicate(checklist/3, \'org.projog.core.function.list.MapList\').\n?- pj_add_predicate(include/3, \'org.projog.core.function.list.SubList\').\n?- pj_add_predicate(sublist/3, \'org.projog.core.function.list.SubList\').\n \n% time\n?- pj_add_predicate(get_time/1, \'org.projog.core.function.time.GetTime\').\n?- pj_add_predicate(convert_time/2, \'org.projog.core.function.time.ConvertTime\').\n\n% numerical operations\n?- pj_add_predicate(arithmetic_function/1, \'org.projog.core.function.math.AddArithmeticFunction\').\n?- pj_add_calculatable(\'+\'/2, \'org.projog.core.function.math.Add\').\n?- pj_add_calculatable(\'/\'(\'-\', 1), \'org.projog.core.function.math.Minus\').\n?- pj_add_calculatable(\'/\'(\'-\', 2), \'org.projog.core.function.math.Subtract\').\n?- pj_add_calculatable(\'/\'/2, \'org.projog.core.function.math.Divide\').\n?- pj_add_calculatable(\'//\'/2, \'org.projog.core.function.math.IntegerDivide\').\n?- pj_add_calculatable(\'*\'/2, \'org.projog.core.function.math.Multiply\').\n?- pj_add_calculatable(\'**\'/2, \'org.projog.core.function.math.Power\').\n?- pj_add_calculatable(mod/2, \'org.projog.core.function.math.Modulo\').\n?- pj_add_calculatable(rem/2, \'org.projog.core.function.math.Remainder\').\n?- pj_add_calculatable(random/1, \'org.projog.core.function.math.Random\').\n?- pj_add_calculatable(\'/\\\'/2, \'org.projog.core.function.math.BitwiseAnd\').\n?- pj_add_calculatable(\'>>\'/2, \'org.projog.core.function.math.ShiftRight\').\n?- pj_add_calculatable(max/2, \'org.projog.core.function.math.Max\').\n?- pj_add_calculatable(abs/1, \'org.projog.core.function.math.Abs\').\n\n% definite clause grammers (DCG)\n?- op(1200, xfx, \'-->\').\n?- op(901, fx, \'{\').\n?- op(900, xf, \'}\').\n\n% operators\n?- op(1200, xfx, \':-\').\n?- op(1200, fx, \':-\').\n?- op(1100, fx, dynamic).\n?- op(1100, xfy, \';\').\n?- op(1000, xfy, \',\').\n?- op(900, fy, \'\\+\').\n?- op(700, xfx, \'=\').\n?- op(700, xfx, \'==\').\n?- op(700, xfx, \'=:=\').\n?- op(700, xfx, \'=\\=\').\n?- op(700, xfx, \'=..\').\n?- op(700, xfx, \'<\').\n?- op(700, xfx, \'>\').\n?- op(700, xfx, \'=<\').\n?- op(700, xfx, \'>=\').\n?- op(700, xfx, \'@<\').\n?- op(700, xfx, \'@=<\').\n?- op(700, xfx, \'@>\').\n?- op(700, xfx, \'@>=\').\n?- op(700, xfx, \'\\=\').\n?- op(700, xfx, is).\n?- op(500, yfx, \'+\').\n?- op(500, yfx, \'-\').\n?- op(400, yfx, \'*\').\n?- op(400, yfx, \'**\').\n?- op(400, yfx, \'//\').\n?- op(400, yfx, mod).\n?- op(400, yfx, rem).\n?- op(400, yfx, \'/\\\').\n?- op(400, yfx, \'>>\').\n?- op(200, fy, \'-\').";

    /*

//% classify
?- pj_add_predicate(var/1, 'org.projog.core.function.classify.IsVar').
?- pj_add_predicate(nonvar/1, 'org.projog.core.function.classify.IsNonVar').
?- pj_add_predicate(atom/1, 'org.projog.core.function.classify.IsAtom').
?- pj_add_predicate(number/1, 'org.projog.core.function.classify.IsNumber').
?- pj_add_predicate(atomic/1, 'org.projog.core.function.classify.IsAtomic').
?- pj_add_predicate(integer/1, 'org.projog.core.function.classify.IsInteger').
?- pj_add_predicate(float/1, 'org.projog.core.function.classify.IsFloat').
?- pj_add_predicate(compound/1, 'org.projog.core.function.classify.IsCompound').
?- pj_add_predicate(is_list/1, 'org.projog.core.function.classify.IsList').
?- pj_add_predicate(char_type/2, 'org.projog.core.function.classify.CharType').

//% compare
?- pj_add_predicate('='/2, 'org.projog.core.function.compare.Equal').
?- pj_add_predicate('=='/2, 'org.projog.core.function.compare.StrictEquality').
?- pj_add_predicate('=:='/2, 'org.projog.core.function.compare.NumericEquality').
?- pj_add_predicate('=\='/2, 'org.projog.core.function.compare.NumericInequality').
?- pj_add_predicate('<'/2, 'org.projog.core.function.compare.NumericLessThan').
?- pj_add_predicate('=<'/2, 'org.projog.core.function.compare.NumericLessThanOrEqual').
?- pj_add_predicate('>'/2, 'org.projog.core.function.compare.NumericGreaterThan').
?- pj_add_predicate('>='/2, 'org.projog.core.function.compare.NumericGreaterThanOrEqual').
?- pj_add_predicate('@<'/2, 'org.projog.core.function.compare.TermLessThan').
?- pj_add_predicate('@>'/2, 'org.projog.core.function.compare.TermGreaterThan').
?- pj_add_predicate('@>='/2, 'org.projog.core.function.compare.TermGreaterThanOrEqual').
?- pj_add_predicate('@=<'/2, 'org.projog.core.function.compare.TermLessThanOrEqual').
?- pj_add_predicate('\='/2, 'org.projog.core.function.compare.NotUnifiable').
?- pj_add_predicate(compare/3, 'org.projog.core.function.compare.Compare').
?- pj_add_predicate(predsort/3, 'org.projog.core.function.compare.PredSort').
?- pj_add_predicate(between/3, 'org.projog.core.function.compare.Between').

//% compound
?- pj_add_predicate(','/2, 'org.projog.core.function.compound.Conjunction').
?- pj_add_predicate(';'/2, 'org.projog.core.function.compound.Disjunction').
?- pj_add_predicate('/'('\+', 1), 'org.projog.core.function.compound.Not').
?- pj_add_predicate(not/1, 'org.projog.core.function.compound.Not').
?- pj_add_predicate(call/1, 'org.projog.core.function.compound.Call').
?- pj_add_predicate(time/1, 'org.projog.core.function.compound.Call').
?- pj_add_predicate(once/1, 'org.projog.core.function.compound.Once').
?- pj_add_predicate(bagof/3, 'org.projog.core.function.compound.BagOf').
?- pj_add_predicate(findall/3, 'org.projog.core.function.compound.FindAll').
?- pj_add_predicate(setof/3, 'org.projog.core.function.compound.SetOf').

//% construct
?- pj_add_predicate(functor/3, 'org.projog.core.function.construct.Functor').
?- pj_add_predicate(arg/3, 'org.projog.core.function.construct.Arg').
?- pj_add_predicate('=..'/2, 'org.projog.core.function.construct.Univ').
?- pj_add_predicate(atom_chars/2, 'org.projog.core.function.construct.AtomChars').
?- pj_add_predicate(number_chars/2, 'org.projog.core.function.construct.NumberChars').
?- pj_add_predicate(atom_concat/3, 'org.projog.core.function.construct.AtomConcat').

//% debug
?- pj_add_predicate(debugging, 'org.projog.core.function.debug.Debugging').
?- pj_add_predicate(nodebug, 'org.projog.core.function.debug.NoDebug').
?- pj_add_predicate(trace, 'org.projog.core.function.debug.Trace').
?- pj_add_predicate(notrace, 'org.projog.core.function.debug.NoTrace').
?- pj_add_predicate(spy/1, 'org.projog.core.function.debug.AlterSpyPoint/spy').
?- pj_add_predicate(nospy/1, 'org.projog.core.function.debug.AlterSpyPoint/noSpy').

//% io
?- pj_add_predicate(close/1, 'org.projog.core.function.io.Close').
?- pj_add_predicate(current_input/1, 'org.projog.core.function.io.CurrentInput').
?- pj_add_predicate(seeing/1, 'org.projog.core.function.io.CurrentInput').
?- pj_add_predicate(see/1, 'org.projog.core.function.io.See').
?- pj_add_predicate(seen, 'org.projog.core.function.io.Seen').
?- pj_add_predicate(tell/1, 'org.projog.core.function.io.Tell').
?- pj_add_predicate(told, 'org.projog.core.function.io.Told').
?- pj_add_predicate(current_output/1, 'org.projog.core.function.io.CurrentOutput').
?- pj_add_predicate(get_char/1, 'org.projog.core.function.io.GetChar').
?- pj_add_predicate(nl, 'org.projog.core.function.io.NewLine').
?- pj_add_predicate(open/3, 'org.projog.core.function.io.Open').
?- pj_add_predicate(put_char/1, 'org.projog.core.function.io.PutChar').
?- pj_add_predicate(read/1, 'org.projog.core.function.io.Read').
?- pj_add_predicate(set_input/1, 'org.projog.core.function.io.SetInput').
?- pj_add_predicate(set_output/1, 'org.projog.core.function.io.SetOutput').
?- pj_add_predicate(write/1, 'org.projog.core.function.io.Write').
?- pj_add_predicate(write_canonical/1, 'org.projog.core.function.io.WriteCanonical').
?- pj_add_predicate(writef/2, 'org.projog.core.function.io.Writef').
?- pj_add_predicate(writef/1, 'org.projog.core.function.io.Writef').

//% kb (knowledge base)
?- pj_add_predicate(pj_add_calculatable/2, 'org.projog.core.function.kb.AddCalculatable').
?- pj_add_predicate(asserta/1, 'org.projog.core.function.kb.Assert/assertA').
?- pj_add_predicate(assertz/1, 'org.projog.core.function.kb.Assert/assertZ').
?- pj_add_predicate(assert/1, 'org.projog.core.function.kb.Assert/assertZ').
?- pj_add_predicate(listing/1, 'org.projog.core.function.kb.Listing').
?- pj_add_predicate(clause/2, 'org.projog.core.function.kb.Inspect/inspectClause').
?- pj_add_predicate(retract/1, 'org.projog.core.function.kb.Inspect/retract').
?- pj_add_predicate(retractall/1, 'org.projog.core.function.kb.RetractAll').
?- pj_add_predicate(consult/1, 'org.projog.core.function.kb.Consult').
?- pj_add_predicate(ensure_loaded/1, 'org.projog.core.function.kb.EnsureLoaded').
?- pj_add_predicate(flag/3, 'org.projog.core.function.kb.Flag').

//% db (recorded database)
?- pj_add_predicate(erase/1, 'org.projog.core.function.db.Erase').
?- pj_add_predicate(recorded/2, 'org.projog.core.function.db.Recorded').
?- pj_add_predicate(recorded/3, 'org.projog.core.function.db.Recorded').
?- pj_add_predicate(recorda/2, 'org.projog.core.function.db.InsertRecord/recordA').
?- pj_add_predicate(recorda/3, 'org.projog.core.function.db.InsertRecord/recordA').
?- pj_add_predicate(recordz/2, 'org.projog.core.function.db.InsertRecord/recordZ').
?- pj_add_predicate(recordz/3, 'org.projog.core.function.db.InsertRecord/recordZ').

//% math
?- pj_add_predicate(is/2, 'org.projog.core.function.math.Is').

//% flow control
?- pj_add_predicate(repeat, 'org.projog.core.function.flow.RepeatInfinitely').
?- pj_add_predicate(repeat/1, 'org.projog.core.function.flow.RepeatSetAmount').
?- pj_add_predicate('!', 'org.projog.core.function.flow.Cut').

//% list
?- pj_add_predicate(length/2, 'org.projog.core.function.list.Length').
?- pj_add_predicate(reverse/2, 'org.projog.core.function.list.Reverse').
?- pj_add_predicate(member/2, 'org.projog.core.function.list.Member').
?- pj_add_predicate(memberchk/2, 'org.projog.core.function.list.MemberCheck').
?- pj_add_predicate(append/3, 'org.projog.core.function.list.Append').
?- pj_add_predicate(subtract/3, 'org.projog.core.function.list.SubtractFromList').
?- pj_add_predicate(keysort/2, 'org.projog.core.function.list.KeySort').
?- pj_add_predicate(flatten/2, 'org.projog.core.function.list.Flatten').
?- pj_add_predicate(sort/2, 'org.projog.core.function.list.SortAsSet').
?- pj_add_predicate(msort/2, 'org.projog.core.function.list.Sort').
?- pj_add_predicate(delete/3, 'org.projog.core.function.list.Delete').
?- pj_add_predicate(subset/2, 'org.projog.core.function.list.Subset').
?- pj_add_predicate(select/3, 'org.projog.core.function.list.Select').
?- pj_add_predicate(nth0/3, 'org.projog.core.function.list.Nth/nth0').
?- pj_add_predicate(nth1/3, 'org.projog.core.function.list.Nth/nth1').
?- pj_add_predicate(nth/3, 'org.projog.core.function.list.Nth/nth1').
?- pj_add_predicate(maplist/2, 'org.projog.core.function.list.MapList').
?- pj_add_predicate(checklist/2, 'org.projog.core.function.list.MapList').
?- pj_add_predicate(maplist/3, 'org.projog.core.function.list.MapList').
?- pj_add_predicate(checklist/3, 'org.projog.core.function.list.MapList').
?- pj_add_predicate(include/3, 'org.projog.core.function.list.SubList').
?- pj_add_predicate(sublist/3, 'org.projog.core.function.list.SubList').

//% time
?- pj_add_predicate(get_time/1, 'org.projog.core.function.time.GetTime').
?- pj_add_predicate(convert_time/2, 'org.projog.core.function.time.ConvertTime').

//% numerical operations
?- pj_add_predicate(arithmetic_function/1, 'org.projog.core.function.math.AddArithmeticFunction').
?- pj_add_calculatable('+'/2, 'org.projog.core.function.math.Add').
?- pj_add_calculatable('/'('-', 1), 'org.projog.core.function.math.Minus').
?- pj_add_calculatable('/'('-', 2), 'org.projog.core.function.math.Subtract').
?- pj_add_calculatable('/'/2, 'org.projog.core.function.math.Divide').
?- pj_add_calculatable('//'/2, 'org.projog.core.function.math.IntegerDivide').
?- pj_add_calculatable('*'/2, 'org.projog.core.function.math.Multiply').
?- pj_add_calculatable('**'/2, 'org.projog.core.function.math.Power').
?- pj_add_calculatable(mod/2, 'org.projog.core.function.math.Modulo').
?- pj_add_calculatable(rem/2, 'org.projog.core.function.math.Remainder').
?- pj_add_calculatable(random/1, 'org.projog.core.function.math.Random').
?- pj_add_calculatable('/\'/2, 'org.projog.core.function.math.BitwiseAnd').
?- pj_add_calculatable('>>'/2, 'org.projog.core.function.math.ShiftRight').
?- pj_add_calculatable(max/2, 'org.projog.core.function.math.Max').
?- pj_add_calculatable(abs/1, 'org.projog.core.function.math.Abs').

//% definite clause grammers (DCG)
?- op(1200, xfx, '-->').
?- op(901, fx, '{').
?- op(900, xf, '}').

//% operators
?- op(1200, xfx, ':-').
?- op(1200, fx, ':-').
?- op(1100, fx, dynamic).
?- op(1100, xfy, ';').
?- op(1000, xfy, ',').
?- op(900, fy, '\+').
?- op(700, xfx, '=').
?- op(700, xfx, '==').
?- op(700, xfx, '=:=').
?- op(700, xfx, '=\=').
?- op(700, xfx, '=..').
?- op(700, xfx, '<').
?- op(700, xfx, '>').
?- op(700, xfx, '=<').
?- op(700, xfx, '>=').
?- op(700, xfx, '@<').
?- op(700, xfx, '@=<').
?- op(700, xfx, '@>').
?- op(700, xfx, '@>=').
?- op(700, xfx, '\=').
?- op(700, xfx, is).
?- op(500, yfx, '+').
?- op(500, yfx, '-').
?- op(400, yfx, '*').
?- op(400, yfx, '**').
?- op(400, yfx, '//').
?- op(400, yfx, mod).
?- op(400, yfx, rem).
?- op(400, yfx, '/\').
?- op(400, yfx, '>>').
?- op(200, fy, '-').
*/
}
