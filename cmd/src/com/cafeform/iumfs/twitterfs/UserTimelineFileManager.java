package com.cafeform.iumfs.twitterfs;

import com.cafeform.iumfs.IumfsFile;

/**
 *
 * @author kaizawa
 */
public interface UserTimelineFileManager 
{
    public IumfsFile getTimelineFile (Account account, String pathname);
}
