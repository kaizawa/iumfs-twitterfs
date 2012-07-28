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
package iumfs.twitterfs;

import iumfs.ControlDevicePollingThread;
import iumfs.IumfsFile;
import iumfs.Request;

/** 
 * Worker Thread for TwitterFS
 */
public class TwitterfsDaemonThread extends ControlDevicePollingThread {
//    protected static Logger logger = Logger.getLogger(Main.class.getName());
    
    public TwitterfsDaemonThread(String name){
        super(name);
    }
    
    @Override
    protected void setFile(Request req){
        IumfsFile file = TwitterfsFile.getFile(req.getUserName(), req.getPathname());
        req.setFile(file);    
    }
}            
