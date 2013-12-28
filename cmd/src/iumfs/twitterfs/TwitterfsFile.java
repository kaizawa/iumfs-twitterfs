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
package iumfs.twitterfs;

import iumfs.AbstractIumfsFile;
import iumfs.FileType;
import static iumfs.FileType.*;
import iumfs.IumfsFile;
import iumfs.NotSupportedException;
import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

abstract public class TwitterfsFile extends AbstractIumfsFile {
    protected boolean is_timeline = false;
    protected static final Logger logger = Logger.getLogger(TwitterfsFile.class.getName());
    protected Account account;

    TwitterfsFile(Account account, String name)
    {
        super(name);
        this.account = account;
        Date now = new Date();
        setAtime(now.getTime());
        setCtime(now.getTime());
        setMtime(now.getTime());          
    }

    public boolean isTimeline() 
    {
        return is_timeline;
    }
    
    /*
     *Return file type
     * If direcory, return VDIR, othewise VREG.(reqular file)
     */
    @Override
    public FileType getFileType() 
    {
        if (isDirectory()) 
        {
            return VDIR;
        } else {
            return VREG;
        }
    }
    
    protected String getUsername()
    {
        return account.getUsername();
    }
    
    @Override
    public boolean isDirectory()
    {
        return directory;
    }
    
    public void setDirectory(boolean directory) 
    {
        this.directory = directory;
    }
    
    @Override
    public void create()
    {
        throw new NotSupportedException();
    }   
    
    @Override
    public boolean mkdir()
    {
        throw new NotSupportedException();        
    }
    
    /*
     * This method is overridden by DirectoryFile.java
     */
    @Override
    public IumfsFile[] listFiles()
    {
        throw new NotSupportedException();                
    }
    
    static public Map<String, IumfsFile> getFileMap (String username) 
    {
        return Account.getAccountMap().get(username).getFileMap();
    }    
    
    @Override
    public boolean delete ()
    {
        throw new NotSupportedException();
    }   
    
    /*
     * If TwitterFsFile instance is returned, the file is always exists.
     */
    @Override
    public boolean exists() 
    {
        return true;
    }
}
