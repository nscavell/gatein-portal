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

import static org.gatein.api.impl.portal.navigation.ApiNodeTest.assertIterator;
import static org.gatein.api.impl.portal.navigation.ApiNodeTest.createRoot;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.util.Filter;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class FilteredNodeTest
{
   private Filter<Node> filter;
   private Node filtered;
   private ApiNode root;

   @Test
   public void addChild()
   {
      filtered.addChild("child5");

      assertIterator(filtered.iterator(), "child0", "child2", "child4", "child5");
      assertIterator(root.iterator(), "child0", "child1", "child2", "child3", "child4", "child5");
   }

   @Test
   public void addChild_IndexFirst()
   {
      filtered.addChild(0, "child5");

      assertIterator(filtered.iterator(), "child5", "child0", "child2", "child4");
      assertIterator(root.iterator(), "child5", "child0", "child1", "child2", "child3", "child4");
   }

   @Test
   public void addChild_IndexLast()
   {
      filtered.addChild(3, "child5");

      assertIterator(filtered.iterator(), "child0", "child2", "child4", "child5");
      assertIterator(root.iterator(), "child0", "child1", "child2", "child3", "child4", "child5");
   }

   @Test
   public void addChild_IndexMiddle()
   {
      filtered.addChild(1, "child5");

      assertIterator(filtered.iterator(), "child0", "child5", "child2", "child4");
      assertIterator(root.iterator(), "child0", "child1", "child5", "child2", "child3", "child4");
   }

   @Test(expected = IndexOutOfBoundsException.class)
   public void addChild_IndexOutOfBounds()
   {
      filtered.addChild(4, "child5");
   }

   @Before
   public void before() throws Exception
   {
      root = createRoot(true);
      root.addChild("child0");
      root.addChild("child1");
      root.addChild("child2");
      root.addChild("child3");
      root.addChild("child4");

      filter = new Filter<Node>()
      {
         @Override
         public boolean accept(Node object)
         {
            return !(object.getName().equals("child1") || object.getName().equals("child3"));
         }
      };

      filtered = root.filter(filter);
   }

   @Test
   public void getChild()
   {
      assertNotNull(filtered.getChild("child0"));
      assertNull(filtered.getChild("child1"));
   }

   @Test
   public void getChild_Index()
   {
      assertEquals("child0", filtered.getChild(0).getName());
      assertEquals("child2", filtered.getChild(1).getName());
   }

   @Test(expected = IndexOutOfBoundsException.class)
   public void getChild_IndexOutOfBounds()
   {
      filtered.getChild(4);
   }

   @Test
   public void getChildCount()
   {
      assertEquals(3, filtered.getChildCount());
   }

   @Test
   public void getDescendant()
   {
      root.getChild("child0").addChild("child0-0");
      root.getChild("child1").addChild("child1-0");

      assertNotNull(filtered.getNode(NodePath.path("child0", "child0-0")));
      assertNull(filtered.getNode(NodePath.path("child1")));
      assertNull(filtered.getNode(NodePath.path("child1", "child0-0")));
   }

   @Test
   public void indexOf()
   {
      assertEquals(0, filtered.indexOf("child0"));
      assertEquals(1, filtered.indexOf("child2"));
   }

   @Test
   public void iterator()
   {
      assertIterator(filtered.iterator(), "child0", "child2", "child4");
   }

   @Test
   public void moveTo_First()
   {
      filtered.getChild("child4").moveTo(0);

      assertIterator(filtered.iterator(), "child4", "child0", "child2");
      assertIterator(root.iterator(), "child4", "child0", "child1", "child2", "child3");
   }

   @Test
   public void moveTo_Last()
   {
      filtered.getChild("child4").moveTo(2);

      assertIterator(filtered.iterator(), "child0", "child2", "child4");
      assertIterator(root.iterator(), "child0", "child1", "child2", "child3", "child4");
   }

   @Test
   public void moveTo_Middle()
   {
      filtered.getChild("child4").moveTo(1);

      assertIterator(filtered.iterator(), "child0", "child4", "child2");
      assertIterator(root.iterator(), "child0", "child1", "child4", "child2", "child3");
   }

   @Test(expected = IndexOutOfBoundsException.class)
   public void moveTo_OutOfBounds()
   {
      filtered.getChild("child4").moveTo(3);
   }

   @Test
   public void moveTo_Parent()
   {
      Node parent0 = root.addChild("parent0");
      parent0.addChild("child0");
      parent0.addChild("child1");
      parent0.addChild("child2");

      filtered.addChild("parent1").addChild("child5").moveTo(1, parent0);

      assertIterator(filtered.getChild("parent0").iterator(), "child0", "child5", "child2");
      assertIterator(root.getChild("parent0").iterator(), "child0", "child1", "child5", "child2");
   }

   @Test
   public void sourceNotChanged()
   {
      root.filter(filter);

      assertEquals(5, root.getChildCount());
      assertNotNull(root.getChild("child1"));
   }
}
