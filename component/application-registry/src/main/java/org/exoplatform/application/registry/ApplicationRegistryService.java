/**
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.application.registry;

import org.exoplatform.application.management.ApplicationCategoryMapper;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.portal.config.model.ApplicationType;
import org.gatein.management.api.annotations.Managed;
import org.gatein.management.api.annotations.ManagedOperation;
import org.gatein.management.api.annotations.ManagedPath;
import org.gatein.management.api.annotations.Mapped;
import org.gatein.management.api.annotations.MappedPath;
import org.gatein.management.api.operation.OperationNames;

import java.util.Comparator;
import java.util.List;

/** Created y the eXo platform team User: Tuan Nguyen Date: 20 april 2007 */

@Managed(description = "Application Registry Managed Resource")
@ManagedPath("/app-registry")
public interface ApplicationRegistryService
{
   String REMOTE_DISPLAY_NAME_SUFFIX = " (remote)";

   /**
    * Return list of ApplicationCatgory (and applications in each category) 
    * @param accessUser
    * @param appTypes - array of ApplicationType, used to filter applications in each application category
   */
   public List<ApplicationCategory> getApplicationCategories(String accessUser, ApplicationType<?>... appTypes) throws Exception;

   public void initListener(ComponentPlugin com) throws Exception;

   /**
    * Return list of all current application categories (unsorted, all Application in all ApplicationType)
    */
   @Managed
   @ManagedPath("categories")
   public List<ApplicationCategory> getApplicationCategories() throws Exception;

   /**
    * Return list of all current application categories (sorted, all applications in all types)
    * @param sortComparator - Comparator used to sort the returned list
    */
   public List<ApplicationCategory> getApplicationCategories(Comparator<ApplicationCategory> sortComparator)
      throws Exception;

   /**
    * Return ApplicationCategory with name provided <br/>
    * if not found, return null
    * @param name - ApplicationCategory's name
    */
   @Managed
   @ManagedPath("categories/{category}")
   public ApplicationCategory getApplicationCategory(@MappedPath("category") String name) throws Exception;

   /**
    * Save an ApplicationCategory to database <br/>
    * If it doesn't exist, a new one will be created, if not, it will be updated
    * @param category - ApplicationCategory object that will be saved
    */
   @Managed
   @ManagedPath("categories/{category}")
   @ManagedOperation(name = OperationNames.UPDATE_RESOURCE, description = "Update an application category")
   public void save(ApplicationCategory category) throws Exception;

   /**
    * Remove application category (and all application in it) from database <br/>
    * If it doesn't exist, it will be ignored
    * @param category - ApplicationCategory object that will be removed
    */
   @Managed
   @ManagedPath("categories/{category}")
   @ManagedOperation(name = OperationNames.REMOVE_RESOURCE, description = "Remove an application category")
   //TODO: For now testing custom mapper, but this method should just accept a string as category name and not object.
   public void remove(@Mapped(value = ApplicationCategoryMapper.class) ApplicationCategory category) throws Exception;

   /**
    * Return list of applications (unsorted) in specific category and have specific type
    * @param category - ApplicationCategory that you want to list applications
    * @param appTypes - array of application type
    */
   public List<Application> getApplications(ApplicationCategory category, ApplicationType<?>... appTypes) throws Exception;

   /**
    * Return list of applications (sorted) in specific category and have specific type
    * @param category - ApplicationCategory that you want to list applications
    * @param sortComparator - comparator used to sort application list
    * @param appTypes - array of application type
    */
   public List<Application> getApplications(ApplicationCategory category, Comparator<Application> sortComparator,
                                            ApplicationType<?>... appTypes) throws Exception;
   /**
    * Return list of all Application in database (unsorted) <br/>
    * If there are not any Application in database, return an empty list
    */
   @Managed
   public List<Application> getAllApplications() throws Exception;

   /**
    * Return Application with id provided
    * @param id - must be valid applicationId (catgoryname/applicationName), if not, this will throw exception
    */
   @Managed
   @ManagedPath("{app-id: .*}")
   public Application getApplication(@MappedPath("app-id") String id) throws Exception;

   /**
    * Return Application in specific category and have name provided in param <br/>
    * If it can't be found, return null
    * @param category - name of application category
    * @param name - name of application   
    */
   @Managed
   @ManagedPath("categories/{category}/{app-name}")
   public Application getApplication(@MappedPath("category") String category, @MappedPath("app-name") String name) throws Exception;

   /**
    * Save Application in an ApplicationCategory <br/>
    * If ApplicationCategory or Application don't exist, they'll be created <br/>
    * If Application has been already existed, it will be updated <br/>
    * @param category - ApplicationCategory that your application'll be saved to
    * @param application - Application that will be saved 
    */
   public void save(ApplicationCategory category, Application application) throws Exception;

   /**
    * Update an Application <br/>
    * It must be existed in database, if not, this will throw an IllegalStateException
    * @param application - Application that you want to update
    */
   public void update(Application application) throws Exception;

   /**
    * Remove an Application from database <br/>
    * If it can't be found, it will be ignored (no exception)
    * @param app - Application that you want to remove, must not be null
    */
   public void remove(Application app) throws Exception;

   /**
    * Get all deployed portlet, add to portlet's ApplicationCategory <br/>
    * If ApplicationCategory currently doesn't exist, it'll be created  <br/>
    * If Application've already existed, it'll be ignored 
    */
   public void importAllPortlets() throws Exception;

   //TODO: dang.tung
   /**
    * Get all Gadget, add to eXoGadgets application category <br/>
    * When first added, it's access permission will be Everyone <br/>
    * If ApplicationCategory currently doesn't exist, it'll be created <br/>
    * Gadget that has been imported will be ignored  
    */
   public void importExoGadgets() throws Exception;
}