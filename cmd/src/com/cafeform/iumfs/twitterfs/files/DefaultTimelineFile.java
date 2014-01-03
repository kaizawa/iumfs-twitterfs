package com.cafeform.iumfs.twitterfs.files;

import com.cafeform.iumfs.twitterfs.Account;
import com.cafeform.iumfs.twitterfs.TwitterFactoryAdapter;
import static com.cafeform.iumfs.twitterfs.files.TwitterFsFile.logger;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import static java.util.logging.Level.*;
import java.util.logging.Logger;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Represents non-streaming timelines. That includs mentions, home and
 * retweets_of_me but not user timeline.
 *
 * @author kaizawa
 */
public class DefaultTimelineFile extends AbstractNonStreamTimelineFile
{

    public DefaultTimelineFile (
            Account account,
            String pathname)
    {
        super(account, pathname);
        interval = calculateInterval();
        if (autoUpdateEnabled)
        {
            readAhead();
            startAutoUpdateThread();
        }
    }

    /**
     * Initialise routine for this type of file
     */
    private void readAhead ()
    {
        try
        {
            /*
             * If not stream timeline, read 1 page(20 tweets)
             * as initial read.
             */
            getTimeline();
        } catch (TwitterException ex)
        {
            logger.log(INFO, "Cannot get " + getName() + " timeline", ex);
        }
    }

    @Override
    protected ResponseList<Status> getTimeLine (Paging paging)
    {
        Twitter twitter = TwitterFactoryAdapter.getInstance(getUsername());
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
        } catch (TwitterException ex)
        {
            logger.log(Level.SEVERE,
                    "Got Twitter Exception statusCode = " + ex.getStatusCode(),
                    ex);
        }
        return statuses;
    }

    final protected void startAutoUpdateThread ()
    {
        // TODO: each time line should use same thread pool
        ScheduledExecutorService executor;

        // Invokde timeline retrieving thread. 
        Runnable timelineUpdater = new Runnable()
        {
            @Override
            public void run ()
            {
                try
                {
                    getTimeline();
                } 
                catch (TwitterException ex)
                {
                    logger.log(INFO, "Cannot get " + getName() + 
                            " timeline.", ex);
                }
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
    public long getInterval ()
    {
        return interval;
    }

    /**
     * @return the interval
     */
    private long calculateInterval ()
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
        // Add margin, try not to exceed rate limit accidentally.
        val += INTERVAL_MARGIN;
        logger.log(Level.FINE, "Calculate interval for " + getName()
                + " timeline" + " is " + val);
        return val;
    }
}
