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

import org.exoplatform.portal.mop.navigation.NodeContextAccessor;
import org.gatein.api.portal.LocalizedString;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.portal.navigation.PublicationDate;
import org.gatein.api.portal.navigation.Visibility;
import org.gatein.api.portal.navigation.Visibility.Flag;
import org.gatein.api.portal.page.PageId;
import org.gatein.api.portal.site.SiteId;
import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ApiNodeTest
{
   public static void assertIterator(Iterator<Node> itr, String... expected)
   {
      for (String e : expected)
      {
         assertTrue(itr.hasNext());
         assertEquals(e, itr.next().getName());
      }
      assertFalse(itr.hasNext());
   }

   public static void assertVisibility(boolean expectedVisible, Flag expectedFlag, PublicationDate expectedDate, Node actualNode)
   {
      assertEquals(expectedVisible, actualNode.isVisible());
      assertEquals(expectedFlag, actualNode.getVisibility().getFlag());
      assertEquals(expectedDate, actualNode.getVisibility().getPublicationDate());
   }

   public static ApiNode createRoot(boolean expanded)
   {
      return NodeContextAccessor.createRootNodeContext(new ApiNodeModel(new SiteId("classic")), expanded).getNode();
   }

   ApiNode root;

   @Test
   public void addChild()
   {
      root.addChild("child");
   }

   @Test
   public void addChild_ChildToNewChild()
   {
      root.addChild("child").addChild("child");
   }

   @Test(expected = IllegalArgumentException.class)
   public void addChild_Existing()
   {
      root.addChild("child");
      root.addChild("child");
   }

   @Test(expected = IllegalStateException.class)
   public void addChild_NotExpanded()
   {
      root = createRoot(false);
      root.addChild("child");
   }

   @Test(expected = NullPointerException.class)
   public void addChild_NullName()
   {
      root.addChild(null);
   }

   @Before
   public void before() throws Exception
   {
      root = createRoot(true);
   }

   @Test
   public void getDescendant()
   {
      root.addChild("child0").addChild("child0-0");
      root.addChild("child1");

      assertTrue(root.getDescendant(NodePath.root()).isRoot());
      assertEquals("child0", root.getDescendant(NodePath.path("child0")).getName());
      assertEquals("child0-0", root.getDescendant(NodePath.path("child0", "child0-0")).getName());
      assertNull(root.getDescendant(NodePath.path("child1", "child0-0")));
   }

   @Test
   public void getNodePath()
   {
      root.addChild("child0").addChild("child0-0");

      assertEquals("/child0", root.getChild("child0").getNodePath().toString());
      assertEquals("/child0/child0-0", root.getChild("child0").getChild("child0-0").getNodePath().toString());
   }

   @Test
   public void iconName()
   {
      Node c = root.addChild("child");
      assertNull(c.getIconName());
      c.setIconName("iconName");
      assertEquals("iconName", c.getIconName());
   }

   @Test
   public void isRoot()
   {
      assertTrue(root.isRoot());
      assertFalse(root.addChild("child").isRoot());
   }

   @Test
   public void iterator()
   {
      root.addChild("child0");
      root.addChild("child1");
      root.addChild("child2");

      assertIterator(root.iterator(), "child0", "child1", "child2");
   }

   @Test(expected = NoSuchElementException.class)
   public void iterator_NoSuchElement()
   {
      root.iterator().next();
   }

   @Test
   public void iterator_Remove()
   {
      root.addChild("child0");
      root.addChild("child1");
      root.addChild("child2");

      Iterator<Node> itr = root.iterator();
      itr.next();
      itr.next();
      itr.remove();

      assertIterator(root.iterator(), "child0", "child2");
   }

   @Test(expected = IllegalStateException.class)
   public void iterator_RemoveIllegalState()
   {
      root.iterator().remove();
   }

   @Test
   public void moveTo()
   {
      root.addChild("0");
      root.addChild("1");
      root.addChild("2");

      root.getChild(0).moveTo(2);
      assertIterator(root.iterator(), "1", "2", "0");

      root.getChild(2).moveTo(0);
      assertIterator(root.iterator(), "0", "1", "2");

      root.getChild(0).moveTo(1);
      assertIterator(root.iterator(), "1", "0", "2");

      root.getChild(1).moveTo(0);
      assertIterator(root.iterator(), "0", "1", "2");
   }

   @Test
   public void moveTo_Parent()
   {
      Node parent0 = root.addChild("parent0");
      Node child = parent0.addChild("0");

      Node parent1 = root.addChild("parent1");

      child.moveTo(parent1);

      assertEquals(0, parent0.getChildCount());
      assertEquals(1, parent1.getChildCount());
   }

   @Test
   public void moveTo_ParentAtIndex()
   {
      Node parent0 = root.addChild("parent0");
      Node child = parent0.addChild("1");

      Node parent1 = root.addChild("parent1");
      parent1.addChild("0");
      parent1.addChild("2");

      child.moveTo(1, parent1);

      assertEquals(0, parent0.getChildCount());
      assertEquals(3, parent1.getChildCount());
      assertEquals("1", parent1.getChild(1).getName());
   }

   @Test
   public void name()
   {
      Node c = root.addChild("child");
      assertEquals("child", c.getName());
   }

   @Test
   public void pageId()
   {
      Node c = root.addChild("child");
      assertNull(c.getPageId());

      c.setPageId(new PageId("classic", "page"));
      assertEquals(new PageId("classic", "page"), c.getPageId());
   }

   @Test
   public void root_getName()
   {
      assertNull(root.getName());
   }

   @Test
   public void root_getNodePath()
   {
      assertEquals("/", root.getNodePath().toString());
   }

   @Test
   public void root_getParent()
   {
      assertNull(root.getParent());
   }

   @Test
   public void root_isRoot()
   {
      assertTrue(root.isRoot());
   }

   @Test(expected = UnsupportedOperationException.class)
   public void root_moveTo()
   {
      root.moveTo(0);
   }

   @Test(expected = UnsupportedOperationException.class)
   public void root_moveToParent()
   {
      root.moveTo(createRoot(true));
   }

   @Test(expected = UnsupportedOperationException.class)
   public void root_moveToParentIndex()
   {
      root.moveTo(0, createRoot(true));
   }

   @Test(expected = UnsupportedOperationException.class)
   public void root_setIconName()
   {
      root.setIconName("iconName");
   }

   @Test(expected = UnsupportedOperationException.class)
   public void root_setDisplayName()
   {
      root.setDisplayName("label");
   }

   @Test(expected = UnsupportedOperationException.class)
   public void root_setPageId()
   {
      root.setPageId(new PageId("siteName", "pageName"));
   }

   @Test(expected = UnsupportedOperationException.class)
   public void root_setVisibility()
   {
      root.setVisibility(true);
   }

   @Test(expected = UnsupportedOperationException.class)
   public void root_setVisibilityPublicationDate()
   {
      root.setVisibility(PublicationDate.startingOn(new Date()));
   }

   @Test(expected = UnsupportedOperationException.class)
   public void root_setVisibilityVisibility()
   {
      root.setVisibility(new Visibility());
   }

   @Test
   public void sort()
   {
      root.addChild("2");
      root.addChild("1");
      root.addChild("0");

      root.sort(new Comparator<Node>()
      {
         @Override
         public int compare(Node o1, Node o2)
         {
            return o1.getName().compareTo(o2.getName());
         }
      });

      assertEquals(3, root.getChildCount());
      assertEquals("0", root.getChild(0).getName());
      assertEquals("1", root.getChild(1).getName());
      assertEquals("2", root.getChild(2).getName());
   }

   @Test
   public void visibility()
   {
      Node c = root.addChild("child");
      assertVisibility(true, Flag.VISIBLE, null, c);

      c.setVisibility(false);
      assertVisibility(false, Flag.HIDDEN, null, c);

      PublicationDate d = PublicationDate.between(new Date(), new Date(System.currentTimeMillis() + 60000));
      c.setVisibility(d);
      assertVisibility(true, Flag.PUBLICATION, d, c);

      c.setVisibility(new Visibility(Flag.SYSTEM));
      assertVisibility(true, Flag.SYSTEM, null, c);
   }
}
