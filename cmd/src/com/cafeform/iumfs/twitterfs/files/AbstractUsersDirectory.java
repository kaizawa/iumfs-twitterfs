package com.cafeform.iumfs.twitterfs.files;

import com.cafeform.iumfs.IumfsFile;
import com.cafeform.iumfs.NotADirectoryException;
import com.cafeform.iumfs.twitterfs.Account;
import static com.cafeform.iumfs.twitterfs.files.TwitterFsFile.logger;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import twitter4j.PagableResponseList;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Directory entry which includes users name as file. 
 * Entries under this directory are dynamically created.
 */
abstract public class AbstractUsersDirectory extends TwitterFsDirectory
{
    private static final long UPDATE_INTERVAL = 3600000; // 1h 
    private static final long REQUEST_INTERVAL = 60000;  // 1m

    private Date lastUpdate = new Date(0);
    private final ScheduledExecutorService pool
            = Executors.newSingleThreadScheduledExecutor();

    public AbstractUsersDirectory (Account account, String name)
    {
        super(account, name);
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
            //    Shutdown but not terminated.
            if (!pool.isShutdown()
                    || (pool.isShutdown() && pool.isTerminated()))
            {
                pool.execute(new UsersUpdater());
                pool.shutdown();
                lastUpdate = new Date();
            }
        } else
        {
            logger.log(Level.FINER, "updater not called");
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
            logger.log(Level.FINER, "updater is called");
            PagableResponseList<User> usersList = null;
            while (true)
            {
                logger.log(Level.FINER, "cursor = " + cursor);
                try
                {
                    usersList = getUsersList(cursor);
                     
                    for (User user : usersList)
                    {
                        addFile(new UserTimeLineFile(
                                account,
                                "/" + getName() + "/" + user.getScreenName()));
                    }
                    cursor = usersList.getNextCursor();
                }
                catch (TwitterException ex)
                {
                    logger.log(Level.WARNING, "Unable to get users list: "
                            + ex.getMessage(), ex);
                }

                logger.log(Level.FINER, "Got " + usersList.size()
                        + " users data. Next cursor = " + cursor);

                    // Wait REQUEST_INTERVAL to avoid exceeding 
                // rate limit of twitter api.
                // Also wait if this is first try and failed with exception.
                if (0 != cursor)
                {
                    try
                    {
                        Thread.sleep(REQUEST_INTERVAL);
                        } catch (InterruptedException ex){}
                    }
                    else
                {
                    // Have gotton all users data.
                    break;
                }
            }
        }
    }
    
    abstract protected PagableResponseList getUsersList (long cursor) 
            throws TwitterException;

    @Override
    public void addFile (IumfsFile file) throws NotADirectoryException
    {
        logger.log(Level.FINEST, file.getName() + " is added.");
        super.addFile(file);
    }
}
