# PoCo Scanner Tool
Static analysis tool for PoCo that visually maps your policy's *match* expressions 
(i.e. the portion of an exchange before the => operator) 
to methods in the monitored program. This can be beneficial when designing a policy
to see if your expressions are too broad (which could cause performance issues at
best and cause undesirable behavior at worst) or too narrow (which could cause
your policy to miss security-relevant events.

## Third-Party Libraries Used
*   [ASM 5.0.1](http://asm.ow2.org) - for reading compiled Java bytecode

*   [ANTLR 4.5](http://www.antlr.org) - lexer and parser generator for PoCo grammar

## Building
This repository contains Intellij IDEA project structure as well as a Gradle build file.
You can use the Gradle build task to compile the sources. **Note that Java 8 is required.**

Changes to the grammar files in `src/main/antlr` will require the ANTLR sources to
be regenerated (and may break code that interacts with this generated code). Java
files generated from the ANTLR grammars should reside in `src/main/java` in the
`com.coryjuhlin.pocoscanner.antlr` namespace.