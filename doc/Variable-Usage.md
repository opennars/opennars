> ### Variable Usage
> When to use what variable type
***


### Introduction
The variable terms of NAL sometimes create confusion. This article is an explanation about when to use which variable type, also some intuitive examples are given.

***
### Independent Variables
They represent any unspecified term that matches the pattern the variable occurs in, and has to start with "$". Intuitively they allow saying things like: "Whenever something has a this property (or instance), it also has that property (or instance)".

This type of variable typically appears as the common subject term or predicate term of two inheritance statements in an implication or equivalency statement. They correspond at least loosely to variables under the universal ("for all") quantifier in first order logic.

Example:

Something which is a cat, is also an animal

`<<$1 --> cat> ==> <$1 --> animal>>.`
***
### Dependent Variables

They refer to a specific term and have to start with "#". Intuitively, they allow talking about a thing without having to name it.

This type of variable typically appears as the common subject term or predicate term of two (or more) inheritance statements in a conjunction. They correspond at least loosely to variables under the existential ("there is") quantifier in first order logic.

Example: There are red apples.

`(&&,<#1 --> [red]>,<#1 --> apple>).`
***
### Query Variables

Used just as placeholder for a specific constant statement to ask about:

Who is a cat?

`<?1 --> cat>?`

Note the difference to for example "Are there red apples?" and "Which is a red apple?" are expressed respectively as

`(&&,<#1 --> [red]>,<#1 --> apple>)?`

`(&&,<?1 --> [red]>,<?1 --> apple>)?`

where the former only asks for a truth-value, while the latter asks for a concrete instance.

***
### Mixed Examples

Different types of variable can be used together in several ways to represent something more complicated. For example, the "uncle" relation in Prolog can be defined as: uncle(X,Y) :- parent(Z,Y), brother(X,Z).

In Narsese, a similar representation is:

```
<(&&,<#1 --> human>,<$Y --> (/,parent,#1,_)>,<$X --> (/,brother,_,#1)>) ==> <(*,$X,$Y) --> uncle>>.
```

Let's try with:

```
<{tim} --> human>.

<{tom} --> human>.

<{john} --> human>.

<{tom} --> (/,parent,{john},_)>.

<{tim} --> (/,brother,_,{john})>.

<(*,{?who},{?ofWho}) --> uncle>?
```

works:

`Answer <(*,{tim},{tom}) --> uncle>. %1.00;0.35%`

More complicated cases: "Every lock can be opened by some key" is expressed as:

`<<$x --> lock> ==> (&&,<#y --> key>,<$x --> (/,open,#y,_)>)>. `

Interesting NAL9 case which involves all variable types:

"Who is similar to Tom?" is a good question.

`<{<?who <-> Tom>} --> (&, [good], question)>.`

If something is a good question, and you ask yourself the question, some agent will be a good agent.

```
<(&&,<#1 --> (&, [good], question)>, (*, {$2}, #1) --> wonder>) ==> ({#2} --> (&, [good], agent)>.
```

NARS is a agent

`<{NARS} --> agent>.`

NARS should also be good

`<{NARS} --> [good]>!`

As a result, in order to make the goal true, it will ask who is similar to Tom.
***
### Syllogistic Pattern

Dependent Variables and Independent Variables only get introduced in the following semantic patterns:

```
               independent                    dependent
extensional    <<$x --> S> ==> <$x --> P>>    (&&,<#x --> S>,<#x --> P>)
intensional    <<P --> $x> ==> <S --> $x>>    (&&,<S --> #x>,<P --> #x>) 
```

and for equivalence

```
               independent                    dependent
extensional    <<$x --> S> <=> <$x --> P>>    <#x --> (&,S,P)>
intensional    <<P --> $x> <=> <S --> $x>>    <(|,P,S) --> #x>
```

For example <<$x --> S> ==> <$x --> P>>. can be created based on <a --> S> and <a --> P> because <<$x --> S> ==> <$x --> P>> matches <<$x --> S> ==> <$x --> P>> by instantiating $x with a.