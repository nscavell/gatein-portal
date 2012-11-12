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
package org.gatein.api.impl;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class StateMap<K, V>
{
   private final List<Value<K, V>> l;

   public StateMap()
   {
      l = new LinkedList<Value<K, V>>();
   }

   public V get(Object key)
   {
      int h = System.identityHashCode(key);
      for (Value<K, V> v : getL())
      {
         if (v.hash == h)
         {
            return v.value;
         }
      }
      return null;
   }

   public int size()
   {
      return getL().size();
   }

   private void clearExpiredEntries()
   {
      for (Iterator<Value<K, V>> itr = l.iterator(); itr.hasNext();)
      {
         if (itr.next().get() == null)
         {
            itr.remove();
         }
      }
   }

   private List<Value<K, V>> getL()
   {
      clearExpiredEntries();
      return l;
   }

   public void put(K key, V value)
   {
      Value<K, V> v = new Value<K, V>(key, value);
      getL().add(v);
   }

   static class Value<K, V> extends WeakReference<K>
   {
      private final V value;

      private final int hash;

      Value(K key, V value)
      {
         super(key);
         this.value = value;
         this.hash = System.identityHashCode(key);
      }

      public V getValue()
      {
         return null;
      }
   }
}
