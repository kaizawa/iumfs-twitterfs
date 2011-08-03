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

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * <p>Write リクエストを表すクラス</p>
 */
public class WriteRequest extends Request {

    private static final String CONT = "(cont) ";

    @Override
    public void process() {
        try {
            long offset = getOffset();
            long size = getSize();
            long filesize = 0;
            Status status;
            
            if (getPathname().equals("/post") == false) {
                setResponseHeader(ENOTSUP, 0);
                return;
            }

            /*
             * ファイルとして書かれた文字列を得る。
             */
            Twitter twitter = TWFactory.getInstance();
            String whole_msg = new String(getData(0, size));
            logger.finer("Orig Text:" + whole_msg);
            logger.finest("whole_msg.length() = " + whole_msg.length());
            int left = whole_msg.length();
            /*
             * 文字列を 140 文字づつステータスとしてポスト.
             */
            MessageSeparator sep = new MessageSeparator(whole_msg);
            while (sep.hasNext()) {
                String msg = (String)sep.next();
                status = twitter.updateStatus((String)sep.next());          
                logger.finest("Text: " + msg);
                logger.fine("Status updated");
            }
            /*
             * レスポンスヘッダをセット
             */
            setResponseHeader(SUCCESS, 0);            
        } catch (TwitterException ex) {
            ex.printStackTrace();
            logger.fine("TwitterException when writing");
            setResponseHeader(EEXIST, 0);
            return;
        } catch (RuntimeException ex) {
            /*
             * 実行時例外が発生した際には EIO(IOエラー)にマップ。
             * なんにしてもちゃんとエラーで返すことが大事。
             */
            ex.printStackTrace();
            setResponseHeader(EIO, 0);
            return;
        }
    }
}
