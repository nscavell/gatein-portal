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

import org.junit.Assert;
import org.junit.Test;

public class StateMapTest
{
   private StateMap<MyObject, Object> map;

   @org.junit.Before
   public void before()
   {
      map = new StateMap<MyObject, Object>();
   }

   @Test
   public void getWithDifferentHash()
   {
      MyObject k = new MyObject("one");
      Object v = new Object();
      map.put(k, v);

      k.v = "two";

      Assert.assertSame(v, map.get(k));
   }

   @Test
   public void multipleEntriesWithSameHash()
   {
      MyObject k1 = new MyObject("one");
      Object v1 = new Object();
      map.put(k1, v1);

      MyObject k2 = new MyObject("one");
      Object v2 = new Object();
      map.put(k2, v2);

      Assert.assertSame(v1, map.get(k1));
      Assert.assertSame(v2, map.get(k2));
   }

   @Test(timeout = 10000)
   public void gc() throws InterruptedException
   {
      MyObject k = new MyObject("one");
      MyObject v = new MyObject("two");
      map.put(k, v);

      k = null;
      v = null;

      while (map.size() != 0)
      {
         System.gc();
      }
   }

   class MyObject
   {
      private String v;

      public MyObject(String v)
      {
         this.v = v;
      }

      @Override
      public boolean equals(Object obj)
      {
         return v.equals(((MyObject) obj).v);
      }

      public int hashCode()
      {
         return v.hashCode();
      }
   }
}
