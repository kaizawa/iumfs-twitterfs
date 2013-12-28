package iumfs.twitterfs;

import iumfs.FileFactory;
import iumfs.InvalidUserException;
import iumfs.IumfsFile;
import iumfs.Request;
import static iumfs.twitterfs.TwitterfsFile.logger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 *
 * @author kaizawa
 */
public class TwitterFsFileFactory implements FileFactory 
{
    @Override
    public IumfsFile createFile(Request request)
    {
        return getFile(request.getUserName(), request.getPathname());
    }
    
    /*
     * Get a IumfsFile object of given pathname for a user.
     * This method is called from TwitterXXXXRequest methods.
     * So this is an entry point for file operation.
     */
    public IumfsFile getFile(String username, String pathname) 
    {
        if (username.isEmpty()) 
        {
            throw new InvalidUserException("Unknown user \"" + username + "\" specified");
        }

        logger.log(Level.FINER, "pathname={0}, usernaem={1}", 
                new Object[]{pathname, username});
        Account account = Account.getAccountMap().get(username);

        if (account == null) 
        {
            account = new Account(username);
            Map<String, IumfsFile> fileMap = new ConcurrentHashMap<>();
            initFileMap(fileMap, account);
            account.setFileMap(fileMap);
            Account.getAccountMap().put(username, account);
            logger.log(Level.FINE, "New Account for " + username + " created."); 
        }
        /* 
         * If file map is initial file map(just has 2 entires) but 
         * has access toke, it seems to have finished setting up twitter
         * account. Create new file map which has timeline file and swap
         * it to existing file map.
         */
        logger.log(Level.FINE, "Map size=" + account.getFileMap().size());
        if (account.getFileMap().size() == 2)
        {
            Prefs.sync();
            if(Prefs.get(username + "/accessToken").length() > 0) 
            {
                Map<String, IumfsFile> fileMap = new ConcurrentHashMap<>();
                fillFileMap(fileMap, account);
                account.setFileMap(fileMap);
            }
        }        
        return account.getFileMap().get(pathname);
    }
    
    private void fillFileMap(Map<String, IumfsFile> fileMap, Account account)
    {
        fileMap.put("/post", new PostFile(account, "/post"));
        fileMap.put("/home", new TimelineFile(account, "/home", true, 0L));
        fileMap.put("/mentions", new TimelineFile(account, "/mentions", false, 120000));
        fileMap.put("/friends", new TimelineFile(account, "/friends", false, 300000));
        fileMap.put("/user", new TimelineFile(account, "/user", false, 300000));
        fileMap.put("/retweets_of_me", new TimelineFile(account, "/retweets_of_me", false, 600000));
        fileMap.put("/", new DirectoryFile(account, ""));
    }

    private void initFileMap(Map<String, IumfsFile> fileMap, Account account) 
    {
        fileMap.put("/setup", new SetupFile(account, "/setup"));
        fileMap.put("/", new DirectoryFile(account, "/"));
    }
}
