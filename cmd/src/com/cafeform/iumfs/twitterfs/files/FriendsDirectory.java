package com.cafeform.iumfs.twitterfs.files;

import com.cafeform.iumfs.twitterfs.Account;
import com.cafeform.iumfs.twitterfs.TwitterFactoryAdapter;
import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Followers directory which contains timeline file of authenticated user.
 */
public class FriendsDirectory extends AbstractUsersDirectory 
{
    public final static String PATH_NAME = "/friends";
    
    public FriendsDirectory (Account account)
    {
        super(account, PATH_NAME);
    }

    @Override
    protected PagableResponseList<User> getUsersList (long cursor) 
            throws TwitterException
    {
        final Twitter twitter
                = TwitterFactoryAdapter.getInstance(getUsername());
        return twitter.getFriendsList(getUsername(), cursor);
    }
}
