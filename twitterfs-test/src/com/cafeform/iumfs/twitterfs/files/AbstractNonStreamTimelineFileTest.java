/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cafeform.iumfs.twitterfs.files;

import com.cafeform.iumfs.twitterfs.Account;
import com.cafeform.iumfs.twitterfs.TwitterFsTestBase;
import static com.cafeform.iumfs.twitterfs.files.AbstractNonStreamTimelineFile.RATE_LIMIT_WINDOW;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import twitter4j.Paging;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 *
 * @author kaizawa
 */
public class AbstractNonStreamTimelineFileTest extends TwitterFsTestBase

{
    
    @Before
    @Override
    public void setUp () throws TwitterException
    {
        super.setUp();
    }
    
    @After
    public void tearDown ()
    {
    }

    /**
     * Test of isAutoUpdateEnabled method, of class AbstractNonStreamTimelineFile.
     */
    @Test
    public void testIsAutoUpdateEnabled ()
    {
        // For test, is is disabled.
        AbstractNonStreamTimelineFile.setAutoUpdateEnabled(false);
    }

    /**
     * Test of setAutoUpdateEnabled method, of class AbstractNonStreamTimelineFile.
     */
    @Test
    public void testSetAutoUpdateEnabled ()
    {
        AbstractNonStreamTimelineFile.setAutoUpdateEnabled(true);
        assertTrue(AbstractNonStreamTimelineFile.isAutoUpdateEnabled());        
        AbstractNonStreamTimelineFile.setAutoUpdateEnabled(false);
        assertFalse(AbstractNonStreamTimelineFile.isAutoUpdateEnabled());        
    }

    /**
     * Test of getTimeline method, of class AbstractNonStreamTimelineFile.
     */
    @Test
    public void testGetTimeline_0args () throws Exception
    {
        System.out.println("getTimeline");
        AbstractNonStreamTimelineFile file = 
                new DummyTimelineFile(account, "/user");
        file.getTimeline();
        // It's ok if it works without throwing exception.
        // Empty list must be return'ed by dummy getTimeline(Paging p)
        assertTrue(file.statusList.isEmpty());
    }

    /**
     * Test of getTimeline method, of class AbstractNonStreamTimelineFile.
     */
    @Test
    public void testGetTimeline_3args () throws Exception
    {
        DummyTimelineFile file = new DummyTimelineFile(account, "/user");
        file.addStatus(createStatus(3));
        file.addStatus(createStatus(2));        
        file.addStatus(createStatus(1));                
 
        // Arguments are not used.
        int result = file.getTimeline(1, 1, 1);
        assertEquals(3, file.statusList.size());
        

        // Order of the list should be reversed.
        // first one must be oldes one.
        assertEquals(1, file.statusList.get(0).getId());
        assertEquals(2, file.statusList.get(1).getId());
        assertEquals(3, file.statusList.get(2).getId());        
        
        // last id should be newest one, 3.
        assertEquals(3, file.lastId);

     }

    /**
     * Test of calculateIntervalByRateLimit method, of class AbstractNonStreamTimelineFile.
     */
    @Test
    public void testCalculateIntervalByRateLimit ()
    {
        long result;
        // Interval for RateLimit=15  is 1 mins + margine
        result = AbstractNonStreamTimelineFile.calculateIntervalByRateLimit(15);
        assertEquals(
                60000 + AbstractNonStreamTimelineFile.INTERVAL_MARGIN,
                result);

        
        // Interval for RateLimit=180 is 5 sec + margine
        result = AbstractNonStreamTimelineFile.calculateIntervalByRateLimit(180);
        assertEquals(
                5000 + AbstractNonStreamTimelineFile.INTERVAL_MARGIN,
                result);
    }


    /**
     * Test of getWaitSec method, of class AbstractNonStreamTimelineFile.
     */
    @Test
    public void testGetWaitSec ()
    {
        long result;
        RateLimitStatus limitStatus = mock(RateLimitStatus.class);
        // Normal case
        when(limitStatus.getSecondsUntilReset()).thenReturn(5);
        result = AbstractNonStreamTimelineFile.getWaitSec(limitStatus);
        assertEquals(5, result);
        
        // Huge value. It must be shorten to RATE_LIMIT_WINDOW * 60
        when(limitStatus.getSecondsUntilReset()).thenReturn(Integer.MAX_VALUE);
        result = AbstractNonStreamTimelineFile.getWaitSec(limitStatus);
        assertEquals(RATE_LIMIT_WINDOW * 60, result);
        
        // 0.  It must be greater than euqals to 0.
        when(limitStatus.getSecondsUntilReset()).thenReturn(0);
        result = AbstractNonStreamTimelineFile.getWaitSec(limitStatus);
        assertEquals(1, result);
        
        // -1.  It must be greater than euqals to 0.
        when(limitStatus.getSecondsUntilReset()).thenReturn(-1);
        result = AbstractNonStreamTimelineFile.getWaitSec(limitStatus);
        assertEquals(1, result);
        
    }

    public class AbstractNonStreamTimelineFileImpl 
    extends AbstractNonStreamTimelineFile
    {

        public AbstractNonStreamTimelineFileImpl ()
        {
            super(null, "");
        }

        public ResponseList<Status> getTimeline (Paging paging) throws TwitterException
        {
            return null;
        }
    }
    
    
    /**
     * Dummy Timeline file which implements AbstractNonStreamTimelineFile
     * getTimeline method of this class returns statuses which was added
     * by addStatus method.
     */ 
    private class DummyTimelineFile extends AbstractNonStreamTimelineFile
    {
        ResponseList statuses = new DummyResponseList<>();            
        public DummyTimelineFile (Account account, String pathname)
        {
            super(account, pathname);

        }
        
        public void addStatus (Status status) 
        {
            statuses.add(status);
        }
        
        @Override
        protected ResponseList<Status> getTimeline (Paging paging) 
                throws TwitterException
        {
            return statuses;
        }
    }
}
