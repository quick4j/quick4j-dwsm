package com.github.quick4j.core.web.http.distributed.session.manager;

import com.github.quick4j.core.web.http.distributed.session.SessionIDManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.util.Random;

/**
 * @author zhaojh.
 */
public class DefaultSessionIDManager implements SessionIDManager {
    private static final Logger logger = LoggerFactory.getLogger(DefaultSessionIDManager.class);

    //    private final static String   __NEW_SESSION_ID  = "com.github.quick4j.newSessionId";
    private Random random;
    private boolean weakRandom;
    private long reseed=100000L;

    @Override
    public String newSessionId(HttpServletRequest request, long created) {
        if(request == null){
            return newSessionId(created);
        }

        return newSessionId(request.hashCode());
    }

    @Override
    public void start() {
        logger.info("启动Session ID Manager.");
        try{
            random = new SecureRandom();
        }catch (Exception e){
            logger.warn("Could not generate SecureRandom for httpsession-id randomness", e);
            random = new Random();
            weakRandom = true;
        }
    }

    @Override
    public void stop() {
        logger.info("关闭Session ID Manager.");
    }

    private String newSessionId(long seedTerm){
        String id = null;
        while (id == null  || id.length() == 0){
            long r0 = weakRandom ? (hashCode() ^ Runtime.getRuntime().freeMemory() ^ random.nextInt() ^ (seedTerm<<32))
                    : random.nextLong();

            if(r0 < 0) r0 = -r0;

            if (reseed>0 && (r0 % reseed)== 1L){
                logger.info("Reseeding {}", this);

                if(random instanceof SecureRandom){
                    SecureRandom secure = (SecureRandom) random;
                    secure.setSeed(secure.generateSeed(8));
                }else{
                    random.setSeed(random.nextLong() ^ System.currentTimeMillis() ^ seedTerm ^ Runtime.getRuntime().freeMemory());
                }
            }

            long r1 = weakRandom ?
                    (hashCode() ^ Runtime.getRuntime().freeMemory() ^ random.nextInt() ^ (seedTerm<<32))
                    : random.nextLong();

            if(r1 < 0) r1 = -r1;

            id = Long.toString(r0, 36) + Long.toString(r1, 36);
        }

        return id + ".quick4j";
    }
}
