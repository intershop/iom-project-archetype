package com.intershop.oms.enums.expand;

import java.util.EnumSet;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.transformer.EnumInterface;
import bakery.persistence.dataobject.transformer.TransformerBeanDefDO;
import bakery.util.DeploymentConfig;
import bakery.util.StringUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

@ExpandedEnum(TransformerBeanDefDO.class)
public enum ExpandedTransformerBeanDefDO implements EnumInterface
{

    /**
     * Start with 10000 to avoid conflict with TransformerBeanDefDO.
     * The name must be unique across both classes.
     * Values with negative id are meant as syntax example and are ignored (won't
     * get persisted within the database).
     */
    
    ICM_TO_IOM_TRANSFORMER(-9999, "java:global/example-app/ICMToIOMTransformer!bakery.logic.job.transformation.Transformer");

    private Integer id;
    private String jndiName;

    private ExpandedTransformerBeanDefDO(Integer id, String jndiName)
    {
        this.id = id;
        this.jndiName = String.format(jndiName, DeploymentConfig.APP_VERSION);
    }

    @Override
    @Id
    public Integer getId()
    {
        return id;
    }

    @Override
    @Column(name = "name")
    public String getName()
    {
        return StringUtils.constantToHungarianNotation(name(), false);
    }

    @Override
    @Transient
    public String getJndiName()
    {
        return jndiName;
    }

    /**
     * get list of expanded enums
     * 
     * @return
     */
    @Transient
    public final EnumSet<ExpandedTransformerBeanDefDO> getExpandedEnums()
    {
        return EnumSet.allOf(ExpandedTransformerBeanDefDO.class);
    }

    /**
     * get list of all enums
     * 
     * @return
     */
    @Transient
    public final EnumSet<TransformerBeanDefDO> getAllEnums()
    {
        return EnumSet.allOf(TransformerBeanDefDO.class);
    }
}
