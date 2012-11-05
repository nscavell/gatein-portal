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

import org.gatein.api.portal.Group;
import org.gatein.api.portal.User;
import org.gatein.api.portal.navigation.Navigation;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.portal.page.PageId;
import org.gatein.api.portal.site.Site;
import org.gatein.api.portal.site.SiteId;
import org.gatein.api.portal.site.SiteQuery;
import org.gatein.api.portal.site.SiteType;
import org.gatein.api.util.Filter;
import org.gatein.api.util.Pagination;
import org.junit.Assert;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@redhat.com">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class PortalTestCase extends AbstractAPITestCase
{
   public void testGetSites()
   {
      cleanup();

      createSite(org.exoplatform.portal.mop.SiteType.PORTAL, "classic");
      List<Site> sites = portal.findSites(new SiteQuery.Builder().withAllSiteTypes().build());

      assertNotNull(sites);
      assertEquals(1, sites.size());
      assertEquals("classic", sites.get(0).getId().getName());

      assertNotNull(portal.getSite(new SiteId("classic")));
   }

   public void testNaturalOrdering()
   {
      cleanup();

      portal.saveSite(new Site("z"));
      portal.saveSite(new Site("a"));
      portal.saveSite(new Site("f"));
      portal.saveSite(new Site("b"));

      List<Site> sites = portal.findSites(new SiteQuery.Builder().build());
      assertEquals(4, sites.size());
      assertEquals("z", sites.get(0).getId().getName());
      assertEquals("a", sites.get(1).getId().getName());
      assertEquals("f", sites.get(2).getId().getName());
      assertEquals("b", sites.get(3).getId().getName());
   }

   public void testSortedSiteQuery_ascending()
   {
      cleanup();

      portal.saveSite(new Site("c"));
      portal.saveSite(new Site("a"));
      portal.saveSite(new Site("d"));
      portal.saveSite(new Site("b"));

      List<Site> sites = portal.findSites(new SiteQuery.Builder().withSorting().ascending().build());

      assertEquals(4, sites.size());
      assertEquals("a", sites.get(0).getId().getName());
      assertEquals("b", sites.get(1).getId().getName());
      assertEquals("c", sites.get(2).getId().getName());
      assertEquals("d", sites.get(3).getId().getName());
   }

   public void testSortedSiteQuery_descending()
   {
      cleanup();

      portal.saveSite(new Site("c"));
      portal.saveSite(new Site("a"));
      portal.saveSite(new Site("d"));
      portal.saveSite(new Site("b"));

      List<Site> sites = portal.findSites(new SiteQuery.Builder().withSorting().descending().build());

      assertEquals(4, sites.size());
      assertEquals("d", sites.get(0).getId().getName());
      assertEquals("c", sites.get(1).getId().getName());
      assertEquals("b", sites.get(2).getId().getName());
      assertEquals("a", sites.get(3).getId().getName());
   }

   public void testSortedSiteQuery_comparator()
   {
      cleanup();

      Site site = new Site("b");
      site.setTitle("Toyota");
      portal.saveSite(site);

      site = new Site("a");
      site.setTitle("Chevy");
      portal.saveSite(site);

      site = new Site("c");
      site.setTitle("Volvo");
      portal.saveSite(site);

      site = new Site("d");
      site.setTitle("Ford");
      portal.saveSite(site);

      List<Site> sites = portal.findSites(new SiteQuery.Builder().withSorting().withComparator(new Comparator<Site>()
      {
         @Override
         public int compare(Site o1, Site o2)
         {
            return o1.getTitle().compareTo(o2.getTitle());
         }
      }).build());

      assertEquals(4, sites.size());
      assertEquals("a", sites.get(0).getId().getName());
      assertEquals("d", sites.get(1).getId().getName());
      assertEquals("b", sites.get(2).getId().getName());
      assertEquals("c", sites.get(3).getId().getName());
   }

   public void testPagedSiteQuery()
   {
      cleanup();

      for (int i = 0; i < 10; i++)
      {
         createSite(org.exoplatform.portal.mop.SiteType.PORTAL, "site" + (i + 1));
      }

      SiteQuery query = new SiteQuery.Builder().withPagination(0, 5).build();
      List<Site> sites = portal.findSites(query);
      assertEquals(5, sites.size());
      // check bounds
      assertEquals("site1", sites.get(0).getName());
      assertEquals("site5", sites.get(4).getName());

      query = query.nextPage();
      sites = portal.findSites(query);
      assertEquals(5, sites.size());
      // check bounds
      assertEquals("site6", sites.get(0).getName());
      assertEquals("site10", sites.get(4).getName());

      query = query.nextPage();
      sites = portal.findSites(query);
      assertEquals(0, sites.size());

      query = query.previousPage();
      sites = portal.findSites(query);
      assertEquals(5, sites.size());

      query = new SiteQuery.Builder().from(query).withPagination(2, 5).withNextPage().build();
      sites = portal.findSites(query);
      assertEquals(3, sites.size());
      assertEquals("site8", sites.get(0).getName());
      assertEquals("site10", sites.get(2).getName());
   }

   public void testPagedSiteQuery_WithMultipleSiteTypes()
   {
      cleanup();

      // Add more sites and check
      createSite(org.exoplatform.portal.mop.SiteType.PORTAL, "site1");
      createSite(org.exoplatform.portal.mop.SiteType.PORTAL, "site2");
      createSite(org.exoplatform.portal.mop.SiteType.PORTAL, "site3");
      createSite(org.exoplatform.portal.mop.SiteType.PORTAL, "site4");

      createSite(org.exoplatform.portal.mop.SiteType.GROUP, "/platform/users");
      createSite(org.exoplatform.portal.mop.SiteType.GROUP, "/foo/bar");
      createSite(org.exoplatform.portal.mop.SiteType.GROUP, "blah");

      createSite(org.exoplatform.portal.mop.SiteType.USER, "root");
      createSite(org.exoplatform.portal.mop.SiteType.USER, "john");
      createSite(org.exoplatform.portal.mop.SiteType.USER, "mary");

      List<Site> sites = portal.findSites(new SiteQuery.Builder().withAllSiteTypes().build());
      assertEquals(10, sites.size());

      // Range

      assertEquals(10, portal.findSites(new SiteQuery.Builder().withAllSiteTypes().withPagination(0, 10).build()).size());
      assertEquals(5, portal.findSites(new SiteQuery.Builder().withAllSiteTypes().withPagination(0, 5).build()).size());

      Pagination pagination = new Pagination(0, 3);
      SiteQuery query = new SiteQuery.Builder().withAllSiteTypes().withPagination(pagination).build();
      assertEquals(3, portal.findSites(query).size());

      query = query.nextPage();
      assertEquals(3, portal.findSites(query).size());

      query = query.nextPage();
      assertEquals(3, portal.findSites(query).size());

      query = query.nextPage();
      assertEquals(1, portal.findSites(query).size());

      query = query.nextPage();
      assertEquals(0, portal.findSites(query).size());

      query = query.previousPage();
      assertEquals(1, portal.findSites(query).size());

      query = query.previousPage();
      assertEquals(3, portal.findSites(query).size());

      // By type
      assertEquals(4, portal.findSites(new SiteQuery.Builder().withSiteTypes(SiteType.SITE).build()).size());
      assertEquals(3, portal.findSites(new SiteQuery.Builder().withSiteTypes(SiteType.SPACE).build()).size());
      assertEquals(3, portal.findSites(new SiteQuery.Builder().withSiteTypes(SiteType.DASHBOARD).build()).size());

      // By type and range
      assertEquals(2, portal.findSites(new SiteQuery.Builder().withSiteTypes(SiteType.SITE).withPagination(0, 2).build())
            .size());
      assertEquals(2, portal.findSites(new SiteQuery.Builder().withSiteTypes(SiteType.SPACE).withPagination(0, 2).build())
            .size());
      assertEquals(2, portal.findSites(new SiteQuery.Builder().withSiteTypes(SiteType.DASHBOARD).withPagination(0, 2).build())
            .size());

      assertEquals(2,
            portal.findSites(new SiteQuery.Builder().withSiteTypes(SiteType.SITE).withPagination(0, 2).withNextPage().build())
                  .size());
      assertEquals(
            1,
            portal.findSites(new SiteQuery.Builder().withSiteTypes(SiteType.SPACE).withPagination(0, 2).withNextPage().build())
                  .size());
      assertEquals(
            1,
            portal.findSites(
                  new SiteQuery.Builder().withSiteTypes(SiteType.DASHBOARD).withPagination(0, 2).withNextPage().build())
                  .size());
   }

   public void testFilteredSiteQuery()
   {
      cleanup();

      portal.saveSite(new Site("c"));
      portal.saveSite(new Site("a"));
      portal.saveSite(new Site("d"));
      portal.saveSite(new Site("b"));

      List<Site> sites = portal.findSites(new SiteQuery.Builder().withFilter(new Filter<Site>()
      {
         @Override
         public boolean accept(Site site)
         {
            return site.getName().equals("a") || site.getName().equals("b");
         }
      }).build());

      Iterator<Site> iter = sites.iterator();
      assertEquals(2, sites.size());
      Site site = iter.next();
      assertEquals("a", site.getId().getName());
      site = iter.next();
      assertEquals("b", site.getId().getName());
   }

   public void testAddSite()
   {
      portal.saveSite(new Site("newsite"));

      assertNotNull(portal.getSite(new SiteId("newsite")));
      assertNull(portal.getSite(new SiteId("xxx")));
   }

   public void testRemoveSite()
   {
      portal.saveSite(new Site("test1"));
      portal.saveSite(new Site("test2"));
      portal.saveSite(new Site("test3"));

      assertNotNull(portal.getSite(new SiteId("test1")));
      assertNotNull(portal.getSite(new SiteId("test2")));
      assertNotNull(portal.getSite(new SiteId("test3")));

      portal.removeSite(new SiteId("test1"));

      assertNull(portal.getSite((new SiteId("test1"))));
      assertNotNull(portal.getSite(new SiteId("test2")));
      assertNotNull(portal.getSite(new SiteId("test3")));

      portal.removeSite(new SiteId(SiteType.SITE, "test2"));

      assertNull(portal.getSite(new SiteId("te")));
      assertNull(portal.getSite(new SiteId("test2")));
      assertNotNull(portal.getSite(new SiteId("test3")));

   }

   public void testGetSpace()
   {
      createSite(org.exoplatform.portal.mop.SiteType.GROUP, "/platform/something");

      Site space = portal.getSite(new SiteId(new Group("platform", "something")));
      assertNotNull(space);
   }

   public void testGetDashboard()
   {
      createSite(org.exoplatform.portal.mop.SiteType.USER, "user10");
      Site dashboard = portal.getSite(new SiteId(new User("user10")));
      assertNotNull(dashboard);
   }
   
   public void testGetNavigation()
   {
      SiteId siteId = new SiteId("classic");

      try
      {
         portal.getNavigation(null, null, null);
         Assert.fail("Expected NullPointerException");
      }
      catch (NullPointerException e)
      {
      }

      Assert.assertNull(portal.getNavigation(siteId, null, null));

      createSite(org.exoplatform.portal.mop.SiteType.PORTAL, "classic");

      portal.saveNavigation(new Navigation(siteId, 20));

      Navigation navigation = portal.getNavigation(siteId, null, null);
      Assert.assertNotNull(navigation);
      Assert.assertEquals(20, navigation.getPriority());
      Assert.assertTrue(navigation.getChildren().isEmpty());

      // TODO Navigation with nodes, filter, visitor
   }

   public void testSaveNavigation()
   {
      createSite(org.exoplatform.portal.mop.SiteType.PORTAL, "classic");
      
      SiteId siteId = new SiteId("classic");

      Navigation navigation = new Navigation(siteId, 10);
      portal.saveNavigation(navigation);
      
      navigation = portal.getNavigation(siteId, null, null);
      Assert.assertEquals(10, navigation.getPriority());

      navigation.setPriority(20);
      portal.saveNavigation(navigation);

      navigation = portal.getNavigation(siteId, null, null);
      Assert.assertEquals(20, navigation.getPriority());
      
      Node parent1 = new Node("parent1");
      parent1.setPageId(new PageId("classic", "homepage"));
      Node child1 = new Node("child1");
      child1.setPageId(new PageId("classic", "homepage"));
      parent1.addChild(child1);

      Node parent2 = new Node("parent2");
      parent2.setPageId(new PageId("classic", "homepage"));
      Node child2 = new Node("child2");
      child2.setPageId(new PageId("classic", "homepage"));
      parent2.addChild(child2);
      
      navigation.addChild(parent1);
      navigation.addChild(parent2);

      portal.saveNavigation(navigation);
   }

   public void testGetNode()
   {
      createSite(org.exoplatform.portal.mop.SiteType.PORTAL, "classic");
      Node node = portal.getNode(new SiteId("classic"), new NodePath("default"));
      assertNotNull(node);
      assertEquals("default", node.getName());
   }

   // Just remove all sites
   void cleanup()
   {
      for (Site site : portal.findSites(new SiteQuery.Builder().withAllSiteTypes().build()))
      {
         portal.removeSite(site.getId());
      }
   }
}
