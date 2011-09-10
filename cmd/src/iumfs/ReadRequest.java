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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

/**
 *  READ Request class
 */
public abstract class ReadRequest extends Request {

    /**
     * Twitter の TL を読み込み結果をレスポンスヘッダをセットする
     */
    @Override
    public void execute() throws FileNotFoundException, IOException {
        long read_size = 0;

        long offset = getOffset(); // ファイルシステムから要求されたファイルオフセット
        long size = getSize(); // ファイルシステムから要求されたサイズ

        logger.fine("offset = " + offset + " size = " + size);

        File file = getFile();
        if (file == null) {
            setResponseHeader(ENOENT, 0);
            return;
        }

        //バッファーの書き込み位置を レスポンスヘッダ分だけずらしておく。 
        wbbuf.clear();
        wbbuf.position(Request.RESPONSE_HEADER_SIZE);
        //読み込む
        read_size = file.read(wbbuf, getSize(), getOffset());
        //最終アクセス時間を変更
        file.setAtime(new Date().getTime());
        logger.fine("read_size = " + read_size);
        /*
         * レスポンスヘッダをセット
         */
        setResponseHeader(SUCCESS, read_size);
        return;
    }

    abstract public File getFile();
}
