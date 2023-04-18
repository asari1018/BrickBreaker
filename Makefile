#####
## if you want to specify a directory java installed explicitly,
## set the java binary directory here
#####
JAVABIN=
#  JAVABIN=$(HOME)/parawork/jdk-16.0.1/bin/
#  JAVABIN=/usr/lib/jvm/default-java/bin

#####
## set javafx-sdk directory your installed 
#####
JAVAFXMODULE=../javafx-sdk-11.0.2/lib

#####
##  set proxy server address and port number, if your machine connects
##  to the internet via proxy
JAVADOCPROXY=
#  JAVADOCPROXY=-J-Dhttp.proxyHost=proxy.csc.titech.ac.jp -J-Dhttp.proxyPort=8080

#####
## csc room setting
#####
#JAVABIN=/opt/Java/JavaVirtualMachines/jdk-11.0.2.jdk/Contents/Home/bin/
#JAVAFXMODULE=/opt/Java/JavaFX/javafx-sdk-11.0.2/lib
##JAVADOCPROXY= -J-Dhttp.proxyHost=proxy.csc.titech.ac.jp -J-Dhttp.proxyPort=8080

# OS-dependent commands and separator
ifeq ($(OS),Windows_NT)
	SEP=;
	RM=rd /s /q
	FIND=echo
else
	SEP=:
	RM=rm -rf
	FIND=find
endif

JAVA=$(JAVABIN)java $(JAVAFLAGS)
JAVAC=$(JAVABIN)javac $(JAVACFLAGS)
JAVADOC0=$(JAVABIN)javadoc $(JAVADOCFLAGS)
MKDIR=mkdir
MAKE=make

MODULEPATH=$(JAVAFXMODULE)
CLASSPATH=lib/*$(SEP)
CLASSFLAGS=-classpath "bin$(SEP)$(CLASSPATH)resource"
JAVACCLASSFLAGS=-classpath "$(CLASSPATH)"
MODULEFLAGS=--module-path $(MODULEPATH) --add-modules javafx.controls,javafx.swing
JAVACFLAGS= -encoding utf8 -d bin -sourcepath src $(MODULEFLAGS) $(JAVACCLASSFLAGS) -Xlint:deprecation -Xdiags:verbose -Xlint:unchecked
JAVAFLAGS = $(MODULEFLAGS) $(CLASSFLAGS)
JAVADOCFLAGS= -html5 -encoding utf-8 -charset utf-8 -package -d javadoc -sourcepath src $(JAVADOCPROXY) -link https://docs.oracle.com/javase/jp/14/docs/api -link https://openjfx.io/javadoc/11 $(MODULEFLAGS) $(CLASSFLAGS)

SERVADDR=localhost
NUMBER=`echo \`whoami\`0 | md5 | sed -e "s/[^0-9]//g" |cut -c 1-6`
PARAMETER=$(SERVADDR)

.PHONY: clean doc

.SUFFIXES: .class .java

.java.class:
	echo $@

.class:
	$(MAKE) bin
	$(eval CLS := $(subst /,.,$(@:src/%=%)))
	$(JAVAC) $@.java
	$(JAVA) $(CLS)  $(PARAMETER)

ALL::	Main15

Main15:
	$(MAKE) src/para/$@

Main16:
	$(MAKE) src/para/$@

Main17:
	$(MAKE) src/para/$@

Main18:
	$(MAKE) src/para/$@

Main19:
	$(MAKE) src/para/$@

Main20:
	$(MAKE) src/para/$@

Main21:
	$(MAKE) src/para/$@

Main22:
	$(MAKE) src/para/$@

Main23:
	$(MAKE) src/para/$@

Main24:
	$(MAKE) src/para/$@

Game04:
	$(MAKE) src/para/$@

GameServer01:
	$(MAKE) src/para/$@

bin:
	$(MKDIR) bin

javadoc:
	$(MKDIR) javadoc

clean:: bin javadoc
	$(RM) bin
	$(RM) javadoc

cleanall:: bin javadoc
	$(FIND) . -name "._*" -exec $(RM) {} \;
	$(RM) bin
	$(RM) javadoc

doc:: javadoc
	$(FIND) . -name "._*" -exec $(RM) {} \;
	cp -R lib/javadoc/easycl javadoc
	$(JAVADOC0) para para.graphic.shape para.graphic.target para.graphic.parser para.graphic.camera para.graphic.opencl para.game
