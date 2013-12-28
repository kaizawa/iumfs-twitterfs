package iumfs;

/**
 *
 */
public enum RequestType 
{    
    READ_REQUEST(1),
    READDIR_REQUEST(2),
    GETATTR_REQUEST(3),
    WRITE_REQUEST(4),
    CREATE_REQUEST(5),
    REMOVE_REQUEST(6),
    MKDIR_REQUEST(7),
    RMDIR_REQUEST(8);
    
    private final long type;    
    
    private RequestType(long type)
    {
        this.type = type;
    }
    
    public static RequestType getType (long requestType)
    {
        switch ((int)requestType)
        {
            case(1):
                return READ_REQUEST;
            case(2):
                return READDIR_REQUEST;
            case(3):
                return GETATTR_REQUEST;
            case(4):
                return WRITE_REQUEST;
            case(5):
                return CREATE_REQUEST;
            case(6):
                return REMOVE_REQUEST;
            case(7):
                return MKDIR_REQUEST;
            case(8):
                return RMDIR_REQUEST; 
            default:
                throw new UnknownRequestException();
        }
    }
    
    public long longVal () 
    {
        return type;
    }
}
