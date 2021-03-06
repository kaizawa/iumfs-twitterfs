package com.cafeform.iumfs;

import java.nio.ByteBuffer;

/**
 * Response to return to controll device
 */
public interface Response {
    public ByteBuffer getBuffer();
    public void setResponseHeader(RequestType requestType, long result, long datalen);
    public RequestType getRequestType ();
    public long getResult ();
}
