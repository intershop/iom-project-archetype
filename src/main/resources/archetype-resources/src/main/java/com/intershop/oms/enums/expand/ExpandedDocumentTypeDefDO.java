package com.intershop.oms.enums.expand;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.Configuration;
import bakery.persistence.dataobject.configuration.common.DocumentTypeDefDO;
import bakery.persistence.dataobject.configuration.common.RightDefDO;
import bakery.persistence.expand.DocumentTypeDefDOEnumInterface;
import bakery.persistence.expand.RightDefDOEnumInterface;

@Entity
@Table(name = "`DocumentTypeDefDO`")
@Configuration
@ExpandedEnum(DocumentTypeDefDO.class)
public enum ExpandedDocumentTypeDefDO implements DocumentTypeDefDOEnumInterface
{
    // start with 1000 to avoid conflicts with DocumentTypeDefDO
    // the name must be unique across both classes
    // values with negative id are meant as syntax example and are ignored (won't get persisted within the db)
    EXAMPLE(Integer.valueOf(-999), "Example Document Type Name", "example.document.type", "Example Document Type Description"),

    // for types of order documents a right needs to be assigned
    // (in order to be displayed at order documents tab)
    EXAMPLE_ORDER_DOCUMENT(Integer.valueOf(-998), "Example Order Document Type", "example.orderdocument.type",
                           "Example Order Document Type Description", ExpandedRightDefDO.EXAMPLE_1),

    EXAMPLE_PERSISTED_I(Integer.valueOf(1001), "pigeonType", "Carrier pigeon document.", "pigeon",  null),

    EXAMPLE_PERSISTED_II(Integer.valueOf(1002), "pigeonTypeII", "Carrier pigeon packet.", "pigeonII",  RightDefDO.APPROVE_RETURN);

    private Integer id;
    private String name;
    private String description;
    private String documentType;
    private RightDefDOEnumInterface rightDefDO=null;
    private Integer rightDefRef=null;


    private ExpandedDocumentTypeDefDO(Integer id, String name, String description, String documentType)
    {
        this(id, name, description, documentType, null);
    }

    private ExpandedDocumentTypeDefDO(Integer id, String name, String description, String documentType, RightDefDOEnumInterface rightDefDO)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.documentType = documentType;
        this.rightDefDO = rightDefDO;

        if (null != rightDefDO)
        {
            rightDefRef = rightDefDO.getId();
        }
    }

    /**
     * Returns ID of document type
     *
     * @return id
     */
    @Override
    @Id
    public Integer getId()
    {
        return id;
    }

    /**
     * Sets the ID of the document type
     *
     * @param id
     */
    protected void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * Returns the name of the document type
     *
     * @return name
     */
    @Override
    @Column(name = "name", length = 50, nullable = false)
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name of the document type
     *
     * @param name
     */
    @SuppressWarnings("unused")
    private void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns the description of the document type
     *
     * @return description
     */
    @Override
    @Column(name = "description", length = 100, nullable = false)
    public String getDescription()
    {
        return description;
    }

    /**
     * Sets the description of the document type
     *
     * @param description
     */
    @SuppressWarnings("unused")
    private void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Returns the API enum name for the document type
     *
     * @return documentType
     */
    @Override
    @Column(name = "`documentType`", nullable = true)
    public String getDocumentType()
    {
        return documentType;
    }

    protected void setDocumentType(String documentType)
    {
        this.documentType = documentType;
    }

    /**
     * Returns the RightDefDO assigned to the document type
     *
     * @return rightDefDO
     */
    @Override
    @Transient
    public RightDefDOEnumInterface getRightDefDO()
    {
        return rightDefDO;
    }

    /**
     * Referenz auf das Recht
     */

    public void setRightDefDO(RightDefDOEnumInterface rightDefDO)
    {
        this.rightDefDO = rightDefDO;

        if (null== rightDefRef)
        {
            rightDefRef = null;
        }
        else
        {
            rightDefRef = rightDefDO.getId();
        }
    }
    /**
     * Id des Rechts
     */
    @Override
    @Column(name = "`rightDefRef`", nullable = true)
    public Integer getRightDefRef()
    {
        return rightDefRef;
    }
    /**
     * Id des Rechts
     */
    protected void setRightDefRef(Integer rightDefRef)
    {
        this.rightDefRef = rightDefRef;

        if (null== rightDefRef)
        {
            rightDefDO = null;
        }
        else
        {
            rightDefDO = RightDefDO.getRightDefDO(rightDefRef);
        }
    }
}