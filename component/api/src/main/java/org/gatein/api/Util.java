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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.LocaleUtils;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.Properties;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.page.PageKey;
import org.gatein.api.common.Attributes;
import org.gatein.api.page.PageId;
import org.gatein.api.security.Group;
import org.gatein.api.security.Membership;
import org.gatein.api.security.Permission;
import org.gatein.api.security.User;
import org.gatein.api.site.Site;
import org.gatein.api.site.SiteId;
import org.gatein.api.site.SiteImpl;
import org.gatein.api.site.SiteType;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class Util {
    public static Site from(PortalConfig portalConfig) {
        if (portalConfig == null)
            return null;

        SiteKey siteKey = new SiteKey(portalConfig.getType(), portalConfig.getName());

        Site site = new SiteImpl(from(siteKey));
        if (portalConfig.getLabel() != null) {
            site.setDisplayName(portalConfig.getLabel());
        }
        if (portalConfig.getDescription() != null) {
            site.setDescription(portalConfig.getDescription());
        }
        if (portalConfig.getLocale() != null) {
            site.setLocale(LocaleUtils.toLocale(portalConfig.getLocale()));
        }
        if (portalConfig.getSkin() != null) {
            site.setSkin(portalConfig.getSkin());
        }

        site.setAccessPermission(from(portalConfig.getAccessPermissions()));
        site.setEditPermission(from(portalConfig.getEditPermission()));

        site.getAttributes().putAll(portalConfig.getProperties());

        return site;
    }

    public static PortalConfig from(Site site) {
        if (site == null)
            return null;

        SiteKey siteKey = from(site.getId());
        PortalConfig portalConfig = new PortalConfig(siteKey.getTypeName(), siteKey.getName());
        if (site.getDisplayName() != null) {
            portalConfig.setLabel(site.getDisplayName());
        }
        if (site.getDescription() != null) {
            portalConfig.setDescription(site.getDescription());
        }
        if (site.getLocale() != null) {
            portalConfig.setLocale(site.getLocale().toString());
        }
        if (site.getSkin() != null) {
            portalConfig.setSkin(site.getSkin());
        }
        portalConfig.setAccessPermissions(from(site.getAccessPermission()));
        String[] editPermission = from(site.getEditPermission());
        if (editPermission != null) {
            portalConfig.setEditPermission(editPermission[0]); // edit permissions currently only support one permission
        }

        portalConfig.setProperties(Util.from(site.getAttributes()));

        return portalConfig;
    }

    public static Properties from(Attributes attributes) {
        if (attributes == null) {
            return null;
        }

        Properties properties = new Properties();
        properties.putAll(attributes);
        return properties;
    }

    public static Attributes from(Properties properties) {
        if (properties == null) {
            return null;
        }

        Attributes attributes = new Attributes();
        for (Entry<String, String> e : properties.entrySet()) {
            attributes.put(Attributes.key(e.getKey(), String.class), e.getValue().toString());
        }
        return attributes;
    }

    public static PageId from(PageKey pageKey) {
        if (pageKey == null)
            return null;

        SiteKey siteKey = pageKey.getSite();
        switch (pageKey.getSite().getType()) {
            case PORTAL:
                return new PageId(siteKey.getName(), pageKey.getName());
            case GROUP:
                return new PageId(new Group(siteKey.getName()), pageKey.getName());
            case USER:
                return new PageId(new User(siteKey.getName()), pageKey.getName());
            default:
                throw new AssertionError();
        }
    }

    public static PageKey from(PageId pageId) {
        if (pageId == null)
            return null;

        return new PageKey(from(pageId.getSiteId()), pageId.getPageName());
    }

    public static SiteId from(SiteKey siteKey) {
        if (siteKey == null)
            return null;

        switch (siteKey.getType()) {
            case PORTAL:
                return new SiteId(siteKey.getName());
            case GROUP:
                return new SiteId(new Group(siteKey.getName()));
            case USER:
                return new SiteId(new User(siteKey.getName()));
            default:
                throw new AssertionError();
        }
    }

    public static SiteKey from(SiteId siteId) {
        if (siteId == null)
            return null;

        switch (siteId.getType()) {
            case SITE:
                return SiteKey.portal(siteId.getName());
            case SPACE:
                return SiteKey.group(siteId.getName());
            case DASHBOARD:
                return SiteKey.user(siteId.getName());
            default:
                throw new AssertionError();
        }
    }

    public static org.exoplatform.portal.mop.SiteType from(SiteType siteType) {
        if (siteType == null)
            return null;

        switch (siteType) {
            case SITE:
                return org.exoplatform.portal.mop.SiteType.PORTAL;
            case SPACE:
                return org.exoplatform.portal.mop.SiteType.GROUP;
            case DASHBOARD:
                return org.exoplatform.portal.mop.SiteType.USER;
            default:
                throw new AssertionError();
        }
    }

    public static Permission from(String... permissions) {
        if (permissions == null)
            return null;
        if (permissions.length == 1 && permissions[0] == null)
            return null; // for some reason this is happening...

        return from(Arrays.asList(permissions));
    }

    public static Permission from(List<String> permissions) {
        if (permissions == null)
            return null;

        if (permissions.size() == 1 && permissions.get(0).equals("Everyone")) {
            return Permission.everyone();
        } else {
            Set<Membership> memberships = new LinkedHashSet<Membership>(permissions.size());
            for (String permission : permissions) {
                memberships.add(Membership.fromString(permission));
            }

            return new Permission(memberships);
        }
    }

    public static String[] from(Permission permission) {
        if (permission == null)
            return null;

        if (permission.isAccessibleToEveryone()) {
            return new String[] { "Everyone" };
        } else {
            String[] permissions = new String[permission.getMemberships().size()];
            Iterator<Membership> memberships = permission.getMemberships().iterator();
            for (int i = 0; i < permissions.length; i++) {
                permissions[i] = memberships.next().toString();
            }

            return permissions;
        }
    }
}
