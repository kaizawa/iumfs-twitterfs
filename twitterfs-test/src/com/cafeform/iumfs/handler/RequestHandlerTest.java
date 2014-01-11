/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cafeform.iumfs.handler;

import static com.cafeform.iumfs.FileType.VREG;
import com.cafeform.iumfs.TestUtil;
import com.cafeform.iumfs.IumfsFile;
import com.cafeform.iumfs.Request;
import com.cafeform.iumfs.RequestFactory;
import com.cafeform.iumfs.RequestHandlerFactory;
import com.cafeform.iumfs.RequestType;
import static com.cafeform.iumfs.RequestType.*;
import com.cafeform.iumfs.Response;
import com.cafeform.iumfs.ResponseImpl;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.cafeform.iumfs.TestUtil.*;
import java.nio.ByteBuffer;
import static com.cafeform.iumfs.handler.AbstractRequestHandler.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.mockito.*;

/**
 *
 * @author kaizawa
 */
public class RequestHandlerTest
{

    RequestHandlerFactory handlerFactory;
    RequestHandler handler;
    Request request;

    @Before
    public void setUp()
    {
        handlerFactory = new RequestHandlerFactory();
    }
    
    @Test 
    public void testAllRequestHandler ()
    {
        for(RequestType requestType : RequestType.values())
        {
            testSimpleData(requestType);
        }
    }
    
    /**
     * Test request type in Response 
     * @param requestType 
     */
    private void testSimpleData (RequestType requestType)
    {
        handler = handlerFactory.getHandler(requestType);
        request = RequestFactory.createRequest(
                TestUtil.createMockBuffer(
                        requestType, //type
                        0, //size
                        0, //offset
                        0, //datasize
                        0, //flags
                        PATHNAME1, //pathname
                        BASEPATH1, //base path                        
                        SERVER1, //server                        
                        USER1, //user
                        PASS1, //password
                        new byte[0]
                )
        );
        
        System.out.println(requestType + " " + requestType.longVal());
        
        IumfsFile file = mock(IumfsFile.class);
        when(file.exists()).thenReturn(true);
        when(file.listFiles()).thenReturn(new IumfsFile[0]);
        when(file.getFileType()).thenReturn(VREG);

        Response response = handler.getResponse(request, file);
        System.out.println (getRequestType(response));
        System.out.println (getRequestType(response));
        ByteBuffer buf = response.getBuffer();
        
        System.out.println("Bufer position = " + buf.position());
        System.out.println("Bufer limit = " + buf.limit());        

        for(int i = 0 ; i < buf.limit() ; i++)
        {
            System.out.print(buf.get(i) + " ");
        }
        assertEquals(requestType.longVal(), getRequestType(response));
    }
    

    @Test
    public void testCreateRequestHandler ()
    {
        RequestType requestType = CREATE_REQUEST;
        handler = handlerFactory.getHandler(requestType);
        request = RequestFactory.createRequest(
                TestUtil.createMockBuffer(
                        requestType, //type
                        0, //size
                        0, //offset
                        0, //datasize
                        0, //flags
                        PATHNAME1, //pathname
                        BASEPATH1, //base path                        
                        SERVER1, //server                        
                        USER1, //user
                        PASS1, //password
                        new byte[0]
                )
        );
        
        IumfsFile file = mock(IumfsFile.class);
        when(file.exists()).thenReturn(false);
               
        Response response = handler.getResponse(request, file);
        System.out.println (getRequestType(response));
        System.out.println (getRequestType(response));
        ByteBuffer buf = response.getBuffer();
        assertEquals(requestType.longVal(), getRequestType(response));
        assertEquals(SUCCESS, getResult(response));        
    }
    
