/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cafeform.iumfs.twitterfs;

import com.cafeform.iumfs.IumfsFile;
import com.cafeform.iumfs.twitterfs.files.AbstractNonStreamTimelineFile;
import com.cafeform.iumfs.twitterfs.files.RepliesDirectory;
import com.cafeform.iumfs.twitterfs.files.TwitterFsDirectory;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import twitter4j.TwitterException;

/**
 *
 * @author kaizawa
 */
public class TwitterFsFileFactoryTest extends TwitterFsTestBase
{
    @Before
    @Override
    public void setUp()
    {
        super.setUp();
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
        
        assertTrue(rootDir.isDirectory());
        IumfsFile[] fileList = rootDir.listFiles();
        // Must have 1 entries
        assertEquals(1, fileList.length);
        
        List<String> foundFileList = new ArrayList<> ();
        for(IumfsFile foundFile : fileList)
        {
               foundFileList.add(foundFile.getName());
        }                
        
        for (String expectedFile : setupExpectedFiles)
        {
            assertTrue(expectedFile + " is not included.", 
            foundFileList.contains(expectedFile));
        }
    }
    
    /**
     * Verify any file other than setup is not found without Token
     */
    @Test 
    public void testLookupFileWithoutToken ()
    {
        // Should exist
        assertNotNull("/setup not found." +  fileFactory.getFile(USER1, "/setup"));
        
        // Should not exist
        for (String dir : expectedDirectories){
            assertNull("/" + dir + " is found without token", 
                    fileFactory.getFile(USER1, dir));                    
            assertNull("/" + dir + " + /hoge is found without token" ,
                    fileFactory.getFile(USER1, "/" + dir + "/hoge"));                                
        }
        
        // Should not exist
        for (String file : expectedFiles){
            assertNull("/" + file + " is found without token", 
                    fileFactory.getFile(USER1, "/" + file));                    
        }
    }
    
    /**
     * Verify expected files can be found.
     */
    @Test 
    public void testLookupFileWithToken ()
    {
        Prefs.put(USER1 + "/accessToken", DUMMY_TOKEN);        

        // Should not exist
        assertNull("/setup found.", fileFactory.getFile(USER1, "/setup"));
        
        for (String dir : expectedDirectories){
            // Should exist
            assertNotNull("/" + dir + " is not found with token", 
                    fileFactory.getFile(USER1, dir));                    

            if (RepliesDirectory.PATH_NAME.equals("/" + dir))
            {
                // Should exist any file if it's under /replies direcotry.
                assertNotNull("/" + dir + "/hoge is not found with token" ,
                        fileFactory.getFile(USER1, "/" + dir + "/hoge"));                                
            }
            else 
            {
                // Should not exist any file under directory                
                assertNull("/" + dir + "/hoge is found with token" ,
                        fileFactory.getFile(USER1, "/" + dir + "/hoge"));                                                                        
            }
        }
        
        // Should exist
        for (String file : expectedFiles){
            assertNotNull("/" + file + " is not found with token", 
                    fileFactory.getFile(USER1, "/" + file));                    
        }
    }
    
    @Test
    public void testTopLevelDirectoryWithToken ()
    {
        Prefs.put(USER1 + "/accessToken", DUMMY_TOKEN);        
        IumfsFile rootDir = fileFactory.getFile(USER1, "/");
               
        assertTrue(rootDir.isDirectory());
        IumfsFile[] fileList = rootDir.listFiles();
        // Must have 8 entries
        assertEquals(expectedFiles.length + expectedDirectories.length, 
                fileList.length);
        
        List<String> foundFileList = new ArrayList<> ();
        for(IumfsFile foundFile : fileList)
        {
               foundFileList.add(foundFile.getName());
        }                
        
        for (String expectedFile : expectedFiles)
        {
            assertTrue(expectedFile + " is not included.", 
                    foundFileList.contains(expectedFile));
        }
        
        for (String expectedFile : expectedDirectories)
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
}
