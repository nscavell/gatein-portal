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

import junit.framework.AssertionFailedError;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.component.test.KernelLifeCycle;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.page.PageState;
import org.gatein.api.Portal;
import org.gatein.api.SiteNotFoundException;
import org.gatein.api.impl.TestPortalRequest;
import org.gatein.api.impl.Util;
import org.gatein.api.portal.LocalizedString;
import org.gatein.api.portal.Permission;
import org.gatein.api.portal.User;
import org.gatein.api.portal.navigation.Navigation;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.portal.navigation.Nodes;
import org.gatein.api.portal.site.SiteId;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@ConfiguredBy(
{ @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
      @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
      @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
      @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.resources-configuration.xml"),
      @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.api-configuration.xml")
})
public class NavigationImplTest
{
   @ClassRule
   public static KernelLifeCycle kernelLifeCycle = new KernelLifeCycle();

   private PortalContainer container;

   private Navigation navigation;

   private Portal portal;

   private SiteId siteId;

   @Test
   public void addChild()
   {
      createNavigationChildren();

      Node root = navigation.loadNodes(Nodes.visitAll());
      Node node = root.addChild("parent2");

      assertNotNull(root.getChild("parent2"));

      navigation.saveNode(node);

      root = navigation.loadNodes(Nodes.visitAll());
      assertNotNull(root.getChild("parent2"));
   }

   @Test(expected = IllegalArgumentException.class)
   public void addChildExisting()
   {
      createNavigationChildren();

      Node node = navigation.loadNodes(Nodes.visitAll());
      node.getChild("parent").addChild("child");
   }

   @Test(expected = IllegalStateException.class)
   public void addChildNotExpanded()
   {
      createNavigationChildren();

      Node node = navigation.loadNodes(Nodes.visitChildren());
      node.getChild("parent").addChild("child");
   }

   @After
   public void after()
   {
      deleteSite(SiteType.PORTAL, "classic");
      TestPortalRequest.clearInstance();
   }

   @Before
   public void before()
   {
      container = kernelLifeCycle.getContainer();
      portal = (Portal) container.getComponentInstanceOfType(Portal.class);
      assertNotNull("Portal component not found in container", portal);

      RequestLifeCycle.begin(container);
      createSite(SiteType.PORTAL, "classic");
      RequestLifeCycle.end();

      RequestLifeCycle.begin(container);

      siteId = new SiteId("classic");
      navigation = portal.getNavigation(siteId);

      TestPortalRequest.setInstance(new User("john"), siteId, NodePath.root(), Locale.ENGLISH, portal);
   }

   @Test
   public void createNavigationChildren()
   {
      Node node = navigation.loadNodes(Nodes.visitAll());

      Node parent = node.addChild("parent");
      parent.addChild("child");

      navigation.saveNode(node);

      navigation = portal.getNavigation(siteId);
      node = navigation.loadNodes(Nodes.visitAll());

      assertEquals(1, node.getChildCount());
      assertEquals(1, node.getChild("parent").getChildCount());
      assertEquals(0, node.getChild("parent").getChild("child").getChildCount());
   }

   @Test
   public void createNavigationEmpty()
   {
      navigation.setPriority(10);

      navigation = portal.getNavigation(siteId);

      assertEquals(10, navigation.getPriority().intValue());
      assertEquals(siteId, navigation.getSiteId());
      assertEquals(0, navigation.loadNodes(Nodes.visitAll()).getChildCount());
   }

   @Test
   public void deleteNode() throws InterruptedException
   {
      createNavigationChildren();

      assertTrue(navigation.deleteNode(NodePath.path("parent", "child")));

      Node node = navigation.loadNodes(Nodes.visitAll());
      assertEquals(0, node.getChild("parent").getChildCount());
   }

   @Test
   public void getChild()
   {
      createNavigationChildren();

      Node node = navigation.loadNodes(Nodes.visitAll());
      assertNotNull(node);
      assertTrue(node.isChildrenLoaded());
      assertTrue(node.getChild("parent").getChild("child").isChildrenLoaded());

      node = navigation.loadNodes(Nodes.visitChildren());
      assertNotNull(node);
      assertTrue(node.isChildrenLoaded());
      assertFalse(node.getChild("parent").isChildrenLoaded());
   }

