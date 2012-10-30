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

import org.gatein.api.ApiException;
import org.gatein.api.portal.Label;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.PublicationDate;
import org.gatein.api.portal.navigation.Visibility;
import org.gatein.api.portal.page.Page.Id;

public class RootNode extends Node
{
   private static final long serialVersionUID = 1L;

   public RootNode()
   {
      super("root");
   }

   @Override
   public void setIconName(String iconName)
   {
      throw new ApiException("Can't set icon name on root node");
   }

   @Override
   public void setLabel(Label label)
   {
      throw new ApiException("Can't set icon name on root node");
   }

   @Override
   public void setPageId(Id pageId)
   {
      // TODO Auto-generated method stub
      super.setPageId(pageId);
   }

   @Override
   public void setVisibility(boolean visible)
   {
      throw new ApiException("Can't set visibility on root node");
   }

   @Override
   public void setVisibility(PublicationDate publicationDate)
   {
      throw new ApiException("Can't set visibility on root node");
   }

   @Override
   public void setVisibility(Visibility visibility)
   {
      throw new ApiException("Can't set visibility on root node");
   }
}
