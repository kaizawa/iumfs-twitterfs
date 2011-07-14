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

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.logging.Logger;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 *  GETATTR リクエストを表すクラス
 */
class GetAttrRequest extends Request {

    final public static int ATTR_DATA_LEN = 72; // long x 9 フィールド

    /**
     * twitterfs ファイルシステム上の仮想エントリに対するファイルの属性情報
     * のリクエストを処理する。
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
        wbbuf.putLong(getPermission());
        wbbuf.putLong(getFileSize());
        wbbuf.putLong(getFileType());
        wbbuf.putLong(now.getTime() / 1000);
        wbbuf.putLong((now.getTime() % 1000) * 1000);
        wbbuf.putLong(now.getTime());
        wbbuf.putLong((now.getTime() % 1000) * 1000);
        wbbuf.putLong(now.getTime() / 1000);
        wbbuf.putLong((now.getTime() % 1000) * 1000);
    }

    /*
     * ファイルタイプを返す。
     * ファイル名で判定し、'/', ',', '..' だったらディレクトリとみなし
     * それ以外は通常ファイルとみなす。
     */
    private long getFileType() {
        if(isDir())
            return Request.VDIR;
        else
            return Request.VREG;
    }
    
    /*
     * 起点となるステータス(homeBaseId)からのタイムラインのサイズを計算する。
     * 基本は 名前(未実装)+時間(未実装)+テキスト+改行文字を足したもの。
     */
    private long getFileSize() {
        File file = twitterfsd.fileMap.get(getPathname());
        
        /*
         * 既知の File エントリ以外はファイルサイズを 1 とする。
         */
        if(file == null){
            return 1;
        }
        return file.getFileSize();
    }
    
    private long getPermission(){
        if(isDir())
            return (long) 0000755; 
        else
            return (long) 0000644;
    }
    
    /**
     * リクエストされているディレクトリエントリがディレクトリであるかどうかを判定する。
     * 名前が . 、..、もしくは / だったらディレクトリ。
     * @return true if file is directory.
     */
    private boolean isDir(){
        if( getPathname().equals("/") == true ||
                getPathname().equals(".") == true ||
                getPathname().equals("..") == true ) 
            return true;
        else 
            return false;
    }
}
