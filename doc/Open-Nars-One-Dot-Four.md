# New functions of Open-NARS version 1.4.x.

# Introduction

Open-NARS 1.4 introduces the following major functions into the system:
1. real-time procedural inference
1. emotion and global indicators
1. self-monitor and self-control

In version 1.4.0, each of the above functions is implemented in its preliminary form. Until the functions are fully tested, the on-line demonstration will still be the previous stable version, 1.3.3, though the source code of 1.4.x will be uploaded from time to time.


# Real-time procedural inference

Now the system takes the time interval between events into consideration, in terms of internal clock cycles. For example,
      IN: a. :|: %1.00;0.90% {0 : 0 : 1} 
    5
      IN: b. :|: %1.00;0.90% {5 : 5 : 2} 
    1
     OUT: <b =\> (&/,a,(^wait,5))>. %1.00;0.45% {6 : 1;2} 
     OUT: <(&/,a,(^wait,5)) =/> b>. %1.00;0.45% {6 : 1;2} 
     OUT: <(&/,a,(^wait,5)) </> b>. %1.00;0.45% {6 : 1;2} 
The "{0 : 0 : 1}" after the first task is a "time stamp", which contains three sections separated by symbol ':'. The first number is the creation time of the judgment, the second is the time the event happens, and the last one is a sequence of serial numbers of input sentence, from them the current task is derived. The "event time" section (the middle one) is optional, and if it is missing, the judgment is about "all the time", rather than about any specific moment. This is the case for the last three sentences of the previous example. The operator *^wait* let the system anticipate a future event, after a given number of clock cycles. In the following example, if event *a* is believed to be followed by event *b* after 5 cycles, the system will expect *b* to happen at that moment.
      IN: <(&/,a,(^wait,5)) =/> b>. %1.00;0.90% {0 : 1} 
    1
      IN: a. :|: %1.00;0.90% {1 : 1 : 2} 
    3
     OUT: b. :/: %1.00;0.81% {4 : 6 : 1;2} 
Such an anticipation will produce a (positive or negative) feedback, which will revise the belief about events implying each other.


# Emotion and global indicators

The primary function of emotion is to provide evaluation to situations from the system's viewpoint, and regulate the system's actions accordingly. In biological systems, emotion and feeling provide the primary way for statements to get desire-values, and for some of them to become goals of the system. In computational systems, it is possible to directly assign desire-values to some statements, and use inference to determine the desire-value of the other statements. Even so, emotional factors still play important roles in the system, such as in the decision-making rule, where statements with higher desire-values are more likely to become goals actually pursued by the system.

Furthermore, in version 1.4, a few indicators are introduced to summarize the current status of the system in different dimensions. The first two are:
- *busy*: this number measures the average resource budget of the recently accepted tasks,
- *happy*: this number measures the average satisfactions of the recently pursued goals.
These factors will be taken into consideration by the control mechanism in various points. For example, when the system is busy, tasks with low resource budget will be simply ignored to achieve high efficiency. 


# Self-monitor and self-control

In previous versions, all input tasks come from the outside environment. In version 1.4.0, the system gets the ability to remember some of its internal events. Especially, it remembers its direct processing of each task as the execution of an operation:
- Operator *^believe* takes a statement and a truth-value as arguments, and its effects is to compose a *belief* from the two, and add it into the associated concept.
- Operator *^want* takes a statement and a desire-value as arguments, and its effects is to compose a *goal* from the two, and add it into the associated concept.
- Operator *^wonder* takes a statement as argument, and its effects is to compose a *question* on the *truth-value* of the statement, and add it into the associated concept.
- Operator *^assess* takes a statement as argument, and its effects is to compose a *question* on the *desire-value* of the ststement, and add it into the associated concept.
Now the above four operators correspond to the four types of sentence in Narsese, respectively. In the first two operations, the "truth-value" and "desire-value" take their "verbal" form, and only have three possible values: "TRUE", "FALSE", and "UNSURE". The conversion from the numerical form to the verbal form is based on the expectation function of the former.

Coming together with this *self-awareness* is the corresponding *self-control* mechanism. Now the system can including the above "inside-oriented" operations, just like how it treats "outside-oriented" operations defined in NAL-8. Especially, the system can explicitly invoke these operations, which serves as a *supplement* (not a *replacement*) of the default inference control mechanism.