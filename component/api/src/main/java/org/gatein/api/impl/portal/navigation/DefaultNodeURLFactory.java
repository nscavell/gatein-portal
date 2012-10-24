/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.gatein.api.impl.portal.navigation;

import java.net.URI;
import java.net.URISyntaxException;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;
import org.gatein.api.ApiException;
import org.gatein.api.impl.Util;
import org.gatein.api.internal.URLFactory;
import org.gatein.api.portal.navigation.Node;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class DefaultNodeURLFactory implements URLFactory
{
   @Override
   public URI createURL(Node node)
   {
      try
      {
         SiteKey siteKey = Util.from(node.getPageId().getSiteId());
         RequestContext requestContext = RequestContext.getCurrentInstance();
         NavigationResource navResource = new NavigationResource(siteKey, node.getPath().subPath(1, node.getPath().size())
               .toString().substring(1));
         NodeURL nodeURL = requestContext.createURL(NodeURL.TYPE, navResource);
         nodeURL.setSchemeUse(true);
         return new URI(nodeURL.toString());
      }
      catch (URISyntaxException e)
      {
         throw new ApiException("Failed to resolve url", e);
      }
   }
}
