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
package org.gatein.api.impl.portal.navigation.scope;

import java.util.Stack;

import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.navigation.VisitMode;
import org.gatein.api.impl.portal.navigation.ObjectFactory;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodeVisitor;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class NodeVisitorScope implements Scope
{
   private final NodeVisitorWrapper nodePathVisitor;

   public NodeVisitorScope(NodeVisitor nodeVisitor)
   {
      nodePathVisitor = new NodeVisitorWrapper(nodeVisitor);
   }

   @Override
   public Visitor get()
   {
      return nodePathVisitor;
   }

   public static class NodeVisitorWrapper implements Visitor
   {
      private final NodeVisitor nodeVisitor;

      private final Stack<Node> stack = new Stack<Node>();

      public NodeVisitorWrapper(NodeVisitor nodeVisitor)
      {
         this.nodeVisitor = nodeVisitor;
      }

      @Override
      public VisitMode enter(int depth, String id, String name, NodeState state)
      {
         Node node = ObjectFactory.createNode(depth == 0 ? Node.ROOT_NODE_NAME : name, state);

         if (!stack.isEmpty())
         {
            stack.peek().addChild(node);
         }

         stack.add(node);

         return nodeVisitor.visit(depth, node) ? VisitMode.ALL_CHILDREN : VisitMode.NO_CHILDREN;
      }

      @Override
      public void leave(int depth, String id, String name, NodeState state)
      {
         stack.pop();
      }
   }
}
