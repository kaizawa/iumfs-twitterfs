package com.cafeform.iumfs.twitterfs.files;

import com.cafeform.iumfs.twitterfs.Account;
import static com.cafeform.iumfs.twitterfs.files.AbstractTimelineFile.MAX_STATUSES;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Represents non-streaming timelines.
 */
abstract public class AbstractNonStreamTimelineFile 
extends AbstractTimelineFile implements NormalTimelineFile
{

    protected static boolean autoUpdateEnabled = true;

    public static boolean isAutoUpdateEnabled ()
    {
        return autoUpdateEnabled;
    }

    public static void setAutoUpdateEnabled (boolean autoUpdateEnabled)
    {
        AbstractNonStreamTimelineFile.autoUpdateEnabled = autoUpdateEnabled;
    }
    protected long interval = 0L;

    public AbstractNonStreamTimelineFile (Account account, String pathname)
    {
        super(account, pathname);
    }

    @Override
    public void getTimeline ()
            throws TwitterException
    {
        getTimeline(MAX_STATUSES, lastId);
    }

    /**
     * Read time line from Twitter This function just invoke getTimeline(int
     * page, int count, long since)
     *
     * @param count
     * @param since
     * @throws twitter4j.TwitterException
     */
    protected void getTimeline (int count, long since) 
            throws TwitterException
    {
        int cnt;
        int page = 1; // page start from 1 !!
        /*
         * Retrieve status up to max_pages.
         * If MAX_PAGES is 4, it's 80 status for public timeline, 
         * and 800 status for the ther timilene.
         *   20 * 4 = 80
         *   200 * 4 = 800
         */
        do
        {
            cnt = getTimeline(page, count, since);
            page++;
        } while ((cnt == count && page < MAX_PAGES) || (cnt == 20 && page < MAX_PAGES));
    }

    /**
     * Retrieve Statuses in given pages. This method must be called exclusively.
     *
     * @param page
     * @param count
     * @param since
     * @return
     * @throws twitter4j.TwitterException
     */
    synchronized public int getTimeline (int page, int count, long since)
            throws TwitterException
    {
        ResponseList<Status> statuses;
        String name = getName();

        Paging paging = new Paging(page, count, since);
        statuses = getTimeLine(paging);

        logger.fine("Got " + name + " timeline, "
                + statuses.size() + " Statuses in page " + page);

        if (statuses.size() == 0)
        {
            // last status
            return 0;
        }
        // Set first status(newest) as last_id.
        lastId = statuses.get(0).getId();
        for (Status status : statuses)
        {
            addStatusToList(status);
        }
        if (initialRead)
        {
            /*
             * Set last status(oldest) to base_id.
             */
            baseId = statuses.get(statuses.size() - 1).getId();
            logger.finer("base_id = " + baseId);
            initialRead = false;
        }
        return statuses.size();
    }

    abstract protected ResponseList<Status> getTimeLine (Paging paging)
            throws TwitterException;
}
