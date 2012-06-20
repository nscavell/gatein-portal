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

package org.gatein.portal.api.impl;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.RequestContext;
import org.gatein.api.GateIn;
import org.gatein.api.commons.PropertyType;
import org.gatein.api.commons.Range;
import org.gatein.api.exception.EntityNotFoundException;
import org.gatein.api.portal.Site;
import org.gatein.api.portal.SiteQuery;
import org.gatein.common.NotYetImplemented;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.portal.api.impl.portal.DataStorageContext;
import org.gatein.portal.api.impl.portal.SiteImpl;
import org.gatein.portal.api.impl.portal.SiteQueryImpl;
import org.picocontainer.Startable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.gatein.common.util.ParameterValidation.*;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@redhat.com">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 */
public class GateInImpl extends DataStorageContext implements GateIn, Startable
{
   private static final Query<PortalConfig> ALL = new Query<PortalConfig>(null, null, PortalConfig.class);
   private static final Query<PortalConfig> SITES = new Query<PortalConfig>(SiteType.PORTAL.getName(), null, PortalConfig.class);
   private static final Query<PortalConfig> SPACES = new Query<PortalConfig>(SiteType.GROUP.getName(), null, PortalConfig.class);
   private static final Query<PortalConfig> DASHBOARDS = new Query<PortalConfig>(SiteType.USER.getName(), null, PortalConfig.class);

   //TODO: Do we want a better name for loggeer ? Probably need to standardize our logging for api
   static final Logger log = LoggerFactory.getLogger(GateInImpl.class);

   //TODO: should be configurable
   public Site.Id DEFAULT_SITE_KEY = Site.Id.create(Site.Type.SITE, "classic");

   private Map<PropertyType, Object> properties = new HashMap<PropertyType, Object>(7);

   private final NavigationService navigationService;
   private final OrganizationService organizationService;

   public GateInImpl(DataStorage dataStorage, NavigationService navigationService, OrganizationService organizationService)
   {
      super(dataStorage);
      this.navigationService = navigationService;
      this.organizationService = organizationService;
   }

   @Override
   public List<Site> getSites()
   {
      List<PortalConfig> list = new LinkedList<PortalConfig>();
      list.addAll(query(SITES));
      list.addAll(query(SPACES));
      list.addAll(query(DASHBOARDS));

      return fromList(list);
   }

   @Override
   public List<Site> getSites(Site.Type siteType)
   {
      throwIllegalArgExceptionIfNull(siteType, "Site.Type");

      switch (siteType)
      {
         case SITE:
            return fromList(query(SITES));
         case SPACE:
            return fromList(query(SPACES));
         case DASHBOARD:
            return fromList(query(DASHBOARDS));
         default:
            throw new IllegalArgumentException(siteType + " is not recognized as a valid site type.");
      }
   }

   @Override
   public List<Site> getSites(Range range)
   {
      throwIllegalArgExceptionIfNull(range, "Range");

      //TODO: true pagination
      return cutPageFromResults(getSites(), range);

   }

   @Override
   public Site getSite(Site.Id siteId)
   {
      throwIllegalArgExceptionIfNull(siteId, "Site.Id");

      try
      {
         return siteImpl(siteId).getSite();
      }
      catch (EntityNotFoundException ex)
      {
         return null;
      }
   }

   @Override
   public Site getSite(Site.Type type, String name)
   {
      throwIllegalArgExceptionIfNull(type, "Site.Type");
      throwIllegalArgExceptionIfNull(name, "name");

      return getSite(Site.Id.create(type, name));
   }

   @Override
   public List<Site> getSites(Site.Type siteType, Range range)
   {
      throwIllegalArgExceptionIfNull(siteType, "Site.Type");
      throwIllegalArgExceptionIfNull(range, "range");

      //TODO: true pagination
      return cutPageFromResults(getSites(siteType), range);
   }

   @Override
   public Site getDefaultSite()
   {
      //TODO: make this configurable on the service level with fallback to classic
      return getSite(DEFAULT_SITE_KEY);
   }

   @Override
   public SiteQuery<Site> createSiteQuery()
   {
      return new SiteQueryImpl(this);
   }

   @Override
   public Site addSite(Site.Type siteType, String name)
   {
      throwIllegalArgExceptionIfNull(siteType, "Site.Type");
      throwIllegalArgExceptionIfNull(name, "name");

      //TODO: check if user or group exist for DASHBOARD / SPACE

      return siteImpl(Site.Id.create(siteType, name)).addSite();
   }

   public Site addSite(Site.Id id)
   {
      throwIllegalArgExceptionIfNull(id, "Site ID");

      return addSite(id.getType(), id.getName());
   }

   @Override
   public void removeSite(Site.Id siteId) throws EntityNotFoundException
   {
      throwIllegalArgExceptionIfNull(siteId, "Site.Id");

      siteImpl(siteId).removeSite();
   }

   @Override
   public void removeSite(Site.Type siteType, String siteName) throws EntityNotFoundException
   {
      throwIllegalArgExceptionIfNull(siteType, "Site.Type");
      throwIllegalArgExceptionIfNull(siteName, "Site name");

      removeSite(Site.Id.create(siteType, siteName));
   }

   public <T> T getProperty(PropertyType<T> property)
   {
      if (property == null)
      {
         return null;
      }

      Class<T> propertyType = property.getValueType();
      Object o = properties.get(property);
      return propertyType.cast(o);
   }

   public <T> void setProperty(PropertyType<T> property, T value)
   {
      if (property != null)
      {
         properties.put(property, value);
      }
   }

   public NavigationService getNavigationService()
   {
      return navigationService;
   }

