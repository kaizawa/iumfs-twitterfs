/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cafeform.iumfs.twitterfs;

import com.cafeform.iumfs.twitterfs.TwitterFsFileFactory;
import com.cafeform.iumfs.twitterfs.Prefs;
import com.cafeform.iumfs.twitterfs.Account;
import com.cafeform.iumfs.IumfsFile;
import com.cafeform.iumfs.twitterfs.files.AbstractNonStreamTimelineFile;
import com.cafeform.iumfs.twitterfs.files.TwitterFsDirectory;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author kaizawa
 */
public class TwitterFsFileFactoryTest extends TwitterFsTestBase
{
    @Before
    public void setUp()
    {
        AbstractNonStreamTimelineFile.setAutoUpdateEnabled(false);
        fileFactory = new TwitterFsFileFactory();
        AccountMap.remove(USER1);
    }
    
    @After
    public void tearDown()
    {
        Prefs.put(USER1 + "/accessToken", "");
    }
    
    @Test
    public void testTopLevelDirectoryWithoutToken ()
    {
        IumfsFile rootDir = fileFactory.getFile(USER1, "/");
        String[] expectedFiles = {"setup"};
        
        assertTrue(rootDir.isDirectory());
        IumfsFile[] fileList = rootDir.listFiles();
        // Must have 1 entries
        assertEquals(1, fileList.length);
        
        List<String> foundFileList = new ArrayList ();
        for(IumfsFile foundFile : fileList)
        {
               foundFileList.add(foundFile.getName());
        }                
        
        for (String expectedFile : expectedFiles)
        {
            assertTrue(expectedFile + " is not included.", 
            foundFileList.contains(expectedFile));
        }
    }
    
    @Test
    public void testTopLevelDirectoryWithToken ()
    {
        Prefs.put(USER1 + "/accessToken", DUMMY_TOKEN);        
        IumfsFile rootDir = fileFactory.getFile(USER1, "/");
        String[] expectedFiles = {"mentions", "home", "user", "friends", 
            "followers", "post", "retweets_of_me"};
        
        
        assertTrue(rootDir.isDirectory());
        IumfsFile[] fileList = rootDir.listFiles();
        // Must have 7 entries
        assertEquals(7, fileList.length);
        
        List<String> foundFileList = new ArrayList ();
        for(IumfsFile foundFile : fileList)
        {
               foundFileList.add(foundFile.getName());
        }                
        
        for (String expectedFile : expectedFiles)
        {
            assertTrue(expectedFile + " is not included.", 
            foundFileList.contains(expectedFile));
        }
    }
    
    @Test
    public void testLookupFile ()
    {
        Prefs.put(USER1 + "/accessToken", DUMMY_TOKEN);        
        IumfsFile home = fileFactory.getFile(USER1, "/home");
        
        assertFalse(home.isDirectory());
        assertEquals("home", home.getName());
    }
    
    @Test
    public void testLookupFileUnderDirectory ()
    {
        Prefs.put(USER1 + "/accessToken", DUMMY_TOKEN);          
        IumfsFile rootDir = fileFactory.getFile(USER1, "/");
        account = AccountMap.get(USER1);
        
        TwitterFsDirectory lv1Dir = new TwitterFsDirectory(account, "lv1Dir");
        lv1Dir.addFile(new TwitterFsDirectory(account, "lv2Dir"));
        rootDir.addFile(lv1Dir);
        
        IumfsFile lv2Dir = fileFactory.getFile(USER1, "/lv1Dir/lv2Dir");    
        assertNotNull(lv2Dir);
    }
    
    @Test
    public void testLookupRootDirectory ()
    {
        Prefs.put(USER1 + "/accessToken", DUMMY_TOKEN);        
        IumfsFile rootDir = fileFactory.getFile(USER1, "/");
        
        assertTrue(rootDir.isDirectory());
        assertEquals("/", rootDir.getPath());
        assertEquals("", rootDir.getName());        
    }
    
    @Test
    public void testLookupReplyPostFile ()
    {
        Prefs.put(USER1 + "/accessToken", DUMMY_TOKEN);        
        IumfsFile postFile = fileFactory.getFile(USER1, "/@twitter");
        assertNotNull(postFile);
        
        // File which start with @ always exist anyware under root dir.
        postFile = fileFactory.getFile(USER1, "/hehehe/@twitter");   
        assertNotNull(postFile);        
    }
}
