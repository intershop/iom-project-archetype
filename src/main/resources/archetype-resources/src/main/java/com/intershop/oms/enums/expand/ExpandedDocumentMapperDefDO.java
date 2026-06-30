package com.intershop.oms.enums.expand;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.Configuration;
import bakery.persistence.dataobject.configuration.common.DocumentMapperDefDO;
import bakery.persistence.expand.DocumentMapperDefDOEnumInterface;
import bakery.util.StringUtils;
import bakery.util.ejb.EJBHelper;

@Entity
@Table(name = "`DocumentMapperDefDO`")
@Configuration
@ExpandedEnum(DocumentMapperDefDO.class)
public enum ExpandedDocumentMapperDefDO implements DocumentMapperDefDOEnumInterface
{
    // start with 1000 to avoid conflicts with DocumentMapperDefDO
    // the name must be unique across both classes
    // values with negative id are meant as syntax example and are ignored
    // (won't get persisted within the db)
    EXAMPLE(Integer.valueOf(-9999), "java:global/example-app/ExampleReturnSlipMapperBean", "example document mapper discription")
    ;

    /*
     * Example: DUMMY_DOCUMENT_MAPPER( Integer.valueOf(1000),
     * "Dummy document mapper jndi name", "dummy document mapper discription" );
     */

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
    @Id
    public Integer getId()
    {
        return id;
    }

    /**
     * @param the Id of the document mapper
     */
    protected void setId(Integer id)
    {
        this.id = id;
    }

    @Override
    @Transient
    public String getDescription()
    {
        return description;
    }

    /**
     * @return the name of the document mapper
     */
    @Override
    @Column(name = "\"name\"", length = 50, nullable = false)
    public String getName()
    {
        return StringUtils.constantToHungarianNotation(name(), StringUtils.FLAG_FIRST_LOWER);
    }

    @SuppressWarnings("unused")
    private void setName(String name)
    {
        // dummy setter for the needs of hibernate
    }

    @Override
    @Transient
    public <T> T getDocumentMapper(Class<T> type)
    {
        return new EJBHelper().getExpectedBean(String.format(jndiName, bakery.util.DeploymentConfig.APP_VERSION), type);
    }
}
