/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package iumfs.twitterfs;

import com.cafeform.iumfs.twitterfs.TwitterFsFileFactory;
import com.cafeform.iumfs.twitterfs.Prefs;
import com.cafeform.iumfs.twitterfs.Account;
import com.cafeform.iumfs.IumfsFile;
import com.cafeform.iumfs.twitterfs.files.TimelineFile;
import com.cafeform.iumfs.twitterfs.files.TwitterFsDirectory;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author kaizawa
 */
public class TwitterFsFileFactoryTest
{
    private TwitterFsFileFactory fileFactory;
    private static Map<String, Account> accountMap;
    private static final String USER = "user1";
    private static final String TOKEN = "dummyToken";
    
    @Before
    public void setUp()
    {
        TimelineFile.setAutoupdateEnabled(false);
        fileFactory = new TwitterFsFileFactory();
        accountMap = Account.getAccountMap();
        accountMap.remove(USER);
    }
    
    @After
    public void tearDown()
    {
        Prefs.put(USER + "/accessToken", "");
    }
    
    @Test
    public void testTopLevelDirectoryWithoutToken ()
    {
        IumfsFile rootDir = fileFactory.getFile(USER, "/");
        
        assertTrue(rootDir.isDirectory());
        IumfsFile[] fileList = rootDir.listFiles();
        // Must have 2 entries (setup and "")
        assertEquals(2, fileList.length);
    }
    
    @Test
    public void testTopLevelDirectoryWithToken ()
    {
        Prefs.put(USER + "/accessToken", TOKEN);        
        IumfsFile rootDir = fileFactory.getFile(USER, "/");
        
        assertTrue(rootDir.isDirectory());
        IumfsFile[] fileList = rootDir.listFiles();
        // Must have 7 entries
        assertEquals(8, fileList.length);
    }
    
    @Test
    public void testLookupFile ()
    {
        Prefs.put(USER + "/accessToken", TOKEN);        
        IumfsFile home = fileFactory.getFile(USER, "/home");
        
        assertFalse(home.isDirectory());
        assertEquals("home", home.getName());
    }
    
    @Test
    public void testLookupFileUnderDirectory ()
    {
        Prefs.put(USER + "/accessToken", TOKEN);          
        IumfsFile rootDir = fileFactory.getFile(USER, "/");
        Account account = accountMap.get(USER);
        
        TwitterFsDirectory lv1Dir = new TwitterFsDirectory(account, "lv1Dir");
        lv1Dir.addFile(new TwitterFsDirectory(account, "lv2Dir"));
        rootDir.addFile(lv1Dir);
        
        IumfsFile lv2Dir = fileFactory.getFile(USER, "/lv1Dir/lv2Dir");    
        assertNotNull(lv2Dir);
    }
}
