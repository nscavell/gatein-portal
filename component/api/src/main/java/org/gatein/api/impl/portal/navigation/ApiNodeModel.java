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

import java.io.IOException;
import java.net.URI;
import java.util.Comparator;
import java.util.Iterator;

import javax.persistence.Transient;

import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.NodeState.Builder;
import org.exoplatform.portal.mop.page.PageKey;
import org.gatein.api.ApiException;
import org.gatein.api.impl.Util;
import org.gatein.api.internal.Objects;
import org.gatein.api.portal.Label;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.portal.navigation.PublicationDate;
import org.gatein.api.portal.navigation.Visibility;
import org.gatein.api.portal.navigation.Visibility.Flag;
import org.gatein.api.portal.page.PageId;
import org.gatein.api.portal.site.SiteId;
import org.gatein.api.util.Filter;

final class ApiNodeModel implements Node
{
   @Transient
   private NodeContext<ApiNodeModel> context;

   private SiteId siteId;

   ApiNodeModel(SiteId siteId, NodeContext<ApiNodeModel> context)
   {
      this.siteId = siteId;
      this.context = context;
   }

   @Override
   public Node addChild(String childName)
   {
      return context.add(null, childName).getNode();
   }

   @Override
   public void filter(Filter<Node> filter)
   {
      context.setHidden(!filter.accept(this));
      if (context.isExpanded())
      {
         for (NodeContext<ApiNodeModel> node = context.getFirst(); node != null; node = node.getNext())
         {
            node.getNode().filter(filter);
         }
      }
   }

   @Override
   public Node getChild(int index)
   {
      return context.isExpanded() ? context.getNode(index) : null;
   }

   @Override
   public Node getChild(String childName)
   {
      return context.isExpanded() ? context.getNode(childName) : null;
   }

   @Override
   public int getChildCount()
   {
      return context.getNodeSize();
   }

   @Override
   public Node getDescendant(NodePath nodePath)
   {
      NodeContext<ApiNodeModel> c = getDescendantContext(nodePath);
      return c != null ? c.getNode() : null;
   }

   @Override
   public String getIconName()
   {
      return context.getState().getIcon();
   }

   @Override
   public Label getLabel()
   {
      // TODO Do we support lazy load of labels?
      return null;
   }

   @Override
   public String getName()
   {
      return context.getName();
   }

   @Override
   public NodePath getNodePath()
   {
      String name = getName();
      ApiNodeModel parent = context.getParentNode();

      NodePath path = (name == null) ? NodePath.root() : NodePath.path(name);
      if (parent != null)
      {
         path = parent.getNodePath().append(path);
      }

      return path;
   }

   @Override
   public PageId getPageId()
   {
      PageKey pageRef = context.getState().getPageRef();
      return pageRef != null ? Util.from(context.getState().getPageRef()) : null;
   }

   @Override
   public Node getParent()
   {
      return context.getParentNode();
   }

   @Override
   public String getResolvedLabel()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public URI getResolvedURI()
   {
      return URIResolver.resolveURI(getSiteId(), getNodePath());
   }

   @Override
   public Visibility getVisibility()
   {
      return ObjectFactory.createVisibility(context.getState());
   }

   @Override
   public boolean hasChild(String childName)
   {
      return context.get(childName) != null;
   }

   @Override
   public int indexOf(String childName)
   {
      return context.get(childName).getIndex();
   }

   @Override
   public boolean isChildrenLoaded()
   {
      return context.isExpanded();
   }

   @Override
   public boolean isRoot()
   {
      return context.getParent() == null;
   }

   @Override
   public boolean isVisible()
   {
      return getVisibility().isVisible();
   }

   @Override
   public Iterator<Node> iterator()
   {
      return new ApiNodeModelIterator();
   }

   @Override
   public void moveTo(int index)
   {
      context.getParent().add(index, context);
   }

   @Override
   public void moveTo(int index, Node parent)
   {
      ((ApiNodeModel) parent).context.add(index, context);
   }

