> ### Natural Language Processing  
> How to provide natural language experience.

***

This is a NLP wiki article for OpenNarsOneDotSixDotOne. Here we will borrow needed ideas from Dr. Pei Wang's publication "Natural Language Processing by Reasoning and Learning", which also presents the idea that natural language processing in AGI, especially in NARS, can be done by the inference rules an AGI provides anyway. #summary How to give NARS natural language experience.

To motivate the examples shown here, two things I consider important: Is there really a way to create "natural language understanding" without using AGI itself? I doubt it, natural language sentences most time talk/are related to things in the environment the AGI perceives (the meaning of a sentence is not inherent in a sentence itself, but by what it relates to), and need a flexible interpretation (for example AGI agents would probably create a language on their own if the current situation demands efficient communication between them). Furthermore I think that natural language processing has to be an "active" and "selective" process which completely shares the goals of the AGI in order to cooperate correctly.

####Selectiveness:

Imagine we have short time to find some important detail in a big text. What parts we will read more carefully in the "fast searching process" will also depend on what parts we judge to be possibly related to the detail we are searching for and what we know about this domain. Also if we are not short in time, we will not remember every detail and all possible interpretations, but we will tend to remember things we consider to be representative/important.

####Activeness:

Just because a sentence describes the current situation, doesn't mean that we will say it. If, and also how we will formulate it, depends on what the current goals demand.

Now we will look at a example which shows exactly those 2 principles, while using like in Pei's NLP paper, a different concept for the word (noted with small letters), and the concept it represents (big letters), to allow a many-to-many mapping like the paper suggests. However we will not explicitely define a REPRESENT relation which maps Englisch to Narsese, instead we assume that the system itself will create a representation of REPRESENT in it by the temporal observations it will make:

Let's assume the following event happened:

```
IN: <(*,CAT,FISH) --> EATS>. :|:

6
```

and some person now says the follwing (like shown in the paper, we express sentences as products):

```
IN: <(*,cat,eats,fish) --> sentence>. :|:
```

first thing which will happen by temporal induction is that NARS will relate the event that the cat eats fish, with the sentence which was told shortly after it.

```
OUT: <(&/,<(*,CAT,FISH) --> EATS>,+1) =/> <(*,cat,eats,fish) --> sentence>>. :|: %1.00;0.45%
```

However by the second property of "activeness", we will make "saying sentences" an operation, and define a goal which has as consequence the saying of sentences under certain conditions. Let's for simplicity just tell NARS that it should say sentences when they are representative for the current situation:

```
IN: <(&&,<$1 --> sentence>,(^say,$1)) =/> <NARS --> chatty>>.

IN: <NARS --> chatty>!
```

as consequence, when now the event happens again:

`IN: <(*,CAT,FISH) --> EATS>. :|:`

NARS will execute telling the sentence that cat eats fish:

`EXE: ^say [(*,cat,eats,fish)]`

just because it is its current goal to do so (Activeness). Also if the event now happens again, NARS may not trigger the telling of the sentence again, maybe because it already told it just some steps ago in which case the goal to be chatty is currently already fullfilled, or because the goal lost importance.

Also there may be "hidden interests" behind telling something, like we observe every day, with this approach this also holds for NARS. NARS may tell a sentence for different reasons than the content may indicate. (highly related: lying)

So far we only demonstrated Activeness and at the same time presented a representation of NLP you may use,

***

but let's go on with the role of Selectiveness for completeness:

This one comes entirely for free in the NARS system, let's say NARS observed two different formulations of the same event:

```
OUT: <(&/,<(*,CAT,FISH) --> EATS>,+1) =/> <(*,cat,eats,fish) --> sentence>>. 

OUT: <(&/,<(*,CAT,FISH) --> EATS>,+1) =/> <(*,the,cat,eats,fish) --> sentence>>. 
```

and lets redefine the goal state to prefer sentences which starts with "the"

```
IN: <<(*,the,$1,$2,$3) --> sentence> ==> <(*,the,$1,$2,$3) --> starts_with_the>>.

IN: <(&&,<$1 --> starts_with_the>,<$1 --> sentence>,(^say,$1)) =/> <NARS --> chatty>>.
```

now

```
(&/,<(*,CAT,FISH) --> EATS>,+1) =/> <(*,the,cat,eats,fish) --> sentence>>.
```

will get higher priority because it more often leads to fullfilling the goal state than

```
<(&/,<(*,CAT,FISH) --> EATS>,+1) =/> <(*,cat,eats,fish) --> sentence>>. 
```

and thus the second concept will not be considered that much often anymore (selectiveness).

Also if and how a sentence gets recognized and if and how NARS takes time to interpret it and think about the consequences in the current context, may depend on what concepts currently are considered being important (most probably the ones related to the current goals)

***

####Teaching NARS NLP:

The examples above at the same time also suggest a way how natural language can be teached to NARS:

```
'concept to englisch pattern_1:
IN: <(*,CAT,FISH) --> EATS>. :|:
6
IN: <(*,the,cat,eats,fish) --> sentence>. :|:
6
'concept to englisch pattern_2:
IN: <(*,TIM,SPORT) --> LIKES>. :|:
6
IN: <(*,tim,likes,also,sport) --> sentence>. :|:
...
6
'concept to englisch pattern_n:
IN: <(*,TOM,TENNIS) --> PLAYS>. :|:
6
IN: <(*,tom,plays,tennis) --> sentence>. :|:
'optional list of meanings of the involved concepts by relating them together
'(but NARS will relate the concepts according to it's experience anyway):
<TENNIS --> SPORT>.
<TENNIS --> ACTIVITY>.
...
```

####Others:

This example was using sentence-level, for example

```
IN: <(*,the,cat,will,now,eat,fish) --> sentence>. :|:
6
IN: <(*,CAT,FISH) --> EATS>. :|:
```

results in

```
<<(*,the,cat,will,now,eat,fish) --> sentence> =/> <(*,CAT,FISH) --> EATS>>.
```

However this also works for word level, which may give some advantages like that NARS can learn to create own sentences:

```
IN: <the --> word>. :|:
6
IN: <cat --> word>. :|:
6
IN: <will --> word>. :|:
6
IN: <now --> word>. :|:
6
IN: <eat --> word>. :|:
6
IN: <fish --> word>. :|:
6
IN: <(*,CAT,FISH) --> EATS>. :|:
```

may, if it happens often, result in a chain like:

```
(&/,<the --> word>,<cat --> word>,<will --> word>,<now --> word>,<eat --> word>,<fish --> word>) =/> <(*,CAT,FISH) --> EATS>>.
```

But here also lies the disadvantage, because it needs more training than the sentence-level approach, because who says that the entire sequence of words described that the cat will eat fish? Maybe just the last word ore the last two words were essential?

Hypotheses like

`<<fish --> word> =/> <(*,CAT,FISH) --> EATS>>.`

or

`<(&/,<eat --> word>,<fish --> word>) =/> <(*,CAT,FISH) --> EATS>>.`

will also compete for priority. Interpretation and understanding of natural language is same like interpreting and understanding its environment, a ever-changing process which demands AGI, there is no way around.