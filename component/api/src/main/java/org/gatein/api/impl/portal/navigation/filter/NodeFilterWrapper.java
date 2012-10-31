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
package org.gatein.api.impl.portal.navigation.filter;

import org.exoplatform.portal.mop.navigation.NodeState;
import org.gatein.api.impl.portal.navigation.ObjectFactory;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.util.Filter;

public class NodeFilterWrapper implements org.exoplatform.portal.mop.navigation.NodeFilter
{
   private Filter<Node> filter;

   public NodeFilterWrapper(Filter<Node> filter)
   {
      this.filter = filter;
   }

   @Override
   public boolean accept(int depth, String id, String name, NodeState state)
   {
      Node node = ObjectFactory.createNode(depth == 0 ? Node.ROOT_NODE_NAME : name, state);
      return filter.accept(node);
   }
}
