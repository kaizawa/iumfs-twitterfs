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

import java.util.Date;
import java.util.logging.Level;
import javax.imageio.stream.FileCacheImageInputStream;

/**
 *  GETATTR リクエストを表すクラス
 */
public abstract class GetAttrRequest extends Request {

    final public static int ATTR_DATA_LEN = 72; // long x 9 フィールド
    private static final long start_time = new Date().getTime();

    /**
     * twitterfs ファイルシステム上の仮想エントリに対するファイルの属性情報
     * のリクエストを処理する。
     */
    @Override
    public void process() {
        try {
            /*
             * 対応した File オブジェクトを得る。
             */
            file = getFile(getPathname());

            if (file == null) {
                /*
                 * 既知のファイル名でなく、ディレクトリでもない。
                 * 不明なファイルの要求。ENOENT を返す。
                 */
                setResponseHeader(ENOENT, 0);
                return;
            }

            /*
             * レスポンスヘッダをセット
             */
            setResponseHeader(0, GetAttrRequest.ATTR_DATA_LEN);

            /*
             * ファイル属性をバッファにセット
             * typedef struct iumfs_vattr
             * {
             *   uint64_t  i_mode; // ファイルモード
             *   uint64_t  i_size; // ファイルサイズ
             *   int64_t   i_type; // ファイルタイプ
             *   int64_t   mtime_sec; // 変更時間(sec)
             *   int64_t   mtime_nsec;// 変更時間(nsec)
             *   int64_t   atime_sec; // 属性変更時間(sec)
             *   int64_t   atime_nsec;// 属性変更時間(nsec)
             *   int64_t   ctime_sec; // 作成時間(sec)
             *   int64_t   ctime_nsec;// 作成時間(nsec)
             * } iumfs_vattr_t;
             */
            Date now = new Date();
            wbbuf.putLong(file.getPermission());
            wbbuf.putLong(file.getFileSize());
            wbbuf.putLong(file.getFileType());
            if (file == null) {
                wbbuf.putLong(start_time / 1000);
                wbbuf.putLong((start_time % 1000) * 1000);
                wbbuf.putLong(start_time / 1000);
                wbbuf.putLong((start_time % 1000) * 1000);
                wbbuf.putLong(start_time / 1000);
                wbbuf.putLong((start_time % 1000) * 1000);
            } else {
                wbbuf.putLong(file.getMtime() / 1000);
                wbbuf.putLong((file.getMtime() % 1000) * 1000);
                wbbuf.putLong(file.getAtime() / 1000);
                wbbuf.putLong((file.getAtime() % 1000) * 1000);
                wbbuf.putLong(file.getCtime() / 1000);
                wbbuf.putLong((file.getCtime() % 1000) * 1000);
            }
            logger.finer("Permission=" + file.getPermission() + " ,FileSize=" + file.getFileSize() + ", FileType=" + file.getFileType());
            
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
