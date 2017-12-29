package kr.msp.redis.clients;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisPool;

import java.util.Map;

/**
 * Created by Y.B.H(mium2) on 17. 1. 11..
 */
public class JedisBean {
    private JedisCommands jedisCommands;
    private String host;
    private boolean isCluster = false;

    public JedisBean(String _host, Jedis _jedis){
        this.host = _host;
        this.jedisCommands = _jedis;
    }

    public JedisBean(String _host, JedisCluster _jedisCluster){
        this.host = _host;
        this.jedisCommands = _jedisCluster;
        this.isCluster = true;
    }

    public Jedis getJedis() {
        if(jedisCommands instanceof Jedis) {
            return (Jedis) jedisCommands;
        }else {
            return null;
        }
    }

    public JedisCluster getJedisCluster(){
        if(jedisCommands instanceof JedisCluster) {
            return (JedisCluster) jedisCommands;
        }else {
            return null;
        }
    }

    public boolean isCluster() {
        return isCluster;
    }

    public void setIsCluster(boolean isCluster) {
        this.isCluster = isCluster;
    }

    public JedisCommands getJedisCommands(){
        return jedisCommands;
    }


    public String getHost() {
        return host;
    }

}
