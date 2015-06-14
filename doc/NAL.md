#NAL: Non-Axiomatic Logic

The logic part of NARS is NAL (Non-Axiomatic Logic), which is defined on a formal language **Narsese**. NAL is designed incrementally with multiple **layers**. So as to increase the intelligence of the system, each successive layer extends NAL and Narsese to have:

 * higher expressive power
 * richer semantics
 * a larger set of inference rules

##NAL 0: Inheritance Logic

###[IL]
To establish a solid semantic foundation for NAL, an **Inheritance Logic** (IL) is introduced first. This logic uses a categorical language, an experience-grounded semantics, and a few syllogistic inference rules. It describes how a statement can be formed by an inheritance copula (or a variant of it) linking one term to another term, and how the relation can transit from given statements to derived statements. 

Since IL assumes sufficient knowledge and resources, it is not non-axiomatic, but helps the designing of NAL by specifying its boundary conditions. At each layer, the extension of grammar rules and inference rules first happens in IL and then the results are further extended in NAL.

##NAL 1: Inference on Atomic Terms & Positive/Negative Inheritance

###[NAL-1]
Inference on atomic terms and inheritance statements, where a statement may have both positive and negative evidence, and the impact of future evidence also needs to be considered. 

Rules include:

 * deduction
 * abduction
 * induction
 * revision
 * choice
 * etc..

Layer 1 establishes the simplest non-axiomatic logic in the framework of term logic, with lessons learned from several non-classical logics, including:

 * inductive logic
 * probabilistic logic
 * fuzzy logic
 * relevance logic
 * non-monotonic logic
 * ..etc


## NAL 2, 3, 4: Applying Set Theory to Compound Terms

###[NAL-2]
Variants of the inheritance copula are introduced, including:
 * similarity
 * instance
 * property

New inference rules include:
 * comparison
 * analogy
 * resemblance
 
Also, a term can represent a set defined by its sole instance or property.

###[NAL-3]
Compound terms can be derived by taking **intersection**, **union**, or **difference** of the **extension (instances)** or **intension (properties)** of the existing terms. 

Inference rules composite new terms, according to the patterns in the experience of the system.

###[NAL-4]
Using term operators **product** and **image**, NAL extends to cover arbitrary relations among terms that cannot be directly represented as copulas.

The meaning of such a relation is determined by the system's experience, rather than fixed and built-in.

## NAL 5 & 6: Predicate and Propositional Logic (respectively)

###[NAL-5]
When a statement is reified as a term, NAL can express statement on statement, as well as execute inference on such "higher-order statements". 

Two higher-order copulas, **implication** and **equivalence**, are added into the logic to express **derivation relations** among statements.

###[NAL-6]
**Variable terms** can be used as symbols for other terms. In inference rules, variable terms can be **introduced**, **unified**, or **eliminated** (i.e., instantiated). 

With variable terms, the system can carry out **hypothetical inference** on **abstract symbols**, so as to serve as a **meta-logic** of an arbitrary reasoning system.

## NAL 7, 8, 9: Logic Programming for Procedural Knowledge and Sensorimotor Control

###[NAL-7]
An **event** is a statement with **temporal attribute**, specified with respect to another event. 

In temporal inference, both the logical information and the temporal information in the premises are processed to derive a **prediction** or **explanation**.

###[NAL-8]
An **operation** is an event that can be directly realized by the system, via the execution of some programs in the host system. 

A **goal** is an event the system desires to realize. With **procedural inference**, the system attempts to use the operations to realize the goals.

###[NAL-9]
When the operations involved in procedural inference are the internal operations of NARS, a **self-referential loop** is formed that gives the system the ability of **self-awareness** and **self-control**. Other related topics include **emotion** and **consciousness**.

_As a whole, NAL does not specifically belong to any of the above logical system._

The reasoning process in NARS uniformly executes many **cognitive functions** that are traditionally studied as separate processes with different mechanisms, such as:

 * learning
 * perceiving
 * planning
 * predicting
 * remembering
 * problem solving
 * decision making
 * ...and so on

----

**Edited from**: https://sites.google.com/site/narswang/home/nars-introduction

