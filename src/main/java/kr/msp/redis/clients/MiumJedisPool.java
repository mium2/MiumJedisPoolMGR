package kr.msp.redis.clients;


import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

/**
 * Created by Y.B.H(mium2) on 17. 1. 11..
 */
public class MiumJedisPool extends JedisPool {
    public MiumJedisPool(final GenericObjectPoolConfig poolConfig, final String host, int port,
                         int timeout, final String password, final int database){
        super(poolConfig,host,port,timeout,password,database);
    }

    @Override
    public void returnBrokenResource(final Jedis resource) {
        if (resource != null) {
            returnBrokenResourceObject(resource);
        }
    }

    @Override
    public void returnResource(final Jedis resource) {
        if (resource != null) {
            try {
                resource.resetState();
                returnResourceObject(resource);
            } catch (Exception e) {
                returnBrokenResource(resource);
                throw new JedisException("Could not return the resource to the pool", e);
            }
        }
    }
}
