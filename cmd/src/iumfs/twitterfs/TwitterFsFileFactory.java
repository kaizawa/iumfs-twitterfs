package iumfs.twitterfs;

import iumfs.FileFactory;
import iumfs.InvalidUserException;
import iumfs.IumfsFile;
import iumfs.Request;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kaizawa
 */
public class TwitterFsFileFactory implements FileFactory
{
    static final Logger logger = Logger.getLogger(TwitterFsFileFactory.class.getName());

    @Override
    public IumfsFile getFile(Request request)
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
                new Object[]
                {
                    pathname, username
                });
        Account account = Account.getAccountMap().get(username);

        if (account == null)
        {
            account = new Account(username);
            account.setRootDirectory(getInitialRootDirectory(account));
            Account.getAccountMap().put(username, account);
            logger.log(Level.FINE, "New Account for " + username + " created.");
        }

        /* 
         * If file root directory has 1 entiry but has access toke, it seems 
         * to have finished setting up twitter account. 
         * Create default root directory swap it to existing root directory.
         */
        logger.log(Level.FINE, "Map size="
                + account.getRootDirector().listFiles().length);
        if (2 == account.getRootDirector().listFiles().length)
        {
            Prefs.sync();
            if (Prefs.get(username + "/accessToken").length() > 0)
            {
                account.setRootDirectory(getDefaultRootDirectory(account));
            }
        }
        return lookup(account.getRootDirector(), pathname);
    }

    private IumfsFile getDefaultRootDirectory(Account account)
    {
        TwitterFsDirectory rootDir = new TwitterFsDirectory(account, "");
        rootDir.addFile(new PostFile(account, "post"));
        rootDir.addFile(new TimelineFile(account, "home", true, 0L));
        rootDir.addFile(new TimelineFile(account, "mentions", false, 120000));
        rootDir.addFile(new TimelineFile(account, "friends", false, 300000));
        rootDir.addFile(new TimelineFile(account, "user", false, 300000));
        rootDir.addFile(new TimelineFile(account, "retweets_of_me", false, 600000));
        rootDir.addFile(new TwitterFsDirectory(account, "friends"));
        // Includes itself
        rootDir.addFile(rootDir);
        return rootDir;
    }

    private IumfsFile getInitialRootDirectory(Account account)
    {
        TwitterFsDirectory rootDir = new TwitterFsDirectory(account, "");
        rootDir.addFile(new SetupFile(account, "setup"));
        // Includes itself        
        rootDir.addFile(rootDir);        
        return rootDir;
    }

    private IumfsFile lookup(IumfsFile directory, String pathname)
    {
        if (pathname.startsWith("/"))
        {
            pathname = pathname.substring(1);
        }

        String[] paths = pathname.split("/", 2);

        for (IumfsFile file : directory.listFiles())
        {
            if (file.getName().equals(paths[0]))
            {
                if (1 == paths.length)
                {
                    return file;
                }
                else
                {
                    if (file.isDirectory())
                    {
                        // path is correct, but not a target entry
                        // dig more.
                        lookup(file, paths[1]);
                    }
                    else 
                    {
                        // the entry must be directory but file!
                        return null;
                    }
                }
            }
        }
        return null;
    }
}
