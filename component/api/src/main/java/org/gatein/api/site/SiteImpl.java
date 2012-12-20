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

package org.gatein.api.site;

import java.util.Locale;

import org.gatein.api.common.Attributes;
import org.gatein.api.common.i18n.LocalizedString;
import org.gatein.api.internal.ObjectToStringBuilder;
import org.gatein.api.security.Group;
import org.gatein.api.security.Permission;
import org.gatein.api.security.User;
import org.gatein.api.site.Site;
import org.gatein.api.site.SiteId;
import org.gatein.api.site.SiteType;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class SiteImpl implements Site
{
   private final SiteId id;

   private LocalizedString displayName;
   private LocalizedString description;
   private Locale locale;
   private String skin;
   private Attributes attributes;
   private Permission accessPermission;
   private Permission editPermission;

   public SiteImpl(String name)
   {
      this(new SiteId(name));
   }

   public SiteImpl(Group group)
   {
      this(new SiteId(group));
   }

   public SiteImpl(User user)
   {
      this(new SiteId(user));
   }

   public SiteImpl(SiteId id)
   {
      if (id == null) throw new IllegalArgumentException("id cannot be null");

      this.id = id;
      this.attributes = new Attributes();
   }

   @Override
   public SiteId getId()
   {
      return id;
   }

   @Override
   public SiteType getType()
   {
      return id.getType();
   }

   @Override
   public String getName()
   {
      return id.getName();
   }

   @Override
   public LocalizedString getDescription()
   {
      return description;
   }

   @Override
   public void setDescription(String description)
   {
      setDescription(new LocalizedString(description));
   }

   @Override
   public void setDescription(LocalizedString description)
   {
      if (description != null && description.isLocalized()) throw new IllegalArgumentException("Localized description is not supported");

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
      setDisplayNames(new LocalizedString(displayName));
   }

   @Override
   public void setDisplayNames(LocalizedString displayName)
   {
      if (displayName != null && displayName.isLocalized()) throw new IllegalArgumentException("Localized displayName is not supported");

      this.displayName = displayName;
   }

   @Override
   public String getDisplayName()
   {
      //TODO: Resolve display name should also take care of group name if site is a group site
      return (displayName == null) ? getName() : displayName.getValue();
   }

   @Override
   public Locale getLocale()
   {
      return locale;
   }

   @Override
   public void setLocale(Locale locale)
   {
      this.locale = locale;
   }

   @Override
   public String getSkin()
   {
      return skin;
   }

   @Override
   public void setSkin(String skin)
   {
      this.skin = skin;
   }

   @Override
   public Attributes getAttributes()
   {
      return attributes;
   }

   @Override
   public Permission getAccessPermission()
   {
      return accessPermission;
   }

   @Override
   public void setAccessPermission(Permission permission)
   {
      this.accessPermission = permission;
   }

   @Override
   public Permission getEditPermission()
   {
      return editPermission;
   }

   @Override
   public void setEditPermission(Permission permission)
   {
      this.editPermission = permission;
   }

   @Override
   public int compareTo(Site other)
   {
      return getName().compareTo(other.getName());
   }

   @Override
   public String toString()
   {
      return ObjectToStringBuilder.toStringBuilder(getClass())
         .add("type", getType().getName())
         .add("name", getName())
         .add("displayName", getDisplayNames())
         .add("description", getDescription())
         .add("locale", getLocale())
         .add("skin", getSkin())
         .add("attributes", getAttributes())
         .add("editPermission", getEditPermission())
         .add("accessPermission", getAccessPermission())
         .toString();
   }

   public static final class AttributeKeys
   {
      public static final Attributes.Key<String> SESSION_BEHAVIOR = Attributes.key("org.gatein.api.portal.session_behavior", String.class);
      public static final Attributes.Key<Boolean> SHOW_PORTLET_INFO_BAR = Attributes.key("org.gatein.api.portal.show_info_bar", Boolean.class);

      private AttributeKeys()
      {
      }
   }
}
