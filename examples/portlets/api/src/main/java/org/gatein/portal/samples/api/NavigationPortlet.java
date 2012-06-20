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

package org.gatein.portal.samples.api;

import org.exoplatform.container.ExoContainerContext;
import org.gatein.api.GateIn;
import org.gatein.api.portal.Navigation;
import org.gatein.api.portal.Node;
import org.gatein.api.portal.Site;
import org.gatein.api.portal.SiteQuery;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class NavigationPortlet extends GenericPortlet
{
   //private GateIn gateIn;

   @Override
   public void init(PortletConfig config) throws PortletException
   {
      super.init(config);
   }

   @Override
   protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
   {
      PrintWriter writer = response.getWriter();

      writer.println("<h1>Sites</h1>");
      SiteQuery<Site> sq = getGateIn().createSiteQuery().setType(Site.Type.SITE).containNavigation(true);
      List<Site> sites = sq.execute();
      for (Site site : sites)
      {
         outputSite(site, writer);
      }

      writer.println("<h1>Spaces</h1>");
      List<Site> spaces = sq.setType(Site.Type.SPACE).execute();
      for (Site space : spaces)
      {
         outputSite(space, writer);
      }

      writer.println("<h1>Dashboard</h1>");
      outputSite(getGateIn().getSite(Site.Type.DASHBOARD, "root"), writer);
   }

   private void outputSite(Site site, PrintWriter writer) throws IOException
   {

      writer.println("<h2>" + site.getLabel().getValue() + "</h2>");
      writer.println("<ul>");

      Navigation navigation = site.getNavigation();

      if (navigation != null && navigation.iterator().hasNext())
      {
         for (Node node : navigation)
         {
            outputNode(node, writer);
         }
      }
      else
      {
         writer.println("<h3>NULL or EMPTY Navigation</h3>");
      }
      writer.println("</ul><br/>");
   }

   private void outputNode(Node node, PrintWriter writer)
   {
      //Collection<? extends Navigation> children = node.getChildren();
      int size = node.getCount();
      boolean isLeaf = size == 0;
      writer.println("<li>"
         + (isLeaf ? "<a style='font-weight: bold; text-decoration: underline; color: #336666;' href='" + node.getURI() + "'>" : "")
         + node.getLabel().getValue()
         + (isLeaf ? "</a>" : "")
         + "</li>");
      if (size != 0)
      {
         writer.println("<ul>");
         for (Node child : node)
         {
            outputNode(child, writer);
         }
         writer.println("</ul>");
      }
   }

   GateIn getGateIn()
   {
      return (GateIn)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(GateIn.class);
   }
}
