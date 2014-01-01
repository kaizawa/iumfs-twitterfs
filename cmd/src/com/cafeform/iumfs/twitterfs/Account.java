/*
 * Copyright 2011 Kazuyoshi Aizawa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this IumfsFile except in compliance with the License.
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
package com.cafeform.iumfs.twitterfs;

import com.cafeform.iumfs.IumfsFile;

/**
 *
 * @author ka78231
 */
public interface Account
{
    public UserTimelineFileManager getUserTimelineManager ();
    public String getUsername ();
    public void setUsername (String user);
    public void setRootDirectory (IumfsFile rootDir);
    public IumfsFile getRootDirectory ();
}
