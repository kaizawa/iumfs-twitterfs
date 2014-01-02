package com.cafeform.iumfs.twitterfs.files;

import com.cafeform.iumfs.twitterfs.Account;

/**
 *
 * @author kaizawa
 */
public class RepliesDirectory extends TwitterFsDirectory 
{
    public final static String PATH_NAME = "/replies";

    public RepliesDirectory (Account account)
    {
        super(account, PATH_NAME);
    }

}
