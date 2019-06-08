
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

# code generator : emit code
def genEmit(premiseA, premiseB, preconditions, conclusion, truthTuple, desire):

    def escape(str_):
        return str_.replace("\\", "\\\\")

    # converts a path to a
    def convertPathToDSrc(path):
        asStringList = []
        for iPathElement in path:
            if isinstance(iPathElement, str):
                asStringList.append('"'+iPathElement+'"')
            else:
                asStringList.append('"' + str(iPathElement) + '"')
        return "[" + ",".join(asStringList) + "]"



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
                return "new shared IntervalImpl(trieCtx.intervalPremiseT)"
            elif name == "t+z": # special case - is the time
                return "new shared IntervalImpl(trieCtx.intervalPremiseT+trieCtx.intervalPremiseZ)"
            elif name == "t-z": # special case - is the time
                return "new shared IntervalImpl(trieCtx.intervalPremiseT-trieCtx.intervalPremiseZ)"
            elif name == "tB-tA": # special case - we have to compute the difference of the timestamps
                return "new shared IntervalImpl(trieCtx.occurrencetimePremiseB-trieCtx.occurrencetimePremiseA)"

            resList = retPathOfName(name)

            if len(resList) == 1:
                if resList[0] == "a":
                    return "a"
                elif resList[0] == "b":
                    return "b"

                if resList[0][0] == 'a' or resList[0][0] == 'b':
                    code = "(" + "cast(Binary)"+resList[0][0] + ")" + resList[0][1:]

                return code
            elif len(resList) == 2:
                code = None
                if resList[0][0] == 'a' or resList[0][0] == 'b':
                    code = "(" + "cast(Binary)"+resList[0][0] + ")" + resList[0][1:]


                if resList[1] == 0:
                    code = "(cast(Binary)("+ code +"))" + ".subject"
                elif resList[1] == 1:
                    code = "(cast(Binary)("+ code +"))" + ".predicate"
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

                return "new shared Binary(\"" + nameCopula + "\"," + codeName0 + "," + codeName1 + ")"
            else:
                # special handling for compound

                (compoundType, name0, name1) = obj

                codeName0 = retCodeOfVar(name0)
                codeName1 = retCodeOfVar(name1)

                if compoundType == "*":
                    raise TODO() # generation for the code of products is not implemented!
                elif compoundType in ["|", "||", "&", "&&", "&/", "&|", "-", "~"]: # fall back to generation of binary
                    return "new shared Binary(\"" + compoundType + "\"," + codeName0 + "," + codeName1 + ")"
                else:
                    raise Exception(compoundType) # not implemented or internal error

                raise TODO()
                # TODO< code return handling >
        else:
            return retCodeOfVar(obj)




    printEffective = False # do we want to print the "effective"(how it is compiled) rule?
    if printEffective:
        # TODO< print desire >
        print "// effective    "+convTermToStr(premiseA)+", "+convTermToStr(premiseB)+"     "+str(preconditions)+"   |-   "+convTermToStr(conclusion)+" \t\t(Truth:"+truth+intervalProjection+")"

    global emitExecCode
    if not emitExecCode:
        return # if we don't emit the code and just the inference rules with comments

    conclusionSubjCode = retCode(conclusionSubj)
    conclusionPredCode = retCode(conclusionPred)


    # build trie


    # TODO< check embedded copula by walking >

    global staticFunctionCounter
    global derivationFunctionsSrc


    print "{"

    teCounter = 0

    if isinstance(premiseA, tuple):
        print "    shared TrieElement te0 = new shared TrieElement(TrieElement.EnumType.CHECKCOPULA);"
        print "    te0.side = EnumSide.LEFT;"
        print "    te0.checkedString = \""+escape(premiseACopula)+"\";"
        print "    "

        teCounter += 1

    if isinstance(premiseB, tuple):
        print "    shared TrieElement te"+str(teCounter)+" = new shared TrieElement(TrieElement.EnumType.CHECKCOPULA);"
        print "    te"+str(teCounter)+".side = EnumSide.RIGHT;"
        print "    te"+str(teCounter)+".checkedString = \""+escape(premiseBCopula)+"\";"

        print "    te"+str(teCounter-1)+".children ~= te"+str(teCounter)+";"
        print "    "

        teCounter+=1



    for iPrecondition in preconditions:
        if iPrecondition == "Time:After(tB,tA)" or iPrecondition == "Time:Parallel(tB,tA)":
            print "    shared TrieElement te"+str(teCounter)+" = new shared TrieElement(TrieElement.EnumType.PRECONDITION);"
            print "    te"+str(teCounter)+".stringPayload = \"" + iPrecondition + "\";"

            if teCounter > 0:
                print "    te"+str(teCounter-1)+".children ~= te"+str(teCounter)+";"
            print "    "

            teCounter+=1
        else:
            raise Exception("not implemented precondition: "+iPrecondition)

    if isinstance(premiseA, tuple) and not isPlaceholder(premiseA[0]):
        comparedCompoundType = premiseA[0][0]

        print "    shared TrieElement te"+str(teCounter)+" = new shared TrieElement(TrieElement.EnumType.WALKCHECKCOMPOUND);"


        print "    te"+str(teCounter)+".pathLeft = [\"a.subject\"];" # print python list to D list
        print "    te"+str(teCounter)+".pathRight = [];"
        print "    te"+str(teCounter)+".checkedString = \"" + comparedCompoundType + "\";"

        print "    te"+str(teCounter-1)+".children ~= te"+str(teCounter)+";"
        print "    "

        teCounter+=1

    if isinstance(premiseA, tuple) and not isPlaceholder(premiseA[2]):
        comparedCompoundType = premiseA[2][0]

        print "    shared TrieElement te"+str(teCounter)+" = new shared TrieElement(TrieElement.EnumType.WALKCHECKCOMPOUND);"


        print "    te"+str(teCounter)+".pathLeft = [\"a.predicate\"];" # print python list to D list
        print "    te"+str(teCounter)+".pathRight = [];"
        print "    te"+str(teCounter)+".checkedString = \"" + comparedCompoundType + "\";"

        print "    te"+str(teCounter-1)+".children ~= te"+str(teCounter)+";"
        print "    "

        teCounter+=1


    if isinstance(premiseB, tuple) and not isPlaceholder(premiseB[0]):
        comparedCompoundType = premiseB[0][0]

        print "    shared TrieElement te"+str(teCounter)+" = new shared TrieElement(TrieElement.EnumType.WALKCHECKCOMPOUND);"


        print "    te"+str(teCounter)+".pathLeft = [];" # print python list to D list
        print "    te"+str(teCounter)+".pathRight = [\"b.subject\"];"
        print "    te"+str(teCounter)+".checkedString = \"" + comparedCompoundType + "\";"

        print "    te"+str(teCounter-1)+".children ~= te"+str(teCounter)+";"
        print "    "

        teCounter+=1

    if isinstance(premiseB, tuple) and not isPlaceholder(premiseB[2]):
        comparedCompoundType = premiseB[2][0]

        print "    shared TrieElement te"+str(teCounter)+" = new shared TrieElement(TrieElement.EnumType.WALKCHECKCOMPOUND);"


        print "    te"+str(teCounter)+".pathLeft = [];" # print python list to D list
        print "    te"+str(teCounter)+".pathRight = [\"b.predicate\"];"
        print "    te"+str(teCounter)+".checkedString = \"" + comparedCompoundType + "\";"

        print "    te"+str(teCounter-1)+".children ~= te"+str(teCounter)+";"
        print "    "

        teCounter+=1



    for iSamePremiseTerms in samePremiseTerms: # need to iterate because there can be multiple terms which have to be the same
        print "    shared TrieElement te"+str(teCounter)+" = new shared TrieElement(TrieElement.EnumType.WALKCOMPARE);"
        print "    te"+str(teCounter)+".pathLeft = "+ convertPathToDSrc( iSamePremiseTerms[0] ) +";" # print python list to D list
        print "    te"+str(teCounter)+".pathRight = "+ convertPathToDSrc( iSamePremiseTerms[1] ) +";" # print python list to D list
        print "    te"+str(teCounter-1)+".children ~= te"+str(teCounter)+";"
        print "    "
        teCounter+=1

    hasIntervalT = "t" in pathsPremiseA or "t" in pathsPremiseB
    hasIntervalZ = "z" in pathsPremiseA or "z" in pathsPremiseB

    if hasIntervalT: # do we need to emit code for the computation of the interval(s)?
        print "    shared TrieElement te"+str(teCounter)+" = new shared TrieElement(TrieElement.EnumType.LOADINTERVAL);"
        print "    te"+str(teCounter)+".stringPayload = \"premiseT\";"
        print "    te"+str(teCounter)+".path = "+convertPathToDSrc(retPathOfName("t"))+";"
        print "    te"+str(teCounter-1)+".children ~= te"+str(teCounter)+";"
        print "    "
        teCounter+=1

    if hasIntervalZ: # do we need to emit code for the computation of the interval(s)?
        print "    shared TrieElement te"+str(teCounter)+" = new shared TrieElement(TrieElement.EnumType.LOADINTERVAL);"
        print "    te"+str(teCounter)+".stringPayload = \"premiseZ\";"
        print "    te"+str(teCounter)+".path = "+convertPathToDSrc(retPathOfName("z"))+";"
        print "    te"+str(teCounter-1)+".children ~= te"+str(teCounter)+";"
        print "    "
        teCounter+=1

    if intervalProjection != "": # do we need to emit code for the computation of the interval(s)?
        if intervalProjection == "IntervalProjection(t,z)":
            print "    shared TrieElement te"+str(teCounter)+" = new shared TrieElement(TrieElement.EnumType.INTERVALPROJECTION);"
            print "    te"+str(teCounter)+".stringPayload = \""+intervalProjection+"\";"
            print "    te"+str(teCounter-1)+".children ~= te"+str(teCounter)+";"
            print "    "
            teCounter+=1
        else:
            raise Exception("Unknown type of interval projection (not implemented)!")



    print "    shared TrieElement teX = new shared TrieElement(TrieElement.EnumType.EXEC);"
    print "    teX.fp = &derive"+str(staticFunctionCounter)+";"
    print "    te"+str(teCounter-1)+".children ~= teX;"
    print "    "
    print "    addToTrieRec(&rootTries, te0); //rootTries ~= te0;"
    print "}"
    print "\n"

    teCounter+=1



    derivationFunctionsSrc+= "static void derive"+str(staticFunctionCounter)+"(shared Sentence aSentence, shared Sentence bSentence, Sentences resultSentences, shared TrieElement trieElement, TrieContext *trieCtx) {\n"
    derivationFunctionsSrc+= "   assert(!(aSentence.isQuestion() && bSentence.isQuestion()), \"Invalid derivation : question-question\");\n"
    derivationFunctionsSrc+= "   \n"
    derivationFunctionsSrc+= "   \n"
    derivationFunctionsSrc+= "   bool hasConclusionTruth = !(aSentence.isQuestion() || bSentence.isQuestion());\n"
    derivationFunctionsSrc+= "   \n"
    derivationFunctionsSrc+= "   char derivationPunctation = aSentence.punctation;\n"
    derivationFunctionsSrc+= "   if (aSentence.isQuestion() || bSentence.isQuestion()) {\n"
    derivationFunctionsSrc+= "       derivationPunctation = '?';\n"
    derivationFunctionsSrc+= "   }\n"
    derivationFunctionsSrc+= "   \n"
    derivationFunctionsSrc+= "   auto a = aSentence.term;\n"
    derivationFunctionsSrc+= "   auto b = bSentence.term;\n"
    derivationFunctionsSrc+= "   \n"
    derivationFunctionsSrc+= "   auto conclusionSubj = "+conclusionSubjCode+";\n"
    derivationFunctionsSrc+= "   auto conclusionPred = "+conclusionPredCode+";\n"

    # TODO< do allow it the conclusion copula is not a real copula >
    derivationFunctionsSrc+= "   if(!isSameRec(conclusionSubj, conclusionPred)) { // conclusion with same subject and predicate are forbidden by NAL\n"

    derivationFunctionsSrc+= "      shared Binary conclusionTerm = new shared Binary(\""+escape(conclusionCopula)+"\", conclusionSubj, conclusionPred);\n"

    derivationFunctionsSrc+= "      auto stamp = Stamp.merge(aSentence.stamp, bSentence.stamp);\n"

    derivationFunctionsSrc+= "      auto tv = hasConclusionTruth ? TruthValue.calc(\""+truth+"\", aSentence.truth, bSentence.truth) : null;\n"


    if intervalProjection == "IntervalProjection(t,z)": # do we need to manipulate the tv for projection?
            derivationFunctionsSrc+= "      tv = new shared TruthValue(tv.freq, tv.conf * trieCtx.projectedTruthConfidence); // multiply confidence with confidence of projection\n"

    derivationFunctionsSrc+= "      if(hasConclusionTruth && tv.conf < 0.0001) {\n"
    derivationFunctionsSrc+= "          return; // conclusions with such a low conf are not relevant to the system\n"
    derivationFunctionsSrc+= "      }\n"

    derivationFunctionsSrc+= "      resultSentences.arr ~= new shared Sentence(derivationPunctation, conclusionTerm, tv, stamp);\n"
    derivationFunctionsSrc+= "   }\n"
    derivationFunctionsSrc+= "}\n"
    derivationFunctionsSrc+= "\n"
    derivationFunctionsSrc+= "\n"


    staticFunctionCounter+=1