   @Test
   public void getNode()
   {
      createNavigationChildren();

      Node node = navigation.getNode("parent", "child");
      assertNotNull(node);
      assertEquals("child", node.getName());
   }

   @Test
   public void getNode_With_Visitor()
   {
      createNavigationChildren();

      Node node = navigation.getNode(NodePath.path("parent"), Nodes.visitNone());
      assertNotNull(node);
      assertFalse(node.isChildrenLoaded());
      assertNull(node.getChild("child"));

      node = navigation.getNode(NodePath.path("parent"), Nodes.visitChildren());
      assertTrue(node.isChildrenLoaded());
      assertNotNull(node.getChild("child"));
   }

   @Test
   public void getNode_Invalid_Path()
   {
      createNavigationChildren();

      Node node = navigation.getNode("foo", "child");
      assertNull(node);
   }

   @Test(expected = SiteNotFoundException.class)
   public void getNavigation_InvalidSite()
   {
      portal.getNavigation(new SiteId("invalid")).loadNodes(Nodes.visitAll());
   }

   @Test
   public void displayName_extended()
   {
      Node node = navigation.loadNodes(Nodes.visitAll());

      Node n = node.addChild("parent");

      Map<Locale, String> m = new HashMap<Locale, String>();
      m.put(Locale.ENGLISH, "extended");
      m.put(Locale.FRENCH, "prolongé");
      
      n.setDisplayName(new LocalizedString(m));

      navigation.saveNode(node);

      n = navigation.loadNodes(Nodes.visitChildren()).getChild("parent");

      assertNotNull(n.getDisplayName());
      assertTrue(n.getDisplayName().isLocalized());
      assertEquals("extended", n.getDisplayName().getValue(Locale.ENGLISH));
      assertEquals("prolongé", n.getDisplayName().getValue(Locale.FRENCH));
   }

   @Test
   public void displayName_simple()
   {
      Node node = navigation.loadNodes(Nodes.visitAll());

      Node n = node.addChild("parent");
      n.setDisplayName("simple");

      navigation.saveNode(n);

      assertEquals("simple", n.getDisplayName().getValue());

      n = navigation.loadNodes(Nodes.visitChildren()).getChild("parent");

      assertNotNull(n.getDisplayName());
      assertEquals("simple", n.getDisplayName().getValue());
      assertFalse(n.getDisplayName().isLocalized());
   }

   @Test
   public void loadChildren()
   {
      createNavigationChildren();

      Node node = navigation.loadNodes(Nodes.visitChildren());
      assertNotNull(node);
      Node parent = node.getChild("parent");
      assertTrue(node.isChildrenLoaded());
      assertFalse(parent.isChildrenLoaded());
      assertNull(parent.getChild("child"));

      navigation.loadChildren(parent);

      assertTrue(parent.isChildrenLoaded());
      assertNotNull(parent.getChild("child"));
   }

   @Test
   public void refreshNode()
   {
      createNavigationChildren();

      Node nodeA = navigation.loadNodes(Nodes.visitAll());
      Node nodeB = navigation.loadNodes(Nodes.visitAll());

      nodeA.addChild("childA");
      navigation.saveNode(nodeA);

      assertNull(nodeB.getChild("childA"));
      navigation.refreshNode(nodeB);
      assertNotNull(nodeB.getChild("childA"));
   }

   @Test
   public void refreshNode_WithVisitor()
   {
      createNavigationChildren();

      Node nodeA = navigation.loadNodes(Nodes.visitAll());
      Node nodeB = navigation.loadNodes(Nodes.visitAll());

      nodeA.addChild("childA").addChild("childA-1").addChild("childA-1-1");
      navigation.saveNode(nodeA);

      assertNull(nodeB.getChild("childA"));
      navigation.refreshNode(nodeB, Nodes.visitNodes(3));
      assertNotNull(nodeB.getChild("childA"));
      assertNotNull(nodeB.getChild("childA").getChild("childA-1"));
      assertNotNull(nodeB.getChild("childA").getChild("childA-1").getChild("childA-1-1"));
   }

