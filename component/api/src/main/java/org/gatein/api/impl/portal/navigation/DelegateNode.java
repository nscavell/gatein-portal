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

import org.gatein.api.portal.Label;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.portal.navigation.PublicationDate;
import org.gatein.api.portal.navigation.Visibility;
import org.gatein.api.portal.page.PageId;
import org.gatein.api.util.Filter;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class DelegateNode implements Node
{
   final Node delegate;

   public DelegateNode(Node delegate)
   {
      this.delegate = delegate;
   }

   @Override
   public String getName()
   {
      return delegate.getName();
   }

   @Override
   public Node getParent()
   {
      return delegate.getParent();
   }

   @Override
   public NodePath getNodePath()
   {
      return delegate.getNodePath();
   }

   @Override
   public URI getResolvedURI()
   {
      return delegate.getResolvedURI();
   }

   @Override
   public Label getLabel()
   {
      return delegate.getLabel();
   }

   @Override
   public void setLabel(Label label)
   {
      delegate.setLabel(label);
   }

   @Override
   public String getResolvedLabel()
   {
      return delegate.getResolvedLabel();
   }

   @Override
   public boolean isVisible()
   {
      return delegate.isVisible();
   }

   @Override
   public Visibility getVisibility()
   {
      return delegate.getVisibility();
   }

   @Override
   public void setVisibility(Visibility visibility)
   {
      delegate.setVisibility(visibility);
   }

   @Override
   public void setVisibility(boolean visible)
   {
      delegate.setVisibility(visible);
   }

   @Override
   public void setVisibility(PublicationDate publicationDate)
   {
      delegate.setVisibility(publicationDate);
   }

   @Override
   public String getIconName()
   {
      return delegate.getIconName();
   }

   @Override
   public void setIconName(String iconName)
   {
      delegate.setIconName(iconName);
   }

   @Override
   public PageId getPageId()
   {
      return delegate.getPageId();
   }

   @Override
   public void setPageId(PageId pageId)
   {
      delegate.setPageId(pageId);
   }

   @Override
   public boolean isRoot()
   {
      return delegate.isRoot();
   }

   @Override
   public Node addChild(String childName)
   {
      return delegate.addChild(childName);
   }

   @Override
   public Node getChild(String childName)
   {
      return delegate.getChild(childName);
   }

   @Override
   public Node getChild(int index)
   {
      return delegate.getChild(index);
   }

   @Override
   public List<Node> getChildren()
   {
      return delegate.getChildren();
   }

   @Override
   public boolean hasChild(String childName)
   {
      return delegate.hasChild(childName);
   }

   @Override
   public boolean isChildrenLoaded()
   {
      return delegate.isChildrenLoaded();
   }

   @Override
   public int indexOf(String childName)
   {
      return delegate.indexOf(childName);
   }

   @Override
   public boolean removeChild(String childName)
   {
      return delegate.removeChild(childName);
   }

   @Override
   public Node filter(Filter<Node> filter)
   {
      return delegate.filter(filter);
   }

   @Override
   public void sort(Comparator<Node> comparator)
   {
      delegate.sort(comparator);
   }

   @Override
   public Node copy()
   {
      return delegate.copy();
   }

   @Override
   public Node copy(String newName)
   {
      return delegate.copy(newName);
   }

   @Override
   public String toString()
   {
      return delegate.toString();
   }

   @Override
   public boolean equals(Object o)
   {
      return delegate.equals(o);
   }

   @Override
   public int hashCode()
   {
      return delegate.hashCode();
   }
}
