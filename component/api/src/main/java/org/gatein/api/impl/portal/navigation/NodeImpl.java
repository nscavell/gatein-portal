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

import org.gatein.api.ApiException;
import org.gatein.api.internal.Objects;
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
public class NodeImpl implements Node
{
   private final String name;
   private Node parent;
   private Label label;
   private Visibility visibility;
   private String iconName;
   private PageId pageId;
   private NodeList children;
   
   /**
    * Creates a new node with the specified name.
    *
    * @param name the name of the node.
    */
   public NodeImpl(String name)
   {
      if (name == null) throw new IllegalArgumentException("name cannot be null");

      this.name = name;
      this.visibility = new Visibility();
      this.children = new NodeList(this);
   }

   /**
    * Creates a new node from the original node object. This is a deep copy, meaning it will copy all
    * children, grandchildren, etc.
    *
    * @param original the node to copy from. Changing this node has no impact on the original node.
    */
   public NodeImpl(Node original)
   {
      this(original.getName(), original, null); // dissociate the parent
   }

   /**
    * Creates a new node with a new name, copying from the original node object. This is a deep copy, meaning it
    * will copy all children, grandchildren, etc.
    *
    * @param name     the new name of the node
    * @param original the node to copy from. Changing this node has no impact on the original node.
    */
   public NodeImpl(String name, Node original)
   {
      this(name, original, null); //dissociate the parent
   }

   private NodeImpl(String name, Node node, NodeImpl parent)
   {
      if (name == null) throw new IllegalArgumentException("name cannot be null");
      if (node == null) throw new IllegalArgumentException("node cannot be null");

      this.name = name;
      this.parent = parent;
      this.label = (node.getLabel() == null) ? null : new Label(node.getLabel());
      this.visibility = node.getVisibility();
      this.iconName = node.getIconName();
      this.pageId = node.getPageId();
      this.children = new NodeList(this, node.getChildren());
   }

   // Used for root node
   NodeImpl()
   {
      this.name = null;
      this.children = new NodeList(this);
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public Node getParent()
   {
      return parent;
   }

   @Override
   public NodePath getNodePath()
   {
      NodePath path = (name == null) ? NodePath.root() : NodePath.path(name);
      if (parent != null)
      {
         path = parent.getNodePath().append(path);
      }
      return path;
   }

   @Override
   public URI getResolvedURI()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Label getLabel()
   {
      return label;
   }

   @Override
   public void setLabel(Label label)
   {
      this.label = label;
   }

   @Override
   public String getResolvedLabel()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public boolean isVisible()
   {
      return visibility.isVisible();
   }

   @Override
   public Visibility getVisibility()
   {
      return visibility;
   }

   @Override
   public void setVisibility(Visibility visibility)
   {
      if (visibility == null) throw new IllegalArgumentException("visibility cannot be null");

      this.visibility = visibility;
   }

   @Override
   public void setVisibility(boolean visible)
   {
      Visibility.Flag flag = (visible) ? Visibility.Flag.VISIBLE : Visibility.Flag.HIDDEN;
      this.visibility = new Visibility(flag, visibility.getPublicationDate());
   }

   @Override
   public void setVisibility(PublicationDate publicationDate)
   {
      Visibility.Flag flag = visibility.getFlag();
      if (publicationDate != null)
      {
         flag = Visibility.Flag.PUBLICATION;
      }
      else if (flag == Visibility.Flag.PUBLICATION)
      {
         flag = Visibility.Flag.VISIBLE;
      }

      this.visibility = new Visibility(flag, publicationDate);
   }

   @Override
   public String getIconName()
   {
      return iconName;
   }

   @Override
   public void setIconName(String iconName)
   {
      this.iconName = iconName;
   }

   @Override
   public PageId getPageId()
   {
      return pageId;
   }

   @Override
   public void setPageId(PageId pageId)
   {
      this.pageId = pageId;
   }

   @Override
   public boolean isRoot()
   {
      return parent == null;
   }

   @Override
   public Node addChild(String childName)
   {
      Node child = new NodeImpl(childName);
      if (!children.add(child)) throw new ApiException("Could not add child " + childName + " to node " + this);

      return child;
   }

   @Override
   public Node getChild(String childName)
   {
      return children.get(childName);
   }

   @Override
   public Node getChild(int index)
   {
      return children.get(index);
   }

   @Override
   public List<Node> getChildren()
   {
      return children;
   }

   @Override
   public boolean hasChild(String childName)
   {
      return getChild(childName) != null;
   }

   void setParent(Node parent)
   {
      this.parent = parent;
   }

   @Override
   public boolean isChildrenLoaded()
   {
      return children.isLoaded();
   }

   @Override
   public int indexOf(String childName)
   {
      return children.indexOf(childName);
   }

   @Override
   public boolean removeChild(String childName)
   {
      return children.remove(childName);
   }

   @Override
   public Node filter(Filter<Node> filter)
   {
      return new FilteredNode(filter, this);
   }

   @Override
   public void sort(Comparator<Node> comparator)
   {
      children.sort(comparator);
   }

   @Override
   public Node copy()
   {
      return new NodeImpl(this);
   }

   @Override
   public Node copy(String newName)
   {
      return new NodeImpl(newName, this);
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

   @Override
   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (!(o instanceof NodeImpl)) return false;

      NodeImpl node = (NodeImpl) o;

      if (!Objects.equals(name, node.name)) return false;
      if (!Objects.equals(label, node.label)) return false;
      if (!Objects.equals(visibility, node.visibility)) return false;
      if (!Objects.equals(iconName, node.iconName)) return false;
      if (!Objects.equals(pageId, node.pageId)) return false;
      if (!Objects.equals(children, node.children)) return false;

      return true;
   }

   @Override
   public int hashCode()
   {
      int result = 31 + (name != null ? name.hashCode() : 0);
      result = 31 * result + (label != null ? label.hashCode() : 0);
      result = 31 * result + (visibility != null ? visibility.hashCode() : 0);
      result = 31 * result + (iconName != null ? iconName.hashCode() : 0);
      result = 31 * result + (pageId != null ? pageId.hashCode() : 0);
      result = 31 * result + (children != null ? children.hashCode() : 0);
      return result;
   }
}