    @Test
    public void testGetAttrRequestHandler()
    {
        long PERMISSION = 777;
        long SIZE = 100;
        RequestType requestType = GETATTR_REQUEST;
        handler = handlerFactory.getHandler(requestType);
        request = RequestFactory.createRequest (
                TestUtil.createMockBuffer(
                        requestType, //type
                        0, //size
                        0, //offset
                        0, //datasize
                        0, //flags
                        PATHNAME1, //pathname
                        BASEPATH1, //base path                        
                        SERVER1, //server                        
                        USER1, //user
                        PASS1, //password
                        new byte[0]
                )
        );
        
        IumfsFile file = mock(IumfsFile.class);
        when(file.exists()).thenReturn(true);
        when(file.getMtime()).thenReturn(1L);            
        when(file.getAtime()).thenReturn(2L);
        when(file.getCtime()).thenReturn(3L);        
        when(file.getPermission()).thenReturn(PERMISSION);
        when(file.length()).thenReturn(SIZE);
        when(file.getFileType()).thenReturn(VREG);
        Response response = handler.getResponse(request, file);
        System.out.println (getRequestType(response));
        System.out.println (getRequestType(response));
        ByteBuffer buf = response.getBuffer();
        
        assertEquals(requestType.longVal(), getRequestType(response));
        assertEquals(SUCCESS, getResult(response));                
        assertEquals(72, getDataSize(response)); 
        
        buf.position(ResponseImpl.RESPONSE_HEADER_SIZE);
        
        assertEquals(PERMISSION, buf.getLong());
        assertEquals(SIZE, buf.getLong());        
        assertEquals(VREG.longVal(), buf.getLong());                
        assertEquals(0, buf.getLong());        
        assertEquals(1000, buf.getLong());  
        assertEquals(0, buf.getLong());
        assertEquals(2000, buf.getLong());     
        assertEquals(0, buf.getLong());
        assertEquals(3000, buf.getLong());             
    }
    
    @Test
    public void testMkdirRequestHandler()
    {

        RequestType requestType = MKDIR_REQUEST;
        handler = handlerFactory.getHandler(requestType);
        request = RequestFactory.createRequest(
                TestUtil.createMockBuffer(
                        requestType, //type
                        0, //size
                        0, //offset
                        0, //datasize
                        0, //flags
                        PATHNAME1, //pathname
                        BASEPATH1, //base path                        
                        SERVER1, //server                        
                        USER1, //user
                        PASS1, //password
                        new byte[0]
                )
        );
        
        IumfsFile file = mock(IumfsFile.class);
        when(file.exists()).thenReturn(false);
        Response response = handler.getResponse(request, file);
        ByteBuffer buf = response.getBuffer();
        assertEquals(requestType.longVal(), getRequestType(response));
        assertEquals(SUCCESS, getResult(response));                
    }
    
    @Test
    public void testReadDirRequestHandler() throws UnsupportedEncodingException
    {
        long SIZE = 100;
        
        RequestType requestType = READDIR_REQUEST;
        handler = handlerFactory.getHandler(requestType);
        request = RequestFactory.createRequest (
                TestUtil.createMockBuffer (
                        requestType, //type
                        0, //size
                        0, //offset
                        0, //datasize
                        0, //flags
                        PATHNAME1, //pathname
                        BASEPATH1, //base path                        
                        SERVER1, //server                        
                        USER1, //user
                        PASS1, //password
                        new byte[0]
                )
        );
        
        IumfsFile file = mock(IumfsFile.class);
        when(file.exists()).thenReturn(true);
        when(file.getName()).thenReturn("hoge");
        when(file.getPath()).thenReturn("/var/tmp/hoge");
        IumfsFile[] fileArray = {file};
        when(file.listFiles()).thenReturn(fileArray);
                
        Response response = handler.getResponse(request, file);
        ByteBuffer buf = response.getBuffer();

        assertEquals(requestType.longVal(), getRequestType(response));
        assertEquals(SUCCESS, getResult(response));          
        assertEquals(16, getDataSize(response)); 
        buf.position(ResponseImpl.RESPONSE_HEADER_SIZE);
        assertEquals(16, buf.getLong());
    }    
    
