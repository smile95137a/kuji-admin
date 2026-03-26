<!-- src/views/DemoDialogs.vue (整份可直接貼上用) -->
<template>
  <div class="demo">
    <div class="demo__actions">
      <!-- 原本：完整流程 -->
      <button class="demo__btn" @click="openGacha10" :disabled="isBusy">
        打開抽卡（10 次）
      </button>

      <!-- 原本：只 Demo 結果 Dialog -->
      <button
        class="demo__btn demo__btn--secondary"
        @click="openResultOnly"
        :disabled="isBusy"
      >
        Demo 結果 Dialog（不撕卡）
      </button>

      <!-- 原本：扭蛋機 Dialog Demo -->
      <button
        class="demo__btn demo__btn--gotcha"
        @click="openGotchaDialog"
        :disabled="isBusy"
      >
        Demo 扭蛋機 Dialog（1~50 次）
      </button>

      <!-- ✅ 新增：刮刮樂（單抽） -->
      <button
        class="demo__btn demo__btn--scratch"
        @click="openScratchSingle"
        :disabled="isBusy"
      >
        Demo 刮刮樂（單抽）
      </button>

      <!-- ✅ 新增：刮刮樂（十連抽） -->
      <button
        class="demo__btn demo__btn--scratch2"
        @click="openScratchTen"
        :disabled="isBusy || isScratchTenDrawing"
      >
        Demo 刮刮樂（十連抽）
      </button>

      <!-- ✅ 新增：Ichiban Info Dialog -->
      <button
        class="demo__btn demo__btn--info"
        @click="openIchibanInfo"
        :disabled="isBusy"
      >
        Demo Ichiban Info Dialog
      </button>

      <!-- ✅ 新增：Ichiban Confirm Dialog -->
      <button
        class="demo__btn demo__btn--confirm"
        @click="openIchibanConfirm"
        :disabled="isBusy"
      >
        Demo Ichiban Confirm Dialog
      </button>

      <!-- ✅✅ 新增：Ichiban Forgot Password Dialog -->
      <button
        class="demo__btn demo__btn--forgot"
        @click="openIchibanForgotPassword"
        :disabled="isBusy"
      >
        Demo Ichiban Forgot Password Dialog
      </button>
    </div>

    <!-- 可選：提示 -->
    <p class="demo__hint" v-if="isScratchTenDrawing">
      正在進行刮刮樂十連抽⋯⋯（中途關閉任一張就會中斷後面抽卡）
    </p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useOverlayStore } from '@/stores/overlay';

import demo1 from '@/assets/image/demo1.jpg';

import { gachaTearDialog } from '@/utils/dialog/kujiRevealStripDialog';
import { ichibanResultDialog } from '@/utils/dialog/ichibanResultDialog';
import { ichibanResultCardDialog } from '@/utils/dialog/ichibanResultCardDialog';
import { gotchaDialog } from '@/utils/dialog/gotchaDialog';
import { scratchCardDialog } from '@/utils/dialog/scratchCardDialog';

import { ichibanInfoDialog } from '@/utils/dialog/ichibanInfoDialog';
import { ichibanConfirmDialog } from '@/utils/dialog/ichibanConfirmDialog';

/** ✅✅ 新增：忘記密碼 Dialog */
import { ichibanForgotPasswordDialog } from '@/utils/dialog/ichibanForgotPasswordDialog';

const overlay = useOverlayStore();

const isBusy = ref(false);
const isScratchTenDrawing = ref(false);

/**
 * mock 獎品資料（之後你可以換成 API 回傳）
 */
function randomGrade(): string {
  const code = 'A'.charCodeAt(0) + Math.floor(Math.random() * 26);
  return String.fromCharCode(code); // 'A' ~ 'Z'
}

function createPrizeList(count: number) {
  return Array.from({ length: count }, (_, i) => {
    const grade = randomGrade();

    return {
      id: `prize-${i}`,
      name: '「春秋戰國大戰王者天下」The Animation 秦國的末裔 MASTERLISE',
      image: demo1,
      grade,
    };
  });
}

/** ====== 刮刮樂：十連抽假資料 ====== */
type Grade = 'A' | 'B' | 'C' | 'D' | 'Last';

interface Prize {
  grade: Grade;
  name: string;
  imageSrc: string;
}

