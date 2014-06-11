#!/bin/sh
cd nlp
~/jdk/bin/java -classpath "./*" edu.stanford.nlp.parser.lexparser.LexicalizedParser -outputFormat "typedDependencies" -sentences newline edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz -


# FULL PARSER:
#!/bin/sh
#cd nlp
#~/jdk/bin/java -classpath "./*" edu.stanford.nlp.parser.lexparser.LexicalizedParser -outputFormat "penn,typedDependencies" -sentences newline edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz -
