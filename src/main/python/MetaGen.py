""" 
The MIT License

Copyright 2019 The OpenNARS authors.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
"""


# Non-Axiomatic Logic generation


# TODO< disable rules like  (where the timing of the conclusion is complicated)
#      (A =/>(t) B),   (C =/>(z) B)                    |-      ((&/(t) A C) =/>(t) B)  (Truth:UnionWithIntervalProjection(t,z))
#       because it is difficult to emit correct coe for it
# >

# TODO< swizzle subject and predicate  when the copula is symetric >
# TODO< implement sets >

# TODO< add special backward inference rules >

# LATER TODO< add rules for products to metaGen.py >
# LATER TODO< add image rules for sequences >
#             ask must be a sequence ProdStar because of images of sequences with a length more than two?





emitted = "" # used to store all of the emitted text

def emit(text):
    global emitted

    #print(text)
    emitted += (text + "\n")



# class to store time with copula
# "copula with time"
class CWT(object):
    def __init__(self, copula, tname):
        self.copula = copula
        self.tname = tname # time name
    def __str__(self):
        return self.copula+"("+self.tname+")"
    def retWithReplacedTName(self, tname): # return new CWT with replaced tname
        return CWT(self.copula, tname)


# helper
def isPlaceholder(string):
    return len(string) == 1 and string.istitle()

emitExecCode = True # do we emit executable code?

staticFunctionCounter = 0

# used to accumulate all static functions for the derivation
derivationFunctionsSrc = ""



# helper to convert a term to a string
def convTermToStr(term):
    if isinstance(term, tuple):
        (a, b, c) = term

        if isPlaceholder(a):
            # normal handling for statement

            (subj, copula, pred) = term # structure of statement

            return "<"+convTermToStr(subj)+str(copula)+convTermToStr(pred)+">"
        else:
            # special handling for compound

            (compoundType, name0, name1) = term

            return "("+str(compoundType)+convTermToStr(name0)+","+convTermToStr(name1)+")"
    else:
        return str(term)

def escape(str_):
    return str_.replace("\\", "\\\\")

# converts a path to a
def convertPathToJavaSrc(path):
    asStringList = []
    for iPathElement in path:
        if isinstance(iPathElement, str):
            asStringList.append('"'+iPathElement+'"')
        else:
            asStringList.append('"' + str(iPathElement) + '"')
    return "[" + ",".join(asStringList) + "]"

