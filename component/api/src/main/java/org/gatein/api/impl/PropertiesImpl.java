/*
* JBoss, a division of Red Hat
* Copyright 2012, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

import org.exoplatform.portal.config.model.PortalProperties;
import org.gatein.api.commons.PropertyType;
import org.gatein.api.Properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
class PropertiesImpl implements Properties
{
   private PropertiesImpl()
   {
   }

   /**
    * SingletonHolder is loaded on the first execution of Singleton.getInstance()
    * or the first access to SingletonHolder.INSTANCE, not before.
    */
   private static class SingletonHolder
   {
      public static final Properties INSTANCE = new PropertiesImpl();
   }

   public static Properties getInstance()
   {
      return SingletonHolder.INSTANCE;
   }

   private static List<String> SESSION_BEHAVIORS;

   static
   {
      List<String> behaviors = new ArrayList<String>(3);
      behaviors.add(PortalProperties.SESSION_ON_DEMAND);
      behaviors.add(PortalProperties.SESSION_ALWAYS);
      behaviors.add(PortalProperties.SESSION_NEVER);
      SESSION_BEHAVIORS = Collections.unmodifiableList(behaviors);
   }

   @Override
   public List<String> getSessionBehaviorValues()
   {
      return SESSION_BEHAVIORS;
   }

   @Override
   public boolean isKnown(PropertyType propertyType)
   {
      return SESSION_BEHAVIOR.equals(propertyType) || SHOW_PORTLET_INFO_BAR.equals(propertyType);
   }
}
