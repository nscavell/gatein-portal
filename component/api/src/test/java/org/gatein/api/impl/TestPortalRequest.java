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

import java.util.Locale;

import org.gatein.api.Portal;
import org.gatein.api.PortalRequest;
import org.gatein.api.navigation.NodePath;
import org.gatein.api.security.User;
import org.gatein.api.site.SiteId;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class TestPortalRequest extends PortalRequest
{
   private User user;
   private SiteId siteId;
   private NodePath nodePath;
   private Locale locale;
   private Portal portal;

   private TestPortalRequest(User user, SiteId siteId, NodePath nodePath, Locale locale, Portal portal)
   {
      this.user = user;
      this.siteId = siteId;
      this.nodePath = nodePath;
      this.locale = locale;
      this.portal = portal;
   }

   @Override
   public User getUser()
   {
      return user;
   }

   @Override
   public SiteId getSiteId()
   {
      return siteId;
   }

   @Override
   public NodePath getNodePath()
   {
      return nodePath;
   }

   @Override
   public Locale getLocale()
   {
      return locale;
   }

   @Override
   public Portal getPortal()
   {
      return portal;
   }

   public static void setInstance(User user, SiteId siteId, NodePath nodePath, Locale locale, Portal portal)
   {
      setInstance(new TestPortalRequest(user, siteId, nodePath, locale, portal));
   }

   public static void clearInstance()
   {
      setInstance(null);
   }
}
