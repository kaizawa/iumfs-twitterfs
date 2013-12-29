package iumfs.handler;

import iumfs.IumfsFile;
import iumfs.Request;
import iumfs.Response;

/**
 *  Handler interface which handle request from control device
 */
public interface RequestHandler 
{
    public Response getResponse (Request request, IumfsFile file);
}
