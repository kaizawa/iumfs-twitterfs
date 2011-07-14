/*
 * Copyright 2010 Kazuyoshi Aizawa
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
package iumfs;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class File {

    private String name;
    private boolean istimeline;
    private long file_size = 0;
    private long last_id = 0;
    private long base_id = 0;
    private long since_id = 0;
    protected static Logger logger = Logger.getLogger(twitterfsd.class.getName());
    private List<Status> status_list = new ArrayList<Status>();
    private static final int MAX_STATUSES = 200;

    File(String name, boolean istimeline) {
        this.name = name;
        this.istimeline = istimeline;
        init();
    }

    private void init() {
        ResponseList<Status> statuses;
        Twitter twitter = TWFactory.getInstance();
        try {
            /*
             * 最初の読み込み. 1ページ分(最大20件)だけうけとり、最も古いステータスを
             * 起点の ID(base_id)とする。
             */
            statuses = twitter.getHomeTimeline();
            for (Status status : statuses) {
                file_size += statusToFormattedString(status).length();
                status_list.add(0, status);
                base_id = last_id = status.getId();
            }
            logger.fine("Initial Read " + statuses.size() + " Statuses");
            logger.fine("File size is " + file_size);
        } catch (TwitterException ex) {
            Logger.getLogger(twitterfsd.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    public String getName() {
        return name;
    }

    public String getPathname() {
        return "/" + name;
    }

    public boolean isTimeline() {
        return istimeline;
    }

    public long getFileSize() {
        /*
         * タイムラインに相当するファイル以外は 0 を返す。
         */
        if (isTimeline() == false) {
            return 0L;
        }
        return file_size;
    }

    /**
     * Twitter から指定オフセットから指定バイト数分だけのステータス情報を得る。
     * オフセットの起点はデーモン起動時に取得した一番古いステータス
     * 
     * @param buf
     * @param size
     * @param offset
     * @return
     * @throws TwitterException
     * @throws UnsupportedEncodingException 
     */
    public long read(ByteBuffer buf, long size, long offset)
            throws TwitterException, UnsupportedEncodingException {
        long curr_size = 0;
        long curr_offset = 0;
        long prev_offset = 0;
        long rel_offset; // ステータス単位での相対オフセット
        int page = 0;

        Twitter twitter = TWFactory.getInstance();
        logger.fine("last_id is " + last_id);

        getTimeline(MAX_STATUSES, last_id);


        /*
         *  OLD                                       NEW
         * Status|Status|......|Status|Status|Status|Status|
         *                     
         * ------offset-----------><---size----->
         *                     ^
         *                     |prev_offset
         *                         ^
         *                         |curr_offset
         */
        for (Status status : status_list) {
            prev_offset = curr_offset;
            String str = statusToFormattedString(status);
            byte[] bytes = str.getBytes("UTF-8");
            long copy_size = 0;
            long status_length = bytes.length;            
            rel_offset = 0;

            logger.finer("ID: " + status.getId());
            logger.finer("status_length = " + status_length);
            logger.finest(str);

            // ステータスの文字数+1文字(改行)を足して現在のオフセットと考える。
            curr_offset += status_length;
            if (curr_offset < offset) {
                logger.fine("offset not yet reached");
                //まだオフセットに到達していない。
                continue;
            } else if (prev_offset >= offset) {
                logger.fine("prev_offset >= offset");
                /*
                 * すでにオフセットを越えている
                 * ステータス境界のオフセット値を0に。
                 */
                rel_offset = 0;
            } else {
                logger.fine("prev_offset < offset");
                /*
                 * このステータスでオフセットを越える。
                 * ステータス境界のオフセット値を計算
                 */
                rel_offset = offset - prev_offset;
            }
            logger.fine("rel_offset = " + rel_offset);

            if (curr_size + status_length >= size) {
                /*
                 * 必要サイズとステータスのサイズが同じ、もしくは大きすぎる。
                 * ステータス内で必要サイズ分だけバッファにコピーしてこれ以上
                 * は読み込まない。
                 */
                copy_size = size - curr_size - rel_offset;
                logger.fine("copy_Size = " + copy_size + ". No need to read more Status");
            } else {
                /*
                 * この時点でまだ必要サイズに満たない。すべてバッファにコピー
                 */
                copy_size = bytes.length - rel_offset;
                logger.fine("copy_Size = " + copy_size + ". need to read more Status");
            }
            /*
             * バッファに書き込む
             */
            buf.put(bytes, (int) rel_offset, (int) copy_size);
            buf.put("\n".getBytes("UTF-8"));
            curr_size += copy_size + "\n".getBytes("UTF-8").length;
            if (curr_size >= size) {
                logger.fine("currSize >= size");
                break;
            }
            logger.fine("currSize < size. continue for statement.");
        }
        return curr_offset;
    }

    /**
     * Status を指定フォーマットのテキストとして返す。
     * 今は改行を加えるだけ。
     * 
     * @param status ステータス。
     * @return フォーマットされたテキスト
     */
    public String statusToFormattedString(Status status) {
        /*
         * フォr-マットを追加。User 名や時間など。
         */
        StringBuffer sb = new StringBuffer();
        Date createdDate = status.getCreatedAt();  
        SimpleDateFormat simpleFormat = new SimpleDateFormat("MM/dd HH:mm:ss");  
  
        sb.append(simpleFormat.format(createdDate));
        sb.append(" [");
        sb.append(status.getUser().getName());
        sb.append("] ");
        sb.append(status.getText());
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Twitter からタイムラインを読み出す。
     * 実際の読み込みは getTimeline(int page, int count, long since) で読み込む。
     * 
     * @param count
     * @param since 
     */
    public void getTimeline(int count, long since) {
        int cnt = 0;
        int page = 1;// ページは 1 から始まる。
        do {
            cnt = getTimeline(page, count, since);
            page++;
        } while (cnt > 0);
    }

    /**
     * Twitter から指定ページ内のタイムラインを読み出す。
     * @param page
     * @param count
     * @param since
     * @return 
     */
    public int getTimeline(int page, int count, long since) {
        ResponseList<Status> statuses;
        Twitter twitter = TWFactory.getInstance();
        try {
            statuses = twitter.getHomeTimeline(new Paging(page, count, since));
            logger.fine("Got " + statuses.size() + " Statuses in page " + page);
            if (statuses.size() == 0) {
                // これ以上ステータスは無いようだ。
                return 0;
            }
            for (Status status : statuses) {
                file_size += statusToFormattedString(status).getBytes("UTF-8").length;
                status_list.add(0, status);
            }
            return statuses.size();
        } catch (TwitterException ex) {
            Logger.getLogger(File.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            System.exit(1);
            return 0;
        }
    }
}
