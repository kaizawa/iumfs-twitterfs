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

    @Override
   public void process() {
       long offset = getOffset();
       long size = getSize();
       long filesize = 0;
       Status status;
       
       if(getPathname().equals("/post") == false){
            setResponseHeader(ENOTSUP, 0);
            return;
       }
        try{
            /*
             * ファイルとして書かれたステータスを twitter にポストする。
             */
            Twitter twitter = TWFactory.getInstance();
            String msg = new String(getData(0,size));
            status = twitter.updateStatus(msg);
            System.out.println(msg);
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
        }
    }
}
