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

import iumfs.File;
import java.util.Date;
import java.util.logging.Logger;

abstract public class TwitterfsFile extends File {
    protected boolean is_timeline = false;
    protected static final Logger logger = Logger.getLogger(Main.class.getName());
    protected Account account;
    private boolean is_dir = false;

    TwitterfsFile(Account account, String name){
        super(name);
        this.account = account;
        Date now = new Date();
        setAtime(now.getTime());
        setCtime(now.getTime());
        setMtime(now.getTime());          
    }

    public String getPathname() {
        return "/" + getName();
    }

    public boolean isTimeline() {
        return is_timeline;
    }

    /*
     *Return file type
     * If direcory, return VDIR, othewise VREG.(reqular file)
     */
    @Override
    public long getFileType() {
        if (isDir()) {
            return File.VDIR;
        } else {
            return File.VREG;
        }
    }
    
    protected String getUsername(){
        return account.getUsername();
    }
    
    @Override
    public boolean isDir(){
        return is_dir;
    }
    
    public void setDir(boolean is_dir){
        this.is_dir = is_dir;
    }
    
    
}