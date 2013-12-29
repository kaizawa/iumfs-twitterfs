/*
 * Copyright 2011 Kazuyoshi Aizawa
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
package com.cafeform.iumfs.handler;

import com.cafeform.iumfs.IumfsFile;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Abstruct class which represents various request from iumfs
 * control device driver.
 * Request would be READ/READDIR/GETATTR/MKDIR/RMDIR/DELETE/CREATE.
 */
public abstract class AbstractRequestHandler implements RequestHandler 
{
    // Status to return to control device
    final public static long SUCCESS = 0;
    final public static long ENOENT = 2;
    final public static long EIO = 5;
    final public static long EEXIST = 17;    
    final public static long ENOTSUP = 48;
    final public static long ENOTDIR = 20;
    final public static long EINVAL = 22;

    private long request_type;
    private long size;
    private long offset;
    private String pathname;
    private String server;
    private String basepath;
    private String fullPath;
    private String username;
    private String password;
    private byte[] data;
    private long flags;
    protected static final Logger logger = Logger.getLogger("iumfs");
    IumfsFile file;    

    /**
     * Return array of data start from given offset.
     *
     * @param  from start offset of data array.
     * @param  to end offset of data array.
     * @return buffer new array start from given position.
     */ 
    public byte[] getData(long from, long to) 
    {
        return Arrays.copyOfRange(data, (int)from, (int)to);
    }

    public void setFile(IumfsFile file)
    {
        this.file = file;
    }
    
    public IumfsFile getFile()
    {
        return file;
    };

    /**
     * @return the user
     */
    public String getUserName() 
    {
        return username;
    }

    /**
     * @param username
     */
    public void setUserName(String username) 
    {
        this.username = username;
    }

    /**
     * @return the pass
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     */
    public void setPassword(String password) 
    {
        this.password = password;
    }
}
