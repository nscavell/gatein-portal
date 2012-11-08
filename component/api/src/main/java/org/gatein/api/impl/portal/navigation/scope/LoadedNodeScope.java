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
package org.gatein.api.impl.portal.navigation.scope;

import java.util.List;
import java.util.Stack;

import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.navigation.VisitMode;
import org.gatein.api.portal.navigation.Navigation;
import org.gatein.api.portal.navigation.Node;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class LoadedNodeScope implements Scope
{
   private final LoadedNodeVisitor visitor;

   public LoadedNodeScope(Node node)
   {
      while (node.getParent() != null)
      {
         node = node.getParent();
      }

      visitor = new LoadedNodeVisitor(node.isChildrenLoaded() ? node.getChildren() : null);
   }

   public LoadedNodeScope(Navigation navigation)
   {
      visitor = new LoadedNodeVisitor(navigation.isChildrenLoaded() ? navigation.getChildren() : null);
   }

   @Override
   public Visitor get()
   {
      return visitor;
   }

   private static class LoadedNodeVisitor implements Visitor
   {
      private final List<Node> nodes;

      private final Stack<Node> stack = new Stack<Node>();

      public LoadedNodeVisitor(List<Node> nodes)
      {
         this.nodes = nodes;
      }

      @Override
      public VisitMode enter(int depth, String id, String name, NodeState state)
      {
         if (depth == 0)
         {
            return VisitMode.ALL_CHILDREN;
         }
         else if (depth == 1)
         {
            for (Node n : nodes)
            {
               if (n.getName().equals(name))
               {
                  stack.add(n);
                  return VisitMode.ALL_CHILDREN;
               }
            }
            return VisitMode.NO_CHILDREN;
         }
         else
         {
            Node n = stack.peek().getChild(name);
            stack.add(n);
            return n != null && n.isChildrenLoaded() ? VisitMode.ALL_CHILDREN : VisitMode.NO_CHILDREN;
         }
      }

      @Override
      public void leave(int depth, String id, String name, NodeState state)
      {
         if (depth > 0)
         {
            stack.pop();
         }
      }
   }
}
