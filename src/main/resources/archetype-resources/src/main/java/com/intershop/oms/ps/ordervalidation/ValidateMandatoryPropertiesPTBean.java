package com.intershop.oms.ps.ordervalidation;

import jakarta.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bakery.logic.service.exception.IncorrectValueException;
import bakery.logic.service.order.task.ValidateOrderPT;
import bakery.logic.valueobject.ProcessContainer;
import bakery.persistence.dataobject.order.OrderAddressDO;
import bakery.persistence.dataobject.order.OrderDO;
import bakery.util.NamedId;
import bakery.util.exception.NoObjectException;
import bakery.util.exception.ValidationException;

@Stateless
public class ValidateMandatoryPropertiesPTBean implements ValidateOrderPT
{
    private static Logger logger = LoggerFactory.getLogger(ValidateMandatoryPropertiesPTBean.class);

    @Override
    public ProcessContainer execute(ProcessContainer container) throws ValidationException, NoObjectException
    {
        /*
         * get order from process container
         */
        OrderDO orderDO = container.getOrderDO();

        logger.info("ValidateMandatoryPropertiesPTBean - startet for order: {}", container.getObjectId());

        if (orderDO != null)
        {
            OrderAddressDO orderAddressDO = orderDO.getBillingAddress();
            if (orderAddressDO != null)
            {
                boolean valid = true;
                // validate order billing address here

                if (!valid)
                {
                    throw new ValidationException(orderAddressDO, new IncorrectValueException("id", orderAddressDO.getId().toString(), ""));
                }
            }
            else
            {
                throw new NoObjectException(OrderAddressDO.class, new NamedId("OrderBillingAddress"));
            }
        }
        else
        {
            throw new NoObjectException(OrderDO.class, new NamedId("OrderDO"));
        }

        logger.info("ValidateMandatoryPropertiesPTBean - finished for order: {}", container.getObjectId());

        return container;
    }
}
