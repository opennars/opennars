#Generates input for the vision system, use https://sagecell.sagemath.org/
A=random_matrix(RR,30,30)
with open("Output.txt", "w") as text_file:
    for i in range(30):
        for j in range(30):
            di = i/(size-1)*2-1
            dj = j/(size-1)*2-1
            text_file.write("<A["+str(di.n())+","+str(dj.n())+"] --> [bright]>. :|: %"+str(A[i,j])+"%\n")
show(plot(A))