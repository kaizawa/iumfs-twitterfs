package com.cafeform.iumfs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author kaizawa
 */
public class Files {
    public static String getNameFromPathName (String pathName)
    {
        // Get exact file name from pathName
        String regex = "((.*)/)*(.*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(pathName);
        if (!matcher.find())
        {
            throw new IllegalArgumentException("pathName " + pathName +
                    " is invalid.");
        }
        String name  = matcher.group(3);
        return name;
    }
}
