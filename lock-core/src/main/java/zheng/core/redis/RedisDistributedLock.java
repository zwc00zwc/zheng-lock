package zheng.core.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zheng.core.lock.DistributedLock;
import zheng.core.lock.LockTypeEnum;

import java.util.Map;
import java.util.UUID;

/**
 * @Author: zhengwenchao
 * @Date: 2019 2019-11-04 16:58
 */
public class RedisDistributedLock implements DistributedLock {
    private ThreadLocal<Map> keyLocalValue = new ThreadLocal<Map>();

    private static Logger logger = LoggerFactory.getLogger(RedisDistributedLock.class);

    private final Integer lock_expire = 10;

    @Override
    public void lock(Map map) throws Exception {

    }

    @Override
    public void lock(LockTypeEnum lockTypeEnum) throws Exception {

    }

    @Override
    public void unlock(Map map) {

    }

    @Override
    public void unlock(LockTypeEnum lockTypeEnum) {

    }

    /**
     * 尝试加锁
     * @return
     */
    private boolean tryLock(Map map){
        if (keyLocalValue.get()!=null){
            return true;
        }
        //加锁成功
        String identifier = UUID.randomUUID().toString();
        try {
            if (RedisManager.setnx(map.get("type")+"",identifier,lock_expire)){
                map.put("value",identifier);
                keyLocalValue.set(map);
                logger.info("---------"+Thread.currentThread().toString()+"获得锁-----------");
                return true;
            }
        } catch (Exception e) {
            logger.error("操作redis异常",e);
            throw e;
        }
        return false;
    }
}
