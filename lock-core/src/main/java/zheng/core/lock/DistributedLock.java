package zheng.core.lock;

import java.util.Map;

/**
 * @Author: zhengwenchao
 * @Date: 2019 2019-11-04 16:49
 */
public interface DistributedLock {
    void lock(Map map) throws Exception;

    /**
     * 加锁
     */
    void lock(LockTypeEnum lockTypeEnum) throws Exception;

    /**
     * 释放锁
     */
    void unlock(Map map);

    /**
     * 释放锁
     */
    void unlock(LockTypeEnum lockTypeEnum);
}
