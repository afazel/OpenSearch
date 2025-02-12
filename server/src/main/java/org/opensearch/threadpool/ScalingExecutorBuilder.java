/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.threadpool;

import org.opensearch.common.settings.Setting;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.common.util.concurrent.OpenSearchExecutors;
import org.opensearch.common.util.concurrent.ThreadContext;
import org.opensearch.node.Node;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * A builder for scaling executors.
 */
public final class ScalingExecutorBuilder extends ExecutorBuilder<ScalingExecutorBuilder.ScalingExecutorSettings> {

    private final Setting<Integer> coreSetting;
    private final Setting<Integer> maxSetting;
    private final Setting<TimeValue> keepAliveSetting;

    /**
     * Construct a scaling executor builder; the settings will have the
     * key prefix "thread_pool." followed by the executor name.
     *
     * @param name      the name of the executor
     * @param core      the minimum number of threads in the pool
     * @param max       the maximum number of threads in the pool
     * @param keepAlive the time that spare threads above {@code core}
     *                  threads will be kept alive
     */
    public ScalingExecutorBuilder(final String name, final int core, final int max, final TimeValue keepAlive) {
        this(name, core, max, keepAlive, "thread_pool." + name);
    }

    /**
     * Construct a scaling executor builder; the settings will have the
     * specified key prefix.
     *
     * @param name      the name of the executor
     * @param core      the minimum number of threads in the pool
     * @param max       the maximum number of threads in the pool
     * @param keepAlive the time that spare threads above {@code core}
     *                  threads will be kept alive
     * @param prefix    the prefix for the settings keys
     */
    public ScalingExecutorBuilder(final String name, final int core, final int max, final TimeValue keepAlive, final String prefix) {
        super(name);
        this.coreSetting =
            Setting.intSetting(settingsKey(prefix, "core"), core, Setting.Property.NodeScope);
        this.maxSetting = Setting.intSetting(settingsKey(prefix, "max"), max, Setting.Property.NodeScope);
        this.keepAliveSetting =
            Setting.timeSetting(settingsKey(prefix, "keep_alive"), keepAlive, Setting.Property.NodeScope);
    }

    @Override
    public List<Setting<?>> getRegisteredSettings() {
        return Arrays.asList(coreSetting, maxSetting, keepAliveSetting);
    }

    @Override
    ScalingExecutorSettings getSettings(Settings settings) {
        final String nodeName = Node.NODE_NAME_SETTING.get(settings);
        final int coreThreads = coreSetting.get(settings);
        final int maxThreads = maxSetting.get(settings);
        final TimeValue keepAlive = keepAliveSetting.get(settings);
        return new ScalingExecutorSettings(nodeName, coreThreads, maxThreads, keepAlive);
    }

    ThreadPool.ExecutorHolder build(final ScalingExecutorSettings settings, final ThreadContext threadContext) {
        TimeValue keepAlive = settings.keepAlive;
        int core = settings.core;
        int max = settings.max;
        final ThreadPool.Info info = new ThreadPool.Info(name(), ThreadPool.ThreadPoolType.SCALING, core, max, keepAlive, null);
        final ThreadFactory threadFactory =
            OpenSearchExecutors.daemonThreadFactory(OpenSearchExecutors.threadName(settings.nodeName, name()));
        final ExecutorService executor =
            OpenSearchExecutors.newScaling(
                    settings.nodeName + "/" + name(),
                    core,
                    max,
                    keepAlive.millis(),
                    TimeUnit.MILLISECONDS,
                    threadFactory,
                    threadContext);
        return new ThreadPool.ExecutorHolder(executor, info);
    }

    @Override
    String formatInfo(ThreadPool.Info info) {
        return String.format(
            Locale.ROOT,
            "name [%s], core [%d], max [%d], keep alive [%s]",
            info.getName(),
            info.getMin(),
            info.getMax(),
            info.getKeepAlive());
    }

    static class ScalingExecutorSettings extends ExecutorBuilder.ExecutorSettings {

        private final int core;
        private final int max;
        private final TimeValue keepAlive;

        ScalingExecutorSettings(final String nodeName, final int core, final int max, final TimeValue keepAlive) {
            super(nodeName);
            this.core = core;
            this.max = max;
            this.keepAlive = keepAlive;
        }
    }

}
