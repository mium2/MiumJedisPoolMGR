package kr.msp.redis.clients;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Y.B.H(mium2) on 17. 1. 11..
 */
public class MiumJedisPoolManager {

    protected Logger logger = Logger.getLogger(getClass().getName());
    private JedisSentinelPool jedisSentinelPool;
    private HostAndPort nowMasterJedisHost;
    private long chkMasterHostUpMiliSecond = 15000;
    private long lastChkMasterMiliSecond = 0;
    private GenericObjectPoolConfig config;

    private Set<String> redisInfoSet = null;
    private Set<String> sentelInfoSet = null;
    // [REDIS 서버별 Jedis pool관리 맵] 설명 : Redis 서버별 연결세션을 생성/반환/삭제할수 있는 세션풀을 서버별로 관리하는맵
    private Map<String,MiumJedisPool> redisPoolMap = new HashMap<String,MiumJedisPool>();

    private List<String> slaveHostList;
    private int lastReturnSlaveNo = 1;
    private int dbNum = 0;
    private String masterName = "mymaster";
    private int timeout = 5000;
    private String password = null;

    private boolean isCluster = false;
    private Set<HostAndPort> clusterHostAndPortSet = new HashSet<HostAndPort>();
    private String clusterHost = "";

    public MiumJedisPoolManager(Set<String> _sentinelInfoSet, Set<String> _redisInfoSet, int _dbnum, String _masterName) throws Exception{
        this.sentelInfoSet = _sentinelInfoSet;
        this.redisInfoSet = _redisInfoSet;
        this.dbNum = _dbnum;
        this.masterName = _masterName;

        init();
    }

    public MiumJedisPoolManager(Set<String> _sentinelInfoSet, Set<String> _redisInfoSet, int _dbnum, String _masterName, GenericObjectPoolConfig poolConfig) throws Exception{
        this.sentelInfoSet = _sentinelInfoSet;
        this.redisInfoSet = _redisInfoSet;
        this.dbNum = _dbnum;
        this.masterName = _masterName;

        init(poolConfig);
    }

    /**
     * Redis cluster용
     * @throws Exception
     */
    public MiumJedisPoolManager(String clusterHost, int clusterPort) throws Exception{
        HostAndPort clusterHostAndPort = new HostAndPort(clusterHost,clusterPort);
        clusterHostAndPortSet.add(clusterHostAndPort);
        this.clusterHost = clusterHost;
        this.isCluster = true;
    }

    public MiumJedisPoolManager(String clusterHost, int clusterPort, GenericObjectPoolConfig poolConfig) throws Exception{
        HostAndPort clusterHostAndPort = new HostAndPort(clusterHost,clusterPort);
        clusterHostAndPortSet.add(clusterHostAndPort);
        this.clusterHost = clusterHost;
        this.isCluster = true;

        init(poolConfig);
    }

    public MiumJedisPoolManager(Set<String> _sentinelInfoSet, Set<String> _redisInfoSet, int _dbnum, String _masterName, int _timeout, String _redisPassword) throws Exception{
        this.sentelInfoSet = _sentinelInfoSet;
        this.redisInfoSet = _redisInfoSet;
        this.dbNum = _dbnum;
        this.masterName = _masterName;
        this.timeout = _timeout;
        this.password = _redisPassword;

        init();
    }

    public MiumJedisPoolManager(Set<String> _sentinelInfoSet, Set<String> _redisInfoSet, int _dbnum, String _masterName, int _timeout, String _redisPassword, GenericObjectPoolConfig poolConfig) throws Exception{
        this.sentelInfoSet = _sentinelInfoSet;
        this.redisInfoSet = _redisInfoSet;
        this.dbNum = _dbnum;
        this.masterName = _masterName;
        this.timeout = _timeout;
        this.password = _redisPassword;

        init(poolConfig);
    }

    public void init() throws Exception{
        init(null);
    }

