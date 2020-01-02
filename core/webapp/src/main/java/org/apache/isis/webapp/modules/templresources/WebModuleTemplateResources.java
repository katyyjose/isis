/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.webapp.modules.templresources;

import lombok.Getter;
import lombok.var;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.webapp.modules.WebModule;
import org.apache.isis.webapp.modules.WebModuleContext;

/**
 * WebModule to provide static resources utilizing an in-memory cache.
 * 
 * @since 2.0
 */
@Service
@Named("isisWebapp.WebModuleTemplateResources")
@Order(OrderPrecedence.MIDPOINT - 100)
@Qualifier("TemplateResources")
public final class WebModuleTemplateResources implements WebModule  {

    private final static String[] urlPatterns = { "*.thtml" };

    private final static int cacheTimeSeconds = 86400;

    public static final String FILTER_NAME = "TemplateResourceCachingFilter";
    private final static String SERVLET_NAME = "TemplateResourceServlet";

    @Getter
    private final String name = "TemplateResources";

    private final ServiceInjector serviceInjector;

    @Inject
    public WebModuleTemplateResources(final ServiceInjector serviceInjector) {
        this.serviceInjector = serviceInjector;
    }

    @Override
    public ServletContextListener init(ServletContext ctx) throws ServletException {

        var filter = ctx.addFilter(FILTER_NAME, TemplateResourceCachingFilter.class);
        if (filter != null) {
            serviceInjector.injectServicesInto(filter);
            filter.setInitParameter(
                    "CacheTime",
                    ""+cacheTimeSeconds);
            filter.addMappingForUrlPatterns(
                    null,
                    true,
                    urlPatterns);

        } else {
            // was already registered, eg in web.xml.
        }

        var servlet = ctx.addServlet(SERVLET_NAME, TemplateResourceServlet.class);
        if (servlet != null) {
            serviceInjector.injectServicesInto(servlet);
            servlet.addMapping(urlPatterns);
        } else {
            // was already registered, eg in web.xml.
        }

        return null; // does not provide a listener
    }


}
