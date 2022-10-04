package com.intershop.oms.enums.expand;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.configuration.common.IdentCodeGenerationBeanDefDO;
import bakery.persistence.dataobject.transformer.EnumInterface;
import bakery.util.StringUtils;

@ExpandedEnum(IdentCodeGenerationBeanDefDO.class)
public enum ExpandedIdentCodeGenerationBeanDefDO implements EnumInterface
{

    /**
     * Minimum ID for custom entries: 1000
     */
;
    private Integer id;
    private String jndiName;

    private ExpandedIdentCodeGenerationBeanDefDO(Integer id, String jndiName)
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
