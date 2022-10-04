package com.intershop.oms.enums.expand;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.configuration.common.ApprovalTypeDefDO;
import bakery.persistence.dataobject.transformer.EnumInterface;
import bakery.util.StringUtils;

@ExpandedEnum(ApprovalTypeDefDO.class)
public enum ExpandedApprovalTypeDefDO implements EnumInterface
{

    /**
     * Minimum ID for custom entries: 10000
     */
    EXAMPLE(-10010, "")
    ;

    private Integer id;
    private String jndiName;

    private ExpandedApprovalTypeDefDO(Integer id, String jndiName)
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
