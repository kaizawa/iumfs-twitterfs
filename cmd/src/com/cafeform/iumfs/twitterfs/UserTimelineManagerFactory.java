package com.cafeform.iumfs.twitterfs;

/**
 * Facgtory class which provide a singleton UserTimelineFileManger instance.
 * Returned instance is shared among process
 * 
 * @author kaizawa
 */
public class UserTimelineManagerFactory 
{
    private static class UserTimelineFileManagerLoader 
    {
        public static final UserTimelineFileManager INSTANCE 
                = new UserTimelineFileManagerImpl();
    }
    
    public static UserTimelineFileManager getInstance ()
    {
        return UserTimelineFileManagerLoader.INSTANCE;
    }
}
