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

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.gatein.api.Properties;
import org.gatein.api.commons.Filter;
import org.gatein.api.commons.PropertyType;
import org.gatein.api.commons.Range;
import org.gatein.api.exception.ApiException;
import org.gatein.api.exception.EntityAlreadyExistsException;
import org.gatein.api.exception.EntityNotFoundException;
import org.gatein.api.impl.GateInImpl;
import org.gatein.api.portal.Label;
import org.gatein.api.portal.Navigation;
import org.gatein.api.portal.Page;
import org.gatein.api.portal.Site;
import org.gatein.api.security.SecurityRestriction;
import org.gatein.common.NotYetImplemented;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static org.gatein.common.util.ParameterValidation.throwIllegalArgExceptionIfNull;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@redhat.com">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 */
public class SiteImpl extends DataStorageContext implements Site
{
   public static final Map<Type, String> OWNER_MAP;

   static
   {
      Map<Type, String> owners = new HashMap<Type, String>();
      owners.put(Type.SITE, "portal");
      owners.put(Type.SPACE, "group");
      owners.put(Type.DASHBOARD, "user");
      OWNER_MAP = Collections.unmodifiableMap(owners);
   }

   /** . */
   private final Id id;
   private final Label label;
   private final GateInImpl gateIn;
   private final Query<org.exoplatform.portal.config.model.Page> pagesQuery;
   private Navigation navigation;

   public SiteImpl(Id id, GateInImpl gateIn)
   {
      super(gateIn.dataStorage);

      this.id = id;
      this.label = new SiteLabel(gateIn.getDescriptionService());
      this.gateIn = gateIn;
      this.pagesQuery = new Query<org.exoplatform.portal.config.model.Page>(
         OWNER_MAP.get(id.getType()), id.getName(), org.exoplatform.portal.config.model.Page.class);
   }

   @Override
   public Id getId()
   {
      return id;
   }

   @Override
   public String getName()
   {
      return getId().getName();
   }

   @Override
   public Type getType()
   {
      return getId().getType();
   }

   @Override
   public String toString()
   {
      return id.toString();
   }

   @Override
   public Label getLabel()
   {
      return label;
   }

   @Override
   public String getDescription()
   {
      return getInternalSite(true).getDescription();
   }

   @Override
   public void setDescription(final String description)
   {
      PortalConfig internalSite = getInternalSite(true);
      execute(internalSite, new Modify<PortalConfig>()
      {
         @Override
         public void modify(PortalConfig data, DataStorage dataStorage) throws Exception
         {
            data.setDescription(description);
            dataStorage.save(data);
         }
      });
   }

   @Override
   public Locale getLocale()
   {
      String lang = getInternalSite(true).getLocale();
      return (lang == null) ? null : new Locale(lang);
   }

   @Override
   public void setLocale(final Locale locale)
   {
      execute(getInternalSite(true), new Modify<PortalConfig>()
      {
         @Override
         public void modify(PortalConfig data, DataStorage dataStorage) throws Exception
         {
            data.setLocale(locale.getLanguage());
            dataStorage.save(data);
         }
      });
   }

   @Override
   public Navigation getNavigation(boolean create)
   {
      if (navigation == null)
      {
         SiteKey siteKey = getSiteKey();
         NavigationContext context = gateIn.getNavigationService().loadNavigation(siteKey);
         if (context == null && create)
         {
            context = new NavigationContext(siteKey, new NavigationState(1));
            gateIn.getNavigationService().saveNavigation(context);
         }

         if (context != null)
         {
            navigation = new NavigationImpl(this, gateIn, context);
         }
      }
      return navigation;
   }

   @Override
   public List<Page> getPages()
   {
      List<org.exoplatform.portal.config.model.Page> internalPages = query(pagesQuery);
      List<Page> pages = new ArrayList<Page>(internalPages.size());

      for (org.exoplatform.portal.config.model.Page internalPage : internalPages)
      {
         pages.add(new PageImpl(this, internalPage.getName()));
      }

      return pages;
   }

   public List<Page> getPages(Range range)
   {
      //TODO: implement range cut
      return getPages();
   }

   @Override
   public Page getPage(String pageName)
   {
      try
      {
         return new PageImpl(this, pageName).getPage();
      }
      catch (EntityNotFoundException ex)
      {
         return null;
      }
   }

