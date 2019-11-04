package zheng.core.redis;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.params.SetParams;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: zhengwenchao
 * @Date: 2019 2019-11-04 17:07
 */
public class RedisManager {
    private static Logger logger = LoggerFactory.getLogger(RedisManager.class);
    private static JedisPool jedisPool;
    public static void getJedisPool(){
        JedisPoolConfig jedisPoolConfig=new JedisPoolConfig();
        //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
        jedisPoolConfig.setMaxIdle(5);
        //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        jedisPoolConfig.setMaxWaitMillis(-1);
        //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPool=new JedisPool(jedisPoolConfig,"host",6379,2000,"password");
    }

    public static Jedis getClient(){
        if (jedisPool==null){
            synchronized (RedisManager.class){
                if (jedisPool==null){
                    getJedisPool();
                }
            }
        }
        return jedisPool.getResource();
    }

    /**
     * 释放jedis资源
     * @param jedis
     */
    public static void returnResource(final Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    public static String get(String key) {
        Jedis jedis = null;
        String value = null;
        boolean borrowOrOprSuccess = true;
        try {
            jedis = getClient();
            value = jedis.get(key);
        } catch (JedisConnectionException e) {
            logger.error("redis,获取 key={}的数据" , key, e);
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    public static void set(String key,String value){
        Jedis jedis = null;
        Serializable serializeble = null;
        if (value instanceof Serializable) {
            serializeble = (Serializable) value;
        } else {
            return;
        }
        try {
            jedis = getClient();
            jedis.set(key, value);
        } catch (JedisConnectionException e) {
            logger.error("set异常",e);
        } finally {
            returnResource(jedis);
        }
    }

    public static boolean setnx(String key,String value,Integer seconds){
        Jedis jedis = null;
        try {
            jedis = getClient();
            SetParams setParams = new SetParams();
            setParams.nx();
            setParams.ex(seconds);
            String result = jedis.set(key,value,setParams);
            return "OK".equals(result);
        } catch (JedisConnectionException e) {
            logger.error("setnx异常",e);
        } finally {
            returnResource(jedis);
        }
        return false;
    }

    public static void del(String key) {
        Jedis jedis = null;
        try {
            jedis = getClient();
            jedis.del(key);
        } catch (JedisConnectionException e) {
            logger.error("redis,删除 key=" + key, e);
        } finally {
            returnResource(jedis);
        }

    }

    /**
     * 判断hash是否存在field
     * @return
     */
    public static boolean hexists(String key,String field){
        Jedis jedis = null;
        boolean value = false;
        try {
            jedis = getClient();
            value = jedis.hexists(key,field);
        } catch (JedisConnectionException e) {
            logger.error("redis hexists异常,失败key=" + key, e);
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    public static String hget(String key,String field) {
        Jedis jedis = null;
        String value = null;
        boolean borrowOrOprSuccess = true;
        try {
            jedis = getClient();
            value = jedis.hget(key,field);
        } catch (JedisConnectionException e) {
            logger.error("redis,获取 key={}的数据" , key, e);
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    public static String hset(String key,String field,String value) {
        Jedis jedis = null;
        boolean borrowOrOprSuccess = true;
        try {
            jedis = getClient();
            jedis.hset(key,field,value);
        } catch (JedisConnectionException e) {
            logger.error("redis,获取 key={}的数据" , key, e);
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    public static boolean exists(String key) {
        Jedis jedis = null;
        boolean value = false;
        try {
            jedis = getClient();
            value = jedis.exists(key);
        } catch (JedisConnectionException e) {
            logger.error("redis exists异常,失败key=" + key, e);
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    public static Long expire(String key, int seconds) {
        Jedis jedis = null;
        Long result = 0L;
        try {
            jedis = getClient();
            result = jedis.expire(key, seconds);
        } catch (JedisConnectionException e) {
            logger.error("hset redis,失败key=" + key, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    public static Object getObject(String key){
        Jedis jedis = null;
        Object value = null;
        try {
            jedis = getClient();
            value = SerializationUtils.deserialize(jedis.get(SerializationUtils
                    .serialize(key.hashCode())));
        } catch (JedisConnectionException e) {
            logger.error("redis,获取 key={}的数据" , key, e);
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    public static boolean isExitByObjectKey(Object key) {
        Jedis jedis = null;
        boolean value = false;
        boolean borrowOrOprSuccess = true;
        try {
            jedis = getClient();
            value = jedis.exists(SerializationUtils.serialize(key.hashCode()));
        } catch (JedisConnectionException e) {
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    public static void putObject(String key, Object value) {
        Jedis jedis = null;
        Serializable serializeble = null;
        if (value instanceof Serializable) {
            serializeble = (Serializable) value;
        } else {
            return;
        }
        try {
            jedis = getClient();
            jedis.set(SerializationUtils.serialize(key.hashCode()), SerializationUtils.serialize(serializeble));
        } catch (JedisConnectionException e) {
            logger.error("putObject异常",e);
        } finally {
            returnResource(jedis);
        }
    }

    public static Long expireObject(String key,int seconds){
        Jedis jedis = null;
        Long result = 0L;
        try {
            jedis = getClient();
            result = jedis.expire(SerializationUtils.serialize(key.hashCode()), seconds);
        } catch (JedisConnectionException e) {
            logger.error("redis expireObject异常,失败key=" + key, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    public static boolean existsObject(String key) {
        Jedis jedis = null;
        boolean value = false;
        try {
            jedis = getClient();
            value = jedis.exists(SerializationUtils.serialize(key.hashCode()));
        } catch (JedisConnectionException e) {
            logger.error("redis existsObject异常,失败key=" + key, e);
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    public static Object removeObject(Object key) {
        Jedis jedis = null;
        Object value = null;
        try {
            jedis = getClient();
            value = jedis.expire(SerializationUtils.serialize(key.hashCode()), 0);
        } catch (JedisConnectionException e) {
            logger.error("removeObject异常",e);
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    /**
     * list push
     * @param key
     * @param value
     * @param expirs
     */
    public static void lpush(String key, String value,int expirs) {
        Jedis jedis = null;
        try {
            jedis = getClient();
            jedis.lpush(key,value);
            jedis.expire(key,expirs);
        } catch (JedisConnectionException e) {
            logger.error("removeObject异常",e);
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * list push
     * @param key
     * @param value
     */
    public static void lpush(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = getClient();
            jedis.lpush(key,value);
        } catch (JedisConnectionException e) {
            logger.error("removeObject异常",e);
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * 获取list长度
     * @param key
     * @return
     */
    public static Long llen(String key) {
        Jedis jedis = null;
        try {
            jedis = getClient();
            return jedis.llen(key);
        } catch (JedisConnectionException e) {
            logger.error("removeObject异常",e);
        } finally {
            returnResource(jedis);
        }
        return 0L;
    }

    /**
     * 取出并移除list第一个元素
     * @param key
     * @return
     */
    public static String blpop(String key) {
        Jedis jedis = null;
        try {
            jedis = getClient();
            List<String> blop = jedis.blpop(1,key);
            return blop.get(1);
        } catch (JedisConnectionException e) {
            logger.error("blpop异常",e);
        } finally {
            returnResource(jedis);
        }
        return null;
    }

    /**
     * 获取100条
     * @param key
     * @return
     */
    public static List<String> lrange(String key){
        Jedis jedis = null;
        try {
            jedis = getClient();
            List<String> list = new ArrayList<>();
            list = jedis.lrange(key, 0,100);
            return list;
        } catch (JedisConnectionException e) {
            logger.error("lrange异常",e);
        } finally {
            returnResource(jedis);
        }
        return null;
    }

    /**
     * 修剪list
     * @param key
     * @return
     */
    public static void ltrim(String key){
        Jedis jedis = null;
        try {
            jedis = getClient();
            jedis.ltrim(key, 0, 100);
        } catch (JedisConnectionException e) {
            logger.error("lrange异常",e);
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * 移除list指定值
     * @param key
     * @return
     */
    public static void lrem(String key,int count,String value){
        Jedis jedis = null;
        try {
            jedis = getClient();
            jedis.lrem(key,count,value);
        } catch (JedisConnectionException e) {
            logger.error("lrange异常",e);
        } finally {
            returnResource(jedis);
        }
    }
}