    public void init(GenericObjectPoolConfig poolConfig) throws Exception{
        if(poolConfig==null) {
            config = new GenericObjectPoolConfig();
            config.setMaxTotal(50);
            config.setMaxWaitMillis(5000);
            config.setTestOnBorrow(true);
        }else{
            config = poolConfig;
        }

        if(sentelInfoSet!=null) {
            //센티널 pool을 생성.
            jedisSentinelPool = new JedisSentinelPool(masterName, sentelInfoSet, config, timeout, password, dbNum);
            nowMasterJedisHost = jedisSentinelPool.getCurrentHostMaster();
            lastChkMasterMiliSecond = System.currentTimeMillis();

            nowMasterJedisHost = jedisSentinelPool.getCurrentHostMaster();

            // 센티널 정보를 이용하여 현재 로드밸런싱 할 슬레이브로 할당되어 있는 레디스 정보를 저장한다.
            // 여러개의 센티널이 있을 수 있고 Exception 없이 연결이되어 slaves 정보를 가져오면 break를 통해 다른 센티널 연결은 하지 않는다
            for (String sentinelInfo : sentelInfoSet) {
                try {
                    String[] sentinelInfoArr = sentinelInfo.split(":");
                    String sentinelHost = sentinelInfoArr[0].trim();
                    int sentinelPort = Integer.parseInt(sentinelInfoArr[1].trim());

                    Jedis sentinelJedis = new Jedis(sentinelHost, sentinelPort);
                    List<Map<String, String>> slaveList = sentinelJedis.sentinelSlaves(masterName);
                    slaveHostList = new ArrayList<String>();
                    for (int i = 0; i < slaveList.size(); i++) {
                        Map<String, String> slaveInfoMap = slaveList.get(i);
                        slaveHostList.add(slaveInfoMap.get("ip"));
                    }
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 현재 설정되어 있는 모든 Redis(마스터,슬레이브 포함) pool을 만들어 맵으로 관리한다.
            for(String redisHostInfo : redisInfoSet){
                String[] redisHostInfoArr = redisHostInfo.split(":");
                String redisHost = redisHostInfoArr[0].trim();
                int redisPort = Integer.parseInt(redisHostInfoArr[1].trim());
                try {
                    MiumJedisPool jedisPool = new MiumJedisPool(config, redisHost, redisPort, 5000, password, dbNum);
                    redisPoolMap.put(redisHost, jedisPool);
                }catch (Exception e){
                    logger.warning("[NOT CONNECTED] : " + e.getMessage());
                }
            }
        }else{
            // Sentinel이 없을 경우 넘어온 Redis리스트중 첫번째를 master로 등록하고 나머지는 무시 처리 한다.
            for(String redisHostInfo : redisInfoSet){
                String[] redisHostInfoArr = redisHostInfo.split(":");
                String redisHost = redisHostInfoArr[0].trim();
                int redisPort = Integer.parseInt(redisHostInfoArr[1].trim());

                HostAndPort masterHostAndPort = new HostAndPort(redisHost,redisPort);
                this.nowMasterJedisHost = masterHostAndPort;
                try {
                    MiumJedisPool jedisPool = new MiumJedisPool(config, redisHost, redisPort, 5000, password, dbNum);
                    redisPoolMap.put(redisHost, jedisPool);
                }catch (Exception e){
                    logger.warning("[NOT CONNECTED] : " + e.getMessage());
                }
                break;
            }

        }


    }

    /**
     * Master Node Redis에 연결된 Jedis세션을 리턴한다.
     * @return
     * @throws Exception
     */
    public JedisBean getMasterJedis() throws Exception {
        if(isCluster){
            return getClusterJedis();
        }
        JedisBean masterJedisBean = null;
        chkMaster();
        final String masterIP = this.nowMasterJedisHost.getHost();
        final int masterPort = this.nowMasterJedisHost.getPort();
        if (redisPoolMap.containsKey(masterIP)){
            MiumJedisPool masterJedisPool = this.redisPoolMap.get(masterIP);
            Jedis masterJedis = masterJedisPool.getResource();
            masterJedisBean = new JedisBean(masterIP, masterJedis);
            return masterJedisBean;
        }
        MiumJedisPool jedisPool = new MiumJedisPool(this.config, masterIP, masterPort, 5000, null, 1);
        redisPoolMap.put(this.nowMasterJedisHost.getHost(), jedisPool);
        return masterJedisBean;
    }

    /**
     * 여러개의 Slave Node Redis 중 순차적으로 연결된 Jedis세션을 찾아 리턴한다.
     * @return
     * @throws Exception
     */
    public JedisBean getSlaveJedis() throws Exception{
        if(isCluster){
            return getClusterJedis();
        }
        // Sentinel이 없는 경우는 무조건 Master를 리턴한다.
        if(sentelInfoSet==null) {
            return getMasterJedis();
        }
        chkMaster();
        //lastReturnSlaveNo가 배열 사이즈를 넘어간 경우를 위해.
        if(lastReturnSlaveNo>slaveHostList.size()){
            lastReturnSlaveNo=1;
        }
        // slave 조회용 redis는 순차적으로 찾아서 리턴한다.
        for(int i=0; i<slaveHostList.size(); i++){
            int nextIndex = slaveHostList.size() % lastReturnSlaveNo;
            String slaveHost = slaveHostList.get(nextIndex);
            lastReturnSlaveNo++;
            MiumJedisPool jedisPool = redisPoolMap.get(slaveHost);
            try {
                Jedis jedis = jedisPool.getResource();
                if(jedis.isConnected()){
                    logger.fine("[RETURN CONNECTED SLAVE HOST]:"+slaveHost);
                    return new JedisBean(slaveHost,jedis);
                }else{
                    jedisPool.returnBrokenResource(jedis);
                }
            }catch (Exception e){
                logger.warning("[" + slaveHost + " NOT CONNECTED] : " + e.getMessage());
            }

        }
        // 리턴되지 않고 이곳까지 왔다는 말은 살아있는 Slave가 없다는 말이므로 마스터 레디스를 보낸다.
        final String masterIP = nowMasterJedisHost.getHost();
        MiumJedisPool masterJedisPool = redisPoolMap.get(masterIP);
        Jedis masterJedis = masterJedisPool.getResource();
        if(masterJedis!=null && masterJedis.isConnected()) {
            return new JedisBean(masterIP, masterJedis);
        }else{
            throw new Exception("A live Redis server does not exist.");
        }
    }

    private JedisBean getClusterJedis() throws Exception{
        JedisCluster jedisCluster = new JedisCluster(clusterHostAndPortSet);
        JedisBean jedisBean = new JedisBean(clusterHost,jedisCluster);
        jedisBean.setIsCluster(true);
        return jedisBean;
    }

    /**
     * 사용을 완료한 Jedis세션을 반환한다.
     * @param jedisBean
     */
    public void returnJedis(JedisBean jedisBean){
        if(jedisBean!=null && redisPoolMap.containsKey(jedisBean.getHost())){
            if(jedisBean.getJedis()!=null && jedisBean.getJedis().isConnected()) {
                redisPoolMap.get(jedisBean.getHost()).returnResource(jedisBean.getJedis());
            }
        }
    }

    /**
     * 연결이 끊긴 Jedis 처리를 한다. 여기를 호출할 경우는 거의 없어 보임. Jedis Pool에서 default로 3초에 한번씩 체크를 하는것으로 보임.
     * @param jedisBean
     */
    public void returnBrokenJedis(JedisBean jedisBean){
        if(redisPoolMap.containsKey(jedisBean.getHost())){
            redisPoolMap.get(jedisBean.getHost()).returnBrokenResource(jedisBean.getJedis());
        }
    }

    private synchronized void chkMaster(){
        if(sentelInfoSet==null) {
            return;
        }
        if(System.currentTimeMillis()>(lastChkMasterMiliSecond+chkMasterHostUpMiliSecond)){

            try {
                //15초이상에 한번씩 현재 마스터 정보와 저장되어 있는 마스터 정보가 일치하는지 체크한다.
                HostAndPort chkMasterJedisHost = jedisSentinelPool.getCurrentHostMaster();
                if(!chkMasterJedisHost.getHost().equals(nowMasterJedisHost.getHost())){
                    logger.warning("[CHANGE MASTER NODE] : "+nowMasterJedisHost.getHost()+ "==>" + chkMasterJedisHost.getHost());
                    // TODO : 마스터가 변경되었을 때 할일. 1. 마스터 정보 변경 2.slave 정보 갱신
                    String preMasterHost = nowMasterJedisHost.getHost();
                    int preMasterPort = nowMasterJedisHost.getPort();
                    nowMasterJedisHost = chkMasterJedisHost;

                    // 센티널 정보를 이용하여 현재 슬레이브 레디스 정보를 다시 갱신한다.
                    for(String sentinelInfo : sentelInfoSet) {
                        try {
                            String[] sentinelInfoArr = sentinelInfo.split(":");
                            String sentinelHost = sentinelInfoArr[0].trim();
                            int sentinelPort = Integer.parseInt(sentinelInfoArr[1].trim());

                            Jedis sentinelJedis = new Jedis(sentinelHost, sentinelPort);
                            List<Map<String, String>> slaveList = sentinelJedis.sentinelSlaves(masterName);
                            slaveHostList = null;
                            slaveHostList = new ArrayList<String>();
                            for(int i=0; i<slaveList.size(); i++){
                                Map<String,String> slaveInfoMap = slaveList.get(i);
                                slaveHostList.add(slaveInfoMap.get("ip"));
                            }
                            break;
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                lastChkMasterMiliSecond = System.currentTimeMillis();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
