package com.intershop.oms.enums.expand;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.configuration.order.OrderExportConfigBeanDefDO;
import bakery.persistence.dataobject.transformer.EnumInterface;
import bakery.util.StringUtils;

@ExpandedEnum(OrderExportConfigBeanDefDO.class)
public enum ExpandedOrderExportConfigBeanDefDO implements EnumInterface
{

    /**
     * Minimum ID for custom entries: 1000
     */
    EXAMPLE(-999, "java:global/blueprint-app/blueprint-ejb/ExampleOrderExportConfigBean!bakery.logic.job.transformation.Transformer")
    ;


    private Integer id;
    private String jndiName;

    private ExpandedOrderExportConfigBeanDefDO(Integer id, String jndiName)
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
