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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NavigationServiceException;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeFilter;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.Scope;
import org.gatein.api.ApiException;
import org.gatein.api.NavigationNotFoundException;
import org.gatein.api.impl.Util;
import org.gatein.api.impl.portal.navigation.filter.NodeFilterWrapper;
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
import org.gatein.api.util.Filter;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class NavigationServiceContext
{
   private DescriptionService descriptionService;

   private NodeFilter filter;

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

   private Label getLabel(NodeContext<NodeContext<?>> node)
   {
      if (node.getState().getLabel() != null)
      {
         return new Label(node.getState().getLabel());
      }

      Map<Locale, Described.State> descriptions = descriptionService.getDescriptions(node.getId());
      if (descriptions != null && !descriptions.isEmpty())
      {
         Map<Locale, String> m = new HashMap<Locale, String>();
         for (Map.Entry<Locale, Described.State> entry : descriptions.entrySet())
         {
            m.put(entry.getKey(), entry.getValue().getName());
         }
         return new Label(m);
      }

      return null;
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
      itr.next();

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
      itr.next();

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
            if (filter != null)
            {
               rootNodeCtx.filter(filter);
            }

            navigation = new Navigation(siteId, navCtx.getState().getPriority());
            for (NodeContext<?> c : rootNodeCtx.getNodes())
            {
               @SuppressWarnings("unchecked")
               Node n = getNode((NodeContext<NodeContext<?>>) c);
               navigation.addNode(n);
            }

            Node rootNode = NodeAccessor.getRootNode(navigation);
            rootNode.setBaseURI(NodeURLFactory.createURL(rootNode));
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
      merge(updated, parent);
   }

   private void merge(Node src, Node dst)
   {
      List<Node> children = new LinkedList<Node>(dst.getNodes());

      dst.setIconName(src.getIconName());
      dst.setLabel(src.getLabel());
      dst.setPageId(src.getPageId());
      dst.setVisibility(src.getVisibility());
      dst.getNodes().clear();

      for (Node srcChild : src.getNodes())
      {
         Node dstChild = null;

         for (Node c : children)
         {
            if (c.getName().equals(srcChild.getName()))
            {
               dstChild = c;
               merge(srcChild, dstChild);
            }
         }

         if (dstChild == null)
         {
            dstChild = src;
         }

         dst.addNode(dstChild);
      }
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

   public void saveNode(Node node)
   {
      NodeContext<NodeContext<?>> nodeCtx = getNodeContext(node.getNodePath());
      updateNodeContext(node, nodeCtx);

      try
      {
         service.saveNode(nodeCtx, null);
      }
      catch (NavigationServiceException e)
      {
         throw new ApiException("Failed to save node", e);
      }
   }

   public void setFilter(Filter<Node> filter)
   {
      if (filter != null)
      {
         this.filter = new NodeFilterWrapper(filter);
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

   private void updateNodeContext(Node node, NodeContext<NodeContext<?>> nodeCtx)
   {
      boolean create = nodeCtx == null;

      if (create)
      {
         NodeContext<NodeContext<?>> parentNodeCtx = getNodeContext(node.getParent().getNodePath());
         nodeCtx = parentNodeCtx.add(node.getParent().getNodes().indexOf(node), node.getName());
      }

      if (!node.getName().equals(nodeCtx.getName()))
      {
         nodeCtx.setName(node.getName());
      }

      NodeState nodeState = ObjectFactory.createNodeState(node);
      if (!nodeState.equals(nodeCtx.getState()))
      {
         nodeCtx.setState(nodeState);
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
         updateNodeContext(childNode, childNodeCtx);
      }
   }
}
