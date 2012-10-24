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

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NavigationServiceException;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.Scope;
import org.gatein.api.ApiException;
import org.gatein.api.NavigationNotFoundException;
import org.gatein.api.impl.Util;
import org.gatein.api.portal.navigation.Navigation;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.portal.navigation.NodeVisitor;
import org.gatein.api.portal.site.Site;
import org.gatein.api.portal.site.Site.Id;

public class NavigationServiceContext
{
   private Id siteId;
   private SiteKey siteKey;
   private NavigationService service;
   private org.exoplatform.portal.mop.navigation.NavigationContext navCtx;
   private NodeContext<NodeContext<?>> rootNodeCtx;
   private Scope scope;

   public NavigationServiceContext(NavigationService service, Site.Id siteId, NodeVisitor visitor)
   {
      this(service, siteId, visitor != null ? new NodeVisitorScope(visitor) : Scope.ALL);
   }

   public NavigationServiceContext(NavigationService service, Site.Id siteId, Scope scope)
   {
      this.service = service;
      this.siteId = siteId;
      this.scope = scope;

      siteKey = Util.from(siteId);

      try
      {
         navCtx = service.loadNavigation(siteKey);
         if (navCtx != null)
         {
            rootNodeCtx = service.loadNode(NodeModel.SELF_MODEL, navCtx, scope, null);
         }
      }
      catch (NavigationServiceException e)
      {
         throw new ApiException("Failed to load navigation", e);
      }
   }

   public NavigationContext getNavigationContext()
   {
      return navCtx;
   }

   public Navigation getNavigation()
   {
      if (navCtx == null)
      {
         return null;
      }

      return NavigationUtil.from(siteId, navCtx, rootNodeCtx);
   }

   public Node getNode(NodePath nodePath)
   {
      if (navCtx == null)
      {
         throw new NavigationNotFoundException(siteId);
      }

      return NavigationUtil.findNode(getNavigation(), nodePath);
   }

   public void updateNavigation(Navigation navigation)
   {
      if (navCtx == null)
      {
         navCtx = new NavigationContext(Util.from(navigation.getSiteId()), new NavigationState(navigation.getPriority()));
      }
      else
      {
         if (navigation.getPriority() != navCtx.getState().getPriority())
         {
            navCtx.setState(new NavigationState(navigation.getPriority()));
         }
      }

      try
      {
         service.saveNavigation(navCtx);
         if (rootNodeCtx != null)
         {
            rootNodeCtx = service.loadNode(NodeModel.SELF_MODEL, navCtx, scope, null);
         }
      }
      catch (NavigationServiceException e)
      {
         throw new ApiException("Failed to save navigation", e);
      }
   }

   public void saveNodes()
   {
      try
      {
         service.saveNode(rootNodeCtx, null);
      }
      catch (NavigationServiceException e)
      {
         throw new ApiException("Failed to save nodes", e);
      }
   }
}
