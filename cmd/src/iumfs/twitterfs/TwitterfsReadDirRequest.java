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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *  READDIR リクエストを表すクラス
 */
class TwitterfsReadDirRequest extends ReadDirRequest {

    @Override
    public File getFile(){
        return  Main.getFile(getUserName(), getPathname());
    }

    @Override
    public Collection<File> getFileList() {
        List<File> fileList = new ArrayList<File>();
        
        for(File file : Main.getFileMap(getUserName()).values()){
            /*
             * name が "" のものはカレントディレクトリを示す File オブジェクト
             * なので含める必要がない。
             */
            if(file.getName().isEmpty())
                continue;

            fileList.add(file);
        }
        return fileList;
    }
}
