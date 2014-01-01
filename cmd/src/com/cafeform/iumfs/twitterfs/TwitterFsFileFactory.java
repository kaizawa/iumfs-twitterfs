package com.cafeform.iumfs.twitterfs;

import com.cafeform.iumfs.twitterfs.files.AbstractTimelineFile;
import com.cafeform.iumfs.twitterfs.files.PostFile;
import com.cafeform.iumfs.twitterfs.files.AbstractUsersDirectory;
import com.cafeform.iumfs.twitterfs.files.TwitterFsDirectory;
import com.cafeform.iumfs.FileFactory;
import com.cafeform.iumfs.Files;
import com.cafeform.iumfs.InvalidUserException;
import com.cafeform.iumfs.IumfsFile;
import com.cafeform.iumfs.Request;
import com.cafeform.iumfs.twitterfs.files.DefaultTimelineFile;
import com.cafeform.iumfs.twitterfs.files.FriendsDirectory;
import com.cafeform.iumfs.twitterfs.files.SetupFile;
import com.cafeform.iumfs.twitterfs.files.StreamTimelineFile;
import com.cafeform.iumfs.twitterfs.files.UserTimelineFileImpl;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author kaizawa
 */
public class TwitterFsFileFactory implements FileFactory
{
    static final Logger logger = Logger.getLogger(TwitterFsFileFactory.class.getName());

    @Override
    public IumfsFile getFile (Request request)
    {
        return getFile(request.getUserName(), request.getPathname());
    }

    /*
     * Get a IumfsFile object of given pathname for a user.
     * This method is called from TwitterXXXXRequest methods.
     * So this is an entry point for file operation.
     */
    public IumfsFile getFile (String username, String pathname)
    {
        if (username.isEmpty())
        {
            throw new InvalidUserException("Unknown user \"" + username + "\" specified");
        }

        logger.log(Level.FINER, "pathname=" + pathname + ", usernaem=" + username);
               
        Account account = AccountMap.get(username);

        if (account == null)
        {
            account = new AccountImpl(username);
            account.setRootDirectory(createInitialRootDirectory(account));
            AccountMap.put(username, account);
            logger.log(Level.FINE, "New Account for " + username + " created.");
        }

        /* 
         * If file root directory has 1 entiry but has access toke, it seems 
         * to have finished setting up twitter account. 
         * Create default root directory swap it to existing root directory.
         */
        logger.log(Level.FINE, "Map size="
                + account.getRootDirectory().listFiles().length);
        if (1 == account.getRootDirectory().listFiles().length)
        {
            Prefs.sync();
            if (Prefs.get(username + "/accessToken").length() > 0)
            {
                account.setRootDirectory(createDefaultRootDirectory(account));
            }
        }
        // Return root directory if requested path is /.
        if ("/".equals(pathname))
        {
            return account.getRootDirectory();
        }
        
        if (isReplyPostFile(pathname))
        {
            // Post file for reply can be exist any place under root dir.
            return new PostFile(
                    account, 
                    pathname, 
                    Files.getNameFromPathName(pathname) + " ");
        }
        
        return lookup(account.getRootDirectory(), pathname);
    }

    private IumfsFile createDefaultRootDirectory (Account account)
    {
        TwitterFsDirectory rootDir = new TwitterFsDirectory(account, "/");
        rootDir.addFile(new PostFile(account, "/post"));
        rootDir.addFile(new StreamTimelineFile(account, "/home"));
        rootDir.addFile(new DefaultTimelineFile(account, "/mentions"));
        rootDir.addFile(new DefaultTimelineFile(account, "/retweets_of_me"));
        rootDir.addFile(new UserTimelineFileImpl(account, "/user"));        
        TwitterFsDirectory friendsDir = new FriendsDirectory(account, "/friends");
        TwitterFsDirectory followersDir = new FollowersDirectory(account, "/followers");        
        rootDir.addFile(friendsDir);
        rootDir.addFile(followersDir);
        return rootDir;
    }

    private IumfsFile createInitialRootDirectory (Account account)
    {
        TwitterFsDirectory rootDir = new TwitterFsDirectory(account, "/");
        rootDir.addFile(new SetupFile(account, "/setup"));
        return rootDir;
    }

    /**
     * Lookup pathname under directory.
     * This method is recursively called. So pathname could be relative path.
     * 
     * @param directory
     * @param pathname 
     * @return 
     */
    private IumfsFile lookup(IumfsFile directory, String pathname)
    {
        if (pathname.startsWith("/"))
        {
            pathname = pathname.substring(1);
        }
        
        String[] paths = pathname.split("/", 2);

        for (IumfsFile file : directory.listFiles())
        {
            // check first element of pathname exist within directory.
            if (file.getName().equals(paths[0]))
            {
                // Found. See if it's target directory entry.
                if (1 == paths.length)
                {
                    logger.log(Level.FINER, "Found " + paths[0] + 
                            " in " + directory.getName());
                    return file;
                }
                else
                {
                    if (file.isDirectory())
                    {
                        // path is correct, but not a target entry
                        // dig more.
                        return lookup(file, paths[1]);
                    }
                    else 
                    {
                        logger.log(Level.WARNING, paths[0] + " is expected" +
                                " to be directory, but regular file.");
                        // the entry must be directory but file!
                        return null;
                    }
                }
            }
        }
        logger.log(Level.FINER, "Cannot find " + paths[0] + " in " + 
                ("".equals(directory.getName()) ? "/":(directory.getName())));
                     
        return null;
    }

    /**
     * See if file name start with @ mark.
     * @param pathname
     * @return 
     */
    private boolean isReplyPostFile (String pathname)
    {
        String name = Files.getNameFromPathName(pathname);
        return name.startsWith("@");
    }
}
