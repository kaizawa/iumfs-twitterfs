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
package iumfs;

import java.util.*;
import java.util.logging.Logger;


/** 
 * User mode daemon for HDFS
 *
 *  * 
 * デバッグ出力する場合
 * -Djava.util.logging.config.file=log.prop
 *
 */
public class twitterfsd {
    static final String version = "0.1.0";  // version
    private static Logger logger = Logger.getLogger(twitterfsd.class.getName());
    private static final int maxThreads = 4;
    static List<File> entries = new ArrayList<File>();

    public static void main(String args[]) {
        twitterfsd fsd = new twitterfsd();
        fsd.init();
        fsd.startDaemonThreads();
    }

    public void init(){
        entries.add(new File("post"));
        entries.add(new File("home"));
    }

    public void startDaemonThreads() {
        for(int i = 0 ; i < maxThreads ; i++){
            new DaemonThread().start();
        }
    }
}            
