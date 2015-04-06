> ### OpenNARS One Dot Six Dot Two  
> What is new in OpenNARS-1.6.2.

***

### Introduction

OpenNARS-1.6.2 is the newest version, which besides of some bugfixes, implements some ideas to make it easier for the system to deal with perception-intense tasks.

***

### News List

* Perception Variable Elimination Rule: https://groups.google.com/forum/#!topic/open-nars/8VVscfLQ034

* Conclusion Depth / Budget Leak Coefficient: https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs

* Tasklink Novelty for Exotic Inference Rules https://groups.google.com/forum/#!topic/open-nars/0k-TxYqg4Mc

* Interval Cut https://groups.google.com/forum/#!topic/open-nars/DojlU8_4R60

* Revised Stamp Policy https://groups.google.com/forum/#!topic/open-nars/FVbbKq5En-M

* Perception Variable Introduction Rule https://groups.google.com/forum/#!topic/open-nars/uoJBa8j7ryE

* Conjunction Flattening (also Perception related, eliminating equal but difficult to revise residues) https://groups.google.com/forum/#!topic/open-nars/VQZF1Fz_t9I

* Fix of structuralCompound function in StructuralRules.java (incomplete condition)

* Temporal-Spatial Perception: https://groups.google.com/forum/#!topic/open-nars/WBEFhlgp49A

* inference rule which uses this Perception to speedup Decision Making: https://groups.google.com/forum/#!searchin/open-nars/0which0can0directly0make0use0of0your0perception0ideaA/open-nars/B8veE-WDd8Q/VHaPHDraqeIJ

* Anticipation (which was introduced in 1.6.1 but bugged): https://groups.google.com/forum/#!topic/open-nars/YZC2ThhGXXw

* Coordination Problem Fix https://groups.google.com/forum/#!topic/open-nars/WvrKk03THtA

* Software-Side: Some Testchamber level loading and inconvenience fixes and some GUI stability fixes + minor additions like comments // do now show in output log and RESET also resets the seed.

* Robotics (here for the first time): a little Arduino robot code which is able to use NARS operators for motor control, with a simple vision system sophisticated enough that for example NARS will learn to chase a red ball if it gets motivated to do so, or to learn to classify objects by what colors the contain and what size they have etc.