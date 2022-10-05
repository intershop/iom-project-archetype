package com.intershop.oms.enums.expand;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.Configuration;
import bakery.persistence.dataobject.configuration.common.DocumentMapperDefDO;
import bakery.persistence.expand.DocumentMapperDefDOEnumInterface;
import bakery.util.StringUtils;
import bakery.util.ejb.EJBHelper;

@ExpandedEnum(DocumentMapperDefDO.class)
public enum ExpandedDocumentMapperDefDO implements DocumentMapperDefDOEnumInterface
{
    // start with 1000 to avoid conflicts with DocumentMapperDefDO
    // the name must be unique across both classes
    // values with negative id are meant as syntax example and are ignored
    // (won't get persisted within the DB)
    EXAMPLE(-999, "java:global/example-app/ExampleReturnSlipMapperBean", "example document mapper discription")
    ;


    private Integer id;
    private String jndiName;
    private String description;

    private ExpandedDocumentMapperDefDO(Integer id, String jndiName, String description)
    {
        this.id = id;
        this.jndiName = jndiName;
        this.description = description;
    }

    /**
     * @return Id of the document mapper
     */
    @Override
    public Integer getId()
    {
        return this.id;
    }

    @Override
    public String getDescription()
    {
        return this.description;
    }

    /**
     * @return the name of the document mapper
     */
    @Override
    public String getName()
    {
        return StringUtils.constantToHungarianNotation(this.name(), true);
    }

    @Override
    public <T> T getDocumentMapper(Class<T> type)
    {
        return new EJBHelper().getExpectedBean(String.format(this.jndiName, bakery.util.DeploymentConfig.APP_VERSION),
                        type);
    }

}
