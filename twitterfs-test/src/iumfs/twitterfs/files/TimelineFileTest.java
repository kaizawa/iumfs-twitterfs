package iumfs.twitterfs.files;

import com.cafeform.iumfs.twitterfs.Account;
import com.cafeform.iumfs.twitterfs.TwitterFsFileFactory;
import com.cafeform.iumfs.twitterfs.files.AbstractTimelineFile;
import com.cafeform.iumfs.twitterfs.files.DefaultTimelineFile;
import com.cafeform.iumfs.twitterfs.files.AbstractNormalTimelineFile;
import com.cafeform.iumfs.twitterfs.files.UserTimeLineFile;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author kaizawa
 */
public class TimelineFileTest 
{
    private static final String USER = "user1";
    
    @Before
    public void setUp()
    {
        AbstractNormalTimelineFile.setAutoUpdateEnabled(false);
    }
    
    @Test
    public void testDefaultTimelineFIleIntervals ()
    {
        Map<String, Long> intervalMap = new HashMap<>();
        intervalMap.put("/mentions", 60000L);
        intervalMap.put("/home", 60000L);
        intervalMap.put("/retweets_of_me", 60000L);
        intervalMap.put("/DummmyFillle", 60000L); // must use default value        
        
        Account account = new Account(USER);
        
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
