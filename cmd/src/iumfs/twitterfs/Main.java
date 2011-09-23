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

import java.util.logging.Logger;

/** 
 * User mode daemon for TwitterFS
 *
 * For debugging.
 * -Djava.util.logging.config.file=log.prop
 *
 */
public class Main {

    static final String version = "0.1.10";  // version
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String args[]) {
        /*
         * setup options. It prepares twitter OAuth key.
         */
        if (args.length == 2) {
            setup(args);
        }

        Main instance = new Main();
        instance.startDaemonThreads();
    }

    /**
     * This routine just setup Twitter OAuth access token.
     * Once it finiced setting up, exit the process.
     */
    protected static void setup(String[] args) {
        try {
            String username = args[1];
            if ("setup".equals(args[0]) && username.length() != 0) {
                IumfsTwitterFactory.getAccessToken(username);
                if (Prefs.get(username + "/accessToken").isEmpty()) {
                    System.out.println("Failed to setup Twitter access token");
                } else {
                    System.out.println("Twitter access token setup sucessfully");
                    System.exit(0);
                }
            } else {
                System.out.println("Usage: iumfs.twitterfs.Main setup <username>");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.exit(1);
    }

    public void startDaemonThreads() {

        for (int i = 0; i < Prefs.getInt("maxThreads"); i++) {
            new TwitterfsDaemonThread("TwitterfsDaemonThread").start();            
        }
        //new TwitterfsDaemonThread().start();        
    }
}
