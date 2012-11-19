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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Described.State;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NavigationServiceException;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.gatein.api.ApiException;
import org.gatein.api.impl.Util;
import org.gatein.api.impl.portal.navigation.ListDiff.Diff;
import org.gatein.api.impl.portal.navigation.scope.NodeVisitorScope;
import org.gatein.api.impl.portal.navigation.visitor.CombinedNodeVisitor;
import org.gatein.api.impl.portal.navigation.visitor.SaveNodeVisitor;
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
   private final SiteId siteId;

   private final SiteKey siteKey;

   public NavigationImpl(SiteId siteId, NavigationService navigationService, DescriptionService descriptionService)
   {
      this.siteId = siteId;
      this.navigationService = navigationService;
      this.descriptionService = descriptionService;

      siteKey = Util.from(siteId);
   }

   private Node convertNode(NodeContext<NodeContext<?>> ctx)
   {
      Node node;
      if (ctx.getParent() == null)
      {
         node = new RootNode(siteId);
      }
      else
      {
         node = ObjectFactory.createNode(ctx.getName(), ctx.getState());
         node.setLabel(getLabel(ctx));
      }

      if (ctx.isExpanded())
      {
         for (NodeContext<?> c : ctx.getNodes())
         {
            @SuppressWarnings("unchecked")
            Node n = convertNode((NodeContext<NodeContext<?>>) c);
            node.addChild(n);
         }

         ((NodeList) node.getChildren()).setLoaded(true);
      }

      return node;
   }

   private Node findNode(Node root, NodePath path)
   {
      Node n = root;
      for (String e : path)
      {
         n = n.getChild(e);
         if (n == null)
         {
            return null;
         }
      }
      return n;
   }

   private NodeContext<NodeContext<?>> findNodeContext(NodeContext<NodeContext<?>> ctx, NodePath path)
   {
      NodeContext<NodeContext<?>> n = ctx;
      for (String e : path)
      {
         n = n.get(e);
         if (n == null)
         {
            return null;
         }
      }
      return n;
   }

   private Label getLabel(NodeContext<NodeContext<?>> ctx)
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
      return navigationService.loadNavigation(siteKey);
   }

   @Override
   public Node getNode(NodePath path, NodeVisitor visitor)
   {
      Node node = loadNodes(Nodes.visitNodes(path, visitor));
      findNode(node, path);
      return null;
   }

   private NodeContext<NodeContext<?>> getNodeContext(NodeVisitor visitor)
   {
      NavigationContext ctx = getNavigationContext();
      if (ctx == null)
      {
         return null;
      }
      return navigationService.loadNode(NodeModel.SELF_MODEL, ctx, new NodeVisitorScope(visitor), null);
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
      Node node = loadNodes(Nodes.visitNodes(parent.getNodePath(), Nodes.visitChildren()));

      Node p = findNode(node, parent.getNodePath());
      ArrayList<Node> children = new ArrayList<Node>(p.getChildren());
      p.getChildren().clear();

      parent.getChildren().addAll(children);
      ((NodeList) parent.getChildren()).setLoaded(true);
   }

   @Override
   public Node loadNodes(NodeVisitor visitor)
   {
      NodeContext<NodeContext<?>> ctx = getNodeContext(visitor);
      return convertNode(ctx);
   }

   @Override
   public void moveNode(NodePath from, NodePath to)
   {
      CombinedNodeVisitor visitor = new CombinedNodeVisitor(Nodes.visitNodes(from), Nodes.visitNodes(to));
      NodeContext<NodeContext<?>> ctx = getNodeContext(visitor);

      NodeContext<NodeContext<?>> src = findNodeContext(ctx, from);
      NodeContext<NodeContext<?>> dstParent = findNodeContext(ctx, to.parent());

      if (src == null)
      {
         throw new ApiException("Node '" + from + "' not found");
      }

      if (dstParent == null)
      {
         throw new ApiException("Parent '" + to.parent() + "' not found");
      }
      
      if (dstParent.get(to.getLastSegment()) != null)
      {
         throw new ApiException("Destination '" + to + "' already exists");
      }
      
      dstParent.add(null, src);
      save(ctx);
   }

   @Override
   public boolean removeNode(NodePath path)
   {
      NodeContext<NodeContext<?>> ctx = getNodeContext(Nodes.visitNodes(path));
      ctx = findNodeContext(ctx, path);
      if (ctx == null)
      {
         return false;
      }

      ctx.remove();
      save(ctx);
      return true;
   }

   @Override
   public void saveNode(Node node)
   {
      NodeContext<NodeContext<?>> rootCtx = getNodeContext(new SaveNodeVisitor(node));
      NodeContext<NodeContext<?>> parentCtx;
      NodeContext<NodeContext<?>> ctx;

      if (node.isRoot())
      {
         parentCtx = null;
         ctx = rootCtx;
      }
      else
      {
         parentCtx = findNodeContext(rootCtx, node.getParent().getNodePath());
         ctx = parentCtx.get(node.getName());
      }

      ctx = updateNodeContext(node, ctx, parentCtx);
      save(ctx);

      saveLabel(node, ctx);
   }

   private void save(NodeContext<NodeContext<?>> nodeCtx)
   {
      try
      {
         navigationService.saveNode(nodeCtx, null);
      }
      catch (NavigationServiceException e)
      {
         throw new ApiException("Failed to save node", e);
      }
   }

   private void saveLabel(Node node, NodeContext<NodeContext<?>> ctx)
   {
      if (node.getLabel() != null && node.getLabel().isLocalized())
      {
         if (!node.getLabel().equals(getLabel(ctx)))
         {
            Map<Locale, State> descriptions = ObjectFactory.createDescriptions(node.getLabel());
            descriptionService.setDescriptions(ctx.getId(), descriptions);
         }
      }

      for (Node c : node.getChildren())
      {
         saveLabel(c, ctx.get(c.getName()));
      }
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

   private NodeContext<NodeContext<?>> updateNodeContext(Node node, NodeContext<NodeContext<?>> ctx,
         NodeContext<NodeContext<?>> parentCtx)
   {
      if (ctx == null)
      {
         ctx = parentCtx.add(node.getParent().getChildren().indexOf(node), node.getName());
         ctx.setState(ObjectFactory.createNodeState(node));

         for (Node c : node.getChildren())
         {
            updateNodeContext(c, null, ctx);
         }
      }
      else
      {
         if (!node.isRoot())
         {
            NodeState nodeState = ObjectFactory.createNodeState(node);
            if (!nodeState.equals(ctx.getState()))
            {
               ctx.setState(nodeState);
            }
         }

         NodeList current = (NodeList) node.getChildren();
         NodeList original = current.getOriginal();
         if (original != null)
         {
            List<Diff> diff = ListDiff.compare(current, original);

            for (Diff d : diff)
            {
               NodeContext<NodeContext<?>> c = ctx.get(d.getNode().getName());
               switch (d.getOperation())
               {
                  case REMOVE:
                     if (c != null)
                     {
                        c.remove();
                     }
                     break;
                  case ADD:
                     updateNodeContext(d.getNode(), c, ctx);
                     break;
                  case MOVE:
                     ctx.add(d.getIndex(), c);
                     updateNodeContext(d.getNode(), c, ctx);
                     break;
                  case UNCHANGED:
                     updateNodeContext(d.getNode(), c, ctx);
                     break;
               }
            }
         }
      }

      return ctx;
   }
}