   @Test
   public void saveNode() throws InterruptedException
   {
      createNavigationChildren();

      Node node = navigation.loadNodes(Nodes.visitAll());

      Node parent = node.getChild("parent");

      parent.addChild("child2");
      
      assertNull(navigation.loadNodes(Nodes.visitAll()).getChild("parent").getChild("child2"));

      navigation.saveNode(parent);

      assertNotNull(navigation.loadNodes(Nodes.visitAll()).getChild("parent").getChild("child2"));
   }

   @Test
   public void saveNode_Merge()
   {
      createNavigationChildren();

      Node nodeA = navigation.loadNodes(Nodes.visitAll());
      Node nodeB = navigation.loadNodes(Nodes.visitAll());

      nodeA.addChild("childA");
      navigation.saveNode(nodeA);

      nodeB.addChild("childB");
      navigation.saveNode(nodeB);

      Node nodeC = navigation.loadNodes(Nodes.visitAll());

      assertNotNull(nodeC.getChild("childA"));
      assertNotNull(nodeC.getChild("childB"));
   }

   @Test
   public void serialization() throws Exception
   {
      createNavigationChildren();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(baos);
      Node parent = navigation.getNode(NodePath.path("parent"), Nodes.visitChildren());
      Node child = parent.getChild("child");
      navigation.loadChildren(child);

      // transient changes
      child.addChild("foo");
      child.setIconName("iconName");

      // serialize parent
      out.writeObject(parent);

      // deserialize parent
      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
      Node parentNode = (Node) in.readObject();

      // test deserialized node
      assertEquals("parent", parentNode.getName());
      assertEquals(((ApiNode) parent).context.getState(), ((ApiNode) parentNode).context.getState());
      assertTrue(parentNode.isChildrenLoaded());
      Node childNode = parentNode.getChild("child");
      assertNotNull(childNode);
      assertEquals(((ApiNode) child).context.getState(), ((ApiNode) childNode).context.getState());

      // test parent relationship
      assertNotNull(childNode.getParent());
      assertEquals("parent", childNode.getParent().getName());

      // test transient changes
      Node fooNode = childNode.getChild("foo");
      assertNotNull(fooNode);
      assertEquals(((ApiNode) child.getChild("foo")).context.getState(), ((ApiNode) fooNode).context.getState());

      // test object identities
      assertTrue(childNode == fooNode.getParent());
      assertTrue(parentNode.getParent() == childNode.getParent().getParent());
   }

   //TODO: Add more serialization tests like moving/removing nodes, displayName, etc

   void createSite(SiteType type, String name)
   {
      try
      {
         DataStorage dataStorage = (DataStorage) container.getComponentInstanceOfType(DataStorage.class);
         NavigationService navService = (NavigationService) container.getComponentInstanceOfType(NavigationService.class);
         PageService pageService = (PageService) container.getComponentInstanceOfType(PageService.class);

         PortalConfig config = new PortalConfig(type.getName(), name);
         config.setAccessPermissions(Util.from(Permission.everyone()));

         dataStorage.create(config);

         NavigationContext nav = new NavigationContext(new SiteKey(type, name), new NavigationState(0));
         navService.saveNavigation(nav);

         pageService.savePage(new PageContext(new PageKey(new SiteKey(type, name), "homepage"), new PageState("displayName",
               "description", false, null, null, null)));
      }
      catch (Exception e)
      {
         AssertionFailedError afe = new AssertionFailedError();
         afe.initCause(e);
         throw afe;
      }
   }

   void deleteSite(SiteType type, String name)
   {
      try
      {
         DataStorage dataStorage = (DataStorage) container.getComponentInstanceOfType(DataStorage.class);
         dataStorage.remove(new PortalConfig(type.getName(), name));
      }
      catch (Exception e)
      {
         AssertionFailedError afe = new AssertionFailedError();
         afe.initCause(e);
         throw afe;
      }
   }

}
