package com.intershop.oms.enums.expand;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.configuration.salesprice.SalesPriceCalculatorBeanDefDO;
import bakery.persistence.dataobject.transformer.EnumInterface;
import bakery.util.StringUtils;

@ExpandedEnum(SalesPriceCalculatorBeanDefDO.class)
public enum ExpandedSalesPriceCalculatorBeanDefDO implements EnumInterface
{
    /**
     * PLEASE START AFTER EXAMPLE WITH ID 1000 and go up THATS IMPORTANT !!!
     */
    EXAMPLE(-999, "java:global/blueprint-app/blueprint-ejb/ExampleCalculatorBean!bakery.logic.service.util.SalesPriceCalculator")
    ;

    private Integer id;
    private String jndiName;

    private ExpandedSalesPriceCalculatorBeanDefDO(Integer id, String jndiName)
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
