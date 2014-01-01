package com.cafeform.iumfs.twitterfs;

import com.cafeform.iumfs.twitterfs.files.AbstractUsersDirectory;
import com.cafeform.iumfs.twitterfs.files.TwitterFsDirectory;
import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Followers directory which contains timeline file of authenticated user.
 */
class FollowersDirectory extends AbstractUsersDirectory {

    public FollowersDirectory (Account account, String name)
    {
        super(account, name);
    }

    @Override
    protected PagableResponseList getUsersList (long cursor) 
            throws TwitterException
    {
        final Twitter twitter
                = IumfsTwitterFactory.getInstance(getUsername());
        return twitter.getFollowersList(
                getUsername(),
                cursor);
    }
}
