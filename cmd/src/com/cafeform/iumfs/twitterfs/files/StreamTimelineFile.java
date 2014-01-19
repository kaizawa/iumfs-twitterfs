package com.cafeform.iumfs.twitterfs.files;

import com.cafeform.iumfs.twitterfs.Account;
import com.cafeform.iumfs.twitterfs.TwitterFactoryAdapter;
import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;
import static java.util.logging.Level.*;
import java.util.logging.Logger;


/**
 * Represents Stream timeline.
 * @author kaizawa
 */
public class StreamTimelineFile extends AbstractTimelineFile
{
    private static final Logger logger = 
            Logger.getLogger(StreamTimelineFile.class.getName());
    public StreamTimelineFile (Account account, String pathname)
    {
        super(account, pathname);
        // Invokde timeline retrieving thread. 
        TwitterFactoryAdapter.setUserStreamListener(getUsername(), listener);
    }

    protected UserStreamListener listener = new UserStreamListener()
    {
        @Override
        public void onStatus (Status status)
        {
            logger.log(FINER, getUserAndName() + 
                    " Read Status id=" + status.getId());
            logger.log(FINEST, getUserAndName() + 
                    " " + AbstractTimelineFile.statusToFormattedString(status));
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
