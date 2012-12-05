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

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.navigation.NodeChange;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.NodeState.Builder;
import org.gatein.api.Portal;
import org.gatein.api.PortalRequest;
import org.gatein.api.impl.Util;
import org.gatein.api.internal.Objects;
import org.gatein.api.portal.LocalizedString;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.portal.navigation.NodeVisitor;
import org.gatein.api.portal.navigation.Nodes;
import org.gatein.api.portal.navigation.PublicationDate;
import org.gatein.api.portal.navigation.Visibility;
import org.gatein.api.portal.navigation.Visibility.Flag;
import org.gatein.api.portal.page.PageId;
import org.gatein.api.portal.site.SiteId;
import org.gatein.api.util.Filter;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
class ApiNode implements Node
{
   transient NodeContext<ApiNode> context;
   transient NavigationImpl navigation;

   private LocalizedString displayName;
   private String resolvedDisplayName;
   private boolean displayNameChanged;

   private final SiteId siteId;

   ApiNode(NavigationImpl navigation, NodeContext<ApiNode> context)
   {
      this.navigation = navigation;
      this.siteId = navigation.getSiteId();
      this.context = context;
   }

   @Override
   public Node addChild(int index, String childName)
   {
      return context.add(index, childName).getNode();
   }

   @Override
   public Node addChild(String childName)
   {
      return context.add(null, childName).getNode();
   }

