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

import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Locale;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

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
             * 二回目以降のポストには「(cont)」という文字を先頭につけて
             * つづきの文章であることを表す。
             */
            int begin = 0;
            int end = 0;
            int post_len = 0;
            while (left > 0) {
                logger.finer("left = " + left);
                logger.finer("begin = " + begin);                
                if (begin == 0) {
                    post_len = Math.min(140, left);
                    end = begin + post_len;
                } else {
                    post_len = Math.min(140, left + CONT.length());
                    end = begin + post_len - CONT.length();
                }
                String msg = whole_msg.substring(begin, end);
                /*
                 * 最初の投稿以外は必ず頭に「(cont)」をつける。
                 */
                if (begin != 0) {
                    msg = CONT.concat(msg);
                }
                status = twitter.updateStatus(msg);                 
                logger.finer("post_len = " + post_len);
                logger.finer("msg.length() = " + msg.length());
                logger.finest("Text: " + msg);
                /*
                 * 残りの文字列長(left)と次の読取位置(begin)を計算
                 * 二回目以降は「(cont)」が着いているのでその分だけカウントを減らす。
                 */
                if (begin == 0) {
                    // 最初
                    left -= post_len; 
                    begin += post_len;
                } else {
                    // 二回目以降
                    left -= (post_len - CONT.length()); 
                    begin += post_len - CONT.length();
                }
            }
            logger.fine("Status updated");
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
