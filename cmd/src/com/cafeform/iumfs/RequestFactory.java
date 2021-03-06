/*
 * Copyright 2011 Kazuyoshi Aizawa
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
package com.cafeform.iumfs;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Logger;

/**
 * <p>
 * Return appropreate Request class based on the request 
 * from control device driver.
 * Implementation must exist every file system type.
 * </p>
 * TODO: should implement instance pool for better performance?
 */
public class RequestFactory 
{
    private static final Logger logger = 
            Logger.getLogger(RequestFactory.class.getName());
    
    public static Request createRequest(ByteBuffer buf) 
    {
        /*
         * Request structure which will be given by control device.
         * 
         *  8+1184+1024+8+8+8=2240 bytes
         * typedef struct request
         * {
         *   int64_t             request_type; // request type
         *   int64_t             size;     // data size 
         *   int64_t             offset;   // off set of file
         *   int64_t             datasize; // size of data following this header
         *   int64_t             flags;    // for ioflag...not used yet.
         *   char                pathname[IUMFS_MAXPATHLEN]; // File path name
         *   iumfs_mount_opts_t  mountopts[1]; // arguments of mount command
         * }
         * 
         * The size of each member of this structure is multiple of 8.
         * So the total structure size must be also mutiple of 8.

         *  40+40+80+1024=1184 bytes
         *
         * typedef struct iumfs_mount_opts
         * {
         *    char basepath[IUMFS_MAXPATHLEN];        
         *    char server[MAX_SERVER_NAME];
         *    char user[MAX_USER_LEN];
         *    char pass[MAX_PASS_LEN];        
         * } iumfs_mount_opts_t;
         */
        Request request = new RequestImpl();
        int bufsize = buf.capacity();
        long request_type; 
        long size;
        long offset;
        long datasize;
        long flags;
        byte pathname[] = new byte[RequestImpl.IUMFS_MAXPATHLEN]; // Ffile pathname
        byte basepath[] = new byte[RequestImpl.IUMFS_MAXPATHLEN]; // Base path when mounting.
        byte server[] = new byte[RequestImpl.MAX_SERVER_LEN]; // Server name(Optiona)
        byte username[] = new byte[RequestImpl.MAX_USER_LEN]; // User name(Optional)
        byte password[] = new byte[RequestImpl.MAX_PASS_LEN]; // Passowrd (Optional)
        byte data[]; // additional data for this request, if any.

        try {
            buf.rewind();
            logger.finer("Buf info pos=" + buf.position() + " limit=" + buf.limit());
            /*
             * Read each structure members from ByteBuffer.
             */
            request_type = buf.getLong();
            size = buf.getLong();
            offset = buf.getLong();
            datasize = buf.getLong();
            flags = buf.getLong();
            buf.get(pathname);
            buf.get(basepath);
            buf.get(server);
            buf.get(username);
            buf.get(password);
            
            RequestType type = RequestType.getType(request_type);
            
            logger.finer("request_type=" + type + ", size=" + size + 
                    ", offset=" + offset + ", datasize=" + datasize);
            logger.finer("basename=" + (new String(basepath)).trim());
            logger.finest("ByteOrder=" + ByteOrder.nativeOrder());
            
            logger.fine("request=" + type + ", pathname=" +
                    (new String(pathname)).trim() + ", username=" + 
                    new String(username).trim());             

            request.setOffset(offset);
            request.setDataSize(datasize);            
            request.setPathname((new String(pathname)).trim()); // remove space
            request.setBasepath((new String(basepath)).trim()); // remove space
            request.setServer((new String(server)).trim()); // remove space
            request.setType(type);
            request.setSize(size);
            request.setFlags(flags);
            request.setUserName((new String(username)).trim());
            request.setPassword((new String(password)).trim());

            /*
             * Check if structure followed by data.
             * size won't exceed DEVICE_BUFFER_SIZE - REQUEST_HEADER_SIZE
             */
            if(size > 0){
                data = new byte[Math.min((int)size, 
                        RequestImpl.DEVICE_BUFFER_SIZE - RequestImpl.REQUEST_HEADER_SIZE) ];
                buf.get(data);
                request.setData(data);
            }           
            return request;
        } 
        catch (BufferUnderflowException | IndexOutOfBoundsException ex) 
        {
            System.exit(1);
        }
        return null;
    }
}
