package com.cafeform.iumfs.utilities;

import java.util.Date;

public class StopWatch 
{
    private long startTime = 0;
    private long stopTime = 0;

    synchronized public void start ()
    {
        reset();
        startTime = new Date().getTime();
    }
    
    synchronized public StopWatch stop ()
    {
        if (0 == startTime)
        {
            throw new IllegalStateException("Not started.");
        }
        stopTime = new Date().getTime();
        return this;
    }
    
    synchronized public void reset ()
    {
        startTime = 0;
        stopTime = 0;
    }
    
    @Override
    synchronized public String toString ()
    {
        if (0 == startTime)
        {
            throw new IllegalStateException("Not started.");
        }
        long difference;
        if (0 == stopTime)
        {
            difference = new Date().getTime() - startTime;
        }
        else 
        {
            difference = stopTime - startTime;
        }
        return (difference + " ms.");
    }
}
