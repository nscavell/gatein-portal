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
package org.gatein.api.impl.portal.navigation;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;

import org.exoplatform.portal.mop.navigation.NodeContext;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.util.Filter;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class FilteredNode extends ApiNode
{
   private final FilteredNodeMap map;

   public FilteredNode(NavigationImpl navigation, NodeContext<ApiNode> context, Filter<Node> filter)
   {
      this(navigation, context, new FilteredNodeMap(filter));
   }

   public FilteredNode(NavigationImpl navigation, NodeContext<ApiNode> context, FilteredNodeMap map)
   {
      super(navigation, context);
      this.map = map;
   }

   @Override
   public Node addChild(int index, String childName)
   {
      return map.getFiltered(super.addChild(realIndex(index), childName));
   }

   @Override
   public Node addChild(String childName)
   {
      return map.getFiltered(super.addChild(childName));
   }

   @Override
   public Node getChild(int index)
   {
      int i = 0;
      for (Iterator<Node> itr = iterator(); itr.hasNext();)
      {
         if (i == index)
         {
            return map.getFiltered(itr.next());
         }

         i++;
         itr.next();
      }

      throw new IndexOutOfBoundsException();
   }

   @Override
   public Node getChild(String childName)
   {
      Node n = super.getChild(childName);
      return map.isAccepted(n) ? map.getFiltered(n) : null;
   }

   @Override
   public int getChildCount()
   {
      int i = 0;
      for (Iterator<Node> itr = iterator(); itr.hasNext(); itr.next())
      {
         i++;
      }
      return i;
   }

   @Override
   public Node getNode(NodePath nodePath)
   {
      Node n = this;
      for (String e : nodePath)
      {
         n = n.getChild(e);
         if (n == null)
         {
            return null;
         }
      }
      return n;
   }

   @Override
   public Node getParent()
   {
      return map.getFiltered(super.getParent());
   }

   @Override
   public boolean hasChild(String childName)
   {
      Node n = super.getChild(childName);
      return map.isAccepted(n);
   }

   @Override
   public int indexOf(String childName)
   {
      int i = 0;
      for (Iterator<Node> itr = iterator(); itr.hasNext();)
      {
         if (itr.next().getName().equals(childName))
         {
            return i;
         }

         i++;
      }
      return -1;
   }

   @Override
   public Iterator<Node> iterator()
   {
      return new FilteredNodeIterator();
   }

   @Override
   public void moveTo(int index)
   {
      super.moveTo(map.getFiltered(super.getParent()).realIndex(index));
   }

   @Override
   public void moveTo(int index, Node parent)
   {
      super.moveTo(map.getFiltered(parent).realIndex(index), parent);
   }

   public int realIndex(int index)
   {
      int i = 0;
      int j = 0;
      Iterator<Node> itr = super.iterator();

      while (itr.hasNext() && j <= index)
      {
         if (map.isAccepted(itr.next()))
         {
            j++;
         }
         i++;
      }

      if (j < index)
      {
         throw new IndexOutOfBoundsException();
      }

      return j > index ? i - 1 : i;
   }

   private class FilteredNodeIterator implements Iterator<Node>
   {
      private Iterator<ApiNode> itr = context.iterator();
      private ApiNode last;
      private ApiNode next = findNext();

      @Override
      public boolean hasNext()
      {
         return next != null;
      }

      @Override
      public Node next()
      {
         if (next == null)
         {
            throw new NoSuchElementException();
         }

         last = next;
         next = findNext();

         return last;
      }

      @Override
      public void remove()
      {
         if (last == null)
         {
            throw new IllegalStateException();
         }

         last.context.remove();
      }

      private ApiNode findNext()
      {
         while (itr.hasNext())
         {
            ApiNode n = itr.next();
            if (map.isAccepted(n))
            {
               return n;
            }
         }

         return null;
      }
   }

   private static class FilteredNodeMap
   {
      private Map<ApiNode, Boolean> acceptedMap = new WeakHashMap<ApiNode, Boolean>();

      private Filter<Node> filter;

      private Map<ApiNode, FilteredNode> filteredMap = new WeakHashMap<ApiNode, FilteredNode>();

      private FilteredNodeMap(Filter<Node> filter)
      {
         this.filter = filter;
      }

      private FilteredNode getFiltered(Node node)
      {
         ApiNode apiNode = (ApiNode) node;
         FilteredNode filteredNode = filteredMap.get(apiNode);
         if (filteredNode == null)
         {
            filteredNode = new FilteredNode(apiNode.navigation, apiNode.context, this);
            filteredMap.put(apiNode, filteredNode);
         }
         return filteredNode;
      }

      private boolean isAccepted(Node node)
      {
         ApiNode apiNode = (ApiNode) node;
         Boolean accepted = acceptedMap.get(apiNode);
         if (accepted == null)
         {
            accepted = filter.accept(node);
            acceptedMap.put(apiNode, accepted);
         }
         return accepted;
      }
   }
}
