package com.group.admin.service;

import com.group.admin.entity.Lottery;
import com.group.admin.entity.LotteryPrize;
import com.group.admin.entity.LotteryTicket;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.mapper.LotteryPrizeMapper;
import com.group.admin.mapper.LotterySessionMapper;
import com.group.admin.mapper.LotteryTicketMapper;
import com.group.admin.res.lottery.TicketListResponse;
import com.group.admin.service.impl.LotteryTicketServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LotteryTicketService 單元測試")
class LotteryTicketServiceImplTest {

    @Mock private LotteryMapper lotteryMapper;
    @Mock private LotteryPrizeMapper lotteryPrizeMapper;
    @Mock private LotteryTicketMapper lotteryTicketMapper;
    @Mock private LotterySessionMapper lotterySessionMapper;
    @Mock private PrizeBoxService prizeBoxService;
    @Mock private CoinService coinService;
    @Mock private ConsumptionRecordService consumptionRecordService;
    @Mock private SystemConfigService systemConfigService;
    @Mock private LotteryService lotteryService;

    @InjectMocks
    private LotteryTicketServiceImpl ticketService;

    @Test
    @DisplayName("票券列表應固定依 ticketNumber 排序並回傳穩定 id")
    void getTicketList_ShouldReturnStableTicketOrderAndIds() {
        Lottery lottery = new Lottery();
        lottery.setId("lottery-1");
        lottery.setGameMode("SCRATCH_PLAYER");

        LotteryTicket ticket3 = ticket("ticket-3", 3, "AVAILABLE", null);
        LotteryTicket ticket1 = ticket("ticket-1", 1, "DRAWN", "prize-1");
        LotteryTicket ticket2 = ticket("ticket-2", 2, "AVAILABLE", null);
        ticket1.setRevealedNumber(77);

        LotteryPrize prize = new LotteryPrize();
        prize.setId("prize-1");
        prize.setName("A Prize");
        prize.setLevel("A");
        prize.setIsGrandPrize((byte) 1);

        when(lotteryMapper.selectByPrimaryKey("lottery-1")).thenReturn(lottery);
        when(lotteryTicketMapper.selectByExample(any())).thenReturn(List.of(ticket3, ticket1, ticket2));
        when(lotteryPrizeMapper.selectByPrimaryKey("prize-1")).thenReturn(prize);

        TicketListResponse response = ticketService.getTicketList("lottery-1");

        assertThat(response.getTickets()).extracting(TicketListResponse.TicketView::getTicketNumber)
                .containsExactly(1, 2, 3);
        assertThat(response.getTickets()).extracting(TicketListResponse.TicketView::getId)
                .containsExactly("ticket-1", "ticket-2", "ticket-3");
    }

    private LotteryTicket ticket(String id, int ticketNumber, String status, String prizeId) {
        LotteryTicket ticket = new LotteryTicket();
        ticket.setId(id);
        ticket.setLotteryId("lottery-1");
        ticket.setTicketNumber(ticketNumber);
        ticket.setStatus(status);
        ticket.setPrizeId(prizeId);
        return ticket;
    }
}
