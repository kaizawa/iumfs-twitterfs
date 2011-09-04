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
package iumfs.twitterfs;

import iumfs.File;
import java.io.IOException;
import java.util.logging.Logger;
import iumfs.MkdirRequest;

/**
 * <p>MKDIR リクエストを表すクラス</p>
 */
class TwitterfsMkdirRequest extends MkdirRequest{
    /**
     * <p>TWITTERFS 上にディレクトリを作成する。未サポート</p>
     */
    @Override
    public void execute() {
        /*
         * サポートしていない
         */
        setResponseHeader(ENOTSUP, 0);        
    }

    @Override
    public File getFile() {
        return Main.getFile(getUserName(), getPathname());
    }
}
