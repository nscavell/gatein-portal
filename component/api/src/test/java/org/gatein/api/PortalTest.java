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

package org.gatein.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.gatein.api.common.Filter;
import org.gatein.api.common.Pagination;
import org.gatein.api.page.Page;
import org.gatein.api.page.PageId;
import org.gatein.api.page.PageQuery;
import org.gatein.api.security.Group;
import org.gatein.api.security.User;
import org.gatein.api.site.Site;
import org.gatein.api.site.SiteId;
import org.gatein.api.site.SiteImpl;
import org.gatein.api.site.SiteQuery;
import org.gatein.api.site.SiteType;
import org.junit.Test;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@redhat.com">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class PortalTest extends AbstractApiTest {
    @Test
    public void getSites() {
        createSite(defaultSiteId);

        List<Site> sites = portal.findSites(new SiteQuery.Builder().withAllSiteTypes().build());

        assertNotNull(sites);
        assertEquals(1, sites.size());
        assertEquals("classic", sites.get(0).getId().getName());

        assertNotNull(portal.getSite(new SiteId("classic")));
    }

    @Test
    public void naturalOrdering() {
        createSite(new SiteId("z"));
        createSite(new SiteId("a"));
        createSite(new SiteId("f"));
        createSite(new SiteId("b"));

        List<Site> sites = portal.findSites(new SiteQuery.Builder().includeEmptySites(true).build());
        assertEquals(4, sites.size());
        assertEquals("z", sites.get(0).getId().getName());
        assertEquals("a", sites.get(1).getId().getName());
        assertEquals("f", sites.get(2).getId().getName());
        assertEquals("b", sites.get(3).getId().getName());
    }

    @Test
    public void sortedSiteQuery_comparator() {
        Site site = new SiteImpl("b");
        site.setDisplayName("Toyota");
        portal.saveSite(site);

        site = new SiteImpl("a");
        site.setDisplayName("Chevy");
        portal.saveSite(site);

        site = new SiteImpl("c");
        site.setDisplayName("Volvo");
        portal.saveSite(site);

        site = new SiteImpl("d");
        site.setDisplayName("Ford");
        portal.saveSite(site);

        List<Site> sites = portal.findSites(new SiteQuery.Builder().includeEmptySites(true).build());

        Comparator<Site> c = new Comparator<Site>() {
            @Override
            public int compare(Site o1, Site o2) {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        };
        Collections.sort(sites, c);

        assertEquals(4, sites.size());
        assertEquals("a", sites.get(0).getId().getName());
        assertEquals("d", sites.get(1).getId().getName());
        assertEquals("b", sites.get(2).getId().getName());
        assertEquals("c", sites.get(3).getId().getName());
    }

    @Test
    public void pagedSiteQuery() {
        for (int i = 0; i < 10; i++) {
            createSite(new SiteId("site" + (i + 1)));
        }

        SiteQuery query = new SiteQuery.Builder().includeEmptySites(true).withPagination(0, 5).build();
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

    @Test
    public void pagedSiteQuery_WithMultipleSiteTypes() {
        // Add more sites and check
        createSite(new SiteId("site1"));
        createSite(new SiteId("site2"));
        createSite(new SiteId("site3"));
        createSite(new SiteId("site4"));

        createSite(new SiteId(new Group("/platform/users")));
        createSite(new SiteId(new Group("/foo/bar")));
        createSite(new SiteId(new Group("blah")));

        createSite(new SiteId(new User("root")));
        createSite(new SiteId(new User("john")));
        createSite(new SiteId(new User("mary")));

        List<Site> sites = portal.findSites(new SiteQuery.Builder().includeEmptySites(true).withAllSiteTypes().build());
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
        assertEquals(4, portal.findSites(new SiteQuery.Builder().includeEmptySites(true).withSiteTypes(SiteType.SITE).build())
                .size());
        assertEquals(3, portal.findSites(new SiteQuery.Builder().includeEmptySites(true).withSiteTypes(SiteType.SPACE).build())
                .size());
        assertEquals(3,
                portal.findSites(new SiteQuery.Builder().includeEmptySites(true).withSiteTypes(SiteType.DASHBOARD).build())
                        .size());

        // By type and range
        assertEquals(
                2,
                portal.findSites(
                        new SiteQuery.Builder().includeEmptySites(true).withSiteTypes(SiteType.SITE).withPagination(0, 2)
                                .build()).size());
        assertEquals(
                2,
                portal.findSites(
                        new SiteQuery.Builder().includeEmptySites(true).withSiteTypes(SiteType.SPACE).withPagination(0, 2)
                                .build()).size());
        assertEquals(
                2,
                portal.findSites(
                        new SiteQuery.Builder().includeEmptySites(true).withSiteTypes(SiteType.DASHBOARD).withPagination(0, 2)
                                .build()).size());

        assertEquals(
                2,
                portal.findSites(
                        new SiteQuery.Builder().includeEmptySites(true).withSiteTypes(SiteType.SITE).withPagination(0, 2)
                                .withNextPage().build()).size());
        assertEquals(
                1,
                portal.findSites(
                        new SiteQuery.Builder().includeEmptySites(true).withSiteTypes(SiteType.SPACE).withPagination(0, 2)
                                .withNextPage().build()).size());
        assertEquals(
                1,
                portal.findSites(
                        new SiteQuery.Builder().includeEmptySites(true).withSiteTypes(SiteType.DASHBOARD).withPagination(0, 2)
                                .withNextPage().build()).size());
    }

    @Test
    public void filteredSiteQuery() {
        createSite(new SiteId("c"));
        createSite(new SiteId("a"));
        createSite(new SiteId("d"));
        createSite(new SiteId("b"));

        List<Site> sites = portal.findSites(new SiteQuery.Builder().includeEmptySites(true).withFilter(new Filter<Site>() {
            @Override
            public boolean accept(Site site) {
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

    @Test
    public void siteQuery_NonHidden() {
        createSite(new SiteId("test-site"), false);

        List<Site> sites = portal.findSites(new SiteQuery.Builder().withSiteTypes(SiteType.SITE).build());
        assertTrue(sites.isEmpty());
    }

    @Test
    public void createSite() {
        Site site = portal.createSite(new SiteId("newsite"));
        portal.saveSite(site);

        assertNotNull(portal.getSite(new SiteId("newsite")));
        assertNull(portal.getSite(new SiteId("xxx")));
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void createSite_SiteExists() {
        createSite();
        portal.createSite(new SiteId("newsite"));
    }

    @Test
    public void removeSite() {
        createSite(new SiteId("test1"));
        createSite(new SiteId("test2"));
        createSite(new SiteId("test3"));

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

    @Test
    public void getSpace() {
        createSite(new SiteId(new Group("/platform/something")));

        Site space = portal.getSite(new SiteId(new Group("platform", "something")));
        assertNotNull(space);
    }

    @Test
    public void getDashboard() {
        createSite(new SiteId(new User("user10")));
        Site dashboard = portal.getSite(new SiteId(new User("user10")));
        assertNotNull(dashboard);
    }

    @Test
    public void getPage() {
        createSite(new SiteId("get-page"), "bar");

        assertNotNull(portal.getPage(new PageId("get-page", "bar")));
        assertNull(portal.getPage(new PageId("get-page", "blah")));
        assertNull(portal.getPage(new PageId("get-page", "foo")));

        try {
            portal.getPage(null);
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    @Test
    public void createPage() {
        createSite(new SiteId("create-page"), "bar");

        Page page = portal.createPage(new PageId("create-page", "baz"));
        assertNotNull(page);
        assertNull(portal.getPage(new PageId("create-page", "baz")));

        // save it
        portal.savePage(page);
        assertNotNull(portal.getPage(new PageId("create-page", "baz")));
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void createPage_PageExists() {
        createSite(new SiteId("create-page-exists"), "bar");
        portal.createPage(new PageId("create-page-exists", "bar"));
    }

    @Test
    public void findPages() {
        createSite(new SiteId("find-pages"), "page3", "page1", "page5", "page2", "page4");

        List<Page> pages = portal.findPages(new PageQuery.Builder().withSiteId(new SiteId("find-pages")).build());
        assertNotNull(pages);
        assertEquals(5, pages.size());
    }

    @Test
    public void findPages_ByTitle() {
        createSite(new SiteId("find-pages"), "page3", "page1", "page5", "page2", "page4");
        Page page = portal.getPage(new PageId("find-pages", "page1"));
        page.setDisplayName("FooTitle");
        portal.savePage(page);

        page = portal.getPage(new PageId("find-pages", "page4"));
        page.setDisplayName("FooTitle");
        portal.savePage(page);

        List<Page> pages = portal.findPages(new PageQuery.Builder().withSiteId(new SiteId("find-pages"))
                .withDisplayName("FooTitle").build());
        assertEquals(2, pages.size());
        for (Page p : pages) {
            assertEquals("FooTitle", p.getDisplayNames().getValue());
        }
    }

    @Test
    public void findPages_BySiteType() {
        createSite(new SiteId("find-pages"), "page1", "page2");
        createSite(new SiteId(new Group("find-pages")), "page3");
        createSite(new SiteId(new User("find-pages")), "page4", "page5", "page6");

        PageQuery query = new PageQuery.Builder().withSiteType(SiteType.SITE).build();
        List<Page> pages = portal.findPages(query);
        assertEquals(2, pages.size());

        query = new PageQuery.Builder().withSiteType(SiteType.SPACE).build();
        pages = portal.findPages(query);
        assertEquals(1, pages.size());

        query = new PageQuery.Builder().withSiteType(SiteType.DASHBOARD).build();
        pages = portal.findPages(query);
        assertEquals(3, pages.size());
    }

    @Test
    public void findPages_BySiteName() {
        createSite(new SiteId("find-pages"), "page1", "page2");
        createSite(new SiteId(new Group("find-pages")), "page3");
        createSite(new SiteId(new User("find-pages")), "page4", "page5", "page6");

        PageQuery query = new PageQuery.Builder().withSiteName("find-pages").build();
        List<Page> pages = portal.findPages(query);
        assertEquals(6, pages.size());
    }

    @Test
    public void findPages_BySiteType_And_SiteName() {
        createSite(new SiteId("find-pages"), "page1", "page2");
        createSite(new SiteId(new Group("find-pages")), "find-pages", "page3");
        createSite(new SiteId(new User("find-pages")), "page4", "page5", "page6");

        PageQuery query = new PageQuery.Builder().withSiteType(SiteType.DASHBOARD).withSiteName("find-pages").build();
        List<Page> pages = portal.findPages(query);
        assertEquals(3, pages.size());
    }

    @Test
    public void findPages_Pagination() {
        createSite(new SiteId("find-pages"), "page1", "page2", "page3", "page4", "page5", "page6", "page7");

        PageQuery query = new PageQuery.Builder().withSiteId(new SiteId("find-pages")).withPagination(0, 5).build();
        List<Page> pages = portal.findPages(query);
        assertEquals(5, pages.size());

        pages = portal.findPages(query.nextPage());
        assertEquals(2, pages.size());
    }

    @Test
    public void hasPermission() {
        createSite(new SiteId("permissions"), "page");
        PageQuery query = new PageQuery.Builder().withSiteId(new SiteId("permissions")).build();

        setPermission(new PageId(new SiteId("permissions"), "page"), "*:/platform/administrators", "Everyone");

        Page page = portal.findPages(query).get(0);

        assertTrue(portal.hasPermission(new User("root"), page.getAccessPermission()));
        assertTrue(portal.hasPermission(new User("root"), page.getEditPermission()));

        assertTrue(portal.hasPermission(User.anonymous(), page.getAccessPermission()));
        assertFalse(portal.hasPermission(User.anonymous(), page.getEditPermission()));
    }
}
