package com.intershop.oms.enums.expand;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.configuration.connections.ExecutionBeanKeyDefDO;
import bakery.persistence.dataobject.configuration.connections.ParameterTypeDefDO;
import bakery.persistence.expand.ExecutionBeanKeyDefDOEnumInterface;

@ExpandedEnum(ExecutionBeanKeyDefDO.class)
public enum ExpandedExecutionBeanKeyDefDO implements ExecutionBeanKeyDefDOEnumInterface
{

    /**
     * Minimum ID for custom entries: 10000
     */
    /*
    EXAMPLE_SHOPCUSTOMERMAILSENDERBEAN_SHOP_EMAIL_ADDRESS( -123, ExpandedExecutionBeanDefDO.EXAMPLE.getId(), "shopEmailAddress", ParameterTypeDefDO.EMAIL, Flag.MANDATORY, null )   
    */
    ;

    private Integer id;
    private Integer executionBeanDefRef;
    private String parameterKey;
    private ParameterTypeDefDO parameterTypeDefDO;
    private Boolean mandatory;
    private String defaultValue;

    private ExpandedExecutionBeanKeyDefDO(int id, Integer executionBeanDefRef, String parameterKey,
                    ParameterTypeDefDO parameterTypeDefDO, boolean mandatory, String defaultValue)
    {
        this.id = Integer.valueOf(id);
        this.executionBeanDefRef = executionBeanDefRef;
        this.parameterKey = parameterKey;
        this.parameterTypeDefDO = parameterTypeDefDO;
        this.mandatory = Boolean.valueOf(mandatory);
        this.defaultValue = defaultValue;
    }

    @Override
    public Integer getId()
    {
        return this.id;
    }

    @Override
    public Integer getExecutionBeanDefRef()
    {
        return this.executionBeanDefRef;
    }

    @Override
    public String getParameterKey()
    {
        return this.parameterKey;
    }

    @Override
    public Integer getParameterTypeDefRef()
    {
        return this.parameterTypeDefDO.getId();
    }

    @Override
    public ParameterTypeDefDO getParameterTypeDefDO()
    {
        return this.parameterTypeDefDO;
    }

    @Override
    public Boolean getMandatory()
    {
        return this.mandatory;
    }

    /**
     * <p>
     * Default parameterValue zum parameterKey.
     * </p>
     * <p>
     * Maximale Anzahl erlaubter Zeichen:
     * {@value ExecutionBeanKeyDefDO#VALUE_LENGTH}.
     * </p>
     */
    @Override
    public String getDefaultValue()
    {
        return this.defaultValue;
    }

}
