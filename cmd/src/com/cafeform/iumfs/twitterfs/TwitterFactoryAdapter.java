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
package com.cafeform.iumfs.twitterfs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserStreamListener;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Adapter of TwitterFactory calss and TwitterStreamFactory class
 */
public class TwitterFactoryAdapter {

    private static final Logger logger =
            Logger.getLogger(TwitterFactoryAdapter.class.getName());
    static TwitterFactory factory = new TwitterFactory();
    static TwitterStreamFactory streamFactory =
            new TwitterStreamFactory();

    /**
     * Return instance of Twitter class which has AccessToken been set.
     * @param username
     * @return twitter instance of Twitter class
     */
    public static Twitter getInstance (String username) 
    {
        Twitter twitter = factory.getInstance();
        twitter.setOAuthConsumer(
                Prefs.get("OAuthConsumerKey"), 
                Prefs.get("consumerSecret"));
        twitter.setOAuthAccessToken(getAccessToken(username));
        return twitter;
    }
    
    public static void setUserStreamListener (
            String username,
            UserStreamListener listener)
    {
        // Invokde timeline retrieving thread. 
        // Stream API (only home)
        // Thread created in TwitterStream.sample()
        TwitterStream twitterStream = streamFactory.getInstance();
        twitterStream.setOAuthConsumer(
                Prefs.get("OAuthConsumerKey"),
                Prefs.get("consumerSecret"));
        twitterStream.setOAuthAccessToken(
                TwitterFactoryAdapter.getAccessToken(username));
        twitterStream.addListener(listener);
        twitterStream.user();
    }

    public static AccessToken getAccessToken (String username)
    {
        AccessToken accessToken = null;
        if (Prefs.get(username + "/accessToken").isEmpty()) 
        {
            Twitter twitter = factory.getInstance();
            twitter.setOAuthConsumer(
                    Prefs.get("OAuthConsumerKey"), 
                    Prefs.get("consumerSecret"));
            RequestToken requestToken;
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            try 
            {
                requestToken = twitter.getOAuthRequestToken();
                while (null == accessToken) 
                {
                    System.out.println("Open the following URL and grant access to your account:");
                    System.out.println(requestToken.getAuthorizationURL());
                    System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
                    String pin = br.readLine();
                    try 
                    {
                        if (pin.length() > 0) {
                            accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                        } else {
                            accessToken = twitter.getOAuthAccessToken();
                        }
                    } 
                    catch (TwitterException ex) 
                    {
                        if (401 == ex.getStatusCode()) 
                        {
                            System.out.println("Unable to get the access token.");
                        }
                        else 
                        {
                            logger.log(Level.SEVERE, 
                                    "Unable to get the Access Token", ex);                            
                        }
                    }
                }
            } 
            catch (IOException | TwitterException ex) 
            {
                logger.log(Level.SEVERE, "Unable to get the Access Token", ex);
            }
            Prefs.put(username + "/accessToken", accessToken.getToken());
            Prefs.put(username + "/accessTokenSecret", accessToken.getTokenSecret());
        }
        logger.finest("Token&Secret: " + Prefs.get(username + "/accessToken") +
                " " + Prefs.get(username + "/accessTokenSecret"));
        logger.finest("OauthConsum&Secret: " + Prefs.get("OAuthConsumerKey") + 
                " " + Prefs.get("consumerSecret"));

        accessToken = new AccessToken(
                Prefs.get(username + "/accessToken"), 
                Prefs.get(username + "/accessTokenSecret"));
        return accessToken;
    }

    static User lookupUser (String username, String screenName)
    {
        try
        {
            return getInstance(username).showUser(screenName);
        } 
        catch (TwitterException ex)
        {
            return null;
        }
    }
}
