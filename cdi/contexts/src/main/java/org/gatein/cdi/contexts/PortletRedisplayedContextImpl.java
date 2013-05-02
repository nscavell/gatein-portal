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
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.gatein.api.cdi.context.PortletRedisplayScoped;
import org.gatein.cdi.contexts.beanstore.SessionBeanStore;
import org.gatein.cdi.contexts.state.Transition;

import static javax.portlet.PortletRequest.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PortletRedisplayedContextImpl extends AbstractCDIPortletContext implements PortletRedisplayedContext {

    private static final String ATTR_ID = PortletRedisplayedContextImpl.class.getName();
    private static final String TRANSITION_PREFIX = Transition.class.getName();
    private static final String TRANSITION_DELIM = "#";

    public PortletRedisplayedContextImpl() {
        super(true);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return PortletRedisplayScoped.class;
    }

    @Override
    public void transitionTo(String windowId, Transition.State state) {
        SessionBeanStore store = (SessionBeanStore) getBeanStore();
        String attributeName = prefix(windowId);
        HttpSession session = store.getSession(true);

        Transition transition = getTransition(windowId);
        if (transition == null) {
            transition = (Transition) session.getAttribute(attributeName);
            if (transition == null) {
                transition = new Transition();
            }
        }

        // Logic on the transition
        //TODO: The following logic assumes that an end of a render or resource is the last of the transition
        if (state.started()) {
            if (state.isPhase(ACTION_PHASE)) {
                destroy(windowId);
                transition = new Transition(state);
            } else if (state.isPhase(EVENT_PHASE) && transition.last() == null) {
                destroy(windowId);
                transition = new Transition(state);
            }
        } else if (state.ended()) {
            if (state.isPhase(RENDER_PHASE, RESOURCE_PHASE)) {
                transition = null;
            }
        }

        if (transition != null) {
            transition.to(state);
            session.setAttribute(attributeName, transition);
        } else {
            // We assume it's the end of the 'lifecycle' transition, so we remove it from session
            session.removeAttribute(attributeName);
        }

        setTransition(transition);
    }

    @Override
    public void associate(HttpServletRequest request) {
        if (request.getAttribute(ATTR_ID) == null) {
            request.setAttribute(ATTR_ID, ATTR_ID);
            setBeanStore(new SessionBeanStore(request));
        }
    }

    @Override
    public void dissociate(HttpServletRequest request) {
        if (request.getAttribute(ATTR_ID) != null) {
            request.removeAttribute(ATTR_ID);
            setBeanStore(null);
        }
    }

    @Override
    public void dissociate(HttpSession session) {
        setBeanStore(new SessionBeanStore(session));
        destroy();
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (name.startsWith(TRANSITION_PREFIX)) {
                session.removeAttribute(name);
            }
        }
    }

    private static String prefix(String id) {
        return TRANSITION_PREFIX + TRANSITION_DELIM + id;
    }
}
