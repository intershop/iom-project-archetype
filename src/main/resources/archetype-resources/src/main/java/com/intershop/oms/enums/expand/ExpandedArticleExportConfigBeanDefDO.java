package com.intershop.oms.enums.expand;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.article.export.ArticleExportConfigBeanDefDO;
import bakery.persistence.dataobject.transformer.EnumInterface;
import bakery.util.StringUtils;

@ExpandedEnum(ArticleExportConfigBeanDefDO.class)
public enum ExpandedArticleExportConfigBeanDefDO implements EnumInterface
{

    /**
     * Minimum ID for custom entries: 1000
     */
    EXAMPLE(-999, "java:global/example-app/example-ejb/ExampleExportCSVBean");

    private Integer id;
    private String jndiName;

    private ExpandedArticleExportConfigBeanDefDO(Integer id, String jndiName)
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
        return StringUtils.constantToHungarianNotation(this.name(), StringUtils.FLAG_FIRST_LOWER);
    }

    @Override
    public String getJndiName()
    {
        return this.jndiName;
    }

}
