package org.projog.core.function.time;

import org.projog.core.Calculatables;
import org.projog.core.function.AbstractSingletonPredicate;
import org.projog.core.term.PAtom;
import org.projog.core.term.PTerm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.projog.core.KnowledgeBaseUtils.getCalculatables;

/* TEST
 %QUERY convert_time(0, X)
 %ANSWER X=1970-01-01T00:00:00.000+0000
 %TRUE convert_time(0, '1970-01-01T00:00:00.000+0000')
 
 %QUERY convert_time(1000*60*60*24*500+(1000*60*72), X)
 %ANSWER X=1971-05-16T01:12:00.000+0000
 %TRUE convert_time(1000*60*60*24*500+(1000*60*72), '1971-05-16T01:12:00.000+0000')
 
 %QUERY convert_time(9223372036854775807, X)
 %ANSWER X=292278994-08-17T07:12:55.807+0000
 */
/**
 * <code>convert_time(X,Y)</code> - converts a timestamp to a textual representation.
 */
public final class ConvertTime extends AbstractSingletonPredicate {
   private Calculatables calculatables;

   @Override
   public void init() {
      calculatables = getCalculatables(getKB());
   }

   @Override
   public boolean evaluate(PTerm timestamp, PTerm text) {
      Date d = createDate(timestamp);
      PAtom a = createAtom(d);
      return text.unify(a);
   }

   private Date createDate(PTerm timestamp) {
      return new Date(calculatables.getNumeric(timestamp).getLong());
   }

   private PAtom createAtom(Date d) {
      // TODO have overloaded versions of convert_time that allow the date format and timezone to be specified?
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      sdf.setTimeZone(TimeZone.getTimeZone("GMT-0"));
      return new PAtom(sdf.format(d));
   }
}
