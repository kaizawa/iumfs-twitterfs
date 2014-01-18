/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cafeform.iumfs.twitterfs;

import com.cafeform.iumfs.twitterfs.files.FollowersDirectory;
import com.cafeform.iumfs.IumfsFile;
import com.cafeform.iumfs.twitterfs.files.FriendsDirectory;
import com.cafeform.iumfs.twitterfs.files.NormalTimelineFile;
import com.cafeform.iumfs.twitterfs.files.UserTimelineFileImpl;
import java.lang.reflect.Field;
import java.util.Queue;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

public class UserTimelineFileManagerImplTest extends TwitterFsTestBase
{
    /**
     * Test of followers directory
     * @throws java.lang.Exception
     */
    @Test
    public void testGetTimelineFile () throws Exception
    {
        String fileName = "hogehoge";
        String pathName = FollowersDirectory.PATH_NAME + "/" + fileName;
        account = new AccountImpl(USER1);        

        UserTimelineFileManager instance = UserTimelineManagerFactory.getInstance();
        IumfsFile result = instance.getTimelineFile(account, pathName);
        assertEquals(fileName, result.getName());
        assertEquals(pathName, result.getPath());        
    }
    
    /**
     * Test of friends directory
     * @throws java.lang.Exception
     */
    @Test
    public void testGetFriendsTimelineFile () throws Exception
    {
        String fileName = "hogehoge";
        String pathName = FriendsDirectory.PATH_NAME + "/" + fileName;
        
        // Mocked UserTimelineFile will be returned.
        account = new AccountImpl(USER1);        
        
        UserTimelineFileManager manager = UserTimelineManagerFactory.getInstance();
        IumfsFile result = manager.getTimelineFile(account, pathName);
        assertEquals(fileName, result.getName());
        assertEquals(pathName, result.getPath());        
    }
    
    /**
     * Test of getting UserTimeLineFile
     * @throws java.lang.Exception
     */
    @Test
    public void testGetUserTimelineFile () throws Exception
    {
        String fileName1 = "file1";
        String fileName2 = "file2";        
        String fileNameAny = "fileAny";                
        String pathName1 = FollowersDirectory.PATH_NAME + "/" + fileName1;
        String pathName2 = FollowersDirectory.PATH_NAME + "/" + fileName2;        
        String pathNameAny = FollowersDirectory.PATH_NAME + "/" + fileNameAny;                
        
        // Create 1st mock UserTimelineFile
        UserTimelineFileImpl mockFile1 = mock(UserTimelineFileImpl.class);
        when(mockFile1.getName()).thenReturn(fileName1);
        when(mockFile1.getPath()).thenReturn(pathName1);
        Mockito.doNothing().when(mockFile1).getTimeline();
        
        // Create 2nd mock UserTimelineFile
        UserTimelineFileImpl mockFile2 = mock(UserTimelineFileImpl.class);
        when(mockFile2.getName()).thenReturn(fileName2);
        when(mockFile2.getPath()).thenReturn(pathName2);
        Mockito.doNothing().when(mockFile2).getTimeline();    
        
        // Anyother mock UserTimelineFile
        UserTimelineFileImpl mockFileAny = mock(UserTimelineFileImpl.class);
        when(mockFileAny.getName()).thenReturn(fileNameAny);
        when(mockFileAny.getPath()).thenReturn(pathNameAny);
        Mockito.doNothing().when(mockFileAny).getTimeline();  
        
        // Mocked UserTimelineFile will be returned.
        whenNew(UserTimelineFileImpl.class).
                withParameterTypes(Account.class, String.class).
                withArguments(anyObject(), eq(pathName1)).
                thenReturn(mockFile1);
        
        // Mocked UserTimelineFile will be returned.
        whenNew(UserTimelineFileImpl.class).
                withParameterTypes(Account.class, String.class).                
                withArguments(anyObject(), eq(pathName2)).
                thenReturn(mockFile2);
        
        account = new AccountImpl(USER1);        
        
        UserTimelineFileManager manager = UserTimelineManagerFactory.getInstance();

        // Get userTimelineQuee from UserTimelineFileManager
        Field field = manager.getClass().getDeclaredField("userTimelineQueue");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Queue<NormalTimelineFile> userTimelineQueue = 
                (Queue<NormalTimelineFile>)field.get(manager);

        // lookup
        IumfsFile result1 = manager.getTimelineFile(account, pathName1); 
        IumfsFile result2 = manager.getTimelineFile(account, pathName2); 

        assertEquals(fileName1, result1.getName());
        assertEquals(fileName2, result2.getName());  
        
        // Only 2 UserTimelineFile should be found.
        // means, 2nd time lookup shouldn't add new another timeline file.
        assertEquals(2, userTimelineQueue.size());
    }
    
    /**
     * Verifiy is 
     * @throws java.lang.Exception
     */
    @Test
    public void testGetExistingUserTimelineFile () throws Exception
    {
        String fileName1 = "file1";
        String fileName2 = "file2";        
        String fileNameAny = "fileAny";                
        String pathName1 = FollowersDirectory.PATH_NAME + "/" + fileName1;
        String pathName2 = FollowersDirectory.PATH_NAME + "/" + fileName2;        
        String pathNameAny = FollowersDirectory.PATH_NAME + "/" + fileNameAny;                
        
        account = new AccountImpl(USER1);        
        
        UserTimelineFileManager manager = UserTimelineManagerFactory.getInstance();
        // Firs time lookup
        manager.getTimelineFile(account, pathName1);
        manager.getTimelineFile(account, pathName2);  
        
        Field field = manager.getClass().getDeclaredField("userTimelineQueue");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Queue<NormalTimelineFile> userTimelineQueue = 
                (Queue<NormalTimelineFile>)field.get(manager);

        // 2nd time lookup
        IumfsFile result1 = manager.getTimelineFile(account, pathName1); 
        IumfsFile result2 = manager.getTimelineFile(account, pathName2); 

        assertEquals(fileName1, result1.getName());
        assertEquals(fileName2, result2.getName());  
        
        // Only 2 UserTimelineFile should be found.
        // means, 2nd time lookup shouldn't add new another timeline file.
        
        for(NormalTimelineFile file : userTimelineQueue)
        {
            System.out.println(file.getName() + " " + file.hashCode());
        }
        
        assertEquals(2, userTimelineQueue.size());
    }
}
