/*
 * Copyright 2007-2013
 * Licensed under GNU Lesser General Public License
 * 
 * This file is part of EpochX: genetic programming software for research
 * 
 * EpochX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EpochX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with EpochX. If not, see <http://www.gnu.org/licenses/>.
 * 
 * The latest version is available from: http://www.epochx.org
 */
package objenome.solver.evolve.grammar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A non-terminal node of a parse tree, that was constructed to satisfy a
 * specific rule of a grammar. The underlying <code>GrammarRule</code> is
 * provided at construction time. An instance's children are those
 * <code>Symbol</code> objects that the non-terminal resolves to, as supported
 * by the grammar rule.
 *
 * @see TerminalSymbol
 * @see GrammarRule
 */
public class NonTerminalSymbol implements Symbol {

    // The child nodes in the parse tree.
    private List<Symbol> children;

    // The associated grammar node.
    private GrammarRule grammarRule;

    /**
     * Constructs a <code>NonTerminalSymbol</code> for the given
     * <code>GrammarRule</code>.
     *
     * @param grammarRule the <code>GrammarRule</code> which this new object is
     * representing an instance of.
     */
    public NonTerminalSymbol(GrammarRule grammarRule) {
        this(grammarRule, new ArrayList<>());
    }

    /**
     * Constructs a <code>NonTerminalSymbol</code> for the given
     * <code>GrammarRule</code> and with a list of child parse tree symbols.
     *
     * @param grammarRule the <code>GrammarRule</code> which this new object is
     * representing an instance of
     * @param children a list of <code>Symbol</code> instances which this
     * <code>NonTerminalSymbol</code> resolves to, as supported by the grammar
     * rule.
     */
    public NonTerminalSymbol(GrammarRule grammarRule, List<Symbol> children) {
        this.grammarRule = grammarRule;
        this.children = children;
    }

    /**
     * Overwrites the <code>Symbol</code> at the specified index. Note that the
     * index must be a currently valid index.
     *
     * @param index the index of the <code>Symbol</code> to change.
     * @param child the <code>Symbol</code> to set at the specified index.
     */
    public void setChild(int index, Symbol child) {
        // Make the change.
        children.set(index, child);
    }

    /**
     * Returns the <code>Symbol</code> at the specified index.
     *
     * @param index the index of the <code>Symbol</code> to return.
     * @return the <code>Symbol</code> found at the given index.
     */
    public Symbol getChild(int index) {
        return children.get(index);
    }

    /**
     * Appends the given <code>Symbol</code> to the list of child nodes.
     *
     * @param child the <code>Symbol</code> instance to append.
     */
    public void addChild(Symbol child) {
        // Make the change.
        children.add(child);
    }

    /**
     * Removes all currently set child <code>Symbols</code>.
     *
     * @return a <code>List</code> of the child <code>Symbol</code> instances
     * that were removed.
     */
    public List<Symbol> removeChildren() {
        // Make the change.
        children.clear();

        return children;
    }

    /**
     * Removes a the <code>Symbol</code> that is at the specified index.
     *
     * @param index the index of the <code>Symbol</code> to remove.
     * @return the <code>Symbol</code> instance that was removed.
     */
    public Symbol removeChild(int index) {
        return children.remove(index);
    }

    /**
     * Returns a reference to the underlying <code>List</code> of child
     * <code>Symbol</code> instances for this non-terminal. Any changes to the
     * returned list will be reflected in this symbol.
     *
     * @return a <code>List</code> of the child <code>Symbol</code> instances.
     */
    public List<Symbol> getChildren() {
        return children;
    }

    /**
     * Overwrites this non-terminal's <code>List</code> of child
     * <code>Symbols</code>.
     *
     * @param newChildren the <code>List</code> of child <code>Symbol</code>
     * instances to set.
     */
    public void setChildren(List<Symbol> newChildren) {
        // Make the change.
        children = newChildren;
    }

