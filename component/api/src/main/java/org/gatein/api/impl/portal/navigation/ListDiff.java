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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.gatein.api.portal.navigation.Node;

public class ListDiff
{
   private ListDiff()
   {
   }

   public static List<Diff> compare(NodeList current, NodeList original)
   {
      List<Diff> d = new LinkedList<Diff>();

      for (int i = 0; i < current.size(); i++)
      {
         Node c = current.get(i);
         Node o = original.get(c.getName());

         if (o == null)
         {
            d.add(new Diff(i, c, DiffOp.ADD));
         }
         else if (original.indexOf(c.getName()) != i)
         {
            d.add(new Diff(i, c, DiffOp.MOVE));
         }
         else
         {
            d.add(new Diff(i, c, DiffOp.UNCHANGED));
         }
      }

      for (Node c : original)
      {
         if (current.get(c.getName()) == null)
         {
            d.add(new Diff(original.indexOf(c.getName()), c, DiffOp.REMOVE));
         }
      }

      Collections.sort(d, new DiffComparator());

      return d;
   }

   public static class Diff
   {
      private int index;
      private Node node;
      private DiffOp operation;

      public Diff(int index, Node data, DiffOp operation)
      {
         this.index = index;
         this.node = data;
         this.operation = operation;
      }

      public int getIndex()
      {
         return index;
      }

      public Node getNode()
      {
         return node;
      }

      public DiffOp getOperation()
      {
         return operation;
      }
   }

   public static enum DiffOp
   {
      REMOVE, ADD, MOVE, UNCHANGED
   }

   static class DiffComparator implements Comparator<Diff>
   {
      @Override
      public int compare(Diff o1, Diff o2)
      {
         return (o1.index<o2.index ? -1 : (o1.index==o2.index ? 0 : 1));
      }
   }
}
