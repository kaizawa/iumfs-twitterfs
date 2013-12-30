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
package com.cafeform.iumfs.handler;

import com.cafeform.iumfs.IumfsFile;
import com.cafeform.iumfs.Request;
import com.cafeform.iumfs.Response;
import com.cafeform.iumfs.ResponseImpl;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.logging.Level;

/**
 * READDIR Request class
 */
public class ReadDirRequestHandler extends AbstractRequestHandler
{

    /**
     * <p>
     * Read virtual directory entry
     * </p>
     *
     * @param request
     * @param directory
     * @return
     */
    @Override
    public Response getResponse(Request request, IumfsFile directory)
    {
        Response response = new ResponseImpl();
        ByteBuffer buffer = response.getBuffer();
        /*
         * proceed the position until heder size.
         * header information including data size will be
         * set after we know actuall data size.
         */
        buffer.position(ResponseImpl.RESPONSE_HEADER_SIZE);

        /*
         * Note, File object here is just used to express file name.
         * Do not call any method of this File object.
         * It would causes unexpected results.
         */
        for (IumfsFile entry : directory.listFiles())
        {
            int reclen;
            int namelen;
            try
            {
                namelen = entry.getName().getBytes("UTF-8").length;
                namelen++; // null terminate。

                /*
                 * For data alinment, reclen must be multiple of 8.
                 *
                 * typedef struct iumfs_dirent
                 * {
                 *   int64_t           i_reclen;
                 *   char              i_name[1];
                 * } iumfs_dirent_t; *
                 */
                reclen = (8 + 1 + (namelen) + 7) & ~7;
                logger.log(Level.FINER, "name=" + entry.getName() + 
                        ", namelen=" + namelen + ", reclen=" + reclen);
                buffer.putLong(reclen);
                buffer.put(entry.getName().getBytes("UTF-8"));
            } 
            catch (UnsupportedEncodingException ex)
            {
                logger.log(Level.INFO, "Cannot decode file name", ex);
                response.setResponseHeader(request.getType(), EIO, 0);
                return response;
            }
            buffer.put((byte) 0); // null terminate           
            // Proceed position to padding unitl reclen.
            buffer.position(buffer.position() + (reclen - 8 - namelen));
        }
        /*
         * Set response header
         */
        response.setResponseHeader(
                request.getType(),
                SUCCESS,
                buffer.position() - ResponseImpl.RESPONSE_HEADER_SIZE);
        return response;
    }
}
