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
package org.gatein.api.impl.portal.navigation.visitor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.gatein.api.impl.portal.navigation.NodeList;
import org.gatein.api.impl.portal.navigation.RootNode;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.portal.navigation.NodeVisitor.NodeDetails;
import org.gatein.api.portal.navigation.Visibility;
import org.gatein.api.portal.page.PageId;
import org.junit.Before;
import org.junit.Test;

public class SaveNodeVisitorTest
{

   private SaveNodeVisitor visitor;

   @Before
   public void before()
   {
      RootNode r = new RootNode(null);

      r.addChild("1");
      r.getChild("1").addChild("1-1");
      r.getChild("1").getChild("1-1").addChild("1-1-1");
      r.getChild("1").getChild("1-1").getChild("1-1-1").addChild("1-1-1-1");
      r.getChild("1").getChild("1-1").addChild("1-1-2");
      r.getChild("1").getChild("1-1").addChild("1-1-3");
      
      ((NodeList) r.getChild("1").getChild("1-1").getChild("1-1-2").getChildren()).setLoaded(true);

      visitor = new SaveNodeVisitor(r.getChild("1").getChild("1-1"));
   }

   @Test
   public void notOnPath()
   {
      assertFalse(visitor.visit(1, "2", createDetails("2")));
      assertFalse(visitor.visit(2, "1", createDetails("1/1")));
      assertFalse(visitor.visit(2, "1-2", createDetails("1/1-2")));
      assertFalse(visitor.visit(2, "1-1-1", createDetails("1/1-1-1")));
   }

   @Test
   public void onPath()
   {
      assertTrue(visitor.visit(1, "1", createDetails("1")));
      assertTrue(visitor.visit(2, "1-1", createDetails("1/1-1")));
   }

   @Test
   public void childrenLoaded()
   {
      assertTrue(visitor.visit(3, "1-1-2", createDetails("1/1/1-1-2")));
   }

   @Test
   public void childrenNotEmpty()
   {
      assertTrue(visitor.visit(3, "1-1-1", createDetails("1/1/1-1-1")));
   }

   @Test
   public void notLoaded()
   {
      assertFalse(visitor.visit(3, "1-1-3", createDetails("1/1/1-1-3")));
      assertFalse(visitor.visit(3, "1-1-4", createDetails("1/1/1-1-4")));
   }

   @Test
   public void root()
   {
      assertTrue(visitor.visit(0, null, null));
   }

   static NodeDetails createDetails(final String path)
   {
      return new NodeDetails()
      {
         @Override
         public Visibility getVisibility()
         {
            return null;
         }

         @Override
         public PageId getPageId()
         {
            return null;
         }

         @Override
         public NodePath getNodePath()
         {
            return NodePath.fromString(path);
         }

         @Override
         public String getIconName()
         {
            return null;
         }
      };
   }

}