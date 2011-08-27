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
package iumfs.twitterfs;

import iumfs.File;
import iumfs.ReadDirRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.logging.Logger;

/**
 *  READDIR リクエストを表すクラス
 */
class TwitterfsReadDirRequest extends ReadDirRequest {
    protected static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * <p>twitterfs 上の仮想ディレクトリエントリを読み込み、結果をレスポンス
     * ヘッダをセットする</p>
     */
    @Override
    public void process() {
        try {
            /*
             * まず最初にヘッダ分だけバッファの位置を進めておく。
             * ヘッダはデータ長がわかってから改めてセットする
             */
            wbbuf.position(TwitterfsRequest.RESPONSE_HEADER_SIZE);

            for (File file : Main.fileMap.values()) {
                int namelen = file.getName().getBytes("UTF-8").length;
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
                logger.finer("name=" + file.getName() + ",namelen=" + namelen + ",reclen=" + reclen);
                wbbuf.putLong(reclen);
                for (byte b : file.getName().getBytes("UTF-8")) {
                    wbbuf.put(b);
                }
                wbbuf.put((byte) 0); // null terminate           
                /*
                 * Position を reclen 分だけ進めるためにパディングする
                 */
                wbbuf.position(wbbuf.position() + (reclen - 8 - namelen));
            }
            /*
             * レスポンスヘッダをセット
             */
            setResponseHeader(SUCCESS, wbbuf.position() - TwitterfsRequest.RESPONSE_HEADER_SIZE);
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            System.exit(1);
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
    
    @Override
    public File getFile(String pathName){
        return  Main.fileMap.get(getPathname());
    }

    @Override
    public Collection<File> getFileList() {
        return Main.fileMap.values();
    }
}
