/*
 * Copyright 2010 Kazuyoshi Aizawa
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
import iumfs.NotSupportedException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

abstract public class TwitterfsFile extends File {
    protected boolean is_timeline = false;
    protected static final Logger logger = Logger.getLogger(Main.class.getName());
    protected Account account;

    TwitterfsFile(Account account, String name){
        super(name);
        this.account = account;
        Date now = new Date();
        setAtime(now.getTime());
        setCtime(now.getTime());
        setMtime(now.getTime());          
    }

    public boolean isTimeline() {
        return is_timeline;
    }
    
    /*
     *Return file type
     * If direcory, return VDIR, othewise VREG.(reqular file)
     */
    @Override
    public long getFileType() {
        if (isDirectory()) {
            return File.VDIR;
        } else {
            return File.VREG;
        }
    }
    
    protected String getUsername(){
        return account.getUsername();
    }
    
    @Override
    public boolean isDirectory(){
        return directory;
    }
    
    public void setDirectory(boolean directory){
        this.directory = directory;
    }
    
    @Override
    public void create(){
        throw new NotSupportedException();
    }    
    
    static public void fillFileMap(Map<String, File> fileMap, Account account) {
        fileMap.put("/post", new PostFile(account, "/post"));
        fileMap.put("/home", new TimelineFile(account, "/home", true, 0L));
        fileMap.put("/mentions", new TimelineFile(account, "/mentions", false, 120000));
        fileMap.put("/public", new TimelineFile(account, "/public", false, 600000));
        fileMap.put("/friends", new TimelineFile(account, "/friends", false, 300000));
        fileMap.put("/retweeted_by_me", new TimelineFile(account, "/retweeted_by_me", false, 600000));
        fileMap.put("/user", new TimelineFile(account, "/user", false, 300000));
        fileMap.put("/retweeted_to_me", new TimelineFile(account, "/retweeted_to_me", false, 600000));
        fileMap.put("/retweets_of_me", new TimelineFile(account, "/retweets_of_me", false, 600000));
        fileMap.put("/", new DirectoryFile(account, ""));
    }

    static public void initFileMap(Map<String, File> fileMap, Account account) {
        fileMap.put("/setup", new SetupFile(account, "/setup"));
        fileMap.put("/", new DirectoryFile(account, "/"));
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
        Account account = Account.getAccountMap().get(username);

        if (account == null) {
            account = new Account(username);
            Map<String, File> fileMap = new ConcurrentHashMap<String, File>();
            initFileMap(fileMap, account);
            account.setFileMap(fileMap);
            Account.getAccountMap().put(username, account);
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
        return Account.getAccountMap().get(username).getFileMap();
    }    
}