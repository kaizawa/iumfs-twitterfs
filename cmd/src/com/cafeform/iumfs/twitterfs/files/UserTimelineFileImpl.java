package com.cafeform.iumfs.twitterfs.files;

import com.cafeform.iumfs.twitterfs.Account;
import com.cafeform.iumfs.twitterfs.TwitterFactoryAdapter;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import static java.util.logging.Level.*;
import java.util.logging.Logger;

/**
 * Represents Authenticated user timeline and friends timeline which is affected
 * by statuses/user_timeline rate limit
 *
 * @author kaizawa
 */
public class UserTimelineFileImpl extends AbstractNonStreamTimelineFile
{
    private static final Logger logger = 
            Logger.getLogger(UserTimelineFileImpl.class.getName());
    public UserTimelineFileImpl (Account account, String pathname)
    {
        super(account, pathname);
    }

    @Override
    protected ResponseList<Status> getTimeline (Paging paging)
            throws TwitterException
    {
        Twitter twitter = TwitterFactoryAdapter.getInstance(getUsername());
        ResponseList<Status> statuses;
        String name = getName();

        statuses = twitter.getUserTimeline(name, paging);

        log(logger, FINER, "UserTimeline rate limit for "
                + getAccount().getUsername()
                + ": "
                + statuses.getRateLimitStatus().getRemaining()
                + "/"
                + statuses.getRateLimitStatus().getLimit()
                + " reset in "
                + statuses.getRateLimitStatus().getSecondsUntilReset()
                + " sec.");
        return statuses;
    }

    // Compare object by its Name (not pathname)
    @Override
    public boolean equals (Object obj)
    {
        if (obj instanceof UserTimelineFileImpl)
        {
            return this.getName().equals(((UserTimelineFileImpl) obj).getName());
        }
        return false;
    }

    @Override
    public int hashCode ()
    {
        int hash = 3;
        return hash;
    }

    @Override
    public String toString ()
    {
        return getName();
    }
}
