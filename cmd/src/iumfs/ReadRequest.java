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
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.ResponseList;
import twitter4j.Status;

import java.io.IOException;
import twitter4j.TwitterException;

/**
 *  READ リクエストを表すクラス
 */
public class ReadRequest extends Request {

    /**
     * Twitter の TL を読み込み結果をレスポンスヘッダをセットする
     * 未実装
     */
    @Override
    public void process() {
        int ret = 0;
        try {
            Twitter twitter = TWFactory.getInstance();
            ResponseList<Status> statuses =  twitter.getHomeTimeline();
            /*
             * ファイルの指定オフセット/サイズのデータを書き込み用バッファに読み込む
             */
            int numRead;
            for(Status status : statuses){
                String text = status.getText();
                // ret = fsdis.read(getOffset(), wbbuf.array(), Request.RESPONSE_HEADER_SIZE, (int) getSize());
                logger.fine("read offset=" + getOffset() + ",size=" + getSize() + " : " + text );
                logger.fine(status.getId() + ":");
            }
            
            /*
             * レスポンスヘッダをセット
             */
            setResponseHeader(SUCCESS, ret);

        } catch (TwitterException ex) {
            logger.fine("TwitterException happend when reading hdfs. offset=" + getOffset() + ",size=" + getSize());
            ex.printStackTrace();
            setResponseHeader(ENOENT, 0);            
        }
    }
}
