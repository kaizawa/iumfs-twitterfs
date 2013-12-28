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
package iumfs;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract public class AbstractIumfsFile implements IumfsFile
{
//    final public static int VREG = 1; // Normal File
//    final public static int VDIR = 2; // Directory
    protected long length = 0;
    private long atime; // Last access time (msec)
    private long ctime; // Modify time(msec)
    private long mtime; // Modify time(msec)
    protected boolean directory = false;
    String pathName;
    
    public AbstractIumfsFile(String pathName)
    {
        this.pathName = pathName;
        init();
    }

    private void init() 
    {
        Date now = new Date();
        setAtime(now.getTime());
        setCtime(now.getTime());
        setMtime(now.getTime());
    }

    @Override
    public void setLength(long length)
    {
        this.length = length;
    }
    
    @Override
    public long length() 
    {
        return length;
    }    

    /**
     * @return the atime
     */
    @Override
    public long getAtime() {
        return atime;
    }

    /**
     * @param atime the atime to set
     */
    @Override
    public void setAtime(long atime) {
        this.atime = atime;
    }

    /**
     * @return the ctime
     */
    @Override
    public long getCtime() {
        return ctime;
    }

    /**
     * @param ctime the ctime to set
     */
    @Override
    public void setCtime(long ctime) {
        this.ctime = ctime;
    }

    /**
     * @return the mtime
     */
    @Override
    public long getMtime() {
        return mtime;
    }

    /**
     * @param mtime the mtime to set
     */
    @Override
    public void setMtime(long mtime) {
        this.mtime = mtime;
    }
    
    @Override
    public boolean isFile() {
        return ! isDirectory();
    }   
    
    public String getPath () 
    {
        return pathName;
    }
    
    public String getName ()
    {
        String regex = "((.*)/)*(.*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(pathName);
        
        return matcher.find() ? matcher.group(3) : null;
    }
}
