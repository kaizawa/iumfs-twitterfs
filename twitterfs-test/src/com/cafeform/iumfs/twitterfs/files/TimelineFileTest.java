package com.cafeform.iumfs.twitterfs.files;

import com.cafeform.iumfs.twitterfs.Account;
import com.cafeform.iumfs.twitterfs.AccountImpl;
import com.cafeform.iumfs.twitterfs.TwitterFsTestBase;
import com.cafeform.iumfs.twitterfs.files.DefaultTimelineFile;
import com.cafeform.iumfs.twitterfs.files.AbstractNonStreamTimelineFile;
import static com.cafeform.iumfs.twitterfs.files.AbstractNonStreamTimelineFile.INTERVAL_MARGIN;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import twitter4j.TwitterException;

/**
 *
 * @author kaizawa
 */
public class TimelineFileTest extends TwitterFsTestBase
{    
    
    @Test
    public void testDefaultTimelineFileIntervals ()
    {
        Map<String, Long> intervalMap = new HashMap<>();
        intervalMap.put("/mentions", 60000L + INTERVAL_MARGIN);
        intervalMap.put("/home", 60000L + INTERVAL_MARGIN);
        intervalMap.put("/user", 5000L + INTERVAL_MARGIN);        
        intervalMap.put("/retweets_of_me", 60000L + INTERVAL_MARGIN);
        // must use default value        
        intervalMap.put("/DummmyFillle", 60000L + INTERVAL_MARGIN); 
        
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
