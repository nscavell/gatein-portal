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

package org.gatein.api.impl.portal;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class NodeImpl //implements Node
{
   //TODO: Do we want a better name for logger ? Probably need to standardize our logging for api
   //private static final Logger log = LoggerFactory.getLogger(NodeImpl.class);

   //final NodeContext<Node> context;

//   private Id id;
//   private final SiteImpl site;
//   private final GateInImpl gateIn;
//   private final Label label;
//
//   NodeImpl(SiteImpl site, GateInImpl gateIn, NodeContext<Node> context)
//   {
//      this.site = site;
//      this.gateIn = gateIn;
//      this.context = context;
//      this.label = new NodeLabel(gateIn.getDescriptionService());
//   }
//
//   @Override
//   public Id getId()
//   {
//      // Can't calculate the id in the constructor (which includes the path) because the internal service is still creating the node.
//      if (id == null)
//      {
//         String buildPath = buildPath().toString();
//         this.id = Node.Id.create(site.getId(), buildPath.split("/"));
//      }
//      return id;
//   }
//
//   @Override
//   public String getName()
//   {
//      return context.getName();
//   }
//
//   @Override
//   public Node getParent()
//   {
//      return context.getParentNode();
//   }
//
//   @Override
//   public boolean removeNode()
//   {
//      try
//      {
//         NodeContext<Node> parent = context.getParent();
//         boolean removed = context.removeNode();
//         gateIn.getNavigationService().saveNode(parent, null);
//         return removed;
//      }
//      catch (IllegalStateException e)
//      {
//         log.error("Invalid internal state, could not remove node.", e);
//         throw new ApiException("Could not remove node. See log for details.");
//      }
//   }
//
//   @Override
//   public Node getChild(String name)
//   {
//      throwIllegalArgExceptionIfNull(name, "name");
//
//      loadChildren(context);
//
//      return context.getNode(name);
//   }
//
//   @Override
//   public boolean removeChild(String name) throws EntityNotFoundException
//   {
//      throwIllegalArgExceptionIfNull(name, "name");
//
//      Node node = getChild(name);
//      if (node == null) throw new EntityNotFoundException("Cannot remove child because child " + name + " does not exist for node " + this);
//
//      return node.removeNode();
//   }
//
//   @Override
//   public Node addChild(String name)
//   {
//      loadChildren(context);
//
//      if (context.get(name) != null) throw new EntityAlreadyExistsException("Child node " + name + " already exists for node " + this);
//
//      Node node = context.add(null, name).getNode();
//      gateIn.getNavigationService().saveNode(context, null);
//
//      return node;
//   }
//
//   @Override
//   public int getChildCount()
//   {
//      loadChildren(context);
//
//      return context.getNodeSize();
//   }
//
//   @Override
//   public URI getURI()
//   {
//      try
//      {
//         SiteKey siteKey = site.getSiteKey();
//         RequestContext requestContext = RequestContext.getCurrentInstance();
//         NavigationResource navResource = new NavigationResource(siteKey, getId().getPathAsString());
//         NodeURL nodeURL = requestContext.createURL(NodeURL.TYPE, navResource);
//         nodeURL.setSchemeUse(true);
//         return new URI(nodeURL.toString());
//      }
//      catch (URISyntaxException e)
//      {
//         throw new RuntimeException(e);
//      }
//   }
//
//   @Override
//   public Label getLabel()
//   {
//      return label;
//   }
//
//   @Override
//   public String getIconName()
//   {
//      return context.getState().getIcon();
//   }
//
//   @Override
//   public void setIconName(String iconName)
//   {
//      context.setState(new NodeState.Builder(context.getState()).icon(iconName).build());
//   }
//
//   @Override
//   public Page getPage()
//   {
//      Page.Id pageId = getPageId();
//      if (pageId == null) return null;
//
//      Site site = gateIn.getSite(pageId.getSiteId());
//      if (site == null) throw new ApiException("Invalid page id " + pageId + " because the site does not exist.");
//
//      return site.getPage(pageId.getPageName());
//   }
//
//   @Override
//   public Page.Id getPageId()
//   {
//      String pageRef = context.getState().getPageRef();
//      if (pageRef == null) return null;
//
//      PageKey pageKey = PageKey.create(pageRef);
//      SiteKey siteKey = new SiteKey(pageKey.getType(), pageKey.getId());
//      Site.Id siteId = SiteImpl.fromSiteKey(siteKey);
//
//      return Page.Id.create(siteId, pageKey.getName());
//   }
//
//   @Override
//   public void setPageId(Page.Id pageId)
//   {
//      String pageRef = null;
//      if (pageId != null)
//      {
//         Site site = gateIn.getSite(pageId.getSiteId());
//         if (site == null) throw new EntityNotFoundException("Cannot set page id " + pageId + " because site does not exist.");
//         Page page = site.getPage(pageId.getPageName());
//         if (page == null) throw new EntityNotFoundException("Cannot set page id " + pageId + " because page does not exist.");
//
//         SiteKey siteKey = SiteImpl.toSiteKey(pageId.getSiteId());
//         pageRef = new PageKey(siteKey.getTypeName(), siteKey.getName(), pageId.getPageName()).getCompositeId();
//      }
//
//      context.setState(context.getState().builder().pageRef(pageRef).build());
//   }
//
//   @Override
//   public Visibility getVisibility()
//   {
//      switch (context.getState().getVisibility())
//      {
//         case DISPLAYED:
//            return Visibility.VISIBLE;
//         case HIDDEN:
//            return Visibility.HIDDEN;
//         case TEMPORAL:
//            return Visibility.PUBLICATION;
//         case SYSTEM:
//            return Visibility.SYSTEM;
//         default:
//            throw new RuntimeException("Cannot map " + context.getState().getVisibility() + " to a valid visibility.");
//      }
//   }
//
//   @Override
//   public Date getStartPublicationDate()
//   {
//      long startPublicationTime = context.getState().getStartPublicationTime();
//      return startPublicationTime != -1 ? new Date(startPublicationTime) : null;
//   }
//
//   @Override
//   public Date getEndPublicationDate()
//   {
//      long endPublicationTime = context.getState().getEndPublicationTime();
//      return endPublicationTime != -1 ? new Date(endPublicationTime) : null;
//   }
//
//   @Override
//   public void setPublication(Date start, Date end)
//   {
//      throwIllegalArgExceptionIfNull(start, "start date");
//      throwIllegalArgExceptionIfNull(start, "end date");
//
//      NodeState state = new NodeState.Builder(context.getState())
//         .startPublicationTime(start.getTime())
//         .endPublicationTime(end.getTime()).build();
//
//      context.setState(state);
//   }
//
//   @Override
//   public void setPublication(Date start)
//   {
//      throwIllegalArgExceptionIfNull(start, "start");
//
//      NodeState state = new NodeState.Builder(context.getState())
//         .startPublicationTime(start.getTime())
//         .endPublicationTime(-1).build();
//
//      context.setState(state);
//   }
//
//   @Override
//   public void removePublication()
//   {
//      NodeState state = new NodeState.Builder(context.getState())
//         .startPublicationTime(-1)
//         .endPublicationTime(-1).build();
//
//      context.setState(state);
//   }
//
//   @Override
//   public void setVisible(boolean visible)
//   {
//      NodeState state = new NodeState.Builder(context.getState())
//         .visibility(org.exoplatform.portal.mop.Visibility.DISPLAYED).build();
//
//      context.setState(state);
//   }
//
//   @Override
//   public void moveUp()
//   {
//      //TODO: Implement or think of better way of "moving" nodes around.
//      throw new NotYetImplemented();
//   }
//
//   @Override
//   public void moveDown()
//   {
//      //TODO: Implement or think of better way of "moving" nodes around.
//      throw new NotYetImplemented();
//   }
//
//   @Override
//   public Iterator<Node> iterator()
//   {
//      loadChildren(context);
//      return context.iterator();
//   }
//
//   @Override
//   public String toString()
//   {
//      return getId().toString();
//   }
//
//   private void loadChildren(NodeContext<Node> ctx)
//   {
//      if (!ctx.isExpanded())
//      {
//         gateIn.getNavigationService().rebaseNode(ctx, Scope.CHILDREN, null);
//      }
//   }
//
//   private StringBuilder buildPath()
//   {
//      NodeImpl parent = (NodeImpl) context.getParentNode();
//      if (parent != null)
//      {
//         StringBuilder builder = parent.buildPath();
//         if (builder.length() > 0)
//         {
//            builder.append('/');
//         }
//         return builder.append(context.getName());
//      }
//      else
//      {
//         return new StringBuilder();
//      }
//   }
//
//   private class NodeLabel extends AbstractLabel
//   {
//      public NodeLabel(DescriptionService service)
//      {
//         super(service);
//      }
//
//      @Override
//      public String getDescriptionId()
//      {
//         return context.getId();
//      }
//
//      @Override
//      public String getSimpleValue()
//      {
//         return context.getState().getLabel();
//      }
//
//      @Override
//      public String getDefaultName()
//      {
//         return context.getName();
//      }
//
//      @Override
//      public void setValue(String value)
//      {
//         context.setState(new NodeState.Builder(context.getState()).label(value).build());
//      }
//
//      @Override
//      public ResourceBundle getResourceBundle()
//      {
//         return gateIn.getNavigationResourceBundle(site.getId());
//      }
//
//      @Override
//      public Locale getUserLocale()
//      {
//         return gateIn.getUserLocale();
//      }
//
//      @Override
//      public Locale getPortalLocale()
//      {
//         return site.getLocale();
//      }
//   }
}
