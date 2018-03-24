package bot.Util;

import bot.entity.QaEntity;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class QaDataUtil {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static Logger logger = Logger.getLogger(QaEntity.class);

    /**
     * 增加访问计数
     *
     * @param idx 问题编号
     */
    public void increAccessCount(String idx) {
        redisTemplate.opsForValue().increment(getAccessKey(idx), 1);
    }

    /**
     * 获取访问计数
     *
     * @param idx 问题编号
     * @return
     */
    public Integer getAccessCount(String idx) {
        String count = redisTemplate.opsForValue().get(getAccessKey(idx));
        return Integer.parseInt(count == null ? "0" : count);
    }

    private List<String> getAllQaData() {
        List<String> ret = redisTemplate.opsForList().range("Qa:database", 0, -1);
        return ret != null ? ret : Collections.EMPTY_LIST;
    }

    public List<QaEntity> getAllQaEntitys() {
        List<String> strList = getAllQaData();
        List<QaEntity> ret = new ArrayList<>();
        for (int i = 0; i < strList.size(); i++) {
            QaEntity qaEntity = new QaEntity();
            qaEntity.setIndex(i);
            String str = strList.get(i);
            logger.info("Q" + i + "  " + str);
            String[] strArr = str.split(",");
            if (strArr == null || strArr.length < 3) continue;
            qaEntity.setQuestion(strArr[0]);
            qaEntity.setAnswer(strArr[1]);
            qaEntity.setKeywords(strArr[2]);
            ret.add(qaEntity);
        }
        return ret;
    }

    public Map<Integer, QaEntity> getQaEntityMap() {
        return getAllQaEntitys().stream().collect(Collectors.toMap(QaEntity::getIndex, qaEntity -> qaEntity));
    }

    private String getAccessKey(String idx) {
        return String.format("count:%s", idx);
    }
}
