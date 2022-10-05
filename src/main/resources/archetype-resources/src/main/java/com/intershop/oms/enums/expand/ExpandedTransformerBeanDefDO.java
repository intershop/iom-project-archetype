package com.intershop.oms.enums.expand;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.transformer.EnumInterface;
import bakery.persistence.dataobject.transformer.TransformerBeanDefDO;
import bakery.util.StringUtils;

@ExpandedEnum(TransformerBeanDefDO.class)
public enum ExpandedTransformerBeanDefDO implements EnumInterface
{

    /**
     * Minimum ID for custom entries: 1000
     */
    ICM_TO_IOM_TRANSFORMER(-999, "java:global/example-app/ICMToIOMTransformer!bakery.logic.job.transformation.Transformer");

    private Integer id;
    private String jndiName;

    private ExpandedTransformerBeanDefDO(Integer id, String jndiName)
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
        return StringUtils.constantToHungarianNotation(this.name(), false);
    }

    @Override
    public String getJndiName()
    {
        return this.jndiName;
    }

}
