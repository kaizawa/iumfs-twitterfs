package com.cafeform.iumfs.twitterfs.files;

import com.cafeform.iumfs.twitterfs.Account;
import com.cafeform.iumfs.twitterfs.AccountImpl;
import com.cafeform.iumfs.twitterfs.TwitterFsTestBase;
import com.cafeform.iumfs.twitterfs.files.DefaultTimelineFile;
import com.cafeform.iumfs.twitterfs.files.AbstractNonStreamTimelineFile;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author kaizawa
 */
public class TimelineFileTest extends TwitterFsTestBase
{    
    @Before
    public void setUp()
    {
        AbstractNonStreamTimelineFile.setAutoUpdateEnabled(false);
    }
    
    @Test
    public void testDefaultTimelineFIleIntervals ()
    {
        Map<String, Long> intervalMap = new HashMap<>();
        intervalMap.put("/mentions", 60000L);
        intervalMap.put("/home", 60000L);
        intervalMap.put("/retweets_of_me", 60000L);
        intervalMap.put("/DummmyFillle", 60000L); // must use default value        
        
        account = new AccountImpl(USER1);
        
        for (String name : intervalMap.keySet())
        {
            DefaultTimelineFile file = 
                    new DefaultTimelineFile(account, name);            
            assertEquals(
                    "Interval for "  + name + " is incorrect", 
                    (long)intervalMap.get(name),
                    file.getInterval());            
        }
    }

}
