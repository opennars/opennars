package nars.prolog.gui.ide;

import java.util.ArrayList;
import nars.prolog.Prolog;
import nars.prolog.SolveInfo;
import nars.prolog.event.PrologEvent;
import nars.prolog.event.QueryEvent;

/**
 * This class represents events concerning information to display in the console.
 * 
 * 
 *
 */
@SuppressWarnings("serial")
public class InformationToDisplayEvent extends PrologEvent {

    private ArrayList<QueryEvent> queryEventList;
    private ArrayList<String> queryEventListString;
    private int solveType;

    public InformationToDisplayEvent(Prolog source, ArrayList<QueryEvent> queryEventList,ArrayList<String> queryEventListString, int solveType){
        super(source);
        this.queryEventList=queryEventList;
        this.queryEventListString=queryEventListString;
        this.solveType=solveType;
    }
    
    public int getSolveType()
    {
        return solveType;
    }

    public QueryEvent[] getQueryResults()
    {
        return (QueryEvent[]) queryEventList.toArray(new QueryEvent[queryEventList.size()]);
    }
    
    public ArrayList<String> getQueryResultsString()
    {
        return queryEventListString;
    }

    public SolveInfo getQueryResult()
    {
        return ( (QueryEvent) queryEventList.get(0)).getSolveInfo();
    }

    public int getListSize()
    {
        return queryEventList.size();
    }
}
