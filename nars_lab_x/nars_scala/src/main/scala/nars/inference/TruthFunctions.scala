package nars.logic

import nars.logic.entity._
//remove if not needed
import scala.collection.JavaConversions._
import UtilityFunctions._

object TruthFunctions {

  /**
   * {<A ==> B>} |- <B ==> A>
   * @param v1 Truth value of the premise
   * @return Truth value of the conclusion
   */
  def conversion(v1: TruthValue): TruthValue = {
    val f1 = v1.getFrequency
    val c1 = v1.getConfidence
    val w = and(f1, c1)
    val c = w2c(w)
    new TruthValue(1, c)
  }

  /**
   * {A} |- (--A)
   * @param v1 Truth value of the premise
   * @return Truth value of the conclusion
   */
  def negation(v1: TruthValue): TruthValue = {
    val f = 1 - v1.getFrequency
    val c = v1.getConfidence
    new TruthValue(f, c)
  }

  /**
   * {<A ==> B>} |- <(--, B) ==> (--, A)>
   * @param v1 Truth value of the premise
   * @return Truth value of the conclusion
   */
  def contraposition(v1: TruthValue): TruthValue = {
    val f1 = v1.getFrequency
    val c1 = v1.getConfidence
    val w = and(1 - f1, c1)
    val c = w2c(w)
    new TruthValue(0, c)
  }

  /**
   * {<S ==> P>, <S ==> P>} |- <S ==> P>
   * @param v1 Truth value of the first premise
   * @param v2 Truth value of the second premise
   * @return Truth value of the conclusion
   */
  def revision(v1: TruthValue, v2: TruthValue): TruthValue = {
    val f1 = v1.getFrequency
    val f2 = v2.getFrequency
    val c1 = v1.getConfidence
    val c2 = v2.getConfidence
    val w1 = c2w(c1)
    val w2 = c2w(c2)
    val w = w1 + w2
    val f = (w1 * f1 + w2 * f2) / w
    val c = w2c(w)
    new TruthValue(f, c)
  }

  /**
   * {<S ==> M>, <M ==> P>} |- <S ==> P>
   * @param v1 Truth value of the first premise
   * @param v2 Truth value of the second premise
   * @return Truth value of the conclusion
   */
  def deduction(v1: TruthValue, v2: TruthValue): TruthValue = {
    val f1 = v1.getFrequency
    val f2 = v2.getFrequency
    val c1 = v1.getConfidence
    val c2 = v2.getConfidence
    val f = and(f1, f2)
    val c = and(c1, c2, f)
    new TruthValue(f, c)
  }

  /**
   * {M, <M ==> P>} |- P
   * @param v1 Truth value of the first premise
   * @param reliance Confidence of the second (analytical) premise
   * @return Truth value of the conclusion
   */
  def deduction(v1: TruthValue, reliance: Float): TruthValue = {
    val f1 = v1.getFrequency
    val c1 = v1.getConfidence
    val c = and(f1, c1, reliance)
    new TruthValue(f1, c)
  }

  /**
   * {<S ==> M>, <M <=> P>} |- <S ==> P>
   * @param v1 Truth value of the first premise
   * @param v2 Truth value of the second premise
   * @return Truth value of the conclusion
   */
  def analogy(v1: TruthValue, v2: TruthValue): TruthValue = {
    val f1 = v1.getFrequency
    val f2 = v2.getFrequency
    val c1 = v1.getConfidence
    val c2 = v2.getConfidence
    val f = and(f1, f2)
    val c = and(c1, c2, f2)
    new TruthValue(f, c)
  }

  /**
   * {<S <=> M>, <M <=> P>} |- <S <=> P>
   * @param v1 Truth value of the first premise
   * @param v2 Truth value of the second premise
   * @return Truth value of the conclusion
   */
  def resemblance(v1: TruthValue, v2: TruthValue): TruthValue = {
    val f1 = v1.getFrequency
    val f2 = v2.getFrequency
    val c1 = v1.getConfidence
    val c2 = v2.getConfidence
    val f = and(f1, f2)
    val c = and(c1, c2, or(f1, f2))
    new TruthValue(f, c)
  }