    @Test
    public void testReadRequestHandler() throws IOException
    {
        final String TEXT = "hehehe";
        RequestType requestType = READ_REQUEST;
        handler = handlerFactory.getHandler(requestType);
        request = RequestFactory.createRequest(
                TestUtil.createMockBuffer(
                        requestType, //type
                        TEXT.length(), //size
                        0, //offset
                        (TEXT.length() / 8 + 1) * 8, //datasize
                        0, //flags
                        PATHNAME1, //pathname
                        BASEPATH1, //base path                        
                        SERVER1, //server                        
                        USER1, //user
                        PASS1, //password
                        new byte[0]
                )
        );
        ByteBuffer dataBuffer = ByteBuffer.allocate(100);
        dataBuffer.put(TEXT.getBytes("UTF-8"));
        
        ArgumentCaptor<ByteBuffer> mockByteBuffer = ArgumentCaptor.forClass(ByteBuffer.class);
        IumfsFile file = mock(IumfsFile.class);
        when(file.exists()).thenReturn(true);
        when(file.read(mockByteBuffer.capture(), Mockito.anyLong(), Mockito.anyLong())).thenReturn((long)TEXT.length());
        
        Response response = handler.getResponse(request, file);
        ByteBuffer buf = response.getBuffer();
        assertEquals(requestType.longVal(), getRequestType(response));
        assertEquals(SUCCESS, getResult(response));          
        assertEquals(TEXT.length(), getDataSize(response));                  
        buf.position(ResponseImpl.RESPONSE_HEADER_SIZE);
    }
    
    @Test
    public void testRemoveRequestHandler()
    {
        RequestType requestType = REMOVE_REQUEST;
        handler = handlerFactory.getHandler(requestType);
        request = RequestFactory.createRequest(
                TestUtil.createMockBuffer(
                        requestType, //type
                        0, //size
                        0, //offset
                        0, //datasize
                        0, //flags
                        PATHNAME1, //pathname
                        BASEPATH1, //base path                        
                        SERVER1, //server                        
                        USER1, //user
                        PASS1, //password
                        new byte[0]
                )
        );
        
        IumfsFile file = mock(IumfsFile.class);
        when(file.exists()).thenReturn(true);
        Response response = handler.getResponse(request, file);
        ByteBuffer buf = response.getBuffer();
        assertEquals(requestType.longVal(), getRequestType(response));
        assertEquals(SUCCESS, getResult(response));          
    }
    
    @Test
    public void testRmdirRequestHandler()
    {
        RequestType requestType = RMDIR_REQUEST;
        handler = handlerFactory.getHandler(requestType);
        request = RequestFactory.createRequest(
                TestUtil.createMockBuffer(
                        requestType, //type
                        0, //size
                        0, //offset
                        0, //datasize
                        0, //flags
                        PATHNAME1, //pathname
                        BASEPATH1, //base path                        
                        SERVER1, //server                        
                        USER1, //user
                        PASS1, //password
                        new byte[0]
                )
        );
        
        IumfsFile file = mock(IumfsFile.class);
        when(file.exists()).thenReturn(true);
        when(file.isDirectory()).thenReturn(true);
        when(file.delete()).thenReturn(true);
        Response response = handler.getResponse(request, file);
        ByteBuffer buf = response.getBuffer();
        assertEquals(requestType.longVal(), getRequestType(response));
        assertEquals(SUCCESS, getResult(response));
    }
    
    @Test
    public void testWriteRequestHandler()
    {

        RequestType requestType = WRITE_REQUEST;
        handler = handlerFactory.getHandler(requestType);
        request = RequestFactory.createRequest(
                TestUtil.createMockBuffer(
                        requestType, //type
                        0, //size
                        0, //offset
                        0, //datasize
                        0, //flags
                        PATHNAME1, //pathname
                        BASEPATH1, //base path                        
                        SERVER1, //server                        
                        USER1, //user
                        PASS1, //password
                        new byte[0]
                )
        );
        IumfsFile file = mock(IumfsFile.class);
        when(file.exists()).thenReturn(true);
        Response response = handler.getResponse(request, file);
        ByteBuffer buf = response.getBuffer();
        assertEquals(requestType.longVal(), getRequestType(response));
        assertEquals(SUCCESS, getResult(response));
    }
}
