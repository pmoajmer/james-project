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

package org.apache.james.backends.cassandra.init;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.WriteType;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.RetryPolicy;

public class NotConsistencyAllRetryPolicy implements RetryPolicy {
    @Override
    public RetryDecision onReadTimeout(Statement statement, ConsistencyLevel cl, int requiredResponses, int receivedResponses, boolean dataRetrieved, int nbRetry) {
        if (cl == ConsistencyLevel.ALL) {
            return RetryDecision.retry(ConsistencyLevel.QUORUM);
        }
        return DefaultRetryPolicy.INSTANCE.onReadTimeout(statement, cl, requiredResponses, receivedResponses, dataRetrieved, nbRetry);
    }

    @Override
    public RetryDecision onWriteTimeout(Statement statement, ConsistencyLevel cl, WriteType writeType, int requiredAcks, int receivedAcks, int nbRetry) {
        if (cl == ConsistencyLevel.ALL) {
            return RetryDecision.retry(ConsistencyLevel.QUORUM);
        }
        return DefaultRetryPolicy.INSTANCE.onWriteTimeout(statement, cl, writeType, requiredAcks, receivedAcks, nbRetry);
    }

    @Override
    public RetryDecision onUnavailable(Statement statement, ConsistencyLevel cl, int requiredReplica, int aliveReplica, int nbRetry) {
        if (cl == ConsistencyLevel.ALL) {
            return RetryDecision.retry(ConsistencyLevel.QUORUM);
        }
        return DefaultRetryPolicy.INSTANCE.onUnavailable(statement, cl, requiredReplica, aliveReplica, nbRetry);
    }

    @Override
    public RetryDecision onRequestError(Statement statement, ConsistencyLevel cl, DriverException e, int nbRetry) {
        return DefaultRetryPolicy.INSTANCE.onRequestError(statement, cl, e, nbRetry);
    }

    @Override
    public void init(Cluster cluster) {

    }

    @Override
    public void close() {

    }
}