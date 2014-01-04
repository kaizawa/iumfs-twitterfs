/*
 * Copyright 2010 Kazuyoshi Aizawa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cafeform.iumfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface IumfsFile 
{
    public void setLength(long length);
    public long length();
    /**
     * Read requested data from requested offset/size and copy to specified buffer.
     * 
     * @param buf
     * @param  size of data
     * @param  offset of date to be read
     * @return read byte
     * @throws java.io.FileNotFoundException
     */
    public long read(ByteBuffer buf, long size, long offset)
            throws FileNotFoundException, IOException, NotSupportedException;   
    /**
     * Write data to requested offset/size.
     * 
     * @param  buf
     * @param  size to be written
     * @param  offset to be written
     * @return 0
     * @throws java.io.FileNotFoundException
     */
    public long write(byte[] buf, long size, long offset)
            throws NotSupportedException, FileNotFoundException, IOException;       
    /**
     * 
     * @return 
     */
    public boolean delete();
    /**
     * Create new directory
     * 
     * @return
     * @throws FileExistException 
     */
    public boolean mkdir()  throws FileExistException;
    public IumfsFile[] listFiles();
    /**
     * @return the atime
     */
    public long getAtime();
    /**
     * @param atime the atime to set
     */
    public void setAtime(long atime);
    /**
     * @return the ctime
     */
    public long getCtime();
    /**
     * @param ctime the ctime to set
     */
    public void setCtime(long ctime);
    /**
     * @return the mtime
     */
    public long getMtime();
    /**
     * @param mtime the mtime to set
     */
    public void setMtime(long mtime);
    /**
     * Get filetype like VDIR, VREG
     * @return filetype
     */
     public FileType getFileType();
    /**
     * Get prmission of this file
     * @return permission
     */
    public long getPermission();
    /**
     * Check if this file represents directory.
     * @return 
     */
    public boolean isDirectory();
    public void create() throws IOException;
    public boolean exists() throws SecurityException;
    public boolean isFile();
    public String getName();
    public String getPath ();
    /**
     * Add file to file list if it's directory file.
     * @param file
     * @Exception NotADirectoryException
     */
    public void addFile (IumfsFile file) throws NotADirectoryException;
}
