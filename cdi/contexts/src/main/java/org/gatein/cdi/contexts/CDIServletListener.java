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

package org.gatein.cdi.contexts;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.gatein.cdi.contexts.state.Transition;

import static org.exoplatform.portal.pc.aspects.PortletLifecyclePhaseInterceptor.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class CDIServletListener implements ServletRequestListener, HttpSessionListener {


    @Override
    public void requestInitialized(ServletRequestEvent event) {
        String windowId = currentWindowId();
        String phase = currentPhase();

        // The phase is null when we access the application registry, so we don't need to do anything
        if (phase != null) {
            for (CDIPortletContext context : CDIPortletContextExtension.getContexts()) {
                context.transitionTo(windowId, Transition.State.starting(phase));
            }
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent event) {
        String windowId = currentWindowId();
        String phase = currentPhase();

        // The phase is null when we access the application registry, so we don't need to do anything
        if (phase != null) {
            for (CDIPortletContext context : CDIPortletContextExtension.getContexts()) {
                context.transitionTo(windowId, Transition.State.ending(phase));
            }
        }
    }

    @Override
    public void sessionCreated(HttpSessionEvent event) {
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        PortletRedisplayedContext context = CDIPortletContextExtension.getContext(PortletRedisplayedContext.class);
        if (context != null) {
            context.dissociate(event.getSession());
        }
    }
}
