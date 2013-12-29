/*
 * Copyright 2011 Kazuyoshi Aizawa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this IumfsFile except in compliance with the License.
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
package iumfs.twitterfs;

import iumfs.IumfsFile;
import iumfs.NotADirectoryException;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author ka78231
 */
public class Account {

    @Deprecated    
    private Map<String, IumfsFile> fileMap;
    private String username;
    private static Map<String, Account> accountMap = new ConcurrentHashMap<String, Account>();
    private IumfsFile rootDir;
    
    Account(String username) {
        this.username = username;
    }

    @Deprecated
    public void setFileMap(Map<String, IumfsFile> IumfsFileMap) 
    {
        this.fileMap = IumfsFileMap;
    }
    @Deprecated
    public Map<String, IumfsFile> getFileMap() 
    {
        return fileMap;
    }

    /**
     * @return the user
     */
    public String getUsername() 
    {
        return username;
    }

    /**
     * @param user the user to set
     */
    public void setUsername(String user) 
    {
        this.username = user;
    }

    /**
     * @return the accountMap
     */
    public static Map<String, Account> getAccountMap() 
    {
        return accountMap;
    }
    
    public void setRootDirectory (IumfsFile rootDir)
    {
        if (!rootDir.isDirectory())
        {
            throw new NotADirectoryException();
        }
        this.rootDir = rootDir;
    }
    
    public IumfsFile getRootDirector ()
    {
        return rootDir;
    }
}