   @Override
   public Page createPage(String pageName) throws EntityAlreadyExistsException
   {
      throwIllegalArgExceptionIfNull(pageName, "Page name");

      Page page = getPage(pageName);
      if (page != null)
      {
         throw new EntityAlreadyExistsException("Page already exists: " + pageName);
      }

      PortalConfig internalSite = getInternalSite(true);
      final org.exoplatform.portal.config.model.Page internalPage =
         new org.exoplatform.portal.config.model.Page(internalSite.getType(), internalSite.getName(), pageName);

      execute(internalPage, new Modify<org.exoplatform.portal.config.model.Page>()
      {
         @Override
         public void modify(org.exoplatform.portal.config.model.Page data, DataStorage dataStorage) throws Exception
         {
            dataStorage.create(internalPage);
         }
      });

      return new PageImpl(this, pageName).getPage();

   }

   @Override
   public void removePage(String pageName) throws EntityNotFoundException
   {
      new PageImpl(this, pageName).removePage();
   }

   @Override
   public List<Page> getPages(Filter<Page> filter)
   {
      List<Page> pages = getPages();
      for (Iterator<Page> iter = pages.iterator(); iter.hasNext(); )
      {
         boolean keep = filter.accept(iter.next());
         if (!keep)
         {
            iter.remove();
         }
      }

      return pages;
   }

   @Override
   public List<Page> getPages(Filter<Page> filter, Range range)
   {
      //TODO: Implement
      throw new NotYetImplemented();
   }

   @Override
   public SecurityRestriction getSecurityRestriction(SecurityRestriction.Type type)
   {
      //TODO: Implement
      throw new NotYetImplemented();
   }

   @Override
   public void updateSecurityRestriction(SecurityRestriction securityRestriction)
   {
      //TODO: Implement
      throw new NotYetImplemented();
   }

   @Override
   public boolean hasAccess(String user)
   {
      //TODO: Implement
      throw new NotYetImplemented();
   }

   @Override
   public <T> T getProperty(PropertyType<T> property)
   {
      final PortalConfig portalConfig = getInternalSite(true);
      Object propertyValue;
      if (Properties.SESSION_BEHAVIOR.equals(property))
      {
         propertyValue = portalConfig.getSessionAlive();
      }
      else if (Properties.SHOW_PORTLET_INFO_BAR.equals(property))
      {
         propertyValue = portalConfig.isShowInfobar();
      }
      else
      {
         return null;
      }

      return property.getValueType().cast(propertyValue);
   }

   @Override
   public <T> void setProperty(PropertyType<T> property, T value)
   {
      throwIllegalArgExceptionIfNull(property, "property");
      throwIllegalArgExceptionIfNull(value, "value");

      final PortalConfig portalConfig = getInternalSite(true);

      if (Properties.SESSION_BEHAVIOR.equals(property))
      {
         portalConfig.setSessionAlive((String)value);
      }
      else if (Properties.SHOW_PORTLET_INFO_BAR.equals(property))
      {
         portalConfig.setShowInfobar((Boolean)value);
      }
      else
      {
         throw new IllegalArgumentException("Unkown property:" + property.getName());
      }
   }

   // Ensures the site exists. Useful to create a simple impl and call this method which handles errors and if site is not found.
   public Site getSite()
   {
      getInternalSite(true);
      return this;
   }

   public void removeSite()
   {
      PortalConfig internalSite = getInternalSite(true);
      execute(internalSite, new Modify<PortalConfig>()
      {
         @Override
         public void modify(PortalConfig data, DataStorage dataStorage) throws Exception
         {
            dataStorage.remove(data);
         }
      });
   }

   public Site addSite()
   {
      PortalConfig internalSite = getInternalSite(false);
      if (internalSite != null)
      {
         throw new ApiException("Cannot add site, site already exists for id " + id);
      }

      //TODO: Need to determine what good default values are when creating a site.
      SiteKey siteKey = getSiteKey();
      PortalConfig newSite = new PortalConfig(siteKey.getTypeName(), siteKey.getName());
      newSite.setLabel(siteKey.getName());

      if (id.getType() == Type.SITE)
      {
         newSite.setAccessPermissions(new String[]{UserACL.EVERYONE});
      }

      execute(newSite, new Modify<PortalConfig>()
      {
         @Override
         public void modify(PortalConfig data, DataStorage dataStorage) throws Exception
         {
            dataStorage.create(data);
         }
      });

      // Create navigation context for new site
      NavigationContext navContext = new NavigationContext(this.getSiteKey(), new NavigationState(1));
      gateIn.getNavigationService().saveNavigation(navContext);

      return this;
   }

