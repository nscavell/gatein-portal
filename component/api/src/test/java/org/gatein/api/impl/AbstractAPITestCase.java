package org.gatein.api.impl;

import java.util.Arrays;
import java.util.Locale;

import junit.framework.AssertionFailedError;

import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.AbstractPortalTest;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.resources.ResourceBundleManager;
import org.gatein.api.Portal;
import org.gatein.api.portal.Permission;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @author <a href="mailto:boleslaw.dawidowicz@redhat.com">Boleslaw Dawidowicz</a>
 */
@ConfiguredBy({
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.resources-configuration.xml")
   //@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.application-registry-configuration.xml"),
   //@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "org/gatein/portal/api/impl/configuration.xml")
})
public abstract class AbstractAPITestCase extends AbstractPortalTest
{

   /**
    * .
    */
   protected POMSessionManager mgr;

   /**
    * .
    */
   protected NavigationService navService;

   /**
    * .
    */
   protected DataStorage storage;

//   /** . */
//   protected PortletRegistry invoker;

   /**
    * .
    */
   protected Portal portal;

   /**
    * The current user locale, may be changed for testing purpose.
    */
   protected Locale userLocale;

   private PageService pageService;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      //
      PortalContainer container = getContainer();
      POMSessionManager mgr = (POMSessionManager) container.getComponentInstanceOfType(POMSessionManager.class);
      NavigationService navService = (NavigationService) container.getComponentInstanceOfType(NavigationService.class);
      OrganizationService orgService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
      DataStorage dataStorage = (DataStorage) container.getComponentInstanceOfType(DataStorage.class);
      DescriptionService descriptionService = (DescriptionService) container.getComponentInstanceOfType(DescriptionService.class);
      ResourceBundleManager bundleManager = (ResourceBundleManager) container.getComponentInstanceOfType(ResourceBundleManager.class);
      pageService = (PageService) container.getComponentInstanceOfType(PageService.class);

      this.mgr = mgr;
      this.navService = navService;
      this.storage = dataStorage;
//      this.invoker = invoker;
      this.userLocale = Locale.ENGLISH;

      this.portal = new PortalImpl(dataStorage, pageService, navService, descriptionService);//, orgService, bundleManager);

      //
      begin();
   }

   @Override
   protected void tearDown() throws Exception
   {
      end(false);
   }

   protected NodeContext createSite(SiteType type, String name)
   {
      try
      {
         PortalConfig config = new PortalConfig(type.getName(), name);
         config.setAccessPermissions(Util.from(Permission.everyone()));

         storage.create(config);
         NavigationContext nav = new NavigationContext(new SiteKey(type, name), new NavigationState(0));
         navService.saveNavigation(nav);
         //
         // storage.create(new org.exoplatform.portal.config.model.Page(type.getName(), name, "homepage"));
         pageService.savePage(new PageContext(new PageKey(new SiteKey(type, name), "homepage"), new PageState("displayName",
               "description", false, null, null, null)));

         //
         return navService.loadNode(NodeModel.SELF_MODEL, nav, Scope.ALL, null);
      }
      catch (Exception e)
      {
         AssertionFailedError afe = new AssertionFailedError();
         afe.initCause(e);
         throw afe;
      }
   }

   protected NodeContext createSite(SiteType type, String name, String...pages)
   {
      try
      {
         PortalConfig config = new PortalConfig(type.getName(), name);
         config.setAccessPermissions(Util.from(Permission.everyone()));

         storage.create(config);
         NavigationContext nav = new NavigationContext(new SiteKey(type, name), new NavigationState(0));
         navService.saveNavigation(nav);
         //

         for (String page : pages)
         {
            pageService.savePage(new PageContext(new PageKey(new SiteKey(type, name), page), new PageState("displayName",
               "description", false, null, Arrays.asList("Everyone"), "Everyone")));
         }

         //
         return navService.loadNode(NodeModel.SELF_MODEL, nav, Scope.ALL, null);
      }
      catch (Exception e)
      {
         AssertionFailedError afe = new AssertionFailedError();
         afe.initCause(e);
         throw afe;
      }
   }
}
