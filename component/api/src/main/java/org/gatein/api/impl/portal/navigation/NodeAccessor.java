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
package org.gatein.api.impl.portal.navigation;

import java.lang.reflect.Field;
import java.util.List;

import org.gatein.api.ApiException;
import org.gatein.api.internal.URLFactory;
import org.gatein.api.portal.navigation.Node;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class NodeAccessor
{
   public static Field children = getField("children");
   public static Field childrenLoaded = getField("childrenLoaded");

   public static Field urlFactory = getField("urlFactory");

   private static Field getField(String name)
   {
      try
      {
         Field f = Node.class.getDeclaredField(name);
         f.setAccessible(true);
         return f;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @SuppressWarnings("unchecked")
   public static List<Node> getChildren(Node node)
   {
      try
      {
         return (List<Node>) NodeAccessor.children.get(node);
      }
      catch (Throwable e)
      {
         throw new ApiException("Internal api error", e);
      }
   }

   public static void setChildrenLoaded(Node node, boolean loaded)
   {
      try
      {
         NodeAccessor.childrenLoaded.set(node, loaded);
      }
      catch (Throwable e)
      {
         throw new ApiException("Internal api error", e);
      }
   }

   public static void setURLFactory(Node node, URLFactory urlFactory)
   {
      try
      {
         NodeAccessor.urlFactory.set(node, urlFactory);
      }
      catch (Throwable e)
      {
         throw new ApiException("Internal api error", e);
      }
   }
}
