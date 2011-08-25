#!/bin/sh

CLASSPATH=cmd/lib/twitter4j-core-2.2.4.jar:cmd/dist/twitterfsd.jar:cmd/lib/twitter4j-stream-2.2.4.jar
#java -Djava.util.logging.config.file=log.prop -cp $CLASSPATH iumfs.twitterfsd 
java -cp $CLASSPATH iumfs.twitterfsd 
