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

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpSessionEvent;

import org.gatein.pc.api.invocation.ActionInvocation;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.RenderInvocation;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class CDIServletListener extends AbstractServletListener {

    @Inject
    private BeanManager beanManager;

    @Override
    public void requestInitialized(ServletRequestEvent event) {
        ServletRequest request = event.getServletRequest();
        boolean attached = PortletLifecycleContext.isAttached();
        if (attached) {
            if (!isRenderRequest(request)) {
                PortletLifecycleContext.attach();
            }
        } else {
            PortletLifecycleContext.attach();
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent event) {
        ServletRequest request = event.getServletRequest();
        boolean attached = PortletLifecycleContext.isAttached();
        if (attached) {
            if (!isActionRequest(request)) {
                PortletLifecycleContext.detach();
            }
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }

    private boolean isRenderRequest(ServletRequest request) {
        PortletInvocation invocation = (PortletInvocation) request.getAttribute("cdi.portlet.invocation");
        return invocation instanceof RenderInvocation;
    }

    private boolean isActionRequest(ServletRequest request) {
        PortletInvocation invocation = (PortletInvocation) request.getAttribute("cdi.portlet.invocation");
        return invocation instanceof ActionInvocation;
    }
}
