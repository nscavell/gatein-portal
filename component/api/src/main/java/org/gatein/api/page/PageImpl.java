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

package org.gatein.api.page;

import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageState;
import org.gatein.api.Util;
import org.gatein.api.common.i18n.LocalizedString;
import org.gatein.api.page.Page;
import org.gatein.api.page.PageId;
import org.gatein.api.security.Permission;
import org.gatein.api.site.SiteId;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PageImpl implements Page
{
   private final transient PageContext pageContext;

   private LocalizedString description;
   private String resolvedDescription;
   private LocalizedString displayName;
   private String resolvedDisplayName;

   public PageImpl(PageContext pageContext)
   {
      this.pageContext = pageContext;
      if (pageContext.getState().getDescription() != null)
      {
         this.description = new LocalizedString(pageContext.getState().getDescription());
      }
      if (pageContext.getState().getDisplayName() != null)
      {
         this.displayName = new LocalizedString(pageContext.getState().getDisplayName());
      }
   }

   @Override
   public PageId getId()
   {
      return Util.from(pageContext.getKey());
   }

   @Override
   public SiteId getSiteId()
   {
      return Util.from(pageContext.getKey().getSite());
   }

   @Override
   public String getName()
   {
      return pageContext.getKey().getName();
   }

   @Override
   public LocalizedString getDescription()
   {
      return description;
   }

   @Override
   public void setDescription(String description)
   {
      setDescription((description == null) ? null : new LocalizedString(description));
   }

   @Override
   public void setDescription(LocalizedString description)
   {
      if (description != null && description.isLocalized()) throw new IllegalArgumentException("Localized description is not supported");

      if (description != null)
      {
         setState(builder().description(description.getValue()));
      }
      else
      {
         setState(builder().description(null));
      }
      this.description = description;
   }

   @Override
   public String resolveDescription()
   {
      return (description == null) ? null : description.getValue();
   }

   @Override
   public LocalizedString getDisplayNames()
   {
      return displayName;
   }

   @Override
   public void setDisplayName(String displayName)
   {
      setDisplayNames((displayName == null) ? null : new LocalizedString(displayName));
   }

   @Override
   public void setDisplayNames(LocalizedString displayName)
   {
      if (displayName != null && displayName.isLocalized()) throw new IllegalArgumentException("Localized displayName is not supported");

      if (displayName != null)
      {
         setState(builder().displayName(displayName.getValue()));
      }
      else
      {
         setState(builder().displayName(null));
      }

      this.displayName = displayName;
   }

   @Override
   public String getDisplayName()
   {
      //TODO: Determine how to create the BasicI18NResolver by finding the appropriate resource bundle and locale, i.e. PortalRequestContext#getTitle()
      return (displayName == null) ? null : displayName.getValue();
   }

   @Override
   public Permission getAccessPermission()
   {
      return Util.from(pageContext.getState().getAccessPermissions());
   }

   @Override
   public void setAccessPermission(Permission permission)
   {
      if (permission == null) throw new IllegalArgumentException("Access permission cannot be null. To allow access to everyone use Permission.everyone()");

      setState(builder().accessPermissions(Util.from(permission)));
   }

   @Override
   public Permission getEditPermission()
   {
      return Util.from(pageContext.getState().getEditPermission());
   }

   @Override
   public void setEditPermission(Permission permission)
   {
      if (permission == null) throw new IllegalArgumentException("Edit permission cannot be null. To allow edit for everyone use Permission.everyone()");

      // Only one edit permission (membership) is allowed at this time.
      String[] permissions = Util.from(permission);
      if (permissions.length != 1) throw new IllegalArgumentException("Invalid permission. Only one membership is allowed for an edit permission");

      setState(builder().editPermission(permissions[0]));
   }

   @Override
   public int compareTo(Page page)
   {
      return getName().compareTo(page.getName());
   }

   public PageContext getPageContext()
   {
      return pageContext;
   }

   private PageState.Builder builder()
   {
      return pageContext.getState().builder();
   }

   private void setState(PageState.Builder builder)
   {
      pageContext.setState(builder.build());
   }
}