# code generator : emit code
def genTrieEmit(premiseA, premiseB, preconditions, conclusion, truthTuple, desire):
    # convert truth function name to the name of the enum
    def convTruthFnNameToEnum(truth):
        if truth == "induction":
            return "TruthFunctions.EnumType.INDUCTION"
        else:
            raise Exception("not implement truth function!")


    # unpack truthTuple into truth and intervalProjection
    (truth, intervalProjection) = truthTuple



    (conclusionSubj, conclusionCopula, conclusionPred) = conclusion

    isConclusionTemporal = \
        "|" in str(conclusionCopula) or \
        "/" in str(conclusionCopula) or \
        "\\" in str(conclusionCopula)


    # need to figure out which terms are the same on both sides
    #
    #
    samePremiseTerms = [] # contains tuple of the paths of the terms which have to be the same
                          # can be multiple

    pathsPremiseA = {}

    if isinstance(premiseA, tuple):
        (premiseASubj, premiseACopula, premiseAPred) = premiseA

        if not isinstance(premiseASubj, tuple):
            pathsPremiseA[premiseASubj] = ["a.subject"]
        else:
            pathsPremiseA[premiseASubj[1]] = ["a.subject", 0]
            pathsPremiseA[premiseASubj[2]] = ["a.subject", 1]

        if not isinstance(premiseAPred, tuple):
            pathsPremiseA[premiseAPred] = ["a.predicate"]
        else:
            pathsPremiseA[premiseAPred[1]] = ["a.predicate", 0]
            pathsPremiseA[premiseAPred[2]] = ["a.predicate", 1]
    else:
        pathsPremiseA[premiseA] = ["a"]


    if isinstance(premiseB, tuple):
        (premiseBSubj, premiseBCopula, premiseBPred) = premiseB

        if not isinstance(premiseBSubj, tuple):
            checkedName = premiseBSubj
            if checkedName in pathsPremiseA:
                samePremiseTerms.append( (pathsPremiseA[checkedName], ["b.subject"]) )
        else:
            checkedName = premiseBSubj[1]
            if checkedName in pathsPremiseA:
                samePremiseTerms.append( (pathsPremiseA[checkedName], ["b.subject", 0]) )

            checkedName = premiseBSubj[2]
            if checkedName in pathsPremiseA:
                samePremiseTerms.append( (pathsPremiseA[checkedName], ["b.subject", 1]) )

        if not isinstance(premiseBPred, tuple):
            checkedName = premiseBPred
            if checkedName in pathsPremiseA:
                samePremiseTerms.append( (pathsPremiseA[checkedName], ["b.predicate"]) )
        else:
            checkedName = premiseBPred[1]
            if checkedName in pathsPremiseA:
                samePremiseTerms.append( (pathsPremiseA[checkedName], ["b.predicate", 0]) )

            checkedName = premiseBPred[2]
            if checkedName in pathsPremiseA:
                samePremiseTerms.append( (pathsPremiseA[checkedName], ["b.predicate", 1]) )



    pathsPremiseB = {}

    if isinstance(premiseB, tuple):
        if not isinstance(premiseBSubj, tuple):
            pathsPremiseB[premiseBSubj] = ["b.subject"]
        else:
            if True: #isPlaceholder(premiseBSubj[0]):
                pathsPremiseB[premiseBSubj[1]] = ["b.subject", 0]
                pathsPremiseB[premiseBSubj[2]] = ["b.subject", 1]
            else:
                # special handling for compounds

                # NOT COMMENTED< because it will be useful for products and images and other compounds >

                pathsPremiseB[premiseBSubj[1]] = ["b.subject", "idx0"] # index indicates array access
                pathsPremiseB[premiseBSubj[2]] = ["b.subject", "idx1"] # index indicates array access

        if not isinstance(premiseBPred, tuple):
            pathsPremiseB[premiseBPred] = ["b.predicate"]
        else:
            if True: #isPlaceholder(premiseBPred[0]):
                pathsPremiseB[premiseBPred[1]] = ["b.predicate", 0]
                pathsPremiseB[premiseBPred[2]] = ["b.predicate", 1]
            else:
                # special handling for compounds

                # NOT COMMENTED< because it will be useful for products and images and other compounds >

                pathsPremiseB[premiseBPred[1]] = ["b.predicate", "idx0"] # index indicates array access
                pathsPremiseB[premiseBPred[2]] = ["b.predicate", "idx1"] # index indicates array access
    else:
        pathsPremiseB[premiseB] = ["b"]


    def retPathOfName(name):
        if name in pathsPremiseA:
            return pathsPremiseA[name]
        elif name in pathsPremiseB:
            return pathsPremiseB[name]
        else:
            raise Exception("couldn't find name " + name)


    def retCode(obj):

        def retCodeOfVar(name):
            if name == "t": # special case - is the time
                return "new Interval(trieCtx.intervalPremiseT)"
            elif name == "t+z": # special case - is the time
                return "new Interval(trieCtx.intervalPremiseT+trieCtx.intervalPremiseZ)"
            elif name == "t-z": # special case - is the time
                return "new Interval(trieCtx.intervalPremiseT-trieCtx.intervalPremiseZ)"
            elif name == "tB-tA": # special case - we have to compute the difference of the timestamps
                return "new Interval(trieCtx.occurrencetimePremiseB-trieCtx.occurrencetimePremiseA)"

            resList = retPathOfName(name)

            if len(resList) == 1:
                if resList[0] == "a":
                    return "a"
                elif resList[0] == "b":
                    return "b"

                if resList[0][0] == 'a' or resList[0][0] == 'b':
                    code = "(" + "(Binary)"+resList[0][0] + ")" + resList[0][1:]

                return code
            elif len(resList) == 2:
                code = None
                if resList[0][0] == 'a' or resList[0][0] == 'b':
                    code = "(" + "(Binary)"+resList[0][0] + ")" + resList[0][1:]


                if resList[1] == 0:
                    code = "((Binary)("+ code +"))" + ".subject"
                elif resList[1] == 1:
                    code = "((Binary)("+ code +"))" + ".predicate"
                #if resList[1] == "idx0": # special handling for compound access
                #    code += ".TODO[0]"
                #elif resList[1] == "idx1":
                #    code += ".TODO[1]" # special handling for compound access
                else:
                    raise Exception("not implemented!")

                return code
            else:
                raise Exception("unexpected length!")


        if isinstance(obj, tuple):
            (a, b, c) = obj

            if isPlaceholder(a):
                # normal handling for statement

                (name0, nameCopula, name1) = obj # structure of conclusion term is encoded as tuple

                codeName0 = retCodeOfVar(name0)
                codeName1 = retCodeOfVar(name1)

                return "DeriverHelpers.makeBinary(\"" + nameCopula + "\"," + codeName0 + "," + codeName1 + ")"
            else:
                # special handling for compound

                (compoundType, name0, name1) = obj

                codeName0 = retCodeOfVar(name0)
                codeName1 = retCodeOfVar(name1)

                if compoundType == "*":
                    raise TODO() # generation for the code of products is not implemented!
                elif compoundType in ["|", "||", "&", "&&", "&/", "&|", "-", "~"]: # fall back to generation of binary
                    return "DeriverHelpers.makeBinary(\"" + compoundType + "\"," + codeName0 + "," + codeName1 + ")"
                else:
                    raise Exception(compoundType) # not implemented or internal error

                raise TODO()
                # TODO< code return handling >
        else:
            return retCodeOfVar(obj)




    printEffective = False # do we want to print the "effective"(how it is compiled) rule?
    if printEffective:
        # TODO< print desire >
        emit("// effective    "+convTermToStr(premiseA)+", "+convTermToStr(premiseB)+"     "+str(preconditions)+"   |-   "+convTermToStr(conclusion)+" \t\t(Truth:"+truth+intervalProjection+")")

    global emitExecCode
    if not emitExecCode:
        return # if we don't emit the code and just the inference rules with comments

    conclusionSubjCode = retCode(conclusionSubj)
    conclusionPredCode = retCode(conclusionPred)


    # build trie


    # TODO< check embedded copula by walking >

    global staticFunctionCounter
    global derivationFunctionsSrc


    emit("{")

    teCounter = 0

    if isinstance(premiseA, tuple):
        emit("    Trie.TrieElement te0 = new Trie.TrieElement(Trie.TrieElement.EnumType.CHECKCOPULA);")
        emit("    te0.side = EnumSide.LEFT;")
        emit("    te0.checkedString = \""+escape(premiseACopula)+"\";")
        emit("    ")

        teCounter += 1

    if isinstance(premiseB, tuple):
        emit("    Trie.TrieElement te"+str(teCounter)+" = new Trie.TrieElement(Trie.TrieElement.EnumType.CHECKCOPULA);")
        emit("    te"+str(teCounter)+".side = EnumSide.RIGHT;")
        emit("    te"+str(teCounter)+".checkedString = \""+escape(premiseBCopula)+"\";")

        emit("    te"+str(teCounter-1)+".children.add( te"+str(teCounter)+");")
        emit("    ")

        teCounter+=1



    for iPrecondition in preconditions:
        if iPrecondition == "Time:After(tB,tA)" or iPrecondition == "Time:Parallel(tB,tA)":
            emit("    Trie.TrieElement te"+str(teCounter)+" = new Trie.TrieElement(Trie.TrieElement.EnumType.PRECONDITION);")
            emit("    te"+str(teCounter)+".stringPayload = \"" + iPrecondition + "\";")

            if teCounter > 0:
                emit("    te"+str(teCounter-1)+".children.add( te"+str(teCounter)+");")
            emit("    ")

            teCounter+=1
        else:
            raise Exception("not implemented precondition: "+iPrecondition)

    if isinstance(premiseA, tuple) and not isPlaceholder(premiseA[0]):
        comparedCompoundType = premiseA[0][0]

        emit("    Trie.TrieElement te"+str(teCounter)+" = new Trie.TrieElement(Trie.TrieElement.EnumType.WALKCHECKCOMPOUND);")


        emit("    te"+str(teCounter)+".pathLeft = [\"a.subject\"];") # print python list to D list
        emit("    te"+str(teCounter)+".pathRight = [];")
        emit("    te"+str(teCounter)+".checkedString = \"" + comparedCompoundType + "\";")

        emit("    te"+str(teCounter-1)+".children.add( te"+str(teCounter)+");")
        emit("    ")

        teCounter+=1

    if isinstance(premiseA, tuple) and not isPlaceholder(premiseA[2]):
        comparedCompoundType = premiseA[2][0]

        emit("    Trie.TrieElement te"+str(teCounter)+" = new Trie.TrieElement(Trie.TrieElement.EnumType.WALKCHECKCOMPOUND);")


        emit("    te"+str(teCounter)+".pathLeft = [\"a.predicate\"];") # print python list to D list
        emit("    te"+str(teCounter)+".pathRight = [];")
        emit("    te"+str(teCounter)+".checkedString = \"" + comparedCompoundType + "\";")

        emit("    te"+str(teCounter-1)+".children.add( te"+str(teCounter)+");")
        emit("    ")

        teCounter+=1


    if isinstance(premiseB, tuple) and not isPlaceholder(premiseB[0]):
        comparedCompoundType = premiseB[0][0]

        emit("    Trie.TrieElement te"+str(teCounter)+" = new Trie.TrieElement(Trie.TrieElement.EnumType.WALKCHECKCOMPOUND);")


        emit("    te"+str(teCounter)+".pathLeft = [];") # print python list to D list
        emit("    te"+str(teCounter)+".pathRight = [\"b.subject\"];")
        emit("    te"+str(teCounter)+".checkedString = \"" + comparedCompoundType + "\";")

        emit("    te"+str(teCounter-1)+".children.add( te"+str(teCounter)+");")
        emit("    ")

        teCounter+=1

    if isinstance(premiseB, tuple) and not isPlaceholder(premiseB[2]):
        comparedCompoundType = premiseB[2][0]

        emit("    Trie.TrieElement te"+str(teCounter)+" = new Trie.TrieElement(Trie.TrieElement.EnumType.WALKCHECKCOMPOUND);")


        emit("    te"+str(teCounter)+".pathLeft = [];") # print python list to D list
        emit("    te"+str(teCounter)+".pathRight = [\"b.predicate\"];")
        emit("    te"+str(teCounter)+".checkedString = \"" + comparedCompoundType + "\";")

        emit("    te"+str(teCounter-1)+".children.add( te"+str(teCounter)+");")
        emit("    ")

        teCounter+=1



    for iSamePremiseTerms in samePremiseTerms: # need to iterate because there can be multiple terms which have to be the same
        emit("    Trie.TrieElement te"+str(teCounter)+" = new Trie.TrieElement(Trie.TrieElement.EnumType.WALKCOMPARE);")
        emit("    te"+str(teCounter)+".pathLeft = "+ convertPathToJavaSrc( iSamePremiseTerms[0] ) +";") # print python list to D list
        emit("    te"+str(teCounter)+".pathRight = "+ convertPathToJavaSrc( iSamePremiseTerms[1] ) +";") # print python list to D list
        emit("    te"+str(teCounter-1)+".children.add( te"+str(teCounter)+");")
        emit("    ")
        teCounter+=1

    hasIntervalT = "t" in pathsPremiseA or "t" in pathsPremiseB
    hasIntervalZ = "z" in pathsPremiseA or "z" in pathsPremiseB

    if hasIntervalT: # do we need to emit code for the computation of the interval(s)?
        emit("    Trie.TrieElement te"+str(teCounter)+" = new Trie.TrieElement(Trie.TrieElement.EnumType.LOADINTERVAL);")
        emit("    te"+str(teCounter)+".stringPayload = \"premiseT\";")
        emit("    te"+str(teCounter)+".path = "+convertPathToJavaSrc(retPathOfName("t"))+";")
        emit("    te"+str(teCounter-1)+".children.add( te"+str(teCounter)+");")
        emit("    ")
        teCounter+=1

    if hasIntervalZ: # do we need to emit code for the computation of the interval(s)?
        emit("    Trie.TrieElement te"+str(teCounter)+" = new Trie.TrieElement(Trie.TrieElement.EnumType.LOADINTERVAL);")
        emit("    te"+str(teCounter)+".stringPayload = \"premiseZ\";")
        emit("    te"+str(teCounter)+".path = "+convertPathToJavaSrc(retPathOfName("z"))+";")
        emit("    te"+str(teCounter-1)+".children.add( te"+str(teCounter)+");")
        emit("    ")
        teCounter+=1

    if intervalProjection != "": # do we need to emit code for the computation of the interval(s)?
        if intervalProjection == "IntervalProjection(t,z)":
            emit("    Trie.TrieElement te"+str(teCounter)+" = new Trie.TrieElement(Trie.TrieElement.EnumType.INTERVALPROJECTION);")
            emit("    te"+str(teCounter)+".stringPayload = \""+intervalProjection+"\";")
            emit("    te"+str(teCounter-1)+".children.add( te"+str(teCounter)+");")
            emit("    ")
            teCounter+=1
        else:
            raise Exception("Unknown type of interval projection (not implemented)!")



    emit("    Trie.TrieElement teX = new Trie.TrieElement(Trie.TrieElement.EnumType.EXEC);")
    emit("    teX.fp = new derive"+str(staticFunctionCounter)+"();")
    emit("    te"+str(teCounter-1)+".children.add(teX);")
    emit("    ")
    emit("    Trie.addToTrieRec(rootTries, te0);")
    emit("}")
    emit("\n")

    teCounter+=1


    derivationFunctionsSrc+= "public static class derive"+str(staticFunctionCounter)+" implements Trie.TrieElement.DerivableAction {\n"
    derivationFunctionsSrc+= "public void derive(Sentence aSentence, Sentence bSentence, List<Sentence> resultSentences, Trie.TrieElement trieElement, long time, Trie.TrieContext trieCtx, Parameters narParameters) {\n"
    derivationFunctionsSrc+= "   assert !(aSentence.isQuestion() && bSentence.isQuestion()) : \"Invalid derivation : question-question\";\n"
    derivationFunctionsSrc+= "   \n"
    derivationFunctionsSrc+= "   \n"
    derivationFunctionsSrc+= "   boolean hasConclusionTruth = !(aSentence.isQuestion() || bSentence.isQuestion());\n"
    derivationFunctionsSrc+= "   \n"
    derivationFunctionsSrc+= "   char derivationPunctuation = aSentence.punctuation;\n"
    derivationFunctionsSrc+= "   if (aSentence.isQuestion() || bSentence.isQuestion()) {\n"
    derivationFunctionsSrc+= "       derivationPunctuation = '?';\n"
    derivationFunctionsSrc+= "   }\n"
    derivationFunctionsSrc+= "   \n"
    derivationFunctionsSrc+= "   Term a = aSentence.term;\n"
    derivationFunctionsSrc+= "   Term b = bSentence.term;\n"
    derivationFunctionsSrc+= "   \n"
    derivationFunctionsSrc+= "   Term conclusionSubj = "+conclusionSubjCode+";\n"
    derivationFunctionsSrc+= "   Term conclusionPred = "+conclusionPredCode+";\n"

    # TODO< do allow it the conclusion copula is not a real copula >
    derivationFunctionsSrc+= "   if(!isSame(conclusionSubj, conclusionPred)) { // conclusion with same subject and predicate are forbidden by NAL\n"

    derivationFunctionsSrc+= "      Term conclusionTerm = DeriverHelpers.makeBinary(\""+escape(conclusionCopula)+"\", conclusionSubj, conclusionPred);\n"

    derivationFunctionsSrc+= "      Stamp stamp = new Stamp(aSentence.stamp, bSentence.stamp, time, narParameters); // merge stamps\n"

    derivationFunctionsSrc+= "      TruthValue tv = hasConclusionTruth ? TruthFunctions.lookupTruthFunctionAndCompute("+convTruthFnNameToEnum(truth)+", aSentence.truth, bSentence.truth, narParameters) : null;\n"


    if intervalProjection == "IntervalProjection(t,z)": # do we need to manipulate the tv for projection?
            derivationFunctionsSrc+= "      tv = new TruthValue(tv.freq, tv.getConfidence() * trieCtx.projectedTruthConfidence); // multiply confidence with confidence of projection\n"

    derivationFunctionsSrc+= "      if(hasConclusionTruth && tv.getConfidence() < 0.0001) {\n"
    derivationFunctionsSrc+= "          return; // conclusions with such a low conf are not relevant to the system\n"
    derivationFunctionsSrc+= "      }\n"

    derivationFunctionsSrc+= "      resultSentences.add(new Sentence(conclusionTerm, derivationPunctuation, tv, stamp));\n"
    derivationFunctionsSrc+= "   }\n"
    derivationFunctionsSrc+= "} // method\n"
    derivationFunctionsSrc+= "} // class\n"
    derivationFunctionsSrc+= "\n"
    derivationFunctionsSrc+= "\n"


    staticFunctionCounter+=1


