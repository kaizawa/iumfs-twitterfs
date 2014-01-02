package com.cafeform.iumfs.twitterfs;

import org.junit.Before;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import org.powermock.api.mockito.PowerMockito;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserStreamListener;

/**
 *
 * @author kaizawa
 */
@PrepareForTest({TwitterFsTestBase.class, TwitterFactoryAdapter.class})
@RunWith(PowerMockRunner.class)
public class TwitterFsTestBase {
    protected TwitterFsFileFactory fileFactory;
    protected Account account;    
    protected static final String USER1 = "user1";
    protected static final String DUMMY_TOKEN = "dummyToken";
    protected String[] expectedFiles =  {
        "mentions", 
        "home",
        "user", 
        "post", 
        "retweets_of_me"};
    protected String[] expectedDirectories =  {
        "friends",
        "followers", 
        "replies"};
    protected String[] setupExpectedFiles = {"setup"};

    
    @Before
    public void setUp () throws TwitterException
    {
        Twitter twitter = mock(Twitter.class);
        @SuppressWarnings("unchecked") 
        ResponseList<Status> mockStatuses 
                = (ResponseList<Status>)mock(ResponseList.class);
        // Always returns 0 length list
        when(mockStatuses.size()).thenReturn(0);
        when(twitter.getUserTimeline(
                (Paging)anyObject())).thenReturn(mockStatuses);
        when(twitter.getUserTimeline(
                anyString(), (Paging)anyObject())).thenReturn(mockStatuses);        
        
        // Mocked Twitter
        PowerMockito.mockStatic(TwitterFactoryAdapter.class);
        when(TwitterFactoryAdapter.getInstance(anyString())).thenReturn(twitter);
        PowerMockito.doNothing().when(TwitterFactoryAdapter.class);
        TwitterFactoryAdapter.setUserStreamListener(anyString(), 
                (UserStreamListener) anyObject());
    }
}
