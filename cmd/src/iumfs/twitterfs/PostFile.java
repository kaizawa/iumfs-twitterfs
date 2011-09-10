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
package iumfs.twitterfs;

import iumfs.NotSupportedException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class PostFile extends TwitterfsFile {
    PostFile(Account account, String name){
        super(account, name);
    }

    @Override
    public long getPermission() {
        return (long) 0100666; // can write
    }

    @Override
    public long read(ByteBuffer buf, long size, long offset) {
        /* Nothing can be read from this file. */
        return 0;
    }
    
    @Override
    public long write(byte[] buf, long size, long offset)
            throws IOException, FileNotFoundException {
        try {
            Status status = null;

            if (getPathname().equals("/post") == false) {
                throw new NotSupportedException();
            }

            /*
             * ファイルとして書かれた文字列を得る。
             */
            Twitter twitter = IumfsTwitterFactory.getInstance(getUsername());
            String whole_msg = new String(buf);
            logger.finer("Orig Text:" + whole_msg);
            logger.finest("whole_msg.length() = " + whole_msg.length());
            int left = whole_msg.length();
            /*
             * 文字列を 140 文字づつステータスとしてポスト.
             */
            MessageSeparator sep = new MessageSeparator(whole_msg);
            while (sep.hasNext()) {
                String msg = (String) sep.next();
                status = twitter.updateStatus(msg);
                logger.finest("Text: " + msg);
                logger.fine("Status updated");
            }
            return 0;
        } catch (TwitterException ex) {
            logger.fine("TwitterException when writing");
            throw new FileNotFoundException();
        } catch (RuntimeException ex) {
            /*
             * 実行時例外が発生した際には EIO(IOエラー)にマップ。
             * なんにしてもちゃんとエラーで返すことが大事。
             */
            throw new IOException();
        }
    }    

}