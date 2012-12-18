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

import org.exoplatform.portal.mop.navigation.NodeChange;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.portal.navigation.NodeVisitor;
import org.gatein.api.portal.navigation.Nodes;

import java.io.Serializable;

/**
 * Used to store uncommitted node changes during serialization, derived from {@link org.exoplatform.portal.mop.navigation.NodeChange}.
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
abstract class ApiNodeChange implements Serializable
{
   protected final NodePath target;

   protected ApiNodeChange(NodeContext<ApiNode> target)
   {
      this.target = target.getNode().getNodePath();
   }

   public abstract void apply(ApiNode root);

   public NodePath getTarget()
   {
      return target;
   }

   protected NodePath getNodePath(NodeContext<ApiNode> context)
   {
      ApiNode node = (context == null) ? null : context.getNode();
      return (node == null) ? null : node.getNodePath();
   }

   protected static ApiNode getNode(ApiNode root, NodePath path)
   {
      ApiNode node = (ApiNode) root.getNode(path);
      if (node == null)
      {
         NodeVisitor visitor = Nodes.visitNodes(path);
         root.navigation.refreshNode(root, visitor);
         node = (ApiNode) root.getNode(path);
      }

      return node;
   }

   public static class Created extends ApiNodeChange
   {
      private final NodePath parent;
      private final NodePath previous;
      private final String name;

      public Created(NodeChange.Created<NodeContext<ApiNode>> created)
      {
         super(created.getTarget());
         this.parent = getNodePath(created.getParent());
         this.previous = getNodePath(created.getPrevious());
         this.name = created.getName();
      }

      @Override
      public void apply(ApiNode root)
      {
         ApiNode node = getNode(root, parent);
         if (!node.isChildrenLoaded())
         {
            node.navigation.refreshNode(node, Nodes.visitChildren());
         }
         int index = (previous == null) ? 0 : node.indexOf(previous.getLastSegment());
         if (index < 0)
         {
            node.addChild(name);
         }
         else
         {
            node.addChild(index, name);
         }
      }

      public NodePath getParent()
      {
         return parent;
      }

      public NodePath getPrevious()
      {
         return previous;
      }

      public String getName()
      {
         return name;
      }
   }

   public static class Destroyed extends ApiNodeChange
   {
      private final NodePath parent;

      protected Destroyed(NodeChange.Destroyed<NodeContext<ApiNode>> destroyed)
      {
         super(destroyed.getTarget());
         this.parent = getNodePath(destroyed.getParent());
      }

      @Override
      public void apply(ApiNode root)
      {
         Node node = getNode(root, parent);
         node.removeChild(parent.getLastSegment());
      }

      public NodePath getParent()
      {
         return parent;
      }
   }

   public static class Renamed extends ApiNodeChange
   {
      private final NodePath parent;
      private final String name;

      public Renamed(NodeChange.Renamed<NodeContext<ApiNode>> renamed)
      {
         super(renamed.getTarget());
         this.parent = getNodePath(renamed.getParent());
         this.name = renamed.getName();
      }

      @Override
      public void apply(ApiNode root)
      {
         ApiNode node = getNode(root, parent);
         node.context.setName(name);
      }

      public NodePath getParent()
      {
         return parent;
      }

      public String getName()
      {
         return name;
      }
   }

   public static class Moved extends ApiNodeChange
   {
      private final NodePath from;
      private final NodePath to;
      private final NodePath previous;

      public Moved(NodeChange.Moved<NodeContext<ApiNode>> moved)
      {
         super(moved.getTarget());
         this.from = getNodePath(moved.getFrom());
         this.to = getNodePath(moved.getTo());
         this.previous = getNodePath(moved.getPrevious());
      }

      @Override
      public void apply(ApiNode root)
      {
         ApiNode fromNode = getNode(root, from);
         ApiNode toNode = getNode(root, to);

         int index = (previous == null) ? 0 : toNode.indexOf(previous.getLastSegment());
         if (index < 0)
         {
            fromNode.moveTo(toNode);
         }
         else
         {
            fromNode.moveTo(index, toNode);
         }
      }

      public NodePath getFrom()
      {
         return from;
      }

      public NodePath getTo()
      {
         return to;
      }

      public NodePath getPrevious()
      {
         return previous;
      }
   }

   public final static class Updated extends ApiNodeChange
   {
      private final NodeState state;

      public Updated(NodeChange.Updated<NodeContext<ApiNode>> updated)
      {
         super(updated.getTarget());
         this.state = updated.getState();
      }

      @Override
      public void apply(ApiNode root)
      {
         ApiNode node = getNode(root, target);
         node.context.setState(state);
      }

      public NodeState getState()
      {
         return state;
      }
   }
}