# helper to convert a premise from the temporal form to something which we can generate the code for
# ex: "A =/>(t) B" to "(&/, A, t) =/> B"
def convTerm2(term):
    if isinstance(term, tuple):
        if len(term) == 3:

            (a, b, c) = term

            if isPlaceholder(a): # normal handling for statement

                (name0, copula, name1) = term # structure of conclusion term is encoded as tuple

                if isinstance(copula, CWT):
                    # we have to rebuild the statement

                    return (("&/", name0, copula.tname), copula.copula, name1)
                else:
                    return term # no special handling necessary because it is not a CWT

            else: # special handling for compound
                return term # because we only care about statements

        else:
            raise Exception("unhandled case") # we expect a tuple of length 3
    else:
        return term # no special treatment necessary

# generate trie code for the rule
def genTrie(premiseA, premiseB, preconditions, conclusion, truthTuple, desire):
    

    # unpack truthTuple into truth and intervalProjection
    (truth, intervalProjection) = truthTuple

    # TODO< print desire >
    emit("// rule         "+convTermToStr(premiseA)+", "+convTermToStr(premiseB)+"   " +str(preconditions)+  "  |-   " +  convTermToStr(conclusion) + "\t\t(Truth:"+truth+intervalProjection+")")


    genTrieEmit(convTerm2(premiseA), convTerm2(premiseB), preconditions,  convTerm2(conclusion), truthTuple, desire)



