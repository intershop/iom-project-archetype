package com.intershop.oms.enums.expand;

import java.util.EnumSet;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.transformer.EnumInterface;
import bakery.persistence.dataobject.transformer.TransformerBeanDefDO;
import bakery.util.DeploymentConfig;
import bakery.util.StringUtils;

@ExpandedEnum( TransformerBeanDefDO.class )
public enum ExpandedTransformerBeanDefDO implements EnumInterface
{
    // start with 1000 to avoid conflicts with TransformerBeanDefDO
    // the name must be unique across both classes
    // values with negative id are meant as syntax example and are ignored (won't get persisted within the db)
    EXAMPLE( Integer.valueOf( -999 ), "java:global/ci-project-app/ExampleTransformer!bakery.logic.job.transformation.Transformer" );

    private Integer id;
    private String jndiName;

    private ExpandedTransformerBeanDefDO( Integer id, String jndiName )
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
    @Column( name = "name" )
    public String getName()
    {
        return StringUtils.constantToHungarianNotation( name(), false );
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
    public final EnumSet<ExpandedTransformerBeanDefDO> getExpandedEnums()
    {
        return EnumSet.allOf( ExpandedTransformerBeanDefDO.class );
    }

    /**
     * get list of all enums
     * @return
     */
    @Transient
    public final EnumSet<TransformerBeanDefDO> getAllEnums()
    {
        return EnumSet.allOf( TransformerBeanDefDO.class );
    }
}
