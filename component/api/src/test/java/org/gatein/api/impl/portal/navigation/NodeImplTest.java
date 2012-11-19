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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;

import org.gatein.api.impl.portal.navigation.NodeImpl;
import org.gatein.api.portal.navigation.Node;
import org.junit.Test;


/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class NodeImplTest
{
   //TODO: More tests

   @Test(expected = IllegalArgumentException.class)
   public void test_NullName()
   {
      new NodeImpl((String) null);
   }

   @Test(expected = NullPointerException.class)
   public void testNewNode_NullNode()
   {
      new NodeImpl((Node) null);
   }

   @Test
   public void testEquals()
   {
      Node foo = new NodeImpl("foo");
      Node foo2 = new NodeImpl("foo");
      Node bar = new NodeImpl("bar");

      assertFalse(foo.equals(bar));
      assertTrue(foo.equals(foo2));
   }

   @Test
   public void testCopy()
   {
      Node original = new NodeImpl("foo");
      Node copy = original.copy();
      assertEquals(original, copy);
   }

   @Test
   public void testCopy_Rename()
   {
      Node original = new NodeImpl("foo");
      original.addChild("1");
      original.addChild("2");

      Node copy = original.copy("bar");
      assertEquals("bar", copy.getName());
      for (int i=0; i<copy.getChildren().size(); i++)
      {
         assertEquals(original.getChild(i), copy.getChild(i));
      }
   }

   @Test
   public void testAdd()
   {
      Node parent = new NodeImpl("root");
      Node child = new NodeImpl("child");
      parent.getChildren().add(child);

      assertEquals(1, parent.getChildren().size());
      assertTrue(parent == child.getParent());
   }

   @Test(expected = NullPointerException.class)
   public void testAdd_NullChild()
   {
      Node parent = new NodeImpl("parent");
      parent.getChildren().add(null);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testAdd_NullChildName()
   {
      Node parent = new NodeImpl("parent");
      parent.addChild(null);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testAdd_SameChild()
   {
      Node parent = new NodeImpl("parent");
      parent.addChild("child");
      parent.addChild("child");
   }

   @Test(expected = IllegalArgumentException.class)
   public void testAdd_ChildWithParent()
   {
      Node parent = new NodeImpl("parent");
      Node parent2 = new NodeImpl("parent2");
      Node child = new NodeImpl("child");
      parent2.getChildren().add(child);

      parent.getChildren().add(child);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testAdd_Self()
   {
      Node node = new NodeImpl("node");
      node.getChildren().add(node);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testAdd_Cyclic()
   {
      Node parent = new NodeImpl("parent");
      Node child1 = new NodeImpl("child1");
      parent.getChildren().add(child1);
      child1.getChildren().add(parent);
   }

   @Test
   public void testSort()
   {
      Node node = new NodeImpl("parent");
      node.addChild("3");
      node.addChild("2");
      node.addChild("1");

      node.sort(new Comparator<Node>()
      {
         @Override
         public int compare(Node o1, Node o2)
         {
            return o1.getName().compareTo(o2.getName());
         }
      });

      assertEquals(3, node.getChildren().size());
      assertEquals("1", node.getChildren().get(0).getName());
      assertEquals("2", node.getChildren().get(1).getName());
      assertEquals("3", node.getChildren().get(2).getName());
   }
}
