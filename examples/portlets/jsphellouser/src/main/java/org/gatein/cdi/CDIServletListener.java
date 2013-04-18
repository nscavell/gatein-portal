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
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.portlet.PortletRequest;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpSessionEvent;

import org.jboss.portal.portlet.samples.JSPHelloUserPortlet;
import org.jboss.weld.Container;
import org.jboss.weld.context.http.HttpRequestContext;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class CDIServletListener extends AbstractServletListener {

    private final ThreadLocal<PortletLifecycleContext> ctx = new ThreadLocal<PortletLifecycleContext>();

    @Inject
    private BeanManager beanManager;

    private boolean action;

    @Override
    public void requestDestroyed(ServletRequestEvent event) {
        ServletRequest request = getUnderlyingRequest(event.getServletRequest());
        PortletLifecycleContext context = ctx.get();
        if (context != null) {
            if (isRenderRequest(request)) {
                context.dissociate(request);
                ctx.remove();
            }
        }
    }

    @Override
    public void requestInitialized(ServletRequestEvent event) {
        ServletRequest request = getUnderlyingRequest(event.getServletRequest());
        PortletLifecycleContext context = ctx.get();
        if (isActionRequest(request)) {
            if (context == null) {
                context = getContext();
                context.associate(request);
                ctx.set(context);
            } else {
                ctx.remove();
            }
        } else if (isRenderRequest(request)) {
            if (context != null) {
                context.associate(request);
            }
        } else {
            ctx.remove();
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        if (ctx.get() != null) {
            ctx.remove();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if (ctx.get() != null) {
            ctx.remove();
        }
    }

    private PortletLifecycleContext getContext() {
        return requestContext();
        //return (PortletLifecycleContext) beanManager.getContext(PortletLifecycleScoped.class);
//        return getContextualInstance(beanManager, PortletLifecycleContext.class);
    }

    private boolean isRenderRequest(ServletRequest request) {
//        return PortletRequest.RENDER_PHASE.equals(getPortletLifecyclePhase(request));
//        boolean render = PortletRequest.RENDER_PHASE.equals(getPortletLifecyclePhase(request));
        //if (isActionRequest(request)) {
        //    count--;
        //} else {
        //    count = 0;
        //}
        if (action) {
            action = false;
            return true;
        }

        return true;
    }

    private boolean isActionRequest(ServletRequest request) {
        return action = PortletRequest.ACTION_PHASE.equals(getPortletLifecyclePhase(request));
    }

    private static final String JAVAX_PORTLET_LIFECYCLE_PHASE = "javax.portlet.lifecycle_phase";

    private static String getPortletLifecyclePhase(ServletRequest request) {
        //return (String) request.getAttribute(JAVAX_PORTLET_LIFECYCLE_PHASE);
        String type = request.getParameter("portal:type");
        if ("action".equals(type)) {
            return PortletRequest.ACTION_PHASE;
        } else if ("render".equals(type)) {
            return PortletRequest.RENDER_PHASE;
        }

        return null;
    }

    private static ServletRequest getUnderlyingRequest(ServletRequest request) {
//        while (request instanceof ServletRequestWrapper) {
//            request = ((ServletRequestWrapper) request).getRequest();
//        }

        return request;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getContextualInstance(final BeanManager manager, final Class<T> type, Annotation... qualifiers) {
        T result = null;
        Bean<?> bean = manager.resolve(manager.getBeans(type, qualifiers));
        if (bean != null) {
            CreationalContext<?> context = manager.createCreationalContext(bean);
            if (context != null) {
                result = (T) manager.getReference(bean, type, context);
            }
        }
        return result;
    }

    private PortletLifecycleContext requestContextCache;

    private PortletLifecycleContext requestContext() {
        if (requestContextCache == null) {
            System.out.println("******* " + Container.instance().deploymentManager().instance().select(HttpRequestContext.class).get());
            this.requestContextCache = Container.instance().deploymentManager().instance().select(PortletLifecycleContext.class).get();
            System.out.println("---- " + requestContextCache);
        }
        return requestContextCache;
    }
}
