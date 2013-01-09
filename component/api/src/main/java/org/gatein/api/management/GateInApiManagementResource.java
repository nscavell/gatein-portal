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

package org.gatein.api.management;

import java.util.List;
import java.util.Locale;

import org.gatein.api.BasicPortalRequest;
import org.gatein.api.EntityNotFoundException;
import org.gatein.api.Portal;
import org.gatein.api.PortalRequest;
import org.gatein.api.navigation.Navigation;
import org.gatein.api.navigation.Node;
import org.gatein.api.navigation.NodePath;
import org.gatein.api.navigation.Nodes;
import org.gatein.api.page.Page;
import org.gatein.api.page.PageQuery;
import org.gatein.api.security.Group;
import org.gatein.api.security.User;
import org.gatein.api.site.Site;
import org.gatein.api.site.SiteId;
import org.gatein.api.site.SiteQuery;
import org.gatein.api.site.SiteType;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.ManagedUser;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.annotations.Managed;
import org.gatein.management.api.annotations.ManagedAfter;
import org.gatein.management.api.annotations.ManagedBefore;
import org.gatein.management.api.annotations.ManagedContext;
import org.gatein.management.api.annotations.ManagedOperation;
import org.gatein.management.api.annotations.MappedPath;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.model.ModelList;
import org.gatein.management.api.model.ModelObject;
import org.gatein.management.api.model.ModelProvider;
import org.gatein.management.api.model.ModelReference;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.OperationNames;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@SuppressWarnings("unused")
@Managed(value = "api", description = "GateIn API Management Resource")
public class GateInApiManagementResource {
    private static final Logger log = LoggerFactory.getLogger("org.gatein.api.management");

    private static final SiteQuery SITE_QUERY = new SiteQuery.Builder().withSiteTypes(SiteType.SITE).build();
    private static final SiteQuery SPACE_QUERY = new SiteQuery.Builder().withSiteTypes(SiteType.SPACE).build();
    private static final SiteQuery DASHBOARD_QUERY = new SiteQuery.Builder().withSiteTypes(SiteType.DASHBOARD).build();

    private final Portal portal;

    @ManagedContext
    private final ModelProvider modelProvider; // gatein-management will set this field via reflection

    public GateInApiManagementResource(Portal portal) {
        this(portal, null);
    }

    // Constructor for testing to specify ModelProvider instead of gatein-management
    GateInApiManagementResource(Portal portal, ModelProvider modelProvider) {
        this.portal = portal;
        this.modelProvider = modelProvider;
    }

    @ManagedBefore
    public void before(@ManagedContext OperationContext context) {
        PortalRequest portalRequest = PortalRequest.getInstance();
        if (portalRequest == null) {
            setCurrentPortalRequest(context);
        }
    }

    @ManagedAfter
    public void after() {
        if (PortalRequest.getInstance() instanceof BasicPortalRequest) {
            BasicPortalRequest.setInstance(null);
        }
    }

    // ------------------------------------------------- Portal Sites --------------------------------------------------//
    @Managed("/sites")
    public ModelList getSites(@ManagedContext PathAddress address) {
        List<Site> sites = portal.findSites(SITE_QUERY);

        ModelList list = modelProvider.newModel(ModelList.class);
        populateModel(sites, list, address);

        return list;
    }

    @Managed("/sites/{site-name}")
    public ModelObject getSite(@MappedPath("site-name") String siteName, @ManagedContext PathAddress address) {
        SiteId id = new SiteId(siteName);

        ModelObject siteModel = modelProvider.newModel(ModelObject.class);
        populateModel(id, siteModel, address);

        return siteModel;
    }

    @Managed("/sites/{site-name}")
    @ManagedOperation(name = OperationNames.REMOVE_RESOURCE, description = "Removes the given site")
    public void removeSite(@MappedPath("site-name") String siteName) {
        SiteId id = new SiteId(siteName);
        try {
            portal.removeSite(id);
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new ResourceNotFoundException("Cannot remove site " + id + " because site does not exist.");
        }
    }

    @Managed("/sites/{site-name}/pages")
    public PageManagementResource getPages(@MappedPath("site-name") String siteName) {
        return new PageManagementResource(portal, modelProvider, new SiteId(siteName));
    }

    @Managed("/sites/{site-name}/navigation")
    public NavigationManagementResource getNavigation(@MappedPath("site-name") String siteName) {
        return new NavigationManagementResource(portal, modelProvider, new SiteId(siteName));
    }

    // --------------------------------------------- Group Sites (Spaces) ----------------------------------------------//
    @Managed("/spaces")
    public ModelList getSpaces(@ManagedContext PathAddress address) {
        List<Site> sites = portal.findSites(SPACE_QUERY);

        ModelList list = modelProvider.newModel(ModelList.class);
        populateModel(sites, list, address);

        return list;
    }

    @Managed("/spaces/{group-name: .*}")
    public ModelObject getSpace(@MappedPath("group-name") String groupName, @ManagedContext PathAddress address) {
        ModelObject siteModel = modelProvider.newModel(ModelObject.class);
        populateModel(new SiteId(new Group(groupName)), siteModel, address);

        return siteModel;
    }

    @Managed("/spaces/{group-name: .*}")
    @ManagedOperation(name = OperationNames.REMOVE_RESOURCE, description = "Removes the given space")
    public void removeSpace(@MappedPath("group-name") String groupName) {
        SiteId id = new SiteId(new Group(groupName));
        try {
            portal.removeSite(id);
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new ResourceNotFoundException("Could not remove site for " + id + " because site does not exist.");
        }
    }

