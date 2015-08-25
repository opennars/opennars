testcases="""
COPY A EXAMPLE FILE HERE
"""



def lineToJava(l,a):
    l=l.replace(";",",").replace("%","").replace(">. ",">.").replace(">! ",">!").replace(">? ",">?").replace("). ",").").replace(")! ",")!").replace(")? ",")?")
    func = "n.believe(\""
    if "OUT:" in l:
        func = "n.mustBelieve(100,\""
    l=l.replace("OUT: ","").replace("OUT:","")
    if ">?" in l or ")?" in l:
        func = "n.ask(\""
    if ">!" in l or ")!" in l:
        func = "n.goal(\""
    return (func+l+"\").en(\""+a.replace("// ","").replace("//","")+"\");").replace(". \"",".\"").replace("? \"","?\"").replace("! \"","!\"")

def transform(test):
    lines = [z for z in test.split("\n") if any(c.isalpha() for c in z) or "//" in z]
    title = lines[0].replace(" ","_")[1:]
    body=""
    
    for i in xrange(2,len(lines),2):
        body+=lineToJava(lines[i-1],lines[i])+"\n"
    
    header="""
@Test
public void """ + title + """() throws InvalidInputException {
""" + body + "}"
    return header
    

tests=[z for z in testcases.split("**********") if "//" in z]
print transform(tests[0])


text = ""
for s in tests:
    text += transform(s)+"\n\n"

text_file = open("Output_.txt", "w")
text_file.write(text)
text_file.close()
