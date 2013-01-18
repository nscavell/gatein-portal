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

package org.gatein.management.gadget.mop.exportimport.server;


import org.gatein.management.api.ContentType;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.controller.ExternalManagedRequest;
import org.gatein.management.api.controller.ManagedRequest;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class GwtManagedRequest implements ExternalManagedRequest {

    private final ManagedRequest managedRequest;
    private final RoleResolver roleResolver;

    public GwtManagedRequest(ManagedRequest managedRequest) {
        this.managedRequest = managedRequest;

        // Our gadget doesn't have any knowledge of the authenticated user. However through page permissions and portal
        // settings, if an admin allows the gadget to be exposed, then no security restrictions are set and all functionality
        // will be allowed.
        this.roleResolver = new RoleResolver() {
            @Override
            public boolean isUserInRole(String s) {
                return true;
            }
        };
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public Object getRequest() {
        return null;
    }

    @Override
    public RoleResolver getRoleResolver() {
        return roleResolver;
    }

    @Override
    public String getOperationName() {
        return managedRequest.getOperationName();
    }

    @Override
    public PathAddress getAddress() {
        return managedRequest.getAddress();
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        return managedRequest.getAttributes();
    }

    @Override
    public InputStream getDataStream() {
        return managedRequest.getDataStream();
    }

    @Override
    public ContentType getContentType() {
        return managedRequest.getContentType();
    }

    @Override
    public Locale getLocale() {
        return managedRequest.getLocale();
    }

    @Override
    public void setLocale(Locale locale) {
        managedRequest.setLocale(locale);
    }
}
