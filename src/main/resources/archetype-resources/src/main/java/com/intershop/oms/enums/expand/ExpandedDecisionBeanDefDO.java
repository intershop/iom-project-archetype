package com.intershop.oms.enums.expand;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.configuration.connections.DecisionBeanDefDO;
import bakery.persistence.dataobject.transformer.EnumInterface;
import bakery.util.StringUtils;

@ExpandedEnum(DecisionBeanDefDO.class)
public enum ExpandedDecisionBeanDefDO implements EnumInterface
{
    /**
     * Minimum ID for custom entries: 1000 suggestion: for regular decision
     * bean: 1xxx for order approval beans: 2xxx
     */
    EXAMPLE(-999, "java:global/example-app/InvoicingDecisionBean")
    ;

    private Integer id;
    private String jndiName;

    private ExpandedDecisionBeanDefDO(Integer id, String jndiName)
    {

        this.id = id;
        this.jndiName = jndiName;

    }

    @Override
    public Integer getId()
    {
        return this.id;
    }

    @Override
    public String getName()
    {
        return StringUtils.constantToHungarianNotation(this.name(), StringUtils.FLAG_FIRST_LOWER);
    }

    @Override
    public String getJndiName()
    {
        return this.jndiName;
    }

}
