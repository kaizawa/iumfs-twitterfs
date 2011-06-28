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

import java.util.*;
import java.util.logging.Logger;

/**
 *  GETATTR リクエストを表すクラス
 */
class GetAttrRequest extends Request {

    final public static int ATTR_DATA_LEN = 72; // long x 9 フィールド

    /**
     * twitterfs ファイルシステム上の仮想ディレクトリエントリに対するのステータス
     * をレスポンスヘッダを返す。
     */
    @Override
    public void process() {
        

        
        
        
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
        wbbuf.putLong((long) 0000777);        
        wbbuf.putLong(1);
        if(getPathname().equals("/") == true ||
                getPathname().equals(".") == true ||
                getPathname().equals("..") == true )
            wbbuf.putLong(Request.VDIR);            
        else
            wbbuf.putLong(Request.VREG);
        wbbuf.putLong(now.getTime() / 1000);
        wbbuf.putLong((now.getTime() % 1000) * 1000);
        wbbuf.putLong(now.getTime());
        wbbuf.putLong((now.getTime() % 1000) * 1000);
        wbbuf.putLong(now.getTime() / 1000);
        wbbuf.putLong((now.getTime() % 1000) * 1000);
    }
}
