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

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Logger;

/**
 * <p>
 * デバイスドライバから受け取った データを元に、適切なリクエストクラスの
 * インスタンスを返すファクトリの抽象クラス。実装はファイルシステム毎に行う。
 * </p>
 * TODO: Instance をプールしておいて、効率的に利用する
 */
public abstract class RequestFactory {

    private static Logger logger = Logger.getLogger("iumfs");
    
    protected RequestFactory(){};

    public Request getInstance(ByteBuffer buf) {
        /*
         * デバイスドライバ から デーモンに渡されるリクエスト構造体
         *  8+1184+1024+8+8+8=2240 bytes
         * typedef struct request
         * {
         *   int64_t             request_type; // リクエストのタイプ
         *   int64_t             size;     // 要求するファイルデータのサイズ
         *   int64_t             offset;   // 要求するファイルデータのオフセット
         *   int64_t             datasize; // リクエスト構造体に続くデータのサイズ
         *   int64_t             flags;    // ioflag の代わり・・と思ったがつかってない。         * 
         *   char                pathname[IUMFS_MAXPATHLEN]; // 操作対象のファイルのパス名
         *   iumfs_mount_opts_t  mountopts[1]; // mount コマンドからの引数
         * }
         *
         * 各メンバは x8 バイトとなっているので、この構造体の
         * 最後は必ず 8バイト境界になっているはず。
         *  40+40+80+1024=1184 bytes
         *
         * typedef struct iumfs_mount_opts
         * {
         *    char user[MAX_USER_LEN];
         *    char pass[MAX_PASS_LEN];
         *    char server[MAX_SERVER_NAME];
         *    char basepath[IUMFS_MAXPATHLEN];
         * } iumfs_mount_opts_t;
         */
        Request req = null;
        int bufsize = buf.capacity();
        long request_type; // リクエストタイプ
        long size;
        long offset;
        long datasize;
        long flags;
        byte pathname[] = new byte[Request.IUMFS_MAXPATHLEN]; // 操作対象のファイルのパス名
        byte basepath[] = new byte[Request.IUMFS_MAXPATHLEN]; // マウント時のベースパス
        byte server[] = new byte[Request.MAX_SERVER_LEN]; // サーバ名(Optional)
        byte username[] = new byte[Request.MAX_USER_LEN]; // ユーザ名(Optional)
        byte password[] = new byte[Request.MAX_PASS_LEN]; // パスワード(Optional)
        byte data[]; // 制御デバイスからの付加データ

        try {
            buf.rewind();
            logger.finer("Buf info pos=" + buf.position() + " limit=" + buf.limit());
            /*
             * 元構造体の ByteBuffer から、各メンバの値を取り出す
             */
            request_type = buf.getLong();
            size = buf.getLong();
            offset = buf.getLong();
            datasize = buf.getLong();
            flags = buf.getLong();
            buf.get(pathname);
            buf.get(basepath);
            buf.get(server);
            buf.get(username);
            buf.get(password);
            
            logger.finer("request_type=" + request_type + ", size=" + size + 
                    ", offset=" + offset + ", datasize=" + datasize);
            logger.finer("basename=" + (new String(basepath)).trim());
            logger.finest("ByteOrder=" + ByteOrder.nativeOrder());
            
            req = createInstance(request_type);
            logger.fine("request=" + req.getClass().getName()
                    + ", pathname=" + (new String(pathname)).trim()
                    + ", username=" + new String(username).trim());             

            req.setByteOrder(ByteOrder.nativeOrder());
            req.setOffset(offset);
            req.setPathname((new String(pathname)).trim()); // 後ろの空白文字を削除
            req.setBasepath((new String(basepath)).trim()); // 後ろの空白文字を削除
            req.setServer((new String(server)).trim()); // 後ろの空白文字を削除
            req.setRequestType(request_type);
            req.setSize(size);
            req.setFlags(flags);
            req.setUserName((new String(username)).trim());
            req.setPassword((new String(password)).trim());
             /*
             * request 構造体の後ろにデータがあるかどうか確認。
             * size は DEVICE_BUFFER_SIZE - REQUEST_HEADER_SIZE を超えない。
             */
            if(size > 0){
                data = new byte[Math.min((int)size, Request.DEVICE_BUFFER_SIZE - Request.REQUEST_HEADER_SIZE) ];
                buf.get(data);
                req.setData(data);
            }           
            return req;
        } catch (BufferUnderflowException ex) {
            ex.printStackTrace();
            System.exit(1);
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    /** 
     * Create FS dependent Request class.
     */
    protected abstract Request createInstance(long request_type);
}


