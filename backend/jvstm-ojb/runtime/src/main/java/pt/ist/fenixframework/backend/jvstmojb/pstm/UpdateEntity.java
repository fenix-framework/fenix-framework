package pt.ist.fenixframework.backend.jvstmojb.pstm;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import pt.ist.fenixframework.ServiceInfo;

/**
 * Copyright © 2013 Quorum Born IT | www.qub-it.com
 *
 * This file is part of Fenix Framework.
 *
 * Fenix Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Fenix Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Fenix Framework.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Paulo Abrantes (paulo.abrantes@qub-it.com)
 */
//TODO: which is the best package to put this?
@SuppressWarnings("serial")
public class UpdateEntity implements Serializable {
    private String username;
    private String serviceName;

    private static String UNKNOWN_STRING = "unknown";

    public UpdateEntity() {
        // TODO: define a way to access the user since ApplicationContext is not available in FF
        username = "unknown";
    }

    public UpdateEntity(String externalizedString) {
        String[] split = externalizedString.split(":");
        if (split.length == 2) {
            this.serviceName = split[0];
            this.username = split[1];
        } else {
            this.username = externalizedString;
        }
    }

    public String getUsername() {
        return username;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String externalize() {
        ServiceInfo currentServiceInfo = ServiceInfo.getCurrentServiceInfo();
        String serviceNameResult = null;
        if (currentServiceInfo != null) {
            serviceNameResult = currentServiceInfo.getServiceName();
        }
        String currentUsername = "unknown"; // same comment has the constructor
        username = !StringUtils.isEmpty(currentUsername) ? currentUsername : UNKNOWN_STRING;
        serviceName = serviceNameResult != null ? serviceNameResult : UNKNOWN_STRING;

        return serviceName + ":" + username;
    }

    @Override
    public int hashCode() {
        return username.hashCode() + (serviceName != null ? serviceName.hashCode() : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UpdateEntity) {
            UpdateEntity other = (UpdateEntity) o;
            return username.equals(other.getUsername());
        }
        return false;
    }
}
