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
import java.util.ArrayList;
import java.util.List;

import org.gatein.api.portal.navigation.Node;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class NodeAccessor
{

   private static Field parent;

   private static Field children;

   private static Field childrenLoaded;

   static
   {
      try
      {
         parent = Node.class.getDeclaredField("parent");
         parent.setAccessible(true);

         children = Node.class.getDeclaredField("children");
         children.setAccessible(true);

         childrenLoaded = Node.class.getDeclaredField("childrenLoaded");
         childrenLoaded.setAccessible(true);
      } catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public static void setChildren(Node node, List<Node> children)
   {
      try
      {
         if (children != null)
         {
            NodeAccessor.children.set(node, children);
            NodeAccessor.childrenLoaded.set(node, true);

            for (Node child : children)
            {
               NodeAccessor.parent.set(child, node);
            }
         }
         else
         {
            NodeAccessor.children.set(node, new ArrayList<Node>());
            NodeAccessor.childrenLoaded.set(node, false);
         }
      } catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
}
