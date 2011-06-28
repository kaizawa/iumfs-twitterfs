/*
 * Copyright 2011 Kazuyoshi Aizawa
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

/**
 *  READDIR リクエストを表すクラス
 */
class ReadDirRequest extends Request {

    /**
     * <p>twitterfs 上の仮想ディレクトリエントリを読み込み、結果をレスポンス
     * ヘッダをセットする</p>
     */
    @Override
    public void process() {
        /*
         * まず最初にヘッダ分だけバッファの位置を進めておく。
         * ヘッダはデータ長がわかってから改めてセットする
         */
	wbbuf.position(Request.RESPONSE_HEADER_SIZE);

        for (File file : twitterfsd.entries) {
            int namelen = file.getPath().getBytes().length;
            namelen++; // null terminate 用。

            /*
             * 受け取り側の driver でのアライメント対策のため reclen
             * (レコード長）は必ず 8 の倍数になるようにする。
             * typedef struct iumfs_dirent
             * {
             *   int64_t           i_reclen;
             *   char              i_name[1];
             * } iumfs_dirent_t; *
             */
            int reclen = (8 + 1 + (namelen) + 7) & ~7;
            logger.finer("name="+file.getPath()+",namelen="+namelen+",reclen="+reclen);
            wbbuf.putLong(reclen);
            for (byte b : file.getPath().getBytes()) {
                wbbuf.put(b);
            }
            wbbuf.put((byte)0); // null terminate           
            /*
             * Position を reclen 分だけ進めるためにパディングする
             */
            wbbuf.position(wbbuf.position() + (reclen - 8 - namelen));
        }
        /*
         * レスポンスヘッダをセット
         */
        setResponseHeader(SUCCESS, wbbuf.position() - Request.RESPONSE_HEADER_SIZE);
    }
}
