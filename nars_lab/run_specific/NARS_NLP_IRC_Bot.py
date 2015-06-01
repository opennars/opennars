import thread 
from subprocess import Popen, PIPE, STDOUT
import subprocess
import re,os
import socket
import sys
import time
import itertools

knowledge=""""""

#NLP messages to NARS start with ' and end with the usual punctuations ? . .., example: 'this is a sentence.

def init_proc():
    global proc
    proc = subprocess.Popen(["./java","-cp","OpenNARS_GUI.jar","nars.core.NARRun"], stdin=subprocess.PIPE, stdout=subprocess.PIPE) 

NLPonlyMode=True
server = "irc.freenode.net"
channel = "#nars"
botnick = "nars"
Narsese_Filter=["EXE: ","Answer:","OUT:"]
Said=[]
Max_Outputs_Before_Reset=999999999999

irc = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
irc.connect((server, 6667))
irc.send("USER "+ botnick +" " + botnick +" "+ botnick +" :Hallo!\r\n")
irc.send("NICK "+ botnick +"\r\n")
irc.send("PRIVMSG nickserv :iNOOPE\r\n")
irc.send("JOIN "+ channel +"\r\n") #fails on euirc, works in all others, euirc is not completely RFC 2810 compatible maybe?
print "connected"
init_proc()
print "NARS instance created"
#proc.stdin.write("*volume=0\n");
#print "NARS is set to shut up: "+proc.stdout.readline()
print "Narsese Filter applied: '"+str(Narsese_Filter)+"'"
print "Max Outputs before Reset: "+str(Max_Outputs_Before_Reset)
cnt=0

proc.stdin.write(knowledge)
print "Knowledge put in"

def nlp_manipulate(TEXT):
    TEXT=TEXT.replace("'","").replace("who","?who").replace("where","?where").replace("what","?what").replace("when","?when")
    parts=[a.replace("\t","").replace(" ","").replace("\r","").replace("\n","") for a  in TEXT.split(" ") if a!=""]
    punctuation=parts[-1][-1]
    if not (punctuation in ["!",".","?"]):
        return None
    parts[-1]=parts[-1][:-1]
    sentence="<(*,"+(",".join(parts))+") --> sentence>"+punctuation+" :|:"
    return sentence

def isNLP(TEXT):
    return TEXT.startswith("'")

def NLP_output(TEXT):
    if "Answer: " not in TEXT and "OUT: " not in TEXT:
        return TEXT #executions
    punct=""
    if " --> sentence>?" in TEXT:
        punct="?"
    if " --> sentence>." in TEXT:
        punct="."
    if punct=="" and NLPonlyMode and "Answer:" not in TEXT:
        return None
    if punct=="":
        return TEXT
    Begin=TEXT.split(" --> sentence>.")[0].replace("\n","").replace("\r","")
    Begin2=Begin.replace("<","").replace(","," ").replace("(*","").replace(")","")
    if "/" in Begin2 or "\\" in Begin2 or "(" in Begin2 or ")" in Begin2 or "<" in Begin2 or ">" in Begin2:
        if NLPonlyMode:
            return None
        return TEXT
    return Begin2
    
def receive_thread(a):
    global cnt, Said
    while True:
       msg=proc.stdout.readline()
       if "% {" in msg:
           msg=msg.split("% {")[0]+"%"
       if msg!=None and msg!="" and True in [u in msg for u in Narsese_Filter]: #we received an execution
          print "NAR output: "+msg
          bReset= cnt>=Max_Outputs_Before_Reset
          MSG=NLP_output(msg)
          if MSG==None:
              continue
          cnt+=1
          transform=lambda u: u.replace("OUT:  ","").replace("OUT: ","").replace("Answer:  ","").replace("Answer: ","")
          if "Answer:" not in MSG and transform(MSG) in Said:
              continue
          Said+=[transform(MSG)]
          irc.send("PRIVMSG "+ channel +" : "+MSG+"\r\n")
          if bReset:
              irc.send("PRIVMSG "+ channel +" : NAR RESET happened (max. amount of output happened)\r\n")
              cnt=0
              Said=[]
              #proc.close()
              #init_proc();
              proc.stdin.write("*****\n")
              proc.stdin.write(knowledge)
              print "Knowledge put in"
          
thread.start_new_thread(receive_thread,(1,))

while True:
    try:
        text=irc.recv(2040)
        if "PING" in text:
            print "ping"
            STR='PONG :' + text.split("PING :")[1].split("\n")[0] + '\r\n';
            irc.send(STR)
        else:
            if "VERSION" in text:
                print "version"
                irc.send("JOIN "+ channel +"\r\n") #join when version private message comes :D
            else:
                if "system" in text.lower() or "from os" in text.lower() or "import os" in text.lower():
                    print "skipped"
                    continue
                SPL=text.split(":")
                TEXT=":".join(SPL[2:len(SPL)])
                if TEXT.replace(" ","").replace("\n","").replace("\r","")=="":
					continue
                print TEXT
                if TEXT.startswith("**"):
                    proc.stdin.write("*****\n")
                    proc.stdin.write(knowledge)
                    print "Knowledge put in"
                if TEXT.startswith("<") or TEXT.startswith("(") or isNLP(TEXT):
                    if isNLP(TEXT):
                        Said+=[TEXT.replace("\n","").replace("\r","").replace("\t","").replace("'","")[:-1]]
                        TEXT=nlp_manipulate(TEXT)
                        if TEXT==None:
                            continue
                    print "NAR input: "+TEXT
                    try:
                        proc.stdin.write(TEXT+"\n")
                    except:
                        None
                
    except:
        print "exception"
        None
