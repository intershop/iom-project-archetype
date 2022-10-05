package com.intershop.oms.enums.expand;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.configuration.process.ProcessTaskDefDO;
import bakery.persistence.dataobject.transformer.EnumInterface;
import bakery.util.DeploymentConfig;

@ExpandedEnum(ProcessTaskDefDO.class)
public enum ExpandedProcessTaskDefDO implements EnumInterface
{
    /**
     * PLEASE START AFTER EXAMPLE WITH ID 1000 and go up THATS IMPORTANT !!!
     */
    EXAMPLE_PT(-999, "CheckSendDebitorPT", "java:global/example-app/SkipSendDebitorPTBean")
    ;

    private Integer id;
    private String name;
    private String jndiName;

    private ExpandedProcessTaskDefDO(Integer id, String name, String jndiName)
    {

        this.id = id;
        this.name = name;
        this.jndiName = String.format(jndiName, DeploymentConfig.APP_VERSION);

    }

    @Override
    public Integer getId()
    {
        return this.id;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public String getJndiName()
    {
        return this.jndiName;
    }

}
