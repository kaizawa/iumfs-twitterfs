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

import java.nio.ByteBuffer;

public class DirectoryFile extends TwitterfsFile { 
    DirectoryFile(Account account, String name){
        super(account, name);
    }

    @Override
    public boolean isDir() {
        return true;
    }

    @Override
    public long getPermission() {
        return (long) 0040755; // directory
    }

    @Override
    public long read(ByteBuffer buf, long size, long offset) {
        return 0;
    }

    @Override
    public long write(byte[] buf, long size, long offset) {
        return 0;
    }
}