   @Override
   public Node filter(Filter<Node> filter)
   {
      return new FilteredNode(navigation, context, filter);
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
   public Node getDescendant(String... nodePath)
   {
      return getDescendant(NodePath.path(nodePath));
   }

   @Override
   public Node getDescendant(NodePath nodePath)
   {
      Node node = this;
      for (String name : nodePath)
      {
         node = node.getChild(name);
         if (node == null) return  null;
      }

      return node;
   }

   @Override
   public String getIconName()
   {
      return context.getState().getIcon();
   }

   @Override
   public LocalizedString getDisplayName()
   {
      if (displayName == null)
      {
         String simple = context.getState().getLabel();
         if (simple != null)
         {
            displayName = new LocalizedString(simple);
         }
         else
         {
            Map<Locale, Described.State> descriptions = navigation.loadDescriptions(context.getId());
            displayName = ObjectFactory.createLocalizedString(descriptions);
         }
      }
      return displayName;
   }

   @Override
   public String getName()
   {
      return isRoot() ? null : context.getName();
   }

   @Override
   public NodePath getNodePath()
   {
      String name = getName();
      ApiNode parent = context.getParentNode();

      NodePath path = isRoot() ? NodePath.root() : NodePath.path(name);
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
   public String resolveDisplayName()
   {
      if (resolvedDisplayName == null)
      {
         resolvedDisplayName = navigation.resolve(context);
      }

      return resolvedDisplayName;
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
      checkRoot();

      NodeContext<ApiNode> parent = context.getParent();
      context.remove();
      parent.add(index, context);
   }

   @Override
   public void moveTo(int index, Node parent)
   {
      checkRoot();

      context.remove();
      ((ApiNode) parent).context.add(index, context);
   }

   @Override
   public void moveTo(Node parent)
   {
      checkRoot();

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
      checkRoot();

      setState(getStateBuilder().icon(iconName));
   }

   @Override
   public void setDisplayName(String displayName)
   {
      setDisplayName(new LocalizedString(displayName));
   }

   @Override
   public void setDisplayName(LocalizedString displayName)
   {
      checkRoot();
      if (displayName == null && this.displayName == null) return;

      if (displayName != null || !this.displayName.equals(displayName))
      {
         if (displayName != null && !displayName.isLocalized())
         {
            setState(getStateBuilder().label(displayName.getValue()));
         }
         else
         {
            setState(getStateBuilder().label(null));
         }
         this.displayName = displayName;
         this.resolvedDisplayName = null;
         displayNameChanged = true;
      }
   }

   @Override
   public void setPageId(PageId pageId)
   {
      checkRoot();

      setState(getStateBuilder().pageRef(Util.from(pageId)));
   }

   @Override
   public void setVisibility(boolean visible)
   {
      checkRoot();

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
      checkRoot();

      long start = publicationDate.getStart() != null ? publicationDate.getStart().getTime() : -1;
      long end = publicationDate.getEnd() != null ? publicationDate.getEnd().getTime() : -1;

      setState(getStateBuilder().startPublicationTime(start).endPublicationTime(end)
            .visibility(org.exoplatform.portal.mop.Visibility.TEMPORAL));
   }

   @Override
   public void setVisibility(Visibility visibility)
   {
      checkRoot();

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
      return Objects.toStringBuilder(getClass()).add("name", getName()).add("path", getNodePath())
            .add("visibility", getVisibility()).add("iconName", getIconName()).add("pageId", getPageId()).toString();
   }

   NodeContext<ApiNode> getContext()
   {
      return context;
   }

   SiteId getSiteId()
   {
      return siteId;
   }

   boolean isDisplayNameChanged()
   {
      return displayNameChanged;
   }

   void setDisplayNameInternal(LocalizedString displayName)
   {
      this.displayName = displayName;
      this.resolvedDisplayName = null;
   }

   private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();

      // deserialize serialization only fields
      NodePath nodePath = (NodePath) in.readObject();
      ApiNode parent = (ApiNode) in.readObject();
      boolean expanded = in.readBoolean();
      boolean hasChanges = in.readBoolean();

      PortalRequest request = PortalRequest.getInstance();
      Portal portal = (request == null) ? null : request.getPortal();
      if (portal != null)
      {
         navigation = (NavigationImpl) portal.getNavigation(siteId);
         if (navigation == null) throw new IOException("Could not retrieve navigation for site " + siteId);
      }
      else
      {
         throw new IOException("Could not retrieve portal API during deserialization.");
      }

      if (parent != null)
      {
         context = parent.context.get(nodePath.getLastSegment());
         if (expanded)
         {
            navigation.rebaseNodeContext(context, new NodeVisitorScope(Nodes.visitChildren()), null);
         }
      }
      else
      {
         NodeVisitor visitor = (expanded) ? Nodes.visitChildren() : Nodes.visitNone();
         context = navigation.getNodeContext(nodePath, visitor);
      }

      if (hasChanges && parent == null) // re-apply changes from root node
      {
         @SuppressWarnings("unchecked")
         List<ApiNodeChange> changes = (List<ApiNodeChange>) in.readObject();
         for (ApiNodeChange change : changes)
         {
            change.apply(this);
         }
      }
   }

   private void writeObject(java.io.ObjectOutputStream out) throws IOException
   {
      out.defaultWriteObject();

      // write serialization only fields
      out.writeObject(getNodePath());
      ApiNode parent = (context.getParent() != null) ? context.getParent().getNode() : null;
      out.writeObject(parent);
      out.writeBoolean(context.isExpanded());

      // serialize uncommitted changes
      boolean hasChanges = context.hasChanges();
      out.writeBoolean(hasChanges);
      if (hasChanges && parent == null) // ensures we only do this once since the changes are for the entire tree
      {
         List<ApiNodeChange> changes = new ArrayList<ApiNodeChange>();
         for (NodeChange<NodeContext<ApiNode>> change : context.getChanges())
         {
            if (change instanceof NodeChange.Created)
            {
               NodeChange.Created<NodeContext<ApiNode>> created = (NodeChange.Created<NodeContext<ApiNode>>) change;
               changes.add(new ApiNodeChange.Created(created));
            }
            else if (change instanceof NodeChange.Destroyed)
            {
               NodeChange.Destroyed<NodeContext<ApiNode>> destroyed = (NodeChange.Destroyed<NodeContext<ApiNode>>) change;
               changes.add(new ApiNodeChange.Destroyed(destroyed));
            }
            else if (change instanceof NodeChange.Moved)
            {
               NodeChange.Moved<NodeContext<ApiNode>> moved = (NodeChange.Moved<NodeContext<ApiNode>>) change;
               changes.add(new ApiNodeChange.Moved(moved));
            }
            else if (change instanceof NodeChange.Renamed)
            {
               NodeChange.Renamed<NodeContext<ApiNode>> renamed = (NodeChange.Renamed<NodeContext<ApiNode>>) change;
               changes.add(new ApiNodeChange.Renamed(renamed));
            }
            else if (change instanceof NodeChange.Updated)
            {
               NodeChange.Updated<NodeContext<ApiNode>> updated = (NodeChange.Updated<NodeContext<ApiNode>>) change;
               changes.add(new ApiNodeChange.Updated(updated));
            }
            else
            {
               throw new IOException("Cannot serialize: Non-compatible node change object " + change);
            }
         }
         out.writeObject(changes);
      }
   }

   private void checkRoot()
   {
      if (isRoot())
      {
         throw new UnsupportedOperationException("Operation not supported on root node");
      }
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