# each copula-type of form [AsymCop,SymCop,[ConjunctiveCops,DisjunctiveCop,MinusCops]]
CopulaTypes = [
    ##["-->","<->",[["&"],"|",["-","~"]]],
    ##["==>","<=>",[["&&"],"||",None]], #
    [CWT("=/>","t"),CWT("</>","t"),[[CWT("&/","t"),"&|"],"||",None]], ##
    ##["=|>","<|>",[["&/","&|"],"||",None]], #
    #[CWT("=\>","t"),None ,[["&/","&|"],"||",None]] ###
]

# generate code for already implemented conversions?
genCodeComplex = False

gen = genTrie # we want to generate trie rules

emit("/*")
emit(" * The MIT License")
emit(" *")
emit(" * Copyright 2019 The OpenNARS authors.")
emit(" *")
emit(" * Permission is hereby granted, free of charge, to any person obtaining a copy")
emit(" * of this software and associated documentation files (the \"Software\"), to deal")
emit(" * in the Software without restriction, including without limitation the rights")
emit(" * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell")
emit(" * copies of the Software, and to permit persons to whom the Software is")
emit(" * furnished to do so, subject to the following conditions:")
emit(" *")
emit(" * The above copyright notice and this permission notice shall be included in")
emit(" * all copies or substantial portions of the Software.")
emit(" *")
emit(" * THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR")
emit(" * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,")
emit(" * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE")
emit(" * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER")
emit(" * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,")
emit(" * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN")
emit(" * THE SOFTWARE.")
emit(" */")

