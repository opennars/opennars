> ### Reinforcement Learning  
> NARS in the RL domain.

***

### Introduction

The domain of classical reinforcement learning is from AGI perspective a very specialized domain, however, if we consider some interesting extensions at once:

- stochastic environment

- partial observability

- delayed reward

- multi-objective

- sparse RL

- RL with markov logic networks

- lifelong learning

and even if the to these topics proposed related solutions are mostly incompatible with each other, if they could be combined we would be in a domain which is not that far away from AGI. But there are still some additional demanded properties:

- Random exploration of action set should always be the AGI's decision and not pre-defined for the specific problem. (see next section)

- The system has to make sense of observations even if no goals exist in the system, and make use of them when goals suddenly come into the system.

- Under the assumption that AIKR is required, the system has not only to be able to communicate an elegant representation of its observations but also input should be able influence the attention of the system.

- And finally, the system may need to be able to observe, reason about and control its own internal thought processes which is at least a key property of human intelligence.

***

### How to use NARS in RL domains

-Actions:-

Let's say we have a finite set of actions: A={a1,...,an}.

In reinforcement learning approaches there is usually a randomization probability, which determines with what probability in the current step, a random element of A is chosen. Usually there is also a fixed decrease-function pre-defined, which is basically problem-specific knowledge, which lowers the randomization-probability in every step.

But with NARS there is a solution to this dilemma: A meta-action ^r, "select a random element of A", where A is presented as

`<{a1,...,an} --> A>.`

an operator ^r is introduced, and an additional goal of average importance which says

`(^r,A)!`

"choose an random action sometimes". The consequence of this action is execution of a random action in the simulation, and giving NARS the information that it was executed, for example

`(^pick,key). :|:`

if (^pick,key) was in A,

in which case NARS will:

1. Learn the consequences of selecting a random action in a certain situtation like it learns the consequence of any other action.

2. Form own sets of actions and apply this meta-action on this new sets.

3. Decide on its own when it chooses a random action, when it has a better idea in a certain situation, it will tend to not do so while a RL approach wouldn't have any control about this. (This principle we may also apply to the mental operators, as innate low priority goals)

-Reinforcement:-

In simplest case in order to represent a binary reward value, it would just be

`<{nars} --> [good]>. :|: `

in case of good reward, and

`(--,<{nars} --> [good]>). :|:`

in case of bad reward.

However, NARS is not restricted to this, you can give anything you can express in Narsese as feedback, and also as goal or other hint information.

The goal in this case would be for example:

`<{nars}--> [good]>!`

so that it will desire this state and try to achieve it.

-Input:-

This one works analogous,

`<{door} --> [opened]>. :|: `

In order to represent numeric values, it is a good idea to give it information of how the numbers are subjectively related:

```
<0.0 <-> 0.1>.
<0.1 <-> 0.2>.
<0.2 <-> 0.3>.
<0.3 <-> 0.4>.
<0.4 <-> 0.5>.
<0.5 <-> 0.6>.
<0.6 <-> 0.7>.
<0.7 <-> 0.8>.
<0.8 <-> 0.9>.
```

***

### Conclusion

RL domains can be well represented in NARS, and a lot of RL limitations are overcome by it. How it performs in such domains may become a practical interest in future.