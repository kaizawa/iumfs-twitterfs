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

import iumfs.NotSupportedException;
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

public class TimelineFile extends TwitterfsFile {
    protected static final String CONT = "(cont) ";
    protected long last_id = 0;
    protected long base_id = 0;
    protected List<Status> status_list = new ArrayList<Status>();
    protected static final int max_statues = Prefs.getInt("maxStatuses");
    protected long interval = 0L;
    protected boolean initial_read = true;
    protected boolean stream_api = false; // Switch for use Stream API
    private int max_pages = Prefs.getInt("maxPages");

    /**
     * Constractor for twitter file 
     * @param account Twitter account
     * @param filename
     * @param is_timeline 
     * @param is_stream_api
     * @param interval 
     */
    TimelineFile(Account account, String filename, boolean is_stream_api, long interval) {
        super(account, filename);
        this.is_timeline = true;
        this.stream_api = is_stream_api;
        this.interval = interval;
        init();
        startAutoUpdateThreads();
    }

    /** 
     * Initialise routine for this type of file
     */
    private void init() {
        if (!isStream_api()) {
            /*
             * If not stream timeline, read 1 page(20 tweets)
             * as initial read.
             */
            getTimeline(1, 20, 1);
        }
    }
    ;    

    protected StatusListener listener = new UserStreamListener() {

        @Override
        public void onStatus(Status status) {
            logger.log(Level.FINER, "Read Status id={0}", status.getId());
            logger.finest(TimelineFile.statusToFormattedString(status));
            addStatusToList(status);
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        }

        @Override
        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        }

        @Override
        public void onException(Exception ex) {
        }

        @Override
        public void onScrubGeo(long l, long l1) {
        }

        @Override
        public void onDeletionNotice(long l, long l1) {
        }

        @Override
        public void onFriendList(long[] longs) {
        }

        @Override
        public void onFavorite(User user, User user1, Status status) {
        }

        @Override
        public void onUnfavorite(User user, User user1, Status status) {
        }

        @Override
        public void onFollow(User user, User user1) {
        }

        @Override
        public void onRetweet(User user, User user1, Status status) {
        }

        @Override
        public void onDirectMessage(DirectMessage dm) {
        }

        @Override
        public void onUserListMemberAddition(User user, User user1, UserList ul) {
        }

        @Override
        public void onUserListMemberDeletion(User user, User user1, UserList ul) {
        }

        @Override
        public void onUserListSubscription(User user, User user1, UserList ul) {
        }

        @Override
        public void onUserListUnsubscription(User user, User user1, UserList ul) {
        }

        @Override
        public void onUserListCreation(User user, UserList ul) {
        }

        @Override
        public void onUserListUpdate(User user, UserList ul) {
        }

        @Override
        public void onUserListDeletion(User user, UserList ul) {
        }

        @Override
        public void onUserProfileUpdate(User user) {
        }

        @Override
        public void onBlock(User user, User user1) {
        }

