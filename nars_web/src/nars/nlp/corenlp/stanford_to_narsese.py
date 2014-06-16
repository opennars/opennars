subj="unspecified"
def setsubj(su):
    global subj
    subj=su
def getsubj():
    return subj
def parse(lines,bQuestion):
    global subj
    argument1=lambda expr: expr.split("(")[1].split("-")[0].replace(" ","")
    argument2=lambda expr: expr.split(",")[1].split("-")[0].replace(" ","")
    prep=lambda expr: expr.split("_")[1].split("(")[0]
    M=[x for x in lines.split("\n") if x!="" and not x.replace(" ","").startswith("(")]
    if "|" in lines:
        M=[x for x in lines.split("|") if x!="" and not x.replace(" ","").startswith("(")]
    
    def check_Negation(arg):
        for z in M: #negation
            if z.startswith("neg") and arg==argument1(z):
                return True
        return False
    
    def negate(neg,expr):
        if neg:
           return "(--,"+expr+")"
        return expr
     
    refs=["he","she","it","they","them","He","She","It","They","Them"]
    def arg(subj,x):
        if x in refs and subj!="unspecified":
            return subj
        return x
        
    #search first subject
    if subj=="unspecified":
        for x in M:
            if x.startswith("nsubj") and argument2(x) not in refs:
                subj=argument2(x)
    
    success=False
    sentences=[]
    for x in M:
        if x.startswith("amod"): #adjectives now better handled
            success=True
            neg=check_Negation(argument2(x))
            sentences+=[negate(neg,"<"+arg(subj,argument1(x))+" --> "+arg(subj,argument2(x)) + ">")]                    
    
    for x in M:
        if x.startswith("nsubj"):
            success=True
            subj=arg(subj,argument2(x))
            neg=check_Negation(argument1(x))
            #is property, is instance of
            for y in M: 
                if y.startswith("cop") and arg(subj,argument1(x))==arg(subj,argument1(y)): #and argument2(y) in ["is","are"]:
                    sentences+=[negate(neg,"<"+arg(subj,argument2(x))+" --> "+arg(subj,argument1(y))+">")]
                    for z in M:
                        if z.startswith("prep"):
                            if argument1(z)==argument1(x): 
                                sentences+=[negate(neg,"<(*,"+arg(subj,argument2(x))+","+prep(z)+","+arg(subj,argument2(z))+") --> "+arg(subj,argument1(y))+">")]
                    #for z in M:
                     #   if z.startswith("amod"): #handle amod as "property at"
                    #        if argument1(z)==argument1(x): 
                     #           sentences+=[negate(neg,"<(*,"+arg(subj,argument2(x))+",be,"+argument1(z)+") --> "+arg(subj,argument2(z))+">")]    
                     #       if argument1(y)==argument1(x): 
                      #          sentences+=[negate(neg,"<(*,"+arg(subj,argument1(y))+",be,"+argument2(x)+") --> "+arg(subj,argument2(z))+">")]           
            #search for prepositions
            for y in M:
                if y.startswith("prep"):
                    if argument1(x)==argument1(y):
                        sentences+=[negate(neg,"<"+arg(subj,argument2(x))+" --> "+arg(subj,argument1(y))+">")];
                        sentences+=[negate(neg,"<(*,"+arg(subj,argument2(x))+","+prep(y)+","+arg(subj,argument2(y))+") --> "+arg(subj,argument1(y))+">")];
                        for z in M:
                            if z.startswith("advmod") and argument1(z)==argument1(x):
                                sentences+=[negate(neg,"<(*,"+arg(subj,argument2(x))+","+arg(subj,argument2(z))+","+prep(y)+","+arg(subj,argument2(y))+") --> "+arg(subj,argument1(y))+">")];
                                sentences+=[negate(neg,"<(*,"+arg(subj,argument2(x))+","+arg(subj,argument1(y))+") --> "+arg(subj,argument2(z))+">")];
                                sentences+=[negate(neg,"<(*,"+arg(subj,argument2(x))+","+arg(subj,argument2(z))+") --> "+arg(subj,argument1(y))+">")];
            
            #simple sentence without a subject
            sentences+=[negate(neg,"<"+arg(subj,argument2(x))+" --> "+arg(subj,argument1(x))+">")]
            for y in M:
                if y.startswith("advmod") and argument1(x)==argument1(y):
                    sentences+=[negate(neg,"<(*,"+arg(subj,argument2(x))+","+arg(subj,argument2(y))+") --> "+arg(subj,argument1(x))+">")]
                    sentences+=[negate(neg,"<(*,"+arg(subj,argument2(x))+","+arg(subj,argument1(x))+") --> "+arg(subj,argument2(y))+">")]
                    
            
            #verb
            for y in M:
                if (y.startswith("dobj") or y.startswith("pobj")) and argument1(y)==argument1(x):# or y.startswith("iobj"):
                    if not neg: 
                        sentences+=["<"+arg(subj,argument2(x))+" --> "+arg(subj,argument1(y))+">"]
                    sentences+=[negate(neg,"<(*,"+arg(subj,argument2(x))+","+arg(subj,argument2(y))+") --> "+arg(subj,argument1(y))+">")]
                    #search for prepositons also  - example: thomas has eaten a lot >of< kebab
                    for z in M:
                        if z.startswith("prep"):
                            if argument1(z)==argument2(y):
                                sentences+=[negate(neg,"<(*,"+arg(subj,argument2(x))+","+arg(subj,argument2(y))+","+prep(z)+","+arg(subj,argument2(z))+") --> "+arg(subj,argument1(y))+">")]
                                for w in M:
                                    if w.startswith("advmod"):
                                        if argument1(w)==argument1(y):
                                            sentences+=[negate(neg,"<(*,"+arg(subj,argument2(x))+","+arg(subj,argument2(y))+","+arg(subj,argument2(w))+","+prep(z)+","+arg(subj,argument2(z))+") --> "+arg(subj,argument1(y))+">")]
                    #another search for preposition
                    for z in M:
                        if z.startswith("prep"):
                            if argument1(z)==argument1(x):
                                sentences+=[negate(neg,"<(*,"+arg(subj,argument2(x))+","+arg(subj,argument2(y))+","+prep(z)+","+arg(subj,argument2(z))+") --> "+arg(subj,argument1(y))+">")]
                    for z in M:
                        if z.startswith("iobj"):
                            if argument1(z)==argument1(x):
                                sentences+=[negate(neg,"<(*,"+arg(subj,argument2(x))+","+arg(subj,argument2(y))+","+arg(subj,argument2(z))+") --> "+arg(subj,argument1(y))+">")]
                    for z in M:
                        if z.startswith("amod"):
                            if argument1(z)==argument2(y):
                                sentences+=[negate(neg,"<(*,"+arg(subj,argument2(x))+","+arg(subj,argument2(y))+","+arg(subj,argument2(z))+") --> "+arg(subj,argument1(y))+">")]
                                sentences+=[negate(neg,"<(*,"+arg(subj,argument2(x))+","+arg(subj,argument2(y))+") --> "+arg(subj,argument2(z))+">")]
    if not success: #may be a 3 word sentence stanford parser is bad at because verb and noun is mixed
        for x in M:
            if x.startswith("prep"):
                sentences+=["<(*,"+argument1(x)+","+argument2(x)+") --> "+prep(x)+">"]
    sentences=list(set(sentences))
    
    if bQuestion: #ask the most precise possibility which the question asks for, the most appealing will be chosen by reasoning
        curi=0
        lens=0
        for i,x in enumerate(sentences):
            if len(x)>lens:
                curi=i
                lens=len(x)
        ret=""
        if len(sentences)>0:
            ret=sentences[curi]+"?"
    else:
        ret=""
        for x in sentences:
            ret+=x+".\n"
    if bQuestion:
        ret=ret.replace("wqho","?who").replace("wqhat","?what").replace("wqhere","?where").replace("wqhen","?when").replace("wqhy","?why").replace("somewhere","#1").replace("something","#2").replace("sometimes","#3").replace("someone","#4")
    return ret


