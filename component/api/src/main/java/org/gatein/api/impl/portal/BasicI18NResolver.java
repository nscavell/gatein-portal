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

import org.gatein.api.PortalRequest;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class BasicI18NResolver extends AbstractI18NResolver
{
   private final Locale portalLocale;
   private final ResourceBundle resourceBundle;

   public BasicI18NResolver(Locale portalLocale, ResourceBundle resourceBundle)
   {
      super(null);
      this.portalLocale = portalLocale;
      this.resourceBundle = resourceBundle;
   }

   @Override
   public ResourceBundle getResourceBundle()
   {
      return resourceBundle;
   }

   @Override
   public Locale getUserLocale()
   {
      return PortalRequest.getInstance().getLocale();
   }

   @Override
   public Locale getSiteLocale()
   {
      return portalLocale;
   }
}
