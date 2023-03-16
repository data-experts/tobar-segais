/*
 * Copyright 2011 Stephen Connolly
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

package org.tobarsegais.webapp;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RedirectFilter implements Filter {

    private String domain = null;
    private int status = HttpServletResponse.SC_MOVED_TEMPORARILY;

    public void init(FilterConfig filterConfig) throws ServletException {
        final ServletContext ctx = filterConfig.getServletContext();
        domain = ServletContextListenerImpl.getInitParameter(ctx, RedirectFilter.class.getName() + ".domain");
        String statusStr = ServletContextListenerImpl.getInitParameter(ctx, RedirectFilter.class.getName() + ".status");
        if (StringUtils.isNotBlank(statusStr)) {
            try {
                switch (Integer.parseInt(statusStr)) {
                    case 301:
                        status = 301;
                        break;
                    case 307:
                        status = 307;
                        break;
                    case 308:
                        status = 308;
                        break;
                    default:
                        status = 302;
                        break;
                }
            } catch (NumberFormatException e) {
                // ignore
            }
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (StringUtils.isEmpty(domain) || !(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
        } else {
            final HttpServletRequest req = (HttpServletRequest) request;
            final HttpServletResponse resp = (HttpServletResponse) response;
            final String serverName = req.getServerName();
            if (domain.equalsIgnoreCase(serverName)) {
                chain.doFilter(request, response);
            } else {
                StringBuffer requestURL = req.getRequestURL();
                int index = requestURL.indexOf(serverName);
                requestURL.replace(index, index + serverName.length(), domain);
                final String queryString = req.getQueryString();
                if (queryString != null) {
                    requestURL.append('?').append(queryString);
                }
                resp.setStatus(status);
                resp.setHeader("Location", requestURL.toString());
            }
        }
    }

    public void destroy() {
    }
}
