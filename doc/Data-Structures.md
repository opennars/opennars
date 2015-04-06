> ### DataStructures  
> What the different data structures represent

***

### Data Types

**Term:**

A Narsese identifier. Special cases: A CompoundTerm consists of an operator and a list of components. A statement has a truth-value and a desire-value. An operation has associated executable code. See wikipage [Input Output Format](https://github.com/opennars/opennars/wiki/Input-Output-Format).

**Sentence:**

A statement with a specific punctuation, a truth-value or desire-value, and a stamp.

**Goal:**

Sentence with '!' as punctuation. (with specific desire-value)

**Judgement:**

Sentence with '.' as punctuation. (with specific truth-value)

**Quest:**

Sentence with '@' as punctuation. (question of desire-value)

**Question:**

Sentence with '?' as punctuation. (question of truth-value)

**Task:**

A sentence with a budget-value and a placeholder for a solution which is a sentence (answer to a question or truth value of a goal which measures to what extent it is already fulfilled).

**Stamp:**

Creation Time, Occurence Time, Evidental Base, Derivation Chain. Exists in order to give the sentence information like from where it was derived (in order to avoid reasoning cycles, derivation chain), from which input it was derived (needed because it is not allowed to allow revision on overlapping evidental base), its temporal information or tense (occurrence time), and its creation time (when it was derived/when it entered memory). See wikipage StampInNARS for details.

**Concept:**

This is the most complex data type in the system which adds some additional terminology: A concept is named by one term. Not all terms are concepts though: For example if the term of the concept has subterms, the concepts which were named by the subterms which once existed may have been already forgotten. A concept links to tasks with different budget which are here called TaskLinks. A concept links to other terms with termlinks, linking to exactly those terms who share a common term in bi-directional manner. Using the terms as name, this type of link eventually link a concept to other concepts. All this links are managed in bags, and as such may be forgotten. Also note: Concepts are itself in a bag fighting against getting forgotten, see [Inference Control](https://github.com/opennars/opennars/wiki/Inference-Control) wikipage for this.

When the term that naming the concept is a statement, the concept also consists of additional information:

* Beliefs, which are judgements (i.e., truth-values on the statement) sorted according to confidence and other factors.

* Desires, which are goals (i.e., desire-values on the statement) sorted according to confidence and other factors.

* Questions, which are questions or quests on the statement.