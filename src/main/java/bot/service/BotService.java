package bot.service;

import bot.entity.QaEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class BotService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public List<Map> getQuestions(String words) {
        return Collections.EMPTY_LIST;
    }

    public QaEntity getAnswer(String idx) {
        // 增加计数
        redisTemplate.opsForValue().increment(getAccessCount(idx), 1);
        String count = redisTemplate.opsForValue().get(getAccessCount(idx));
        Integer total = Integer.parseInt(count == null ? "0" : count);

        // 返回答案
        QaEntity mock = new QaEntity();
        mock.setIndex(1);
        mock.setAnswer(count);
        return mock;
    }

    private String getAccessCount(String idx) {
        return String.format("count:%s", idx);
    }
}
