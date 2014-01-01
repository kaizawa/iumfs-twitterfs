/*
 * Copyright 2011 Kazuyoshi Aizawa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this IumfsFile except in compliance with the License.
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
package com.cafeform.iumfs.twitterfs;

import com.cafeform.iumfs.IumfsFile;
import com.cafeform.iumfs.NotADirectoryException;
import com.cafeform.iumfs.twitterfs.files.UserTimeLineFile;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ka78231
 */
public class Account
{
    Logger logger = Logger.getLogger(Account.class.getName());
    private String username;
    private static final Map<String, Account> accountMap = new ConcurrentHashMap<>();
    private IumfsFile rootDir;
    private ScheduledExecutorService userTimelineScheduler = null;
    private final BlockingQueue<UserTimeLineFile> userTimelineQueue
            = new LinkedBlockingQueue<>();

    public Account (String username)
    {
        this.username = username;
    }

    /**
     * @return the user
     */
    public String getUsername ()
    {
        return username;
    }

    /**
     * @param user the user to set
     */
    public void setUsername (String user)
    {
        this.username = user;
    }

    /**
     * @return the accountMap
     */
    public static Map<String, Account> getAccountMap ()
    {
        return accountMap;
    }

    public void setRootDirectory (IumfsFile rootDir)
    {
        if (!rootDir.isDirectory())
        {
            throw new NotADirectoryException();
        }
        this.rootDir = rootDir;
    }

    public IumfsFile getRootDirectory ()
    {
        return rootDir;
    }

    /**
     * Add user time line which will be feching by dequeing in order.
     *
     * @param newFile
     */
    public void addUserTimeLine (UserTimeLineFile newFile)
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
                        UserTimeLineFile nextFile = userTimelineQueue.poll();
                        nextFile.getTimeline();
                        userTimelineQueue.offer(nextFile);
                    } 
                    catch (Exception ex)
                    {
                        logger.log(
                                Level.SEVERE,
                                "User timeline watcher thread is down " +
                                "due to " + ex.toString(), ex);
                    }
                }
            };
            userTimelineScheduler.scheduleAtFixedRate(
                    queueWatcher,
                    0,
                    UserTimeLineFile.calculateInterval(),
                    TimeUnit.MILLISECONDS);
        }
        userTimelineQueue.offer(newFile);
    }
}
