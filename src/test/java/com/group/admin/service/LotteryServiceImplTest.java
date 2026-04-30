package com.group.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.admin.entity.Lottery;
import com.group.admin.entity.LotteryPrize;
import com.group.admin.example.LotteryPrizeExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.LotteryDrawRecordMapper;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.mapper.LotteryPrizeMapper;
import com.group.admin.mapper.PointLogMapper;
import com.group.admin.mapper.StoreMapper;
import com.group.admin.mapper.UserMapper;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.lottery.LotteryCondition;
import com.group.admin.req.lottery.LotteryCreateReq;
import com.group.admin.req.lottery.LotteryUpdateReq;
import com.group.admin.res.lottery.LotteryRes;
import com.group.admin.service.impl.LotteryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LotteryService 單元測試")
class LotteryServiceImplTest {

    @Mock private LotteryMapper lotteryMapper;
    @Mock private LotteryPrizeMapper lotteryPrizeMapper;
    @Mock private LotteryDrawRecordMapper drawRecordMapper;
    @Mock private UserMapper userMapper;
    @Mock private PointLogMapper pointLogMapper;
    @Mock private StoreMapper storeMapper;
    @Mock private LotteryTicketService lotteryTicketService;

    private LotteryServiceImpl lotteryService;
    private Lottery insertedLottery;

    @BeforeEach
    void setUp() {
        lotteryService = new LotteryServiceImpl(
                lotteryMapper,
                lotteryPrizeMapper,
                drawRecordMapper,
                userMapper,
                pointLogMapper,
                new ObjectMapper(),
                storeMapper,
                lotteryTicketService
        );
        insertedLottery = null;
        lenient().doAnswer(invocation -> {
            insertedLottery = invocation.getArgument(0, Lottery.class);
            return 1;
        }).when(lotteryMapper).insert(any(Lottery.class));
        lenient().when(lotteryPrizeMapper.selectByExample(any(LotteryPrizeExample.class))).thenReturn(List.of());
    }

    @Test
    @DisplayName("前台上架列表應優先顯示大獎未抽完商品")
    void queryLotteries_OnShelf_ShouldSortActiveBeforeGrandSoldOutAndSoldOut() {
        Lottery soldOut = lottery("sold-out", 10, 10, 0, LocalDateTime.of(2026, 4, 1, 0, 0));
        Lottery grandSoldOut = lottery("grand-sold-out", 10, 2, 1, LocalDateTime.of(2026, 4, 2, 0, 0));
        Lottery active = lottery("active", 10, 2, 9, LocalDateTime.of(2026, 4, 3, 0, 0));

        when(lotteryMapper.selectByExample(any())).thenReturn(List.of(soldOut, grandSoldOut, active));
        when(lotteryPrizeMapper.selectByExample(any(LotteryPrizeExample.class)))
                .thenAnswer(invocation -> filterPrizes(invocation.getArgument(0)));
        when(lotteryPrizeMapper.countByExample(any(LotteryPrizeExample.class)))
                .thenAnswer(invocation -> (long) filterPrizes(invocation.getArgument(0)).size());

        QueryReq<LotteryCondition> req = new QueryReq<>();
        LotteryCondition condition = new LotteryCondition();
        condition.setStatus("ON_SHELF");
        req.setCondition(condition);

        List<LotteryRes> result = lotteryService.queryLotteries(req);

        assertThat(result).extracting(LotteryRes::getId)
                .containsExactly("active", "grand-sold-out", "sold-out");
    }

    private Lottery lottery(String id, int maxDraws, int totalDraws, int orderNum, LocalDateTime createdAt) {
        Lottery lottery = new Lottery();
        lottery.setId(id);
        lottery.setStoreId("store-1");
        lottery.setTitle(id);
        lottery.setCategory("OFFICIAL_ICHIBAN");
        lottery.setPlayMode("LOTTERY_MODE");
        lottery.setPricePerDraw(100L);
        lottery.setStatus("ON_SHELF");
        lottery.setMaxDraws(maxDraws);
        lottery.setTotalDraws(totalDraws);
        lottery.setOrderNum(orderNum);
        lottery.setCreatedAt(createdAt);
        lottery.setUpdatedAt(createdAt);
        return lottery;
    }

    private List<LotteryPrize> filterPrizes(LotteryPrizeExample example) {
        String lotteryId = null;
        boolean grandPrizeOnlyFlag = false;
        boolean remainingPositiveOnlyFlag = false;

        for (LotteryPrizeExample.Criteria criteria : example.getOredCriteria()) {
            for (LotteryPrizeExample.Criterion criterion : criteria.getAllCriteria()) {
                if ("lottery_id =".equals(criterion.getCondition())) {
                    lotteryId = (String) criterion.getValue();
                } else if ("is_grand_prize =".equals(criterion.getCondition())) {
                    grandPrizeOnlyFlag = Byte.valueOf((byte) 1).equals(criterion.getValue());
                } else if ("remaining >".equals(criterion.getCondition())) {
                    remainingPositiveOnlyFlag = true;
                }
            }
        }

        final boolean grandPrizeOnly = grandPrizeOnlyFlag;
        final boolean remainingPositiveOnly = remainingPositiveOnlyFlag;
        List<LotteryPrize> prizes = prizeData().getOrDefault(lotteryId, List.of());
        return prizes.stream()
                .filter(prize -> !grandPrizeOnly || (prize.getIsGrandPrize() != null && prize.getIsGrandPrize() == 1))
                .filter(prize -> !remainingPositiveOnly || (prize.getRemaining() != null && prize.getRemaining() > 0))
                .toList();
    }

