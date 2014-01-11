package com.cafeform.iumfs.utilities;

/**
 * Utility methods
 * @author kaizawa
 */
public class Util 
{
    public static void sleep(long msec) 
    {
        try {
            Thread.sleep(msec);
        } catch (InterruptedException ex) {}
    }
}
