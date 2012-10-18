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
import org.exoplatform.portal.mop.importer.ImportMode;
import org.exoplatform.portal.mop.management.exportimport.PageImportTask;
import org.exoplatform.portal.mop.management.operations.MOPSiteProvider;
import org.exoplatform.portal.mop.management.operations.Utils;
import org.exoplatform.portal.mop.management.operations.page.PageUtils;
import org.exoplatform.portal.pom.data.ApplicationData;
import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.ContainerData;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.Preference;
import org.gatein.mop.api.workspace.Site;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class TestPageImport2 extends AbstractTestPageService
{
   public void disable_testPageSave() throws Exception
   {
      final DataStorage ds = getComponent(DataStorage.class);
      final SiteKey siteKey = SiteKey.portal("import_pages");
      createSite(siteKey);
      final int numberOfPages = Integer.parseInt(System.getProperty("numberOfPages", "25"));
      timeIt("Saving site", new TimedTask()
      {
         @Override
         public void execute() throws Exception
         {
            ds.save();
         }
      });

      timeIt("Saving " + numberOfPages + " pages", new TimedTask()
      {
         @Override
         public void execute() throws Exception
         {
            for (int i = 0; i < numberOfPages; i++)
            {
               PageKey pageKey = siteKey.page("page" + i);
               Page page = createPage(pageKey);
               service.savePage(new PageContext(pageKey, PageUtils.toPageState(page)));
               ds.save(page);
            }
         }
      });

      timeIt("Saving session", new TimedTask()
      {
         @Override
         public void execute() throws Exception
         {
            ds.save();
         }
      });
   }

   public void testPageDelete() throws Exception
   {
      final DataStorage ds = getComponent(DataStorage.class);
      final SiteKey siteKey = SiteKey.portal("import_pages");
      createSite(siteKey);
      final int numberOfPages = Integer.parseInt(System.getProperty("numberOfPages", "25"));
      timeIt("Saving site", new TimedTask()
      {
         @Override
         public void execute() throws Exception
         {
            ds.save();
         }
      });

      timeIt("Saving " + numberOfPages + " pages", new TimedTask()
      {
         @Override
         public void execute() throws Exception
         {
            for (int i = 0; i < numberOfPages; i++)
            {
               PageKey pageKey = siteKey.page("page" + i);
               Page page = createPage(pageKey);
               service.savePage(new PageContext(pageKey, PageUtils.toPageState(page)));
               ds.save(page);
            }
         }
      });

      timeIt("Saving session", new TimedTask()
      {
         @Override
         public void execute() throws Exception
         {
            ds.save();
         }
      });

      service.clearCache();

      timeIt("Deleting " + numberOfPages + " pages", new TimedTask()
      {
         @Override
         public void execute() throws Exception
         {
            for (int i = 0; i < numberOfPages; i++)
            {
               service.destroyPage(siteKey.page("page" + i));
            }
         }
      });

      timeIt("Saving session", new TimedTask()
      {
         @Override
         public void execute() throws Exception
         {
            ds.save();
         }
      });
   }

   public void disable_testMergeImport() throws Exception
   {
      final DataStorage ds = getComponent(DataStorage.class);
      final SiteKey siteKey = SiteKey.portal("import_pages");
      createSite(siteKey);
      final int numberOfPages = Integer.parseInt(System.getProperty("numberOfPages", "25"));
      final int preload = numberOfPages / 2;
      timeIt(preload + " preloaded pages", new TimedTask()
      {
         @Override
         public void execute() throws Exception
         {
            for (int i = 0; i < preload; i++)
            {
               PageKey pageKey = siteKey.page("page" + i);
               Page page = createPage(pageKey);
               service.savePage(new PageContext(pageKey, PageUtils.toPageState(page)));
               ds.save(page);
            }
         }
      });
      timeIt("Preloaded pages sync(true)", new TimedTask()
      {
         @Override
         public void execute() throws Exception
         {
            sync(true);
         }
      });
      service.clearCache();

      Page.PageSet pages = new Page.PageSet();
      for (int i = 0; i < numberOfPages; i++)
      {
         pages.getPages().add(createPage(siteKey.page("page" + i)));
      }

      final PageImportTask importTask = new PageImportTask(pages, SiteKey.portal("import_pages"), ds, service, new MOPSiteProvider()
      {
         @Override
         public Site getSite(SiteKey siteKey)
         {
            return mgr.getPOMService().getModel().getWorkspace().getSite(Utils.getObjectType(siteKey.getType()), siteKey.getName());
         }
      });

      timeIt(numberOfPages + " pages imported (MERGE)", new TimedTask()
      {
         @Override
         public void execute() throws Exception
         {
            importTask.importData(ImportMode.MERGE);
         }
      });
   }

   public void disable_testOverwriteImport() throws Exception
   {
      final DataStorage ds = getComponent(DataStorage.class);
      final SiteKey siteKey = SiteKey.portal("import_pages");
      createSite(siteKey);
      final int numberOfPages = Integer.parseInt(System.getProperty("numberOfPages", "25"));
      final int preload = numberOfPages / 2;
      timeIt(preload + " preloaded pages", new TimedTask()
      {
         @Override
         public void execute() throws Exception
         {
            for (int i = 0; i < preload; i++)
            {
               PageKey pageKey = siteKey.page("page" + i);
               Page page = createPage(pageKey);
               service.savePage(new PageContext(pageKey, PageUtils.toPageState(page)));
               ds.save(page);
            }
         }
      });
      timeIt("Preloaded pages sync(true)", new TimedTask()
      {
         @Override
         public void execute() throws Exception
         {
            sync(true);
         }
      });
      service.clearCache();

      Page.PageSet pages = new Page.PageSet();
      for (int i = 0; i < numberOfPages; i++)
      {
         pages.getPages().add(createPage(siteKey.page("page" + i)));
      }

      final PageImportTask importTask = new PageImportTask(pages, SiteKey.portal("import_pages"), ds, service, new MOPSiteProvider()
      {
         @Override
         public Site getSite(SiteKey siteKey)
         {
            return mgr.getPOMService().getModel().getWorkspace().getSite(Utils.getObjectType(siteKey.getType()), siteKey.getName());
         }
      });

      timeIt(numberOfPages + " pages imported (OVERWRITE)", new TimedTask()
      {
         @Override
         public void execute() throws Exception
         {
            importTask.importData(ImportMode.OVERWRITE);
         }
      });
   }

   private <T> T getComponent(Class<T> type)
   {
      return type.cast(getContainer().getComponentInstanceOfType(type));
   }

   private void createSite(SiteKey siteKey)
   {
      Site site = mgr.getPOMService().getModel().getWorkspace().addSite(Utils.getObjectType(siteKey.getType()), siteKey.getName());
      site.getRootPage().addChild("pages");
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
         "app-theme", "app-wdith", "app-height", new HashMap<String, String>(),
         Collections.singletonList("app-edit-permissions"));

      ContainerData containerData = new ContainerData(null, "cd-id", "cd-name", "cd-icon", "cd-template", "cd-factoryId", "cd-title", "cd-description", "cd-width", "cd-height", Collections.singletonList("cd-access-permissions"), Collections.singletonList((ComponentData) applicationData));
      List<ComponentData> children = Collections.singletonList((ComponentData) containerData);

      org.exoplatform.portal.pom.data.PageData data = new org.exoplatform.portal.pom.data.PageData(null, null, pageKey.getName(), null, null, null, "Title", null, null, null,
         Collections.singletonList("access-permissions"), children, pageKey.getSite().getTypeName(), pageKey.getSite().getName(), "edit-permission", true);

      return new Page(data);
   }

   private static void timeIt(String taskName, TimedTask task) throws Exception
   {
      long start = System.currentTimeMillis();
      task.execute();
      long end = System.currentTimeMillis();
      System.out.println(taskName + " took " + (end - start) + " ms");
   }

   private static interface TimedTask
   {
      void execute() throws Exception;
   }
}
