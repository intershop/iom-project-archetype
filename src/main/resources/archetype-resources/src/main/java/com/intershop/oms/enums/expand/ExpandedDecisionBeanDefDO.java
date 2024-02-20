package com.intershop.oms.enums.expand;

import java.util.EnumSet;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.configuration.connections.DecisionBeanDefDO;
import bakery.persistence.dataobject.transformer.EnumInterface;
import bakery.util.DeploymentConfig;
import bakery.util.StringUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

@ExpandedEnum(DecisionBeanDefDO.class)
public enum ExpandedDecisionBeanDefDO implements EnumInterface
{

    /**
     * Start with 10000 to avoid conflict with DecisionBeanDefDO.
     * The name must be unique across both classes.
     * Values with negative id are meant as syntax example and are ignored (won't get persisted within the database).
     */

    EXAMPLE1(-9999, "java:global/example-app/InvoicingDecisionBean"),
    EXAMPLE2(-9998, "java:global/example-app/TBD")
    ;

    private Integer id;
    private String jndiName;

    private ExpandedDecisionBeanDefDO( Integer id, String jndiName )
    {
        this.id = id;
        this.jndiName = String.format( jndiName, DeploymentConfig.APP_VERSION );
    }

    @Override
    @Id
    public Integer getId()
    {
        return id;
    }

    @Override
    @Column( name = "`description`" )
    public String getName()
    {
        return StringUtils.constantToHungarianNotation( name(), StringUtils.FLAG_FIRST_LOWER );
    }

    @Override
    @Transient
    public String getJndiName()
    {
        return jndiName;
    }

    /**
     * get list of expanded enums
     * @return
     */
    @Transient
    public final EnumSet<ExpandedDecisionBeanDefDO> getExpandedEnums()
    {
        return EnumSet.allOf( ExpandedDecisionBeanDefDO.class );
    }

    /**
     * get list of all enums
     * @return
     */
    @Transient
    public final EnumSet<DecisionBeanDefDO> getAllEnums()
    {
        return EnumSet.allOf( DecisionBeanDefDO.class );
    }

}
