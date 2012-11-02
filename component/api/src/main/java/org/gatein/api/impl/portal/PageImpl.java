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

package org.gatein.api.impl.portal;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@redhat.com">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 */
public class PageImpl// extends DataStorageContext implements Page
{
//   private final Id id;
//   private final String internalId;
//   private final SiteImpl site;
//
//   public PageImpl(SiteImpl site, String pageName)
//   {
//      super(site.dataStorage);
//
//      this.site = site;
//      this.id = Page.Id.create(site.getId(), pageName);
//      SiteKey siteKey = site.getSiteKey();
//      internalId = new PageKey(siteKey.getTypeName(), siteKey.getName(), pageName).getCompositeId();
//   }
//
//   @Override
//   public Site getSite()
//   {
//      return site.getSite();
//   }
//
//   @Override
//   public String getTitle()
//   {
//      return getInternalPage(true).getTitle();
//   }
//
//   @Override
//   public Id getId()
//   {
//      return id;
//   }
//
//   @Override
//   public String getName()
//   {
//      return id.getPageName();
//   }
//
//   @Override
//   public void setTitle(final String title)
//   {
//      org.exoplatform.portal.config.model.Page page = getInternalPage(true);
//
//      execute(page, new PageModify()
//      {
//         @Override
//         public void modifyPage(org.exoplatform.portal.config.model.Page page, DataStorage dataStorage) throws Exception
//         {
//            page.setTitle(title);
//         }
//      });
//   }
//
//   @Override
//   public SecurityRestriction getSecurityRestriction(SecurityRestriction.Type type)
//   {
//      SecurityRestriction restriction = null;
//      switch (type)
//      {
//         case EDIT:
//            String permission = getInternalPage(true).getEditPermission();
//            restriction = SecurityRestrictionUtils.forPermission(SecurityRestriction.Type.EDIT, permission);
//            break;
//         case ACCESS:
//            String[] permissions = getInternalPage(true).getAccessPermissions();
//            if (permissions != null)
//            {
//               restriction = SecurityRestrictionUtils.forPermissions(SecurityRestriction.Type.ACCESS, Arrays.asList(permissions));
//            }
//            break;
//         default:
//            throw new IllegalArgumentException("Unsupported SecurityRestriction type " + type);
//      }
//
//      return restriction;
//   }
//
//   @Override
//   public void updateSecurityRestriction(SecurityRestriction securityRestriction)
//   {
//      //TODO: Implement
//      throw new NotYetImplemented();
//   }
//
//   @Override
//   public boolean hasAccess(String user)
//   {
//      //TODO: Implement
//      throw new NotYetImplemented();
//   }
//
//   @Override
//   public String toString()
//   {
//      return id.toString();
//   }
//
//   // Ensures the page exists. Useful to create a simple impl and call this method which handles errors and if page is not found.
//   Page getPage()
//   {
//      getInternalPage(true);
//      return this;
//   }
//
//   void removePage()
//   {
//      org.exoplatform.portal.config.model.Page page = getInternalPage(true);
//      execute(page, new Modify<org.exoplatform.portal.config.model.Page>()
//      {
//         @Override
//         public void modify(org.exoplatform.portal.config.model.Page data, DataStorage dataStorage) throws Exception
//         {
//            dataStorage.remove(data);
//         }
//      });
//   }
//
//   private org.exoplatform.portal.config.model.Page getInternalPage(boolean required)
//   {
//      org.exoplatform.portal.config.model.Page page = execute(new Read<org.exoplatform.portal.config.model.Page>()
//      {
//         @Override
//         public org.exoplatform.portal.config.model.Page read(DataStorage dataStorage) throws Exception
//         {
//            return dataStorage.getPage(internalId);
//         }
//      });
//
//      if (page == null && required) throw new EntityNotFoundException("Page not found for id " + id);
//
//      return page;
//   }
//
//   private static abstract class PageModify implements Modify<org.exoplatform.portal.config.model.Page>
//   {
//      @Override
//      public final void modify(org.exoplatform.portal.config.model.Page page, DataStorage dataStorage) throws Exception
//      {
//         modifyPage(page, dataStorage);
//         dataStorage.save(page);
//      }
//
//      abstract void modifyPage(org.exoplatform.portal.config.model.Page page, DataStorage dataStorage) throws Exception;
//   }
//
//   private static Page.Id fromPageId(String pageId)
//   {
//      PageKey pageKey = PageKey.create(pageId);
//      SiteKey siteKey = new SiteKey(pageKey.getType(), pageKey.getId());
//      Site.Id siteId = SiteImpl.fromSiteKey(siteKey);
//
//      return Page.Id.create(siteId, pageKey.getName());
//   }
}
