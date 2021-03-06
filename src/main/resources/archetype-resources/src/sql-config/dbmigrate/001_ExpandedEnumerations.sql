DO $$
BEGIN

/*
	INSERT INTO oms."DecisionBeanDefDO"(id, description) values
		(1000, 'Example')

	    ON CONFLICT (id) DO NOTHING;

	INSERT INTO oms."DocumentMapperDefDO"(id,name) values
		(1000, 'Example')
		
		ON CONFLICT (id) DO NOTHING;
	    
	INSERT INTO oms."ExecutionBeanDefDO"(id, "decisionBeanDefRef", description) VALUES
		(1000, null, 'Example')

	ON CONFLICT (id) DO NOTHING;

	INSERT INTO oms."ExecutionBeanKeyDefDO"(
            id, "executionBeanDefRef", "parameterKey", "parameterTypeDefRef",
            mandatory, "defaultValue", "activeOMT") VALUES
	    (11200, 1003, 'shopEmailAddress', 11, true, null, true),
	    (11201, 1003, 'shopEmailSenderName', 11, false, null, true)

	ON CONFLICT (id) DO NOTHING;

	INSERT INTO oms."JobDefDO"(id, name, description) VALUES
		(1000, 'Example', 'Example')

	ON CONFLICT (id) DO NOTHING;

	INSERT INTO oms."MessageTypeDefDO"(id, name, description) VALUES
		(10500, 'Send customer mail - order', 'Send customer mail - order')

	ON CONFLICT (id) DO NOTHING;

	INSERT INTO oms."OrderSupplierEvaluationRuleDefDO"(id, description, mandatory, name, rank) VALUES
	    (1000, 'Example', false, 'Example', 30)

	ON CONFLICT (id) DO NOTHING;

	INSERT INTO oms."OrderValidationRuleDefDO"(id, name, rank, mandatory, description) VALUES
		(10000, 'ValidateMandatoryPropertiesPTBean', 999, false, 'ValidateMandatoryPropertiesPTBean')

	ON CONFLICT (id) DO NOTHING;

	INSERT INTO oms."TransformerBeanDefDO"(id, name) VALUES
		(1000, 'Example')
	ON CONFLICT (id) DO NOTHING;

	INSERT INTO oms."TransmissionTypeDefDO"(id, name, "roleDefRef", description, "messageTypeName") VALUES
		(10500, 'Example', 6, 'Example', 'EXAMPLE')
	ON CONFLICT (id) DO NOTHING;

	INSERT INTO oms."EventDefDO"(id, description) VALUES
		(1001, 'EXAMPLE_EVENT_MANAGER_BEAN')
	ON CONFLICT (id) DO NOTHING;
*/
END;
$$;
