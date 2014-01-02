/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cafeform.iumfs.twitterfs;

import com.cafeform.iumfs.IumfsFile;
import com.cafeform.iumfs.twitterfs.files.UserTimelineFile;
import com.cafeform.iumfs.twitterfs.files.UserTimelineFileImpl;
import java.lang.reflect.Field;
import java.util.concurrent.BlockingQueue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest(UserTimelineFileManagerImpl.class) 
@RunWith(PowerMockRunner.class)
public class UserTimelineFileManagerImplTest extends TwitterFsTestBase
{
    /**
     * Test of getTimelineFile method, of class UserTimelineFileManagerImpl.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetTimelineFile () throws Exception
    {
        System.out.println("getTimelineFile");
        String fileName = "hogehoge";
        String pathName = "/follwowers/" + fileName;
        
        // Create mock UserTimelineFile
        UserTimelineFileImpl mockTimelineFile = mock(UserTimelineFileImpl.class);
        when(mockTimelineFile.getName()).thenReturn(fileName);
        when(mockTimelineFile.getPath()).thenReturn(pathName);
        
        // Mocked UserTimelineFile will be returned.
        whenNew(UserTimelineFileImpl.class).withAnyArguments().thenReturn(mockTimelineFile);
        account = new AccountImpl(USER1);        
        
        UserTimelineFileManager instance = account.getUserTimelineManager();
        IumfsFile result = instance.getTimelineFile(account, pathName);
        assertEquals(fileName, result.getName());
        assertEquals(pathName, result.getPath());        
    }
    
    /**
     * Test of getTimelineFile method, of class UserTimelineFileManagerImpl.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetExistingTimelineFile () throws Exception
    {
        System.out.println("getTimelineFile");
        String fileName1 = "file1";
        String fileName2 = "file2";        
        String fileNameAny = "fileAny";                
        String pathName1 = "/follwowers/" + fileName1;
        String pathName2 = "/follwowers/" + fileName2;        
        String pathNameAny = "/follwowers/" + fileNameAny;                
        
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
        
        UserTimelineFileManager instance = account.getUserTimelineManager();
        // Firs time lookup
        instance.getTimelineFile(account, pathName1);
        instance.getTimelineFile(account, pathName2);  
        
        Field field = instance.getClass().getDeclaredField("userTimelineQueue");
        field.setAccessible(true);
        BlockingQueue<UserTimelineFile> userTimelineQueue = 
                (BlockingQueue<UserTimelineFile>)field.get(instance);

        // 2nd time lookup
        IumfsFile result1 = instance.getTimelineFile(account, pathName1); 
        IumfsFile result2 = instance.getTimelineFile(account, pathName2); 

        assertEquals(fileName1, result1.getName());
        assertEquals(fileName2, result2.getName());  
        
        // Only 2 UserTimelineFile should be found.
        // means, 2nd time lookup shouldn'nt add new another timeline file.
        assertEquals(2, userTimelineQueue.size());
    }
}