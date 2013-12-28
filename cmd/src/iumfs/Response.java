package iumfs;

import java.nio.ByteBuffer;

/**
 * Response to return to controll device
 */
public interface Response {
    public ByteBuffer getBuffer();
    public void setResponseHeader(long requestType, long result, long datalen);
}
