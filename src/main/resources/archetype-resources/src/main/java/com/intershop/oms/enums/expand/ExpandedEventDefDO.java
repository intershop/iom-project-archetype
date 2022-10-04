package com.intershop.oms.enums.expand;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.configuration.event.EventDefDO;
import bakery.persistence.expand.EventDefDOEnumInterface;

@ExpandedEnum(EventDefDO.class)
public enum ExpandedEventDefDO implements EventDefDOEnumInterface
{
    // start with 1000 to avoid conflicts with EventDefDO
    // the name must be unique across both classes
    // values with negative id are meant as syntax example and are ignored (won't get persisted within the DB)
    CUSTOM_MAIL_EVENT_MANAGER( -999, "CUSTOM_MAIL_EVENT_MANAGER_BEAN", "java:global/example-app/CustomMailEventManagerBean" )
    ;
    
    private Integer id;
    private String description;
    private String jndiName;

    private ExpandedEventDefDO(Integer id, String description, String jndiName)
    {
        this.id = id;
        this.description = description;
        this.jndiName = jndiName;
    }

    /**
     * Returns the ID of the event.
     * 
     * @return the ID of the event.
     */
    @Override
    public Integer getId()
    {
        return id;
    }

    /**
     * Returns the description of the event.
     * 
     * @return the description of the event.
     */
    @Override
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Returns the JNDI-name of the event.
     * 
     * @return the JNDI-name of the event.
     */
    @Override
    public String getName()
    {
        return this.getJndiName();
    }

    public String getJndiName()
    {
        return String.format(this.jndiName, bakery.util.DeploymentConfig.APP_VERSION);
    }
}
