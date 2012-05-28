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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.gatein.api.common.Pagination;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class DataStorageContext {
    // TODO: Do we want a better name for loggeer ? Probably need to standardize our logging for api
    static final Logger log = LoggerFactory.getLogger("org.gatein.api.datastorage");

    protected final DataStorage dataStorage;

    protected DataStorageContext(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    protected <T> T execute(Read<T> callback) throws ApiException {
        try {
            return callback.read(dataStorage);
        } catch (Exception e) {
            log.error(e);
            throw new ApiException("Exception reading internal data storage. See logs for details.", e);
        }
    }

    protected <T> void execute(T data, Modify<T> callback) throws ApiException {
        try {
            callback.modify(data, dataStorage);
            dataStorage.save();
        } catch (Exception e) {
            log.error(e);
            throw new ApiException("Exception saving to internal data storage. See logs for details.", e);
        }
    }

    protected <T> List<T> query(Query<T> query) {
        return query(query, null);
    }

    protected <T> List<T> query(Query<T> query, Comparator<T> comparator) {
        try {
            return dataStorage.find(query, comparator).getAll();
        } catch (Exception e) {
            log.error(e);
            throw new ApiException("Exception querying internal data storage. See logs for details.", e);
        }
    }

    protected <T> List<T> find(Pagination pagination, Query<T> query, Comparator<T> comparator) {
        try {
            if (pagination != null) {
                ListAccess<T> access = dataStorage.find2(query, comparator);
                int size = access.getSize();
                int offset = pagination.getOffset();
                int limit = pagination.getLimit();
                if (offset >= size) {
                    return Collections.emptyList();
                } else if (offset + limit > size) {
                    return Arrays.asList(access.load(offset, size - offset));
                } else {
                    return Arrays.asList(access.load(offset, limit));
                }
            } else {
                return dataStorage.find(query, comparator).getAll();
            }
        } catch (Exception e) {
            log.error(e);
            throw new ApiException("Exception querying internal data storage. See logs for details.", e);
        }
    }

    protected interface Read<T> {
        T read(DataStorage dataStorage) throws Exception;
    }

    protected interface Modify<T> {
        void modify(T data, DataStorage dataStorage) throws Exception;
    }
}
