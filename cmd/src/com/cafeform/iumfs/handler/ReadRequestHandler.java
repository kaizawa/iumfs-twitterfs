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
package com.cafeform.iumfs.handler;

import com.cafeform.iumfs.IumfsFile;
import com.cafeform.iumfs.NotSupportedException;
import com.cafeform.iumfs.Request;
import com.cafeform.iumfs.Response;
import com.cafeform.iumfs.ResponseImpl;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 *  READ Request class
 */
public class ReadRequestHandler extends AbstractRequestHandler 
{
    /**
     * Read file data and return it to driver with response header
     * @param request
     * @param file
     * @return 
     */
    @Override
    public Response getResponse(Request request, IumfsFile file) 
    {
        long read_size;
        long offset = request.getOffset(); // file offset to read
        long size = request.getSize(); // data size to read
        Response response = new ResponseImpl();

        logger.fine("offset = " + offset + " size = " + size);

        if (file == null) 
        {
            response.setResponseHeader(request.getType(), ENOENT, 0);
            return response;
        }
        
        ByteBuffer buffer = response.getBuffer();

        // adjust buffer write position.
        buffer.clear();
        buffer.position(ResponseImpl.RESPONSE_HEADER_SIZE);
        try
        {
            // read from file
            read_size = file.read(buffer, request.getSize(), request.getOffset());
        }
        catch (IOException ex)
        {
            response.setResponseHeader(request.getType(), EIO, 0);
            return response;
        } 
        catch (NotSupportedException ex)
        {
            response.setResponseHeader(request.getType(), ENOTSUP, 0);
            return response;
        }

        // change access time
        file.setAtime(new Date().getTime());
        logger.fine("read_size = " + read_size);
        // set reseponse header
        response.setResponseHeader(request.getType(),SUCCESS, read_size);
        return response;
    }
}
