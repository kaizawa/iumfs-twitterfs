package iumfs;

import static iumfs.RequestImpl.*;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import static org.junit.Assert.fail;

/**
 *
 * @author kaizawa
 */
public class Util 
{
    public static final String PATHNAME1 = "/dir1/file1";
    public static final String USER1 = "user1";
    public static final String PASS1 = "pass1";
    public static final String SERVER1 = "server1";
    public static final String BASEPATH1 = "/export";
    public static final byte DATA1[] = {0x01, 0x02, 0x03};

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
    public static ByteBuffer createMockBuffer(
            RequestType type,
            long size,
            long offset,
            long datasize,
            long flag,
            String pathname,
            String basepath,
            String server,            
            String user,
            String pass,
            byte[] data
    )
    {
        ByteBuffer buf = ByteBuffer.allocate(RequestImpl.DEVICE_BUFFER_SIZE);
        buf.putLong(type.longVal()); // type
        buf.putLong(size);
        buf.putLong(offset);
        buf.putLong(datasize);
        buf.putLong(flag);
        buf.put(stringToByteArray(pathname, IUMFS_MAXPATHLEN));
        buf.put(stringToByteArray(basepath, IUMFS_MAXPATHLEN));        
        buf.put(stringToByteArray(server, MAX_SERVER_LEN));        
        buf.put(stringToByteArray(user, MAX_USER_LEN));
        buf.put(stringToByteArray(pass, MAX_PASS_LEN));
        buf.put(data);
        return buf;
    }

    public static byte[] stringToByteArray (String text, int size)
    {
        byte bytes[] = new byte[size];
        try
        {
            int index = 0;
            for (byte b : text.getBytes("UTF-8"))
            {
                if (index > size)
                {
                    fail("text too long");
                }
                bytes[index++] = b;
            }
        } catch (UnsupportedEncodingException ex)
        {
            fail("Unsupported Encoding");
        }
        return bytes;
    }
    
    public static long getRequestType (Response response)
    {
        ByteBuffer buf = response.getBuffer();
        buf.position(0);
        return buf.getLong();
    }
    public static long getResult (Response response)
    {
        ByteBuffer buf = response.getBuffer();
        buf.position(8);
        return buf.getLong();
    }
    
    public static long getDataSize (Response response)
    {
        ByteBuffer buf = response.getBuffer();
        buf.position(16);
        return buf.getLong();
    }
    
    public static byte[] getData (Response response)
    {
        ByteBuffer buf = response.getBuffer();        
        long datasize = getDataSize(response);
        buf.position(32);
        byte[] bytes = new byte[(int)datasize];
        buf.get(bytes);
        return bytes;
    }
}
