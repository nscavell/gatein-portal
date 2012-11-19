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

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.page.PageKey;
import org.gatein.api.ApiException;
import org.gatein.api.impl.Util;
import org.gatein.api.portal.Label;
import org.gatein.api.portal.Localized.Value;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.PublicationDate;
import org.gatein.api.portal.navigation.Visibility;
import org.gatein.api.portal.navigation.Visibility.Flag;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ObjectFactory
{
   private ObjectFactory()
   {
   }

   public static Node createNode(String name, NodeState state)
   {
      Node node = new NodeImpl(name);
      node.setIconName(state.getIcon());
      if (state.getPageRef() != null)
      {
         node.setPageId(Util.from(state.getPageRef()));
      }
      node.setVisibility(createVisibility(state));
      return node;
   }

   public static NodeState createNodeState(Node node)
   {
      String label = node.getLabel() != null && !node.getLabel().isLocalized() ? node.getLabel().getValue() : null;
      String icon = node.getIconName();

      PublicationDate publicationDate = node.getVisibility().getPublicationDate();

      long startPublicationTime = -1;
      long endPublicationTime = -1;

      if (publicationDate != null)
      {
         if (publicationDate.getStart() != null)
         {
            startPublicationTime = publicationDate.getStart().getTime();
         }

         if (publicationDate.getEnd() != null)
         {
            endPublicationTime = publicationDate.getEnd().getTime();
         }
      }

      org.exoplatform.portal.mop.Visibility visibility = from(node.getVisibility().getFlag());
      
      PageKey pageKey = node.getPageId() != null ? Util.from(node.getPageId()) : null;

      return new NodeState(label, icon, startPublicationTime, endPublicationTime, visibility, pageKey);
   }

   private static org.exoplatform.portal.mop.Visibility from(Flag flag)
   {
      switch (flag)
      {
         case VISIBLE:
            return org.exoplatform.portal.mop.Visibility.DISPLAYED;
         case HIDDEN:
            return org.exoplatform.portal.mop.Visibility.HIDDEN;
         case SYSTEM:
            return org.exoplatform.portal.mop.Visibility.SYSTEM;
         case PUBLICATION:
            return org.exoplatform.portal.mop.Visibility.TEMPORAL;
         default:
            throw new ApiException("Unknown visibility flag '" + flag + "'");
      }
   }

   public static Label createLabel(Map<Locale, Described.State> descriptions)
   {
      if (descriptions != null && !descriptions.isEmpty())
      {
         Map<Locale, String> m = new HashMap<Locale, String>();
         for (Map.Entry<Locale, Described.State> entry : descriptions.entrySet())
         {
            m.put(entry.getKey(), entry.getValue().getName());
         }
         return new Label(m);
      }
      else
      {
         return null;
      }
   }

   public static Map<Locale, Described.State> createDescriptions(Label label)
   {
      Map<Locale, Described.State> descriptions = new HashMap<Locale, Described.State>();
      for (Value<String> v : label.getLocalizedValues())
      {
         descriptions.put(v.getLocale(), new Described.State(v.getValue(), null));
      }
      return descriptions;
   }

   public static Visibility createVisibility(NodeState nodeState)
   {
      Flag flag = from(nodeState.getVisibility());

      long start = nodeState.getStartPublicationTime();
      long end = nodeState.getEndPublicationTime();

      PublicationDate publicationDate = null;
      if (start != -1 && end != -1)
      {
         publicationDate = PublicationDate.between(new Date(start), new Date(end));
      }
      else if (start != -1)
      {
         publicationDate = PublicationDate.startingOn(new Date(start));
      }
      else if (end != -1)
      {
         publicationDate = PublicationDate.endingOn(new Date(end));
      }

      return new Visibility(flag, publicationDate);
   }

   private static Flag from(org.exoplatform.portal.mop.Visibility visibility)
   {
      switch (visibility)
      {
         case DISPLAYED:
            return Flag.VISIBLE;
         case HIDDEN:
            return Flag.HIDDEN;
         case SYSTEM:
            return Flag.SYSTEM;
         case TEMPORAL:
            return Flag.PUBLICATION;
         default:
            throw new ApiException("Unknown internal visibility '" + visibility + "'");
      }
   }
}
