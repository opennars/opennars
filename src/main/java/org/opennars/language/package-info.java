/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Term hierarchy in Narsese
 *
 * Open-NARS implements the following formal language, Narsese.
 * <pre>
 *            &lt;sentence&gt; ::= &lt;judgment&gt;
 *                         | &lt;question&gt;
 *            &lt;judgment&gt; ::= &lt;statement&gt; &lt;truth-value&gt;
 *            &lt;question&gt; ::= &lt;statement&gt;
 *           &lt;statement&gt; ::= &lt;&lt;term&gt; &lt;relation&gt; &lt;term&gt;&gt;
 *                         | &lt;compound-statement&gt;
 *                         | &lt;term&gt;
 *                &lt;term&gt; ::= &lt;word&gt;
 *                         | &lt;variable&gt;
 *                         | &lt;compound-term&gt;
 *                         | &lt;statement&gt;
 *            &lt;relation&gt; ::= -->    // Inheritance
 *                         | <->    // Similarity
 *                         | {--    // Instance
 *                         | --]    // Property
 *                         | {-]    // InstanceProperty
 *                         | ==>    // Implication
 *                         | <=>    // Equivalence
 *  &lt;compound-statement&gt; ::= (-- &lt;statement&gt;)                 // Negation
 *                         | (|| &lt;statement&gt; &lt;statement&gt;<sup>+</sup>)    // Disjunction
 *                         | (&& &lt;statement&gt; &lt;statement&gt;<sup>+</sup>)    // Conjunction
 *       &lt;compound-term&gt; ::= {&lt;term&gt;<sup>+</sup>}    // SetExt
 *                         | [&lt;term&gt;<sup>+</sup>]    // SetInt
 *                         | (& &lt;term&gt; &lt;term&gt;<sup>+</sup>)    // IntersectionExt
 *                         | (| &lt;term&gt; &lt;term&gt;<sup>+</sup>)    // IntersectionInt
 *                         | (- &lt;term&gt; &lt;term&gt;)     // DifferenceExt
 *                         | (~ &lt;term&gt; &lt;term&gt;)     // DifferenceInt
 *                         | (* &lt;term&gt; &lt;term&gt;<sup>+</sup>)    // Product
 *                         | (/ &lt;term&gt;<sup>+</sup> _ &lt;term&gt;<sup>*</sup>)    // ImageExt
 *                         | (\ &lt;term&gt;<sup>+</sup> _ &lt;term&gt;<sup>*</sup>)    // ImageInt
 *            &lt;variable&gt; ::= &lt;independent-var&gt;
 *                         | &lt;dependent-var&gt;
 *                         | &lt;query-var&gt;
 *     &lt;independent-var&gt; ::= $[&lt;word&gt;]
 *       &lt;dependent-var&gt; ::= #&lt;word&gt;
 *           &lt;query-var&gt; ::= ?[&lt;word&gt;]
 *                &lt;word&gt; : string in an alphabet
 *         &lt;truth-value&gt; : a pair of real numbers in [0, 1] x (0, 1)
 * </pre>
 *
 * <p>
 * Major methods in the Term classes:
 * </p>
 *
 * <ul>
 * <li>constructors</li>
 * <li>get and set</li>
 * <li>clone, compare, and unify</li>
 * <li>create and access corresponding concept</li>
 * <li>structural operation in compound</li>
 * </ul>
 */
package org.opennars.language;
