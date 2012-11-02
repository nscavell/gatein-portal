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
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.page.PageState;
import org.gatein.api.Portal;
import org.gatein.api.portal.Permission;
import org.gatein.api.portal.navigation.Navigation;
import org.gatein.api.portal.navigation.Node;
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
   public void getNavigation()
   {
      Navigation navigation = portal.getNavigation(siteId, Nodes.visitAll(), null);
      assertNull(navigation);
   }
   
   @Test
   public void createNavigationNoChildren()
   {
      Navigation n = new Navigation(siteId, 10);
      portal.saveNavigation(n);

      n = portal.getNavigation(siteId, Nodes.visitAll(), null);

      assertEquals(10, n.getPriority());
      assertEquals(siteId, n.getSiteId());
      assertTrue(n.getNodes().isEmpty());
   }

   @Test
   public void createNavigationWithChildren()
   {
      Navigation n = new Navigation(siteId, 10);
      n.addNode(new Node("parent"));
      n.getNode("parent").addNode(new Node("child"));
      portal.saveNavigation(n);

      n = portal.getNavigation(siteId, Nodes.visitAll(), null);

      assertEquals(10, n.getPriority());
      assertEquals(siteId, n.getSiteId());
      assertEquals(1, n.getNodes().size());
      assertEquals(1, n.getNode("parent").getNodes().size());
      assertEquals(0, n.getNode("parent").getNode("child").getNodes().size());
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
