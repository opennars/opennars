package nars.tuprolog;

public interface ISolution<Q,S,T> {
    
    public <Z extends T> Z agetVarValue(String varName) throws nars.tuprolog.NoSolutionException;

    public <Z extends T> Z getTerm(String varName) throws nars.tuprolog.NoSolutionException, UnknownVarException ;

    public boolean isSuccess();

    public boolean isHalted();

    public boolean hasOpenAlternatives();

    public S getSolution() throws NoSolutionException;

    public Q getQuery();

    public java.util.List<? extends T> getBindingVars() throws nars.tuprolog.NoSolutionException;
}
