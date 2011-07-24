/*
 * Copyright 2011 Kazuyoshi Aizawa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package iumfs;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/** 
 * User mode daemon for HDFS
 *
 *  * 
 * デバッグ出力する場合
 * -Djava.util.logging.config.file=log.prop
 *
 */
public class twitterfsd {

    static final String version = "0.1.5";  // version
    private static final Logger logger = Logger.getLogger(twitterfsd.class.getName());
    private static final int maxThreads = 4;
    static Map<String, File> fileMap = new HashMap<String, File>();

    public static void main(String args[]) {
        twitterfsd fsd = new twitterfsd();
        fsd.init();
        fsd.startDaemonThreads();
    }

    public void init() {
        fileMap.put("/post", new File("post", false, 0));
        fileMap.put("/home", new File("home", true, 20000));
        fileMap.put("/mentions", new File("mentions", true, 120000));
        fileMap.put("/public", new File("public", true, 600000));
        fileMap.put("/friends", new File("friends", true, 300000));
        fileMap.put("/retweeted_by_me", new File("retweeted_by_me", true, 600000));
        fileMap.put("/user", new File("user", true, 300000));
        fileMap.put("/retweeted_to_me", new File("retweeted_to_me", true, 600000));        
        fileMap.put("/retweets_of_me", new File("retweets_of_me", true, 600000));        
    }

    public void startDaemonThreads() {
        /*
        for (int i = 0; i < maxThreads; i++) {
            new DaemonThread().start();
        }
         */
        new DaemonThread().start();        
        startAutoupdateThreads();
    }
    
    public void startAutoupdateThreads(){
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        for (final File file : twitterfsd.fileMap.values()) {
            if (file.isTimeline() == true) {
                executor.scheduleAtFixedRate(new Runnable() {

                    public void run() {
                        file.getTimeline();
                    }
                }, file.getInterval(), file.getInterval(), TimeUnit.MILLISECONDS);
            }
        }        
    }
}
