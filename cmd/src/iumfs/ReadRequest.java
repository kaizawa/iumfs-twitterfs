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
import java.io.UnsupportedEncodingException;
import java.util.Date;
import twitter4j.Paging;
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
        long read_size = 0;

        long offset = getOffset(); // ファイルシステムから要求されたファイルオフセット
        long size = getSize(); // ファイルシステムから要求されたサイズ
        
        logger.fine("offset = " + offset + "size = " + size);
        
        File file = twitterfsd.fileMap.get(getPathname());

        /*
         * 既知の File エントリ以外はファイルサイズを 1 とする。
         */
        if (file == null) {
            setResponseHeader(ENOENT, 0);
            return;
        }
        
        try {
            //バッファーの書き込み位置を レスポンスヘッダ分だけずらしておく。 
            wbbuf.clear();
            wbbuf.position(Request.RESPONSE_HEADER_SIZE);
            //読み込む
            read_size = file.read(wbbuf, size, offset);
            //最終アクセス時間を変更
            file.setAtime(new Date().getTime());
        } catch (TwitterException ex) {
            ex.printStackTrace();
            setResponseHeader(ENOENT, 0);
            return;
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            setResponseHeader(ENOENT, 0);
            return;
        }

        logger.fine("read_size = " + read_size);
        /*
         * レスポンスヘッダをセット
         */
        setResponseHeader(SUCCESS, read_size);
        return;
    }
}
