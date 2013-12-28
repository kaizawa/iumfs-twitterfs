package iumfs;

/**
 * Factory for File for iumfs
 */
public interface FileFactory {
    public IumfsFile createFile(Request request);
}
