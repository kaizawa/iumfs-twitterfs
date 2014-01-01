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
package com.cafeform.iumfs.twitterfs;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Separate long message into small chank(less than or
 * equal to given maxLength characters).
 * 2nd and later chank has "(contd)" prefix which indicates that it is
 * a part of continual messages.
 * 
 */
public class MessageSeparator implements Iterator 
{    
    private String wholeMessage;
    int begin = 0;
    int end = 0;
    int left = 0;
    protected static final Logger logger =
            Logger.getLogger(MessageSeparator.class.getName());
    private static final String CONT = "(contd) ";
    private String prefix;
    private final int MAX_LENGTH = 140;
    
    private MessageSeparator(){}    

    public MessageSeparator(String whole_msg, String prefix) 
    {
        this.wholeMessage = whole_msg;
        left = whole_msg.length();  
        this.prefix = null == prefix ? "" : prefix;
    }

    @Override
    public boolean hasNext() 
    {
        if(left > 0 )
            return true;
        else 
            return false;
    }

    /*
     * Return next chank of mesasge.
     * Length of returns string is less than equal to 140 charactors.
     */
    @Override
    public Object next() 
    {
        int prefixLength; // Length of prefix(@XXXX and "(contd)"). 
        int messageLength; // body part of message length.
        int postlen;// total message length
        StringBuilder builder = new StringBuilder();

        logger.finer("left=" + left);
        prefixLength = begin == 0 ? prefix.length() :
                CONT.length() + prefix.length();
        messageLength = Math.min(MAX_LENGTH - prefixLength, left);
        end = begin + messageLength;        
        postlen = messageLength + prefixLength;
        logger.finer("begin=" + begin + ",end=" + end );
        
        builder.append(prefix);
        if (0 != begin ){
            builder.append(CONT);
        }
        builder.append(wholeMessage.substring(begin, end));
        
        logger.finest(builder.toString());
        begin += messageLength;
        left -= messageLength;
        return builder.toString();
    }

    @Override
    public void remove() 
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
