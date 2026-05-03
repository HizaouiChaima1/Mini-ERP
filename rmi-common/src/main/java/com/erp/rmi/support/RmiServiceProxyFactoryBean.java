package com.erp.rmi.support;

import java.rmi.Naming;
import java.rmi.Remote;

import org.springframework.beans.factory.FactoryBean;

/**
 * Remplace {@code RmiProxyFactoryBean} (supprimé dans Spring 6). Le lookup est fait au premier accès si le bean est
 * {@code @Lazy}.
 */
public class RmiServiceProxyFactoryBean<T extends Remote> implements FactoryBean<T> {

    private final Class<T> serviceInterface;
    private final String serviceUrl;
    private volatile T cached;

    public RmiServiceProxyFactoryBean(Class<T> serviceInterface, String serviceUrl) {
        this.serviceInterface = serviceInterface;
        this.serviceUrl = serviceUrl;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() throws Exception {
        if (cached == null) {
            synchronized (this) {
                if (cached == null) {
                    cached = (T) Naming.lookup(serviceUrl);
                }
            }
        }
        return cached;
    }

    @Override
    public Class<?> getObjectType() {
        return serviceInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
