//package nars.tuprolog.store;
//
//import alice.tuprolog.ClauseInfo;
//import alice.tuprolog.Struct;
//import alice.tuprolog.SubGoalTree;
//
//public class FactClauseInfo implements ClauseInfo
//{
//	private static final SubGoalTree empty_body = new SubGoalTree();
//	protected Struct clause;
//
//	public FactClauseInfo(Struct clause)
//	{
//		this.clause = clause;
//	}
//
//	public Struct getClause()
//	{
//		return clause;
//	}
//
//	public SubGoalTree getBody()
//	{
//		return empty_body;
//	}
//
//	public SubGoalTree getBodyCopy()
//	{
//		return empty_body;
//	}
//
//	public Struct getHead()
//	{
//		return getClause();
//	}
//
//	public Struct getHeadCopy()
//	{
//		return getHead();
//	}
//
//	public String getLibName()
//	{
//		return null;
//	}
//
//	public void performCopy(int idExecCtx)
//	{
//		// TODO...
//	}
//}