package com.group.admin.service;

import com.group.admin.entity.Lottery;
import com.group.admin.entity.LotteryPrize;
import com.group.admin.entity.LotterySession;
import com.group.admin.entity.LotteryTicket;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.mapper.LotteryPrizeMapper;
import com.group.admin.mapper.LotterySessionMapper;
import com.group.admin.mapper.LotteryTicketMapper;
import com.group.admin.res.lottery.DesignationCheckResponse;
import com.group.admin.res.lottery.TicketListResponse;
import com.group.admin.service.impl.LotteryTicketServiceImpl;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LotteryTicketServiceImplTest")
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
    @DisplayName("getTicketList 會依 ticketNumber 穩定排序")
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

    @Test
    @DisplayName("designation-check 會從已落庫指定結果回補已指定狀態")
    void getDesignationStatus_ShouldRecoverFromPersistedPlayerDesignation() {
        Lottery lottery = new Lottery();
        lottery.setId("lottery-1");
        lottery.setGameMode("SCRATCH_PLAYER");

        LotterySession session = new LotterySession();
        session.setId("session-1");
        session.setLotteryId("lottery-1");
        session.setStatus("ACTIVE");
        session.setOpenerUserId("user-1");

        LotteryTicket designatedTicket = new LotteryTicket();
        designatedTicket.setLotteryId("lottery-1");
        designatedTicket.setRevealedNumber(22);
        designatedTicket.setIsDesignatedPrize((byte) 1);
        designatedTicket.setDesignatedBy("PLAYER");

        when(lotteryMapper.selectByPrimaryKey("lottery-1")).thenReturn(lottery);
        when(lotterySessionMapper.selectByExample(any())).thenReturn(List.of(session));
        when(lotteryTicketMapper.selectByExample(any())).thenReturn(List.of(designatedTicket));

        DesignationCheckResponse response = ticketService.getDesignationStatus("lottery-1", "user-1");

        assertThat(response.isRequired()).isFalse();
        assertThat(response.getAlreadyDesignated()).isTrue();
        verify(lotterySessionMapper).updateByPrimaryKey(any(LotterySession.class));
    }

    @Test
    @DisplayName("已存在玩家指定結果時不可再次指定")
    void designatePrizePositions_ShouldRejectAlreadyDesignatedSession() {
        Lottery lottery = new Lottery();
        lottery.setId("lottery-1");
        lottery.setGameMode("SCRATCH_PLAYER");

        LotterySession session = new LotterySession();
        session.setId("session-1");
        session.setLotteryId("lottery-1");
        session.setStatus("ACTIVE");
        session.setOpenerUserId("user-1");

        LotteryTicket designatedTicket = new LotteryTicket();
        designatedTicket.setLotteryId("lottery-1");
        designatedTicket.setRevealedNumber(22);
        designatedTicket.setIsDesignatedPrize((byte) 1);
        designatedTicket.setDesignatedBy("PLAYER");

        when(lotteryMapper.selectByPrimaryKey("lottery-1")).thenReturn(lottery);
        when(lotterySessionMapper.selectByExample(any())).thenReturn(List.of(session));
        when(lotteryTicketMapper.selectByExample(any()))
                .thenReturn(List.of(designatedTicket))
                .thenReturn(List.of(designatedTicket));

        assertThatThrownBy(() -> ticketService.designatePrizePositions(
                "lottery-1",
                "user-1",
                List.of(new LotteryTicketService.PrizeDesignation(22, "prize-grand"))))
                .hasMessageContaining("ALREADY_DESIGNATED");
    }

    @Test
    @DisplayName("非開套玩家不可指定大獎位置")
    void designatePrizePositions_ShouldRejectNonOpener() {
        Lottery lottery = new Lottery();
        lottery.setId("lottery-1");
        lottery.setGameMode("SCRATCH_PLAYER");

        LotterySession session = new LotterySession();
        session.setId("session-1");
        session.setLotteryId("lottery-1");
        session.setStatus("ACTIVE");
        session.setOpenerUserId("opener");

        when(lotteryMapper.selectByPrimaryKey("lottery-1")).thenReturn(lottery);
        when(lotterySessionMapper.selectByExample(any())).thenReturn(List.of(session));
        when(lotteryTicketMapper.selectByExample(any()))
                .thenReturn(List.of())
                .thenReturn(List.of());

        assertThatThrownBy(() -> ticketService.designatePrizePositions(
                "lottery-1",
                "other-user",
                List.of(new LotteryTicketService.PrizeDesignation(22, "prize-grand"))))
                .hasMessageContaining("NOT_OPENER");
    }

    @Test
    @DisplayName("重複的 revealedNumber 應被擋下")
    void designatePrizePositions_ShouldRejectDuplicateRevealedNumber() {
        Lottery lottery = new Lottery();
        lottery.setId("lottery-1");
        lottery.setGameMode("SCRATCH_PLAYER");

        LotterySession session = new LotterySession();
        session.setId("session-1");
        session.setLotteryId("lottery-1");
        session.setStatus("ACTIVE");
        session.setOpenerUserId("user-1");

        LotteryPrize grandPrize = new LotteryPrize();
        grandPrize.setId("prize-grand");
        grandPrize.setLotteryId("lottery-1");
        grandPrize.setLevel("GRAND");
        grandPrize.setIsGrandPrize((byte) 1);
        grandPrize.setQuantity(2);

        LotteryTicket availableTicket = new LotteryTicket();
        availableTicket.setId("ticket-1");
        availableTicket.setLotteryId("lottery-1");
        availableTicket.setStatus("AVAILABLE");
        availableTicket.setRevealedNumber(22);

        when(lotteryMapper.selectByPrimaryKey("lottery-1")).thenReturn(lottery);
        when(lotterySessionMapper.selectByExample(any())).thenReturn(List.of(session));
        when(lotteryTicketMapper.selectByExample(any()))
                .thenReturn(List.of())
                .thenReturn(List.of())
                .thenReturn(List.of(availableTicket));
        when(lotteryPrizeMapper.selectByExample(any())).thenReturn(List.of(grandPrize));
        when(lotteryPrizeMapper.selectByPrimaryKey("prize-grand")).thenReturn(grandPrize);

        assertThatThrownBy(() -> ticketService.designatePrizePositions(
                "lottery-1",
                "user-1",
                List.of(
                        new LotteryTicketService.PrizeDesignation(22, "prize-grand"),
                        new LotteryTicketService.PrizeDesignation(22, "prize-grand"))))
                .hasMessageContaining("DUPLICATE_REVEALED_NUMBER");
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
