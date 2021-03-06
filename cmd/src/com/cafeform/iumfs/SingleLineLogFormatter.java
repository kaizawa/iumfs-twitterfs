package com.cafeform.iumfs;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public final class SingleLineLogFormatter extends Formatter {

    /**
     * Setting log format
     * @param rec
     * @return 
     */    
    @Override
    public synchronized String format(final LogRecord rec) 
    {
        StringBuilder line =  new StringBuilder();
        

        
        line.append(String.format("%tD %<tT.%<tL", rec.getMillis()));
        line.append(" ");
        line.append(rec.getThreadID());
        line.append(" ");
        line.append(rec.getLevel().toString());
        line.append(" ");
        int i = rec.getSourceClassName().lastIndexOf('.');        
        if (i > 0)
        {
            line.append(rec.getSourceClassName().substring(i + 1));
        }
        else
        {
            line.append(rec.getSourceClassName()); 
        }
        line.append(".");
        line.append(rec.getSourceMethodName());
        line.append(": ");        
        line.append(rec.getMessage());
        line.append("\n");        
        Throwable throwable = rec.getThrown();        
        if (null != throwable)
        {
            line.append(throwable.toString());
            line.append("\n");                
            if(null != throwable.getCause())
            {
                line. append("Nested exception:");
                line.append(throwable.getCause().toString());
                line.append("\n");         
            }
            for(StackTraceElement element : throwable.getStackTrace())
            {
                line.append(element.getClassName());
                line.append(".");
                line.append(element.getMethodName());
                line.append("\n");                
            }
        }
        return line.toString();
    }
}