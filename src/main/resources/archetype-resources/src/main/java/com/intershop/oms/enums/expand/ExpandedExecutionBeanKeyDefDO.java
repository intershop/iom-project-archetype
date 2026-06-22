package com.intershop.oms.enums.expand;

import java.util.EnumSet;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.configuration.connections.ExecutionBeanKeyDefDO;
import bakery.persistence.dataobject.configuration.connections.ParameterTypeDefDO;
import bakery.persistence.dataobject.configuration.connections.ExecutionBeanKeyDefDO.Flag;
import bakery.persistence.expand.ExecutionBeanKeyDefDOEnumInterface;

@ExpandedEnum( ExecutionBeanKeyDefDO.class )
public enum ExpandedExecutionBeanKeyDefDO implements ExecutionBeanKeyDefDOEnumInterface
{
    // start with 10000 to avoid conflicts with ExecutionBeanKeyDefDO
    // the name must be unique across both classes
    // values with negative id are meant as syntax example and are ignored (won't get persisted within the db)
    CUSTOM_WISH(100001, ExpandedExecutionBeanDefDO.CUSTOM_ORDER_MESSAGE_TRANSMITTER.getId(), "customWish", ParameterTypeDefDO.UNSPECIFIED, false, null ),
    MIME_TYPE(100002, ExpandedExecutionBeanDefDO.CUSTOM_MAIL_TRANSMITTER.getId(), "mimeType", ParameterTypeDefDO.UNSPECIFIED, true, "text/plain" ),
    SHOPCUSTOMERMAILSENDERBEAN_SHOP_EMAIL_ADDRESS( 100003, ExpandedExecutionBeanDefDO.CUSTOM_MAIL_TRANSMITTER.getId(), "shopEmailAddress", ParameterTypeDefDO.EMAIL, Flag.MANDATORY, null ),
    SHOPCUSTOMERMAILSENDERBEAN_SHOP_EMAIL_SENDERNAME( 100004, ExpandedExecutionBeanDefDO.CUSTOM_MAIL_TRANSMITTER.getId(), "shopEmailSenderName", ParameterTypeDefDO.STRING, Flag.OPTIONAL, null ),

    ;

    private Integer id;
    private Integer executionBeanDefRef;
    private String parameterKey;
    private ParameterTypeDefDO parameterTypeDefDO;
    private Boolean mandatory;
    private String defaultValue;

    private ExpandedExecutionBeanKeyDefDO( int id, Integer executionBeanDefRef, String parameterKey, ParameterTypeDefDO parameterTypeDefDO, boolean mandatory, String defaultValue )
    {
        this.id = Integer.valueOf( id );
        this.executionBeanDefRef = executionBeanDefRef;
        this.parameterKey = parameterKey;
        this.setParameterTypeDefDO( parameterTypeDefDO );
        this.mandatory = Boolean.valueOf( mandatory );
        this.defaultValue = defaultValue;
    }

    @Override
    @Id
    public Integer getId()
    {
        return id;
    }

    protected void setId( Integer id )
    {
        this.id = id;
    }

    @Override
    @Column( name = "`executionBeanDefRef`" )
    public Integer getExecutionBeanDefRef()
    {
        return executionBeanDefRef;
    }

    protected void setExecutionBeanDefRef( Integer executionBeanDefRef )
    {
        this.executionBeanDefRef = executionBeanDefRef;
    }

    @Override
    @Column( name = "`parameterKey`", length = ExecutionBeanKeyDefDO.KEY_LENGTH )
    public String getParameterKey()
    {
        return parameterKey;
    }

    protected void setParameterKey( String parameterKey )
    {
        this.parameterKey = parameterKey;
    }

    @Override
    @Column( name = "`parameterTypeDefRef`" )
    public Integer getParameterTypeDefRef()
    {
        return parameterTypeDefDO.getId();
    }

    protected void setParameterTypeDefRef( Integer paramterTypeDefRef )
    {
        parameterTypeDefDO = ParameterTypeDefDO.valueOf( paramterTypeDefRef );
    }

    @Override
    @Transient
    public ParameterTypeDefDO getParameterTypeDefDO()
    {
        return parameterTypeDefDO;
    }

    protected void setParameterTypeDefDO( ParameterTypeDefDO parameterTypeDefDO )
    {
        this.parameterTypeDefDO = parameterTypeDefDO;
    }

    @Override
    @Column( name = "`mandatory`" )
    public Boolean getMandatory()
    {
        return mandatory;
    }

    protected void setMandatory( Boolean mandatory )
    {
        this.mandatory = mandatory;
    }

    /**
     * <p>
     * Default parameterValue zum parameterKey.
     * </p>
     * <p>
     * Maximale Anzahl erlaubter Zeichen: {@value #VALUE_LENGTH}.
     * </p>
     */
    @Override
    @Column( name = "`defaultValue`", length = ExecutionBeanKeyDefDO.VALUE_LENGTH )
    public String getDefaultValue()
    {
        return defaultValue;
    }

    /**
     * <p>
     * Default parameterValue zum parameterKey.
     * </p>
     * <p>
     * Maximale Anzahl erlaubter Zeichen: {@value #VALUE_LENGTH}.
     * </p>
     */
    protected void setDefaultValue( String defaultValue )
    {
        this.defaultValue = defaultValue;
    }

    /**
     * get list of expanded enums
     * @return
     */
    @Transient
    public final EnumSet<ExpandedExecutionBeanKeyDefDO> getExpandedEnums()
    {
        return EnumSet.allOf( ExpandedExecutionBeanKeyDefDO.class );
    }

    /**
     * get list of all enums
     * @return
     */
    @Transient
    public final EnumSet<ExpandedExecutionBeanKeyDefDO> getAllEnums()
    {
        return EnumSet.allOf( ExpandedExecutionBeanKeyDefDO.class );
    }
}
