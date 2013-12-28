package iumfs;

/**
 *
 */
public enum FileType 
{    
    VREG(1),
    VDIR(2);
    
    private final long type;    
    
    private FileType(long type)
    {
        this.type = type;
    }
    
    public static FileType getType (long fileType)
    {
        switch ((int)fileType)
        {
            case(2):
                return VDIR;
            case(1):                
            default:
                return VREG;
        }
    }
    
    public long longVal () 
    {
        return type;
    }
}
