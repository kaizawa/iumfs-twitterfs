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
package iumfs.twitterfs;

import iumfs.File;
import java.util.*;
import java.util.logging.Logger;

/** 
 * User mode daemon for TwitterFS
 *
 *  * 
 * デバッグ出力する場合
 * -Djava.util.logging.config.file=log.prop
 *
 */
public class Main {
    static final String version = "0.1.9";  // version
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static final int maxThreads = 4;
    static Map<String, File> fileMap = new HashMap<String, File>();

    public static void main(String args[]) {
        Main fsd = new Main();
        fsd.init();
        fsd.startDaemonThreads();
    }

    public void init() {
        fileMap.put("/post", new TwitterfsFile("post", false, 0));
        fileMap.put("/home", new TwitterfsFile("home", true, true));
        fileMap.put("/mentions", new TwitterfsFile("mentions", true, 120000));
        fileMap.put("/public", new TwitterfsFile("public", true, 600000));
        fileMap.put("/friends", new TwitterfsFile("friends", true, 300000));
        fileMap.put("/retweeted_by_me", new TwitterfsFile("retweeted_by_me", true, 600000));
        fileMap.put("/user", new TwitterfsFile("user", true, 300000));
        fileMap.put("/retweeted_to_me", new TwitterfsFile("retweeted_to_me", true, 600000));
        fileMap.put("/retweets_of_me", new TwitterfsFile("retweets_of_me", true, 600000));
        fileMap.put("/", new TwitterfsFile("", true));
    }

    public void startDaemonThreads() {
        /*
        for (int i = 0; i < maxThreads; i++) {
        new DaemonThread().start();
        }
         */
        new TwitterfsDaemonThread().start();
    }
}
