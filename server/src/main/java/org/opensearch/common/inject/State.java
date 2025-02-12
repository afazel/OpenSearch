/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.common.inject;

import org.opensearch.common.inject.internal.BindingImpl;
import org.opensearch.common.inject.internal.Errors;
import org.opensearch.common.inject.internal.MatcherAndConverter;
import org.opensearch.common.inject.spi.TypeListenerBinding;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptySet;

/**
 * The inheritable data within an injector. This class is intended to allow parent and local
 * injector data to be accessed as a unit.
 *
 * @author jessewilson@google.com (Jesse Wilson)
 */
interface State {

    State NONE = new State() {
        @Override
        public State parent() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> BindingImpl<T> getExplicitBinding(Key<T> key) {
            return null;
        }

        @Override
        public Map<Key<?>, Binding<?>> getExplicitBindingsThisLevel() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putBinding(Key<?> key, BindingImpl<?> binding) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Scope getScope(Class<? extends Annotation> scopingAnnotation) {
            return null;
        }

        @Override
        public void putAnnotation(Class<? extends Annotation> annotationType, Scope scope) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addConverter(MatcherAndConverter matcherAndConverter) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MatcherAndConverter getConverter(String stringValue, TypeLiteral<?> type, Errors errors,
                                                Object source) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterable<MatcherAndConverter> getConvertersThisLevel() {
            return emptySet();
        }

        @Override
        public void addTypeListener(TypeListenerBinding typeListenerBinding) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<TypeListenerBinding> getTypeListenerBindings() {
            return Collections.emptyList();
        }

        @Override
        public void blacklist(Key<?> key) {
        }

        @Override
        public boolean isBlacklisted(Key<?> key) {
            return true;
        }

        @Override
        public void clearBlacklisted() {
        }

        @Override
        public void makeAllBindingsToEagerSingletons(Injector injector) {
        }

        @Override
        public Object lock() {
            throw new UnsupportedOperationException();
        }
    };

    State parent();

    /**
     * Gets a binding which was specified explicitly in a module, or null.
     */
    <T> BindingImpl<T> getExplicitBinding(Key<T> key);

    /**
     * Returns the explicit bindings at this level only.
     */
    Map<Key<?>, Binding<?>> getExplicitBindingsThisLevel();

    void putBinding(Key<?> key, BindingImpl<?> binding);

    /**
     * Returns the matching scope, or null.
     */
    Scope getScope(Class<? extends Annotation> scopingAnnotation);

    void putAnnotation(Class<? extends Annotation> annotationType, Scope scope);

    void addConverter(MatcherAndConverter matcherAndConverter);

    /**
     * Returns the matching converter for {@code type}, or null if none match.
     */
    MatcherAndConverter getConverter(
            String stringValue, TypeLiteral<?> type, Errors errors, Object source);

    /**
     * Returns all converters at this level only.
     */
    Iterable<MatcherAndConverter> getConvertersThisLevel();

    void addTypeListener(TypeListenerBinding typeListenerBinding);

    List<TypeListenerBinding> getTypeListenerBindings();

    /**
     * Forbids the corresponding injector from creating a binding to {@code key}. Child injectors
     * blacklist their bound keys on their parent injectors to prevent just-in-time bindings on the
     * parent injector that would conflict.
     */
    void blacklist(Key<?> key);

    /**
     * Returns true if {@code key} is forbidden from being bound in this injector. This indicates that
     * one of this injector's descendent's has bound the key.
     */
    boolean isBlacklisted(Key<?> key);

    /**
     * Returns the shared lock for all injector data. This is a low-granularity, high-contention lock
     * to be used when reading mutable data (ie. just-in-time bindings, and binding blacklists).
     */
    Object lock();

    // ES_GUICE: clean blacklist keys
    void clearBlacklisted();

    void makeAllBindingsToEagerSingletons(Injector injector);
}
