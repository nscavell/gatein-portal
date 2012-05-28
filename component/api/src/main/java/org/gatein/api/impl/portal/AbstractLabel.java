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

import org.exoplatform.commons.utils.ExpressionUtil;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.gatein.api.portal.Localized;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public abstract class AbstractLabel// implements Label
{
   public abstract String getDescriptionId();

   public abstract String getSimpleValue();

   public abstract String getDefaultName();

   public abstract void setValue(String value);

   public abstract ResourceBundle getResourceBundle();

   public abstract Locale getUserLocale();

   public abstract Locale getPortalLocale();

   private final DescriptionService service;
   public AbstractLabel(DescriptionService service)
   {
      this.service = service;
   }

   //@Override
   public String getValue(boolean resolve)
   {
      String simple = getSimpleValue();
      String id = getDescriptionId();
      if (simple != null)
      {
         ResourceBundle bundle = getResourceBundle();
         return ExpressionUtil.getExpressionValue(bundle, simple);
      }
      else if (id != null)
      {
         Locale userLocale = getUserLocale();
         Locale portalLocale = getPortalLocale();
         Described.State described = service.resolveDescription(id, portalLocale, userLocale);
         if (described != null)
         {
            return described.getName();
         }
      }

      return getDefaultName();
   }

   //@Override
   public boolean isLocalized()
   {
      return getDescriptionId() != null;
   }

   //@Override
   public Localized.Value<String> getValue(Locale locale)
   {
      String id = getDescriptionId();
      if (id != null)
      {
         Described.State described = service.resolveDescription(id, locale);
         if (described != null)
         {
            //return new LocalizedString(locale, described.getName());
         }
      }
      return null;
   }

   //@Override
   public Localized.Value<String> setValue(Locale locale, String value)
   {
      //TODO: Need way to create a localized label. Meaning going from simple to "extended".
      String id = getDescriptionId();
      if (id == null) throw new IllegalStateException("Label is not localized.");

      service.setDescription(id, locale, new Described.State(value, null));

      //return new LocalizedString(locale, value);
      return null;
   }

   //@Override
   public void removeValue(Locale locale)
   {
      String id = getDescriptionId();
      if (id == null) throw new IllegalStateException("This label is not localized, hence a localized value cannot be removed for locale " + locale);

      service.setDescription(id, locale, new Described.State(null, null));
   }

   //@Override
   public Iterator<Localized.Value<String>> iterator()
   {
      final String id = getDescriptionId();
      if (id == null) return EMPTY_ITERATOR;

      Map<Locale, Described.State> values = service.getDescriptions(id);
      if (values == null) return EMPTY_ITERATOR;

      final Iterator<Map.Entry<Locale, Described.State>> iterator = values.entrySet().iterator();

      return new Iterator<Localized.Value<String>>()
      {
         private Map.Entry<Locale, Described.State> current;

         @Override
         public boolean hasNext()
         {
            return iterator.hasNext();
         }

         @Override
         public Localized.Value<String> next()
         {
            Map.Entry<Locale, Described.State> entry = iterator.next();
            current = entry;
            //return new Localized.Value<String>(entry.getKey(), entry.getValue().getName());
            return null;
         }

         @Override
         public void remove()
         {
            if (current != null)
            {
               service.setDescription(id, current.getKey(), new Described.State(null, null));
            }
            iterator.remove();
         }
      };
   }

//   private static class LocalizedString extends Localized.Value<String>
//   {
//      private String value;
//      private Locale locale;
//
//      private LocalizedString(Locale locale, String value)
//      {
//         this.locale = locale;
//         this.value = value;
//      }
//
//      @Override
//      public String getValue()
//      {
//         return value;
//      }
//
//      @Override
//      public void setValue(String value)
//      {
//         this.value = value;
//      }
//
//      @Override
//      public Locale getLocale()
//      {
//         return locale;
//      }
//   }

   private static final Iterator<Localized.Value<String>> EMPTY_ITERATOR = new Iterator<Localized.Value<String>>()
   {
      @Override
      public boolean hasNext()
      {
         return false;
      }

      @Override
      public Localized.Value<String> next()
      {
         throw new IllegalStateException("No elements to iterate.");
      }

      @Override
      public void remove()
      {
         throw new IllegalStateException("No elements available for removal.");
      }
   };
}
