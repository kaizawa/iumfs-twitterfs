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
package iumfs.twitterfs;

import iumfs.Request;
import iumfs.RequestFactory;
import iumfs.UnknownRequestException;
import java.nio.BufferUnderflowException;
import java.util.logging.Logger;

/**
 * <p>デバイスドライバから受け取った データを元に、適切なリクエストクラスの
 * インスタンスを返すファクトリ</p>
 * TODO: Instance をプールしておいて、効率的に利用する
 */
public class TwitterfsRequestFactory extends RequestFactory {

    private static Logger logger = Logger.getLogger(Main.class.getName());

    public Request createInstance(long request_type) {

        /*
         *  TODO: リクエストによって確保する Buffer のサイズを可変にしてもいいかも..
         *  そうすると使いまわしはできなくなるが？
         */
        try {
            Request req = null;
            switch ((int) request_type) {
                case TwitterfsRequest.READ_REQUEST:
                    req = new TwitterfsReadRequest();
                    break;
                case TwitterfsRequest.READDIR_REQUEST:
                    req = new TwitterfsReadDirRequest();
                    break;
                case TwitterfsRequest.GETATTR_REQUEST:
                    req = new TwitterfsGetAttrRequest();
                    break;
                case TwitterfsRequest.WRITE_REQUEST:
                    req = new TwitterfsWriteRequest();
                    break;
                case TwitterfsRequest.CREATE_REQUEST:
                    req = new TwitterfsCreateRequest();
                    break;
                case TwitterfsRequest.REMOVE_REQUEST:
                    req = new TwitterfsRemoveRequest();
                    break;
                case TwitterfsRequest.MKDIR_REQUEST:
                    req = new TwitterfsMkdirRequest();
                    break;
                case TwitterfsRequest.RMDIR_REQUEST:
                    req = new TwitterfsRmdirRequest();
                    break;
                default:
                    logger.warning("Unknown request: " + request_type);
                    throw new UnknownRequestException();
            }
            return req;
        } catch (BufferUnderflowException ex) {
            ex.printStackTrace();
            System.exit(1);
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        return null;
    }
}
