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
package org.gatein.api.navigation;

import java.net.URI;
import java.net.URISyntaxException;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;
import org.gatein.api.Util;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class URIResolver {
    private URIResolver() {
    }

    public static URI resolveURI(ApiNode node) {
        try {
            SiteKey siteKey = Util.from(node.getSiteId());
            RequestContext requestContext = RequestContext.getCurrentInstance();
            if (requestContext != null) {
                NavigationResource navResource = new NavigationResource(siteKey, node.getNodePath().toString().substring(1));
                NodeURL nodeURL = requestContext.createURL(NodeURL.TYPE, navResource);
                nodeURL.setSchemeUse(true);
                return new URI(nodeURL.toString());
            }
        } catch (URISyntaxException e) {
        }

        return null;
    }
}
