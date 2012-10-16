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

package org.exoplatform.portal.mop.page;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.management.operations.page.PageUtils;
import org.exoplatform.portal.pom.data.ApplicationData;
import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.ContainerData;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.Preference;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/*
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class TestPageImport extends AbstractTestPageService
{  
   @Test
   public void testSave() throws Exception
   {
      Site site = mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "import_pages");
      site.getRootPage().addChild("pages");
      sync(true);

      int numberOfPages = Integer.parseInt(System.getProperty("numberOfPages", "25"));
      Map<Page, PageContext> pageMap = createPageContexts(createPages(numberOfPages));
      
      long start = System.currentTimeMillis();
      createAndSavePages(pageMap);
      sync(true);

      long end = System.currentTimeMillis();
      System.out.println("Time to SAVE " + numberOfPages + " pages : " + (end - start));
      site.destroy();
   }
   
   @Test
   public void testLoadPage() throws Exception
   {
      Site site = mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "load_pages");
      site.getRootPage().addChild("pages");
      sync(true);

      //create and save the pages so we can test the time it takes to load them
      int numberOfPages = Integer.parseInt(System.getProperty("numberOfPages", "25"));
      Map<Page, PageContext> pageMap = createPageContexts(createPages(numberOfPages));
      createAndSavePages(pageMap);
      
      long start = System.currentTimeMillis();
      for (Page page: pageMap.keySet())
      {
         PageContext pageContext = service.loadPage(page.getPageKey());
         assertNotNull(pageContext);
         assertEquals(page.getPageKey(), pageContext.getKey());
         assertEquals(pageMap.get(page).getState(), pageContext.getState());
      }
      sync(true);

      long end = System.currentTimeMillis();
      System.out.println("Time to LOAD " + numberOfPages + " pages : " + (end - start));
      site.destroy();
   }
   
   @Test
   public void testDestroyPage() throws Exception
   {
      Site site = mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "destroy_pages");
      site.getRootPage().addChild("pages");
      sync(true);

      //create and save the pages so we can test the time it takes to destroy them
      int numberOfPages = Integer.parseInt(System.getProperty("numberOfPages", "25"));
      Map<Page, PageContext> pageMap = createPageContexts(createPages(numberOfPages));
      createAndSavePages(pageMap);
      
      long start = System.currentTimeMillis();
      for (Page page: pageMap.keySet())
      {
         boolean result = service.destroyPage(page.getPageKey());
         assertTrue(result);
      }
      sync(true);

      long end = System.currentTimeMillis();
      System.out.println("Time to DESTROY " + numberOfPages + " pages : " + (end - start));
      site.destroy();
   }
   
   protected void createAndSavePages(Map<Page, PageContext> pageMap) throws Exception
   {
      for (Page page: pageMap.keySet())
      {
         DataStorage ds = getComponent(DataStorage.class);
         service.savePage(pageMap.get(page));
         ds.save(page);
      }
   }

   protected List<Page> createPages(int numPages)
   {
      List<Page> pages = new ArrayList<Page>();
      SiteKey siteKey = SiteKey.portal("import_pages");
      for (int i = 0; i < numPages; i++)
      {
         PageKey pageKey = siteKey.page("page"+i);
         Page page = createPage(pageKey);
         pages.add(page);
      }
      return pages;
   }
   
   protected Map<Page, PageContext> createPageContexts(List<Page> pages)
   {
      Map<Page, PageContext> pageContexts = new HashMap<Page, PageContext>();
      for (Page page: pages)
      {
         PageContext pageContext = new PageContext(page.getPageKey(), PageUtils.toPageState(page));
         pageContexts.put(page, pageContext);
      }
      
      return pageContexts;
   }
   
   private <T> T getComponent(Class<T> type)
   {
      return type.cast(getContainer().getComponentInstanceOfType(type));
   }
   
   private static Page createPage(PageKey pageKey)
   {
      Portlet portlet = new Portlet();
      portlet.putPreference(new Preference("pref-1", "value-1", true));
      portlet.putPreference(new Preference("pref-2", "value-2", false));
      portlet.putPreference(new Preference("multi-value-pref", Arrays.asList("one", "two", "three"), false));
      portlet.putPreference(new Preference("empty-value-pref", (String) null, true));

      ApplicationState<Portlet> state = new TransientApplicationState<Portlet>("app-ref/portlet-ref", portlet);
      ApplicationData<Portlet> applicationData = new ApplicationData<Portlet>(null, null,
         ApplicationType.PORTLET, state, null, "app-title", "app-icon", "app-description", false, true, false,
         "app-theme", "app-wdith", "app-height", new HashMap<String,String>(),
         Collections.singletonList("app-edit-permissions"));

      ContainerData containerData = new ContainerData(null, "cd-id", "cd-name", "cd-icon", "cd-template", "cd-factoryId", "cd-title", "cd-description", "cd-width", "cd-height", Collections.singletonList("cd-access-permissions"), Collections.singletonList((ComponentData) applicationData));
      List<ComponentData> children = Collections.singletonList((ComponentData) containerData);

      org.exoplatform.portal.pom.data.PageData data = new org.exoplatform.portal.pom.data.PageData(null, null, pageKey.getName(), null, null, null, "Title", null, null, null,
         Collections.singletonList("access-permissions"), children, pageKey.getSite().getTypeName(), pageKey.getSite().getName(), "edit-permission", true);

      return new Page(data);
   }
}
