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

package org.gatein.portal.api.impl.portal;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.exoplatform.portal.mop.navigation.NodeChangeListener;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.Scope;
import org.gatein.api.portal.Navigation;
import org.gatein.api.portal.Node;
import org.gatein.api.portal.Site;
import org.gatein.common.NotYetImplemented;
import org.gatein.portal.api.impl.GateInImpl;

import java.util.Iterator;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@redhat.com">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 */
public class NavigationImpl implements Navigation, NodeChangeListener<NodeContext<Node>>
{
   private final SiteImpl site;
   private final NavigationContext context;
   private final NodeContext<Node> nodeContext;

   public NavigationImpl(SiteImpl site, GateInImpl gateIn)
   {
      this.site = site;
      NavigationContext navContext = gateIn.getNavigationService().loadNavigation(site.getSiteKey());
      if (navContext == null)
      {
         navContext = new NavigationContext(site.getSiteKey(), new NavigationState(0));
         gateIn.getNavigationService().saveNavigation(navContext);
         this.context = navContext;
      }
      else
      {
         context = navContext;
      }
      this.nodeContext = gateIn.getNavigationService().loadNode(new NavigationNodeModel(site, gateIn), context, Scope.SINGLE, this);
   }

   @Override
   public int getPriority()
   {
      Integer priority = context.getState().getPriority();
      return (priority == null) ? 1 : priority;
   }

   @Override
   public void setPriority(int priority)
   {
      NavigationState state =  new NavigationState(priority);
      context.setState(state);
   }

   @Override
   public Node getNode(String... path)
   {
      return nodeContext.getNode().getDescendant(path);
   }

   @Override
   public void removeNode(String... path)
   {
      nodeContext.getNode().removeDescendant(path);
   }

   @Override
   public Node addNode(String name)
   {
      return nodeContext.getNode().addChild(name);
   }

   @Override
   public Iterator<Node> iterator()
   {
      return nodeContext.getNode().iterator();
   }

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

   public Site getSite()
   {
      return site.getSite();
   }

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

   @Override
   public void moveDown()
   {
      //TODO:
      throw new NotYetImplemented();
   }

   @Override
   public void moveUp()
   {
      //TODO:
      throw new NotYetImplemented();
   }

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


   @Override
   public void onAdd(NodeContext<Node> target, NodeContext<Node> parent, NodeContext<Node> previous)
   {
      System.out.println("onAdd");

   }

   @Override
   public void onCreate(NodeContext<Node> target, NodeContext<Node> parent, NodeContext<Node> previous, String name)
   {
      System.out.println("onCreate");
   }

   @Override
   public void onRemove(NodeContext<Node> target, NodeContext<Node> parent)
   {
      System.out.println("onRemove");
   }

   @Override
   public void onDestroy(NodeContext<Node> target, NodeContext<Node> parent)
   {
      System.out.println("onDestroy");
   }

   @Override
   public void onRename(NodeContext<Node> target, NodeContext<Node> parent, String name)
   {
      System.out.println("onRename");
   }

   @Override
   public void onUpdate(NodeContext<Node> target, NodeState state)
   {
      System.out.println("onUpdate");
   }

   @Override
   public void onMove(NodeContext<Node> target, NodeContext<Node> from, NodeContext<Node> to, NodeContext<Node> previous)
   {
      System.out.println("onMove");
   }

   static class NavigationNodeModel implements NodeModel<Node>
   {
      private final SiteImpl site;
      private final GateInImpl gateIn;

      NavigationNodeModel(SiteImpl site, GateInImpl gateIn)
      {
         this.site = site;
         this.gateIn = gateIn;
      }

      public NodeContext<Node> getContext(Node node)
      {
         return ((NodeImpl) node).context;
      }

      public Node create(NodeContext<Node> context)
      {
         return new NodeImpl(site, gateIn, context);
      }
   }
}
