package bot.repository;

import bot.entity.QaEntity;
import org.apache.log4j.Logger;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;

/**
 * 问答库
 */
@Repository
public class RedisRepository {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final static String PREFIX = "click:count:";

    private static Logger logger = Logger.getLogger(QaEntity.class);

    /**
     * 增加访问计数
     *
     * @param id 问题编号
     */
    public void increAccessCount(String id) {
        stringRedisTemplate.opsForValue().increment(getAccessKey(id), 1);
    }

    /**
     * 获取访问计数
     *
     * @param id 问题编号
     * @return
     */
    public Integer getAccessCount(String id) {
        String count = stringRedisTemplate.opsForValue().get(getAccessKey(id));
        return Integer.parseInt(count == null ? "0" : count);
    }

    public Map<String, Integer> getAllAccessCountMap() {
        List<String> keyList = getAllAccessKeys();
        List<String> collections = stringRedisTemplate.opsForValue().multiGet(keyList);
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < keyList.size(); i++) {
            map.put(keyList.get(i).replace(PREFIX, ""), Integer.parseInt(collections.get(i)));
        }
        return map;
    }

    public List<String> getAllAccessKeys() {
        Set<String> keys = stringRedisTemplate.keys(PREFIX + "*");
        if (keys == null) return Collections.EMPTY_LIST;
        return new ArrayList<>(keys);
    }

    private String getAccessKey(String id) {
        return PREFIX + id;
    }
}