# generate code for the rule
def gen(premiseA, premiseB, preconditions, conclusion, truthTuple, desire):
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

    # unpack truthTuple into truth and intervalProjection
    (truth, intervalProjection) = truthTuple

    # TODO< print desire >
    print "// rule         "+convTermToStr(premiseA)+", "+convTermToStr(premiseB)+"   " +str(preconditions)+  "  |-   " +  convTermToStr(conclusion) + "\t\t(Truth:"+truth+intervalProjection+")"


    genEmit(convTerm2(premiseA), convTerm2(premiseB), preconditions,  convTerm2(conclusion), truthTuple, desire)


# each copula-type of form [AsymCop,SymCop,[ConjunctiveCops,DisjunctiveCop,MinusCops]]
CopulaTypes = [
    ["-->","<->",[["&"],"|",["-","~"]]],
    ["==>","<=>",[["&&"],"||",None]], #
    [CWT("=/>","t"),CWT("</>","t"),[[CWT("&/","t"),"&|"],"||",None]], ##
    ["=|>","<|>",[["&/","&|"],"||",None]], #
    #[CWT("=\>","t"),None ,[["&/","&|"],"||",None]] ###
]

# generate code for already implemented conversions?
genCodeComplex = False

print "// AUTOGEN: initializes and fills tries"
print "shared(TrieElement)[] initTrie() {"
print "   shared(TrieElement)[] rootTries;"

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

    if True:
        #print "(A "+copAsym+" B),\t(B "+copAsymZ+" C)\t\t\t|-\t(A "+ival(copAsym,"t+z")+" C)\t\t(Truth:deduction"+OmitForHOL(", Desire:Strong")+")"
        gen(("A",copAsym,"B"), ("B",copAsymZ,"C"),   [] ,("A",ival(copAsym,"t+z"),"C"),    ("deduction", ""), OmitForHOL("strong"))

    copAsymHasTimeOffset = "/" in str(copAsym) or "\\" in str(copAsym)
    IntervalProjection = "IntervalProjection(t,z)" if copAsymHasTimeOffset else ""

    if True: # block
        gen(("A", copAsym, "B"),   ("C", copAsymZ, "B"),   [], ("A", ival(copAsym, "t-z"), "C"),   ("induction", IntervalProjection), OmitForHOL("weak"))

    if True:
        gen(("A", copAsym, "B"),   ("A", copAsymZ, "C"),   [], ("B", ival(copAsym, "t-z"), "C"),   ("abduction", IntervalProjection), OmitForHOL("strong"))

    if True: # added comparison
        gen(("A", copAsym, "B"),  ("C", copAsymZ, "B"),   [],("A",ival(copSym, "t-z"),"C"), ("comparison", IntervalProjection), OmitForHOL("weak"))
        gen(("A", copAsym, "B"),  ("A", copAsymZ, "C"),   [],("C",ival(copSym, "t-z"),"B"), ("comparison", IntervalProjection), OmitForHOL("weak"))


    if copSym != None:
        copSymZ = ival(copSym,"z")

        if True:
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

print "  return rootTries;"
print "}"
print ""
print ""

print derivationFunctionsSrc

