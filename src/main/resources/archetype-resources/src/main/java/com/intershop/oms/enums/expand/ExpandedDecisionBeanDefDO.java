package com.intershop.oms.enums.expand;

import java.util.EnumSet;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import bakery.persistence.annotation.ExpandedEnum;
import bakery.persistence.dataobject.configuration.connections.DecisionBeanDefDO;
import bakery.persistence.dataobject.transformer.EnumInterface;
import bakery.util.DeploymentConfig;
import bakery.util.StringUtils;

@ExpandedEnum( DecisionBeanDefDO.class )
public enum ExpandedDecisionBeanDefDO implements EnumInterface
{
    // start with 10000 to avoid conflict with DecisionBeanDefDO
    // the name must be unique across both classes
    // values with negative id are meant as syntax example and are ignored (won't get persisted within the db)
    EXAMPLE( Integer.valueOf( -999 ), "java:global/ci-project-app/ExampleDecisionBean!bakery.logic.job.transformation.Transformer" ),
    INVOICE_DECISION_BEAN( 10000, "java:global/ci-project-app/InvoicingDecisionBean" ),
    CUSTOM_PAYMENT_DECISION_BEAN( 10003, "java:global/ci-project-app/CustomPaymentDecisionBean" ),
    APPROVAL_DECISION_BEAN( 10004, "java:global/ci-project-app/ApprovalDecisionBean" ),
    MAIL_APPROVAL_DECISION_BEAN( 10005, "java:global/ci-project-app/MailApprovalDecisionBean" ),
    ORDER_LIMIT_DECISION_BEAN( 10006, "java:global/ci-project-app/OrderLimitDecisionBean" );

    private Integer id;
    private String jndiName;

    private ExpandedDecisionBeanDefDO( Integer id, String jndiName )
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
        return StringUtils.constantToHungarianNotation( name(), StringUtils.FLAG_FIRST_LOWER );
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
    public final EnumSet<ExpandedDecisionBeanDefDO> getExpandedEnums()
    {
        return EnumSet.allOf( ExpandedDecisionBeanDefDO.class );
    }

    /**
     * get list of all enums
     * @return
     */
    @Transient
    public final EnumSet<DecisionBeanDefDO> getAllEnums()
    {
        return EnumSet.allOf( DecisionBeanDefDO.class );
    }
}