    private Map<String, List<LotteryPrize>> prizeData() {
        return Map.of(
                "active", List.of(
                        prize("active-grand", (byte) 1, 1, 1),
                        prize("active-normal", (byte) 0, 7, 9)
                ),
                "grand-sold-out", List.of(
                        prize("grand-sold-out-grand", (byte) 1, 0, 1),
                        prize("grand-sold-out-normal", (byte) 0, 5, 9)
                ),
                "sold-out", List.of(
                        prize("sold-out-grand", (byte) 1, 0, 1),
                        prize("sold-out-normal", (byte) 0, 0, 9)
                )
        );
    }

    private LotteryPrize prize(String id, Byte isGrandPrize, Integer remaining, Integer quantity) {
        LotteryPrize prize = new LotteryPrize();
        prize.setId(id);
        prize.setIsGrandPrize(isGrandPrize);
        prize.setRemaining(remaining);
        prize.setQuantity(quantity);
        return prize;
    }

    @Test
    @DisplayName("CUSTOM_GACHA + SCRATCH_MODE 可接受 freeDrawThreshold = null")
    void createLottery_ScratchMode_NullThreshold_ShouldSucceed() {
        LotteryCreateReq req = new LotteryCreateReq();
        req.setStoreId("store-1");
        req.setTitle("scratch");
        req.setCategory("CUSTOM_GACHA");
        req.setSubCategory("SCRATCH_MODE");
        req.setGameMode("RANDOM");
        req.setPricePerDraw(100L);

        LotteryRes res = lotteryService.createLottery(req);

        assertThat(res.getPlayMode()).isEqualTo("SCRATCH_MODE");
        assertThat(res.getDelistStrategy()).isEqualTo("GRAND_PRIZE_DRAWN");
        assertThat(res.getFreeDrawThreshold()).isNull();
        assertThat(insertedLottery).isNotNull();
        assertThat(insertedLottery.getFreeDrawThreshold()).isNull();
    }

    @Test
    @DisplayName("CUSTOM_GACHA + SCRATCH_MODE 的 freeDrawThreshold = 0 應拒絕")
    void createLottery_ScratchMode_ZeroThreshold_ShouldFail() {
        LotteryCreateReq req = new LotteryCreateReq();
        req.setStoreId("store-1");
        req.setTitle("scratch");
        req.setCategory("CUSTOM_GACHA");
        req.setSubCategory("SCRATCH_MODE");
        req.setGameMode("SCRATCH_STORE");
        req.setPricePerDraw(100L);
        req.setFreeDrawThreshold(0);

        assertThrows(BusinessException.class, () -> lotteryService.createLottery(req));
    }

    @Test
    @DisplayName("CUSTOM_GACHA + LOTTERY_MODE 會清空 gameMode 與 freeDrawThreshold")
    void createLottery_CustomLotteryMode_ShouldNormalizeFields() {
        LotteryCreateReq req = new LotteryCreateReq();
        req.setStoreId("store-1");
        req.setTitle("custom-lottery");
        req.setCategory("CUSTOM_GACHA");
        req.setSubCategory("LOTTERY_MODE");
        req.setGameMode("SCRATCH_PLAYER");
        req.setPricePerDraw(100L);
        req.setFreeDrawThreshold(5);

        LotteryRes res = lotteryService.createLottery(req);

        assertThat(res.getPlayMode()).isEqualTo("LOTTERY_MODE");
        assertThat(res.getGameMode()).isNull();
        assertThat(res.getFreeDrawThreshold()).isNull();
        assertThat(res.getDelistStrategy()).isEqualTo("ALL_DRAWN");
        assertThat(insertedLottery.getGameMode()).isNull();
        assertThat(insertedLottery.getFreeDrawThreshold()).isNull();
    }

    @Test
    @DisplayName("未指定 paymentType 時應預設為 GOLD")
    void createLottery_PaymentTypeMissing_ShouldDefaultGold() {
        LotteryCreateReq req = new LotteryCreateReq();
        req.setStoreId("store-1");
        req.setTitle("payment-default");
        req.setCategory("OFFICIAL_ICHIBAN");
        req.setPricePerDraw(100L);
        req.setDelistStrategy("MANUAL");

        LotteryRes res = lotteryService.createLottery(req);

        assertThat(res.getPaymentType()).isEqualTo("GOLD");
        assertThat(insertedLottery.getPaymentType()).isEqualTo("GOLD");
    }

    @Test
    @DisplayName("非 DRAFT 商品不可修改 paymentType")
    void updateLottery_NonDraftChangePaymentType_ShouldFail() {
        Lottery existing = new Lottery();
        existing.setId("lottery-1");
        existing.setStatus("ON_SHELF");
        existing.setCategory("OFFICIAL_ICHIBAN");
        existing.setSubCategory(null);
        existing.setGameMode("TICKET");
        existing.setPlayMode("LOTTERY_MODE");
        existing.setPaymentType("GOLD");
        existing.setDelistStrategy("MANUAL");
        when(lotteryMapper.selectByPrimaryKey("lottery-1")).thenReturn(existing);

        LotteryUpdateReq req = new LotteryUpdateReq();
        req.setPaymentType("BONUS");

        assertThrows(BusinessException.class, () -> lotteryService.updateLottery("lottery-1", req));
    }
}
