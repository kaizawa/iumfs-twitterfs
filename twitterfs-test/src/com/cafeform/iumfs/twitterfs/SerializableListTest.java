package com.cafeform.iumfs.twitterfs;

import java.io.IOException;
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
            List<Integer> list = new SerializableList<>(fileName, false);

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
            List<Integer> list = new SerializableList<>(fileName, false);
            for (int i = 0; i < 100; i++)
            {
                list.add(i);
            }
            
            list = new SerializableList<>(fileName, true);
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
            List<Integer> list = new SerializableList<>(fileName, false);

            for (int i = 0 ; i < 100; i++)
            {
                list.add(i);
            }
            assertEquals(100, list.size());
            
            for (int i = 0 ; i < 50 ; i++)
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
}
