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
import java.lang.Math;

/**
 * Separate long message (more than 140 char) into small chank(less than or
 * equal to 140 char).
 * 2nd and later chank has "(contd)" prefix which indicates that it is
 * a part of continual messages.
 * 
 */
public class MessageSeparator implements Iterator {    
    private MessageSeparator(){}
    private String whole_msg;
    int begin = 0;
    int end = 0;
    int left = 0;
    protected static Logger logger = Logger.getLogger(MessageSeparator.class.getName());
    private static final String CONT = "(contd) ";

    public MessageSeparator(String whole_msg) {
        this.whole_msg = whole_msg;
        left = whole_msg.length();        
    }

    @Override
    public boolean hasNext() {
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
    public Object next() {
        int hdrlen; // Length of prefix(="contd"). zero if needless.
        int msglen; // body part of message length.
        int postlen;// total message length

        logger.finer("left=" + left);
        hdrlen = begin == 0 ? 0 : CONT.length();
        msglen = Math.min(140-hdrlen, left);
        end = begin + msglen;        
        postlen = msglen + hdrlen;
        logger.finer("begin=" + begin + ",end=" + end );
        String str = whole_msg.substring(begin, end);
        str = begin == 0 ? str : CONT.concat(str);        
        logger.finest(str);
        begin += msglen;
        left -= msglen;
        return str;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
