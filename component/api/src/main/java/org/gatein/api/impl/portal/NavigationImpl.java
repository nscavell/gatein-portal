/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
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

package org.gatein.api.impl.portal;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@redhat.com">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 */
public class NavigationImpl// implements Navigation
{
//   private final SiteImpl site;
//   private final NavigationContext context;
//   private final NodeContext<Node> nodeContext;

//   public NavigationImpl(SiteImpl site, GateInImpl gateIn, NavigationContext context)
//   {
//      if (context == null) throw new IllegalStateException("Navigation does not exist for site " + site);
//
//      this.site = site;
//      this.context = context;
//      this.nodeContext = gateIn.getNavigationService().loadNode(new NavigationNodeModel(site, gateIn), context, Scope.SINGLE, null);
//   }

//   @Override
//   public int getPriority()
//   {
//      Integer priority = context.getState().getPriority();
//      return (priority == null) ? 1 : priority;
//   }
//
//   @Override
//   public void setPriority(int priority)
//   {
//      NavigationState state =  new NavigationState(priority);
//      context.setState(state);
//   }
//
//   @Override
//   public Node getNode(String... path)
//   {
//      throwIllegalArgExceptionIfNullOrEmpty(path, "path");
//
//      Node node = nodeContext.getNode();
//      for (String name : path)
//      {
//         node = node.getChild(name);
//         if (node == null) return null;
//      }
//
//      return node;
//   }
//
//   @Override
//   public boolean removeNode(String... path) throws EntityNotFoundException
//   {
//      throwIllegalArgExceptionIfNullOrEmpty(path, "path");
//
//      Node node = getNode(path);
//      if (node == null) throw new EntityNotFoundException("Cannot find node for path " + Arrays.asList(path));
//
//      return node.removeNode();
//   }
//
//   @Override
//   public Node addNode(String...path) throws EntityNotFoundException
//   {
//      throwIllegalArgExceptionIfNullOrEmpty(path, "path");
//      if (path.length == 1)
//      {
//         return nodeContext.getNode().addChild(path[0]);
//      }
//
//      String[] parentPath = new String[path.length-1];
//      System.arraycopy(path, 0, parentPath, 0, path.length-1);
//
//      Node node = getNode(parentPath);
//      if (node == null) throw new EntityNotFoundException("Cannot add node " + path[path.length-1] +" because parent for path " + Arrays.asList(parentPath) + " does not exist.");
//
//      return node.addChild(path[path.length-1]);
//   }
//
//   @Override
//   public int getNodeCount()
//   {
//      return nodeContext.getNode().getChildCount();
//   }
//
//   @Override
//   public Iterator<Node> iterator()
//   {
//      return nodeContext.getNode().iterator();
//   }

   //   @Override
//   public String toString()
//   {
//      String pageRef = context.getState().getPageRef();
//      StringBuilder s = new StringBuilder("Navigation@").append(getId()).append(" URI: ").append(getURI());
//
//      if (pageRef != null)
//      {
//         s.append("-target->").append(pageRef);
//      }
//
//      if (context.getNodeCount() != 0)
//      {
//         loadChildrenIfNeeded();
//         s.append("\n|");
//         Iterator<NavigationImpl> children = context.iterator();
//         while (children.hasNext())
//         {
//            s.append("\n+--").append(children.next());
//         }
//         s.append("\n");
//      }
//
//      return s.toString();
//   }



//   public URI getURI()
//   {
//      if (uri != null)
//      {
//         return uri;
//      }
//      else
//      {
//         try
//         {
//            PortalKey key = site.getPortalKey();
//            RequestContext requestContext = RequestContext.getCurrentInstance();
//            SiteType siteType = SiteType.valueOf(key.getType().toUpperCase());
//            String siteName = key.getId();
//            NavigationResource navResource = new NavigationResource(siteType, siteName, buildURI().toString());
//            NodeURL nodeURL = requestContext.createURL(NodeURL.TYPE, navResource);
//            nodeURL.setSchemeUse(true);
//            uri = new URI(nodeURL.toString());
//         }
//         catch (URISyntaxException e)
//         {
//            throw new RuntimeException(e);
//         }
//         return uri;
//      }
//   }

//   private StringBuilder buildURI()
//   {
//      NavigationImpl parent = context.getParentNode();
//      if (parent != null)
//      {
//         StringBuilder builder = parent.buildURI();
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
//   public Page getTargetPage()
//   {
//      String pageRef = context.getState().getPageRef();
//      if (pageRef != null)
//      {
//         return site.pages.getPageByRef(pageRef);
//      }
//      else
//      {
//         return null;
//      }
//   }
//
//   public void setTargetPage(Page target)
//   {
//      if (target != null)
//      {
//         Site po = target.getSite();
//         PortalKey key = SiteImpl.createPortalKey(po.getId());
//         String ref = key.getType() + "::" + key.getId() + "::" + target.getName();
//         context.setState(context.getState().builder().pageRef(ref).build());
//      }
//      else
//      {
//
//         context.setState(context.getState().builder().pageRef(null).build());
//      }
//   }
//
//   @Override
//   public void setTargetPage(String targetId)
//   {
//      if (targetId != null)
//      {
//         context.setState(context.getState().builder().pageRef(targetId).build());
//      }
//      else
//      {
//
//         context.setState(context.getState().builder().pageRef(null).build());
//      }
//   }

//   public Site getSite()
//   {
//      return site.getSite();
//   }

//   public String getDisplayName()
//   {
//      // basically duplicating code from UserNode and PortalRequestContext because we can't use it as is
//      if (displayName == null)
//      {
//         String resolvedLabel = null;
//
//         String id = context.getId();
//
//         if (context.getState().getLabel() != null)
//         {
//            resolvedLabel = ExpressionUtil.getExpressionValue(getBundle(), context.getState().getLabel());
//         }
//         else if (id != null)
//         {
//            Locale userLocale = gateIn.getUserLocale();
//            DescriptionService descriptionService = gateIn.getDescriptionService();
//            Described.State description = descriptionService.resolveDescription(id, userLocale);
//            if (description != null)
//            {
//               resolvedLabel = description.getName();
//            }
//         }
//
//         //
//         if (resolvedLabel == null)
//         {
//            resolvedLabel = getName();
//         }
//
//         //
//         this.displayName = EntityEncoder.FULL.encode(resolvedLabel);
//      }
//      return displayName;
//   }

//   public ResourceBundle getBundle()
//   {
//      if (bundle == null)
//      {
//         PortalKey key = site.getPortalKey();
//         ExoContainer container = ExoContainerContext.getCurrentContainer();
//         ResourceBundleManager rbMgr = (ResourceBundleManager)container.getComponentInstanceOfType(ResourceBundleManager.class);
//         Locale locale = gateIn.getUserLocale();
//         bundle = rbMgr.getNavigationResourceBundle(
//            locale.getLanguage(),
//            key.getType(),
//            key.getId());
//
//         if (bundle == null)
//         {
//            bundle = EmptyResourceBundle.INSTANCE;
//         }
//      }
//      return bundle;
//   }

//   static class NavigationNodeModel implements NodeModel<Node>
//   {
//      private final SiteImpl site;
//      private final GateInImpl gateIn;
//
//      NavigationNodeModel(SiteImpl site, GateInImpl gateIn)
//      {
//         this.site = site;
//         this.gateIn = gateIn;
//      }
//
//      public NodeContext<Node> getContext(Node node)
//      {
//         return ((NodeImpl) node).context;
//      }
//
//      public Node create(NodeContext<Node> context)
//      {
//         return new NodeImpl(site, gateIn, context);
//      }
//   }
}
