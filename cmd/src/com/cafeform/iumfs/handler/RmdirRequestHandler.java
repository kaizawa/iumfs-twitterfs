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

/**
 *  RMDIR request class
 */
public class RmdirRequestHandler extends AbstractRequestHandler {
    /**
     * remove directory
     * @param request
     * @param file
     * @return 
     */
    @Override
    public Response getResponse(Request request, IumfsFile file) 
    {      
        Response response = new ResponseImpl();

        if(file.isDirectory() == false)
        {
            response.setResponseHeader(request.getType(), ENOTDIR, 0);
            return response;
        }
        
        if (file.delete() == false) {
            logger.fine("cannot remove " + request.getFullPath());
            response.setResponseHeader(request.getType(), EIO, 0);
            return response;
        }
        /*
         * Set response header
         */
        response.setResponseHeader(request.getType(), SUCCESS, 0);
        return response;
    }
}
