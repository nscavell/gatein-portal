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
package org.gatein.api.impl;

import static org.junit.Assert.*;

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
import org.gatein.api.portal.Label;
import org.gatein.api.portal.Permission;
import org.gatein.api.portal.navigation.Navigation;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.portal.navigation.Nodes;
import org.gatein.api.portal.site.SiteId;
import org.junit.After;
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
public class PortalImplTest
{
   @ClassRule
   public static KernelLifeCycle kernelLifeCycle = new KernelLifeCycle();

   private Portal portal;

   private SiteId siteId;

   private PortalContainer container;

   @Before
   public void before()
   {
      container = kernelLifeCycle.getContainer();
      portal = (Portal) container.getComponentInstanceOfType(Portal.class);
      assertNotNull("Portal component not found in container", portal);

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
   { // TODO Not working at the moment, question? should the navigation be created when the site is?
      portal.getNavigation(new SiteId("invalid"), Nodes.visitAll(), null);
      fail("Expected SiteNotFoundException");
   }

   @Test
   public void getNavigationNoNavigation()
   {
      Navigation navigation = portal.getNavigation(siteId, Nodes.visitAll(), null);
      assertNull(navigation);
   }

   @Test
   public void getChild()
   {
      createNavigationWithChildren();

      Node node = portal.getNode(siteId, Nodes.visitAll(), null);
      assertNotNull(node);
      assertTrue(node.isChildrenLoaded());
      assertTrue(node.getChild("parent").getChild("child").isChildrenLoaded());

      node = portal.getNode(siteId, Nodes.visitChildren(), null);
      assertNotNull(node);
      assertTrue(node.isChildrenLoaded());
      assertFalse(node.getChild("parent").isChildrenLoaded());
   }

   @Test
   public void loadNodes()
   {
      createNavigationWithChildren();

      Node node = portal.getNode(siteId, Nodes.visitChildren(), null);
      Node parent = node.getChild("parent");
      assertNotNull(node);
      assertTrue(node.isChildrenLoaded());
      assertFalse(parent.isChildrenLoaded());

      portal.loadNodes(parent, Nodes.visitAll()); // TODO Problem as we don't know siteId!

      assertTrue(parent.isChildrenLoaded());
   }

   @Test
   public void saveNode()
   {
      createNavigationWithChildren();

      Navigation navigation = portal.getNavigation(siteId, Nodes.visitAll(), null);
      Node parent = navigation.getChild("parent");
      Node child2 = new Node("child2");
      parent.addChild(child2);
      
      assertNull(portal.getNode(siteId, child2.getNodePath()));

      portal.saveNode(parent);

      assertNotNull(portal.getNode(siteId, child2.getNodePath()));
   }

   @Test
   public void getChildWithNodePath()
   {
      createNavigationWithChildren();

      Node node = portal.getNode(siteId, new NodePath());
      assertEquals(null, node.getName());
      assertTrue(node.isChildrenLoaded());
      assertFalse(node.getChild("parent").isChildrenLoaded());

      node = portal.getNode(siteId, new NodePath("parent"));
      assertEquals("parent", node.getName());
      assertTrue(node.isChildrenLoaded());
      assertFalse(node.getChild("child").isChildrenLoaded());

      node = portal.getNode(siteId, new NodePath("parent", "child"));
      assertEquals("child", node.getName());
      assertTrue(node.isChildrenLoaded());
   }

   @Test
   public void createNavigationNoChildren()
   {
      Navigation n = new Navigation(siteId, 10);
      portal.saveNavigation(n);

      n = portal.getNavigation(siteId, Nodes.visitAll(), null);

      assertEquals(10, n.getPriority());
      assertEquals(siteId, n.getSiteId());
      assertTrue(n.getChildren().isEmpty());
   }

   @Test
   public void createNavigationWithChildren()
   {
      Navigation n = new Navigation(siteId, 10);
      n.addChild(new Node("parent"));
      n.getChild("parent").addChild(new Node("child"));
      portal.saveNavigation(n);

      n = portal.getNavigation(siteId, Nodes.visitAll(), null);

      assertEquals(10, n.getPriority());
      assertEquals(siteId, n.getSiteId());
      assertEquals(1, n.getChildren().size());
      assertEquals(1, n.getChild("parent").getChildren().size());
      assertEquals(0, n.getChild("parent").getChild("child").getChildren().size());
   }

   @Test
   public void simpleLabel()
   {
      Navigation navigation = new Navigation(siteId, 10);

      Node n = new Node("parent");
      n.setLabel(new Label("simple"));
      navigation.addChild(n);

      portal.saveNavigation(navigation);

      n = portal.getNode(siteId, n.getNodePath());

      assertEquals("simple", n.getLabel().getValue());
      assertFalse(n.getLabel().isLocalized());
   }

   @Test
   public void extendedLabel()
   {
      Navigation navigation = new Navigation(siteId, 10);

      Node n = new Node("parent");

      Map<Locale, String> m = new HashMap<Locale, String>();
      m.put(Locale.ENGLISH, "extended");
      m.put(Locale.FRENCH, "prolongé");
      
      n.setLabel(new Label(m));
      navigation.addChild(n);

      portal.saveNavigation(navigation);

      n = portal.getNode(siteId, n.getNodePath());

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
