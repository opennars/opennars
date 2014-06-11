def parse(lines):
    argument1=lambda expr: expr.split("(")[1].split("-")[0].replace(" ","")
    argument2=lambda expr: expr.split(",")[1].split("-")[0].replace(" ","")
    prep=lambda expr: expr.split("_")[1].split("(")[0]
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
    
    refs=["he","she","it"]
    def arg(subj,x):
        if x in refs:
            return subj
        return x
        
    #search first subject
    subj="notresolved"
    for x in M:
        if x.startswith("nsubj") and argument2(x) not in refs:
            subj=argument2(x)
    
    sentences=[]
    for x in M:
        if x.startswith("nsubj"):
            subj=arg(subj,argument2(x))
            #is property, is instance of
            for y in M: 
                if y.startswith("cop") and arg(subj,argument1(x))==arg(subj,argument1(y)): #and argument2(y) in ["is","are"]:
                    neg=check_Negation(argument1(x))
                    sentences+=[negate(neg,"<"+arg(subj,argument2(x))+" --> "+arg(subj,argument1(y))+">")]
                    for z in M:
                        if z.startswith("prep"):
                            if argument1(z)==argument1(x): 
                                sentences+=[negate(neg,"<(*,"+arg(subj,argument2(x))+","+prep(z)+","+argument2(z)+") --> "+arg(subj,argument1(y))+">")]  
                    for z in M:
                        if z.startswith("amod"): #handle amod as "property at"
                            if argument1(z)==argument1(x): 
                                sentences+=[negate(neg,"<(*,"+arg(subj,argument2(x))+",be,"+argument1(z)+") --> "+arg(subj,argument2(z))+">")]    
                            if argument1(y)==argument1(x): 
                                sentences+=[negate(neg,"<(*,"+arg(subj,argument1(y))+",be,"+argument2(x)+") --> "+arg(subj,argument2(z))+">")]           
            #verb
            for y in M:
                if y.startswith("dobj"):
                    neg=check_Negation(argument1(x))
                    if not neg: 
                        sentences+=["<"+arg(subj,argument2(x))+" --> "+arg(subj,argument1(y))+">"]
                    sentences+=[negate(neg,"<(*,"+arg(subj,argument2(x))+","+arg(subj,argument2(y))+") --> "+arg(subj,argument1(y))+">")]
                    #search for preposition
                    for z in M:
                        if z.startswith("prep"):
                            if argument1(z)==argument1(x):
                                sentences+=[negate(neg,"<(*,"+arg(subj,argument2(x))+","+arg(subj,argument2(y))+","+prep(z)+","+argument2(z)+") --> "+arg(subj,argument1(y))+">")]

    return sentences
    #return [(s,narsese_to_sentence(s)) for s in sentences]
