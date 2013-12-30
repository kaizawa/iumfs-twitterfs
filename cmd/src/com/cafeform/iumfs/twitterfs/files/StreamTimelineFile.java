package com.cafeform.iumfs.twitterfs.files;

import com.cafeform.iumfs.twitterfs.Account;
import com.cafeform.iumfs.twitterfs.IumfsTwitterFactory;
import com.cafeform.iumfs.twitterfs.Prefs;
import static com.cafeform.iumfs.twitterfs.files.TwitterFsFile.logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;

/**
 * Represents Stream timeline.
 * @author kaizawa
 */
public class StreamTimelineFile extends TimelineFile
{

    public StreamTimelineFile (Account account, String filename)
    {
        super(account, filename);
        startAutoUpdateThreads();
    }

    private void startAutoUpdateThreads ()
    {
        // TODO: each time line should use same thread pool
        ScheduledExecutorService executor;

        // Invokde timeline retrieving thread. 
        // Stream API (only home)
        // Thread created in TwitterStream.sample()
        TwitterStream twitterStream
                = new TwitterStreamFactory().getInstance();
        twitterStream.setOAuthConsumer(
                Prefs.get("OAuthConsumerKey"),
                Prefs.get("consumerSecret"));
        twitterStream.setOAuthAccessToken(
                IumfsTwitterFactory.getAccessToken(getUsername()));
        twitterStream.addListener(listener);
        twitterStream.user();
    }

    protected UserStreamListener listener = new UserStreamListener()
    {
        @Override
        public void onStatus (Status status)
        {
            logger.log(Level.FINER, "Read Status id=" + status.getId());
            logger.finest(TimelineFile.statusToFormattedString(status));
            addStatusToList(status);
        }

        @Override
        public void onDeletionNotice (StatusDeletionNotice statusDeletionNotice)
        {
        }

        @Override
        public void onTrackLimitationNotice (int numberOfLimitedStatuses)
        {
        }

        @Override
        public void onException (Exception ex)
        {
        }

        @Override
        public void onScrubGeo (long l, long l1)
        {
        }

        @Override
        public void onDeletionNotice (long l, long l1)
        {
        }

        @Override
        public void onFriendList (long[] longs)
        {
        }

        @Override
        public void onFavorite (User user, User user1, Status status)
        {
        }

        @Override
        public void onUnfavorite (User user, User user1, Status status)
        {
        }

        @Override
        public void onFollow (User user, User user1)
        {
        }

        @Override
        public void onDirectMessage (DirectMessage dm)
        {
        }

        @Override
        public void onUserListMemberAddition (User user, User user1, UserList ul)
        {
        }

        @Override
        public void onUserListMemberDeletion (User user, User user1, UserList ul)
        {
        }

        @Override
        public void onUserListSubscription (User user, User user1, UserList ul)
        {
        }

        @Override
        public void onUserListUnsubscription (User user, User user1, UserList ul)
        {
        }

        @Override
        public void onUserListCreation (User user, UserList ul)
        {
        }

        @Override
        public void onUserListUpdate (User user, UserList ul)
        {
        }

        @Override
        public void onUserListDeletion (User user, UserList ul)
        {
        }

        @Override
        public void onUserProfileUpdate (User user)
        {
        }

        @Override
        public void onBlock (User user, User user1)
        {
        }

        @Override
        public void onUnblock (User user, User user1)
        {
        }

        @Override
        public void onStallWarning (StallWarning sw)
        {
        }
    };
}
