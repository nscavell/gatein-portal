/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.gatein.cdi;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;

import org.gatein.api.cdi.context.PortletLifecycleScoped;
import org.jboss.weld.context.AbstractBoundContext;
import org.jboss.weld.context.beanstore.NamingScheme;
import org.jboss.weld.context.beanstore.SimpleNamingScheme;
import org.jboss.weld.context.beanstore.http.RequestBeanStore;
import org.jboss.weld.context.cache.RequestScopedBeanCache;
import org.jboss.weld.context.http.HttpRequestContext;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@Singleton
public class PortletLifecycleContext extends AbstractBoundContext<ServletRequest> implements HttpRequestContext {

    private static final String IDENTIFIER = PortletLifecycleContext.class.getName();

    private final BeanManager beanManager;
    private final NamingScheme namingScheme;

    public PortletLifecycleContext(BeanManager beanManager) {
        super(false);
        this.beanManager = beanManager;
        this.namingScheme = new SimpleNamingScheme(HttpRequestContext.class.getName());
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return PortletLifecycleScoped.class;
    }

    @Override
    public boolean associate(ServletRequest request) {
        System.out.println("Associating request...");
        if (request.getAttribute(IDENTIFIER) == null) {
            request.setAttribute(IDENTIFIER, IDENTIFIER);
            setBeanStore(new RequestBeanStore(request, namingScheme));
            getBeanStore().attach();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean dissociate(ServletRequest request) {
        System.out.println("Dissociating request...");
        if (request.getAttribute(IDENTIFIER) != null) {
            try {
                setBeanStore(null);
                request.removeAttribute(IDENTIFIER);
                return true;
            } finally {
                cleanup();
            }
        } else {
            return false;
        }
    }

    @Override
    public void activate() {
        super.activate();
        RequestScopedBeanCache.beginRequest();
    }

    @Override
    public void deactivate() {
        try {
            RequestScopedBeanCache.endRequest();
        } finally {
            super.deactivate();
        }
    }
}
