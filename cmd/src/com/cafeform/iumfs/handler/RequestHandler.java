package com.cafeform.iumfs.handler;

import com.cafeform.iumfs.IumfsFile;
import com.cafeform.iumfs.Request;
import com.cafeform.iumfs.Response;

/**
 *  Handler interface which handle request from control device
 */
public interface RequestHandler 
{
    public Response getResponse (Request request, IumfsFile file);
}
