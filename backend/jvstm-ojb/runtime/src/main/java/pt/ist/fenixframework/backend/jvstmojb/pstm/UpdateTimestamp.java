package pt.ist.fenixframework.backend.jvstmojb.pstm;

import java.io.Serializable;

import org.joda.time.DateTime;

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
//TODO: where should this really be?
@SuppressWarnings("serial")
public class UpdateTimestamp implements Serializable {
    private DateTime date;

    public UpdateTimestamp() {
        this.date = new DateTime();
    }

    public UpdateTimestamp(DateTime date) {
        this.date = date;
    }

    public DateTime getDate() {
        return date;
    }

    public DateTime externalize() {
        date = new DateTime();
        return date;
    }

    public int hashCode() {
        return date.hashCode();
    }

    public boolean equals(Object o) {
        if (o instanceof UpdateTimestamp) {
            UpdateTimestamp other = (UpdateTimestamp) o;
            return date.equals(other.getDate());
        }
        return false;
    }
}
