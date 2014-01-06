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
package com.cafeform.iumfs.twitterfs.files;

import com.cafeform.iumfs.AbstractIumfsFile;
import com.cafeform.iumfs.FileType;
import static com.cafeform.iumfs.FileType.*;
import com.cafeform.iumfs.NotSupportedException;
import com.cafeform.iumfs.twitterfs.Account;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.logging.Level.*;

abstract public class TwitterFsFileImpl extends AbstractIumfsFile 
implements TwitterFsFile
{
    protected boolean isTimeline = false;
    protected static final Logger logger = 
            Logger.getLogger(TwitterFsFileImpl.class.getName());
    protected Account account;

    @Override
    public Account getAccount()
    {
        return account;
    }

    @Override
    public void setAccount(Account account)
    {
        this.account = account;
    }

    public TwitterFsFileImpl(Account account, String pathname)
    {
        super(pathname);
        this.account = account;
        Date now = new Date();
        setAtime(now.getTime());
        setCtime(now.getTime());
        setMtime(now.getTime());          
    }

    @Override
    public boolean isTimeline() 
    {
        return isTimeline;
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
    
    @Override
    public String getUsername()
    {
        return account.getUsername();
    }
    
    @Override
    public boolean isDirectory()
    {
        return directory;
    }
    
    @Override
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
    
    protected void log(Level level, String msg, Throwable thrown)
    {
        logger.log(
                level, 
                getAccount().getUsername() + ":" + getName() + " " + msg, 
                thrown);
    }
    
    protected void log(Level level, String msg)
    {
        logger.log(
                level, 
                getAccount().getUsername() + ":" + getName() + " " + msg);
    }
}
