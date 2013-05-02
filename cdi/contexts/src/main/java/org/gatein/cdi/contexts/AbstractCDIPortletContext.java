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

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.PassivationCapable;

import org.exoplatform.portal.pc.aspects.PortletLifecyclePhaseInterceptor;
import org.gatein.cdi.contexts.beanstore.BeanStore;
import org.gatein.cdi.contexts.beanstore.BeanStoreInstance;
import org.gatein.cdi.contexts.beanstore.LockedBean;
import org.gatein.cdi.contexts.beanstore.serial.SerializableBeanStoreInstance;
import org.gatein.cdi.contexts.state.Transition;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public abstract class AbstractCDIPortletContext implements CDIPortletContext {

    private final ThreadLocal<BeanStore> beanStore;
    private final ThreadLocal<Map<String, Transition>> transitions;
    private final ThreadLocal<Transition> transition;
    private final boolean multithreaded;

    protected AbstractCDIPortletContext(boolean multithreaded) {
        this.multithreaded = multithreaded;
        this.beanStore = new ThreadLocal<BeanStore>();
        this.transitions = new ThreadLocal<Map<String, Transition>>();
        this.transition = new ThreadLocal<Transition>();
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        if (!isActive()) {
            throw new ContextNotActiveException();
        }

        BeanStore beanStore = getBeanStore();
        if (beanStore == null) {
            return null;
        }

        String id = getId(contextual);
        BeanStoreInstance<T> beanInstance = beanStore.getBean(id);
        if (beanInstance != null) {
            return beanInstance.getInstance();
        } else if (creationalContext != null) {
            LockedBean lock = null;
            try {
                if (multithreaded) {
                    lock = beanStore.lock(id);
                    beanInstance = beanStore.getBean(id);
                    if (beanInstance != null) {
                        return beanInstance.getInstance();
                    }
                }

                T instance = contextual.create(creationalContext);
                if (instance != null) {
                    beanInstance = new SerializableBeanStoreInstance<T>(contextual, instance, creationalContext);
                    beanStore.put(id, beanInstance);
                }
                return instance;
            } finally {
                if (lock != null) {
                    lock.unlock();
                }
            }
        } else {
            return null;
        }
    }

    @Override
    public <T> T get(Contextual<T> contextual) {
        return get(contextual, null);
    }

    @Override
    public boolean isActive() {
        return transition.get() != null;
    }

    protected void setTransition(Transition transition) {
        if (transition == null) {
            this.transition.remove();
        } else {
            this.transition.set(transition);
        }
    }

    protected Transition getTransition(String windowId) {
        Map<String, Transition> map = transitions.get();
        if (map == null) {
            map = new HashMap<String, Transition>();
            transitions.set(map);
        }

        return map.get(windowId);
    }

    /*@Override
    public boolean isActive() {
        return isActive(windowId());
    }
    */

    /*
    @Override
    public boolean isActive(String windowId) {
        //ActivePortlets portlets = activePortlets.get();
        //return (portlets != null) && portlets.isActive(windowId);
    }

    @Override
    public void activate(String windowId) {
        ActivePortlets portlets = activePortlets.get();
        if (portlets == null) {
            portlets = new ActivePortlets();
            activePortlets.set(portlets);
        }

        portlets.activate(windowId);
    }

    @Override
    public void deactivate(String windowId) {
        ActivePortlets portlets = activePortlets.get();
        if (portlets != null) {
            portlets.deactivate(windowId);
            BeanStore store = getBeanStore();
            if (store != null) {
                store.clear(windowId);
            }
        }
    }*/

    protected BeanStore getBeanStore() {
        return beanStore.get();
    }

    protected void setBeanStore(BeanStore beanStore) {
        if (beanStore == null) {
            this.beanStore.remove();
        } else {
            this.beanStore.set(beanStore);
        }
    }

    protected void destroy(String windowId) {
        try {
            BeanStore store = getBeanStore();
            if (store != null) {
                store.destroy(windowId);
            }
        } finally {
            Map<String, Transition> map = transitions.get();
            if (map != null) {
                map.remove(windowId);
            }
        }
    }

    protected void destroy() {
        try {
            BeanStore store = getBeanStore();
            if (store != null) {
                store.destroy();
            }
        } finally {
            cleanup();
        }
    }

    protected void cleanup() {
        transitions.remove();
        beanStore.remove();
    }

    //TODO: Need to revisit this implementation
    private static String getId(Contextual contextual) {
        String id;
        if (contextual instanceof PassivationCapable) {
            id = ((PassivationCapable) contextual).getId();
        } else {
            id = "" + contextual.hashCode();
        }

        return windowId() + "#" + id;
    }

    private static String windowId() {
        return PortletLifecyclePhaseInterceptor.currentWindowId();
    }
}
