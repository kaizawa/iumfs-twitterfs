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
package iumfs.handler;

import iumfs.IumfsFile;
import iumfs.Request;
import iumfs.Response;
import iumfs.ResponseImpl;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 *  GETATTR request class
 */
public class GetAttrRequestHandler extends AbstractRequestHandler
{

    final public static int ATTR_DATA_LEN = 72; // long x 9 フィールド
    private static final long start_time = new Date().getTime();

    /**
     * Return file attribute information of file on filesystem
     * @param request
     * @param file
     * @return response
     */
    @Override
    public Response getResponse(Request request, IumfsFile file) 
    {
        Response response = new ResponseImpl();
        if (null == file || false == file.exists()) {
             // Unknown file
             // return ENOENT
            response.setResponseHeader(request.getType(), ENOENT, 0);
            return response;
        }
        
        System.out.println("reqest type = " + request.getType());

        response.setResponseHeader(
                request.getType(),
                SUCCESS, 
                GetAttrRequestHandler.ATTR_DATA_LEN);
        
        ByteBuffer buffer = response.getBuffer();
        /*
         * proceed the position until heder size.
         * header information including data size will be
         * set after we know actuall data size.
         */
        buffer.position(ResponseImpl.RESPONSE_HEADER_SIZE);
        
        /*
         * ファイル属性をバッファにセット
         * typedef struct iumfs_vattr
         * {
         *   uint64_t  i_mode; // file mode
         *   uint64_t  i_size; // file size
         *   int64_t   i_type; // file type
         *   int64_t   mtime_sec; // modify time(sec)
         *   int64_t   mtime_nsec;// modify time(nsec)
         *   int64_t   atime_sec; // access time(sec)
         *   int64_t   atime_nsec;// access time(nsec)
         *   int64_t   ctime_sec; // change time(sec)
         *   int64_t   ctime_nsec;// change time(nsec)
         * } iumfs_vattr_t;
         */
        Date now = new Date();
        buffer.putLong(file.getPermission());
        buffer.putLong(file.length());
        buffer.putLong(file.getFileType().longVal());
        if (file == null) 
        {
            buffer.putLong(start_time / 1000);
            buffer.putLong((start_time % 1000) * 1000);
            buffer.putLong(start_time / 1000);
            buffer.putLong((start_time % 1000) * 1000);
            buffer.putLong(start_time / 1000);
            buffer.putLong((start_time % 1000) * 1000);
        } 
        else 
        {
            buffer.putLong(file.getMtime() / 1000);
            buffer.putLong((file.getMtime() % 1000) * 1000);
            buffer.putLong(file.getAtime() / 1000);
            buffer.putLong((file.getAtime() % 1000) * 1000);
            buffer.putLong(file.getCtime() / 1000);
            buffer.putLong((file.getCtime() % 1000) * 1000);
        }
        logger.finer("filename=" + file.getName() + ", Permission="
                + String.format("%1$o", file.getPermission()) + " ,FileSize=" + file.length()
                + ", FileType=" + file.getFileType());
        
        return response;
    }
}
