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
        Main instance = new Main();
        instance.startDaemonThreads();
    }

    static public void initFileMap(Map<String, File> fileMap, Account account) {
        fileMap.put("/post", new TwitterfsFile(account, "post", false, 0));
        fileMap.put("/home", new TwitterfsFile(account, "home", true, true));
        fileMap.put("/mentions", new TwitterfsFile(account, "mentions", true, 120000));
        fileMap.put("/public", new TwitterfsFile(account, "public", true, 600000));
        fileMap.put("/friends", new TwitterfsFile(account, "friends", true, 300000));
        fileMap.put("/retweeted_by_me", new TwitterfsFile(account, "retweeted_by_me", true, 600000));
        fileMap.put("/user", new TwitterfsFile(account, "user", true, 300000));
        fileMap.put("/retweeted_to_me", new TwitterfsFile(account, "retweeted_to_me", true, 600000));
        fileMap.put("/retweets_of_me", new TwitterfsFile(account, "retweets_of_me", true, 600000));
        fileMap.put("/", new TwitterfsFile(account, "", false));
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
    
    static public File getFile(String username, String pathName) {
       if(username.isEmpty()){
           throw new InvalidUserException("Unknown user \"" + username + "\" specified");
       }
       
       logger.finer("usernaem=" + username + ", pathName=" + pathName);
        Account account = getAccountMap().get(username);
        
        if(account == null){
            account = new Account(username);
            Map<String, File> fileMap = new ConcurrentHashMap<String, File>(); 
            initFileMap(fileMap, account);
            account.setFileMap(fileMap);
            getAccountMap().put(username, account);
            logger.finer("New Account for "+ username + " created.");
        }
        logger.finer("Account username=" + account.getUsername());

        return account.getFileMap().get(pathName);
    }
    
    static public Map<String, File> getFileMap(String user){
        return accountMap.get(user).getFileMap();
    }
}
