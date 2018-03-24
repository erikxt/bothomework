package bot.service;

import bot.entity.QaEntity;
import bot.util.QaDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class BotService {

    @Autowired
    QaDataUtil qaDataUtil;

    public List<Map> getQuestions(String words) {

        return Collections.EMPTY_LIST;
    }

    public QaEntity getAnswer(String idx) {
        // 增加计数
        qaDataUtil.increAccessCount(idx);

        // 返回答案
        QaEntity mock = new QaEntity();
        mock.setIndex(1);
        mock.setAnswer(String.valueOf(qaDataUtil.getAccessCount(idx)));
        return mock;
    }
}
