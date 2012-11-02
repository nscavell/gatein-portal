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

import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.navigation.VisitMode;
import org.gatein.api.portal.navigation.NodePath;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class NodePathScope implements Scope
{
   private final NodePathVisitor nodePathVisitor;

   public NodePathScope(NodePath nodePath)
   {
      nodePathVisitor = new NodePathVisitor(nodePath);
   }

   @Override
   public Visitor get()
   {
      return nodePathVisitor;
   }

   public static class NodePathVisitor implements Visitor
   {
      private final NodePath nodePath;

      public NodePathVisitor(NodePath nodePath)
      {
         this.nodePath = nodePath;
      }

      @Override
      public VisitMode enter(int depth, String id, String name, NodeState state)
      {
         if (depth < nodePath.size() && nodePath.getSegment(depth).equals(name))
         {
            return VisitMode.ALL_CHILDREN;
         }
         else
         {
            return VisitMode.NO_CHILDREN;
         }
      }

      @Override
      public void leave(int depth, String id, String name, NodeState state)
      {
      }
   }
}
