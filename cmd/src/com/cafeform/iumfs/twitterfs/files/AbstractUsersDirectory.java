package com.cafeform.iumfs.twitterfs.files;

import com.cafeform.iumfs.IumfsFile;
import com.cafeform.iumfs.NotADirectoryException;
import com.cafeform.iumfs.twitterfs.Account;
import com.cafeform.iumfs.twitterfs.UserTimelineManagerFactory;
import static com.cafeform.iumfs.twitterfs.files.AbstractNonStreamTimelineFile.getWaitSec;
import com.cafeform.iumfs.utilities.Util;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import twitter4j.PagableResponseList;
import twitter4j.TwitterException;
import twitter4j.User;
import static java.util.logging.Level.*;
import java.util.logging.Logger;

/**
 * Directory entry which includes users name as file. Entries under this
 * directory are dynamically created.
 */
abstract public class AbstractUsersDirectory extends TwitterFsDirectory
{
    private static final Logger logger = 
            Logger.getLogger(AbstractUsersDirectory.class.getName());

    private static final long UPDATE_INTERVAL = 3600000; // 1h 
    private static final long REQUEST_INTERVAL = 60000;  // 1m

    private Date lastUpdate = new Date(0);
    private ScheduledExecutorService pool = null;

    public AbstractUsersDirectory (Account account, String pathname)
    {
        super(account, pathname);
    }

    @Override
    public IumfsFile[] listFiles ()
    {
        Date now = new Date();

        // If has passed UPDATE_INTERVAL since last update
        // update users list again.
        if (now.getTime() - lastUpdate.getTime() > UPDATE_INTERVAL)
        {
            // Update if
            //    First try (not shutdown).
            //       or
            //    Terminated.
            if (null == pool || pool.isTerminated())
            {
                pool = Executors.newSingleThreadScheduledExecutor();
                pool.execute(new UsersUpdater());
                pool.shutdown();
                lastUpdate = new Date();
            }
        } else
        {
            logger.log(FINER, getUserAndName() + " updater not called");
        }
        return super.listFiles();
    }

    /**
     * Task which update users of authenticated user
     */
    private class UsersUpdater implements Runnable
    {
        long cursor = -1;

        @Override
        public void run ()
        {
            logger.log(FINER, getUserAndName() + " updater  is called");
            PagableResponseList<User> usersList = null;
            while (true)
            {
                logger.log(FINER, getUserAndName() +  " cursor = " + cursor);
                try
                {
                    usersList = getUsersList(cursor);

                    for (User user : usersList)
                    {
                        IumfsFile newFile = UserTimelineManagerFactory.
                                getInstance().getTimelineFile(
                                        account,
                                        "/" + getName() + "/"
                                        + user.getScreenName());
                        addFile(newFile);
                    }
                    cursor = usersList.getNextCursor();
                } catch (TwitterException ex)
                {
                    handleTwitterException(ex);
                }

                logger.log(FINER, getUserAndName() + 
                        " Got " + usersList.size() + 
                        " users data for " + getName() + 
                        ". Next cursor = " + cursor);

                // Wait REQUEST_INTERVAL to avoid exceeding 
                // rate limit of twitter api.
                // Also wait if this is first try and failed with exception.
                if (0 != cursor)
                {
                    Util.sleep(REQUEST_INTERVAL);
                } else
                {
                    // Have gotton all users data.
                    break;
                }
            }
        }

        private void handleTwitterException (TwitterException ex)
        {
            if (ex.exceededRateLimitation())
            {
                // User Timeline rate limit exceeds.                                            
                // wait for reset time.                                            
                long waitSec = getWaitSec(ex.getRateLimitStatus());
                logger.log(INFO, getUserAndName() + 
                        " Exceeds rate limit for retrieving users list. " + 
                        "Wait for " + waitSec + " sec.");
                Util.sleep(waitSec * 1000);
            } else
            {
                logger.log(WARNING, getUserAndName() + 
                        "Unable to get users list." + ex.getMessage(), ex);
            }
        }
    }

    abstract protected PagableResponseList<User> getUsersList (long cursor)
            throws TwitterException;

    @Override
    public void addFile (IumfsFile file) throws NotADirectoryException
    {
        logger.log(FINEST, getUserAndName() + " Added " + file.getName());
        super.addFile(file);
    }
}
