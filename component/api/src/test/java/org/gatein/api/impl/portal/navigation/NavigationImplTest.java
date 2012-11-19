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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.page.PageState;
import org.gatein.api.Portal;
import org.gatein.api.impl.Util;
import org.gatein.api.portal.Label;
import org.gatein.api.portal.Permission;
import org.gatein.api.portal.navigation.Navigation;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.portal.navigation.Nodes;
import org.gatein.api.portal.site.SiteId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

@ConfiguredBy(
{ @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
      @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
      @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
      @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.resources-configuration.xml"),
      @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.api-configuration.xml")
// @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.application-registry-configuration.xml")
})
public class NavigationImplTest
{
   @ClassRule
   public static KernelLifeCycle kernelLifeCycle = new KernelLifeCycle();

   private Portal portal;

   private SiteId siteId;

   private PortalContainer container;

   private Navigation navigation;

   @Before
   public void before()
   {
      container = kernelLifeCycle.getContainer();
      portal = (Portal) container.getComponentInstanceOfType(Portal.class);
      assertNotNull("Portal component not found in container", portal);

      navigation = portal.getNavigation(siteId);

      siteId = new SiteId("classic");

      RequestLifeCycle.begin(container);
      createSite(SiteType.PORTAL, "classic");
      RequestLifeCycle.end();

      RequestLifeCycle.begin(container);
   }

   @After
   public void after()
   {
      deleteSite(SiteType.PORTAL, "classic");
   }

   @Test
   public void getNavigationInvalidSite()
   { // TODO Is Navigation always created for a site?
      portal.getNavigation(new SiteId("invalid"));
      fail("Expected SiteNotFoundException");
   }

   @Test
   public void getNavigationNoNavigation()
   { // TODO Is Navigation always created for a site?
      Navigation navigation = portal.getNavigation(siteId);
      assertNull(navigation);
   }

   @Test
   public void getChild()
   {
      createNavigationWithChildren();

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
   public void loadNodes()
   {
      createNavigationWithChildren();

      Node node = navigation.loadNodes(Nodes.visitChildren());
      assertNotNull(node);
      Node parent = node.getChild("parent");
      assertTrue(node.isChildrenLoaded());
      assertFalse(parent.isChildrenLoaded());

      navigation.loadChildren(parent);

      assertTrue(parent.isChildrenLoaded());
   }

   @Test
   public void saveNode() throws InterruptedException
   {
      createNavigationWithChildren();

      Node node = navigation.loadNodes(Nodes.visitAll());

      Node parent = node.getChild("parent");

      Node child2 = parent.addChild("child2");
      
      assertNull(navigation.getNode(child2.getNodePath(), Nodes.visitNone()));

      navigation.saveNode(parent);

      assertNotNull(navigation.getNode(child2.getNodePath(), Nodes.visitNone()));
   }

   @Test
   public void getChildWithNodePath()
   {
      createNavigationWithChildren();

      Node node = navigation.loadNodes(Nodes.visitAll());
      assertEquals(null, node.getName());
      assertTrue(node.isChildrenLoaded());
      assertFalse(node.getChild("parent").isChildrenLoaded());

      node = navigation.getNode(NodePath.path("parent"), Nodes.visitChildren());
      assertEquals("parent", node.getName());
      assertTrue(node.isChildrenLoaded());
      assertFalse(node.getChild("child").isChildrenLoaded());

      node = navigation.getNode(NodePath.path("parent", "child"), Nodes.visitChildren());
      assertEquals("child", node.getName());
      assertTrue(node.isChildrenLoaded());
   }

   @Test
   public void createNavigationNoChildren()
   {
      navigation.setPriority(10);

      navigation = portal.getNavigation(siteId);

      assertEquals(10, navigation.getPriority().intValue());
      assertEquals(siteId, navigation.getSiteId());
      assertTrue(navigation.loadNodes(Nodes.visitAll()).getChildren().isEmpty());
   }

   @Test
   public void createNavigationWithChildren()
   {
      Node node = navigation.loadNodes(Nodes.visitAll());

      Node parent = node.addChild("parent");
      parent.addChild("child");

      navigation.saveNode(node);

      navigation = portal.getNavigation(siteId);
      node = navigation.loadNodes(Nodes.visitAll());

      assertEquals(1, node.getChildren().size());
      assertEquals(1, node.getChild("parent").getChildren().size());
      assertEquals(0, node.getChild("parent").getChild("child").getChildren().size());
   }

   @Test
   public void simpleLabel()
   {
      Node node = navigation.loadNodes(Nodes.visitAll());

      Node n = node.addChild("parent");
      n.setLabel(new Label("simple"));

      navigation.saveNode(node);

      n = navigation.getNode(n.getNodePath(), Nodes.visitNone());

      assertEquals("simple", n.getLabel().getValue());
      assertFalse(n.getLabel().isLocalized());
   }

   @Test
   public void extendedLabel()
   {
      Node node = navigation.loadNodes(Nodes.visitAll());

      Node n = node.addChild("parent");

      Map<Locale, String> m = new HashMap<Locale, String>();
      m.put(Locale.ENGLISH, "extended");
      m.put(Locale.FRENCH, "prolongé");
      
      n.setLabel(new Label(m));

      navigation.saveNode(node);

      n = navigation.getNode(n.getNodePath(), Nodes.visitNone());

      assertTrue(n.getLabel().isLocalized());
      assertEquals("extended", n.getLabel().getValue(Locale.ENGLISH));
      assertEquals("prolongé", n.getLabel().getValue(Locale.FRENCH));
   }

   void createSite(SiteType type, String name)
   {
      try
      {
         DataStorage dataStorage = (DataStorage) container.getComponentInstanceOfType(DataStorage.class);
         PageService pageService = (PageService) container.getComponentInstanceOfType(PageService.class);

         PortalConfig config = new PortalConfig(type.getName(), name);
         config.setAccessPermissions(Util.from(Permission.everyone()));

         dataStorage.create(config);

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
