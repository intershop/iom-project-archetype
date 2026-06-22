package com.intershop.oms.enums.expand;

import java.util.EnumSet;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.configuration.common.IdentCodeGenerationBeanDefDO;
import bakery.persistence.dataobject.transformer.EnumInterface;
import bakery.util.DeploymentConfig;
import bakery.util.StringUtils;


@ExpandedEnum( IdentCodeGenerationBeanDefDO.class )
public enum ExpandedIdentCodeGenerationBeanDefDO implements EnumInterface
{
    // start with 1000 to avoid conflicts with IdentCodeGenerationBeanDefDO
    // the name must be unique across both classes
    // values with negative id are meant as syntax example and are ignored (won't get persisted within the db)
    EXAMPLE( Integer.valueOf( -999 ), "java:global/ci-project-app/ExampleIdentCodeGenerationBean!bakery.logic.job.transformation.Transformer" );

    private Integer id;
    private String jndiName;

    private ExpandedIdentCodeGenerationBeanDefDO( Integer id, String jndiName )
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
    @Column( name = "\"name\"" )
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
    public final EnumSet<ExpandedIdentCodeGenerationBeanDefDO> getExpandedEnums()
    {
        return EnumSet.allOf( ExpandedIdentCodeGenerationBeanDefDO.class );
    }

    /**
     * get list of all enums
     * @return
     */
    @Transient
    public final EnumSet<IdentCodeGenerationBeanDefDO> getAllEnums()
    {
        return EnumSet.allOf( IdentCodeGenerationBeanDefDO.class );
    }
}
