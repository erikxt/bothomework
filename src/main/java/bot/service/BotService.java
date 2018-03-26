package bot.service;

import bot.entity.QaEntity;
import bot.repository.QaRepository;
import bot.repository.RedisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BotService {

    @Resource
    RedisRepository redisRepository;

    @Resource
    private QaRepository qaRepository;

    @Resource
    private SearchService searchService;

    private static Map<String, QaEntity> cacheMap = Collections.EMPTY_MAP;

    private Logger logger = LoggerFactory.getLogger(BotService.class);

    @PostConstruct
    public void init() {
        List<QaEntity> allEntities = qaRepository.findAll();
        if (allEntities != null)
            cacheMap = allEntities.stream().collect(Collectors.toConcurrentMap(QaEntity::getId, qaEntity -> qaEntity));
    }

    public List<Map<String, Object>> getQuestions(String words) throws IOException {
        List<String> ids = searchService.executeQuery(words);
        return ids.stream().map(str -> {
            QaEntity qaEntity = cacheMap.get(str);
            Map<String, Object> map = new HashMap() {{
                put("id", qaEntity.getId());
                put("question", qaEntity.getQuestion());
                put("keywords", qaEntity.getKeywords());
            }};
            return map;
        }).collect(Collectors.toList());
    }

    public QaEntity getAnswer(String id) {
        // 增加计数
        redisRepository.increAccessCount(id);
        // 返回答案
        logger.info(id + "id count" + redisRepository.getAccessCount(id));
        QaEntity ret = cacheMap.get(id);
        return ret;
    }
}
