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

import java.util.Iterator;
import java.util.Locale;

import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.page.PageKey;
import org.gatein.api.portal.Group;
import org.gatein.api.portal.Membership;
import org.gatein.api.portal.Permission;
import org.gatein.api.portal.User;
import org.gatein.api.portal.page.PageId;
import org.gatein.api.portal.site.Site;
import org.gatein.api.portal.site.SiteId;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class Util
{
   public static Site from(PortalConfig portalConfig)
   {
      if (portalConfig == null) return null;

      SiteKey siteKey = new SiteKey(portalConfig.getType(), portalConfig.getName());

      Site site = new Site(from(siteKey));
      site.setTitle(portalConfig.getLabel());
      if (portalConfig.getLocale() != null)
      {
         site.setLocale(new Locale(portalConfig.getLocale()));
      }
      site.setAccessPermission(from(portalConfig.getAccessPermissions()));
      site.setEditPermission(from(portalConfig.getEditPermission()));

      return site;
   }

   public static PortalConfig from(Site site)
   {
      if (site == null) return null;

      SiteKey siteKey = from(site.getId());
      PortalConfig portalConfig = new PortalConfig(siteKey.getTypeName(), siteKey.getName());
      portalConfig.setLabel(site.getTitle());
      if (site.getLocale() != null)
      {
         portalConfig.setLocale(site.getLocale().getLanguage());
      }
      portalConfig.setAccessPermissions(from(site.getAccessPermission()));
      String[] editPermission = from(site.getEditPermission());
      if (editPermission != null)
      {
         portalConfig.setEditPermission(editPermission[0]); // edit permissions currently only support one permission
      }

      return portalConfig;
   }

   public static PageId from(PageKey pageKey)
   {
      SiteKey siteKey = pageKey.getSite();
      switch (pageKey.getSite().getType())
      {
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

   public static PageKey from(PageId pageId)
   {
      return new PageKey(from(pageId.getSiteId()), pageId.getPageName());
   }

   public static SiteId from(SiteKey siteKey)
   {
      switch (siteKey.getType())
      {
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

   public static SiteKey from(SiteId siteId)
   {
      switch (siteId.getType())
      {
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

   public static Permission from(String...permissions)
   {
      if (permissions == null) return null;
      if (permissions.length == 1 && permissions[0] == null) return null; // for some reason this is happening...

      if (permissions.length == 1 && permissions[0].equals("Everyone"))
      {
         return Permission.everyone();
      }
      else
      {
         return null; // TODO new Permission(permissions);
      }
   }

   public static String[] from(Permission permission)
   {
      if (permission == null) return null;

      if (permission.isAccessibleToEveryone())
      {
         return new String[] {"Everyone"};
      }
      else
      {
         String[] permissions = new String[permission.getMemberships().size()];
         Iterator<Membership> memberships = permission.getMemberships().iterator();
         for (int i=0; i < permissions.length; i++)
         {
            // Membership.toString gives us this, however it's safer to not rely on it.
            Membership membership = memberships.next();
            permissions[i] = membership.getMembershipType() + ":" + membership.getGroup().getId();
         }

         return permissions;
      }
   }
}