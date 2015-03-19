package pt.ist.fenixframework;

/**
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Fenix Framework. If not, see <http://www.gnu.org/licenses/>.
 *
 * 
 * This is actually a file that was present in FenixFramework 1. Reviving it
 * due to auditing scope 
 * 
 * @author Paulo Abrantes (paulo.abrantes@qub-it.com)
 */
public class ServiceInfo {

    private static final ThreadLocal<ServiceInfo> CURRENT_SERVICE = new ThreadLocal<ServiceInfo>();

    final String username;
    final String serviceName;
    final Object[] arguments;

    ServiceInfo(String username, String serviceName, Object[] arguments) {
        this.username = username;
        this.serviceName = serviceName;
        this.arguments = arguments;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getArgumentsAsString() {
        StringBuilder argumentsInString = new StringBuilder();

        for (Object argument : arguments) {
            if (argument != null) {
                try {
                    argumentsInString.append(argument.toString());
                } catch (NullPointerException e) {
                    argumentsInString.append(argument.getClass().getName());
                }
                argumentsInString.append("; ");
            }
        }
        return argumentsInString.toString();
    }

    public static void setCurrentServiceInfo(String username, String serviceName, Object[] args) {
        CURRENT_SERVICE.set(new ServiceInfo(username, serviceName, args));
    }

    public static void clearCurrentServiceInfo() {
        CURRENT_SERVICE.remove();
    }

    public static ServiceInfo getCurrentServiceInfo() {
        return CURRENT_SERVICE.get();
    }

}
