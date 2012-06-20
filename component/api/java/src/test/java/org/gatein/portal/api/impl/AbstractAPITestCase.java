package org.gatein.portal.api.impl;

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
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.RequestContext;
import org.gatein.api.GateIn;

import java.util.Locale;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @author <a href="mailto:boleslaw.dawidowicz@redhat.com">Boleslaw Dawidowicz</a>
 */
@ConfiguredBy({
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml")
   //@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.application-registry-configuration.xml"),
   //@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "org/gatein/portal/api/impl/configuration.xml")
})
public abstract class AbstractAPITestCase extends AbstractPortalTest
{

   /** . */
   protected POMSessionManager mgr;

   /** . */
   protected NavigationService navService;

   /** . */
   protected DataStorage storage;

//   /** . */
//   protected PortletRegistry invoker;

   /** . */
   protected GateIn gatein;

   /** The current user locale, may be changed for testing purpose. */
   protected Locale userLocale;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      //
      PortalContainer container = getContainer();
      POMSessionManager mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      NavigationService navService = (NavigationService)container.getComponentInstanceOfType(NavigationService.class);
      OrganizationService orgService = (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class);
      DataStorage dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);

      GateInImpl gatein = new GateInImpl(dataStorage, navService, orgService);
//      PortletRegistry invoker = (PortletRegistry)container.getComponentInstanceOfType(PortletInvoker.class);

      //
      gatein.start();

      // Clear the cache for each test
      //navService.clearCache();

      //
      this.gatein = gatein;
      this.mgr = mgr;
      this.navService = navService;
      this.storage = dataStorage;
//      this.invoker = invoker;
      this.userLocale = Locale.ENGLISH;

      //
      begin();

      //
      RequestContext.setCurrentInstance(new SimpleRequestContext(null)
      {
         @Override
         public Locale getLocale()
         {
            return userLocale;
         }
      });
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
         storage.create(new PortalConfig(type.getName(), name));
         NavigationContext nav = new NavigationContext(new SiteKey(type, name), new NavigationState(0));
         navService.saveNavigation(nav);
         //
         storage.create(new org.exoplatform.portal.config.model.Page(type.getName(), name, "homepage"));

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
