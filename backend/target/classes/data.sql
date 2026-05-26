INSERT INTO dashboard_daily_metric (stat_date, order_count, new_user_count, pending_payment_count, sales_amount, yesterday_sales_amount)
SELECT DATE '2026-05-20', 2100, 1200, 55, 52000.00, 48000.00
WHERE NOT EXISTS (SELECT 1 FROM dashboard_daily_metric WHERE stat_date = DATE '2026-05-20');

INSERT INTO dashboard_daily_metric (stat_date, order_count, new_user_count, pending_payment_count, sales_amount, yesterday_sales_amount)
SELECT DATE '2026-05-21', 2300, 1350, 60, 54800.00, 52000.00
WHERE NOT EXISTS (SELECT 1 FROM dashboard_daily_metric WHERE stat_date = DATE '2026-05-21');

INSERT INTO dashboard_daily_metric (stat_date, order_count, new_user_count, pending_payment_count, sales_amount, yesterday_sales_amount)
SELECT DATE '2026-05-22', 2400, 1400, 62, 56000.00, 54800.00
WHERE NOT EXISTS (SELECT 1 FROM dashboard_daily_metric WHERE stat_date = DATE '2026-05-22');

INSERT INTO dashboard_daily_metric (stat_date, order_count, new_user_count, pending_payment_count, sales_amount, yesterday_sales_amount)
SELECT DATE '2026-05-23', 2500, 1500, 65, 58000.00, 56000.00
WHERE NOT EXISTS (SELECT 1 FROM dashboard_daily_metric WHERE stat_date = DATE '2026-05-23');

INSERT INTO dashboard_daily_metric (stat_date, order_count, new_user_count, pending_payment_count, sales_amount, yesterday_sales_amount)
SELECT DATE '2026-05-24', 2600, 1580, 66, 61200.00, 58000.00
WHERE NOT EXISTS (SELECT 1 FROM dashboard_daily_metric WHERE stat_date = DATE '2026-05-24');

INSERT INTO dashboard_daily_metric (stat_date, order_count, new_user_count, pending_payment_count, sales_amount, yesterday_sales_amount)
SELECT DATE '2026-05-25', 2550, 1620, 67, 63000.00, 61200.00
WHERE NOT EXISTS (SELECT 1 FROM dashboard_daily_metric WHERE stat_date = DATE '2026-05-25');

INSERT INTO dashboard_daily_metric (stat_date, order_count, new_user_count, pending_payment_count, sales_amount, yesterday_sales_amount)
SELECT DATE '2026-05-26', 2654, 1652, 68, 65658.87, 45258.51
WHERE NOT EXISTS (SELECT 1 FROM dashboard_daily_metric WHERE stat_date = DATE '2026-05-26');

INSERT INTO dashboard_pending_task (task_key, label, count_value, sort_num)
SELECT 'pendingShipment', '待发货订单', 154, 1
WHERE NOT EXISTS (SELECT 1 FROM dashboard_pending_task WHERE task_key = 'pendingShipment');

INSERT INTO dashboard_pending_task (task_key, label, count_value, sort_num)
SELECT 'pendingRefund', '待处理退款', 1524, 2
WHERE NOT EXISTS (SELECT 1 FROM dashboard_pending_task WHERE task_key = 'pendingRefund');

INSERT INTO dashboard_pending_task (task_key, label, count_value, sort_num)
SELECT 'pendingReceipt', '待确认收货', 16, 3
WHERE NOT EXISTS (SELECT 1 FROM dashboard_pending_task WHERE task_key = 'pendingReceipt');

INSERT INTO dashboard_pending_task (task_key, label, count_value, sort_num)
SELECT 'pendingAfterSale', '待处理售后', 24, 4
WHERE NOT EXISTS (SELECT 1 FROM dashboard_pending_task WHERE task_key = 'pendingAfterSale');

INSERT INTO dashboard_pending_task (task_key, label, count_value, sort_num)
SELECT 'outOfStock', '缺货登记', 45, 5
WHERE NOT EXISTS (SELECT 1 FROM dashboard_pending_task WHERE task_key = 'outOfStock');

INSERT INTO dashboard_pending_task (task_key, label, count_value, sort_num)
SELECT 'pendingVerify', '待核销订单', 487, 6
WHERE NOT EXISTS (SELECT 1 FROM dashboard_pending_task WHERE task_key = 'pendingVerify');

INSERT INTO dashboard_pending_task (task_key, label, count_value, sort_num)
SELECT 'pendingReview', '待评价回复', 25, 7
WHERE NOT EXISTS (SELECT 1 FROM dashboard_pending_task WHERE task_key = 'pendingReview');

INSERT INTO dashboard_pending_task (task_key, label, count_value, sort_num)
SELECT 'adExpire', '广告位到期提醒', 8, 8
WHERE NOT EXISTS (SELECT 1 FROM dashboard_pending_task WHERE task_key = 'adExpire');
