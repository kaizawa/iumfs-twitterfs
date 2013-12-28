package iumfs;

import iumfs.handler.CreateRequestHandler;
import iumfs.handler.GetAttrRequestHandler;
import iumfs.handler.MkdirRequestHandler;
import iumfs.handler.WriteRequestHandler;
import iumfs.handler.RmdirRequestHandler;
import iumfs.handler.RemoveRequestHandler;
import iumfs.handler.ReadDirRequestHandler;
import iumfs.handler.ReadRequestHandler;
import iumfs.handler.RequestHandler;
import java.nio.BufferUnderflowException;
import java.util.logging.Logger;

/**
 * Factory of RequestHander which corresponds to the request from 
 * controll device
 */
public class RequestHandlerFactory 
{
    private static final Logger logger = Logger.getLogger(RequestHandlerFactory.class.getName());
    private ReadRequestHandler readRequestHander;
    private ReadDirRequestHandler readDirRequestHandler;
    private GetAttrRequestHandler getAttrRequestHandler;
    private WriteRequestHandler writeRequestHandler;
    private CreateRequestHandler createRequestHandler;
    private RemoveRequestHandler removeRequestHandler;
    private MkdirRequestHandler mkdirRequestHandler;
    private RmdirRequestHandler rmdirRequestHandler;

    public RequestHandler getHandler (long request_type)
    {
        RequestHandler handler;

            switch ((int) request_type) {
                case RequestImpl.READ_REQUEST:
                    if (null == readRequestHander) 
                    {
                        readRequestHander = new ReadRequestHandler();
                    }
                    handler = readRequestHander;
                    break;
                case RequestImpl.READDIR_REQUEST:
                    if (null == readDirRequestHandler)
                    {
                        readDirRequestHandler = new ReadDirRequestHandler();
                    }
                    handler = readDirRequestHandler;
                    break;
                case RequestImpl.GETATTR_REQUEST:
                    if (null == getAttrRequestHandler)
                    {
                        getAttrRequestHandler = new GetAttrRequestHandler();                        
                    }
                    handler = getAttrRequestHandler;
                    break;
                case RequestImpl.WRITE_REQUEST:
                    if (null == writeRequestHandler)
                    {
                        writeRequestHandler = new WriteRequestHandler();
                    }
                    handler = writeRequestHandler;
                    break;
                case RequestImpl.CREATE_REQUEST:
                    if (null == createRequestHandler)
                    {
                        createRequestHandler = new CreateRequestHandler();
                    }
                    handler = createRequestHandler;
                    break;
                case RequestImpl.REMOVE_REQUEST:
                    if (null == removeRequestHandler)
                    {
                        removeRequestHandler = new RemoveRequestHandler();
                    }
                    handler = removeRequestHandler;
                    break;
                case RequestImpl.MKDIR_REQUEST:
                    if (null == mkdirRequestHandler)
                    {
                        mkdirRequestHandler = new MkdirRequestHandler();
                    }
                    handler = mkdirRequestHandler;
                    break;
                case RequestImpl.RMDIR_REQUEST:
                    if (null == rmdirRequestHandler)
                    {
                        rmdirRequestHandler = new RmdirRequestHandler();
                    }
                    handler = rmdirRequestHandler;
                    break;
                default:
                    logger.warning("Unknown request: " + request_type);
                    throw new UnknownRequestException();
            }
            return handler;
    } 
}
