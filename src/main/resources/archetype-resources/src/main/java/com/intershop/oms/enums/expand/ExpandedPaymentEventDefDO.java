package com.intershop.oms.enums.expand;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.configuration.payment.PaymentEventDefDO;
import bakery.persistence.expand.EventDefDOEnumInterface;

@ExpandedEnum(PaymentEventDefDO.class)
public enum ExpandedPaymentEventDefDO implements EventDefDOEnumInterface
{
    // start with 1000 to avoid conflicts with PaymentEventDefDO
    // the name must be unique across both classes
    // values with negative id are meant as syntax example and are ignored (won't get persisted within the db)
    EXAMPLE(Integer.valueOf(-999), "Example Payment Event", "example.payment.event.key");

    /*
     * Example:
     * DUMMY_EVENT(Integer.valueOf(1000), "Dummy Payment Event", "dummy.payment.event.key");
     */

    private Integer id;
    private String description;
    private String jndiName;

    private ExpandedPaymentEventDefDO(Integer id, String description, String jndiName)
    {
        this.id = id;
        this.description = description;
        this.jndiName = jndiName;
    }

    /**
     * Returns the ID of the payment event.
     *
     * @return the ID of the payment event.
     */
    @Override
    @Id
    public Integer getId()
    {
        return id;
    }

    /**
     * Sets the ID of the payment event.
     */
    protected void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * Returns the description of the payment event.
     *
     * @return the description of the payment event.
     */
    @Override
    @Column(name = "`description`")
    public String getDescription()
    {
        return description;
    }

    /**
     * Sets the description of the payment event.
     *
     * @return
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Returns the JNDI-name of the payment event.
     *
     * @return the JNDI-name of the payment event.
     */
    @Override
    @Transient
    public String getName()
    {
        return getJndiName();
    }

    @Transient
    public String getJndiName()
    {
        return String.format(jndiName, bakery.util.DeploymentConfig.APP_VERSION);
    }
}