        @Override
        public void onUnblock(User user, User user1) {
        }
    };

    private void startAutoUpdateThreads() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        /*
         * Invokde timeline retrieving thread. 
         */
        if (isStream_api() == true) {
            /*
             * Stream API (only home)
             * Thread created in TwitterStream.sample()
             */
            TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
            twitterStream.setOAuthConsumer(Prefs.get("OAuthConsumerKey"), Prefs.get("consumerSecret"));
            twitterStream.setOAuthAccessToken(IumfsTwitterFactory.getAccessToken(getUsername()));
            twitterStream.addListener(listener);
            twitterStream.user();
        } else {
            /*
             * retrieve periodically
             */
            executor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    getTimeline();
                }
            }, getInterval(), getInterval(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public long getFileSize() {
        return file_size;
    }

    /**
     * Get Status data of requested bytes at requested offset.
     * The base of offset statts from oldest status.
     * 
     * Procect list by synchronizing, not to read when time line is updated.
     *
     * TODO: CopyOnWriteArrayList would be best, but it doesn't support sorting..
     * TODO: In current implementation, calculation of offset is very costly.
     *       Should I store status as simple text data?
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
        long rel_offset; // relative offset within each status 
        int page = 0;

        Twitter twitter = IumfsTwitterFactory.getInstance(getUsername());

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
                String text = statusToFormattedString(status);
                byte[] bytes = text.getBytes("UTF-8");
                long copy_size = 0;
                long status_length = bytes.length;
                rel_offset = 0;

                logger.finer("Read status_list id=" + status.getId()
                        + ", status_length=" + status_length);

                logger.finest(text);

                /*
                 * Calculate current offset by adding number of characters to 
                 * previous offset.
                 */
                curr_offset += status_length;
                logger.finer("curr_off=" + curr_offset);
                if (curr_offset < offset) {
                    logger.finer("offset not yet reached");
                    continue;
                } else if (prev_offset >= offset) {
                    logger.finer("prev_offset >= offset");
                    /*
                     * Already exceed offset. set relative offset to 0.
                     */
                    rel_offset = 0;
                } else {
                    logger.finer("prev_offset < offset");
                    /*
                     * offset is within this status.
                     * calculate relative offset.
                     */
                    rel_offset = offset - prev_offset;
                }
                logger.finer("rel_offset = " + rel_offset);

                if (curr_size + status_length >= size) {
                    /*
                     * status size is larger or equal to requested size.
                     * Copy necessary data size and won't read any more. 
                     */
                    copy_size = size - curr_size - rel_offset;
                    logger.finer("copy_Size = " + copy_size + ". No need to read more Status");
                } else {
                    /*
                     * need more data... copy all status data to buffer.
                     */
                    copy_size = bytes.length - rel_offset;
                    logger.finer("copy_Size = " + copy_size + ". need to read more Status");
                }
                /*
                 * write to buffer
                 */
                buf.put(bytes, (int) rel_offset, (int) copy_size);
                curr_size += copy_size;
                if (curr_size >= size) {
                    logger.finer("currSize >= size");
                    break;
                }
                logger.finer("curr_size < size. continue for statement.");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(TimelineFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        logger.fine("curr_size = " + curr_size);
        return curr_size;
    }

    /**
     * Convert Status to formated text.
     * 
     * @param status Status
     * @return formatted text
     */
    public static String statusToFormattedString(Status status) {
        /*
         * Add user name time..
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
        getTimeline(max_statues, last_id);
    }

    /**
     * Read time line from Twitter
     * This function just invoke getTimeline(int page, int count, long since) 
     * 
     * @param count
     * @param since 
     */
    public void getTimeline(int count, long since) {
        int cnt = 0;
        int page = 1; // page start from 1 !!
        /*
         * Retrieve status up to max_pages(4 by default)
         * It's 80 status for public timeline, and 800 status for the ther timilene
         *   20 * 4 = 80
         *   200 * 4 = 800
         */
        do {
            cnt = getTimeline(page, count, since);
            page++;
        } while ((cnt == count && page < max_pages) || (cnt == 20 && page < max_pages));
    }

    /**
     * Retrieve Statuses in given pages.
     * This method must be called exclusively.
     * 
     * @param page
     * @param count
     * @param since
     * @return 
     */
    synchronized public int getTimeline(int page, int count, long since) {
        ResponseList<Status> statuses = null;
        Twitter twitter = IumfsTwitterFactory.getInstance(getUsername());
        String name = getName();
        try {
            String timeline = getName();
            if (name.equals("mentions")) {
                statuses = twitter.getMentions(new Paging(page, count, since));
            } else if (name.equals("public")) {
                statuses = twitter.getPublicTimeline();
            } else if (name.equals("friends")) {
                statuses = twitter.getHomeTimeline(new Paging(page, count, since));
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
                // last status
                return 0;
            }
            // Set first status(newest) as last_id.
            last_id = statuses.get(0).getId();
            for (Status status : statuses) {
                logger.finer("Read Status id=" + status.getId());
                logger.finest(statusToFormattedString(status));
                setFileSize(getFileSize() + statusToFormattedString(status).getBytes("UTF-8").length);
                status_list.add(status);
            }
            if (initial_read) {
                /*
                 * Set last status(oldest) to base_id.
                 */
                base_id = statuses.get(statuses.size() - 1).getId();
                logger.finer("base_id = " + base_id);
                initial_read = false;
            }

            logger.fine("new file_size is " + getFileSize());
            java.util.Collections.sort(status_list);
            /*
             * Timelie is update. So changed mtime and ctime
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
             * Timelie is update. So changed mtime and ctime
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
    public long write(byte[] buf, long size, long offset) {
        throw new NotSupportedException();
    }

    @Override
    public long getPermission() {
        return (long) 0100444; // regular file      
    }

    @Override
    protected String getUsername() {
        return account.getUsername();
    }
}