   public PortalConfig getInternalSite(boolean required)
   {
      final SiteKey siteKey = getSiteKey();

      PortalConfig pc = execute(new Read<PortalConfig>()
      {
         @Override
         public PortalConfig read(DataStorage dataStorage) throws Exception
         {
            return dataStorage.getPortalConfig(siteKey.getTypeName(), siteKey.getName());
         }
      });
      if (pc == null && required)
      {
         throw new EntityNotFoundException("Site not found for id " + id);
      }

      return pc;
   }

   SiteKey getSiteKey()
   {
      return toSiteKey(id);
   }

   public static SiteKey toSiteKey(Id id)
   {
      switch (id.getType())
      {
         case SITE:
            return SiteKey.portal(id.getName());
         case SPACE:
            return SiteKey.group(id.getName());
         case DASHBOARD:
            return SiteKey.user(id.getName());
      }

      throw new RuntimeException(id.getType() + " is not recognized as a valid site type");
   }

   public static Id fromSiteKey(SiteKey siteKey)
   {
      switch (siteKey.getType())
      {
         case PORTAL:
            return Id.create(Type.SITE, siteKey.getName());
         case GROUP:
            return Id.create(Type.SPACE, siteKey.getName());
         case USER:
            return Id.create(Type.DASHBOARD, siteKey.getName());
         default:
            throw new RuntimeException(siteKey.getType() + " cannot be mapped to proper site type.");
      }
   }

//   class PageContainer
//   {
//
//      /** . */
//      private final Query<PageData> pageDataQuery;
//
//      /** A local cache so we return the same pages. */
//      private Map<PageKey, PageImpl> cache;
//
//      private PageContainer()
//      {
//         this.pageDataQuery = new Query<PageData>(OWNER_MAP.get(getId().getType()), null, PageData.class);
//         this.cache = EMPTY_CACHE;
//      }
//
//      PageImpl getPageByRef(String pageRef)
//      {
//         Matcher m = PAGE_REF_PATTERN.matcher(pageRef);
//         m.matches();
//         PageKey key = new PageKey(m.group(1), m.group(2), m.group(3));
//         return getPageData(key);
//      }
//
//      PageImpl getPageByName(String name)
//      {
//         SiteKey siteKey = getMOPSiteKey();
//         PageKey key = new PageKey(siteKey.getTypeName(), siteKey.getName(), name);
//         return getPageData(key);
//      }
//
//      private PageImpl getPageData(PageKey key)
//      {
//         PageImpl page = cache.get(key);
//         if (page == null)
//         {
//            try
//            {
//               gateIn.begin();
//               PageData data = gateIn.getDataStorage().getPage(key.getId());
//               if (data != null)
//               {
//                  page = new PageImpl(data, getId(), gateIn);
//                  if (cache == EMPTY_CACHE)
//                  {
//                     cache = new HashMap<PageKey, PageImpl>();
//                  }
//                  cache.put(key, page);
//               }
//            }
//            catch (Exception e)
//            {
//               throw new UndeclaredThrowableException(e);
//            }
//            finally
//            {
//               gateIn.end();
//            }
//         }
//         return page;
//      }
//   }

   private class SiteLabel extends AbstractLabel
   {

      public SiteLabel(DescriptionService service)
      {
         super(service);
      }

      @Override
      public String getDescriptionId()
      {
         return null;
      }

      @Override
      public String getSimpleValue()
      {
         return getInternalSite(true).getLabel();
      }

      @Override
      public String getDefaultName()
      {
         return getInternalSite(true).getName();
      }

      @Override
      public void setValue(final String value)
      {
         execute(getInternalSite(true), new Modify<PortalConfig>()
         {
            @Override
            public void modify(PortalConfig data, DataStorage dataStorage) throws Exception
            {
               data.setLabel(value);
               dataStorage.save(data);
            }
         });
      }

      @Override
      public ResourceBundle getResourceBundle()
      {
         return null;
      }

      @Override
      public Locale getUserLocale()
      {
         return null;
      }

      @Override
      public Locale getPortalLocale()
      {
         return null;
      }
   }
}
