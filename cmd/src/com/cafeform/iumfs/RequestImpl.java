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
package com.cafeform.iumfs;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Concrete class of Request inteface which represents request from iumfs
 * control device driver.
 * Request would be READ/READDIR/GETATTR/MKDIR/RMDIR/DELETE/CREATE.
 */
public class RequestImpl implements Request 
{
    final public static int MAX_USER_LEN = 40; // Must be multiple of 8
    final public static int MAX_PASS_LEN = 40; // Must be multiple of 8
    final public static int MAX_SERVER_LEN = 80; // Must be multiple of 8
    final public static int IUMFS_MAXPATHLEN = 1024; // Must be multiple of 8
    final public static int DEVICE_BUFFER_SIZE = 1024 * 1024; // buffer size of device
    final public static int MAX_RESPONSE_SIZE = DEVICE_BUFFER_SIZE;
    final public static int MAX_REQUEST_SIZE = DEVICE_BUFFER_SIZE;    
    final public static int REQUEST_HEADER_SIZE = 2248; // from iumfs.h

    private RequestType requestType;
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
    private long dataSize;    
    IumfsFile file;

    
    @Override
    public long getFlags() 
    {
        return flags;
    }

    @Override
    public void setFlags(long flag)
    {
        this.flags = flag;
    }

    @Override
    public byte[] getData() 
    {
        return data;
    }

    @Override
    public void setData(byte[] data) 
    {
        this.data = data;
    }

    @Override
    public String getBasepath() {
        return basepath;
    }

    @Override
    public void setBasepath(String basepath) {
        this.basepath = basepath;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public void setOffset(long offset) {
        this.offset = offset;
    }

    /**
     *
     * @return
     */
    @Override
    public String getPathname() {
        return pathname;
    }

    @Override
    public void setPathname(String pathname) 
    {
        this.pathname = pathname;
    }

    @Override
    public RequestType getType() {
        return requestType;
    }

    @Override
    public void setType(RequestType requestType) {
        this.requestType = requestType;
    }

    @Override
    public String getServer() {
        return server;
    }

    @Override
    public void setServer(String server) {
        this.server = server;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String getFullPath() {
        if (fullPath == null) {
            fullPath = getBasepath() + getPathname();
        }
        return fullPath;
    }

    /**
     * Return array of data start from given offset.
     *
     * @param  from start offset of data array.
     * @param  to end offset of data array.
     * @return buffer new array start from given position.
     */ 
    @Override
    public byte[] getData(long from, long to) 
    {
        return Arrays.copyOfRange(data, (int)from, (int)to);
    }

    @Override
    public void setFile(IumfsFile file){
        this.file = file;
    }
    
    @Override
    public IumfsFile getFile()
    {
        return file;
    };

    /**
     * @return the user
     */
    @Override
    public String getUserName() 
    {
        return username;
    }

    /**
     * @param username
     */
    @Override
    public void setUserName(String username) {
        this.username = username;
    }

    /**
     * @return the pass
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     */
    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    /** 
     * Get data size after header data.
     * This includes lengh of padding to make total size multiple of 8.
     * So, this infomation is not usefull for daemon. 
     * Use getSize() instead.
     */
    @Override
    public long getDataSize()
    {
        return dataSize;
    }

    @Override
    public void setDataSize(long dataSize)
    {
        this.dataSize = dataSize;
    }
}
