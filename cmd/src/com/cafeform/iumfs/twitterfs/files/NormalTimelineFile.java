package com.cafeform.iumfs.twitterfs.files;

import com.cafeform.iumfs.IumfsFile;
import twitter4j.TwitterException;

/**
 * 
 */
public interface NormalTimelineFile extends IumfsFile
{
    public void getTimeline () throws TwitterException;
}
