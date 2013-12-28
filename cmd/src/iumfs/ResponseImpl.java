package iumfs;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author kaizawa
 */
public class ResponseImpl implements Response
{
    protected ByteBuffer wbbuf;
    
    public ResponseImpl (long requestType, long result)
    {
        this();
        setResponseHeader(requestType, result, 0);
    }
    
    public ResponseImpl()
    {
        // write buffer for control device
        wbbuf = ByteBuffer.allocate(RequestImpl.DEVICE_BUFFER_SIZE); 
        wbbuf.order(ByteOrder.nativeOrder());
    }
    
    /**
     * Get a buffer which has response. Position is set to 0.<br/>
     * Limit won't be unchanged.
     * @return buffer
     */
    @Override
    public ByteBuffer getBuffer() 
    {
        wbbuf.rewind();
        return wbbuf;
    }
    
    /**
     * Set response header to buffer. limit is set to header plus datalen.<br/>
     * Position is set at the end of header.<br/>
     * @param requestType
     * @param result
     * @param datalen
     */
    @Override
    public final void setResponseHeader(long requestType, long result, long datalen) 
    {
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
        wbbuf.clear();
        wbbuf.limit(RequestImpl.RESPONSE_HEADER_SIZE + (int) datalen);
        wbbuf.putLong(requestType);
        wbbuf.putLong(result);
        wbbuf.putLong(datalen);
    }
}
