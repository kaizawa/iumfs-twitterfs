package com.cafeform.iumfs.twitterfs;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.*;

/**
 * Preferences for twitterfsd
 */
public class Prefs 
{
    static Preferences prefs = Preferences.userNodeForPackage(TwitterFs.class);        
  
    static final Map<String, Boolean> booleanPrefDefaultMap = new HashMap<>();
    static final Map<String, String> stringPreDefaultfMap  = new HashMap<>();
    static final Map<String, Integer> intPrefDefaultMap  = new HashMap<>();    

    static 
    {
        booleanPrefDefaultMap.put("autoUpdateEnabled", true); // Default value for auto-update
        booleanPrefDefaultMap.put("progressBarEnabled", false); // Enable/Disable progress bar.

        stringPreDefaultfMap.put("OAuthConsumerKey", "NSCnnDYBngnnalYQHj2sA");
        stringPreDefaultfMap.put("consumerSecret", "9hMmw0zNJiK0I3rlJ46HR33yjxulH0kU6UPnDQpPEk");

        intPrefDefaultMap.put("homeUpdateInterval", 30);  // Default interval of auto-update
        intPrefDefaultMap.put("userUpdateInterval", 300);  // Default interval of auto-update
        intPrefDefaultMap.put("publicUpdateInterval", 300); // Default interval of auto-update
        intPrefDefaultMap.put("mentionUpdateInterval", 120); // Default interval of auto-update  
        intPrefDefaultMap.put("maxStatuses", 100); // Number of tweets to be shown.
        intPrefDefaultMap.put("corePoolSize", 6); // NUmber of Threads used for actor
        intPrefDefaultMap.put("maxCacheIcons", 100); // Max number of user icons to be cached.
        intPrefDefaultMap.put("maxThreads", 4); // Max number of threads. This number is limited by iumfs driver.
        intPrefDefaultMap.put("maxPages", 4); // Max number of Twitter TimeLine Page
        intPrefDefaultMap.put("maxStatuses", 200); // Max number of Twitter Statuses
    }
    

    /**
     * return String preference
     * @param name preference name
     * @return value value of preference
     */
    static public String get(String name) 
    {
        String value = stringPreDefaultfMap.get(name);
        if(value == null)
            value = "";
        return prefs.get(name, value);
    }
    
    /**
     * return Int preference
     * @param name preference name
     * @return value value of preference
     */  
    static public int getInt(String name) 
    {
        Integer value  = intPrefDefaultMap.get(name);
        if(value == null)
            value = 0;
        return prefs.getInt(name, value);
    }
    
    /**
     * return Boolean preference
     * @param name preference name
     * @return value value of preference
     */  
    static public Boolean getBoolean(String name) 
    {
        Boolean value = booleanPrefDefaultMap.get(name);
        if(value == null)
            value = false;
        return prefs.getBoolean(name, value);
    }
    
    /**
     * Set String preference
     * @param key
     * @param value value of preference
     */  
    static public void put(String key ,String value) {
        prefs.put(key, value);
    }
    
    /**
     * Set Int preference
     * @param key
     * @param value value of preference
     */    
    static public void putInt(String key ,int value) 
    {
        prefs.putInt(key, value);
    }
    
    /**
     * Set Boolean preference
     * @param key
     * @param value value of preference
     */    
    static public void putBoolean(String key, Boolean value) {
        prefs.putBoolean(key, value);
    }
    
    static public void sync(){
        try 
        {
            prefs.sync();
        } 
        catch (BackingStoreException ex) 
        {
            
        }
    }
}
