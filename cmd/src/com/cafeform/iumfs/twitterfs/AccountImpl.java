package com.cafeform.iumfs.twitterfs;

import com.cafeform.iumfs.IumfsFile;
import com.cafeform.iumfs.NotADirectoryException;
import java.util.logging.Logger;

/**
 *
 * @author kaizawa
 */
public class AccountImpl implements Account
{
    static final Logger logger = Logger.getLogger(Account.class.getName());
    private String username;

    private IumfsFile rootDir;
    private final UserTimelineFileManager userTimelineManager = 
            new UserTimelineFileManagerImpl();

    @Override
    public UserTimelineFileManager getUserTimelineManager ()
    {
        return userTimelineManager;
    }

    public AccountImpl (String username)
    {
        this.username = username;
    }

    /**
     * @return the user
     */
    @Override
    public String getUsername ()
    {
        return username;
    }

    /**
     * @param user the user to set
     */
    @Override
    public void setUsername (String user)
    {
        this.username = user;
    }

    @Override
    public void setRootDirectory (IumfsFile rootDir)
    {
        if (!rootDir.isDirectory())
        {
            throw new NotADirectoryException();
        }
        this.rootDir = rootDir;
    }

    @Override
    public IumfsFile getRootDirectory ()
    {
        return rootDir;
    }
}