    /**
     * Calculates and returns the number of non-terminal symbols that exist
     * within the tree rooted at this non-terminal symbol, including this
     * <code>Symbol</code>. The result should always be equal or greater than 1.
     *
     * @return a positive integer which is the count of the number of
     * <code>NonTerminalSymbol</code> instances in the parse tree rooted at this
     * <code>Symbol</code>, inclusive of this symbol.
     */
    public int getNoNonTerminalSymbols() {
        // Start by adding self.
        int noNonTerminals = 1;

        // Count all the non-terminals below each child.
        for (Symbol child : children) {
            if (child instanceof NonTerminalSymbol) {
                noNonTerminals += ((NonTerminalSymbol) child).getNoNonTerminalSymbols();
            }
        }

        assert (noNonTerminals >= 1);

        return noNonTerminals;
    }

    /**
     * Calculates and returns the number of non-terminal symbols that exist
     * within the tree rooted at this non-terminal symbol which have the
     * specified underlying <code>GrammarRule</code>. The count is inclusive of
     * this <code>NonTerminalSymbol</code>. A <code>NonTerminalSymbol</code>
     * <code>x</code>is included in the count if the following expression is
     * <code>true</code>.
     *
     * <blockquote><code>
     * 		this.getGrammarRule().equals(x.getGrammarRule())
     * </code></blockquote>
     *
     * @param rule the <code>GrammarRule</code> that should be matched in all
     * <code>NonTerminalSymbols</code> included in the count.
     * @return the total number of non-terminal symbols that have a matching
     * <code>GrammarRule</code>.
     */
    public int getNoNonTerminalSymbols(GrammarRule rule) {
        int noNonTerminals = 0;

        // Start by adding self.
        if (getGrammarRule().equals(rule)) {
            noNonTerminals++;
        }

        // Count all the non-terminals below each child.
        for (Symbol child : children) {
            if (child instanceof NonTerminalSymbol) {
                noNonTerminals += ((NonTerminalSymbol) child).getNoNonTerminalSymbols(rule);
            }
        }

        return noNonTerminals;
    }

    /**
     * Calculates and returns the number of terminal symbols that exist at the
     * leaves of the parse tree rooted at this non-terminal symbol. The count
     * should always be positive, and will only ever be zero in the case of an
     * incomplete parse tree.
     *
     * @return an <code>int</code> which is the total number of terminal symbols
     * in this parse tree.
     */
    public int getNoTerminalSymbols() {
        int noTerminals = 0;

        // Count all the terminals below each child.
        for (Symbol child : children) {
            if (child instanceof TerminalSymbol) {
                noTerminals++;
            } else if (child instanceof NonTerminalSymbol) {
                noTerminals += ((NonTerminalSymbol) child).getNoTerminalSymbols();
            }
        }

        return noTerminals;
    }

    /**
     * Calculates and returns the total number of <code>Symbol</code> instances
     * that exist in the parse tree rooted at this non-terminal symbol,
     * including this <code>Symbol</code> itself. The result should be equal to
     * the sum of the results from the <code>getNoNonTerminalSymbols()</code>
     * and <code>getNoTerminalSymbols()</code> methods.
     *
     * @return the total number of symbols that exist in the parse tree rooted
     * at this <code>Symbol</code>.
     */
    public int getNoSymbols() {
        // Start by adding self.
        int noSymbols = 1;

        // Count all the symbols below each child.
        for (Symbol child : children) {
            if (child instanceof TerminalSymbol) {
                noSymbols++;
            } else if (child instanceof NonTerminalSymbol) {
                noSymbols += ((NonTerminalSymbol) child).getNoSymbols();
            }
        }

        return noSymbols;
    }

    /**
     * Returns the number of direct child <code>Symbols</code> this non-terminal
     * symbol has.
     *
     * @return an <code>int</code> which is the number of child symbols this
     * non-terminal has.
     */
    public int getNoChildren() {
        return children.size();
    }

    /**
     * Removes the nth non-terminal from the parse tree rooted at this
     * <code>NonTerminalSymbol</code> instance, as counted using a pre-order
     * traversal. Indexing starts at zero for this non-terminal symbol. As such,
     * valid values of n must be greater than or equal to 1, since it is
     * impossible to remove a symbol from itself.
     *
     * @param n an <code>int</code> with a value of 1 or greater which is the
     * index of the <code>NonTerminalSymbol</code> that should be removed.
     * @return the <code>NonTerminalSymbol</code> that was removed, or
     * <code>null</code> if none were removed.
     */
    public NonTerminalSymbol removeNthNonTerminal(int n) {
        return removeNthNonTerminal(n, 0, null);
    }

