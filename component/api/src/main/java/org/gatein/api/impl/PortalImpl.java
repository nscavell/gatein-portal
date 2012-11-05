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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.resources.ResourceBundleManager;
import org.gatein.api.Portal;
import org.gatein.api.impl.portal.DataStorageContext;
import org.gatein.api.impl.portal.navigation.NavigationServiceContext;
import org.gatein.api.portal.Label;
import org.gatein.api.portal.Permission;
import org.gatein.api.portal.User;
import org.gatein.api.portal.navigation.Navigation;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodeAccessor;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.portal.navigation.NodeVisitor;
import org.gatein.api.portal.page.Page;
import org.gatein.api.portal.page.PageId;
import org.gatein.api.portal.page.PageQuery;
import org.gatein.api.portal.site.Site;
import org.gatein.api.portal.site.SiteId;
import org.gatein.api.portal.site.SiteQuery;
import org.gatein.api.portal.site.SiteType;
import org.gatein.api.util.Filter;
import org.gatein.api.util.Pagination;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@redhat.com">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class PortalImpl extends DataStorageContext implements Portal, Startable
{
   private static final Query<PortalConfig> SITES = new Query<PortalConfig>(org.exoplatform.portal.mop.SiteType.PORTAL.getName(), null, PortalConfig.class);
   private static final Query<PortalConfig> SPACES = new Query<PortalConfig>(org.exoplatform.portal.mop.SiteType.GROUP.getName(), null, PortalConfig.class);
   private static final Query<PortalConfig> DASHBOARDS = new Query<PortalConfig>(org.exoplatform.portal.mop.SiteType.USER.getName(), null, PortalConfig.class);

   //TODO: Do we want a better name for loggeer ? Probably need to standardize our logging for api
   static final Logger log = LoggerFactory.getLogger(PortalImpl.class);

   //TODO: should be configurable
   public SiteId DEFAULT_SITE = new SiteId("classic");

   private final NavigationService navigationService;
   private final DescriptionService descriptionService;
   private final OrganizationService organizationService;
   private final ResourceBundleManager bundleManager;

   public PortalImpl(DataStorage dataStorage, NavigationService navigationService, DescriptionService descriptionService, OrganizationService organizationService, ResourceBundleManager bundleManager)
   {
      super(dataStorage);
      this.navigationService = navigationService;
      this.descriptionService = descriptionService;
      this.organizationService = organizationService;
      this.bundleManager = bundleManager;
   }

   @Override
   public Site getSite(SiteId siteId)
   {
      if (siteId == null) throw new IllegalArgumentException("siteId cannot be null");

      final SiteKey siteKey = Util.from(siteId);
      PortalConfig pc = execute(new Read<PortalConfig>()
      {
         @Override
         public PortalConfig read(DataStorage dataStorage) throws Exception
         {
            return dataStorage.getPortalConfig(siteKey.getTypeName(), siteKey.getName());
         }
      });

      return Util.from(pc);
   }

   @Override
   public List<Site> findSites(SiteQuery query)
   {
      Pagination pagination = query.getPagination();
      if (pagination != null && query.getSiteTypes().size() > 1)
      {
         pagination = null; // set it to null so the internal DataStorageContext.find method doesn't use it, and we manually page later.
         log.warn("Pagination is not supported internally for SiteQuery's with multiple site types. Therefore this query has the possibility to perform poorly.");
      }

      List<Site> sites = new ArrayList<Site>();
      for (SiteType type : query.getSiteTypes())
      {
         List<PortalConfig> internalSites;
         switch (type)
         {
            case SITE:
               internalSites = find(pagination, SITES, Comparators.site(query.getSorting()));
               break;
            case SPACE:
               internalSites = find(pagination, SPACES, Comparators.site(query.getSorting()));
               break;
            case DASHBOARD:
               internalSites = find(pagination, DASHBOARDS, Comparators.site(query.getSorting()));
               break;
            default:
               throw new AssertionError();
         }

         sites.addAll(fromList(internalSites));
      }

      filter(sites, query.getFilter());

      // Manually do paging for multiple site types.
      if (query.getSiteTypes().size() > 1)
      {
         sites = page(sites, query.getPagination());
      }

      return sites;
   }

   @Override
   public void saveSite(Site site)
   {
      execute(Util.from(site), new Modify<PortalConfig>()
      {
         @Override
         public void modify(PortalConfig data, DataStorage dataStorage) throws Exception
         {
            if (dataStorage.getPortalConfig(data.getType(), data.getName()) != null)
            {
               dataStorage.save(data);
            }
            else
            {
               dataStorage.create(data);
            }
         }
      });
   }

   @Override
   public void removeSite(SiteId siteId)
   {
      SiteKey siteKey = Util.from(siteId);

      execute(new PortalConfig(siteKey.getTypeName(), siteKey.getName()), new Modify<PortalConfig>()
      {
         @Override
         public void modify(PortalConfig data, DataStorage dataStorage) throws Exception
         {
            dataStorage.remove(data);
         }
      });
   }

   @Override
   public Navigation getNavigation(SiteId siteId, NodeVisitor visitor, Filter<Node> filter)
   {
      NavigationServiceContext ctx = new NavigationServiceContext(navigationService, descriptionService, siteId);
      ctx.setScope(visitor);
      ctx.init();

      Navigation navigation = ctx.getNavigation();
      if (navigation != null && filter != null)
      {
         filter(navigation.getChildren(), filter);
      }
      return navigation;
   }

   @Override
   public void saveNavigation(Navigation navigation)
   {
      NavigationServiceContext ctx = new NavigationServiceContext(navigationService, descriptionService, navigation.getSiteId());
      ctx.setScope(NodeAccessor.getRootNode(navigation));
      ctx.init();

      ctx.saveNavigation(navigation);
   }

   @Override
   public Node getNode(SiteId siteId, NodePath nodePath)
   {
      NavigationServiceContext ctx = new NavigationServiceContext(navigationService, descriptionService, siteId);
      ctx.setScope(nodePath);
      ctx.init();

      return ctx.getNode(nodePath);
   }

   @Override
   public Node getNode(SiteId siteId, NodeVisitor visitor, Filter<Node> filter)
   {
      NavigationServiceContext ctx = new NavigationServiceContext(navigationService, descriptionService, siteId);
      ctx.setScope(visitor);
      ctx.init();

      Node rootNode = NodeAccessor.getRootNode(ctx.getNavigation());
      if (rootNode != null && filter != null)
      {
         filter(rootNode.getChildren(), filter);
      }
      return rootNode;
   }

   @Override
   public void loadNodes(Node parent, NodeVisitor visitor)
   {
      SiteId siteId = parent.getSiteId();
      NavigationServiceContext ctx = new NavigationServiceContext(navigationService, descriptionService, siteId);
      ctx.setScope(visitor);
      ctx.init();

      ctx.loadNodes(parent);
   }

   @Override
   public void saveNode(Node node)
   {
      SiteId siteId = node.getSiteId();
      NavigationServiceContext ctx = new NavigationServiceContext(navigationService, descriptionService, siteId);
      ctx.setScope(node);
      ctx.init();

      ctx.saveNode(node);
   }

   @Override
   public Label resolveLabel(Label label)
   {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

   @Override
   public Page getPage(PageId pageId)
   {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

   @Override
   public List<Page> findPages(PageQuery query)
   {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

   @Override
   public void savePage(Page page)
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   @Override
   public void removePage(PageId pageId)
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   @Override
   public boolean hasPermission(User user, Permission permission)
   {
      return false;  //To change body of implemented methods use File | Settings | File Templates.
   }

   private static <T> void filter(List<T> list, Filter<T> filter)
   {
      if (filter == null) return;

      for (Iterator<T> iterator = list.iterator(); iterator.hasNext(); )
      {
         if (!filter.accept(iterator.next()))
         {
            iterator.remove();
         }
      }
   }

   private static <T> List<T> page(List<T> list, Pagination pagination)
   {
      if (pagination == null) return list;
      if (pagination.getOffset() >= list.size()) return Collections.emptyList();

      if (pagination.getOffset() + pagination.getLimit() > list.size())
      {
         return new ArrayList<T>(list.subList(pagination.getOffset(), list.size()));
      }
      else
      {
         return new ArrayList<T>(list.subList(pagination.getOffset(), pagination.getOffset() + pagination.getLimit()));
      }
   }

   private static List<Site> fromList(List<PortalConfig> internalSites)
   {
      List<Site> sites = new ArrayList<Site>(internalSites.size());
      for (PortalConfig internalSite : internalSites)
      {
         sites.add(Util.from(internalSite));
      }
      return sites;
   }

//   public Locale getUserLocale()
//   {
//      //TODO: Workaround until RequestContext is sorted out in rest context
//      RequestContext rc = RequestContext.getCurrentInstance();
//      if (rc == null) return Locale.getDefault();
//
//      return rc.getLocale();
//   }

   // public ResourceBundle getNavigationResourceBundle(SiteId id)
//   {
//      SiteKey siteKey = Util.from(id);
//      return bundleManager.getNavigationResourceBundle(getUserLocale().getLanguage(), siteKey.getTypeName(), siteKey.getName());
//   }

   public NavigationService getNavigationService()
   {
      return navigationService;
   }

   public DescriptionService getDescriptionService()
   {
      return descriptionService;
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
}
