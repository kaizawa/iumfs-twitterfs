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

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * factory class for Twitter
 */
public class IumfsTwitterFactory {

    private static Logger logger = Logger.getLogger(Main.class.getName());
    
    static TwitterFactory factory = new TwitterFactory();

    /**
     * Return instance of Twitter class which has AccessToken been set.
     * @return twitter instance of Twitter class
     */
    public static Twitter getInstance() {        
        Twitter twitter = factory.getInstance();
        twitter.setOAuthConsumer(Prefs.get("OAuthConsumerKey"), Prefs.get("consumerSecret"));
        twitter.setOAuthAccessToken(getAccessToken());
        return twitter;
    }

    public static void retrieveAccessToken() {
        AccessToken accessToken = null;
        Twitter twitter = factory.getInstance();
        twitter.setOAuthConsumer(Prefs.get("OAuthConsumerKey"), Prefs.get("consumerSecret"));
        RequestToken requestToken;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            requestToken = twitter.getOAuthRequestToken();
            while (null == accessToken) {
                System.out.println("Open the following URL and grant access to your account:");
                System.out.println(requestToken.getAuthorizationURL());
                System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
                String pin = br.readLine();
                try {
                    if (pin.length() > 0) {
                        accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                    } else {
                        accessToken = twitter.getOAuthAccessToken();
                    }
                } catch (TwitterException ex) {
                    if (401 == ex.getStatusCode()) {
                        System.out.println("Unable to get the access token.");
                    } else {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(IumfsTwitterFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TwitterException ex) {
            Logger.getLogger(IumfsTwitterFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        Prefs.put("accessToken", accessToken.getToken());
        Prefs.put("accessTokenSecret", accessToken.getTokenSecret());
    }
    
    public static AccessToken getAccessToken(){
        AccessToken accessToken = null;
        if (Prefs.get("accessToken").isEmpty()) {
            retrieveAccessToken();
        }
        logger.finest("Token&Secret: " +  Prefs.get("accessToken") + " " + Prefs.get("accessTokenSecret"));
        logger.finest("OauthConsum&Secret: " + Prefs.get("OAuthConsumerKey") + " " +  Prefs.get("consumerSecret"));
        
        accessToken = new AccessToken(Prefs.get("accessToken"), Prefs.get("accessTokenSecret"));        
        return accessToken;
    }
}
