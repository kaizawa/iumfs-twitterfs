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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Threads that update time lines periodically in background.
 * 
 */
public class AutoUpdateThread extends Thread {

    protected static Logger logger = Logger.getLogger(twitterfsd.class.getName());

    public void run() {
        while (true) {
            try {
                Thread.sleep(12000);
            } catch (InterruptedException ex) {
                Logger.getLogger(AutoUpdateThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            logger.fine("AutoUpdateThread wake") ;
            for (File file : twitterfsd.fileMap.values()) {
                if (file.isTimeline() == false) {
                    continue;
                }
                file.getTimeline();
                logger.fine("Got " + file.getName() + " timeline");
            }
        }
    }
}
