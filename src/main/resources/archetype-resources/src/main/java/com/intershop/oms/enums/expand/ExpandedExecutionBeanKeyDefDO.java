package com.intershop.oms.enums.expand;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;

import bakery.persistence.annotation.PersistedEnumerationTable;
import bakery.persistence.dataobject.configuration.connections.ExecutionBeanKeyDefDO;
import bakery.persistence.dataobject.configuration.connections.ParameterTypeDefDO;
import bakery.persistence.expand.ExecutionBeanKeyDefDOEnumInterface;

@PersistedEnumerationTable(ExecutionBeanKeyDefDO.class)
public enum ExpandedExecutionBeanKeyDefDO implements ExecutionBeanKeyDefDOEnumInterface
{

    /**
     * Minimum ID for custom entries: 10000
     */
    /*
    EXAMPLE_SHOPCUSTOMERMAILSENDERBEAN_SHOP_EMAIL_ADDRESS( -123, ExpandedExecutionBeanDefDO.EXAMPLE.getId(), "shopEmailAddress", ParameterTypeDefDO.EMAIL, Flag.MANDATORY, null, Flag.ACTIVE_OMT )   
    */
    ;

    private Integer id;
    private Integer executionBeanDefRef;
    private String parameterKey;
    private ParameterTypeDefDO parameterTypeDefDO;
    private Boolean mandatory;
    private String defaultValue;
    private Boolean activeOMT;

    private ExpandedExecutionBeanKeyDefDO(int id, Integer executionBeanDefRef, String parameterKey,
                    ParameterTypeDefDO parameterTypeDefDO, boolean mandatory, String defaultValue, boolean activeOMT)
    {
        this.id = Integer.valueOf(id);
        this.executionBeanDefRef = executionBeanDefRef;
        this.parameterKey = parameterKey;
        this.setParameterTypeDefDO(parameterTypeDefDO);
        this.mandatory = Boolean.valueOf(mandatory);
        this.defaultValue = defaultValue;
        this.activeOMT = Boolean.valueOf(activeOMT);
    }

    @Override
    @Id
    public Integer getId()
    {
        return this.id;
    }

    protected void setId(Integer id)
    {
        this.id = id;
    }

    @Override
    @Column(name = "`executionBeanDefRef`")
    public Integer getExecutionBeanDefRef()
    {
        return this.executionBeanDefRef;
    }

    protected void setExecutionBeanDefRef(Integer executionBeanDefRef)
    {
        this.executionBeanDefRef = executionBeanDefRef;
    }

    @Override
    @Column(name = "`parameterKey`", length = ExecutionBeanKeyDefDO.KEY_LENGTH)
    public String getParameterKey()
    {
        return this.parameterKey;
    }

    protected void setParameterKey(String parameterKey)
    {
        this.parameterKey = parameterKey;
    }

    @Override
    @Column(name = "`parameterTypeDefRef`")
    public Integer getParameterTypeDefRef()
    {
        return this.parameterTypeDefDO.getId();
    }

    protected void setParameterTypeDefRef(Integer paramterTypeDefRef)
    {
        this.parameterTypeDefDO = ParameterTypeDefDO.valueOf(paramterTypeDefRef);
    }

    @Override
    @Transient
    public ParameterTypeDefDO getParameterTypeDefDO()
    {
        return this.parameterTypeDefDO;
    }

    protected void setParameterTypeDefDO(ParameterTypeDefDO parameterTypeDefDO)
    {
        this.parameterTypeDefDO = parameterTypeDefDO;
    }

    @Override
    @Column(name = "`mandatory`")
    public Boolean getMandatory()
    {
        return this.mandatory;
    }

    protected void setMandatory(Boolean mandatory)
    {
        this.mandatory = mandatory;
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
    @Column(name = "`defaultValue`", length = ExecutionBeanKeyDefDO.VALUE_LENGTH)
    public String getDefaultValue()
    {
        return this.defaultValue;
    }

    /**
     * <p>
     * Default parameterValue zum parameterKey.
     * </p>
     * <p>
     * Maximale Anzahl erlaubter Zeichen:
     * {@value ExecutionBeanKeyDefDO#VALUE_LENGTH}
     * </p>
     */
    protected void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    /**
     * activ/inactiv
     */
    @Override
    @Column(name = "`activeOMT`")
    public Boolean isActiveOMT()
    {
        return this.activeOMT;
    }

    /**
     * activ/inactiv
     */
    public void setActiveOMT(Boolean activeOMT)
    {
        this.activeOMT = activeOMT;
    }

}
