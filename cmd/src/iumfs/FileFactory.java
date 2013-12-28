package iumfs;

import iumfs.Request;
import java.io.File;

/**
 * Factory for File for iumfs
 */
public interface FileFactory {
    public IumfsFile createFile(Request request);
}
