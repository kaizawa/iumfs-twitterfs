package com.cafeform.iumfs.twitterfs.files;

import com.cafeform.iumfs.twitterfs.Account;
import com.cafeform.iumfs.twitterfs.IumfsTwitterFactory;
import static com.cafeform.iumfs.twitterfs.files.TwitterFsFile.logger;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Express Authenticated user time line and friends time line which is affected
 * by statuses/user_timeline rate limit
 *
 * @author kaizawa
 */
public class UserTimeLine extends AbstractNormalTimelineFile
{
    public UserTimeLine (Account account, String filename)
    {
        super(account, filename);
    }

    @Override
    protected ResponseList<Status> getTimeLine (Paging paging)
            throws TwitterException
    {
        Twitter twitter = IumfsTwitterFactory.getInstance(getUsername());
        ResponseList<Status> statuses = null;
        String name = getName();

        switch (name)
        {
            case "user":
                statuses = twitter.getUserTimeline(paging);
                break;
            default:
                if (getPath().startsWith("/friends/"))
                {
                    statuses = twitter.getUserTimeline(name, paging);
                } else
                {
                    logger.severe("Unknown timeline(\"" + name
                            + "\") specified.");
                    System.exit(1);
                }
        }
        return statuses;
    }

    @Override
    protected void startAutoUpdateThreads ()
    {
        //hehe
    }
}
