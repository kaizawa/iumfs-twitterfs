package com.cafeform.iumfs.twitterfs;

import com.cafeform.iumfs.Files;
import com.cafeform.iumfs.IumfsFile;
import com.cafeform.iumfs.twitterfs.files.UserTimelineFile;
import com.cafeform.iumfs.twitterfs.files.UserTimelineFileImpl;
import com.cafeform.iumfs.twitterfs.files.UserTimelineFileAdapter;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manage user timelines for each account
 */
public class UserTimelineFileManagerImpl implements UserTimelineFileManager
{
    static final Logger logger = Logger.getLogger(
            UserTimelineFileManagerImpl.class.getName());
    private ScheduledExecutorService userTimelineScheduler = null;
    private final BlockingQueue<UserTimelineFile> userTimelineQueue = 
            new LinkedBlockingQueue<>();

    /**
     * Add user time line which will be feching by dequeing in order.
     *
     * @param newFile
     */
    private void addUserTimeLine (UserTimelineFile newFile)
    {
        // Start user timeline updater scheduler
        startScheduler();
        if(userTimelineQueue.contains(newFile))
        {
           logger.log(Level.WARNING, "UserTimeline " + newFile.getName() + 
                   " has already exists.");
        }
        else 
        {
             userTimelineQueue.offer(newFile);
        }
    }

    /**
     * Start separate thread which update all user/friends timelines
     * in fixed inteval.
     */
    private void startScheduler ()
    {
        if (null == userTimelineScheduler)
        {
            userTimelineScheduler
                    = Executors.newScheduledThreadPool(1);
            Runnable queueWatcher = new Runnable()
            {
                @Override
                public void run ()
                {
                    try
                    {
                        UserTimelineFile nextFile = userTimelineQueue.poll();
                        if (null != nextFile)
                        {
                            nextFile.getTimeline();
                            userTimelineQueue.offer(nextFile);    
                        }
                    } 
                    catch (Exception ex)
                    {
                        logger.log(
                                Level.SEVERE,
                                "User timeline watcher thread is down "
                                + "due to " + ex.toString(), ex);
                    }
                }
            };
            userTimelineScheduler.scheduleAtFixedRate(
                    queueWatcher,
                    0,
                    UserTimelineFileImpl.calculateInterval(),
                    TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * Find and return existing timeline by timeline name.(not pathname)
     * Null if not exist. 
     * 
     * @param name
     * @return 
     */
    private UserTimelineFile lookupUserTimeline (String name) 
    {
       for(UserTimelineFile file : userTimelineQueue)
       {
           if (file.getName().equals(name))
           {
               logger.log(Level.FINER, name + " found in existing list.");
               return file;
           }
       }
       logger.log(Level.FINER, name + " not found in existing list.");
        return null;
    }

    @Override
    public IumfsFile getTimelineFile (Account account, String pathName)
    {
        String name  = Files.getNameFromPathName(pathName);

        IumfsFile newFile;
        // Lookup existing UserTimeline by name.
        UserTimelineFile userTimelineFile = lookupUserTimeline(name);
        if (null == userTimelineFile)
        {
             userTimelineFile = new UserTimelineFileImpl(account, pathName);
             addUserTimeLine(userTimelineFile);
        }
        
        // Create adopter instance which include actual timeline file.
        newFile = new UserTimelineFileAdapter(pathName, userTimelineFile);
        return newFile;
    }
}
