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
package org.gatein.api.impl.portal.navigation.visitor;

import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.impl.NodeList;
import org.gatein.api.portal.navigation.impl.RootNode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SaveNodeVisitorTest
{

   private SaveNodeVisitor visitor;

   @Before
   public void before()
   {
      RootNode r = new RootNode(null);

      r.addChild("1");
      r.getChild("1").addChild("1-1");
      r.getChild("1").getChild("1-1").addChild("1-1-1");
      r.getChild("1").getChild("1-1").getChild("1-1-1").addChild("1-1-1-1");
      r.getChild("1").getChild("1-1").addChild("1-1-2");
      r.getChild("1").getChild("1-1").addChild("1-1-3");
      
      ((NodeList) r.getChild("1").getChild("1-1").getChild("1-1-2").getChildren()).setLoaded(true);

      visitor = new SaveNodeVisitor(r.getChild("1").getChild("1-1"));
   }

   @Test
   public void notOnPath()
   {
      assertFalse(visitor.visit(1, "2", null));
   }

   @Test
   public void onPath()
   {
      assertTrue(visitor.visit(1, "1", null));
      assertTrue(visitor.visit(2, "1/1", null));
   }

   @Test
   public void root()
   {
      assertTrue(visitor.visit(0, null, null));
   }

}

