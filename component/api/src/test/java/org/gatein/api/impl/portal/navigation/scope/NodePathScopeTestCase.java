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

import org.exoplatform.portal.mop.navigation.VisitMode;
import org.exoplatform.portal.mop.navigation.Scope.Visitor;
import org.gatein.api.impl.portal.navigation.scope.NodePathScope;
import org.gatein.api.portal.navigation.NodePath;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class NodePathScopeTestCase extends TestCase
{
   public void testNodePathScope()
   {
      Visitor visitor = new NodePathScope(new NodePath("default", "1", "1-1", "1-1-1")).get();

      assertEquals(VisitMode.ALL_CHILDREN, visitor.enter(0, null, "default", null));

      assertEquals(VisitMode.ALL_CHILDREN, visitor.enter(1, null, "1", null));
      assertEquals(VisitMode.NO_CHILDREN, visitor.enter(1, null, "2", null));

      assertEquals(VisitMode.ALL_CHILDREN, visitor.enter(2, null, "1-1", null));
      assertEquals(VisitMode.NO_CHILDREN, visitor.enter(2, null, "1-2", null));

      assertEquals(VisitMode.ALL_CHILDREN, visitor.enter(3, null, "1-1-1", null));
      assertEquals(VisitMode.NO_CHILDREN, visitor.enter(3, null, "1-1-2", null));

      assertEquals(VisitMode.NO_CHILDREN, visitor.enter(4, null, "1-1-1-1", null));
   }
}
