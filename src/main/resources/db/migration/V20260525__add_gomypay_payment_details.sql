ALTER TABLE recharge_order
  ADD COLUMN payment_method VARCHAR(30) NULL AFTER status,
  ADD COLUMN gateway_result VARCHAR(20) NULL AFTER gateway_order_id,
  ADD COLUMN gateway_ret_msg VARCHAR(255) NULL AFTER gateway_result,
  ADD COLUMN virtual_account VARCHAR(100) NULL AFTER gateway_ret_msg,
  ADD COLUMN payment_info VARCHAR(255) NULL AFTER virtual_account,
  ADD COLUMN limit_date VARCHAR(50) NULL AFTER payment_info;

ALTER TABLE `order`
  ADD COLUMN gateway_result VARCHAR(20) NULL AFTER gomypay_trade_no,
  ADD COLUMN gateway_ret_msg VARCHAR(255) NULL AFTER gateway_result,
  ADD COLUMN virtual_account VARCHAR(100) NULL AFTER gateway_ret_msg,
  ADD COLUMN payment_info VARCHAR(255) NULL AFTER virtual_account,
  ADD COLUMN limit_date VARCHAR(50) NULL AFTER payment_info,
  ADD COLUMN gateway_raw_resp TEXT NULL AFTER limit_date;