   @Override
   public void moveTo(Node parent)
   {
      ((ApiNodeModel) parent).context.add(null, context);
   }

   @Override
   public boolean removeChild(String childName)
   {
      return context.removeNode(childName);
   }

   @Override
   public void setIconName(String iconName)
   {
      setState(getState().icon(iconName));
   }

   @Override
   public void setLabel(Label label)
   {
      // TODO Auto-generated method stub
   }

   @Override
   public void setPageId(PageId pageId)
   {
      setState(getState().pageRef(Util.from(pageId)));
   }

   @Override
   public void setVisibility(boolean visible)
   {
      Builder b = getState().startPublicationTime(-1).endPublicationTime(-1);
      if (visible)
      {
         b.visibility(org.exoplatform.portal.mop.Visibility.DISPLAYED);
      }
      else
      {
         b.visibility(org.exoplatform.portal.mop.Visibility.HIDDEN);
      }
      setState(b);
   }

   @Override
   public void setVisibility(PublicationDate publicationDate)
   {
      long start = publicationDate.getStart() != null ? publicationDate.getStart().getTime() : -1;
      long end = publicationDate.getEnd() != null ? publicationDate.getEnd().getTime() : -1;

      setState(getState().startPublicationTime(start).endPublicationTime(end)
            .visibility(org.exoplatform.portal.mop.Visibility.TEMPORAL));
   }

   @Override
   public void setVisibility(Visibility visibility)
   {
      if (visibility.getFlag() == Flag.PUBLICATION)
      {
         setVisibility(visibility.getPublicationDate());
      }
      else
      {
         Builder b = getState().startPublicationTime(-1).endPublicationTime(-1);
         switch (visibility.getFlag())
         {
            case VISIBLE:
               b.visibility(org.exoplatform.portal.mop.Visibility.DISPLAYED);
               break;
            case HIDDEN:
               b.visibility(org.exoplatform.portal.mop.Visibility.HIDDEN);
               break;
            case SYSTEM:
               b.visibility(org.exoplatform.portal.mop.Visibility.SYSTEM);
               break;
            default:
               throw new ApiException("Unknown visibility flag " + visibility.getFlag());
         }
         setState(b);
      }
   }

   @Override
   public void sort(Comparator<Node> comparator)
   {
      // TODO Auto-generated method stub
   }

   @Override
   public String toString()
   {
      return Objects.toStringBuilder(getClass())
         .add("name", getName())
         .add("path", getNodePath())
         .add("label", getLabel())
         .add("visibility", getVisibility())
         .add("iconName", getIconName())
         .add("pageId", getPageId())
         .toString();
   }


   NodeContext<ApiNodeModel> getContext()
   {
      return context;
   }

   NodeContext<ApiNodeModel> getDescendantContext(NodePath nodePath)
   {
      NodeContext<ApiNodeModel> c = context;
      for (String e : nodePath)
      {
         c = c.get(e);
         if (c == null)
         {
            return null;
         }
      }
      return c;
   }
   private SiteId getSiteId()
   {
      if (siteId != null)
      {
         return siteId;
      }
      else
      {
         ApiNodeModel parent = context.getParentNode();
         return parent != null ? parent.getSiteId() : null;
      }
   }

   private Builder getState()
   {
      return new NodeState.Builder(context.getState());
   }

   private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      // TODO Read 'context'
   }

   private void setState(Builder builder)
   {
      context.setState(builder.build());
   }

   private void writeObject(java.io.ObjectOutputStream out) throws IOException
   {
      out.defaultWriteObject();
      // TODO Write 'context'
   }

   private class ApiNodeModelIterator implements Iterator<Node>
   {
      private Iterator<ApiNodeModel> itr = context.iterator();
      private ApiNodeModel last;

      @Override
      public boolean hasNext()
      {
         return itr.hasNext();
      }

      @Override
      public Node next()
      {
         last = itr.next();
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
   }
}
