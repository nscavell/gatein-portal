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

import org.gatein.api.common.i18n.Localized;
import org.gatein.api.common.i18n.LocalizedString;
import org.gatein.api.navigation.Visibility;
import org.gatein.api.security.Membership;
import org.gatein.api.security.Permission;
import org.gatein.common.xml.stax.writer.WritableValueTypes;
import org.gatein.management.api.model.ModelList;
import org.gatein.management.api.model.ModelObject;
import org.gatein.management.api.model.ModelString;

import java.util.Date;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class ModelUtils
{
   private ModelUtils(){}

   public static void populate(String fieldName, LocalizedString string, ModelObject model)
   {
      if (string == null) return;

      ModelList list = model.get(fieldName, ModelList.class);
      if (string.isLocalized())
      {
         for (Localized.Value<String> value : string.getLocalizedValues())
         {
            String localeString = value.getLocale().getLanguage();
            if (localeString == null)
            {
               throw new RuntimeException("Language was null for locale " + value.getLocale());
            }
            String country = value.getLocale().getCountry();
            if (country != null && country.length() > 0)
            {
               localeString += "-" + country.toLowerCase();
            }

            list.add().asValue(ModelObject.class)
               .set("value", value.getValue()).set("lang", localeString);
         }
      }
      else
      {
         list.add().asValue(ModelObject.class).set("value", string.getValue());
      }
   }

   public static void populate(String fieldName, Permission permission, ModelObject model)
   {
      if (permission != null)
      {
         ModelList list = model.get(fieldName, ModelList.class);
         if (permission.isAccessibleToEveryone())
         {
            list.add("Everyone");
         }

         for (Membership membership : permission.getMemberships())
         {
            list.add(membership.toString());
         }
      }
   }

   public static void set(String name, Object value, ModelObject model)
   {
      String s = (value == null) ? null : value.toString();
      model.set(name, s);
   }

   public static void set(String name, Date value, ModelObject model)
   {
      String s = null;
      if (value != null)
      {
         s = WritableValueTypes.DATE_TIME.format(value);
      }

      set(name, s, model);
   }
}
