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
package iumfs.twitterfs;

import iumfs.File;
import iumfs.NotSupportedException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;

public class TwitterfsFile extends File {

    private static final String CONT = "(cont) ";
    private boolean istimeline;
    private long last_id = 0;
    private long base_id = 0;
    protected static final Logger logger = Logger.getLogger(Main.class.getName());
    private List<Status> status_list = new ArrayList<Status>();
    private static final int MAX_STATUSES = 200;
    private static final int MAX_PAGES = 5;
    private long interval = 0L;
    private boolean initial_read = true;
    private boolean stream_api = false; // Switch for use Stream API

    TwitterfsFile(String name, boolean istimeline, long interval) {
        super(name);
        this.istimeline = istimeline;
        this.interval = interval;
        init();
    }

    TwitterfsFile(String name, boolean istimeline, boolean stream_api) {
        super(name);
        this.istimeline = istimeline;
        this.stream_api = stream_api;
        init();
        startAutoUpdateThreads();
    }

    private void init() {
        /*
         * もしタイムラインファイルだったら最初に Twitter から読み込む。
         * (起点となる Status の ID(base_id) を得るため)
         */
        if (isTimeline()) {
            /*
             * 最初の読み込み. 1ページ分(最大20件)だけうけとる。
             */
            getTimeline(1, 20, 1);
        }
        Date now = new Date();
        setAtime(now.getTime());
        setCtime(now.getTime());
        setMtime(now.getTime());
    }

