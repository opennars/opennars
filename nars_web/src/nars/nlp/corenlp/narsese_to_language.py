simple=lambda expr: not "*" in expr and not "-->" in expr and not "#" in expr
normalize=lambda expr: expr.replace("<","").replace(","," ").replace(">","").replace("(","").replace(")","").replace("  "," ")
def narsese_to_sentence(expr):
    nope = "--," in expr
    cut=lambda exp: exp.replace("  "," ") if not nope else "not "+exp.replace("  "," ")
    left=expr.split("-->")[0]
    right=expr.split("-->")[1]
    if nope:
        left=expr.split("--,")[1].split("-->")[0]
    if simple(left) and simple(right):
        return cut("a "+normalize(right)+" thing "+normalize(left)+" seams to be")
    if simple(right) and left.count(",")==2:
        if not normalize(right).endswith("ed") and not normalize(right).endswith("ing"):
            return cut(normalize(left).split(" ")[1]+" seams to "+normalize(right)+" "+normalize(left).split(" ")[2])
        else:
            if ",being," not in normalize(left):
                return cut(normalize(left).split(" ")[1]+" seams to be "+normalize(right)+" "+normalize(left).split(" ")[2])
            else:
                return cut(normalize(left).split(" ")[1]+" seams to be "+normalize(right)+" "+normalize(left).split(" ")[2])
    if simple(right) and left.count(",")==3:
        if ",be," not in left:
            return cut(normalize(right)+" "+normalize(left).split(" ")[1]+" seams to be "+normalize(left).split(" ")[2]+" "+normalize(left).split(" ")[3])
        else:
            return cut(normalize(left).split(" ")[1]+" seams to be a"+cut(normalize(right)+" "+normalize(left).split(" ")[3]))
    if simple(right) and left.count(",")==4:
        return cut(normalize(left).split(" ")[1]+" seams to be "+normalize(right)+" "+normalize(left).split(" ")[3]+" "+normalize(left).split(" ")[4])
