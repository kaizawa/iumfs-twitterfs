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
import iumfs.GetAttrRequest;
import java.util.*;

/**
 *  GETATTR request
 */
class TwitterfsGetAttrRequest extends GetAttrRequest {
    private static final long start_time = new Date().getTime();

    @Override
    public File getFile() {
        return TwitterfsFile.getFile(getUserName(), getPathname());
    }
}
