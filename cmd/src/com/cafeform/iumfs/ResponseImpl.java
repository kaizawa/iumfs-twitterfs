package com.cafeform.iumfs;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author kaizawa
 */
public class ResponseImpl implements Response
{
    private RequestType requestType = null;
    private long result = 0;
    final public static int RESPONSE_HEADER_SIZE = 24; // long x 3
    protected ByteBuffer buffer;
    
    public ResponseImpl (RequestType requestType, long result)
    {
        this();
        this.requestType = requestType;
        setResponseHeader(requestType, result, 0);
    }
    
    public ResponseImpl()
    {
        // write buffer for control device
        buffer = ByteBuffer.allocate(RequestImpl.DEVICE_BUFFER_SIZE); 
        buffer.order(ByteOrder.nativeOrder());
    }
    
    /**
     * Get a buffer which has response. Position is set to 0.<br/>
     * Limit won't be unchanged.
     * @return buffer
     */
    @Override
    public ByteBuffer getBuffer() 
    {
        buffer.rewind();
        return buffer;
    }
    
    /**
     * Set response header to buffer. limit is set to header plus datalen.<br/>
     * Position is set at the end of header.<br/>
     * @param requestType
     * @param result
     * @param datalen
     */
    @Override
    public final void setResponseHeader(
            RequestType requestType, 
            long result, 
            long datalen) 
    {
        this.result = result;
        /*
         * Response structure passed from damon to control device.
         *  8+8+8=24 bytes
         * typedef struct response
         * {
         *   int64_t            request_type; // request type
         *   int64_t            result;       // execution result of requestn
         *   int64_t            datasize; // size of data following this header.
         * } response_t;
         */
        buffer.clear();
        buffer.limit(RESPONSE_HEADER_SIZE + (int) datalen);
        buffer.putLong(requestType.longVal());
        buffer.putLong(result);
        buffer.putLong(datalen);
    }
    
    @Override
    public RequestType getRequestType ()
    {
        return requestType;
    }
    @Override
    public long getResult ()
    {
        return result;
    }
}