    /*
     * Recursive helper method for the removeNthNonTerminal method.
     */
    private NonTerminalSymbol removeNthNonTerminal(int n, int current, GrammarRule rule) {
        for (int i = 0; i < children.size(); i++) {
            Symbol child = children.get(i);

            if (child instanceof NonTerminalSymbol) {
                NonTerminalSymbol nt = (NonTerminalSymbol) child;

                boolean valid = false;
                if ((rule == null) || rule.equals(nt.getGrammarRule())) {
                    valid = true;
                }

                if (valid && (n == current + 1)) {
                    // It is this child.
                    return (NonTerminalSymbol) removeChild(i);
                } else {
                    NonTerminalSymbol nth = nt.removeNthNonTerminal(n, (valid ? current + 1 : current), rule);

                    if (nth != null) {
                        return nth;
                    }

                    current += nt.getNoNonTerminalSymbols(rule);
                }
            }
        }

        return null;
    }

    /**
     * Removes the nth non-terminal with the given <code>GrammarRule</code> from
     * the parse tree rooted at this <code>NonTerminalSymbol</code> instance, as
     * counted using a pre-order traversal. Indexing starts at zero and from
     * this non-terminal symbol itself. It does not make sense to remove this
     * <code>Symbol</code> from itself however so a value for <code>n</code> of
     * <code>0</code> is only possible if the given <code>GrammarRule</code>
     * does not match this <code>Symbol</code>.
     *
     * @param n an <code>int</code> which is the index of the
     * <code>NonTerminalSymbol</code> with the given grammar rule that should be
     * removed.
     * @param grammarRule the <code>GrammarRule</code> that the symbol to be
     * removed should have.
     * @return the <code>NonTerminalSymbol</code> that was removed, or
     * <code>null</code> if none were removed.
     */
    public NonTerminalSymbol removeNthNonTerminal(int n, GrammarRule grammarRule) {
        return removeNthNonTerminal(n, 0, grammarRule);
    }

    /**
     * Returns the nth non-terminal from the parse tree rooted at this
     * <code>NonTerminalSymbol</code>. Indexing starts at zero for this symbol
     * as the root, and proceeds in a pre-order traversal of the tree.
     *
     * @param n the index of the non-terminal to return.
     * @return the <code>NonTerminalSymbol</code> which was the nth in the parse
     * tree.
     */
    public NonTerminalSymbol getNthNonTerminal(int n) {
        return getNthNonTerminal(n, 0);
    }

    /*
     * Recursive helper method for the getNthNonTerminal method.
     */
    private NonTerminalSymbol getNthNonTerminal(int n, int current) {
        // Is this the one we're looking for?
        if (n == current) {
            return this;
        }

        for (Symbol child : children) {
            if (child instanceof NonTerminalSymbol) {
                NonTerminalSymbol nt = (NonTerminalSymbol) child;

                NonTerminalSymbol nth = nt.getNthNonTerminal(n, current + 1);

                if (nth != null) {
                    return nth;
                }

                current += nt.getNoNonTerminalSymbols();
            }
        }

        return null;
    }

    /**
     * Returns the nth terminal from the parse tree rooted at this
     * <code>NonTerminalSymbol</code>. Indexing starts at zero and proceeds
     * according to the order that terminals are met while performing a
     * pre-order traversal of the tree from this symbol.
     *
     * @param n the index of the terminal to return.
     * @return the <code>TerminalSymbol</code> which was the nth in the parse
     * tree.
     */
    public TerminalSymbol getNthTerminal(int n) {
        List<TerminalSymbol> terminals = getTerminalSymbols();

        return terminals.get(n);
    }

    /**
     * Returns the nth symbol from the parse tree rooted at this symbol.
     * Indexing starts at zero for this, the root, and proceeds in a pre-order
     * traversal of the tree until the nth symbol is found.
     *
     * @param n the index of the symbol to be returned.
     * @return the nth symbol from this parse tree.
     */
    public Symbol getNthSymbol(int n) {
        return getNthSymbol(n, 0);
    }

