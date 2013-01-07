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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.QueryResult;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.page.PageServiceImpl;
import org.exoplatform.portal.mop.page.PageServiceWrapper;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.services.resources.ResourceBundleManager;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.security.IdentityRegistry;
import org.gatein.api.common.Filter;
import org.gatein.api.common.Pagination;
import org.gatein.api.internal.StringJoiner;
import org.gatein.api.navigation.Navigation;
import org.gatein.api.navigation.NavigationImpl;
import org.gatein.api.page.Page;
import org.gatein.api.page.PageId;
import org.gatein.api.page.PageImpl;
import org.gatein.api.page.PageQuery;
import org.gatein.api.security.Permission;
import org.gatein.api.security.User;
import org.gatein.api.site.Site;
import org.gatein.api.site.SiteId;
import org.gatein.api.site.SiteQuery;
import org.gatein.api.site.SiteType;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@redhat.com">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class PortalImpl extends DataStorageContext implements Portal {
    private static final Query<PortalConfig> SITES = new Query<PortalConfig>(
            org.exoplatform.portal.mop.SiteType.PORTAL.getName(), null, PortalConfig.class);
    private static final Query<PortalConfig> SPACES = new Query<PortalConfig>(
            org.exoplatform.portal.mop.SiteType.GROUP.getName(), null, PortalConfig.class);
    private static final Query<PortalConfig> DASHBOARDS = new Query<PortalConfig>(
            org.exoplatform.portal.mop.SiteType.USER.getName(), null, PortalConfig.class);

    static final Logger log = LoggerFactory.getLogger("org.gatein.api");

    // TODO: should be configurable
    public SiteId DEFAULT_SITE = new SiteId("classic");

    private final PageService pageService;
    private final NavigationService navigationService;
    private final DescriptionService descriptionService;
    private final ResourceBundleManager bundleManager;
    private UserACL acl;
    private Authenticator authenticator;
    private IdentityRegistry identityRegistry;

    public PortalImpl(DataStorage dataStorage, PageService pageService, NavigationService navigationService,
            DescriptionService descriptionService, ResourceBundleManager bundleManager, Authenticator authenticator,
            IdentityRegistry identityRegistry, UserACL acl) {
        super(dataStorage);
        this.pageService = pageService;
        this.navigationService = navigationService;
        this.descriptionService = descriptionService;
        this.bundleManager = bundleManager;
        this.authenticator = authenticator;
        this.identityRegistry = identityRegistry;
        this.acl = acl;
    }

    @Override
    public Site getSite(SiteId siteId) {
        Parameters.requireNonNull(siteId, "siteId");

        final SiteKey siteKey = Util.from(siteId);
        PortalConfig pc = execute(new Read<PortalConfig>() {
            @Override
            public PortalConfig read(DataStorage dataStorage) throws Exception {
                return dataStorage.getPortalConfig(siteKey.getTypeName(), siteKey.getName());
            }
        });

        return Util.from(pc);
    }

    @Override
    public List<Site> findSites(SiteQuery query) {
        Pagination pagination = query.getPagination();
        if (pagination != null && query.getSiteTypes().size() > 1) {
            pagination = null; // set it to null so the internal DataStorageContext.find method doesn't use it, and we manually
                               // page later.
            log.warn("Pagination is not supported internally for SiteQuery's with multiple site types. Therefore this query has the possibility to perform poorly.");
        }

        List<Site> sites = new ArrayList<Site>();
        for (SiteType type : query.getSiteTypes()) {
            List<PortalConfig> internalSites;
            switch (type) {
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

            sites.addAll(fromList(internalSites, navigationService, query.isIncludeEmptySites()));
        }

        filter(sites, query.getFilter());

        // Manually do paging for multiple site types.
        if (query.getSiteTypes().size() > 1) {
            sites = paginate(sites, query.getPagination());
        }

        return sites;
    }

    @Override
    public void saveSite(Site site) {
        execute(Util.from(site), new Modify<PortalConfig>() {
            @Override
            public void modify(PortalConfig data, DataStorage dataStorage) throws Exception {
                if (dataStorage.getPortalConfig(data.getType(), data.getName()) != null) {
                    dataStorage.save(data);
                } else {
                    dataStorage.create(data);
                }
            }
        });
    }

    @Override
    public boolean removeSite(SiteId siteId) {
        SiteKey siteKey = Util.from(siteId);

        execute(new PortalConfig(siteKey.getTypeName(), siteKey.getName()), new Modify<PortalConfig>() {
            @Override
            public void modify(PortalConfig data, DataStorage dataStorage) throws Exception {
                dataStorage.remove(data);
            }
        });

        return true;
    }

    @Override
    public Navigation getNavigation(SiteId siteId) {
        return new NavigationImpl(siteId, navigationService, descriptionService, bundleManager);
    }

    @Override
    public Page getPage(PageId pageId) {
        Parameters.requireNonNull(pageId, "pageId");

        PageContext context = pageService.loadPage(Util.from(pageId));
        return (context == null) ? null : new PageImpl(context);
    }

    @Override
    public Page createPage(PageId pageId) throws EntityAlreadyExistsException {
        if (getPage(pageId) != null) {
            throw new EntityAlreadyExistsException("Cannot create page. Page " + pageId + " already exists.");
        }

        // TODO: Provide valid defaults for creating a page i.e. permissions, etc.
        // TODO: Do we want to support page template ?
        // TODO: We can also ask for more information in API during page creation.

        Permission access = Permission.everyone();
        Permission edit = Permission.any("platform", "administrators");
        PageState pageState = new PageState(pageId.getPageName(), null, false, null, Arrays.asList(Util.from(access)),
                Util.from(edit)[0]);

        return new PageImpl(new PageContext(Util.from(pageId), pageState));
    }

    @Override
    public List<Page> findPages(PageQuery query) {
        Pagination pagination = query.getPagination();
        Iterator<PageContext> iterator;
        if (pagination == null) {
            if (query.getSiteType() == null || query.getSiteName() == null)
                throw new IllegalArgumentException("Pagination is required when site type or site name is null.");

            SiteKey siteKey = Util.from(new SiteId(query.getSiteType(), query.getSiteName()));
            if (pageService instanceof PageServiceImpl) {
                iterator = ((PageServiceImpl) pageService).loadPages(siteKey).iterator();
            } else if (pageService instanceof PageServiceWrapper) {
                iterator = ((PageServiceWrapper) pageService).loadPages(siteKey).iterator();
            } else {
                throw new RuntimeException("Unable to retrieve all pages for " + siteKey);
            }
        } else {
            QueryResult<PageContext> result = pageService.findPages(pagination.getOffset(), pagination.getLimit(),
                    Util.from(query.getSiteType()), query.getSiteName(), null, query.getDisplayName());

            iterator = result.iterator();
        }

        List<Page> pages = new ArrayList<Page>();
        while (iterator.hasNext()) {
            pages.add(new PageImpl(iterator.next()));
        }

        filter(pages, query.getFilter());

        return pages;
    }

    @Override
    public void savePage(Page page) {
        PageContext context = ((PageImpl) page).getPageContext();
        pageService.savePage(context);
    }

    @Override
    public boolean removePage(PageId pageId) {
        return pageService.destroyPage(Util.from(pageId));
    }

    @Override
    public boolean hasPermission(User user, Permission permission) {
        String expPerm = StringJoiner.joiner(",").join(Util.from(permission));

        Identity identity;
        if (user == User.anonymous()) {
            identity = new Identity(IdentityConstants.ANONIM);
        } else {
            identity = identityRegistry.getIdentity(user.getId());
        }

        if (identity == null) {
            try {
                identity = authenticator.createIdentity(user.getId());
            } catch (Exception e) {
                throw new ApiException("Failed to retrive user identity", e);
            }

            if (identity == null) {
                throw new EntityNotFoundException("User not found");
            }

            identityRegistry.register(identity);
        }

        return acl.hasPermission(identity, expPerm);
    }

    private static <T> void filter(List<T> list, Filter<T> filter) {
        if (filter == null)
            return;

        for (Iterator<T> iterator = list.iterator(); iterator.hasNext();) {
            if (!filter.accept(iterator.next())) {
                iterator.remove();
            }
        }
    }

    private static <T> List<T> paginate(List<T> list, Pagination pagination) {
        if (pagination == null)
            return list;
        if (pagination.getOffset() >= list.size())
            return Collections.emptyList();

        if (pagination.getOffset() + pagination.getLimit() > list.size()) {
            return new ArrayList<T>(list.subList(pagination.getOffset(), list.size()));
        } else {
            return new ArrayList<T>(list.subList(pagination.getOffset(), pagination.getOffset() + pagination.getLimit()));
        }
    }

    private static List<Site> fromList(List<PortalConfig> internalSites, NavigationService service, boolean includeAllSites) {
        List<Site> sites = new ArrayList<Site>(internalSites.size());
        for (PortalConfig internalSite : internalSites) {
            NavigationContext ctx = null;
            if (!includeAllSites) {
                ctx = service.loadNavigation(new SiteKey(internalSite.getType(), internalSite.getName()));
            }

            if (includeAllSites || ctx != null) {
                sites.add(Util.from(internalSite));
            }
        }
        return sites;
    }
}
