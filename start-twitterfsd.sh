#!/bin/sh

CLASSPATH=$PWD/cmd/lib/twitter4j-core-2.2.4.jar:$PWD/cmd/dist/twitterfsd.jar:$PWD/cmd/lib/twitter4j-stream-2.2.4.jar
#java -Djava.util.logging.config.file=log.prop -cp $CLASSPATH iumfs.twitterfs.Main
java -cp $CLASSPATH iumfs.twitterfs.Main
