package com.cafeform.iumfs.handler;

import com.cafeform.iumfs.Util;
import com.cafeform.iumfs.Request;
import com.cafeform.iumfs.RequestFactory;
import static com.cafeform.iumfs.RequestType.GETATTR_REQUEST;
import static com.cafeform.iumfs.Util.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author kaizawa
 */
public class RequestFactoryTest 
{
    Request request;

    private static final long SIZE = 3;
    private static final long OFFSET = 100;
    private static final long DATASIZE = 8;
    private static final long FLAGS = 5555;
    private static final byte DATA1[] = {0x01, 0x02, 0x03};

    /**
     * Test of getResponse method, of class GetAttrRequestHandler.
     */
    @Test
    public void testRequestFactory()
    {
        request = RequestFactory.createRequest(
                Util.createMockBuffer(
                        GETATTR_REQUEST, //type
                        SIZE, //size
                        OFFSET, //offset
                        DATASIZE, //datasize
                        FLAGS, //flags
                        PATHNAME1, //pathname
                        BASEPATH1, //base path                        
                        SERVER1, //server                        
                        USER1, //user
                        PASS1, //password
                        DATA1
                )
        );
        assertEquals(SIZE, request.getSize());
        assertEquals(OFFSET, request.getOffset());
        assertEquals(DATASIZE, request.getDataSize());
        assertEquals(GETATTR_REQUEST, request.getType());
        assertEquals(PATHNAME1, request.getPathname());
        assertEquals(USER1, request.getUserName());
        assertEquals(PASS1, request.getPassword());
        assertEquals(SERVER1, request.getServer());
        assertEquals(BASEPATH1, request.getBasepath());
        assertArrayEquals(DATA1, request.getData());
    }
}
