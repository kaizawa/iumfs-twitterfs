package com.cafeform.iumfs.twitterfs.files;

import com.cafeform.iumfs.twitterfs.Account;
import com.cafeform.iumfs.twitterfs.IumfsTwitterFactory;
import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Followers directory which contains timeline file of authenticated user.
 */
public class FriendsDirectory extends AbstractUsersDirectory 
{
    public FriendsDirectory (Account account, String name)
    {
        super(account, name);
    }

    @Override
    protected PagableResponseList getUsersList (long cursor) 
            throws TwitterException
    {
        final Twitter twitter
                = IumfsTwitterFactory.getInstance(getUsername());

        return twitter.getFriendsList(
                getUsername(),
                cursor);
    }
}
