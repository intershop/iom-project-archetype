package com.intershop.oms.enums.expand;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.configuration.common.OrderValidationRuleDefDO;
import bakery.persistence.expand.OrderValidationRuleDefDOEnumInterface;

@ExpandedEnum(OrderValidationRuleDefDO.class)
public enum ExpandedOrderValidationRuleDefDO implements OrderValidationRuleDefDOEnumInterface
{

    /**
     * Minimum ID for custom entries: 1000 length restriction for name: 50
     */
    VALIDATE_PROPERTIES(-9999, "ValidateMandatoryPropertiesPTBean", "java:global/example-app/ValidateMandatoryPropertiesPTBean!bakery.logic.service.order.task.ValidateOrderPT", 999, false)
    ;
    
    private Integer id;
    private String name;
    private String jndiName;
    private int rank;
    private boolean mandatory;

    private ExpandedOrderValidationRuleDefDO(Integer id, String name, String jndiName, int rank, boolean mandatory)
    {
        this.id = id;
        this.name = name;
        this.jndiName = jndiName;
        this.rank = rank;
        this.mandatory = mandatory;
    }

    /**
     * Id der Pruefungsart
     */
    @Override
    public Integer getId()
    {
        return this.id;
    }

    /**
     * Namen der Pruefungsregel
     */
    @Override
    public String getName()
    {
        return this.name;
    }

    /**
     * Ranking (Reihenfolge) der Pruefung
     */
    @Override
    public int getRank()
    {
        return this.rank;
    }

    /**
     * Gibt an ob dieser Parameter abgeschaltet werden darf.
     *
     * @return <b>true</b> oder <b>false</b>
     */
    @Override
    public boolean isMandatory()
    {
        return this.mandatory;
    }

    @Override
    public String getJndiName()
    {
        return this.jndiName;
    }

}
