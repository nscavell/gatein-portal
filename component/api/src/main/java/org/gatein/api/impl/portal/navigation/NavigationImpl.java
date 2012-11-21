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
package org.gatein.api.impl.portal.navigation;

import java.util.Locale;
import java.util.Map;

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.Described.State;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NavigationServiceException;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.gatein.api.ApiException;
import org.gatein.api.SiteNotFoundException;
import org.gatein.api.impl.Util;
import org.gatein.api.impl.portal.navigation.scope.NodeVisitorScope;
import org.gatein.api.portal.Label;
import org.gatein.api.portal.navigation.Navigation;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.portal.navigation.NodeVisitor;
import org.gatein.api.portal.navigation.Nodes;
import org.gatein.api.portal.site.SiteId;

public class NavigationImpl implements Navigation
{
   private final DescriptionService descriptionService;
   private final NavigationService navigationService;

   private final ApiNodeContext model;

   private final SiteId siteId;
   private final SiteKey siteKey;

   public NavigationImpl(SiteId siteId, NavigationService navigationService, DescriptionService descriptionService)
   {
      this.siteId = siteId;
      this.navigationService = navigationService;
      this.descriptionService = descriptionService;

      this.siteKey = Util.from(siteId);
      this.model = new ApiNodeContext(siteId);
   }

   @Override
   public boolean deleteNode(NodePath path)
   {
      NodeContext<ApiNodeModel> ctx = getNodeContext(Nodes.visitNodes(path));
      ctx = ctx.getNode().getDescendantContext(path);
      if (ctx == null)
      {
         return false;
      }

      ctx.remove();
      save(ctx);
      return true;
   }

   @Override
   public Node getNode(NodeVisitor visitor)
   {
      // TODO Load labels
      return getNodeContext(visitor).getNode();
   }

   @Override
   public Integer getPriority()
   {
      NavigationContext ctx = getNavigationContext();
      return ctx != null ? ctx.getState().getPriority() : null;
   }

   @Override
   public SiteId getSiteId()
   {
      return siteId;
   }

   @Override
   public void loadChildren(Node parent)
   {
      try
      {
         NodeVisitor visitor = Nodes.visitNodes(parent.getNodePath(), Nodes.visitChildren());
         navigationService.rebaseNode(getNodeContext(parent), new NodeVisitorScope(visitor), null);
      }
      catch (NavigationServiceException e)
      {
         throw new ApiException("Failed to load children", e);
      }
   }

   @Override
   public void saveNode(Node node)
   {
      save(getNodeContext(node));

      // TODO Update label
      // saveLabel(node, ctx);
   }

   @Override
   public void setPriority(Integer priority)
   {
      NavigationContext ctx = getNavigationContext();
      if (ctx == null)
      {
         ctx = new NavigationContext(siteKey, new NavigationState(priority));
      }
      else
      {
         ctx.setState(new NavigationState(priority));
      }
      navigationService.saveNavigation(ctx);
   }

   private Label getLabel(NodeContext<ApiNodeModel> ctx)
   {
      if (ctx.getState().getLabel() != null)
      {
         return new Label(ctx.getState().getLabel());
      }
      else
      {
         Map<Locale, Described.State> descriptions = descriptionService.getDescriptions(ctx.getId());
         return ObjectFactory.createLabel(descriptions);
      }
   }

   private NavigationContext getNavigationContext()
   {
      NavigationContext ctx = navigationService.loadNavigation(siteKey);
      if (ctx == null)
      {
         throw new SiteNotFoundException(siteId);
      }
      return ctx;
   }

   private NodeContext<ApiNodeModel> getNodeContext(Node node)
   {
      return ((ApiNodeModel) node).getContext();
   }

   private NodeContext<ApiNodeModel> getNodeContext(NodeVisitor visitor)
   {
      NavigationContext ctx = getNavigationContext();
      if (ctx == null)
      {
         return null;
      }
      return navigationService.loadNode(model, ctx, new NodeVisitorScope(visitor), null);
   }

   private void save(NodeContext<ApiNodeModel> ctx)
   {
      try
      {
         navigationService.saveNode(ctx, null);
      }
      catch (NavigationServiceException e)
      {
         throw new ApiException("Failed to save node", e);
      }
   }

   private void saveLabel(Node node, NodeContext<ApiNodeModel> ctx)
   {
      if (node.getLabel() != null && node.getLabel().isLocalized())
      {
         if (!node.getLabel().equals(getLabel(ctx)))
         {
            Map<Locale, State> descriptions = ObjectFactory.createDescriptions(node.getLabel());
            descriptionService.setDescriptions(ctx.getId(), descriptions);
         }
      }

      for (Node c : node)
      {
         saveLabel(c, ctx.get(c.getName()));
      }
   }
}
