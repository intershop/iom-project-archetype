-- Function: oms.nuke_orders()

-- DROP FUNCTION oms.nuke_orders();
/*
CREATE OR REPLACE FUNCTION oms.nuke_orders()
	RETURNS void AS
$BODY$
BEGIN

	DROP TABLE IF EXISTS "testorderids";
	DROP TABLE IF EXISTS "testinvoiceids";
	create temporary table "testorderids" (id bigint) on commit drop;
	create temporary table "testinvoiceids" (id bigint) on commit drop;
	INSERT INTO "testorderids" select id from "OrderDO" where "shopOrderNo" like '000%';
	INSERT INTO "testinvoiceids" select id FROM "InvoicingDO" where id in (select "invoicingRef" from "InvoicingPosDO" where "orderPosRef" in (select id from "OrderPosDO" where "orderRef" in (select id from testorderids)));
	DELETE FROM "OrderStateAO" WHERE "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "OrderChargeDO" WHERE "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "ShopCustomerMailTransmissionStateHistoryDO"  WHERE "shopCustomerMailTransmissionRef" IN (SELECT "id" FROM "ShopCustomerMailTransmissionDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "ShopCustomerMailTransmissionParameterDO" WHERE "shopCustomerMailTransmissionRef" IN (SELECT "id" FROM "ShopCustomerMailTransmissionDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "ShopCustomerMailTransmission2DocumentDO" WHERE "shopCustomerMailTransmissionRef" IN (SELECT "id" FROM "ShopCustomerMailTransmissionDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "ShopCustomerMailTransmissionDO"  WHERE "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "OrderPosTransmissionDO" WHERE "orderTransmissionRef" IN (SELECT "id" FROM "OrderTransmissionDO"  WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "OrderTransmissionStateHistoryDO" WHERE "orderTransmissionRef" IN (SELECT "id" FROM "OrderTransmissionDO"  WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "OrderTransmissionParameterDO" WHERE "orderTransmissionRef" IN (SELECT "id" FROM "OrderTransmissionDO"  WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "OrderTransmissionDO" WHERE "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "OrderSubTotalTaxGroupDO" WHERE "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "DocumentStateHistoryDO" WHERE "documentRef" IN (SELECT "documentRef" from "Document2OrderDO" where "orderRef" in (SELECT id from testorderids));
	DELETE FROM "DocumentPropertyDO" WHERE "documentRef" IN (SELECT "documentRef" from "Document2OrderDO" where "orderRef" in (SELECT id from testorderids));
	DELETE FROM "InvoicingNoGeneratedDO" WHERE "invoicingRef" IN (SELECT id from testinvoiceids);
	DELETE FROM "InvoicingBalanceDO" WHERE "invoicingRef" IN (SELECT id from testinvoiceids);
	DELETE FROM "InvoicingSalesPricePosDO" WHERE "invoicingPosRef" IN (SELECT "id" FROM "InvoicingPosDO" WHERE "invoicingRef" IN (SELECT id from testinvoiceids));
	DELETE FROM "InvoicingPos2DispatchPosDO" WHERE "invoicingPosRef" IN (SELECT "id" FROM "InvoicingPosDO" WHERE "invoicingRef" IN (SELECT id from testinvoiceids));
	DELETE FROM "InvoicingPos2ReturnPosDO" WHERE "invoicingPosRef" IN (SELECT "id" FROM "InvoicingPosDO" WHERE "invoicingRef" IN (SELECT id from testinvoiceids));
	DELETE FROM "InvoicingPosDO" WHERE "invoicingRef" IN (SELECT id from testinvoiceids);
	DELETE FROM "InvoicingStateHistoryDO"  WHERE "invoicingRef" IN (SELECT id from testinvoiceids);
	DELETE FROM "InvoicingSalesPriceTaxDO" WHERE "invoicingSalesPriceRef" IN (SELECT "id" FROM "InvoicingSalesPriceDO" WHERE "invoicingRef" IN (SELECT id from testinvoiceids));
	DELETE FROM "InvoicingSalesPriceChargeDO" WHERE "invoicingSalesPriceRef"  IN (SELECT "id" FROM "InvoicingSalesPriceDO" WHERE "invoicingRef" IN (SELECT id from testinvoiceids));
	DELETE FROM "InvoicingSalesPriceCouponDO" WHERE "invoicingSalesPriceRef"  IN (SELECT "id" FROM "InvoicingSalesPriceDO" WHERE "invoicingRef" IN (SELECT id from testinvoiceids));
	DELETE FROM "InvoicingSalesPriceDO" WHERE "invoicingRef" IN (SELECT id from testinvoiceids);
	DELETE FROM "Document2InvoicingDO" WHERE "invoicingRef" IN (SELECT id from testinvoiceids);
	DELETE FROM "InvoicingTransmissionParameterDO"  WHERE "invoicingTransmissionRef"  IN (SELECT ID FROM "InvoicingTransmissionDO"  WHERE "invoicingRef" IN (SELECT id from testinvoiceids));
	DELETE FROM "InvoicingTransmissionStateHistoryDO" WHERE "invoicingTransmissionRef"  IN (SELECT ID FROM "InvoicingTransmissionDO"  WHERE "invoicingRef" IN (SELECT id from testinvoiceids));
	DELETE FROM "InvoicingTransmissionDO" WHERE "invoicingRef" IN (SELECT id from testinvoiceids);
	DELETE FROM "PaymentNotification2InvoicingDO" WHERE "invoicingRef" IN (SELECT id from testinvoiceids);
	DELETE FROM "Invoicing2OrderDO" where "invoicingRef" IN (SELECT id from testinvoiceids);
	DELETE FROM "InvoicingDO"  WHERE "id" IN (SELECT id from testinvoiceids);
	DELETE FROM "Document2OrderDO" where "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "DocumentDO" WHERE id IN (SELECT "documentRef" from "Document2OrderDO" where "orderRef" in (SELECT id from testorderids));
	DELETE FROM "OrderAddressDO" WHERE "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "ReturnStateHistoryDO"  WHERE "returnRef" IN (SELECT "id" FROM "ReturnDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "ReturnPosTransmissionPropertyDO" where "returnPosTransmissionRef" in (select id FROM "ReturnPosTransmissionDO" where "returnPosRef" in (select "id" FROM "ReturnPosDO"  WHERE "returnRef" IN (SELECT "id" FROM "ReturnDO" WHERE "orderRef" IN (SELECT id from testorderids))));
	DELETE FROM "ReturnPosTransmissionDO" where "returnPosRef" in (select "id" FROM "ReturnPosDO"  WHERE "returnRef" IN (SELECT "id" FROM "ReturnDO" WHERE "orderRef" IN (SELECT id from testorderids)));
	DELETE FROM "ReturnPosDO"  WHERE "returnRef" IN (SELECT "id" FROM "ReturnDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "OrderCancelStateHistoryDO" WHERE "orderCancelRef" IN (SELECT "id" FROM "OrderCancelDO"  WHERE "returnRef" IN (SELECT "id" FROM "ReturnDO" WHERE "orderRef" IN (SELECT id from testorderids)));
	DELETE FROM "OrderPosCancelDO" WHERE "orderCancelRef" IN (SELECT "id" FROM "OrderCancelDO"  WHERE "returnRef" IN (SELECT "id" FROM "ReturnDO" WHERE "orderRef" IN (SELECT id from testorderids)));
	DELETE FROM "OrderCancelDO"  WHERE "returnRef" IN (SELECT "id" FROM "ReturnDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "PaymentNotification2ReturnDO"  WHERE "returnRef" IN (SELECT "id" FROM "ReturnDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "ReturnTransmissionParameterDO" WHERE "returnTransmissionRef" IN (SELECT ID FROM "ReturnTransmissionDO" WHERE "returnRef"  in (SELECT "id" FROM "ReturnDO" WHERE "orderRef" IN (SELECT id from testorderids)));
	DELETE FROM "ReturnTransmissionPropertyDO" WHERE "returnTransmissionRef" IN (SELECT ID FROM "ReturnTransmissionDO" WHERE "returnRef"  in (SELECT "id" FROM "ReturnDO" WHERE "orderRef" IN (SELECT id from testorderids)));
	DELETE FROM "ReturnTransmissionStateHistoryDO" WHERE "returnTransmissionRef" IN (SELECT ID FROM "ReturnTransmissionDO" WHERE "returnRef"  in (SELECT "id" FROM "ReturnDO" WHERE "orderRef" IN (SELECT id from testorderids)));
	DELETE FROM "ReturnTransmissionDO" WHERE "returnRef"  in (SELECT "id" FROM "ReturnDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "ReturnDO" WHERE "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "OrderTotalTaxGroupDO" WHERE "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "OrderSupplierEvaluationStateHistoryDO" WHERE "orderSupplierEvaluationRef" IN (SELECT "id" FROM "OrderSupplierEvaluationDO"  WHERE "orderPosRef"  IN (SELECT "id" FROM "OrderPosDO" WHERE "orderRef" IN (SELECT id from testorderids)));
	DELETE FROM "OrderSupplierEvaluationDO"  WHERE "orderPosRef"  IN (SELECT "id" FROM "OrderPosDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "ResponsePosDO"  WHERE "orderPosRef"  IN (SELECT "id" FROM "OrderPosDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "OrderPosStateHistoryDO"  WHERE "orderPosRef"  IN (SELECT "id" FROM "OrderPosDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "DispatchPosTransmissionPropertyDO" WHERE "dispatchPosTransmissionRef"  IN (SELECT "id" FROM  "DispatchPosTransmissionDO" WHERE "dispatchPosRef" IN (SELECT "id" FROM "DispatchPosDO"  WHERE "orderPosRef"  IN (SELECT "id" FROM "OrderPosDO" WHERE "orderRef" IN (SELECT id from testorderids))));
	DELETE FROM "DispatchPosTransmissionDO" WHERE "dispatchPosRef" IN (SELECT "id" FROM "DispatchPosDO"  WHERE "orderPosRef"  IN (SELECT "id" FROM "OrderPosDO" WHERE "orderRef" IN (SELECT id from testorderids)));
	DELETE FROM "DispatchPosDO"  WHERE "orderPosRef"  IN (SELECT "id" FROM "OrderPosDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "OrderPosDeliveryOptionDO"  WHERE "orderPosRef"  IN (SELECT "id" FROM "OrderPosDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "ReturnRequestPosDO"  WHERE "orderPosRef"  IN (SELECT "id" FROM "OrderPosDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "OrderPosPropertyDO"  WHERE "orderPosRef"  IN (SELECT "id" FROM "OrderPosDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "PaymentSalesPricePosDO"  WHERE "orderPosRef"  IN (SELECT "id" FROM "OrderPosDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "OrderPosCancelDO"  WHERE "orderPosRef"  IN (SELECT "id" FROM "OrderPosDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "OrderPosDO" WHERE "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "OrderPropertyDO" WHERE "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "OrderStateHistoryDO" WHERE "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "ResponseStateHistoryDO" WHERE "responseRef" IN (SELECT "id" FROM "ResponseDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "ResponseDO" WHERE "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "PaymentInformationDO" WHERE  "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "DispatchStateHistoryDO" WHERE "dispatchRef" IN (SELECT "id" FROM "DispatchDO" where "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "DispatchTransmissionParameterDO" WHERE "dispatchTransmissionRef" IN (SELECT "id" FROM "DispatchTransmissionDO"  WHERE "dispatchRef" IN (SELECT "id" FROM "DispatchDO" WHERE  "orderRef" IN (SELECT id from testorderids)));
	DELETE FROM "DispatchTransmissionPropertyDO" WHERE "dispatchTransmissionRef" IN (SELECT "id" FROM "DispatchTransmissionDO"  WHERE "dispatchRef" IN (SELECT "id" FROM "DispatchDO" WHERE  "orderRef" IN (SELECT id from testorderids)));
	DELETE FROM "DispatchTransmissionStateHistoryDO" WHERE "dispatchTransmissionRef" IN (SELECT "id" FROM "DispatchTransmissionDO"  WHERE "dispatchRef" IN (SELECT "id" FROM "DispatchDO" WHERE  "orderRef" IN (SELECT id from testorderids)));
	DELETE FROM "DispatchTransmissionDO"  WHERE "dispatchRef" IN (SELECT "id" FROM "DispatchDO" WHERE  "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "DispatchDO" WHERE  "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "PaymentNotificationStateHistoryDO" WHERE "paymentNotificationRef" IN (SELECT "id" FROM "PaymentNotificationDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "PaymentNotificationTransmissionParameterDO" WHERE "paymentNotificationTransmissionRef" IN (SELECT "id" FROM "PaymentNotificationTransmissionDO" WHERE "paymentNotificationRef" IN (SELECT "id" FROM "PaymentNotificationDO" WHERE "orderRef" IN (SELECT id from testorderids)));
	DELETE FROM "PaymentNotificationTransmissionStateHistoryDO" WHERE "paymentNotificationTransmissionRef" IN (SELECT "id" FROM "PaymentNotificationTransmissionDO" WHERE "paymentNotificationRef" IN (SELECT "id" FROM "PaymentNotificationDO" WHERE "orderRef" IN (SELECT id from testorderids)));
	DELETE FROM "PaymentNotificationTransmissionDO" WHERE "paymentNotificationRef" IN (SELECT "id" FROM "PaymentNotificationDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "PaymentSalesPriceChargeDO" WHERE "paymentSalesPriceRef"  IN (SELECT "id" FROM "PaymentSalesPriceDO" WHERE "paymentNotificationRef" IN (SELECT "id" FROM "PaymentNotificationDO" WHERE "orderRef" IN (SELECT id from testorderids)));
	DELETE FROM "PaymentSalesPriceTaxDO" WHERE "paymentSalesPriceRef"  IN (SELECT "id" FROM "PaymentSalesPriceDO" WHERE "paymentNotificationRef" IN (SELECT "id" FROM "PaymentNotificationDO" WHERE "orderRef" IN (SELECT id from testorderids)));
	DELETE FROM "PaymentSalesPriceDO" WHERE "paymentNotificationRef" IN (SELECT "id" FROM "PaymentNotificationDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "PaymentNotificationDO" WHERE "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "ReturnRequestStateHistoryDO" WHERE "returnRequestRef"  IN (SELECT "id" FROM "ReturnRequestDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "ReturnRequestDO" WHERE "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "ApprovalResponseStateHistoryDO"  WHERE "approvalResponseRef"  IN (SELECT "id" FROM "ApprovalResponseDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "ApprovalResponseDO" WHERE "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "OrderCouponDO" WHERE "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "OrderNoteDO" WHERE "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "OrderCancelStateHistoryDO"  WHERE "orderCancelRef"  IN (SELECT "id" FROM "OrderCancelDO" WHERE "orderRef" IN (SELECT id from testorderids));
	DELETE FROM "OrderCancelDO" WHERE "orderRef" IN (SELECT id from testorderids);
	DELETE FROM "OrderDO" WHERE id IN (SELECT id from testorderids);

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
*/