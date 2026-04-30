package com.group.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.group.admin.entity.Lottery;
import com.group.admin.entity.LotteryPrize;
import com.group.admin.entity.User;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.mapper.LotteryPrizeMapper;
import com.group.admin.mapper.UserMapper;
import com.group.admin.res.draw.DrawResultRes;
import com.group.admin.service.impl.DrawServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("DrawService 單元測試")
class DrawServiceImplTest {

    @Mock private LotteryMapper lotteryMapper;
    @Mock private LotteryPrizeMapper lotteryPrizeMapper;
    @Mock private UserMapper userMapper;
    @Mock private CoinService walletService;
    @Mock private PrizeBoxService prizeBoxService;
    @Mock private ConsumptionRecordService consumptionRecordService;
    @Mock private LotteryService lotteryService;

    @InjectMocks
    private DrawServiceImpl drawService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(drawService, "lotteryService", lotteryService);
    }

    @Test
    @DisplayName("paymentType=BONUS 時應扣紅利並記錄 DRAW_BONUS")
    void executeDraw_BonusPayment_ShouldDeductBonus() {
        Lottery lottery = new Lottery();
        lottery.setId("lottery-1");
        lottery.setTitle("測試商品");
        lottery.setStoreId("store-1");
        lottery.setStatus("ON_SHELF");
        lottery.setPricePerDraw(100L);
        lottery.setPaymentType("BONUS");

        User user = new User();
        user.setId("user-1");
        user.setGoldCoins(50L);
        user.setBonusCoins(1000L);

        LotteryPrize prize = new LotteryPrize();
        prize.setId("prize-1");
        prize.setLotteryId("lottery-1");
        prize.setName("A賞");
        prize.setLevel("A");
        prize.setRemaining(1);
        prize.setWeight(1);
        prize.setQuantity(1);
        prize.setIsGrandPrize((byte) 0);

        when(lotteryMapper.selectByPrimaryKey("lottery-1")).thenReturn(lottery);
        when(userMapper.selectByPrimaryKey("user-1")).thenReturn(user);
        when(lotteryPrizeMapper.selectByExample(any())).thenReturn(List.of(prize));
        when(lotteryPrizeMapper.updateByPrimaryKeySelective(any())).thenReturn(1);

        List<DrawResultRes> results = drawService.executeDraw("user-1", "lottery-1", 1);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCostType()).isEqualTo("BONUS");
        verify(walletService).deductBonus(eq("user-1"), eq(100L), eq("DRAW"), eq("lottery-1"), any());
        verify(walletService, never()).deductGold(any(), any(), any(), any(), any());
        verify(consumptionRecordService).recordConsumption(
                eq("user-1"), eq("DRAW_BONUS"), eq("lottery-1"), eq("測試商品"),
                eq(null), eq(null), eq(0L), eq(100L), any()
        );
        verify(lotteryService).checkAndDelist("lottery-1");
    }
}
