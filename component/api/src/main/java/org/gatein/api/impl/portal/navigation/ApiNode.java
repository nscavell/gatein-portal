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

import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.NodeState.Builder;
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

final class ApiNode implements Node
{
   private NodeContext<ApiNode> context;

   private Label label;

   private boolean labelChanged;

   private SiteId siteId;

   ApiNode(SiteId siteId, NodeContext<ApiNode> context)
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
         for (NodeContext<ApiNode> node = context.getFirst(); node != null; node = node.getNext())
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
      NodeContext<ApiNode> c = getDescendantContext(nodePath);
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
      return label;
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
      ApiNode parent = context.getParentNode();

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
      return Util.from(context.getState().getPageRef());
   }

   @Override
   public Node getParent()
   {
      return context.getParentNode();
   }

   @Override
   public String getResolvedLabel()
   {
      return LabelResolver.resolveLabel(this);
   }

   @Override
   public URI getResolvedURI()
   {
      return URIResolver.resolveURI(this);
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
      NodeContext<ApiNode> parent = context.getParent();
      context.remove();
      parent.add(index, context);
   }

   @Override
   public void moveTo(int index, Node parent)
   {
      context.remove();
      ((ApiNode) parent).context.add(index, context);
   }

   @Override
   public void moveTo(Node parent)
   {
      context.remove();
      ((ApiNode) parent).context.add(null, context);
   }

   @Override
   public boolean removeChild(String childName)
   {
      return context.removeNode(childName);
   }

   @Override
   public void setIconName(String iconName)
   {
      setState(getStateBuilder().icon(iconName));
   }

   @Override
   public void setLabel(Label label)
   {
      if (!label.equals(this.label))
      {
         if (!label.isLocalized())
         {
            setState(getStateBuilder().label(label.getValue()));
            this.label = label;
         }
         else
         {
            setState(getStateBuilder().label(null));
            this.label = label;
         }
         labelChanged = true;
      }
   }

   @Override
   public void setPageId(PageId pageId)
   {
      setState(getStateBuilder().pageRef(Util.from(pageId)));
   }

   @Override
   public void setVisibility(boolean visible)
   {
      Builder b = getStateBuilder().startPublicationTime(-1).endPublicationTime(-1);
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

      setState(getStateBuilder().startPublicationTime(start).endPublicationTime(end)
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
         setState(getStateBuilder().startPublicationTime(-1).endPublicationTime(-1)
               .visibility(ObjectFactory.createVisibility(visibility.getFlag())));
      }
   }

   @Override
   public void sort(Comparator<Node> comparator)
   {
      if (context.isExpanded())
      {
         ApiNode[] a = new ApiNode[context.getNodeSize()];
         for (NodeContext<ApiNode> c = context.getFirst(); c != null; c = c.getNext())
         {
            a[c.getIndex()] = c.getNode();
         }

         Arrays.sort(a, comparator);

         for (int i = 0; i < a.length; i++)
         {
            ApiNode n = a[i];
            NodeContext<ApiNode> c = n.getContext();
            if (c.getIndex() != i)
            {
               c.getNode().moveTo(i);
            }
         }
      }
   }

   @Override
   public String toString()
   {
      return Objects.toStringBuilder(getClass()).add("name", getName()).add("path", getNodePath()).add("label", getLabel())
            .add("visibility", getVisibility()).add("iconName", getIconName()).add("pageId", getPageId()).toString();
   }

   NodeContext<ApiNode> getContext()
   {
      return context;
   }

   NodeContext<ApiNode> getDescendantContext(NodePath nodePath)
   {
      NodeContext<ApiNode> c = context;
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

   SiteId getSiteId()
   {
      return siteId;
   }

   boolean isLabelChanged()
   {
      return labelChanged;
   }

   void setLabelInternal(Label label)
   {
      this.label = label;
   }

   private Builder getStateBuilder()
   {
      return new NodeState.Builder(context.getState());
   }

   private void setState(Builder builder)
   {
      context.setState(builder.build());
   }

   private class ApiNodeModelIterator implements Iterator<Node>
   {
      private Iterator<ApiNode> itr = context.iterator();
      private ApiNode last;

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
