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

package org.exoplatform.portal.application;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;
import org.gatein.api.Portal;
import org.gatein.api.PortalRequest;
import org.gatein.api.Util;
import org.gatein.api.common.URIResolver;
import org.gatein.api.navigation.NodePath;
import org.gatein.api.security.User;
import org.gatein.api.site.SiteId;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PortalRequestImpl extends PortalRequest {
    private final PortalRequestContext context;
    private final User user;
    private final SiteId siteId;
    private final NodePath nodePath;
    private final Portal portal;
    private final URIResolver uriResolver;

    private PortalRequestImpl(PortalRequestContext context) {
        this.context = context;
        String userId = context.getRemoteUser();

        this.user = (userId == null) ? User.anonymous() : new User(userId);
        this.siteId = Util.from(context.getSiteKey());
        this.nodePath = NodePath.fromString(context.getNodePath());
        this.portal = getPortalApi(context);

        uriResolver = new RequestContextURIResolver();
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public SiteId getSiteId() {
        return siteId;
    }

    @Override
    public NodePath getNodePath() {
        return nodePath;
    }

    @Override
    public Locale getLocale() {
        return context.getLocale();
    }

    @Override
    public Portal getPortal() {
        return portal;
    }

    @Override
    public URIResolver getURIResolver() {
        return uriResolver;
    }

    private static Portal getPortalApi(PortalRequestContext context) {
        return (Portal) context.getApplication().getApplicationServiceContainer().getComponentInstanceOfType(Portal.class);
    }

    static void createInstance(PortalRequestContext context) {
        PortalRequest request = new PortalRequestImpl(context);
        PortalRequest.setInstance(request);
    }

    static void clearInstance() {
        PortalRequest.setInstance(null);
    }

    public class RequestContextURIResolver implements org.gatein.api.common.URIResolver {
        public URI resolveURI(SiteId siteId, NodePath path) {
            try {
                SiteKey siteKey = Util.from(siteId);
                NavigationResource navResource = new NavigationResource(siteKey, path.toString().substring(1));
                NodeURL nodeURL = context.createURL(NodeURL.TYPE, navResource);
                nodeURL.setSchemeUse(true);
                return new URI(nodeURL.toString());
            } catch (URISyntaxException e) {
                return null;
            }
        }
    }
}
