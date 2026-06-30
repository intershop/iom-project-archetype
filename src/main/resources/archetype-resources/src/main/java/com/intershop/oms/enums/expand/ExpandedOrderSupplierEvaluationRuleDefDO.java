package com.intershop.oms.enums.expand;

import java.util.EnumSet;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.configuration.common.OrderSupplierEvaluationRuleDefDO;
import bakery.persistence.expand.OrderSupplierEvaluationRuleDefDOEnumInterface;

@ExpandedEnum( OrderSupplierEvaluationRuleDefDO.class )
public enum ExpandedOrderSupplierEvaluationRuleDefDO implements OrderSupplierEvaluationRuleDefDOEnumInterface
{
    // start with 1000 to avoid conflicts with OrderSupplierEvaluationRuleDefDO
    // the name must be unique across both classes
    // values with negative id are meant as syntax example and are ignored (won't get persisted within the db)
    EXAMPLE( Integer.valueOf( -9999 ), "SupplierHasStockCheckPTBean", "Filters for suppliers that have stock to deliver.", "java:global/example-project/SupplierHasStockCheckPTBean!bakery.logic.service.order.task.OrderSupplierCheckPT", 1000, Boolean.FALSE.booleanValue() )
    ;

    private Integer id;
    private String name;
    private String description;
    private String jndiName;
    private int rank;
    private boolean mandatory;

    private ExpandedOrderSupplierEvaluationRuleDefDO( Integer id, String name, String description, String jndiName, int rank, boolean mandatory )
    {
        this.id = id;
        this.name = name;
        this.jndiName = jndiName;
        this.description = description;
        this.rank = rank;
        this.mandatory = mandatory;
    }

    /**
     * Id der Pruefungsart
     */
    @Override
    @Id
    public Integer getId()
    {
        return id;
    }

    /**
     * Id der Pruefungsart
     */
    protected void setId( Integer id )
    {
        this.id = id;
    }

    /**
     * Namen der Pruefungsregel
     */
    @Override
    @Column( name = "name", length = 50, nullable = false )
    public String getName()
    {
        return name;
    }

    /**
     * Namen der Pruefungsregel
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * Ranking (Reihenfolge) der Pruefung
     */
    @Override
    @Column( name = "rank", nullable = false )
    public int getRank()
    {
        return rank;
    }

    /**
     * Ranking (Reihenfolge) der Pruefung
     */
    public void setRank( int rank )
    {
        this.rank = rank;
    }

    /**
     * Gibt an ob dieser Parameter abgeschaltet werden darf.
     * @return <b>true</b> oder <b>false</b>
     */
    @Override
    @Column( name = "mandatory", nullable = false )
    public boolean isMandatory()
    {
        return mandatory;
    }

    /**
     * Gibt an ob dieser Parameter abgeschaltet werden darf.
     * @param mandatory <b>true</b> oder <b>false</b>
     */
    public void setMandatory( boolean mandatory )
    {
        this.mandatory = mandatory;
    }

    @Override
    @Transient
    public String getJndiName()
    {
        return String.format( this.jndiName, bakery.util.DeploymentConfig.APP_VERSION );
    }

    /**
     * get list of expanded enums
     * @return
     */
    @Transient
    public final EnumSet<ExpandedOrderSupplierEvaluationRuleDefDO> getExpandedEnums()
    {
        return EnumSet.allOf( ExpandedOrderSupplierEvaluationRuleDefDO.class );
    }

    /**
     * get list of all enums
     * @return
     */
    @Transient
    public final EnumSet<OrderSupplierEvaluationRuleDefDO> getAllEnums()
    {
        return EnumSet.allOf( OrderSupplierEvaluationRuleDefDO.class );
    }

    /**
     * Beschreibung der Pruefungsregel
     */
    @Override
    @Column(name="description", length = 100, nullable = false)
    public String getDescription()
    {
        return description;
    }

    /**
     * Beschreibung der Pruefungsregel
     */
    public void setDescription(String description)
    {
        this.description = description;
    }
}