const TEN_PRIZES: Prize[] = [
  { grade: 'A', name: '皮卡丘超大抱枕', imageSrc: demo1 },
  { grade: 'B', name: '皮卡丘馬克杯', imageSrc: demo1 },
  { grade: 'B', name: '皮卡丘環保袋', imageSrc: demo1 },
  { grade: 'C', name: '皮卡丘貼紙組', imageSrc: demo1 },
  { grade: 'C', name: '皮卡丘鑰匙圈', imageSrc: demo1 },
  { grade: 'D', name: '皮卡丘徽章', imageSrc: demo1 },
  { grade: 'D', name: '皮卡丘小吊飾', imageSrc: demo1 },
  { grade: 'D', name: '皮卡丘明信片', imageSrc: demo1 },
  { grade: 'Last', name: '最後一抽特別賞・大抱枕套組', imageSrc: demo1 },
  { grade: 'D', name: '皮卡丘資料夾', imageSrc: demo1 },
];

/**
 * 原本：完整流程（撕卡 -> 結果）
 */
async function openGacha10() {
  if (isBusy.value) return;
  isBusy.value = true;

  const randomCount = Math.floor(Math.random() * 50) + 1; // 1~50
  const prizeList = createPrizeList(randomCount);

  const pulls = prizeList.map((prize, index) => ({
    index,
    ...prize,
    title: `今日一番賞・第 ${index + 1} 抽`,
  }));

  overlay.open('gacha-tear');

  try {
    const tearResult = await gachaTearDialog({ pulls });
    if (!tearResult) return;

    await ichibanResultDialog({
      remain: 71,
      count: prizeList.length,
      totalPrice: prizeList.length * 120,
      items: prizeList,
    });
  } finally {
    overlay.close();
    isBusy.value = false;
  }
}

/**
 * 原本：只 Demo 結果 Dialog（不撕卡）
 */
async function openResultOnly() {
  if (isBusy.value) return;
  isBusy.value = true;

  const randomCount = Math.floor(Math.random() * 50) + 1; // 1~50
  const prizeList = createPrizeList(randomCount);

  overlay.open('gacha-tear', false);

  try {
    const again = await ichibanResultCardDialog({
      remain: 88,
      count: prizeList.length,
      totalPrice: prizeList.length * 120,
      items: prizeList,
    });

    console.log('[Result Only Demo] 再抽一次？', again);
  } finally {
    overlay.close();
    isBusy.value = false;
  }
}

/**
 * 原本：扭蛋機 Dialog Demo
 */
async function openGotchaDialog() {
  if (isBusy.value) return;
  isBusy.value = true;

  const randomCount = Math.floor(Math.random() * 50) + 1; // 1~50
  const prizeList = createPrizeList(randomCount);

  const pulls = prizeList.map((prize, index) => ({
    title: `今日一番賞・第 ${index + 1} 抽`,
    image: prize.image,
    prize,
    index,
  }));

  overlay.open('gacha-tear');

  try {
    const results = await gotchaDialog({
      title: '扭蛋機抽獎中',
      pulls,
      speed: 0.6,
    });

    if (!results) return;

    await ichibanResultDialog({
      remain: 71,
      count: prizeList.length,
      totalPrice: prizeList.length * 120,
      items: prizeList,
    });

    console.log('[Gotcha Dialog Demo] results=', results);
  } finally {
    overlay.close();
    isBusy.value = false;
  }
}

/** ✅ 刮刮樂（單抽） */
async function openScratchSingle() {
  if (isBusy.value) return;
  isBusy.value = true;

  overlay.open('scratch-card');

  try {
    const ok = await scratchCardDialog({
      title: '今日一番賞・刮刮樂（單抽）',
      imageSrc: demo1,
      idleText: '刮開看看，抽到什麼賞？',
      revealText: '恭喜抽中 A賞！',
      threshold: 45,
      grade: 'A',
    });

    console.log('[Scratch Single] ok=', ok);
  } finally {
    overlay.close();
    isBusy.value = false;
  }
}

/** ✅ 刮刮樂（十連抽） */
async function openScratchTen() {
  if (isBusy.value || isScratchTenDrawing.value) return;

  isBusy.value = true;
  isScratchTenDrawing.value = true;

  overlay.open('scratch-card');

  let finished = 0;
  let completed = 0;
  let canceledAt: number | undefined;

  const gradeCounts: Record<Grade, number> = {
    A: 0,
    B: 0,
    C: 0,
    D: 0,
    Last: 0,
  };

  try {
    for (let i = 0; i < TEN_PRIZES.length; i++) {
      const drawNo = i + 1;
      const prize = TEN_PRIZES[i];

      const ok = await scratchCardDialog({
        title: `今日一番賞・十連抽（第 ${drawNo} 張｜${prize.grade}賞）`,
        imageSrc: prize.imageSrc,
        idleText: `第 ${drawNo} 張，刮開看看抽到什麼賞？`,
        revealText: `第 ${drawNo} 張：${prize.grade}賞・${prize.name}`,
        threshold: 45,
        grade: prize.grade,
      });

      finished++;

      if (!ok) {
        canceledAt = drawNo;
        break;
      }

      completed++;
      gradeCounts[prize.grade] += 1;
    }

    console.log('[Scratch Ten] summary=', {
      finished,
      completed,
      canceledAt,
      gradeCounts,
    });
  } finally {
    overlay.close();
    isScratchTenDrawing.value = false;
    isBusy.value = false;
  }
}

