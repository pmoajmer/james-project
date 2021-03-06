/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.mailbox.quota;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.james.core.Domain;
import org.apache.james.core.quota.QuotaCountLimit;
import org.apache.james.core.quota.QuotaSizeLimit;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.Quota;
import org.apache.james.mailbox.model.Quota.Scope;
import org.apache.james.mailbox.model.QuotaRoot;

import com.github.fge.lambdas.Throwing;

/**
 * This interface describe how to set the max quotas for users
 * Part of RFC 2087 implementation
 */
public interface MaxQuotaManager {

    /**
     * Method allowing you to set the maximum storage quota for a given user
     *
     * @param quotaRoot Quota root argument from RFC 2087 ( correspond to the user owning this mailbox )
     * @param maxStorageQuota The new storage quota ( in bytes ) for this user
     */
    void setMaxStorage(QuotaRoot quotaRoot, QuotaSizeLimit maxStorageQuota) throws MailboxException;

    /**
     * Method allowing you to set the maximum message count allowed for this quotaroot
     *
     * @param quotaRoot Quota root argument from RFC 2087
     * @param maxMessageCount The new message count allowed.
     */
    void setMaxMessage(QuotaRoot quotaRoot, QuotaCountLimit maxMessageCount) throws MailboxException;

    /**
     * Method allowing you to remove the maximum messages count allowed for this quotaroot
     *
     * @param quotaRoot Quota root argument from RFC 2087
     */
    void removeMaxMessage(QuotaRoot quotaRoot) throws MailboxException;

    /**
     * Method allowing you to remove the maximum messages size allowed for this quotaroot
     *
     * @param quotaRoot Quota root argument from RFC 2087
     */
    void removeMaxStorage(QuotaRoot quotaRoot) throws MailboxException;

    /**
     * Method allowing you to set the global maximum storage in bytes.
     *
     * @param globalMaxStorage new global maximum storage
     */
    void setGlobalMaxStorage(QuotaSizeLimit globalMaxStorage) throws MailboxException;

    /**
     * Method allowing you to remove the global maximum messages size in bytes.
     */
    void removeGlobalMaxStorage() throws MailboxException;

    /**
     * Method allowing you to set the global maximum message count allowed
     *
     * @param globalMaxMessageCount new global message count
     */
    void setGlobalMaxMessage(QuotaCountLimit globalMaxMessageCount) throws MailboxException;

    /**
     * Method allowing you to remove the global maximum messages count.
     */
    void removeGlobalMaxMessage() throws MailboxException;

    /**
     * Method allowing you to get the global maximum storage in bytes.
     *
     * @return global maximum storage, if defined
     */
    Optional<QuotaSizeLimit> getGlobalMaxStorage() throws MailboxException;

    /**
     * Method allowing you to get the global maximum message count allowed
     *
     * @return global maximum message count, if defined
     */
    Optional<QuotaCountLimit> getGlobalMaxMessage() throws MailboxException;

    /**
     * Return the maximum storage which is allowed for the given {@link QuotaRoot} (in fact the user which the session is bound to)
     *
     * The returned valued must be in <strong>bytes</strong>
     *
     * @param quotaRoot Quota root argument from RFC 2087 ( correspond to the user owning this mailbox )
     * @return The maximum storage in bytes if any
     */
    default Optional<QuotaSizeLimit> getMaxStorage(QuotaRoot quotaRoot) throws MailboxException {
        Map<Scope, QuotaSizeLimit> maxStorageDetails = listMaxStorageDetails(quotaRoot);
        return getMaxStorage(maxStorageDetails);
    }

    default Optional<QuotaSizeLimit> getMaxStorage(Map<Quota.Scope, QuotaSizeLimit> maxStorageDetails) {
        return Quota.allScopes()
            .stream()
            .map(maxStorageDetails::get)
            .filter(Objects::nonNull)
            .findFirst();
    }

    /**
     * Return the maximum message count which is allowed for the given {@link QuotaRoot} (in fact the user which the session is bound to)
     *
     * @param quotaRoot Quota root argument from RFC 2087 ( correspond to the user owning this mailbox )
     * @return maximum of allowed message count
     */
    default Optional<QuotaCountLimit> getMaxMessage(QuotaRoot quotaRoot) throws MailboxException {
        Map<Scope, QuotaCountLimit> maxMessagesDetails = listMaxMessagesDetails(quotaRoot);
        return getMaxMessage(maxMessagesDetails);
    }

    default Optional<QuotaCountLimit> getMaxMessage(Map<Quota.Scope, QuotaCountLimit> maxMessagesDetails) {
        return Stream.of(Quota.Scope.User, Quota.Scope.Domain, Quota.Scope.Global)
            .map(maxMessagesDetails::get)
            .filter(Objects::nonNull)
            .findFirst();
    }

    Map<Quota.Scope, QuotaCountLimit> listMaxMessagesDetails(QuotaRoot quotaRoot);

    Map<Quota.Scope, QuotaSizeLimit> listMaxStorageDetails(QuotaRoot quotaRoot);

    Optional<QuotaCountLimit> getDomainMaxMessage(Domain domain);

    void setDomainMaxMessage(Domain domain, QuotaCountLimit count) throws MailboxException;

    void removeDomainMaxMessage(Domain domain) throws MailboxException;

    void setDomainMaxStorage(Domain domain, QuotaSizeLimit size) throws MailboxException;

    Optional<QuotaSizeLimit> getDomainMaxStorage(Domain domain);

    void removeDomainMaxStorage(Domain domain) throws MailboxException;

    default Optional<QuotaCountLimit> getComputedMaxMessage(Domain domain) throws MailboxException {
        return Stream.of(
                Throwing.supplier(() -> getDomainMaxMessage(domain)).sneakyThrow(),
                Throwing.supplier(this::getGlobalMaxMessage).sneakyThrow())
            .map(Supplier::get)
            .flatMap(Optional::stream)
            .findFirst();
    }

    default Optional<QuotaSizeLimit> getComputedMaxStorage(Domain domain) throws MailboxException {
        return Stream.of(
                Throwing.supplier(() -> getDomainMaxStorage(domain)).sneakyThrow(),
                Throwing.supplier(this::getGlobalMaxStorage).sneakyThrow())
            .map(Supplier::get)
            .flatMap(Optional::stream)
            .findFirst();
    }
}