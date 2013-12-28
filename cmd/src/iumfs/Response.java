package iumfs;

import java.nio.ByteBuffer;

/**
 * Response to return to controll device
 */
public interface Response {
    public ByteBuffer getBuffer();
    public void setResponseHeader(RequestType requestType, long result, long datalen);
}
