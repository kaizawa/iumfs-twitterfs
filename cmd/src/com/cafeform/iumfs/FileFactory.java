package com.cafeform.iumfs;

/**
 * Factory for File for iumfs
 */
public interface FileFactory 
{
    public IumfsFile getFile(Request request);
}
