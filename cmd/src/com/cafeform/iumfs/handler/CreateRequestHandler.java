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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>CREATE Request Class</p>
 */
public class CreateRequestHandler extends AbstractRequestHandler 
{

    /**
     * <p>Excecute FileSystem.create</p>
     * <p>
     * </p>
     * @param request
     * @param file
     * @return 
     */
    
    @Override
    public Response getResponse(Request request, IumfsFile file) 
    {            
        ResponseImpl response = new ResponseImpl();
        if (null == file) {
            response.setResponseHeader(request.getType(), ENOENT, 0L);
            return response;
        }
        try
        {
            file.create();
        } 
        catch (IOException ex)
        {
            response.setResponseHeader(request.getType(), EIO, 0L);
            return response;
        }
        response.setResponseHeader(request.getType(), SUCCESS, 0);
        return response;
    }
}
