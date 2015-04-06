> ### Application Programs  
> Example Programs using OpenNARS as AI.

***

### Introduction

Since NARS is now very stable and has reached a certain value of stability and completeness, we decided to create first application examples, also to proof that NARS works in domains where usual a lot of domain knowledge and special algorithms are needed, also domains where usually numerical learning algorithms are applied are shown. In all these examples, NARS is used and no special domain knowledge is input. (although NARS can make use of such knowledge) Usually AI in such domains is unable to master other such domains without programmatical changes and reconfigurations, or even more important, is not able to take knowledge from one domain to the other even if some situations might be similar and be solvable by similar solutions.

### Test Chamber

Originally built just to be able to visualize the existing NAL8 examples, it is now our most complex application example, there you can built arbitrary logic circuits, combine doors and lights with switches, create machines which produce pizza, build arbitrary mazes the system has to solve on the fly, where the structure of the circuits are not known to NARS (if not especially told). This example will one day show the difference between narrow AI and general AI very clearly, and already now is a source of very interesting results.

**The Elements of TestChamber in the left window from top to bottom:**

* Structural: Place walls, floors, or water

* Logic: Build logic circuits

* Machine: Add things like lights, doors etc.

**Force Action: Force NARS to do something.**

* "Go-To named" will make NARS go to a clicked place.

* "Pick named" will make NARS pick an object, for example a key.

* "Activate switch" will make it activate a switch.

* "Deactivate switch" deactivates it.

* perceive/name lets you define new places, you can let NARS go there with "Go-To named" or force the goal "be somewhere" on it in which case NARS will try to get there whatever it involves to make this possible.

**Request Goal: Force NARS to desire something.**

* try things will desire applying its actions on different things, which keeps it active. (Since desires like visiting switches will develop anyway, this is not needed after some training, at least if tell object categories is enabled)

* be somewhere: let NARS desire to be somewhere

* hold something: let NARS desire to pick up something, like for example a key

* make switched on: let NARS desire to make a light or a switch switched on

* make switched off: same for off

* make opened: let NARS desire to open a door

* make closed: same for closing

* be chatty: let NARS gain some defined experience in which case it will get desire to apply the say operator in different situations. (experimental, but works, communication as a desire, see more in: [NaturalLanguageProcessing](https://github.com/opennars/opennars/wiki/Natural-Language-Processing) wiki page)

Predefine knowledge: Contains some knowledge one can tell it about, like for example common sense in which case it will have evidence that it is somewhere after it goes there, that it holds something after it picks it, made to speed up learning a bit.

Need of resources: if "need pizza" is activated, a hunger goal will show up in NARS from time to time, which is only achievable by eating pizza which can be placed.

**Advanced Settings:**

* Allow joy in action inserts forced actions as goal, in which case they will gain desire of forced actions directly, which distracts NARS from doing what you want often as consequence, but allows it to like repeating what it was forced to do. Don't allow joy deactivates this mode.

* Tell object categories will suggest a category to objects which get placed, like when {key0} is placed, NARS will get the information <{key0} --> key>, what this means NARS needs to find out, but it helps NARS to categorize keys together for example, and not just objects by what actions leaded to what changes in the environment (in which case NARS would develop its own concept of key, but this can happen despite the given information, but the similarity of the own concept to key will likely be recognized)

* Load/Save: Save the state of TestChamber into a file, or load examples we provided.

![testchamber](https://cloud.githubusercontent.com/assets/11791925/6994213/5a8a3f5e-db43-11e4-8097-fe40c33d5f2b.png)

####Rover

This is a usual reinforcement learning example, except that the input space is so complex that usually a classifier is applied. NARS has to correlate sequences of actions itself with getting the reward from its body when an object has been catched, like a simple lifeform searching for food, driven by body rewards.

![rover](https://cloud.githubusercontent.com/assets/11791925/6994215/75e88512-db43-11e4-964f-2f2d82151b0e.png)

####Tic Tac Toe

This example is our first board game. Board games have a lot of implicit assumptions we are not aware of when we play them: Objects stay where they are until someone moves them. The board situations are most times not dependent on time at all, most times not even on how the situation looked like previously. Where a stone is at a certain position there can't be another one at the same place, nor can a stone be at different positions in the same game situation. Randomized things may not happen for a certain reason even if there is always some artificial reason to find for something happening. This example shows the extreme case, where not only the board game rules have to be learnt, but also typical assumptions of board games itself. In order to give information of whether NARS thinks about a certain board position, the current priority of the tile is visualized with color.

![nartactoe](https://cloud.githubusercontent.com/assets/11791925/6994217/8a64930a-db43-11e4-9bc0-324669fc46fa.png)

####Nario

More complex games like jumps and runs usually have a lot more than above, logical strict coherences in the environment, physical dynamics, partial observability, and so on. AI which plays such games usually knows everything (has a complete model), or has tremendous training time or computational resources in order to deal with such domains. Here you can watch NARS starting from no knowledge, learning to jump over obstacles and so on.

![nario](https://cloud.githubusercontent.com/assets/11791925/6994223/a0cee2f8-db43-11e4-94cf-e1fd6f4a1fd6.png)

####Predict

This example uses NARS to predict the future values of an observed curve: This is where knowledge about often used functions like sine can help a lot, but also here we decided to don't tell the system about anything by default, so that it has to built logic on how the values may behave in future entirely on its own.

![predict](https://cloud.githubusercontent.com/assets/11791925/6994227/b5020c6e-db43-11e4-9150-c6aa4ee797b6.png)

####Natural Language and Structure Learning

Besides the fact that the input window gives information about syntax error in your Narsese, input is not restricted to Narsese. You can put in any data you can think of, in which case it will be up to NARS to make sense of it in the current situation (for example in the examples).

![structurelearning](https://cloud.githubusercontent.com/assets/11791925/6994232/ca3ba5ae-db43-11e4-86c0-c5cfedee3be6.png)

Such a input will be transformed to

```
<(*,word-tom,word-is,word-the,word-brother,word-of,word-me) --> linepart>. :|: %1.00;0.90%
```

where the words are changed automatically so that the words won't conflict with the concepts the words represent. See [Natural Language Processing](https://github.com/opennars/opennars/wiki/Natural-Language-Processing) wiki page for more information of how such a input will be interpreted according to the current situation. Also "Natural Language Processing by Reasoning and Learning" is relevant here: http://www.cis.temple.edu/~pwang/Publication/NLP.pdf