package bot.controller;

import bot.entity.QaEntity;
import bot.entity.Response;
import bot.service.BotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bot")
public class BotController {

    @Autowired
    private BotService botService;

    private Logger logger = LoggerFactory.getLogger(BotController.class);

    @RequestMapping("/getquestions")
    public Response getQuestions(@RequestParam(value = "words") String words) {
        List<Map<String, Object>> retList = null;
        Map<String, Object> ret = new HashMap<>();
        try {
            retList = botService.getQuestions(words);
            ret.put("questions", retList);
        } catch (IOException e) {
            logger.error("getquestions error ", e);
            return Response.getErrorResp("error");
        }
        return Response.getSuccessResp(ret);
    }

    @RequestMapping("/getanswer")
    public Response getAnswer(@RequestParam(value = "id") String id) {
        QaEntity res = botService.getAnswer(id);
        return Response.getSuccessResp(res);
    }
}