  /**
   * {<S ==> M>, <P ==> M>} |- <S ==> P>
   * @param v1 Truth value of the first premise
   * @param v2 Truth value of the second premise
   * @return Truth value of the conclusion
   */
  def abduction(v1: TruthValue, v2: TruthValue): TruthValue = {
    val f1 = v1.getFrequency
    val f2 = v2.getFrequency
    val c1 = v1.getConfidence
    val c2 = v2.getConfidence
    val w = and(f2, c1, c2)
    val c = w2c(w)
    new TruthValue(f1, c)
  }

  /**
   * {M, <P ==> M>} |- P
   * @param v1 Truth value of the first premise
   * @param reliance Confidence of the second (analytical) premise
   * @return Truth value of the conclusion
   */
  def abduction(v1: TruthValue, reliance: Float): TruthValue = {
    val f1 = v1.getFrequency
    val c1 = v1.getConfidence
    val w = and(c1, reliance)
    val c = w2c(w)
    new TruthValue(f1, c)
  }

  /**
   * {<M ==> S>, <M ==> P>} |- <S ==> P>
   * @param v1 Truth value of the first premise
   * @param v2 Truth value of the second premise
   * @return Truth value of the conclusion
   */
  def induction(v1: TruthValue, v2: TruthValue): TruthValue = abduction(v2, v1)

  /**
   * {<M ==> S>, <P ==> M>} |- <S ==> P>
   * @param v1 Truth value of the first premise
   * @param v2 Truth value of the second premise
   * @return Truth value of the conclusion
   */
  def exemplification(v1: TruthValue, v2: TruthValue): TruthValue = {
    val f1 = v1.getFrequency
    val f2 = v2.getFrequency
    val c1 = v1.getConfidence
    val c2 = v2.getConfidence
    val w = and(f1, f2, c1, c2)
    val c = w2c(w)
    new TruthValue(1, c)
  }

  /**
   * {<M ==> S>, <M ==> P>} |- <S <=> P>
   * @param v1 Truth value of the first premise
   * @param v2 Truth value of the second premise
   * @return Truth value of the conclusion
   */
  def comparison(v1: TruthValue, v2: TruthValue): TruthValue = {
    val f1 = v1.getFrequency
    val f2 = v2.getFrequency
    val c1 = v1.getConfidence
    val c2 = v2.getConfidence
    val f0 = or(f1, f2)
    val f = if ((f0 == 0)) 0 else (and(f1, f2) / f0)
    val w = and(f0, c1, c2)
    val c = w2c(w)
    new TruthValue(f, c)
  }

  /**
   * A function specially designed for desire value [To be refined]
   * @param v1 Truth value of the first premise
   * @param v2 Truth value of the second premise
   * @return Truth value of the conclusion
   */
  def desireStrong(v1: TruthValue, v2: TruthValue): TruthValue = {
    val f1 = v1.getFrequency
    val f2 = v2.getFrequency
    val c1 = v1.getConfidence
    val c2 = v2.getConfidence
    val f = and(f1, f2)
    val c = and(c1, c2, f2)
    new TruthValue(f, c)
  }

  /**
   * A function specially designed for desire value [To be refined]
   * @param v1 Truth value of the first premise
   * @param v2 Truth value of the second premise
   * @return Truth value of the conclusion
   */
  def desireWeak(v1: TruthValue, v2: TruthValue): TruthValue = {
    val f1 = v1.getFrequency
    val f2 = v2.getFrequency
    val c1 = v1.getConfidence
    val c2 = v2.getConfidence
    val f = and(f1, f2)
    val c = and(c1, c2, f2, w2c(1.0f))
    new TruthValue(f, c)
  }

