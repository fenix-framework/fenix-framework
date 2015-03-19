package pt.ist.fenixframework.backend.jvstmojb.pstm;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Fenix Framework. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Paulo Abrantes (paulo.abrantes@qub-it.com)
 */
// TODO: where should this really be?
public class ValueTypeSerializationGenerator {

    // VT: UpdateTimestamp
    public static class Serialized$UpdateTimestamp implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private org.joda.time.DateTime externalize;

        private Serialized$UpdateTimestamp(UpdateTimestamp obj) {
            this.externalize = (org.joda.time.DateTime) obj.externalize();

        }
    }

    public static Serialized$UpdateTimestamp serialize$UpdateTimestamp(UpdateTimestamp obj) {
        return (obj == null) ? null : new Serialized$UpdateTimestamp(obj);
    }

    public static UpdateTimestamp deSerialize$UpdateTimestamp(Serialized$UpdateTimestamp obj) {
        return (obj == null) ? null : (UpdateTimestamp) new UpdateTimestamp(obj.externalize);
    }

    // VT: UpdateEntity
    public static class Serialized$UpdateEntity implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private java.lang.String externalize;

        private Serialized$UpdateEntity(UpdateEntity obj) {
            this.externalize = (java.lang.String) obj.externalize();

        }

    }

    public static Serialized$UpdateEntity serialize$UpdateEntity(UpdateEntity obj) {
        return (obj == null) ? null : new Serialized$UpdateEntity(obj);
    }

    public static UpdateEntity deSerialize$UpdateEntity(Serialized$UpdateEntity obj) {
        return (obj == null) ? null : (UpdateEntity) new UpdateEntity(obj.externalize);
    }

}
