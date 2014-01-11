package com.cafeform.iumfs.twitterfs;

import com.cafeform.iumfs.utilities.Util;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SerializableListTest
{

    public static final String fileName = "/var/tmp/iumfs-twitterfs/tesfile";

    @Test
    public void testAddElement () throws IOException
    {
        try
        {
            List<Integer> list = new DiskStoredArrayList<>(fileName, false);

            for (int i = 0; i < 100; i++)
            {
                list.add(i);
            }
            assertEquals(100, list.size());
        } catch (IllegalStateException ex)
        {
            fail(ex.getCause().toString());
        }
    }

    @Test
    public void testInstanceUseBackup () throws IOException
    {
        try
        {
            // First create file from scrach.
            List<Integer> list = new DiskStoredArrayList<>(
                    fileName, 
                    false,
                    100,
                    200);
            for (int i = 0; i < 100; i++)
            {
                list.add(i);
            }
            
            Util.sleep(500);

            list = new DiskStoredArrayList<>(fileName, true);
            
            assertEquals(100, list.size());

        } catch (IllegalStateException ex)
        {
            fail(ex.getCause().toString());
        }
    }

    @Test
    public void testDeleteElement () throws IOException
    {
        try
        {
            List<Integer> list = new DiskStoredArrayList<>(fileName, false);

            for (int i = 0; i < 100; i++)
            {
                list.add(i);
            }
            assertEquals(100, list.size());

            for (int i = 0; i < 50; i++)
            {
                list.remove(0);
            }
            // should be decreased to 50
            assertEquals(50, list.size());

            list.clear();
            // should be empty
            assertTrue(list.isEmpty());

        } catch (IllegalStateException ex)
        {
            fail(ex.getCause().toString());
        }
    }

    // This test takes long time..
    //@Test
    public void testLargeObject () throws IOException
    {
        int total = 0; 
        List<List<Byte[]>> grandList = new ArrayList<>();
        for (int i = 1; i < 5; i++)
        {
            List<Byte[]> list = new DiskStoredArrayList<>(
                    "/var/tmp/byteBufferList" + i,
                    false,
                    500,
                    1000
            );

            int mb = 1024 * 1024;
            for (int j = 1; j < 101; j++)
            {
                Byte bytes[] = new Byte[mb];
                list.add(bytes);
                }
            Util.sleep(500);

            total += 100;
            grandList.add(list);
            System.out.println ("Total " + total + " MB.");
        }
        
        for(List<Byte[]> list : grandList)
        {
            assertEquals(100, list.size());
        }
    }
}
