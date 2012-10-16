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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class TestPageImport extends AbstractTestPageService
{
   public void testImport() throws Exception
   {
      mgr.getPOMService().getModel().getWorkspace().addSite(ObjectType.PORTAL_SITE, "import_pages").getRootPage().addChild("pages");
      sync(true);

      SiteKey siteKey = SiteKey.portal("import_pages");
      int numberOfPages = Integer.parseInt(System.getProperty("numberOfPages", "100"));

      long start = System.currentTimeMillis();
      for (int i=0; i<numberOfPages; i++)
      {
         PageKey pageKey = siteKey.page("page"+i);
         Page page = createPage(pageKey);
         DataStorage ds = getComponent(DataStorage.class);

         service.savePage(new PageContext(pageKey, PageUtils.toPageState(page)));
         ds.save(page);
      }
      sync(true);

      long end = System.currentTimeMillis();
      System.out.println("Elapsed Time: " + (end - start));
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