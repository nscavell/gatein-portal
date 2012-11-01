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

import org.exoplatform.portal.mop.navigation.Scope.Visitor;
import org.exoplatform.portal.mop.navigation.VisitMode;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodeAccessor;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class LoadedNodeScopeTestCase extends TestCase
{
   public void testLoadedNodeScope() throws IllegalArgumentException, IllegalAccessException
   {
      Node r = new Node("default");

      Node p = new Node("1");
      NodeAccessor.setNodesLoaded(p, true);
      r.addNode(p);

      p.addNode(new Node("1-1"));
      NodeAccessor.setNodesLoaded(p.getNode("1-1"), true);
      p.getNode("1-1").addNode(new Node("1-1-1"));
      p.getNode("1-1").addNode(new Node("1-1-2"));

      p.addNode(new Node("1-2"));

      Visitor visitor = new LoadedNodeScope(r).get();

      assertEquals(VisitMode.ALL_CHILDREN, visitor.enter(0, null, "default", null));
      assertEquals(VisitMode.ALL_CHILDREN, visitor.enter(1, null, "1", null));
      assertEquals(VisitMode.ALL_CHILDREN, visitor.enter(2, null, "1-1", null));
      assertEquals(VisitMode.NO_CHILDREN, visitor.enter(3, null, "1-1-1", null));
      visitor.leave(3, null, "1-1-1", null);
      assertEquals(VisitMode.NO_CHILDREN, visitor.enter(3, null, "1-1-2", null));
      visitor.leave(3, null, "1-1-2", null);
      visitor.leave(2, null, "1-1", null);
      assertEquals(VisitMode.NO_CHILDREN, visitor.enter(2, null, "1-2", null));
      visitor.leave(2, null, "1-2", null);
      assertEquals(VisitMode.NO_CHILDREN, visitor.enter(2, null, "1-3", null));
      visitor.leave(2, null, "1-3", null);
      visitor.leave(1, null, "1", null);
      visitor.leave(0, null, "default", null);
   }
}