   private List<Site> fromList(List<PortalConfig> internalSites)
   {
      List<Site> sites = new ArrayList<Site>(internalSites.size());
      for (PortalConfig internalSite : internalSites)
      {
         sites.add(fromPortalConfig(internalSite));
      }
      return sites;
   }

   private Site fromPortalConfig(PortalConfig internalSite)
   {
      Site.Id siteId = SiteImpl.fromSiteKey(new SiteKey(internalSite.getType(), internalSite.getName()));
      return siteImpl(siteId);
   }

   private SiteImpl siteImpl(Site.Id siteId)
   {
      return new SiteImpl(siteId, this);
   }

//   private PortalConfig getPortalConfig(Site.Id siteId)
//   {
//      try
//      {
//         begin();
//
//         SiteKey siteKey = SiteImpl.createMOPSiteKey(siteId);
//
//         return getDataStorage().getPortalConfig(siteKey.getTypeName(), siteKey.getName());
//      }
//      catch (Exception e)
//      {
//         throw new UndeclaredThrowableException(e);
//      }
//      finally
//      {
//         end();
//      }
//   }
//
//   private void removePortalDataFor(Site.Id siteId) throws EntityNotFoundException
//   {
//      PortalKey key = SiteImpl.createPortalKey(siteId);
//      try
//      {
//         begin();
//         ModelDataStorage mds = getDataStorage();
//         PortalData data = mds.getPortalConfig(key);
//         if (data == null)
//         {
//            throw new EntityNotFoundException("Site " + siteId + " does not exist.");
//         }
//         mds.remove(data);
//      }
//      catch (EntityNotFoundException e)
//      {
//         throw e;
//      }
//      catch (Exception e)
//      {
//         throw new UndeclaredThrowableException(e);
//      }
//      finally
//      {
//         end();
//      }
//   }



   //TODO: helper until pagination is not built into layer below
   private List<Site> cutPageFromResults(List<Site> sites, Range range)
   {

      List<Site> results = new LinkedList<Site>();

      if (range.getLimit() == 0)
      {
         for (int i = range.getOffset(); i < sites.size(); i++)
         {
            if (i < sites.size())
            {
               results.add(sites.get(i));
            }
         }
      }
      else
      {
         for (int i = range.getOffset(); i < range.getOffset() + range.getLimit(); i++)
         {
            if (i < sites.size())
            {
               results.add(sites.get(i));
            }
         }
      }
      return results;
   }

   public Locale getUserLocale()
   {
      //TODO: Workaround until RequestContext is sorted out in rest context
      RequestContext rc = RequestContext.getCurrentInstance();
      if (rc == null) return Locale.getDefault();

      return rc.getLocale();
   }

   @Override
   public void start()
   {
      //nothing
   }

   @Override
   public void stop()
   {
      //nothing
   }

   public int getQueryResultCount(SiteQueryImpl query)
   {
      //TODO: performance
      return executeSiteQueryWithoutRange(query).size();
   }

   public List<Site> executeSiteQuery(SiteQueryImpl query)
   {
      List<Site> results = executeSiteQueryWithoutRange(query);

      if (query.getRange() != null)
      {
         //TODO: true pagination
         return cutPageFromResults(results, query.getRange());
      }

      return results;
   }

   List<Site> executeSiteQueryWithoutRange(SiteQueryImpl query)
   {
      List<PortalConfig> queryResults = new LinkedList<PortalConfig>();

      if(query.getId() != null)
      {
         queryResults.add(siteImpl(query.getId()).getInternalSite(false));
      }
      else if (query.getType() == null)
      {
         queryResults.addAll(query(SITES));
         queryResults.addAll(query(SPACES));
         queryResults.addAll(query(DASHBOARDS));
      }
      else
      {
         Query<PortalConfig> dataQuery = new Query<PortalConfig>(null, null, PortalConfig.class);

         dataQuery.setOwnerType(SiteImpl.OWNER_MAP.get(query.getType()));

         // In case of SITE type ownerships like query.getUserId() or query.getGroupId() are ignored.

         // In case of SPACE group owner can be specified
         if (query.getType() == Site.Type.SPACE && query.getGroupId() != null)
         {
            dataQuery.setOwnerId(query.getGroupId());
         }

         // In case of DASHBOARD just set user id.
         if (query.getType() == Site.Type.DASHBOARD)
         {
            dataQuery.setOwnerId(query.getUserId());

            //if query.getGroupId() is provided just ignore.
         }

         queryResults = query(dataQuery);


         // In case of SPACE it may need to be filtered by user memberships in groups
         if (query.getType() == Site.Type.SPACE &&
            query.getUserId() != null &&
            query.getGroupId() == null)
         {
            List<PortalConfig> newResults = new LinkedList<PortalConfig>();

            Set<String> groupIds = new HashSet<String>();

            // Obtain all group ids related to given user
            try
            {
               Collection<Group> groups = organizationService
                  .getGroupHandler().findGroupsOfUser(query.getUserId());

               for (Group group : groups)
               {
                  groupIds.add(group.getId());
               }
            }
            catch (Exception ex)
            {
               log.error("Failed to obtain groups of given user: " + query.getUserId(), ex);
            }

            // Filter out only related spaces
            for (PortalConfig pc : queryResults)
            {
               if (groupIds.contains(pc.getName()))
               {
                  newResults.add(pc);
               }
            }
            queryResults = newResults;
         }
      }



      // Convert to site list
      List<Site> results = fromList(queryResults);

      //Check contains navigations
      if (query.isContainNavigation())
      {
         List<Site> newResults = new LinkedList<Site>();
         for (Site site : results)
         {
            if (site.getNavigation() != null && site.getNavigation().iterator().hasNext())
            {
               newResults.add(site);
            }
         }
         results = newResults;
      }


      return results;
   }


}
