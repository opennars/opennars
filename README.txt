Source Code
-----------
Under the nars/ directory is the code Pei Wang originally moved into the project, which is still the base of his own programming. This is no active anymore, replaces by nars_core_java/ and nars_gui/ .

Later Joe Geldart started the open-nars version of NARS, which contains many good ideas (many of which are accepted into 1.5), but it isn't fully consistent with Pei's plan, especially about the new layers (7,8,9), so Pei didn't continue on that code base.

In nars_core_java/ and nars_gui/ are the NARS core and the Swing GUI in Java. This came out of the code of Pei Wang in nars/ directory. This is the currently active code.

nars-dist/ contains a NARS distribution (executable, examples, web page with applet), that could be zipped for releases.

In nars_core/ there is an embryo of NARS in Scala (not currently active, just to see how NARS could look in Scala).

The test suite is nars_core/src/test/java/nars/main_nogui/TestReasoning0.java .


Build
-----
There are scripts for Linux and Windows to compile and create the executable jar:
build.sh and build.bat .

Test
----
The unit test suite is here. It ensures non-regression of the reasoner:
nars_core/src/test/java/nars/main_nogui/TestReasoning.java
It works classically: for each  XX-in.txt in directory nars-dist/Examples, it runs NARBatch, and compares actual result with reference result  XX-out.txt.
To create a new test input, add the NARS input as XX-in.txt in nars-dist/Examples , run the test suite, and move result file from temporary directory
/tmp/nars_test/XX-out.txt
into nars-dist/Example .
NOTE:
Due to the sensitivity of results regarding the implementation of the reasonner, it is difficult to write robust tests. But for pure non-regression tests, the test is usable.


Source Code status
------------------
See also http://code.google.com/p/open-nars/wiki/ProjectStatus
Current version has been fully tested for single capability at a time; there may still be bugs when combining capabilities.

Jean-Marc Vanel is working on this roadmap, mainly in GUI and software engineering tasks :
- reestablish a non-regression test suite
- make an independant syntax verifyer based on a grammar parser : it will give the column & line of error (there is a Scala combinator grammar)
- separe NARS in 2 modules with a Maven build : nars_gui and nars_core_java
