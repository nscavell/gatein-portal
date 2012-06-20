/*
* JBoss, a division of Red Hat
* Copyright 2012, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.gatein.portal.api.impl.portal;

import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.gatein.api.portal.Node;
import org.gatein.api.portal.Page;
import org.gatein.api.portal.Site;
import org.gatein.portal.api.impl.AbstractAPITestCase;

/** @author <a href="mailto:boleslaw.dawidowicz@redhat.com">Boleslaw Dawidowicz</a> */
public class SiteTestCase extends AbstractAPITestCase
{

   //TODO: label -NYI
   //TODO: navigation
   //TODO: properties -NYI

   public void testLabel()
   {
      //TODO: NYI

   }

   public void testDescription()
   {
      Site classic = gatein.addSite(Site.Id.site("classic"));

      assertNull(classic.getDescription());

      classic.setDescription("TEST");
      assertEquals("TEST", classic.getDescription());
      assertEquals("TEST", gatein.getSite(Site.Id.site("classic")).getDescription());

      classic.setDescription("TEST2");
      assertEquals("TEST2", classic.getDescription());

      classic.setDescription(null);
      assertEquals(null, classic.getDescription());

   }

   public void testPages() throws Exception
   {
      NodeContext<?> root = createSite(SiteType.PORTAL, "test1");

      Site site = gatein.getSite(Site.Id.site("test1"));


      // Check page created in method
      assertEquals(1, site.getPages().size());
      assertNotNull(site.getPage("homepage"));
      assertNull(site.getPage("page1"));

      // Add more pages internally with reference to test site
      storage.create(new org.exoplatform.portal.config.model.Page(SiteType.PORTAL.getName(), "test1", "page1"));
      storage.create(new org.exoplatform.portal.config.model.Page(SiteType.PORTAL.getName(), "test1", "page2"));
      storage.create(new org.exoplatform.portal.config.model.Page(SiteType.PORTAL.getName(), "test1", "page3"));

      // Check new stuff
      assertEquals(4, gatein.getSite(Site.Id.site("test1")).getPages().size());
      assertNotNull(site.getPage("homepage"));
      assertNotNull(site.getPage("page1"));
      assertNotNull(site.getPage("page2"));
      assertNotNull(site.getPage("page3"));
      assertNull(site.getPage("page4"));


      // Remove
      site.removePage("homepage");
      site.removePage("page3");

      // Check
      assertEquals(2, gatein.getSite(Site.Id.site("test1")).getPages().size());
      assertEquals(2, site.getPages().size());
      assertNull(site.getPage("homepage"));
      assertNotNull(site.getPage("page1"));
      assertNotNull(site.getPage("page2"));
      assertNull(site.getPage("page3"));
      assertNull(site.getPage("page4"));

      //TODO: test getPages(Range range)

   }


   public void testCreatePage()
   {
      Site site = gatein.getSite(Site.Id.site("test1"));

      Page page = site.createPage("newPage1");
      assertNotNull(page);
      assertEquals("newPage1", page.getName());

      page = site.getPage("newPage1");
      assertNotNull(page);
      assertEquals("newPage1", page.getName());

      // Check including ones from previous test
      assertEquals(3, site.getPages().size());
   }

   public void testNavigation()
   {
      // After last two tests there should be "test1" site  with "page1", "page2" and "newPage1"

      Site site = gatein.getSite(Site.Id.site("test1"));

      // There should be empty navigation
      assertNotNull(site.getNavigation());
      assertFalse(site.getNavigation().iterator().hasNext());

      //TODO: uncomment when Site.setPageReference is implemented
//      site.getNavigation().addNode("node1").setPageReference(site.getPage("page1").getId());
//
//      Node node = gatein.getSite(Site.Id.site("test1")).getNavigation().getNode("node1");
//      assertEquals("page1", node.getPage().getName());

   }



   public void testProperties()
   {
      //TODO: NYI
   }

}
