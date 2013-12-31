package com.cafeform.iumfs.twitterfs.files;

import com.cafeform.iumfs.twitterfs.Account;
import com.cafeform.iumfs.twitterfs.IumfsTwitterFactory;
import static com.cafeform.iumfs.twitterfs.files.TwitterFsFile.logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Represents non-streaming timelines.
 * That includs mentions, home and retweets_of_me but not user timeline.
 * 
 * @author kaizawa
 */
public class DefaultTimelineFile extends AbstractNormalTimelineFile
{
    public DefaultTimelineFile (
            Account account, 
            String filename)
    {
        super(account, filename);
        interval = calculateInterval();
        if (autoUpdateEnabled)
        {
            readAhead();
            startAutoUpdateThreads();
        }
    }
    
    /**
     * Initialise routine for this type of file
     */
    private void readAhead ()
    {
        /*
        * If not stream timeline, read 1 page(20 tweets)
        * as initial read.
        */
        getTimeline(1, 20, last_id);
    }

    @Override
    protected ResponseList<Status> getTimeLine (Paging paging)
    {
        Twitter twitter = IumfsTwitterFactory.getInstance(getUsername());
        ResponseList<Status> statuses = null;
        String name = getName();

        try 
        {
            switch (name)
            {
                case "mentions":
                    statuses = twitter.getMentionsTimeline(paging);
                    break;
                case "home":
                    // For home TL, stream API should be used.
                    statuses = twitter.getHomeTimeline(paging);
                    break;
                case "retweets_of_me":
                    statuses = twitter.getRetweetsOfMe(paging);
                    break;
                default:
                    logger.severe("Unknown timeline(\"" + name
                            + "\") specified.");
                    System.exit(1);
            }    
        } 
        catch (TwitterException ex)
        {
            logger.log(Level.SEVERE,
                    "Got Twitter Exception statusCode = " + ex.getStatusCode(),
                    ex);
        } 
        return statuses;
    }
    
    @Override
    final protected void startAutoUpdateThreads ()

    {
        // TODO: each time line should use same thread pool
        ScheduledExecutorService executor;

        // Invokde timeline retrieving thread. 
        Runnable timelineUpdater = new Runnable()
        {
            @Override
            public void run ()
            {
                getTimeline();
            }
        };
        
        executor = Executors.newSingleThreadScheduledExecutor();
        // retrieve periodically
        executor.scheduleAtFixedRate(
                timelineUpdater,
                getInterval(),
                getInterval(),
                TimeUnit.MILLISECONDS);
    }
 
    /**
     * Calculate rate limit based on type of timeline
     *
     * @return the interval
     */
    public long getInterval()
    {
        return interval;
    }

    /**
     * @return the interval
     */
    private long calculateInterval()
    {
        String name = getName();
        int rateLimit = 0;
        long val;

        switch (name)
        {
            case "mentions":
                rateLimit = MENTION_RATE_LIMIT;
                break;
            case "home":
                rateLimit = HOME_RATE_LIMIT;
                break;
            case "user":
                rateLimit = USER_RATE_LIMIT;
                break;
            case "retweets_of_me":
                rateLimit = RETWEET_RATE_LIMIT;
                break;
            default:
                rateLimit = DEFAULT_RATE_LIMIT;
        }
        // Need to take MAX_PAGES into account, since API would be called 
        // MAX_PAGES times per each trial.
        val = (RATE_LIMIT_WINDOW * 60 / rateLimit) * 1000 * MAX_PAGES;
        logger.log(Level.FINE, "Calculate interval for " + getName()
                + " timeline" + " is " + val);
        return val;
    }
}
