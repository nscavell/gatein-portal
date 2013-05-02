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

import java.lang.annotation.Annotation;

import javax.servlet.http.HttpServletRequest;

import org.gatein.api.cdi.context.PortletLifecycleScoped;
import org.gatein.cdi.contexts.beanstore.LocalBeanStore;
import org.gatein.cdi.contexts.state.Transition;

import static javax.portlet.PortletRequest.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PortletLifecycleContextImpl extends AbstractCDIPortletContext implements PortletLifecycleContext {

    private static final String ATTR_ID = PortletLifecycleContextImpl.class.getName();

    public PortletLifecycleContextImpl() {
        super(false);
    }

    //TODO: I think it would be cleaner to have transition methods like onActionStart, onRenderStart, etc.
    @Override
    public void transitionTo(String windowId, Transition.State state) {
        Transition transition = getTransition(windowId);
        if (transition == null) {
            setTransition(new Transition(state));
        } else {
            transition.to(state);
            if (state.started()) {
                if (state.isPhase(ACTION_PHASE)) {
                    destroy(windowId);
                    setTransition(new Transition(state));
                }
            } else if (state.ended()) {
                if (state.isPhase(RENDER_PHASE, RESOURCE_PHASE)) {
                    destroy(windowId);
                }
            }
        }
    }

    @Override
    public void associate(HttpServletRequest request) {
        if (request.getAttribute(ATTR_ID) == null) {
            request.setAttribute(ATTR_ID, ATTR_ID);
            setBeanStore(new LocalBeanStore());
        }
    }

    @Override
    public void dissociate(HttpServletRequest request) {
        if (request.getAttribute(ATTR_ID) != null) {
            request.removeAttribute(ATTR_ID);
            destroy();
        }
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return PortletLifecycleScoped.class;
    }
}
