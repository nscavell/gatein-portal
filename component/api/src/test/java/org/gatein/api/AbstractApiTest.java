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

package org.gatein.api;

import static org.junit.Assert.assertNotNull;

import java.util.Locale;

import junit.framework.AssertionFailedError;

import org.chromattic.common.collection.Collections;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.component.test.KernelLifeCycle;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.page.PageState;
import org.gatein.api.navigation.NodePath;
import org.gatein.api.security.Permission;
import org.gatein.api.security.User;
import org.gatein.api.site.SiteId;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.resources-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.api-configuration.xml") })
public class AbstractApiTest {
    @ClassRule
    public static KernelLifeCycle kernelLifeCycle = new KernelLifeCycle();

    private PortalContainer container;

    protected Portal portal;

    protected SiteId siteId;

    @After
    public void after() throws Exception {
        deleteSite(siteId);
        BasicPortalRequest.setInstance(null);
    }

    @Before
    public void before() throws Exception {
        siteId = new SiteId("classic");
        container = kernelLifeCycle.getContainer();
        portal = (Portal) container.getComponentInstanceOfType(Portal.class);
        assertNotNull("Portal component not found in container", portal);

        RequestLifeCycle.begin(container);
        createSite(siteId);
        RequestLifeCycle.end();

        RequestLifeCycle.begin(container);

        BasicPortalRequest
                .setInstance(new BasicPortalRequest(new User("john"), siteId, NodePath.root(), Locale.ENGLISH, portal));
    }

    protected void createSite(SiteId siteId) {
        try {
            DataStorage dataStorage = (DataStorage) container.getComponentInstanceOfType(DataStorage.class);
            NavigationService navService = (NavigationService) container.getComponentInstanceOfType(NavigationService.class);
            PageService pageService = (PageService) container.getComponentInstanceOfType(PageService.class);

            SiteKey siteKey = Util.from(siteId);

            PortalConfig config = new PortalConfig(siteKey.getTypeName(), siteKey.getName());
            config.setAccessPermissions(Util.from(Permission.everyone()));

            dataStorage.create(config);

            NavigationContext nav = new NavigationContext(new SiteKey(siteKey.getTypeName(), siteKey.getName()),
                    new NavigationState(null));
            navService.saveNavigation(nav);

            pageService.savePage(new PageContext(
                    new PageKey(new SiteKey(siteKey.getTypeName(), siteKey.getName()), "homepage"), new PageState(
                            "displayName", "description", false, null, Collections.list("Everyone"),
                            "*:/platform/administrators")));
        } catch (Exception e) {
            AssertionFailedError afe = new AssertionFailedError();
            afe.initCause(e);
            throw afe;
        }
    }

    protected void deleteSite(SiteId siteId) {
        try {
            SiteKey siteKey = Util.from(siteId);
            DataStorage dataStorage = (DataStorage) container.getComponentInstanceOfType(DataStorage.class);
            dataStorage.remove(new PortalConfig(siteKey.getTypeName(), siteKey.getName()));
        } catch (Exception e) {
            AssertionFailedError afe = new AssertionFailedError();
            afe.initCause(e);
            throw afe;
        }
    }
}
