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
import iumfs.InvalidUserException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import twitter4j.TwitterException;

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
    private static Map<String, Account> accountMap = new ConcurrentHashMap<String, Account>();

    public static void main(String args[]) {
        /*
         * setup options. It prepares twitter OAuth key.
         */
        if (args.length == 2) {
            setup(args);
        }

        Main instance = new Main();
        instance.startDaemonThreads();
    }

    /**
     * This routine just setup Twitter OAuth access token.
     * Once it finiced setting up, exit the process.
     */
    protected static void setup(String[] args) {
        try {
            String username = args[1];
            if ("setup".equals(args[0]) && username.length() != 0) {
                IumfsTwitterFactory.getAccessToken(username);
                if (Prefs.get(username + "/accessToken").isEmpty()) {
                    System.out.println("Failed to setup Twitter access token");
                } else {
                    System.out.println("Twitter access token setup sucessfully");
                    System.exit(0);
                }
            } else {
                System.out.println("Usage: iumfs.twitterfs.Main setup <username>");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.exit(1);
    }

    static public void fillFileMap(Map<String, File> fileMap, Account account) {
        fileMap.put("/post", new PostFile(account, "post"));
        fileMap.put("/home", new TimelineFile(account, "home", true, 0L));
        fileMap.put("/mentions", new TimelineFile(account, "mentions", false, 120000));
        fileMap.put("/public", new TimelineFile(account, "public", false, 600000));
        fileMap.put("/friends", new TimelineFile(account, "friends", false, 300000));
        fileMap.put("/retweeted_by_me", new TimelineFile(account, "retweeted_by_me", false, 600000));
        fileMap.put("/user", new TimelineFile(account, "user", false, 300000));
        fileMap.put("/retweeted_to_me", new TimelineFile(account, "retweeted_to_me", false, 600000));
        fileMap.put("/retweets_of_me", new TimelineFile(account, "retweets_of_me", false, 600000));
        fileMap.put("/", new DirectoryFile(account, ""));
    }

    static public void initFileMap(Map<String, File> fileMap, Account account) {
        fileMap.put("/setup", new SetupFile(account, "setup"));
        fileMap.put("/", new DirectoryFile(account, ""));
    }

    /**
     * @return the accountMap
     */
    public static Map<String, Account> getAccountMap() {
        return accountMap;
    }

    public void startDaemonThreads() {
        /*
        for (int i = 0; i < maxThreads; i++) {
        new DaemonThread().start();
        }
         */
        new TwitterfsDaemonThread().start();
    }

    /*
     * Get a File object of given pathname for a user.
     * This method is called from TwitterXXXXRequest methods.
     * So this is an entry point for file operation.
     */
    static public File getFile(String username, String pathname) {
        if (username.isEmpty()) {
            throw new InvalidUserException("Unknown user \"" + username + "\" specified");
        }

        logger.finer("pathname=" + pathname + ", usernaem=" + username);
        Account account = getAccountMap().get(username);

        if (account == null) {
            account = new Account(username);
            Map<String, File> fileMap = new ConcurrentHashMap<String, File>();
            initFileMap(fileMap, account);
            account.setFileMap(fileMap);
            getAccountMap().put(username, account);
            logger.fine("New Account for " + username + " created.");
        }
        /* 
         * If file map is initial file map(just has 2 entires) but 
         * has access toke, it seems to have finished setting up twitter
         * account. Create new file map which has timeline file and swap
         * it to existing file map.
         */
        logger.fine("Map size=" + account.getFileMap().size());
        if (account.getFileMap().size() == 2){
            Prefs.sync();
            if(Prefs.get(username + "/accessToken").length() > 0) {
                Map<String, File> fileMap = new ConcurrentHashMap<String, File>();
                fillFileMap(fileMap, account);
                account.setFileMap(fileMap);
            }
        }        
        return account.getFileMap().get(pathname);
    }

    static public Map<String, File> getFileMap(String username) {
        return accountMap.get(username).getFileMap();
    }
}
