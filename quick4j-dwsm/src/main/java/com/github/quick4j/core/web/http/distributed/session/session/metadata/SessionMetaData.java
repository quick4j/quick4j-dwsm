package com.github.quick4j.core.web.http.distributed.session.session.metadata;

/**
 * @author zhaojh.
 */
public class SessionMetaData {
    public static final String CREATION_TIME_KEY = "creationTime";
    public static final String LAST_ACCESSED_TIME_KEY = "lastAccessedTime";
    public static final String MAX_INACTIVE_INTERVAL_KEY = "maxInactiveInterval";

    private String id;
    private long creationTime;
    private long lastAccessedTime;
    private int maxInactiveInterval;

    public SessionMetaData(String id, long creationTime, long lastAccessedTime, int maxInactiveInterval) {
        this.id = id;
        this.creationTime = creationTime;
        this.lastAccessedTime = lastAccessedTime;
        this.maxInactiveInterval = maxInactiveInterval;
    }

    public String getId() {
        return id;
    }

    public long getCreationTime() {
        return creationTime;
    }


    public long getLastAccessedTime() {
        return lastAccessedTime;
    }


    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }
}
