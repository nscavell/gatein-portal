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

import org.gatein.api.Portal;
import org.gatein.api.PortalRequest;
import org.gatein.api.impl.Util;
import org.gatein.api.portal.User;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.portal.navigation.Nodes;
import org.gatein.api.portal.site.Site;

import java.util.Locale;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PortalRequestImpl extends PortalRequest
{
   private final PortalRequestContext context;
   private final User user;
   private final Site.Id siteId;
   private final NodePath nodePath;
   private final Portal portal;

   private PortalRequestImpl(PortalRequestContext context)
   {
      this.context = context;
      String userId = context.getRemoteUser();

      this.user = (userId == null) ? User.anonymous() : new User(userId);
      this.siteId = Util.from(context.getSiteKey());
      this.nodePath = Nodes.path(context.getNodePath());
      this.portal = getPortalApi(context);
   }

   @Override
   public User getUser()
   {
      return user;
   }

   @Override
   public Site.Id getSiteId()
   {
      return siteId;
   }

   @Override
   public NodePath getNodePath()
   {
      return nodePath;
   }

   @Override
   public Locale getLocale()
   {
      return context.getLocale();
   }

   @Override
   public Portal getPortal()
   {
      return portal;
   }

   private static Portal getPortalApi(PortalRequestContext context)
   {
      return (Portal) context.getApplication().getApplicationServiceContainer().getComponentInstanceOfType(Portal.class);
   }

   static void createInstance(PortalRequestContext context)
   {
      PortalRequest request = new PortalRequestImpl(context);
      PortalRequest.setInstance(request);
   }

   static void clearInstance()
   {
      PortalRequest.setInstance(null);
   }
}
