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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.gatein.api.ApiException;
import org.gatein.api.impl.Util;
import org.gatein.api.portal.navigation.Navigation;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.portal.navigation.PublicationDate;
import org.gatein.api.portal.navigation.Visibility;
import org.gatein.api.portal.navigation.Visibility.Flag;
import org.gatein.api.portal.site.Site;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class NavigationUtil
{
   public static Node findNode(Navigation navigation, NodePath nodePath)
   {
      Iterator<String> itr = nodePath.iterator();
      List<Node> nodes = navigation.getNodes();
      String rootNodeName = itr.next();
      for (Node n : nodes)
      {
         if (n.getName().equals(rootNodeName))
         {
            while (itr.hasNext())
            {
               n = n.getChild(itr.next());
               if (n == null)
               {
                  return null;
               }
            }
            return n;
         }
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   public static NodeContext<NodeContext<?>> findNodeContext(NodeContext<NodeContext<?>> rootNodeCtx, NodePath nodePath)
   {
      Iterator<String> itr = nodePath.iterator();
      Collection<NodeContext<?>> nodes = rootNodeCtx.getNodes();
      String rootNodeName = itr.next();
      for (NodeContext<?> n : nodes)
      {
         if (n.getName().equals(rootNodeName))
         {
            while (itr.hasNext())
            {
               n = n.get(itr.next());
               if (n == null)
               {
                  return null;
               }
            }
            return (NodeContext<NodeContext<?>>) n;
         }
      }
      return null;
   }

   private static Flag from(org.exoplatform.portal.mop.Visibility visibility)
   {
      switch (visibility)
      {
         case DISPLAYED:
            return Flag.VISIBLE;
         case HIDDEN:
            return Flag.HIDDEN;
         case SYSTEM:
            return Flag.SYSTEM;
         case TEMPORAL:
            return Flag.PUBLICATION;
         default:
            throw new ApiException("Unknown internal visibility '" + visibility + "'");
      }
   }

   public static org.exoplatform.portal.mop.Visibility from(Flag flag)
   {
      switch (flag)
      {
         case VISIBLE:
            return org.exoplatform.portal.mop.Visibility.DISPLAYED;
         case HIDDEN:
            return org.exoplatform.portal.mop.Visibility.HIDDEN;
         case SYSTEM:
            return org.exoplatform.portal.mop.Visibility.SYSTEM;
         case PUBLICATION:
            return org.exoplatform.portal.mop.Visibility.TEMPORAL;
         default:
            throw new ApiException("Unknown visibility flag '" + flag + "'");
      }
   }

   public static NodeState from(Node node, NodeContext<?> nodeContext)
   {
      String label = nodeContext.getState().getLabel(); // TODO Set label on NodeState
      String icon = node.getIconName();

      PublicationDate publicationDate = node.getVisibility().getPublicationDate();

      long startPublicationTime = -1;
      long endPublicationTime = -1;

      if (publicationDate != null)
      {
         if (publicationDate.getStart() != null)
         {
            startPublicationTime = publicationDate.getStart().getTime();
         }

         if (publicationDate.getEnd() != null)
         {
            endPublicationTime = publicationDate.getEnd().getTime();
         }
      }

      org.exoplatform.portal.mop.Visibility visibility = from(node.getVisibility().getFlag());

      return new NodeState(label, icon, startPublicationTime, endPublicationTime, visibility, nodeContext.getState()
            .getPageRef());
   }

   private static Visibility from(NodeState nodeState)
   {
      Flag flag = from(nodeState.getVisibility());

      long start = nodeState.getStartPublicationTime();
      long end = nodeState.getEndPublicationTime();

      PublicationDate publicationDate = null;
      if (start != -1 && end != -1)
      {
         publicationDate = PublicationDate.between(new Date(start), new Date(end));
      }
      else if (start != -1)
      {
         publicationDate = PublicationDate.startingOn(new Date(start));
      }
      else if (end != -1)
      {
         publicationDate = PublicationDate.endingOn(new Date(end));
      }

      return new Visibility(flag, publicationDate);
   }

   @SuppressWarnings("unchecked")
   private static Node from(Site.Id siteId, NodeContext<NodeContext<?>> nodeInternal)
   {
      Node node;
      if (nodeInternal.getParent() == null)
      {
         node = new Node("default"); // TODO Make immutable except for children
      }
      else
      {
         node = from(nodeInternal.getName(), nodeInternal.getState());
      }

      if (nodeInternal.isExpanded())
      {
         List<Node> children = new ArrayList<Node>(nodeInternal.getNodeCount());
         for (NodeContext<?> childNodeCtx : nodeInternal.getNodes())
         {
            // TODO Why are there nodes with the name 'notfound'?
            if (!childNodeCtx.getName().equals("notfound"))
            {
               Node childNode = from(siteId, (NodeContext<NodeContext<?>>) childNodeCtx);
               children.add(childNode);
            }
         }
         node.addChildren(children);
         node.setChildrenLoaded(true);
      }

      return node;
   }


   public static Node from(String name, NodeState state)
   {
      Node node = new Node(name);
      node.setIconName(state.getIcon());
      // n.setLabel(label); // TODO Set label on node
      node.setPageId(Util.from(state.getPageRef()));
      node.setVisibility(from(state));
      node.setUrlFactory(new DefaultNodeURLFactory());
      return node;
   }

   public static Navigation from(Site.Id siteId, NavigationContext navigationInternal,
         NodeContext<NodeContext<?>> rootNodeInternal)
   {
      Navigation navigation = new Navigation(siteId, navigationInternal.getState().getPriority());
      Node rootNode = from(siteId, rootNodeInternal);
      navigation.setNodes(new ArrayList<Node>(rootNode.getChildren()));
      return navigation;
   }
}
