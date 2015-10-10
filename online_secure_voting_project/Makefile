JCC = javac
JFLAGS = -g

default: LaServ.class VoterCli.class VfSer.class

LaServ.class: LaServ.java
	$(JCC) $(JFLAGS) LaServ.java

VoterCli.class: VoterCli.java
	$(JCC) $(JFLAGS) VoterCli.java

VfSer.class: VfSer.java
	$(JCC) $(JFLAGS) VfSer.java
	
clean:
	$(RM) *.class 
