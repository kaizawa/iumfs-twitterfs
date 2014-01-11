package com.cafeform.iumfs.twitterfs;

import com.cafeform.iumfs.twitterfs.files.AbstractNonStreamTimelineFile;
import com.cafeform.iumfs.twitterfs.files.NormalTimelineFile;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserStreamListener;
import java.util.Date;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.fail;

/**
 *
 * @author kaizawa
 */
@PrepareForTest(
{
    TwitterFsTestBase.class, TwitterFactoryAdapter.class
})
@RunWith(PowerMockRunner.class)
public class TwitterFsTestBase
{

    protected TwitterFsFileFactory fileFactory;
    protected Account account;
    protected static final String USER1 = "user1";
    protected static final String DUMMY_TOKEN = "dummyToken";
    protected String[] expectedFiles =
    {
        "mentions",
        "home",
        "user",
        "post",
        "retweets_of_me"
    };
    protected String[] expectedDirectories =
    {
        "friends",
        "followers",
        "replies"
    };
    protected String[] setupExpectedFiles =
    {
        "setup"
    };

    @Before
    public void setUp () 
    {
        try
        {
            UserTimelineFileManager manager = UserTimelineManagerFactory.getInstance();
            // Get userTimelineQuee from UserTimelineFileManager
            Field field = manager.getClass().getDeclaredField("userTimelineQueue");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
                    Queue<NormalTimelineFile> userTimelineQueue
                    = (Queue<NormalTimelineFile>) field.get(manager);
            userTimelineQueue.clear();
            // Disable autoupdate of user timeline
            AbstractNonStreamTimelineFile.setAutoUpdateEnabled(false);
            
            Twitter mockTwitter = mock(Twitter.class);
            User mockUser = mock(User.class);
            @SuppressWarnings("unchecked")
                    
                    // Mock ResponseList which has zero Status.
                    ResponseList<Status> mockStatuses = new DummyResponseList<>();
            
            when(mockTwitter.getUserTimeline(
                    (Paging) anyObject())).thenReturn(mockStatuses);
            when(mockTwitter.getUserTimeline(
                    anyString(), (Paging) anyObject())).thenReturn(mockStatuses);
            
            // Mocked Twitter
            PowerMockito.mockStatic(TwitterFactoryAdapter.class);
            when(TwitterFactoryAdapter.getInstance(anyString())).
                    thenReturn(mockTwitter);
            when(TwitterFactoryAdapter.lookupUser(anyString(), anyString())).
                    thenReturn(mockUser);
            PowerMockito.doNothing().when(TwitterFactoryAdapter.class);
            TwitterFactoryAdapter.setUserStreamListener(anyString(),
                    (UserStreamListener) anyObject());
        } catch (NoSuchFieldException | SecurityException | 
                IllegalArgumentException | IllegalAccessException | 
                TwitterException ex)
        {
            fail("setUp filed." +  ex.getMessage());
        }
    }

    public static RateLimitStatus getRateLimitStatus ()
    {
        RateLimitStatus mockRateLimitStatus = mock(RateLimitStatus.class);
        when(mockRateLimitStatus.getLimit()).thenReturn(0);
        when(mockRateLimitStatus.getRemaining()).thenReturn(0);
        when(mockRateLimitStatus.getSecondsUntilReset()).thenReturn(0);
        return mockRateLimitStatus;
    }

    public static Status createStatus (long id)
    {
        Status status = mock(Status.class);
        when(status.getId()).thenReturn(id);

        User user = mock(User.class);
        when(user.getName()).thenReturn("");
        when(user.getScreenName()).thenReturn("");

        when(status.getUser()).thenReturn(user);
        when(status.getText()).thenReturn("");
        when(status.getCreatedAt()).thenReturn(new Date());

        return status;
    }

    public class DummyResponseList<T> extends ArrayList<T>
            implements ResponseList<T>
    {

        @Override
        public RateLimitStatus getRateLimitStatus ()
        {
            return TwitterFsTestBase.getRateLimitStatus();
        }

        @Override
        public int getAccessLevel ()
        {
            return 0;
        }
    }
}
