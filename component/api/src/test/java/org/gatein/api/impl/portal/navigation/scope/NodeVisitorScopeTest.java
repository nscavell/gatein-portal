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

import junit.framework.TestCase;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.VisitMode;
import org.exoplatform.portal.mop.navigation.Scope.Visitor;
import org.exoplatform.portal.mop.page.PageKey;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodeVisitor;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class NodeVisitorScopeTest extends TestCase
{
   public void testNodePathScope()
   {
      NodeState nodeState = new NodeState("label", "icon", -1, -1, Visibility.DISPLAYED, new PageKey(new SiteKey(SiteType.PORTAL, "site"), "page"));
      NodeVisitorMock mock = new NodeVisitorMock();
      Visitor visitor = new NodeVisitorScope(mock).get();
      
      mock.instrument(true);
      assertEquals(VisitMode.ALL_CHILDREN, visitor.enter(0, "id", "default", nodeState));
      assertNotNull(mock.node);
      assertNull(mock.node.getParent());

      mock.instrument(true);
      assertEquals(VisitMode.ALL_CHILDREN, visitor.enter(1, "id", "1", nodeState));
      assertNotNull(mock.node);
      assertNotNull(mock.node.getParent());
      assertNotNull(mock.node.getChildren());
      assertFalse(mock.node.isChildrenLoaded());
      assertEquals(1, mock.depth);

      mock.instrument(false);
      assertEquals(VisitMode.NO_CHILDREN, visitor.enter(2, "id", "1-1", nodeState));
      assertNotNull(mock.node);
      assertNotNull(mock.node.getParent());
      assertNotNull(mock.node.getChildren());
      assertFalse(mock.node.isChildrenLoaded());
      assertEquals(2, mock.depth);
      
      visitor.leave(2, "id", "1-1", nodeState);
      visitor.leave(1, "id", "1", nodeState);
      visitor.leave(0, "id", "default", null);

      mock.instrument(false);
   }

   class NodeVisitorMock implements NodeVisitor
   {
      private int depth;

      private Node node;

      private boolean visit;

      @Override
      public boolean visit(int depth, Node node)
      {
         this.depth = depth;
         this.node = node;
         return visit;
      }

      public void instrument(boolean visit)
      {
         this.visit = visit;
         depth = -1;
         node = null;
      }
   }
}
