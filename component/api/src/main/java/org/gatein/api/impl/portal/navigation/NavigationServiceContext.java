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

import java.util.List;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NavigationServiceException;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.Scope;
import org.gatein.api.ApiException;
import org.gatein.api.NavigationNotFoundException;
import org.gatein.api.impl.Util;
import org.gatein.api.impl.portal.navigation.scope.NodeVisitorScope;
import org.gatein.api.portal.navigation.Navigation;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.portal.navigation.NodeVisitor;
import org.gatein.api.portal.site.SiteId;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class NavigationServiceContext
{
   private final SiteId siteId;

   private final SiteKey siteKey;

   private final NavigationService service;

   private final Scope scope;

   private NavigationContext navCtx;

   private NodeContext<NodeContext<?>> rootNodeCtx;

   public NavigationServiceContext(NavigationService service, SiteId siteId, NodeVisitor visitor)
   {
      this(service, siteId, visitor != null ? new NodeVisitorScope(visitor) : Scope.ALL);
   }

   public NavigationServiceContext(NavigationService service, SiteId siteId, Scope scope)
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

   public Node getRootNode()
   {
      if (navCtx == null)
      {
         throw new NavigationNotFoundException(siteId);
      }

      return NavigationUtil.from(siteId, rootNodeCtx);
   }

   public Node getNode(NodePath nodePath)
   {
      if (navCtx == null)
      {
         throw new NavigationNotFoundException(siteId);
      }

      NodeContext<NodeContext<?>> nodeContext = NavigationUtil.findNodeContext(rootNodeCtx, nodePath);
      return NavigationUtil.from(siteId, nodeContext);
   }

   public void saveNavigation(Navigation navigation)
   {
      boolean create = navCtx == null;

      if (create)
      {
         navCtx = new NavigationContext(Util.from(navigation.getSiteId()), new NavigationState(navigation.getPriority()));
      }
      else
      {
         updateNavigationContext(navigation);
      }

      try
      {
         service.saveNavigation(navCtx);

         if (create)
         {
            rootNodeCtx = service.loadNode(NodeModel.SELF_MODEL, navCtx, scope, null);
         }
      }
      catch (NavigationServiceException e)
      {
         throw new ApiException("Failed to save navigation", e);
      }

      for (Node node : navigation.getNodes())
      {
         saveNode(node);
      }
   }

   private void updateNavigationContext(Navigation navigation)
   {
      if (navigation.getPriority() != navCtx.getState().getPriority())
      {
         navCtx.setState(new NavigationState(navigation.getPriority()));
      }
   }

   public void saveNode(Node node)
   {
      NodeContext<NodeContext<?>> nodeCtx = NavigationUtil.findNodeContext(rootNodeCtx, node.getPath());
      updateNodeContext(node, nodeCtx);

      try
      {
         service.saveNode(rootNodeCtx, null);
      }
      catch (NavigationServiceException e)
      {
         throw new ApiException("Failed to save node", e);
      }
   }

   public void loadNodes(Node parent)
   {
      Node updated = getNode(parent.getPath());
      merge(updated, parent);
   }
   
   private void merge(Node src, Node dst)
   {
      List<Node> children = dst.getChildren();

      dst.setIconName(src.getIconName());
      dst.setLabel(src.getLabel());
      dst.setPageId(src.getPageId());
      dst.setVisibility(src.getVisibility());

      for (Node c : children)
      {
         dst.removeNode(c.getName());
      }

      for (Node srcChild : src.getChildren())
      {
         Node dstChild = null;

         for (Node c : children)
         {
            if (c.getName().equals(srcChild.getName()))
            {
               dstChild = c;
            }
         }

         if (dstChild == null)
         {
            dstChild = src;
         }

         merge(srcChild, dstChild);
         dst.addChild(dstChild);
      }
   }

   private void updateNodeContext(Node node, NodeContext<NodeContext<?>> nodeCtx)
   {
      boolean create = nodeCtx == null;
      
      if (create)
      {
         NodeContext<NodeContext<?>> parentNodeCtx = NavigationUtil.findNodeContext(rootNodeCtx, node.getParent().getPath());
         nodeCtx = parentNodeCtx.add(node.getParent().getChildren().indexOf(node), node.getName());
      }

      if (!node.getName().equals(nodeCtx.getName()))
      {
         nodeCtx.setName(node.getName());
      }

      NodeState nodeState = NavigationUtil.from(node, nodeCtx);
      if (!nodeState.equals(nodeCtx.getState()))
      {
         nodeCtx.setState(nodeState);
      }

      for (NodeContext<?> childCtx : nodeCtx.getNodes())
      {
         if (node.getChild(childCtx.getName()) == null)
         {
            if (!nodeCtx.removeNode(childCtx.getName()))
            {
               throw new ApiException("Failed to remove child");
            }
         }
      }

      for (Node childNode : node.getChildren())
      {
         NodeContext<NodeContext<?>> childNodeCtx = nodeCtx.get(childNode.getName());
         updateNodeContext(childNode, childNodeCtx);
      }
   }
}