    /*
     * Recursive helper method for the getNthSymbol method.
     */
    private Symbol getNthSymbol(int n, int current) {
        // Is this the one we're looking for?
        if (n == current) {
            return this;
        }

        for (Symbol child : children) {
            if (child instanceof NonTerminalSymbol) {
                NonTerminalSymbol nt = (NonTerminalSymbol) child;

                Symbol nth = nt.getNthSymbol(n, current + 1);

                if (nth != null) {
                    return nth;
                }

                current += nt.getNoSymbols();
            } else {
                if (n == ++current) {
                    return child;
                }
            }
        }

        return null;
    }

    /**
     * Overwrites the nth symbol in the parse tree rooted at this symbol.
     * Indexing starts at zero for this, the root and proceeds in a pre-order
     * traversal of the tree until the nth symbol is found. However, it is not
     * possible to set the zeroth symbol since that would mean replacing this
     * instance itself. To replace it, the <code>setNthSymbol</code> method
     * should be called upon any parent <code>NonTerminalSymbol</code> or if it
     * is the root of the whole tree then by using the replacement directly as
     * the new parse tree.
     *
     * @param n the index of where to set the new symbol.
     * @param newSymbol the replacement <code>Symbol</code> to set at the nth
     * position.
     */
    public void setNthSymbol(int n, Symbol newSymbol) {
        setNthSymbol(n, newSymbol, 0);
    }

    /*
     * Recursive helper method for the setNthSymbol method.
     */
    private void setNthSymbol(int n, Symbol symbol, int current) {
        int noChildren = getNoChildren();
        for (int i = 0; i < noChildren; i++) {
            if (current + 1 == n) {
                setChild(i, symbol);
                break;
            }

            if (children.get(i) instanceof NonTerminalSymbol) {
                NonTerminalSymbol child = (NonTerminalSymbol) children.get(i);
                int noChildSymbols = child.getNoSymbols();

                // Only look at the subtree if it contains the right range of
                // nodes.
                if (n <= current + noChildSymbols) {
                    child.setNthSymbol(n, symbol, current + 1);
                }

                current += noChildSymbols;
            } else {
                // It's a terminal so just increment 1.
                current++;
            }
        }
    }

    /**
     * Returns a <code>List</code> of all the non-terminal symbols in the parse
     * tree below this symbol, including this symbol itself.
     *
     * @return a <code>List</code> of <code>NonTerminalSymbol</code> instances
     * from the parse tree rooted at this symbol.
     */
    public Collection<NonTerminalSymbol> getNonTerminalSymbols() {
        List<NonTerminalSymbol> nonTerminals = new ArrayList<>();

        // Start by adding self.
        nonTerminals.add(this);

        // Add all the non-terminals below each child.
        children.stream().filter(child -> child instanceof NonTerminalSymbol).forEach(child -> nonTerminals.addAll(((NonTerminalSymbol) child).getNonTerminalSymbols()));

        return nonTerminals;
    }

    /**
     * Returns a <code>List</code> of the indexes of all the symbols in the
     * parse tree rooted at this symbol that are instances of
     * <code>NonTerminalSymbol</code>.
     *
     * @return a <code>List</code> of <code>Integers</code> which are the
     * indexes of the non-terminal symbols in the parse tree rooted at this
     * symbol.
     */
    public List<Integer> getNonTerminalIndexes() {
        return getNonTerminalIndexes(0);
    }

    /*
     * Recursive helper method for the getNonTerminalIndexes method.
     */
    private List<Integer> getNonTerminalIndexes(int index) {
        List<Integer> nonTerminals = new ArrayList<>();

        // Start by adding self.
        nonTerminals.add(index);

        // Add all the non-terminals below each child.
        for (Symbol child : children) {
            if (child instanceof NonTerminalSymbol) {
                NonTerminalSymbol nt = (NonTerminalSymbol) child;
                nonTerminals.addAll(nt.getNonTerminalIndexes(index + 1));
                index += nt.getNoSymbols();
            } else {
                index++;
            }
        }

        return nonTerminals;
    }

    /**
     * Returns a <code>List</code> of all the terminal symbols in the parse tree
     * below this non-terminal symbol.
     *
     * @return a <code>List</code> of <code>TerminalSymbol</code> instances from
     * the parse tree rooted at this symbol.
     */
    public List<TerminalSymbol> getTerminalSymbols() {
        List<TerminalSymbol> terminals = new ArrayList<>();

        // Add all terminal children and terminals below a non-terminal child.
        for (Symbol child : children) {
            if (child instanceof TerminalSymbol) {
                terminals.add((TerminalSymbol) child);
            } else if (child instanceof NonTerminalSymbol) {
                terminals.addAll(((NonTerminalSymbol) child).getTerminalSymbols());
            }
        }

        return terminals;
    }

