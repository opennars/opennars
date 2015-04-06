> ### Videos  
> Description of the Video Examples

***

### Introduction

There are already videos which show what OpenNARS is capable of. Since Youtube supports timing, descriptions of the situations and a direct link is given:

### Relating parts of Natural Language

https://www.youtube.com/watch?v=BBmim5CTE4k&t=01m14s

This example shows that when two nearly identical sentences are added, where just one word is different, that already the property of being in the sentence at same position, will cause NARS to relate them. This is rather abstract, but it shows what we target for: Instead of having a narrow-AI subsystem which pre-determines the meaning of sentences, NARS will itself interpret and relate sentences and parts of them as previous experience suggests. Fixed interpretations would conflict with AGI fundamentally. For example "pick the key up" may at the beginning not have any meaning to NARS but this doesn't make the sentence useless, for example when you say it while you let it pick up a key, NARS will relate it automatically, and next time it might go-to and pick up the key just because you said "pick the key up", the situations it occured in formed its meaning to NARS. This is how we humans learn natural language in reality, and this is what NARS aims for.

### Make Light On in Closed Room

https://www.youtube.com/watch?v=BBmim5CTE4k&t=01m48s

This example shows a every-day situation: Having to activate a switch to open a door to a room, entering it, and activating another switch in that room to turn a light on.

In this example, NARS starts without prior knowledge about anything. We force it to execute the previous described sequence two times, to make sure it observed, that this sequence leaded to "light on". However there was no goal like "light on" when we forced NARS to execute the sequence. Instead, we add "light on" as goal later, which ultimatively shows, that NARS is able to relate a new goal which pops up later ("light on"), with its past experience it previously observed. Shown is that it then recalls what happened in the past, and uses the same sequence of actions to make this goal true.

This also shows a big difference to Reinforcement Learning, where a goal has to be defined before observation begins. Also different is that NARS will answer to very high-level questions like: "do both switches open the door?", which also sets it widely apart of the scope of Reinforcement Learning and also of all other current AGI approaches.

### Make Light On

https://www.youtube.com/watch?v=NfMvMOeC_rU&t=1m54s

This is a old simpler version of the "Make Light On in Closed Room" example, which already the current release version OpenNARS 1.6.0 is able to do. However after release we played a lot with control mechanism ideas and tuning, with success, so for more complex examples like "Make Light On in Closed Room" the git version (which will result in 1.6.1 maybe soon) has to be used.

### Move to new place

https://www.youtube.com/watch?v=NfMvMOeC_rU&t=5m56s

This example shows, how NARS generalizes what it finds out about the go-to operations. At first it recognizes, after several go-tos, that after go-to X, at X is relatively sure true. (first induction) and then it recognizes that this always holds, that this is not dependent on time. With this knowledge it successfully moves to a new place it was never before just by getting told that it should be at this new place.