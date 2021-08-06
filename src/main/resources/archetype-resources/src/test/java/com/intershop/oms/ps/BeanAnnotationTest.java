package com.intershop.oms.ps;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

public class BeanAnnotationTest
{
    @SuppressWarnings("static-method")
    @Test
    public void test() throws Exception
    {
        Reflections reflectionsSN = new Reflections("com.intershop.oms.musgrave", new SubTypesScanner(false));
        Reflections reflectionsFramework = new Reflections("com.intershop.oms.ps", new SubTypesScanner(false));

        List<Class<? extends Object>> classes = new ArrayList<>();
        classes.addAll(reflectionsSN.getSubTypesOf(Object.class));
        classes.addAll(reflectionsFramework.getSubTypesOf(Object.class));

        for (Class<? extends Object> clazz : classes)
        {
            if (Modifier.isAbstract(clazz.getModifiers()) || isJaxRsProvider(clazz))
            {
                continue;
            }
            for (Field field : clazz.getDeclaredFields())
            {
                if (field.isAnnotationPresent(EJB.class) || field.isAnnotationPresent(PersistenceContext.class))
                {
                    assertTrue(clazz.isAnnotationPresent(Stateless.class) || clazz.isAnnotationPresent(Stateful.class)
                                    || clazz.isAnnotationPresent(Singleton.class), clazz.getName());
                    break;
                }
            }
        }
    }

    private static boolean isJaxRsProvider(Class<? extends Object> clazz)
    {
        return clazz.getPackageName().startsWith("com.intershop.oms.ps.rest.filter");
    }
}
