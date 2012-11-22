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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.exoplatform.portal.mop.navigation.NodeContextAccessor;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.PublicationDate;
import org.gatein.api.portal.navigation.Visibility;
import org.gatein.api.portal.navigation.Visibility.Flag;
import org.gatein.api.portal.page.PageId;
import org.gatein.api.portal.site.SiteId;
import org.junit.Before;
import org.junit.Test;

public class ApiNodeTest
{
   private ApiNode root;

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

   private ApiNode createRoot(boolean expanded)
   {
      return NodeContextAccessor.createRootNodeContext(new ApiNodeModel(new SiteId("classic")), expanded).getNode();
   }

   @Test(expected = NullPointerException.class)
   public void addChild_NullName()
   {
      root.addChild(null);
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
   public void name()
   {
      Node c = root.addChild("child");
      assertEquals("child", c.getName());
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

   @Test
   public void pageId()
   {
      Node c = root.addChild("child");
      assertNull(c.getPageId());

      c.setPageId(new PageId("classic", "page"));
      assertEquals(new PageId("classic", "page"), c.getPageId());
   }

   @Test
   public void isRoot()
   {
      assertTrue(root.isRoot());
      assertFalse(root.addChild("child").isRoot());
   }
   

//   @Test
//   public void moveNode()
//   {
//      Node root = navigation.getNode(Nodes.visitNodes(NodePath.root(), Nodes.visitAll()));
//      root.addChild("foo").addChild("bar");
//      root.addChild("baz");
//
//      navigation.saveNode(root);
//
//      root = navigation.getNode(Nodes.visitNodes(NodePath.root(), Nodes.visitAll()));
//      assertNotNull(root.getChild("baz"));
//      assertNull(root.getChild("foo").getChild("bar").getChild("baz"));
//
//      navigation.moveNode(NodePath.path("baz"), NodePath.path("foo", "bar"));
//
//      root = navigation.getNode(Nodes.visitNodes(NodePath.root(), Nodes.visitAll()));
//      assertNull(root.getChild("baz"));
//      assertNotNull(root.getChild("foo").getChild("bar").getChild("baz"));
//   }

   static void assertVisibility(boolean expectedVisible, Flag expectedFlag, PublicationDate expectedDate, Node actualNode)
   {
      assertEquals(expectedVisible, actualNode.isVisible());
      assertEquals(expectedFlag, actualNode.getVisibility().getFlag());
      assertEquals(expectedDate, actualNode.getVisibility().getPublicationDate());
   }

   @Before
   public void before() throws Exception
   {
      root = createRoot(true);
   }
}
