package com.cafeform.iumfs.twitterfs;

/**
 * Standalone progrem to setup Twitter account
 * which is called from setup shell script.
 */
public class Setup
{
    public static void main(String args[])
    {

        if (1 == args.length)
        {
            String username = args[0];            
            TwitterFactoryAdapter.getAccessToken(username);
            if (Prefs.get(username + "/accessToken").isEmpty())
            {
                System.out.println("Failed to setup Twitter access token");
            } 
            else
            {
                System.out.println("Twitter access token setup sucessfully");
            }
        } 
        else
        {
            System.out.println("Usage: iumfs.twitterfs.Setup <username>");
        }
    }
}
