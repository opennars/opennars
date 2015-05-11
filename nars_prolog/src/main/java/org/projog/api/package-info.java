/**
 * Provides a programming interface for Java applications to interact with Projog.
 * <p>
 * As well as interacting with Projog using the console application it is also possible to embed Projog in your Java applications.
 * The steps required for applications to interact with Projog are as follows:
 * <ul>
 * <li>Create a new {@link org.projog.api.Projog} instance.</li>
 * <li>Load in clauses and facts using {@link org.projog.api.Projog#consultFile(File)} or {@link org.projog.api.Projog#consultReader(Reader)}.</li>
 * <li>Create a {@link org.projog.api.QueryStatement} by using {@link org.projog.api.Projog#query(String)}.</li>
 * <li>Create a {@link org.projog.api.QueryResult} by using {@link org.projog.api.QueryStatement#get()}.</li>
 * <li>Iterate through all possible solutions to the query by using {@link org.projog.api.QueryResult#next()}.</li>
 * <li>For each solution get the {@link org.projog.core.term.PTerm} instantiated to a {@link org.projog.core.term.PVar} in the query by calling {@link org.projog.api.QueryResult#getTerm(String)}.</li>
 * </ul>
 */
package org.projog.api;
