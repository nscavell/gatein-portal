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

import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
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
import org.gatein.api.impl.StateMap;
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

   private NodeContext<NodeContext<?>> rootNodeCtx;

   private Scope scope;

   private final NavigationService service;

   private final SiteId siteId;

   private final SiteKey siteKey;

   private static StateMap<Node, NodeContext<NodeContext<?>>> stateMap = new StateMap<Node, NodeContext<NodeContext<?>>>();

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
            node.addChild(n);
         }
      }
      else
      {
         NodeAccessor.setNodesLoaded(node, false);
      }

      stateMap.put(node, nodeCtx);

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
      if (navCtx == null)
      {
         return null;
      }

      Navigation navigation = new Navigation(siteId, navCtx.getState().getPriority());

      List<Node> nodes = getNodes();
      if (nodes != null)
      {
         for (Node n : nodes)
         {
            navigation.addChild(n);
         }
      }
      else
      {
         NodeAccessor.setNodesLoaded(navigation, false);
      }

      return navigation;
   }

   private List<Node> getNodes()
   {
      if (rootNodeCtx.isExpanded())
      {
         List<Node> nodes = new LinkedList<Node>();
         for (NodeContext<?> c : rootNodeCtx.getNodes())
         {
            @SuppressWarnings("unchecked")
            Node n = getNode((NodeContext<NodeContext<?>>) c);

            URI uri = new NodeURLFactory().createBaseURL(siteId);
            if (uri != null)
            {
               n.setBaseURI(uri);
            }

            nodes.add(n);
         }
         return nodes;
      }
      else
      {
         return null;
      }
   }

   public NavigationContext getNavigationContext()
   {
      return navCtx;
   }

   public Node getNode()
   {
      Node node = Node.rootNode(siteId);

      List<Node> nodes = getNodes();
      if (nodes != null)
      {
         for (Node n : nodes)
         {
            node.addChild(n);
         }
      }
      else
      {
         NodeAccessor.setNodesLoaded(node, false);
      }

      return node;
   }

   public Node getNode(NodePath nodePath)
   {
      Node node = getNode();
      for (String n : nodePath)
      {
         node = node.getChild(n);
         if (node == null)
         {
            return null;
         }
      }
      return node;
   }

   private NodeContext<NodeContext<?>> getNodeContext(NodePath nodePath)
   {
      NodeContext<NodeContext<?>> ctx = rootNodeCtx;
      for (String n : nodePath)
      {
         ctx = ctx.isExpanded() ? ctx.get(n) : null;
         if (ctx == null)
         {
            return null;
         }
      }
      return ctx;
   }

   public void init()
   {
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

   public void loadNodes(Node parent)
   {
      Node updated = getNode(parent.getNodePath());
      parent.getChildren().clear();
      for (Node c : updated.getChildren())
      {
         parent.addChild(new Node(c));
      }
      NodeAccessor.setNodesLoaded(parent, updated.isChildrenLoaded());
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

      for (Node n : navigation.getChildren())
      {
         NodeContext<NodeContext<?>> nodeCtx = getNodeContext(n.getNodePath());
         updateNodeContext(n, nodeCtx, rootNodeCtx);
      }

      save(rootNodeCtx);

      for (Node n : navigation.getChildren())
      {
         saveLabel(n);
      }
   }

   public void saveNode(Node node)
   {
      NodeContext<NodeContext<?>> nodeCtx = null;
      NodeContext<NodeContext<?>> parentNodeCtx = null;

      if (node.getParent() == null)
      {
         nodeCtx = rootNodeCtx;
      }
      else
      {
         nodeCtx = getNodeContext(node.getNodePath());
         parentNodeCtx = getNodeContext(node.getParent().getNodePath());
      }

      updateNodeContext(node, nodeCtx, parentNodeCtx);

      save(nodeCtx);
      saveLabel(node);
   }

   private void save(NodeContext<NodeContext<?>> nodeCtx)
   {
      try
      {
         service.saveNode(nodeCtx, null);
      }
      catch (NavigationServiceException e)
      {
         throw new ApiException("Failed to save node", e);
      }
   }

   private void saveLabel(Node node)
   {
      NodeContext<NodeContext<?>> nodeCtx = getNodeContext(node.getNodePath());
      if (node.getLabel() != null && node.getLabel().isLocalized())
      {
         if (!node.getLabel().equals(getLabel(nodeCtx)))
         {
            Map<Locale, State> descriptions = ObjectFactory.createDescriptions(node.getLabel());
            descriptionService.setDescriptions(nodeCtx.getId(), descriptions);
         }
      }

      for (Node c : node.getChildren())
      {
         saveLabel(c);
      }
   }

   public void setScope(Navigation navigation)
   {
      this.scope = new LoadedNodeScope(navigation);
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

   private NodeContext<NodeContext<?>> updateNodeContext(Node node, NodeContext<NodeContext<?>> nodeCtx,
         NodeContext<NodeContext<?>> parentNodeCtx)
   {
      boolean create = nodeCtx == null;

      if (create)
      {
         nodeCtx = parentNodeCtx.add(node.getParent().getChildren().indexOf(node), node.getName());
         nodeCtx.setState(ObjectFactory.createNodeState(node));

         for (Node c : node.getChildren())
         {
            updateNodeContext(c, null, nodeCtx);
         }
      }
      else
      {
         NodeContext<NodeContext<?>> originalCtx = stateMap.get(node);
         if (originalCtx == null)
         {
            throw new IllegalArgumentException("Node not associated with portal");
         }

         if (node.getParent() != null)
         {
            // TODO Do we merge state or simply overwrite? Merging is difficult as it could already have changed
            NodeState nodeState = ObjectFactory.createNodeState(node);
            if (!nodeState.equals(nodeCtx.getState()))
            {
               nodeCtx.setState(nodeState);
            }
         }

         if (node.isChildrenLoaded())
         {
            List<Diff> d = new LinkedList<Diff>();

            for (int i = 0; i < node.getChildren().size(); i++)
            {
               Node child = node.getChildren().get(i);
               NodeContext<NodeContext<?>> orgChildCtx = originalCtx.get(child.getName());

               if (orgChildCtx == null)
               {
                  d.add(new Diff(i, child, DiffOp.ADD));
               }
               else if (orgChildCtx.getIndex() != i)
               {
                  d.add(new Diff(i, child, DiffOp.MOVE));
               }
            }

            for (NodeContext<?> c : originalCtx.getNodes())
            {
               if (node.getChild(c.getName()) == null)
               {
                  d.add(new Diff(originalCtx.getIndex(), null, DiffOp.REMOVE));
               }
            }

            Collections.sort(d, new DiffComparator());

            for (Diff o : d)
            {
               NodeContext<NodeContext<?>> c = nodeCtx.get(o.node.getName());
               switch (o.operation)
               {
                  case REMOVE:
                     if (c != null)
                     {
                        c.remove();
                     }
                     break;
                  case ADD:
                     updateNodeContext(o.node, c, nodeCtx);
                     break;
                  case MOVE:
                     c.remove();
                     nodeCtx.add(o.index, c);
                     updateNodeContext(o.node, c, nodeCtx);
                     break;
               }
            }
         }
      }

      return nodeCtx;
   }

   class Diff
   {
      private int index;
      private Node node;
      private DiffOp operation;

      public Diff(int index, Node data, DiffOp operation)
      {
         this.index = index;
         this.node = data;
         this.operation = operation;
      }
   }

   enum DiffOp
   {
      REMOVE, ADD, MOVE;
   }

   class DiffComparator implements Comparator<Diff>
   {
      @Override
      public int compare(Diff o1, Diff o2)
      {
         return Integer.compare(o1.index, o2.index);
      }
   }
}