    @Managed("/spaces/{group-name: .*}/pages")
    public PageManagementResource getSpacePages(@MappedPath("group-name") String groupName) {
        return new PageManagementResource(portal, modelProvider, new SiteId(new Group(groupName)));
    }

    @Managed("/spaces/{group-name: .*}/navigation")
    public NavigationManagementResource getSpaceNavigation(@MappedPath("group-name") String groupName) {
        return new NavigationManagementResource(portal, modelProvider, new SiteId(new Group(groupName)));
    }

    // -------------------------------------------- User Sites (Dashboard) ---------------------------------------------//
    @Managed("/dashboards")
    public ModelList getDashboards(@ManagedContext PathAddress address) {
        List<Site> sites = portal.findSites(DASHBOARD_QUERY);

        ModelList list = modelProvider.newModel(ModelList.class);
        populateModel(sites, list, address);

        return list;
    }

    @Managed("/dashboards/{user-name}")
    public ModelObject getDashboard(@MappedPath("user-name") String userName, @ManagedContext PathAddress address) {
        ModelObject siteModel = modelProvider.newModel(ModelObject.class);
        populateModel(new SiteId(new User(userName)), siteModel, address);

        return siteModel;
    }

    @Managed("/dashboards/{user-name}")
    @ManagedOperation(name = OperationNames.REMOVE_RESOURCE, description = "Removes the given dashboard")
    public void removeDashboard(@MappedPath("user-name") String userName) {
        SiteId id = new SiteId(new User(userName));
        try {
            portal.removeSite(id);
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            throw new ResourceNotFoundException("Cannot remove site " + id + " because site does not exist.");
        }
    }

    @Managed("/dashboards/{user-name}/pages")
    public PageManagementResource getDashboardPages(@MappedPath("user-name") String userName) {
        return new PageManagementResource(portal, modelProvider, new SiteId(new User(userName)));
    }

    @Managed("/dashboards/{user-name}/navigation")
    public NavigationManagementResource getDashboardNavigation(@MappedPath("user-name") String userName) {
        return new NavigationManagementResource(portal, modelProvider, new SiteId(new User(userName)));
    }

    static PathAddress getSiteAddress(SiteId siteId) {
        PathAddress address = PathAddress.pathAddress("api");
        switch (siteId.getType()) {
            case SITE:
                address = address.append("sites");
                break;
            case SPACE:
                address = address.append("spaces");
                break;
            case DASHBOARD:
                address = address.append("dashboards");
                break;
            default:
                throw new AssertionError();
        }

        return address.append(siteId.getName());
    }

    static PathAddress getPagesAddress(SiteId siteId) {
        return getSiteAddress(siteId).append("pages");
    }

    static PathAddress getNavigationAddress(SiteId siteId) {
        return getSiteAddress(siteId).append("navigation");
    }

    private Site getSite(SiteId id, boolean require) {
        Site site = portal.getSite(id);
        if (require && site == null)
            throw new ResourceNotFoundException("Site not found for " + id);

        return site;
    }

    private void populateModel(SiteId id, ModelObject siteModel, PathAddress address) {
        Site site = getSite(id, true);

        siteModel.set("name", site.getId().getName());
        siteModel.set("type", site.getId().getType().name().toLowerCase());

        // Pages
        ModelList pagesList = siteModel.get("pages", ModelList.class);
        List<Page> pages = portal.findPages(new PageQuery.Builder().withSiteId(id).build());
        for (Page page : pages) {
            ModelReference pageRef = pagesList.add().asValue(ModelReference.class);
            pageRef.set("name", page.getName());
            pageRef.set(address.append("pages").append(page.getName()));
        }

        // Navigation
        Navigation navigation = portal.getNavigation(id);
        Node node = navigation.getRootNode(Nodes.visitChildren());
        ModelList navList = siteModel.get("navigation", ModelList.class);
        for (Node child : node) {
            ModelReference navRef = navList.add().asValue(ModelReference.class);
            navRef.set("name", child.getName());
            navRef.set(address.append("navigation").append(child.getName()));
        }
    }

    private void populateModel(List<Site> sites, ModelList list, PathAddress address) {
        for (Site site : sites) {
            ModelReference siteRef = list.add().asValue(ModelReference.class);
            siteRef.set("name", site.getName());
            siteRef.set("type", site.getType().getName());
            siteRef.set(address.append(site.getName()));
        }
    }

    private void setCurrentPortalRequest(OperationContext context) {
        ManagedUser managedUser = context.getUser();
        PathAddress address = context.getAddress();
        SiteType siteType = null;
        StringBuilder sb = null;
        String siteName = null;
        NodePath nodePath = null;
        for (String segment : address) {
            if (segment.equals("sites")) {
                siteType = SiteType.SITE;
                sb = new StringBuilder();
            } else if (segment.equals("spaces")) {
                siteType = SiteType.SPACE;
                sb = new StringBuilder();
            } else if (segment.equals("dashboards")) {
                siteType = SiteType.DASHBOARD;
                sb = new StringBuilder();
            } else if (segment.equals("navigation")) {
                siteName = sb.toString();
                sb = null;
                nodePath = NodePath.root();
            } else if (segment.equals("pages")) {
                siteName = sb.toString();
                sb = null;
                break;
            } else if (nodePath != null) {
                nodePath.append(segment);
            } else if (sb != null) {
                sb.append(segment);
            }
        }
        SiteId siteId = (siteName == null) ? null : new SiteId(siteType, siteName);
        Locale locale = context.getLocale();

        User user = (managedUser == null || managedUser.getUserName() == null) ? User.anonymous() : new User(managedUser.getUserName());
        BasicPortalRequest.setInstance(new BasicPortalRequest(user, siteId, nodePath, locale, portal));
    }
}
