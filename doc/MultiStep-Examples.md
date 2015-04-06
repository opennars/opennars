> ### Multi-Step Examples  
> Inference Examples 

***

At the current stage, Open-NARS is not ready to handle complicated inference processes consisting many steps. Even so, this file contains a group of multi-step inference examples to show the expressive and inferential capability of NARS, as implemented in Open-NARS version 1.5.5. The basic format and usage are the same as explained in [Single Step Testing Cases](https://github.com/opennars/opennars/wiki/Single-Step-Testing-Cases).

In the following, each example starts at a line with a "*" to empty the memory of the system. To run an example, copy/paste the input lines into the input window of the NARS. Listed after the input lines are the lines displayed in the main window of NARS during the processing of the input, followed by a brief explanation of the example.

To only display the relevant output, in the main window of NARS go to menu item "Paremeter", then select "Report Silence Level", and move the scrollbar to its right-most position (value 100). Click "Hide" to finish the change.

***

**Choice**

Input:

```
*
<robin --> bird>.
10
<swan --> bird>. %1.00;0.80%
<penguin --> bird>. %0.80;0.95%
5
<?1 --> bird>?
800
```

Display:

```
IN: <robin --> bird>. %1.00;0.90% {0 : 1} 
10
IN: <swan --> bird>. %1.00;0.80% {10 : 2} 
IN: <penguin --> bird>. %0.80;0.95% {10 : 3} 
5
IN: <?1 --> bird>?  {15 : 4} 
448
OUT: <(|,robin,(&,penguin,swan)) --> bird>. %1.00;0.68% {338 : 3;1;2} 
OUT: <penguin --> bird>. %0.80;0.95% {10 : 3} 
291
OUT: <swan --> bird>. %1.00;0.80% {10 : 2} 
46
OUT: <robin --> bird>. %1.00;0.90% {0 : 1} 
```

When a question has more than one candidate answers, their order of evaluation is highly context-sensative. The system reports the best answer it has found so far, and therefore may report more than one answer to a given question. In this example, the system will settle down at the last answer even if it is given longer time.

***

**Contradiction**

Input:

```
*
<coffee --> beverage>.
<Java --> coffee>.
(--,<Java --> coffee>).
10 
<Java --> coffee>?
10
<tea --> beverage>?
10
<coffee --> beverage>?
10
```

Display:

```
IN: <coffee --> beverage>. %1.00;0.90% {0 : 1} 
IN: <Java --> coffee>. %1.00;0.90% {0 : 2} 
IN: (--,<Java --> coffee>). %1.00;0.90% {0 : 3} 
10
IN: <Java --> coffee>?  {10 : 4} 
1
OUT: <Java --> coffee>. %0.50;0.95% {5 : 2;3} 
9
IN: <tea --> beverage>?  {20 : 5} 
10
IN: <coffee --> beverage>?  {30 : 6} 
1
OUT: <coffee --> beverage>. %1.00;0.90% {0 : 1} 
```

A contradiction makes the system unsure on directly related questions, but will not make the system to derive an arbitrary conclusion on other questions, as in propositional logic.

***

**Confidence and revision**

Input:

```
*
<{Willy} --> swimmer>.
<fish --> swimmer>.
<{Willy} --> fish>?
20
<{Willy} --> whale>.
<whale --> [black]>. 
<{Willy} --> [black]>? 
31
<{Willy} --> [black]>. %0% 
<{Willy} --> fish>. %0% 
2
```

Display:

```
IN: <{Willy} --> swimmer>. %1.00;0.90% {0 : 1} 
IN: <fish --> swimmer>. %1.00;0.90% {0 : 2} 
IN: <{Willy} --> fish>?  {0 : 3} 
2
OUT: <{Willy} --> fish>. %1.00;0.45% {1 : 2;1} 
18
IN: <{Willy} --> whale>. %1.00;0.90% {20 : 4} 
IN: <whale --> [black]>. %1.00;0.90% {20 : 5} 
IN: <{Willy} --> [black]>?  {20 : 6} 
26
OUT: <{Willy} --> [black]>. %1.00;0.81% {45 : 5;4} 
5
IN: <{Willy} --> [black]>. %0.00;0.90% {51 : 7} 
IN: <{Willy} --> fish>. %0.00;0.90% {51 : 8} 
1
OUT: <{Willy} --> [black]>. %0.00;0.90% {51 : 7} 
OUT: <{Willy} --> fish>. %0.00;0.90% {51 : 8} 
1
OUT: <{Willy} --> [black]>. %0.32;0.93% {52 : 5;7;4} 
OUT: <{Willy} --> fish>. %0.08;0.91% {52 : 2;8;1} 
```

Even when all the input judgments using the default confidence value, different rules produce conclusions with difference confidence, which have different sensitivity when facing the same amount of new evidence.

***

**Deduction chain**

Input:

```
*
<Tweety {-- robin>.
<robin --> bird>.
<bird --> animal>.
30 
<Tweety {-- bird>?
10
<Tweety {-- animal>?
1
```

Display:

```
IN: <{Tweety} --> robin>. %1.00;0.90% {0 : 1} 
IN: <robin --> bird>. %1.00;0.90% {0 : 2} 
IN: <bird --> animal>. %1.00;0.90% {0 : 3} 
30
IN: <{Tweety} --> bird>?  {30 : 4} 
1
OUT: <{Tweety} --> bird>. %1.00;0.81% {1 : 2;1} 
9
IN: <{Tweety} --> animal>?  {40 : 5} 
1
OUT: <{Tweety} --> animal>. %1.00;0.73% {20 : 3;1;2} 
```

The conclusion of a previous step may be used as a premise in a following step. In the example, though both answers are positive (with frequency 1), their confidence is getting lower as the deduction chain gets longer.

***

**Resemblance Chain**

Input:

```
*
<dog <-> cat>. %0.9%
<cat <-> tiger>. %0.9%
<tiger <-> lion>. %0.9%
<dog <-> lion>?
30
```

Display:

```
IN: <cat <-> dog>. %0.90;0.90% {0 : 1} 
IN: <cat <-> tiger>. %0.90;0.90% {0 : 2} 
IN: <lion <-> tiger>. %0.90;0.90% {0 : 3} 
IN: <dog <-> lion>?  {0 : 4} 
26
OUT: <dog <-> lion>. %0.73;0.71% {25 : 3;1;2} 
```

Given incomplete similarity, both frequency and the confidence decrease alone an inference chain.

***

**Induction and revision**

Input:

```
*
<bird --> swimmer>?  
<swimmer --> bird>? 
10
<swan --> bird>.
<swan --> swimmer>.
10
<gull --> bird>.
<gull --> swimmer>.
40
<crow --> bird>.
(--,<crow --> swimmer>).
50
```

Display:

```
IN: <bird --> swimmer>?  {0 : 1} 
IN: <swimmer --> bird>?  {0 : 2} 
10
IN: <swan --> bird>. %1.00;0.90% {10 : 3} 
IN: <swan --> swimmer>. %1.00;0.90% {10 : 4} 
8
OUT: <swimmer --> bird>. %1.00;0.45% {17 : 4;3} 
OUT: <bird --> swimmer>. %1.00;0.45% {17 : 4;3} 
2
IN: <gull --> bird>. %1.00;0.90% {20 : 5} 
IN: <gull --> swimmer>. %1.00;0.90% {20 : 6} 
34
OUT: <swimmer --> bird>. %1.00;0.62% {53 : 4;6;3;5} 
OUT: <bird --> swimmer>. %1.00;0.62% {53 : 4;6;3;5} 
6
IN: <crow --> bird>. %1.00;0.90% {60 : 7} 
IN: (--,<crow --> swimmer>). %1.00;0.90% {60 : 8} 
45
OUT: <bird --> swimmer>. %0.67;0.71% {104 : 4;8;6;7;3;5} 
```

(1) Question may still be remembered before available knowledge arrives, or after answers are reported; (2) The system can change its mind when new evidence is taken into consideration; (3) Positive evidence has the same effect on symmetric inductive conclusions, but negative evidence does not.

***

**Mixed Inference**

Input:

```
*
<swan --> bird>.
<swan --> swimmer>.
5
<bird --> swimmer>?
8
<gull --> bird>.
<gull --> swimmer>.
31
<bird --> [feathered]>.
<robin --> [feathered]>.
20
<robin --> bird>?
11
<robin --> swimmer>?
200
```

Display:

```
IN: <swan --> bird>. %1.00;0.90% {0 : 1} 
IN: <swan --> swimmer>. %1.00;0.90% {0 : 2} 
5
IN: <bird --> swimmer>?  {5 : 3} 
7
OUT: <bird --> swimmer>. %1.00;0.45% {5 : 2;1} 
1
IN: <gull --> bird>. %1.00;0.90% {13 : 4} 
IN: <gull --> swimmer>. %1.00;0.90% {13 : 5} 
11
OUT: <bird --> swimmer>. %1.00;0.62% {23 : 2;5;1;4} 
20
IN: <bird --> [feathered]>. %1.00;0.90% {44 : 6} 
IN: <robin --> [feathered]>. %1.00;0.90% {44 : 7} 
20
IN: <robin --> bird>?  {64 : 8} 
10
OUT: <robin --> bird>. %1.00;0.45% {73 : 7;6} 
1
IN: <robin --> swimmer>?  {75 : 9} 
154
OUT: <robin --> swimmer>. %1.00;0.28% {228 : 2;7;5;6;1;4} 
```

The final conclusion is produced using induction, abduction, deduction, and revision. The selection of inference rule is data driven, not specified explicitly in the input. There is no guarantee that all relevant evidence will be taken into consideration.

***

**Semi-compositionality**

Input:

```
*
<(&,light,[red]) --> traffic_signal>? 
5
<light --> traffic_signal>. %0.1%  
<[red] --> traffic_signal>. %0.1%
196
<{light_1} --> (&,light,[red])>. 
<{light_1} --> traffic_signal>.
50
<{light_2} --> (&,light,[red])>. 
<{light_2} --> traffic_signal>. 
30
```

Display:

```
IN: <(&,light,[red]) --> traffic_signal>?  {0 : 1} 
5
IN: <light --> traffic_signal>. %0.10;0.90% {5 : 2} 
IN: <[red] --> traffic_signal>. %0.10;0.90% {5 : 3} 
4
OUT: <(&,light,[red]) --> traffic_signal>. %0.10;0.08% {8 : 3} 
7
OUT: <(&,light,[red]) --> traffic_signal>. %0.10;0.15% {15 : 3;2} 
154
OUT: <(&,light,[red]) --> traffic_signal>. %0.19;0.81% {169 : 3;2} 
31
IN: <{light_1} --> (&,light,[red])>. %1.00;0.90% {201 : 4} 
IN: <{light_1} --> traffic_signal>. %1.00;0.90% {201 : 5} 
27
OUT: <(&,light,[red]) --> traffic_signal>. %0.32;0.84% {227 : 3;4;2;5} 
23
IN: <{light_2} --> (&,light,[red])>. %1.00;0.90% {251 : 6} 
IN: <{light_2} --> traffic_signal>. %1.00;0.90% {251 : 7} 
30
OUT: <(&,light,[red]) --> traffic_signal>. %0.41;0.85% {280 : 3;6;4;7;2;5} 
```

Initially, the meaning of compound term "(&,red,light)" is determined by the meaning of its components "red" and "light", but it will no longer be the case when the system gets experience about the compound that cannot be reduced to its components.

***

**Fuzzy Concept**

Input:

```
*
<{John} --> boy>.
<{John} --> (/,taller_than,{Tom},_)>.
5
<{Tom} --> (/,taller_than,_,boy)>? 
251
<{David} --> boy>. 
(--,<{David} --> (/,taller_than,{Tom},_)>).
135
<{Karl} --> boy>. 
<{Karl} --> (/,taller_than,{Tom},_)>.
141
```

Display:

```
IN: <{John} --> boy>. %1.00;0.90% {0 : 1} 
IN: <{John} --> (/,taller_than,{Tom},_)>. %1.00;0.90% {0 : 2} 
5
IN: <{Tom} --> (/,taller_than,_,boy)>?  {5 : 3} 
246
OUT: <{Tom} --> (/,taller_than,_,boy)>. %1.00;0.45% {250 : 2;1} 
5
IN: <{David} --> boy>. %1.00;0.90% {256 : 4} 
IN: (--,<{David} --> (/,taller_than,{Tom},_)>). %1.00;0.90% {256 : 5} 
113
OUT: <{Tom} --> (/,taller_than,_,boy)>. %0.50;0.62% {368 : 2;5;1;4} 
22
IN: <{Karl} --> boy>. %1.00;0.90% {391 : 6} 
IN: <{Karl} --> (/,taller_than,{Tom},_)>. %1.00;0.90% {391 : 7} 
141
OUT: <{Tom} --> (/,taller_than,_,boy)>. %0.67;0.71% {531 : 2;7;5;6;1;4} 
```

John's degree of membership to fuzzy concept "tall boy" depends on the extent to which he is taller than the other boys, determined according to available evidence.