    /**
     * Returns a <code>List</code> of all <code>Symbol</code> instances from the
     * parse tree rooted at this symbol.
     *
     * @return a <code>List</code> of <code>Symbol</code> instances from the
     * parse tree rooted at this symbol.
     */
    public Collection<Symbol> getAllSymbols() {
        List<Symbol> symbols = new ArrayList<>();

        symbols.add(this);

        for (Symbol child : children) {
            if (child instanceof TerminalSymbol) {
                symbols.add(child);
            } else if (child instanceof NonTerminalSymbol) {
                symbols.addAll(((NonTerminalSymbol) child).getAllSymbols());
            }
        }

        return symbols;
    }

    /**
     * Returns this non-terminal symbol's grammar rule.
     *
     * @return the underlying grammar rule this non-terminal symbol is defined
     * by.
     */
    public GrammarRule getGrammarRule() {
        return grammarRule;
    }

    /**
     * Returns the depth of the parse tree rooted at this
     * <code>NonTerminalSymbol</code>. The depth is considered to be the maximum
     * number of steps down the tree from this symbol to a terminal symbol. A
     * tree made up of one non-terminal symbol with all terminal children will
     * have a depth of <code>1</code>.
     *
     * @return the depth of the parse tree rooted at this symbol.
     */
    public int getDepth() {
        int maxChildDepth = 0;

        for (Symbol child : children) {
            int childDepth;
            childDepth = child instanceof NonTerminalSymbol ? ((NonTerminalSymbol) child).getDepth() + 1 : 1;

            if (childDepth > maxChildDepth) {
                maxChildDepth = childDepth;
            }
        }

        return maxChildDepth;
    }

    /**
     * Returns a string representation of this non-terminal symbol, which is a
     * conjunction of the string representations of each child symbol.
     *
     * @return a <code>String</code> representation of this object.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(children.size());
        for (Symbol c : children) {
            buffer.append(c);
        }

        return buffer.toString();
    }

    /**
     * Constructs and returns a copy of this non-terminal symbol. Each child
     * <code>Symbol</code> is itself cloned, but the grammar rule is shallow
     * copied.
     *
     * @return a copy of this non-terminal symbol.
     */
    @Override
    public NonTerminalSymbol clone() {
        NonTerminalSymbol clone = null;
        try {
            clone = (NonTerminalSymbol) super.clone();
        } catch (CloneNotSupportedException e) {
            // This shouldn't ever happen - if it does then everything is
            // going to blow up anyway.
            assert false;
        }

        // Copy cloned child symbols.
        clone.children = new ArrayList<>();
        for (Symbol c : children) {
            clone.children.add(c.clone());
        }

        // Shallow copy the grammar rules.
        clone.grammarRule = grammarRule;

        return clone;
    }

    /**
     * Tests the given <code>Object</code> for equality with this non-terminal
     * symbol. They will be considered equal if the given <code>Object</code> is
     * an instance of <code>NonTerminalSymbol</code>, all their child symbols
     * are equal according to the contract of their <code>equals</code> method,
     * in the same order, and their grammar rules refer to the same instance.
     *
     * @param obj the <code>Object</code> to test for equality.
     * @return <code>true</code> if the given <code>Object</code> is equal to
     * this non-terminal according to the contract outlined above and
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        boolean equal = true;

        if ((obj instanceof NonTerminalSymbol)) {
            NonTerminalSymbol otherSymbol = (NonTerminalSymbol) obj;

            if (getGrammarRule() == otherSymbol.getGrammarRule()) {
                for (int i = 0; i < children.size(); i++) {
                    Symbol thatChild = otherSymbol.getChild(i);
                    Symbol thisChild = getChild(i);

                    if (!Objects.equals(thisChild, thatChild)) {
                        equal = false;
                        break;
                    }
                }
            } else {
                equal = false;
            }
        } else {
            equal = false;
        }

        return equal;
    }
}
