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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.Scope;
import org.gatein.api.ApiException;
import org.gatein.api.NavigationNotFoundException;
import org.gatein.api.impl.Util;
import org.gatein.api.impl.portal.navigation.scope.LoadedNodeScope;
import org.gatein.api.impl.portal.navigation.scope.NodePathScope;
import org.gatein.api.impl.portal.navigation.scope.NodeVisitorScope;
import org.gatein.api.portal.Label;
import org.gatein.api.portal.navigation.Navigation;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodeAccessor;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.portal.navigation.NodeVisitor;
import org.gatein.api.portal.site.SiteId;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class NavigationServiceContext
{
   private DescriptionService descriptionService;

   private NavigationContext navCtx;

   private Navigation navigation;

   private NodeContext<NodeContext<?>> rootNodeCtx;

   private Scope scope;

   private final NavigationService service;

   private final SiteId siteId;

   private final SiteKey siteKey;

   public NavigationServiceContext(NavigationService service, DescriptionService descriptionService, SiteId siteId)
   {
      this.service = service;
      this.descriptionService = descriptionService;
      this.siteId = siteId;

      siteKey = Util.from(siteId);
   }

   private Node getNode(NodeContext<NodeContext<?>> nodeCtx)
   {
      Node node = ObjectFactory.createNode(nodeCtx.getName(), nodeCtx.getState());
      node.setLabel(getLabel(nodeCtx));

      if (nodeCtx.isExpanded())
      {
         for (NodeContext<?> c : nodeCtx.getNodes())
         {
            @SuppressWarnings("unchecked")
            Node n = getNode((NodeContext<NodeContext<?>>) c);
            node.addNode(n);
         }
         NodeAccessor.setNodesLoaded(node, true);
      }

      return node;
   }

   private Label getLabel(NodeContext<NodeContext<?>> nodeCtx)
   {
      if (nodeCtx.getState().getLabel() != null)
      {
         return new Label(nodeCtx.getState().getLabel());
      }
      else
      {
         Map<Locale, Described.State> descriptions = descriptionService.getDescriptions(nodeCtx.getId());
         return ObjectFactory.createLabel(descriptions);
      }
   }

   public Navigation getNavigation()
   {
      return navigation;
   }

   public NavigationContext getNavigationContext()
   {
      return navCtx;
   }

   public Node getNode(NodePath nodePath)
   {
      if (navigation == null)
      {
         throw new NavigationNotFoundException(siteId);
      }

      Iterator<String> itr = nodePath.iterator();
      Node n = NodeAccessor.getRootNode(navigation);
      while (itr.hasNext())
      {
         n = n.getNode(itr.next());
         if (n == null)
         {
            return null;
         }
      }

      return n;
   }

   public NodeContext<NodeContext<?>> getNodeContext(NodePath nodePath)
   {
      if (navCtx == null)
      {
         throw new NavigationNotFoundException(siteId);
      }

      Iterator<String> itr = nodePath.iterator();
      NodeContext<NodeContext<?>> n = rootNodeCtx;
      while (itr.hasNext())
      {
         n = n.get(itr.next());
         if (n == null)
         {
            return null;
         }
      }
      return n;
   }

   public void init()
   {
      try
      {
         navCtx = service.loadNavigation(siteKey);
         if (navCtx != null)
         {
            rootNodeCtx = service.loadNode(NodeModel.SELF_MODEL, navCtx, scope, null);
            navigation = new Navigation(siteId, navCtx.getState().getPriority());
            if (rootNodeCtx.isExpanded())
            {
               Node rootNode = NodeAccessor.getRootNode(navigation);

               for (NodeContext<?> c : rootNodeCtx.getNodes())
               {
                  @SuppressWarnings("unchecked")
                  Node n = getNode((NodeContext<NodeContext<?>>) c);
                  rootNode.addNode(n);
               }

               NodeAccessor.setNodesLoaded(rootNode, true);
            }

            // Node rootNode = NodeAccessor.getRootNode(navigation);
            // rootNode.setBaseURI(NodeURLFactory.createBaseURL(navigation.getSiteId())); // TODO This doesn't work at the
            // moment, the URL is context dependent! for example two users may go to the server with different hostnames
         }
      }
      catch (NavigationServiceException e)
      {
         throw new ApiException("Failed to load navigation", e);
      }
   }

   public void loadNodes(Node parent)
   {
      Node updated = getNode(parent.getNodePath());
      parent.getNodes().clear();
      for (Node c : updated.getNodes())
      {
         parent.getNodes().add(new Node(c));
      }
      NodeAccessor.setNodesLoaded(parent, updated.isNodesLoaded());
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
         if (navigation.getPriority() != navCtx.getState().getPriority())
         {
            navCtx.setState(new NavigationState(navigation.getPriority()));
         }
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

      saveNode(NodeAccessor.getRootNode(navigation));
   }

   // TODO Detect if NodeContext or Descriptions have changed since Node was loaded. If they have we probably want to throw a
   // OptimisticLockException or something like that.
   public void saveNode(Node node)
   {
      NodeContext<NodeContext<?>> nodeCtx = getNodeContext(node.getNodePath());
      updateNodeContext(node, nodeCtx, null);

      try
      {
         service.saveNode(nodeCtx, null);
      }
      catch (NavigationServiceException e)
      {
         throw new ApiException("Failed to save node", e);
      }

      saveLabel(node, nodeCtx);
   }

   private void saveLabel(Node node, NodeContext<NodeContext<?>> nodeCtx)
   {
      if (node.getLabel() != null && node.getLabel().isLocalized())
      {
         if (!node.getLabel().equals(getLabel(nodeCtx)))
         {
            Map<Locale, State> descriptions = ObjectFactory.createDescriptions(node.getLabel());
            descriptionService.setDescriptions(nodeCtx.getId(), descriptions);
         }
      }

      for (Node c : node.getNodes())
      {
         saveLabel(c, nodeCtx.get(c.getName()));
      }
   }

   public void setScope(Node node)
   {
      this.scope = new LoadedNodeScope(node);
   }

   public void setScope(NodePath nodePath)
   {
      this.scope = new NodePathScope(nodePath);
   }

   public void setScope(NodeVisitor visitor)
   {
      this.scope = new NodeVisitorScope(visitor);
   }

   private void updateNodeContext(Node node, NodeContext<NodeContext<?>> nodeCtx, NodeContext<NodeContext<?>> parentNodeCtx)
   {
      boolean create = nodeCtx == null;

      if (create)
      {
         nodeCtx = parentNodeCtx.add(node.getParent().getNodes().indexOf(node), node.getName());
      }

      if (node.getParent() != null)
      {
         if (!node.getName().equals(nodeCtx.getName()))
         {
            nodeCtx.setName(node.getName());
         }

         NodeState nodeState = ObjectFactory.createNodeState(node);
         if (!nodeState.equals(nodeCtx.getState()))
         {
            nodeCtx.setState(nodeState);
         }
      }

      for (NodeContext<?> childCtx : nodeCtx.getNodes())
      {
         if (node.getNode(childCtx.getName()) == null)
         {
            if (!nodeCtx.removeNode(childCtx.getName()))
            {
               throw new ApiException("Failed to remove child");
            }
         }
      }

      for (Node childNode : node.getNodes())
      {
         NodeContext<NodeContext<?>> childNodeCtx = nodeCtx.get(childNode.getName());
         updateNodeContext(childNode, childNodeCtx, nodeCtx);
      }
   }
}
