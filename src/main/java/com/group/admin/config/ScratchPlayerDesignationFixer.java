package com.group.admin.config;

import com.group.admin.service.impl.LotteryTicketServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(25)
@RequiredArgsConstructor
public class ScratchPlayerDesignationFixer implements CommandLineRunner {

    private final LotteryTicketServiceImpl lotteryTicketService;

    @Override
    public void run(String... args) {
        try {
            int repairedCount = lotteryTicketService.repairDuplicatePlayerDesignations();
            log.info("Scratch player designation repair completed: repairedLotteries={}", repairedCount);
        } catch (Exception ex) {
            log.error("Scratch player designation repair failed: {}", ex.getMessage(), ex);
        }
    }
}
