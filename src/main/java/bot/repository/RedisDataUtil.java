package bot.repository;

import bot.entity.QaEntity;
import org.apache.log4j.Logger;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * 问答库
 */
@Repository
public class RedisDataUtil {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static Logger logger = Logger.getLogger(QaEntity.class);

    /**
     * 增加访问计数
     *
     * @param id 问题编号
     */
    public void increAccessCount(Integer id) {
        stringRedisTemplate.opsForValue().increment(getAccessKey(id), 1);
    }

    /**
     * 获取访问计数
     *
     * @param id 问题编号
     * @return
     */
    public Integer getAccessCount(Integer id) {
        String count = stringRedisTemplate.opsForValue().get(getAccessKey(id));
        return Integer.parseInt(count == null ? "0" : count);
    }

    private String getAccessKey(Integer id) {
        return String.format("count:%d", id);
    }
}