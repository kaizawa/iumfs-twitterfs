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

import java.util.Iterator;
import java.util.logging.Logger;
import java.lang.Math;

/**
 *
 * @author ka78231
 */
public class MessageSeparator implements Iterator {    
    private MessageSeparator(){}
    private String whole_msg;
    int begin = 0;
    int end = 0;
    int postlen = 0;    
    int left = 0;
    protected static Logger logger = Logger.getLogger(twitterfsd.class.getName());
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

    @Override
    public Object next() {
        logger.finer("left=" + left);
        int hdrlen = begin == 0 ? 0 : CONT.length();
        int msglen = Math.min(140-hdrlen, left);
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
