package com.intershop.oms.enums.expand;

import java.util.EnumSet;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.configuration.order.OrderExportConfigBeanDefDO;
import bakery.persistence.dataobject.transformer.EnumInterface;
import bakery.util.DeploymentConfig;
import bakery.util.StringUtils;


@ExpandedEnum( OrderExportConfigBeanDefDO.class )
public enum ExpandedOrderExportConfigBeanDefDO implements EnumInterface
{
    // start with 1000 to avoid conflicts with OrderExportConfigBeanDefDO
    // the description must be unique across both classes
    // values with negative id are meant as syntax example and are ignored (won't get persisted within the db)
    EXAMPLE( Integer.valueOf( -9999 ), "java:global/example-app/example-ejb/ExampleOrderExportConfigBean!bakery.logic.job.transformation.Transformer" )
    ;

    private Integer id;
    private String jndiName;

    private ExpandedOrderExportConfigBeanDefDO( Integer id, String jndiName )
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
    @Column( name = "description" )
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
    public final EnumSet<ExpandedOrderExportConfigBeanDefDO> getExpandedEnums()
    {
        return EnumSet.allOf( ExpandedOrderExportConfigBeanDefDO.class );
    }

    /**
     * get list of all enums
     * @return
     */
    @Transient
    public final EnumSet<OrderExportConfigBeanDefDO> getAllEnums()
    {
        return EnumSet.allOf( OrderExportConfigBeanDefDO.class );
    }
}