emit("package org.opennars.inference;")

emit("import org.opennars.entity.Sentence;")
emit("import org.opennars.entity.Stamp;")
emit("import org.opennars.entity.TruthValue;")
emit("import org.opennars.language.Interval;")
emit("import org.opennars.language.Term;")
emit("import org.opennars.main.Parameters;")

emit("")
emit("import java.util.List;")
emit("import java.util.ArrayList;")


emit("public class InitTrie {")


emit("// AUTOGEN: initializes and fills tries")
emit("public static List<Trie.TrieElement> initTrie() {")
emit("   List<Trie.TrieElement> rootTries = new ArrayList<>();")

for [copAsym,copSym,[ConjCops,DisjCop,MinusCops]] in CopulaTypes:
    isTemporal = \
        "|" in str(copAsym) or \
        "/" in str(copAsym) or \
        "\\" in str(copAsym)

    bFOL = copAsym == "-->"
    OmitForHOL = lambda str: str if bFOL else ""

    # replace the potentially existing interval with a different name
    def ival(obj,tname):
        if isinstance(obj, CWT):
            return obj.retWithReplacedTName(tname)
        return obj.replace("t",tname)

    copAsymZ = ival(copAsym, "z")




    # TODO< implement inference generation function to generate code which accepts only one argument >
    #print "(A "+copAsym+" B)\t\t\t\t\t|-\t(B "+ival(copAsym,"-t")+" A)\t\t(Truth:Conversion)"

    if False:
        #print "(A "+copAsym+" B),\t(B "+copAsymZ+" C)\t\t\t|-\t(A "+ival(copAsym,"t+z")+" C)\t\t(Truth:deduction"+OmitForHOL(", Desire:Strong")+")"
        gen(("A",copAsym,"B"), ("B",copAsymZ,"C"),   [] ,("A",ival(copAsym,"t+z"),"C"),    ("deduction", ""), OmitForHOL("strong"))

    copAsymHasTimeOffset = "/" in str(copAsym) or "\\" in str(copAsym)
    IntervalProjection = "IntervalProjection(t,z)" if copAsymHasTimeOffset else ""

    if False: # block
        gen(("A", copAsym, "B"),   ("C", copAsymZ, "B"),   [], ("A", ival(copAsym, "t-z"), "C"),   ("induction", IntervalProjection), OmitForHOL("weak"))

    if False:
        gen(("A", copAsym, "B"),   ("A", copAsymZ, "C"),   [], ("B", ival(copAsym, "t-z"), "C"),   ("abduction", IntervalProjection), OmitForHOL("strong"))

    if False: # added comparison
        gen(("A", copAsym, "B"),  ("C", copAsymZ, "B"),   [],("A",ival(copSym, "t-z"),"C"), ("comparison", IntervalProjection), OmitForHOL("weak"))
        gen(("A", copAsym, "B"),  ("A", copAsymZ, "C"),   [],("C",ival(copSym, "t-z"),"B"), ("comparison", IntervalProjection), OmitForHOL("weak"))


    if copSym != None:
        copSymZ = ival(copSym,"z")

        if False:
            #print "(A "+copSym+" B),\t(B "+copSymZ+" C)\t\t\t|-\t(A "+ival(copSym,"t+z")+" C)\t\t(Truth:resemblance"+OmitForHOL(", Desire:Strong")+")"
            gen(("A",copSym,"B"),("B",copSymZ,"C"),  [], ("A",ival(copSym,"t+z"),"C"),  ("resemblance", ""), OmitForHOL("strong"))

            #print "(A "+copAsym+" B),\t(C "+copSymZ+" B)\t\t\t|-\t(A "+copAsym+" C)\t\t(Truth:analogy"+IntervalProjection+OmitForHOL(", Desire:Strong")+")"
            gen(("A",copAsym,"B"),("C",copSymZ,"B"),   [], ("A",copAsym,"C"),   ("analogy", IntervalProjection), OmitForHOL("strong"))

            #print "(A "+copAsym+" B),\t(C "+copSymZ+" A)\t\t\t|-\t(C "+ival(copSym,"t+z")+" B)\t\t(Truth:analogy"+OmitForHOL(", Desire:Strong")+")"
            gen(("A",copAsym,"B"),("C",copSymZ,"A"),   [], ("C",ival(copSym,"t+z"),"B"),  ("analogy", ""), OmitForHOL("strong"))

            #print "(A "+copAsym+" B),\t(C "+copSymZ+" B)\t\t\t|-\t(A "+copSym+" C)\t\t(Truth:comparison"+IntervalProjection+OmitForHOL(", Desire:Weak")+")"
            gen(("A", copAsym, "B"),  ("C", copSymZ, "B"),   [],("A",copSym,"C"), ("comparison", IntervalProjection), OmitForHOL("weak"))

            #print "(A "+copAsym+" B),\t(A "+copSymZ+" C)\t\t\t|-\t(C "+copSym+" B)\t\t(Truth:comparison"+IntervalProjection+OmitForHOL(", Desire:Weak")+")"
            gen(("A", copAsym, "B"),  ("A",copSymZ,"C"),    [],("C",copSym,"B"), ("comparison", IntervalProjection), OmitForHOL("weak"))



    if isTemporal:
        isBackward = copSym == None
        for ConjCop in ConjCops:
            predRel = ["Time:After(tB,tA)"] if copAsymHasTimeOffset else (["Time:Parallel(tB,tA)"] if "|" in str(copAsym) else [])
            predConj = ["Time:After(tB,tA)"] if "/" in str(ConjCop) or "\\" in str(ConjCop) else (["Time:Parallel(tB,tA)"] if "|" in str(copAsym) else [])
            forwardRel = "tB-tA" if "Time:After" in str(predRel) else ""
            forwardConj = "tB-tA" if "Time:After" in str(predConj) else ""

            if not isBackward:
                pass

                #print "A, \t\tB\t"+predRel+"\t|-\t(A "+copAsym.replace("t",forwardRel)+ "B)\t\t(Truth:Induction, Variables:Introduce$#)"
                #print "A, \t\tB\t"+predRel+"\t|-\t(A "+copAsym.replace("t",forwardRel)+ "B)\t\t(Truth:Induction, Variables:Introduce$#)"
                gen("A", "B",  predRel,("A", ival(copAsym, forwardRel), "B"),  ("induction", ""), "")

                #print "A,\t\tB\t"+predConj+"\t|-\t("+ConjCop.replace("t",forwardConj)+" A B)\t\t(Truth:Intersection, Variables:Introduce#)"

                #print "A\t\tB\t"+predRel+"\t|-\t(B "+copSym.replace("t",forwardRel)+"A)\t\t(Truth:Comparison, Variables:Introduce$#)"

            else:
                pass
                #print "A, \t\tB\t"+predRel+"\t|-\t(B "+copAsym+"(tA-tB) A)\t(Truth:Induction, Variables:Introduce$#)"

            #print "("+ConjCop+" A B)\t\t\t\t\t|-\tA\t\t\t(Truth:Deduction, Desire:Induction)"

        (tParam, tParam2) = (", Time:-t" if isBackward else ", Time:+t", ", Time:+t" if isBackward else ", Time:-t")
        #print "A,\t\t(A "+copAsym+" B)\t\t\t|-\tB\t\t\t(Truth:Deduction, Desire:Induction, Variables:Unify$#"+(tParam if copAsymHasTimeOffset else "")+")"
        #print "B,\t\t(A "+copAsym+" B)\t\t\t|-\tA\t\t\t(Truth:Abduction, Desire:Deduction, Variables:Unify$#"+(tParam2 if copAsymHasTimeOffset else "")+")"
        #if copSym != None:
        #    print "B,\t\t(A "+copSym+" B)\t\t\t|-\tA\t\t\t(Truth:Analogy, Desire:Strong, Variables:Unify$#)"

    for cop in [copAsym,copSym]:
        if cop == None:
            continue

        copZ = ival(cop,"z")
        if MinusCops != None:
            if True:
                gen(("A",cop,"B"),("C",copZ,"B"),   [], ((MinusCops[1],"A","C"),cop,"B"),    ("difference", ""), "")
                gen(("A",cop,"B"),("A",copZ,"C"),   [], ("B",cop,(MinusCops[0],"A","C")),    ("difference", ""), "")
                gen(("S",cop,"M"),((MinusCops[1],"S","P"),copZ,"B"),   [],("P",cop,"M"),   ("decomposePNP", ""), "")
                gen(("S",cop,"M"),((MinusCops[1],"P","S"),copZ,"B"),   [],("P",cop,"M"),   ("decomposeNNN", ""), "")
                gen(("M",cop,"S"),("M",copZ,(MinusCops[0],"S","P")),   [],("M",cop,"P"),   ("decomposePNP", ""), "")
                gen(("M",cop,"S"),("M",copZ,(MinusCops[0],"P","S")),    [],("M",cop,"P"),    ("decomposeNNN", ""), "")



    for cop in [copAsym,copSym]:
        if cop == None:
            continue

        copZ = ival(cop,"z")

        for ConjCop in ConjCops:
            for [junc,[TruthSet1,TruthSet2],[TruthDecomp1,TruthDecomp2]] in [[ConjCop,["union","intersection"],["decomposeNPP","decomposePNN"]],
                                                                             [DisjCop,["intersection","union"],["decomposePNN","decomposeNPP"]]]:
                if junc == None:
                    continue

                if junc == ConjCop:
                    pass
                    # commented because it only consumes a single premise on the left side! - we haven't implemented this case
                    #print "A,\t\t((" + junc + " A C) "+copZ+" B)\t\t|-\t(C "+ copZ + " B)\t\t(Truth:Deduction"+(tParam.replace("-","+") if copAsymHasTimeOffset else "")+")"

                if not isinstance(cop, CWT): # is disabled for temporal inference until we can traverse & build these complicated binary terms


                    for enableSetForm in [False]: #([True, False] if cop == "-->" else [False]): # TODO< add False >
                        def setFormSubj(): # returns the form of the set of the conclusions
                            if enableSetForm:
                                return "R"
                            return (junc,"A", "C")

                        def setFormPred():
                            if enableSetForm:
                                return "R"
                            return (junc,"B", "C")

                        if True:
                            #print "(A "+cop+" B),\t(C "+copZ+" B)\t\t\t|-\t((" + junc + " A C) "+ cop + " B) \t" + TruthSet1 + IntervalProjection+")"
                            gen(("A",cop,"B"),("C",copZ,"B"),   (["extset?(A)",TruthSet2+"(A,B,R)"] if enableSetForm else []),(setFormSubj(), cop, "B"),  (TruthSet1, IntervalProjection), "")

                            #print "(A "+cop+" B),\t(A "+copZ+" C)\t\t\t|-\t(A "+ cop + " (" + junc + " B C)) \t"  + TruthSet2 + IntervalProjection+")"
                            gen(("A",cop,"B"),("A",copZ,"C"),   (["intset?(B)",TruthSet1+"(B,C,R)"] if enableSetForm else []),("A",cop,setFormPred()),  (TruthSet2, IntervalProjection), "")

                    if True:
                        gen(("S",cop,"M"),((junc,"S", "L"),copZ,"M"),    [],("L",cop,"M"),   (TruthDecomp1, IntervalProjection), "")

                    if True:
                        gen(("M",cop,"S"),("M",copZ,(junc,"S","L")),     [],("M",cop,"L"),   (TruthDecomp2, IntervalProjection), "")

emit("  return rootTries;")
emit("}")

emit("")
emit("")

emit(derivationFunctionsSrc)


emit("// helper")
emit("static boolean isSame(Term a, Term b) {")
emit("   return a.equals(b);")
emit("}")


emit("} // class")



import sys, os

pathname = os.path.dirname(sys.argv[0])
currentdir = os.path.abspath(pathname)
del pathname

currentdir = os.path.join(currentdir,"..\\java\\org\\opennars\\inference\\InitTrie.java")
print("emit code into file "+str(currentdir))

f = open(currentdir, "w")
f.write(emitted)
f.close()
