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

/**
 * Request from controll device
 * Request would be READ/READDIR/GETATTR/MKDIR/RMDIR/DELETE/CREATE.
 */
public interface Request 
{
    public long getFlags();
    public void setFlags(long flag);
    public byte[] getData();
    public void setData(byte[] data);
    public String getBasepath();
    public void setBasepath(String basepath);
    public long getOffset();
    public void setOffset(long offset);
    public String getPathname();
    public void setPathname(String pathname);
    public RequestType getType();
    public void setType(RequestType type); 
    public String getServer(); 
    public void setServer(String server);
    public long getSize();
    public void setSize(long size);
    public String getFullPath();
    public byte[] getData(long from, long to);
    public void setFile(IumfsFile file);
    public IumfsFile getFile();
    public String getUserName();
    public void setUserName(String username);
    public String getPassword();
    public void setPassword(String password);
    public long getDataSize();
    public void setDataSize(long size);
}
