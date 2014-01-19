/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cafeform.iumfs.twitterfs.files;

import com.cafeform.iumfs.IumfsFile;
import com.cafeform.iumfs.NotADirectoryException;
import com.cafeform.iumfs.NotSupportedException;
import com.cafeform.iumfs.twitterfs.Account;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import static java.util.logging.Level.*;
import java.util.logging.Logger;

/**
 *
 * @author ka78231
 */
public class SetupFile extends TwitterFsFileImpl {
    private static final Logger logger = 
            Logger.getLogger(SetupFile.class.getName());
    private String script;

    public SetupFile(Account account, String pathname)
    {
        super(account, pathname);
        setScript("#!/bin/sh\n# Do not edit this file.\n" 
                + "java -cp " +  System.getProperty("java.class.path")
                + " \"$@\" " // java options, if any
                + " com.cafeform.iumfs.twitterfs.Setup " + account.getUsername() + "\n"
                + "if [ $? != 0 ]; then\n" 
                + "     echo \"\ntry\n    $ setup -Dhttp.proxyHost=<proxy_address> -Dhttp.proxyPort=<proxy_port>\" \n"
                + "fi \n"
                );
    }

    @Override
    public long getPermission() {
        return (long) 0100555; // executable
    }

    @Override
    public long read(ByteBuffer buf, long size, long offset) throws UnsupportedEncodingException {
        byte[] bytes = getScript().getBytes("UTF-8");
        /*
         * バッファに書き込む
         */
        buf.put(bytes, 0, bytes.length);        
        return bytes.length;
    }

    @Override
    public long write(byte[] buf, long size, long offset) {
        throw new NotSupportedException();
    }

    /**
     * @return the script
     */
    public String getScript() {
        return script;
    }

    /**
     * @param script the script to set
     */
    private void setScript(String script) {
        this.script = script;
        try {
            byte[] bytes = script.getBytes("UTF-8");        
            setLength(bytes.length);            
        } catch (UnsupportedEncodingException ex) {
            logger.log(SEVERE, getUserAndName() + " " + ex.getMessage());
        }        
    }

    @Override
    public void addFile(IumfsFile file) throws NotADirectoryException
    {
        throw new NotADirectoryException();
    }

    @Override
    public IumfsFile[] listFiles()
    {
        throw new NotADirectoryException();
    }
}
