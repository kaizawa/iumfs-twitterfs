#!/bin/sh

CLASSPATH=cmd/lib/twitter4j-core-2.2.1.jar:cmd/dist/twitterfsd.jar
#java -Djava.util.logging.config.file=log.prop -cp $CLASSPATH iumfs.twitterfsd 
java -cp $CLASSPATH iumfs.twitterfsd 