    private void startAutoUpdateThreads() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        StatusListener listener = new UserStreamListener() {

            @Override
            public void onStatus(Status status) {
                try {
                    logger.finer("Read Status id=" + status.getId());
                    logger.finest(TwitterfsFile.statusToFormattedString(status));
                    setFileSize(getFileSize() + TwitterfsFile.statusToFormattedString(status).getBytes("UTF-8").length);
                    addStatusToList(status);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
            @Override
            public void onException(Exception ex) {}
            @Override
            public void onScrubGeo(long l, long l1) {}
            @Override
            public void onDeletionNotice(long l, long l1) {}
            @Override
            public void onFriendList(long[] longs) {}
            @Override
            public void onFavorite(User user, User user1, Status status) {}
            @Override
            public void onUnfavorite(User user, User user1, Status status) {}
            @Override
            public void onFollow(User user, User user1) {}
            @Override
            public void onRetweet(User user, User user1, Status status) {}
            @Override
            public void onDirectMessage(DirectMessage dm) {}
            @Override
            public void onUserListMemberAddition(User user, User user1, UserList ul) {}
            @Override
            public void onUserListMemberDeletion(User user, User user1, UserList ul) {}
            @Override
            public void onUserListSubscription(User user, User user1, UserList ul) {}
            @Override
            public void onUserListUnsubscription(User user, User user1, UserList ul) {}
            @Override
            public void onUserListCreation(User user, UserList ul) {}
            @Override
            public void onUserListUpdate(User user, UserList ul) {}
            @Override
            public void onUserListDeletion(User user, UserList ul) {}
            @Override
            public void onUserProfileUpdate(User user) {}
            @Override
            public void onBlock(User user, User user1) {}
            @Override
            public void onUnblock(User user, User user1) {}
        };

        /*
         * タイムライン取得のスレッドを起動
         */
        if (isTimeline() == true) {
            if (isStream_api() == true) {
                /*
                 * Stream API (現在は HOME のみ)
                 * TwitterStream.sample() の中でスレッドが生成される
                 */
                TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
                twitterStream.setOAuthConsumer(Prefs.get("OAuthConsumerKey"), Prefs.get("consumerSecret"));
                twitterStream.setOAuthAccessToken(IumfsTwitterFactory.getAccessToken());
                twitterStream.addListener(listener);
                twitterStream.user();
            } else {
                /*
                 * 定期的に取得
                 */
                executor.scheduleAtFixedRate(new Runnable() {

                    public void run() {
                        getTimeline();
                    }
                }, getInterval(), getInterval(), TimeUnit.MILLISECONDS);
            }
        }
    }

    public String getPathname() {
        return "/" + getName();
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
     * list が更新されているときに読み込まれないよう synchronized にしている。
     * TODO: CopyOnWriteArrayListを使えばいいが、単純には sort ができなくので後で検討
     * 
     * @param buf
     * @param size
     * @param offset
     * @return
     * @throws TwitterException
     * @throws UnsupportedEncodingException 
     */
    @Override
    public synchronized long read(ByteBuffer buf, long size, long offset) {
        long curr_size = 0;
        long curr_offset = 0;
        long prev_offset = 0;
        long rel_offset; // ステータス単位での相対オフセット
        int page = 0;

        Twitter twitter = IumfsTwitterFactory.getInstance();

        /*
         * OLD                                                         NEW
         * Status             |Status                      |Status     
         * --------offset------------------->|<----------size------------->
         *                     
         * ----prev_offset--->|
         *                    |<-rel_offset->|
         *                    
         * ------------------curr_offset------------------>|
         *                                   |<-copy_size->|
         */
        for (Status status : status_list) {
            try {
                prev_offset = curr_offset;
                String str = statusToFormattedString(status);
                byte[] bytes = str.getBytes("UTF-8");
                long copy_size = 0;
                long status_length = bytes.length;
                rel_offset = 0;

                logger.finer("Read status_list id=" + status.getId() + " status_length = " + status_length);
                logger.finest(str);

                // 以前のオフセットにステータスの文字数を足して現在のオフセットと考える。
                curr_offset += status_length;
                if (curr_offset < offset) {
                    logger.finer("offset not yet reached");
                    //まだオフセットに到達していない。
                    continue;
                } else if (prev_offset >= offset) {
                    logger.finer("prev_offset >= offset");
                    /*
                     * すでにオフセットを越えている
                     * ステータス境界のオフセット値を0に。
                     */
                    rel_offset = 0;
                } else {
                    logger.finer("prev_offset < offset");
                    /*
                     * このステータスでオフセットを越える。
                     * ステータス境界のオフセット値を計算
                     */
                    rel_offset = offset - prev_offset;
                }
                logger.finer("rel_offset = " + rel_offset);

                if (curr_size + status_length >= size) {
                    /*
                     * 必要サイズとステータスのサイズが同じ、もしくは大きすぎる。
                     * ステータス内で必要サイズ分だけバッファにコピーしてこれ以上
                     * は読み込まない。
                     */
                    copy_size = size - curr_size - rel_offset;
                    logger.finer("copy_Size = " + copy_size + ". No need to read more Status");
                } else {
                    /*
                     * この時点でまだ必要サイズに満たない。すべてバッファにコピー
                     */
                    copy_size = bytes.length - rel_offset;
                    logger.finer("copy_Size = " + copy_size + ". need to read more Status");
                }
                /*
                 * バッファに書き込む
                 */
                buf.put(bytes, (int) rel_offset, (int) copy_size);
                curr_size += copy_size;
                if (curr_size >= size) {
                    logger.finer("currSize >= size");
                    break;
                }
                logger.finer("curr_size < size. continue for statement.");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(TwitterfsFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        logger.fine("curr_size = " + curr_size);
        return curr_size;
    }

    /**
     * Status を指定フォーマットのテキストとして返す。
     * 今は改行を加えるだけ。
     * 
     * @param status ステータス。
     * @return フォーマットされたテキスト
     */
    public static String statusToFormattedString(Status status) {
        /*
         * フォr-マットを追加。User 名や時間など。
         */
        StringBuilder sb = new StringBuilder();
        Date createdDate = status.getCreatedAt();
        SimpleDateFormat simpleFormat = new SimpleDateFormat("MM/dd HH:mm:ss");

        sb.append(simpleFormat.format(createdDate));
        sb.append(" ");
        sb.append(status.getUser().getScreenName());
        sb.append("[");
        sb.append(status.getUser().getName());
        sb.append("] \n");
        sb.append(status.getText());
        sb.append("\n\n");
        return sb.toString();
    }

    public void getTimeline() {
        getTimeline(MAX_STATUSES, last_id);
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
        /*
         * 最後に読み込んだステータスまで読み込む。ただし、最高 4 ページをとする。
         * public だと最大 80 ステータス。他は 800 ステータスに相当。
         *   20 * 4 = 80
         *   200 * 4 = 800
         * 特に public ではギャップが生じてしまうがしょうがない。(でないと、ずっと
         * 取得しつづけることになってしまう)
         */
        do {
            cnt = getTimeline(page, count, since);
            page++;
        } while ((cnt == count && page < MAX_PAGES) || (cnt == 20 && page < MAX_PAGES));
    }

    /**
     * Twitter から指定ページ内のタイムラインを読み出す。
     * リストを変更するので排他的に呼ばれる必要がある。
     * 
     * @param page
     * @param count
     * @param since
     * @return 
     */
    @SuppressWarnings({"LoggerStringConcat", "CallToThreadDumpStack"})
    synchronized public int getTimeline(int page, int count, long since) {
        ResponseList<Status> statuses = null;
        Twitter twitter = IumfsTwitterFactory.getInstance();
        String name = getName();
        try {
            String timeline = getName();
            if (name.equals("home")) {
                statuses = twitter.getHomeTimeline(new Paging(page, count, since));
            } else if (name.equals("mentions")) {
                statuses = twitter.getMentions(new Paging(page, count, since));
            } else if (name.equals("public")) {
                statuses = twitter.getPublicTimeline();
            } else if (name.equals("friends")) {
                statuses = twitter.getFriendsTimeline(new Paging(page, count, since));
            } else if (name.equals("retweeted_by_me")) {
                statuses = twitter.getRetweetedByMe(new Paging(page, count, since));
            } else if (name.equals("user")) {
                statuses = twitter.getUserTimeline(new Paging(page, count, since));
            } else if (name.equals("retweeted_to_me")) {
                statuses = twitter.getRetweetedToMe(new Paging(page, count, since));
            } else if (name.equals("retweets_of_me")) {
                statuses = twitter.getRetweetsOfMe(new Paging(page, count, since));
            } else {
                logger.severe("Unknown timeline(\"" + name + "\") specified.");
                System.exit(1);
            }
            logger.fine("Got " + name + " timeline, "
                    + statuses.size() + " Statuses in page " + page);

            if (statuses.size() == 0) {
                // これ以上ステータスは無いようだ。
                return 0;
            }
            //最初のステータス(最新)を最終読み込みステータスとする。            
            last_id = statuses.get(0).getId();
            for (Status status : statuses) {
                logger.finer("Read Status id=" + status.getId());
                logger.finest(statusToFormattedString(status));
                setFileSize(getFileSize() + statusToFormattedString(status).getBytes("UTF-8").length);
                status_list.add(status);
            }
            if (initial_read) {
                /*
                 * 一番最初の読み込み時の最も古い(リストの最後)ステータスを起点の 
                 * ID(base_id)とする。                            
                 */
                base_id = statuses.get(statuses.size() - 1).getId();
                logger.fine("base_id = " + base_id);
                initial_read = false;
            }

            logger.fine("new file_size is " + getFileSize());
            java.util.Collections.sort(status_list);
            /*
             * TL が更新されているので mtime, ctime(更新時間)を変更する。
             */
            Date now = new Date();
            setMtime(now.getTime());
            setCtime(now.getTime());
            return statuses.size();
        } catch (TwitterException ex) {
            logger.severe("Got Twitter Exception statusCode = " + ex.getStatusCode());
            ex.printStackTrace();
            return 0;
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            System.exit(1);
            return 0;
        }
    }

    /**
     * @return the interval
     */
    public long getInterval() {
        return interval;
    }

    /**
     * @param interval the interval to set
     */
    public void setInterval(long interval) {
        this.interval = interval;
    }

    /**
     * @return the stream_api
     */
    public boolean isStream_api() {
        return stream_api;
    }

    /**
     * @param stream_api the stream_api to set
     */
    public void setStream_api(boolean stream_api) {
        this.stream_api = stream_api;
    }

    synchronized public void addStatusToList(Status status) {
        try {
            logger.finer("Read Status id=" + status.getId());
            logger.finest(statusToFormattedString(status));
            setFileSize(getFileSize() + statusToFormattedString(status).getBytes("UTF-8").length);
            status_list.add(status);
            last_id = status.getId();

            logger.fine("new file_size is " + getFileSize());
            java.util.Collections.sort(status_list);
            /*
             * TL が更新されているので mtime, ctime(更新時間)を変更する。
             */
            Date now = new Date();
            setMtime(now.getTime());
            setCtime(now.getTime());
            return;
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            System.exit(1);
            return;
        }
    }

    @Override
    public long write(byte[] buf, long size, long offset)
            throws IOException, FileNotFoundException {
        try {
            Status status = null;

            if (getPathname().equals("/post") == false) {
                throw new NotSupportedException();
            }

            /*
             * ファイルとして書かれた文字列を得る。
             */
            Twitter twitter = IumfsTwitterFactory.getInstance();
            String whole_msg = new String(buf);
            logger.finer("Orig Text:" + whole_msg);
            logger.finest("whole_msg.length() = " + whole_msg.length());
            int left = whole_msg.length();
            /*
             * 文字列を 140 文字づつステータスとしてポスト.
             */
            MessageSeparator sep = new MessageSeparator(whole_msg);
            while (sep.hasNext()) {
                String msg = (String) sep.next();
                status = twitter.updateStatus(msg);
                logger.finest("Text: " + msg);
                logger.fine("Status updated");
            }
            return 0;
        } catch (TwitterException ex) {
            logger.fine("TwitterException when writing");
            throw new FileNotFoundException();
        } catch (RuntimeException ex) {
            /*
             * 実行時例外が発生した際には EIO(IOエラー)にマップ。
             * なんにしてもちゃんとエラーで返すことが大事。
             */
            throw new IOException();
        }
    }
}