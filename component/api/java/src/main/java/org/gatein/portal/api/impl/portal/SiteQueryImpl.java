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
package org.gatein.portal.api.impl.portal;

import org.gatein.api.commons.Range;
import org.gatein.api.portal.Site;
import org.gatein.api.portal.SiteQuery;
import org.gatein.common.NotYetImplemented;
import org.gatein.portal.api.impl.GateInImpl;

import java.util.List;

/**
 *
 * @author <a href="mailto:bdawidow@redhat.com">Boleslaw Dawidowicz</a>
 */
public class SiteQueryImpl implements SiteQuery<Site>
{

   // General
   private boolean sort = false;
   private boolean ascending = true;
   private Range range = null;

   // Site
   private Site.Id siteId;
   private Site.Type siteType;
   private String userId;
   private String groupId;
   private boolean containsNavigation = false;

   private final GateInImpl gateinImpl;

   public SiteQueryImpl(GateInImpl gateinImpl)
   {
      this.gateinImpl = gateinImpl;
   }

   SiteQueryImpl(GateInImpl gateinImpl,
                 Site.Id siteId,
                 Site.Type siteType,
                 String userId,
                 String groupId,
                 boolean nav,
                 boolean sort,
                 boolean ascending,
                 Range range)
   {
      this.gateinImpl = gateinImpl;

      this.siteId = siteId;
      this.siteType = siteType;
      this.userId = userId;
      this.groupId = groupId;
      this.containsNavigation = nav;
      this.sort = sort;
      this.ascending = ascending;
      this.range = range;
   }

   SiteQueryImpl(SiteQueryImpl queryImpl)
   {
      this.gateinImpl = queryImpl.getGateInImpl();

      // Making sure it doesn't pass same objects (Range, Site.Id) so it performs true clone
      this.siteId = Site.Id.create(queryImpl.getId().getType(), queryImpl.getId().getName());
      this.siteType = queryImpl.getType();
      this.userId = queryImpl.getUserId();
      this.groupId = queryImpl.getGroupId();
      this.containsNavigation = queryImpl.isContainNavigation();
      this.sort = queryImpl.isSorted();
      this.ascending = queryImpl.isAscending();
      this.range = Range.of(queryImpl.getRange().getOffset(), queryImpl.getRange().getLimit());
   }

   @Override
   public SiteQuery<Site> setId(Site.Id id)
   {
      this.siteId = id;
      this.siteType = id.getType();
      return this;
   }

   @Override
   public Site.Id getId()
   {
      return siteId;
   }

   @Override
   public SiteQuery<Site> setType(Site.Type siteType)
   {
      this.siteType = siteType;
      return this;
   }

   @Override
   public Site.Type getType()
   {
      return siteType;
   }

   @Override
   public SiteQuery<Site> setUserId(String userId)
   {
      this.userId = userId;
      return this;
   }

   @Override
   public String getUserId()
   {
      return userId;
   }

   @Override
   public SiteQuery<Site> setGroupId(String groupId)
   {
      this.groupId = groupId;
      return this;
   }

   @Override
   public SiteQuery<Site> setGroupId(String... groupId)
   {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < groupId.length; i++)
      {
         String gid = groupId[i];
         sb.append("/").append(gid);
      }
      this.groupId = sb.toString();
      return this;
   }

   @Override
   public String getGroupId()
   {
      return groupId;
   }

   @Override
   public SiteQuery<Site> containNavigation(boolean option)
   {
      this.containsNavigation = option;
      return this;
   }

   @Override
   public boolean isContainNavigation()
   {
      return containsNavigation;
   }

   @Override
   public SiteQuery<Site> reset()
   {
      siteId = null;
      siteType = null;
      userId = null;
      groupId = null;
      containsNavigation = false;
      range = null;
      sort = false;
      ascending = true;

      return this;
   }

   @Override
   public SiteQuery<Site> immutable()
   {
      //TODO:
      throw new NotYetImplemented();
   }

   @Override
   public SiteQuery<Site> clone()
   {
      return new SiteQueryImpl(this);
   }

   @Override
   public int getResultsCount()
   {
      return getGateInImpl().getQueryResultCount(this);
   }

   @Override
   public int getPageCount()
   {
      //TODO:
      throw new NotYetImplemented();
   }

   @Override
   public SiteQuery<Site> sort(boolean ascending)
   {
      this.sort = true;
      this.ascending = ascending;
      return this;
   }

   boolean isSorted()
   {
      return sort;
   }

   boolean isAscending()
   {
      return ascending;
   }

   @Override
   public SiteQuery<Site> setRange(Range range)
   {
      this.range = range;
      return this;
   }

   @Override
   public Range getRange()
   {
      return range;
   }

   @Override
   public int getCurrentPage()
   {
      if (range == null)
      {
         return 1;
      }
      return range.getPage();
   }

   public SiteQuery<Site> setPage(int page)
   {
      //TODO:
      return this;
   }

   @Override
   public SiteQuery<Site> nextPage()
   {
      range.next();
      return this;
   }

   @Override
   public SiteQuery<Site> previousPage()
   {
      if (range != null)
      {
         range.previous();
      }
      return this;  //To change body of implemented methods use File | Settings | File Templates.
   }

   @Override
   public SiteQuery<Site> firstPage()
   {
      if (siteId != null)
      {
         range = range.of(0, range.getLimit());
      }
      return this;
   }

   @Override
   public SiteQuery<Site> lastPage()
   {
      if (range == null)
      {
         return this;
      }

      //TODO:
      throw new NotYetImplemented();

      //return this;
   }

   @Override
   public List<Site> execute()
   {
      return getGateInImpl().executeSiteQuery(this);
   }

   public GateInImpl getGateInImpl()
   {
      return gateinImpl;
   }

}
