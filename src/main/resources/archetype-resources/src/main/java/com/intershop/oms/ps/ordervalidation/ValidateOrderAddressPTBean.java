package com.intershop.oms.ps.ordervalidation;

import static org.apache.commons.lang3.StringUtils.isBlank;

import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bakery.logic.service.order.task.ValidateOrderPT;
import bakery.logic.valueobject.ProcessContainer;
import bakery.persistence.dataobject.order.OrderAddressDO;
import bakery.persistence.dataobject.order.OrderDO;
import bakery.util.exception.DatabaseException;
import bakery.util.exception.ModifiedObjectException;
import bakery.util.exception.NoObjectException;
import bakery.util.exception.ValidationException;

@Stateless
public class ValidateOrderAddressPTBean implements ValidateOrderPT
{

    private static final Logger log = LoggerFactory.getLogger(ValidateOrderAddressPTBean.class);

    @Override
    public ProcessContainer execute(ProcessContainer container)
                    throws ValidationException, NoObjectException, DatabaseException, ModifiedObjectException
    {
        OrderDO orderDO = container.getOrderDO();

        /*
         * workaround for annoying house number issue
         */

        for (OrderAddressDO address : orderDO.getOrderAddressList())
        {
            if (isBlank(address.getHouseNumber())
                            || address.getHouseNumber().length() > OrderAddressDO.HOUSE_NUMBER_LENGTH)
            {
                address.setStreetName(null);
                address.setHouseNumber(null);
                log.debug("house number issue fixed for order#{}", orderDO.getShopOrderNo());
            }
        }

        return container;
    }

}