/** ✅ Demo Ichiban Info Dialog */
async function openIchibanInfo() {
  if (isBusy.value) return;
  isBusy.value = true;

  overlay.open('ichiban-info', false);

  try {
    const ok = await ichibanInfoDialog({
      title: '提示訊息',
      content: `<b>儲存成功</b><br/>已完成新增`,
      data: {
        id: 'hello',
        role: 'dialog',
        'aria-label': 'info-dialog',
      },
    });

    console.log('[Ichiban Info Demo] ok=', ok);
  } finally {
    overlay.close();
    isBusy.value = false;
  }
}

/** ✅ Demo Ichiban Confirm Dialog */
async function openIchibanConfirm() {
  if (isBusy.value) return;
  isBusy.value = true;

  overlay.open('ichiban-confirm', false);

  try {
    const ok = await ichibanConfirmDialog({
      title: '刪除確認',
      content: `確定要刪除這筆資料嗎？<br/><b>刪除後無法復原</b>`,
      data: {
        id: 'confirm-delete',
        role: 'dialog',
        'aria-label': 'delete-confirm',
      },
    });

    console.log('[Ichiban Confirm Demo] ok=', ok);
  } finally {
    overlay.close();
    isBusy.value = false;
  }
}

/* =========================================================
   ✅✅ 新增：Demo Ichiban Forgot Password Dialog
   - 回傳 email 或 null
========================================================= */
async function openIchibanForgotPassword() {
  if (isBusy.value) return;
  isBusy.value = true;

  overlay.open('ichiban-forgot-password', false);

  try {
    const email = await ichibanForgotPasswordDialog({
      title: '忘記密碼',
      content: `請輸入你的 <b>Email</b><br/>我們會寄送重設密碼連結給你`,
      confirmText: '送出',
      cancelText: '取消',
      placeholder: '請輸入 Email，例如：test@gmail.com',
      defaultEmail: 'test@gmail.com',
      hint: '※ 信件可能會在垃圾郵件，請稍微找一下',
      data: {
        id: 'forgot-password',
        role: 'dialog',
        'aria-label': 'forgot-password-dialog',
      },
    });

    console.log('[Ichiban Forgot Password Demo] email =', email);

    // ✅ 你要接 API 的話可以直接用這個 email
    // if (email) { await forgotPasswordApi({ email }) }
  } finally {
    overlay.close();
    isBusy.value = false;
  }
}
</script>

<style scoped>
.demo {
  min-height: 100vh;
  padding: 28px 18px;
}

.demo__actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.demo__btn {
  padding: 12px 18px;
  border-radius: 14px;
  border: 0;
  cursor: pointer;
  font-weight: 800;
  background: linear-gradient(135deg, #f59e0b, #facc15);
  color: #111827;
}

.demo__btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.demo__btn--secondary {
  background: linear-gradient(135deg, #38bdf8, #a78bfa);
  color: #0b1220;
}

.demo__btn--gotcha {
  background: linear-gradient(135deg, #34d399, #22c55e);
  color: #052e16;
}

.demo__btn--scratch {
  background: linear-gradient(135deg, #fb7185, #f97316);
  color: #111827;
}

.demo__btn--scratch2 {
  background: linear-gradient(135deg, #f472b6, #818cf8);
  color: #0b1220;
}

.demo__btn--info {
  background: linear-gradient(135deg, #22c55e, #a3e635);
  color: #052e16;
}

.demo__btn--confirm {
  background: linear-gradient(135deg, #fb7185, #ef4444);
  color: #1f2937;
}

/* ✅✅ Forgot Password 按鈕 */
.demo__btn--forgot {
  background: linear-gradient(135deg, #f97316, #f59e0b);
  color: #1f2937;
}

.demo__hint {
  margin-top: 12px;
  font-size: 13px;
  color: #6b7280;
}
</style>

