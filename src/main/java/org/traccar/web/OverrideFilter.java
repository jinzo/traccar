/*
 * Copyright 2023 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.web;

import com.google.inject.Provider;
import org.traccar.api.security.PermissionsService;
import org.traccar.model.Server;
import org.traccar.storage.StorageException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class OverrideFilter implements Filter {

    private final Provider<PermissionsService> permissionsServiceProvider;

    @Inject
    public OverrideFilter(Provider<PermissionsService> permissionsServiceProvider) {
        this.permissionsServiceProvider = permissionsServiceProvider;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        CharResponseWrapper wrapper = new CharResponseWrapper((HttpServletResponse) response);

        chain.doFilter(request, wrapper);

        Server server;
        try {
            server = permissionsServiceProvider.get().getServer();
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }

        String title = server.getString("serverTitle", "Traccar");
        String description = server.getString("serverDescription", "Traccar GPS Tracking System");
        String colorPrimary = server.getString("serverColorPrimary", "#1a237e");

        String alteredContent = wrapper.getCaptureAsString()
                .replace("${title}", title)
                .replace("${description}", description)
                .replace("${colorPrimary}", colorPrimary);

        response.setContentLength(alteredContent.length());
        response.getWriter().write(alteredContent);
    }

}