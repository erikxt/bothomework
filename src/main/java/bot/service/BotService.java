package bot.service;

import bot.entity.QaEntity;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class BotService {

    public List<Map> getQuestions(String words) {
        return Collections.EMPTY_LIST;
    }

    public QaEntity getAnswer(String idx) {
        // 增加计数

        // 返回答案
        QaEntity mock = new QaEntity();
        mock.setIndex(1);
        mock.setAnswer("answer");
        return mock;
    }
}
