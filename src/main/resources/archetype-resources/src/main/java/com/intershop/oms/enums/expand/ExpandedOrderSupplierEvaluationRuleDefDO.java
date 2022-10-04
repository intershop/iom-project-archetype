package com.intershop.oms.enums.expand;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.configuration.common.OrderSupplierEvaluationRuleDefDO;
import bakery.persistence.expand.OrderSupplierEvaluationRuleDefDOEnumInterface;

@ExpandedEnum(OrderSupplierEvaluationRuleDefDO.class)
public enum ExpandedOrderSupplierEvaluationRuleDefDO implements OrderSupplierEvaluationRuleDefDOEnumInterface
{

    /**
     * Minimum ID for custom entries: 1000
     */
;
    private Integer id;
    private String name;
    private String description;
    private String jndiName;
    private int rank;
    private boolean mandatory;

    private ExpandedOrderSupplierEvaluationRuleDefDO(Integer id, String name, String description, String jndiName,
                    int rank, boolean mandatory)
    {
        this.id = id;
        this.name = name;
        this.jndiName = jndiName;
        this.description = description;
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

    /**
     * Beschreibung der Pruefungsregel
     */
    @Override
    public String getDescription()
    {
        return this.description;
    }

}
