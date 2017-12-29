package redis;

import kr.msp.redis.clients.JedisBean;
import kr.msp.redis.clients.JsonObjectConverter;
import kr.msp.redis.clients.MiumJedisPoolManager;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Y.B.H(mium2) on 17. 1. 11..
 */
public class MiumJedisTestMain {

    public static void main(String[] args){

        MiumJedisTestMain miumJedisTestMain = new MiumJedisTestMain();

        // Sentinel 정보 set 센티널 정보 등록
        Set<String> sentelInfoSet = new HashSet<String>();
        sentelInfoSet.add("211.241.199.215:26379");
        // Redis 정보 set에 레디스 정보 등록
        Set<String> redisInfoSet = new HashSet<String>();
        redisInfoSet.add("211.241.199.215:6379");
        redisInfoSet.add("211.241.199.217:6379");

        try {
            MiumJedisPoolManager miumJedisPoolManager = new MiumJedisPoolManager(sentelInfoSet, redisInfoSet, 5, "mymaster");


            JedisBean masterJedisBean = miumJedisPoolManager.getMasterJedis();
            Jedis masterJedis = masterJedisBean.getJedis();
            miumJedisTestMain.putTestPushUser("com.uracle.push.test",0,10,masterJedis);

            System.out.println("##### 마스터 정보 ==> 연결 상태 : " + masterJedis.isConnected() + "  정보:" + masterJedis.info());
            miumJedisPoolManager.returnJedis(masterJedisBean);

            JedisBean slaveJedisBean = miumJedisPoolManager.getSlaveJedis();
            Jedis slaveJedis = slaveJedisBean.getJedis();
            System.out.println("##### 슬레이브 정보 ==> 연결 상태 : " + slaveJedis.isConnected() + "  정보:" + slaveJedis.info());
            miumJedisPoolManager.returnJedis(slaveJedisBean);

        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true){
            try {
                Thread.sleep(3600000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 테스트 유저 등록
     */
    private void putTestPushUser(String appid,int startJ, int endJ, Jedis masterJedis){
        Map<String,String> putPushUserMap = new HashMap<String, String>();
        //RDB 트랜젝션

        BasicPushUserBean basicPushUserBean = null;
        try {
            for (int j = startJ; j < endJ; j++) {

                String MAKE_CUID = String.format("P%09d", j);
                String deviceID = "deviceID_" + j;
                basicPushUserBean = new BasicPushUserBean();
                basicPushUserBean.setAPPID(appid);
                basicPushUserBean.setCNAME("이름_" + j);
                basicPushUserBean.setCUID(MAKE_CUID);
                basicPushUserBean.setDEVICEID(deviceID);
                basicPushUserBean.setPSID("PSID_" + j);
                basicPushUserBean.setPUSHKEY("PUSHKEY_" + j);

                basicPushUserBean.setPNSID("UPNS");
                basicPushUserBean.setUPNSID("UPNS_01");

                //UPNS서버만 서비스 가입 시키기
                //basicPushUserBean.setPNSID("UPNS");
//                basicPushUserBean.setUPNSID(RedisUPNSInfoMgr.getInstance().allocateUpnsConInfo().getSERVERID());

                putPushUserMap.put(basicPushUserBean.getPUSHKEY(), JsonObjectConverter.getAsJSON(basicPushUserBean));

                masterJedis.hmset("USERINFO",putPushUserMap);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
