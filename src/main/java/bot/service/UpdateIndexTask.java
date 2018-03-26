package bot.service;

import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;

public class UpdateIndexTask {

    @Resource
    private SearchService searchService;

    @Scheduled(initialDelay = 15_000, fixedRate = 30_000)
    public void work() {
        long now = System.currentTimeMillis();
        searchService.reloadIndex();
        System.out.println("time cost: " + (System.currentTimeMillis() - now));
    }
}
