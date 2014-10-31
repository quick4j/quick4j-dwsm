package com.github.quick4j.core.web.http.distributed.session.manager.task;

import com.github.quick4j.core.web.http.distributed.session.session.DistributedHttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author zhaojh.
 */
public class ClearInvalidSessionScheduledTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClearInvalidSessionScheduledTask.class);
    private Map<String, HttpSession> localSessionContainer;

    public ClearInvalidSessionScheduledTask(Map<String, HttpSession> localSessionContainer) {
        this.localSessionContainer = localSessionContainer;
    }

    @Override
    public void run() {
//        logger.info("执行清除过期Session任务。");
        for (HttpSession session: localSessionContainer.values()){
            if(!((DistributedHttpSession)session).isValid()){
                logger.info("Clear session[{}]", session.getId());
                session.invalidate();
            }
        }
    }
}
