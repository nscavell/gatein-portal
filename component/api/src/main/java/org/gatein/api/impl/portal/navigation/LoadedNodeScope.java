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

import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.navigation.VisitMode;
import org.gatein.api.portal.navigation.Node;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class LoadedNodeScope implements Scope
{
   private final NodePathVisitor visitor;
   private List<Node> nodes;

   public LoadedNodeScope()
   {
      this.nodes = nodes;
      visitor = new NodePathVisitor();
   }

   @Override
   public Visitor get()
   {
      return visitor;
   }

   public static class NodePathVisitor implements Visitor
   {
      public NodePathVisitor()
      {
      }

      @Override
      public VisitMode enter(int depth, String id, String name, NodeState state)
      {
         return VisitMode.ALL_CHILDREN;
      }

      @Override
      public void leave(int depth, String id, String name, NodeState state)
      {
      }
   }
}
