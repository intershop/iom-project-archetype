DO $$
BEGIN

/*
	INSERT INTO oms."DecisionBeanDefDO"(id, description) values
		(1000, 'InvoicingDecider'),
		(1001, 'PaymentActionDecider'),
		(1010, 'IsDefectReturnDeciderBean'),
		(1050, 'BrontoSendEmailDecid'),
		(2000, 'PaymentApproval'),
		(2001, 'FraudApproval'),
		(2010, 'ReplacementApprovalDecisionBean')

	    ON CONFLICT (id) DO NOTHING;

	INSERT INTO oms."DocumentMapperDefDO"(id,name) values
		(1000, 'sharkninjaReturnSlipMapper')
		
		ON CONFLICT (id) DO NOTHING;
	    
	INSERT INTO oms."ExecutionBeanDefDO"(id, "decisionBeanDefRef", description) VALUES
		(1000, null, 'PayPalMessageTransmitterBean'),
		(1001, null, 'StripeMessageTransmitterBean'),
		(1003, null, 'BrontoSenderBean'),
		(1004, null, 'VeritasMessageTransmitterBean'),
		(1010, null, 'ApproveReplacementOrderBean'),
		(1011, null, 'ReturnAnnouncementTransmitterBean'),
		(1012, null, 'UPSReturnLabelDocumentCreationBean'),
		(1013, null, 'DummyDocumentCreationBean'),
		(1014, null, 'DPDReturnLabelDocumentCreationBean')

	ON CONFLICT (id) DO NOTHING;

	INSERT INTO oms."ExecutionBeanKeyDefDO"(
            id, "executionBeanDefRef", "parameterKey", "parameterTypeDefRef",
            mandatory, "defaultValue", "activeOMT") VALUES
	    (11200, 1003, 'shopEmailAddress', 11, true, null, true),
	    (11201, 1003, 'shopEmailSenderName', 11, false, null, true)

	ON CONFLICT (id) DO NOTHING;

	INSERT INTO oms."JobDefDO"(id, name, description) VALUES
		(1000, 'PayPalCheckRefundJob', 'PayPalCheckRefundJob'),
		(1001, 'StripeCheckRefundJob', 'StripeCheckRefundJob'),
		(1002, 'ProcessWebhooksJob', 'ProcessWebhooksJob'),
		(1003, 'InventoryExportJob', 'InventoryExportJob')

	ON CONFLICT (id) DO NOTHING;

	INSERT INTO oms."MessageTypeDefDO"(id, name, description) VALUES
		(10500, 'Send customer mail - order', 'Send customer mail - order'),
		(10503, 'Send customer mail - transmission to carrier', 'Send customer mail - transmission to carrier'),
		(10530, 'Send customer mail - dispatch', 'Send customer mail - dispatch'),
		(10532, 'Send customer mail - dispatch including invoice', 'Send customer mail - dispatch including invoice'),
		(10545, 'Send customer mail - return including credit note', 'Send customer mail - return including credit note'),
		(10549, 'Send customer mail - return announcement transmiss', 'Send customer mail - return announcement transmiss')

	ON CONFLICT (id) DO NOTHING;
	*/
/*
	INSERT INTO oms."OrderSupplierEvaluationRuleDefDO"(id, description, mandatory, name, rank) VALUES
	    (1000, 'ElefantPreferredSupplierCheck', false, 'ElefantPreferredSupplierCheck', 30),
	    (1001, 'QuantityCheck', false, 'QuantityCheck', 20),
	    (1002, 'ElefantPurchasePriceCheck', false, 'ElefantPurchasePriceCheck', 500),
	    (1003, 'StdAvailabilityCheckPTBean', false, 'StdAvailabilityCheckPTBean', 101)

	ON CONFLICT (id) DO NOTHING;
*/

/*
	INSERT INTO oms."OrderValidationRuleDefDO"(id, name, rank, mandatory, description) VALUES
		(10000, 'ValidateMandatoryPropertiesPTBean', 999, false, 'ValidateMandatoryPropertiesPTBean'),
		(10001, 'ValidateOrderAddressPTBean', 285, false, 'ValidateOrderAddressPTBean'),
		(10010, 'ValidateReplacementOrderPTBean', 290, false, 'ValidateReplacementOrderPTBean'),
		(10011, 'ValidateBrontoContactPTBean', 1000, false, 'ValidateBrontoContactPTBean')

	ON CONFLICT (id) DO NOTHING;

	INSERT INTO oms."TransformerBeanDefDO"(id, name) VALUES
		(1000, 'ICMToIOMTransformer')
	ON CONFLICT (id) DO NOTHING;

	INSERT INTO oms."TransmissionTypeDefDO"(id, name, "roleDefRef", description, "messageTypeName") VALUES
		(10500, 'brontoSendCustomerMailOrder', 6, 'brontoSendCustomerMailOrder', 'BRONTO_SEND_CUSTOMER_MAIL_ORDER'),
		(10503, 'brontoSendCustomerMailOrderCommissioned', 6, 'brontoSendCustomerMailOrderCommissioned', 'BRONTO_SEND_CUSTOMER_MAIL_ORDER_COMMISSIONED'),
		(10530, 'brontoSendCustomerMailDispatch', 6, 'brontoSendCustomerMailDispatch', 'BRONTO_SEND_CUSTOMER_MAIL_DISPATCH'),
		(10700, 'brontoSendCustomerMailDispatchInvoice', 6, 'brontoSendCustomerMailDispatchInvoice', 'BRONTO_SEND_CUSTOMER_MAIL_DISPATCH_INVOICE'),
		(10703, 'brontoSendCustomerMailReturnCreditnote_RCL', 6, 'brontoSendCustomerMailReturnCreditnote_RCL', 'BRONTO_SEND_CUSTOMER_MAIL_RETURN_CREDITNOTE'),
		(10705, 'brontoSendCustomerMailReturnCreditnote_RET', 6, 'brontoSendCustomerMailReturnCreditnote_RET', 'BRONTO_SEND_CUSTOMER_MAIL_RETURN_CREDITNOTE'),
		(10715, 'brontoSendCustomerMailReturnAnnouncementTrans', 6, 'brontoSendCustomerMailReturnAnnouncementTrans', 'BRONTO_SEND_CUSTOMER_MAIL_RETURN_ANNOUNCEMENT_TRAN')

	ON CONFLICT (id) DO NOTHING;

	INSERT INTO oms."EventDefDO"(id, description) VALUES
		(1001, 'CUSTOM_MAIL_EVENT_MANAGER_BEAN')
	ON CONFLICT (id) DO NOTHING;
*/
END;
$$;
