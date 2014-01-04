package com.cafeform.iumfs.twitterfs.files;

import twitter4j.TwitterException;

/**
 * 
 */
public interface NormalTimelineFile extends TwitterFsFile
{
    public void getTimeline () throws TwitterException;
}
