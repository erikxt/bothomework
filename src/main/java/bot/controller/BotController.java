package bot.controller;

import bot.entity.QaEntity;
import bot.entity.Response;
import bot.service.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/bot")
public class BotController {

    @Autowired
    private BotService botService;

    @RequestMapping("/getquestions")
    public Response getQuestions(@RequestParam(value = "words") String words) {
        List retList = botService.getQuestions(words);
        return Response.getSuccessResp(retList);
    }

    @RequestMapping("/getanswer")
    public Response getAnswer(@RequestParam(value = "index") String idx) {
        QaEntity res = botService.getAnswer(idx);
        return Response.getSuccessResp(res);
    }
}
