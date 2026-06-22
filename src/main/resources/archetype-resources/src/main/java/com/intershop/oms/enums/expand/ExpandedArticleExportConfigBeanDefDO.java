package com.intershop.oms.enums.expand;

import java.util.EnumSet;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.article.export.ArticleExportConfigBeanDefDO;
import bakery.persistence.dataobject.transformer.EnumInterface;
import bakery.util.DeploymentConfig;
import bakery.util.StringUtils;

@ExpandedEnum( ArticleExportConfigBeanDefDO.class )
public enum ExpandedArticleExportConfigBeanDefDO implements EnumInterface
{
    // start with 1000 to avoid conflict with ArticleExportConfigBeanDefDO
    // the name (here 'EXAMPLE')must be unique across both classes
    // values with negative id are meant as syntax example and are ignored (won't get persisted within the db)
    EXAMPLE( Integer.valueOf( -999 ), "java:global/ci-project-app/ExampleArticleExportConfig!bakery.logic.job.transformation.Transformer" );

    private Integer id;
    private String jndiName;

    private ExpandedArticleExportConfigBeanDefDO( Integer id, String jndiName )
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
        return StringUtils.constantToHungarianNotation(name(), StringUtils.FLAG_FIRST_LOWER );
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
    public final EnumSet<ExpandedArticleExportConfigBeanDefDO> getExpandedEnums()
    {
        return EnumSet.allOf( ExpandedArticleExportConfigBeanDefDO.class );
    }

    /**
     * get list of all enums
     * @return
     */
    @Transient
    public final EnumSet<ArticleExportConfigBeanDefDO> getAllEnums()
    {
        return EnumSet.allOf( ArticleExportConfigBeanDefDO.class );
    }
}