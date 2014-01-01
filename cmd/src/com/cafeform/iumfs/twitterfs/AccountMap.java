package com.cafeform.iumfs.twitterfs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author kaizawa
 */
public class AccountMap {
    
    private static final Map<String, Account> accountMap = 
            new ConcurrentHashMap<>();
    
    static void put (String username, Account account)
    {
        accountMap.put(username, account);
    }

    static Account get (String username)
    {
        return accountMap.get(username);
    }

    static void remove (String USER1)
    {
        accountMap.remove(USER1);
    }

}
