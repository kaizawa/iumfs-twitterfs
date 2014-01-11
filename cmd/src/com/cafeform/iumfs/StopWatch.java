package com.cafeform.iumfs;

import java.util.Date;

public class StopWatch 
{
    private long startTime = 0;
    private long stopTime = 0;

    public void start ()
    {
        if (0 != startTime && 0 == stopTime)
        {
            throw new IllegalStateException("Already started. Stop first");
        }
        reset();
        startTime = new Date().getTime();
    }
    
    public StopWatch stop ()
    {
        stopTime = new Date().getTime();
        return this;
    }
    
    public void reset ()
    {
        startTime = 0;
        stopTime = 0;
    }
    
    @Override
    public String toString ()
    {
        if (0 == startTime)
        {
            throw new IllegalStateException("Not started");
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
