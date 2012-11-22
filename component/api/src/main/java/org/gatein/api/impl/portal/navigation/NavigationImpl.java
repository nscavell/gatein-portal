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
import org.exoplatform.portal.mop.navigation.Scope;
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
   private final ApiNodeModel model;

   private NavigationContext navCtx;

   private final NavigationService navigationService;
   private final SiteId siteId;
   private final SiteKey siteKey;

   public NavigationImpl(SiteId siteId, NavigationService navigationService, DescriptionService descriptionService)
   {
      this.siteId = siteId;
      this.navigationService = navigationService;
      this.descriptionService = descriptionService;

      this.siteKey = Util.from(siteId);
      this.model = new ApiNodeModel(siteId);

      updateNavigationContext();
   }

   @Override
   public boolean deleteNode(NodePath path)
   {
      NodeContext<ApiNode> ctx = loadNode(new NodeVisitorScope(Nodes.visitNodes(path)));
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
      NodeContext<ApiNode> ctx = loadNode(new NodeVisitorScope(visitor));
      loadLabels(ctx);
      return ctx.getNode();
   }

   @Override
   public Integer getPriority()
   {
      updateNavigationContext();
      return navCtx.getState().getPriority();
   }

   @Override
   public SiteId getSiteId()
   {
      return siteId;
   }

   @Override
   public void loadChildren(Node parent)
   {
      NodeContext<ApiNode> ctx = ((ApiNode) parent).getContext();
      NodeVisitor visitor = Nodes.visitNodes(parent.getNodePath(), Nodes.visitChildren());
      refreshNode(ctx, new NodeVisitorScope(visitor));
      loadLabels(ctx);
   }

   @Override
   public void refreshNode(Node node)
   {
      NodeContext<ApiNode> ctx = ((ApiNode) node).getContext();
      refreshNode(ctx, null);
      loadLabels(ctx);
   }

   @Override
   public void saveNode(Node node)
   {
      NodeContext<ApiNode> ctx = ((ApiNode) node).getContext();
      save(ctx);
      saveLabels(ctx);
   }

   @Override
   public void setPriority(Integer priority)
   {
      navCtx.setState(new NavigationState(priority));
      save(navCtx);
   }

   private void loadLabels(NodeContext<ApiNode> ctx)
   {
      String simple = ctx.getState().getLabel();
      if (simple != null)
      {
         ctx.getNode().setLabelInternal(new Label(simple));
      }
      else
      {
         Map<Locale, Described.State> descriptions = descriptionService.getDescriptions(ctx.getId());
         if (descriptions != null)
         {
            ctx.getNode().setLabelInternal(ObjectFactory.createLabel(descriptions));
         }
      }

      if (ctx.isExpanded())
      {
         for (NodeContext<ApiNode> c = ctx.getFirst(); c != null; c = c.getNext())
         {
            loadLabels(c);
         }
      }
   }

   private NodeContext<ApiNode> loadNode(Scope scope)
   {
      try
      {
         return navigationService.loadNode(model, navCtx, scope, null);
      }
      catch (NavigationServiceException e)
      {
         throw new ApiException("Failed to load node", e);
      }
   }

   private void refreshNode(NodeContext<ApiNode> ctx, Scope scope)
   {
      try
      {
         navigationService.rebaseNode(ctx, scope, null);
      }
      catch (NavigationServiceException e)
      {
         throw new ApiException("Failed to refresh node", e);
      }
   }

   private void save(NodeContext<ApiNode> ctx)
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

   private void save(NavigationContext ctx)
   {
      try
      {
         navigationService.saveNavigation(ctx);
      }
      catch (NavigationServiceException e)
      {
         throw new ApiException("Failed to save navigation", e);
      }
   }

   private void saveLabels(NodeContext<ApiNode> ctx)
   {
      ApiNode node = ctx.getNode();
      if (node.isLabelChanged())
      {
         if (!node.getLabel().isLocalized())
         {
            Map<Locale, Described.State> descriptions = descriptionService.getDescriptions(ctx.getId());
            if (descriptions != null)
            {
               descriptionService.setDescriptions(ctx.getId(), null);
            }
         }
         else
         {
            Map<Locale, State> descriptions = ObjectFactory.createDescriptions(node.getLabel());
            descriptionService.setDescriptions(ctx.getId(), descriptions);
         }
      }

      for (NodeContext<ApiNode> c = ctx.getFirst(); c != null; c = c.getNext())
      {
         saveLabels(c);
      }
   }

   private void updateNavigationContext()
   {
      try
      {
         navCtx = navigationService.loadNavigation(siteKey);
      }
      catch (NavigationServiceException e)
      {
         throw new ApiException("Failed to load navigation", e);
      }

      if (navCtx == null)
      {
         throw new SiteNotFoundException(siteId);
      }
   }
}
