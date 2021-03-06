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
import com.cafeform.iumfs.Request;
import com.cafeform.iumfs.Response;
import com.cafeform.iumfs.ResponseImpl;

/**
 * <p>MKDIR Reuqest class</p>
 */
public class MkdirRequestHandler extends AbstractRequestHandler {

    /**
     * <p>Create directory.</p>
     * 
     * @param request
     * @param file
     * @return response
     */
    @Override
    public Response getResponse(Request request, IumfsFile file) 
    {
        Response response = new ResponseImpl();
        if (file.mkdir() != true)
        {
            logger.fine("cannot create directory " + request.getFullPath());
            response.setResponseHeader(request.getType(), EIO, 0);
        }
        response.setResponseHeader(request.getType(),SUCCESS, 0);
        return response;
    }
}