  /**
   * A function specially designed for desire value [To be refined]
   * @param v1 Truth value of the first premise
   * @param v2 Truth value of the second premise
   * @return Truth value of the conclusion
   */
  def desireDed(v1: TruthValue, v2: TruthValue): TruthValue = {
    val f1 = v1.getFrequency
    val f2 = v2.getFrequency
    val c1 = v1.getConfidence
    val c2 = v2.getConfidence
    val f = and(f1, f2)
    val c = and(c1, c2)
    new TruthValue(f, c)
  }

  /**
   * A function specially designed for desire value [To be refined]
   * @param v1 Truth value of the first premise
   * @param v2 Truth value of the second premise
   * @return Truth value of the conclusion
   */
  def desireInd(v1: TruthValue, v2: TruthValue): TruthValue = {
    val f1 = v1.getFrequency
    val f2 = v2.getFrequency
    val c1 = v1.getConfidence
    val c2 = v2.getConfidence
    val w = and(f2, c1, c2)
    val c = w2c(w)
    new TruthValue(f1, c)
  }

  /**
   * {<M --> S>, <M <-> P>} |- <M --> (S|P)>
   * @param v1 Truth value of the first premise
   * @param v2 Truth value of the second premise
   * @return Truth value of the conclusion
   */
  def union(v1: TruthValue, v2: TruthValue): TruthValue = {
    val f1 = v1.getFrequency
    val f2 = v2.getFrequency
    val c1 = v1.getConfidence
    val c2 = v2.getConfidence
    val f = or(f1, f2)
    val c = and(c1, c2)
    new TruthValue(f, c)
  }

  /**
   * {<M --> S>, <M <-> P>} |- <M --> (S&P)>
   * @param v1 Truth value of the first premise
   * @param v2 Truth value of the second premise
   * @return Truth value of the conclusion
   */
  def intersection(v1: TruthValue, v2: TruthValue): TruthValue = {
    val f1 = v1.getFrequency
    val f2 = v2.getFrequency
    val c1 = v1.getConfidence
    val c2 = v2.getConfidence
    val f = and(f1, f2)
    val c = and(c1, c2)
    new TruthValue(f, c)
  }

  /**
   * {(||, A, B), (--, B)} |- A
   * @param v1 Truth value of the first premise
   * @param v2 Truth value of the second premise
   * @return Truth value of the conclusion
   */
  def reduceDisjunction(v1: TruthValue, v2: TruthValue): TruthValue = {
    val v0 = intersection(v1, negation(v2))
    deduction(v0, 1f)
  }

  /**
   * {(--, (&&, A, B)), B} |- (--, A)
   * @param v1 Truth value of the first premise
   * @param v2 Truth value of the second premise
   * @return Truth value of the conclusion
   */
  def reduceConjunction(v1: TruthValue, v2: TruthValue): TruthValue = {
    val v0 = intersection(negation(v1), v2)
    negation(deduction(v0, 1f))
  }

  /**
   * {(--, (&&, A, (--, B))), (--, B)} |- (--, A)
   * @param v1 Truth value of the first premise
   * @param v2 Truth value of the second premise
   * @return Truth value of the conclusion
   */
  def reduceConjunctionNeg(v1: TruthValue, v2: TruthValue): TruthValue = reduceConjunction(v1, negation(v2))

  /**
   * {(&&, <#x() ==> M>, <#x() ==> P>), S ==> M} |- <S ==> P>
   * @param v1 Truth value of the first premise
   * @param v2 Truth value of the second premise
   * @return Truth value of the conclusion
   */
  def anonymousAnalogy(v1: TruthValue, v2: TruthValue): TruthValue = {
    val f1 = v1.getFrequency
    val c1 = v1.getConfidence
    val v0 = new TruthValue(f1, w2c(c1))
    analogy(v2, v0)
  }
}
