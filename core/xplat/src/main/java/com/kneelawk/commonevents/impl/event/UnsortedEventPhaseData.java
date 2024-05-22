/*
 * Copyright (c) 2024 Cyan Kneelawk.
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

package com.kneelawk.commonevents.impl.event;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

public class UnsortedEventPhaseData<T> implements EventPhaseData<T> {
    private T[] callbacks;
    private Object[] keys;

    @SuppressWarnings("unchecked")
    public UnsortedEventPhaseData(Class<?> callbackClass) {
        callbacks = (T[]) Array.newInstance(callbackClass, 0);
        keys = new Object[0];
    }

    @Override
    public void addListener(Object key, T listener) {
        int oldLength = callbacks.length;
        callbacks = Arrays.copyOf(callbacks, oldLength + 1);
        callbacks[oldLength] = listener;
        keys = Arrays.copyOf(keys, oldLength + 1);
        keys[oldLength] = key;
    }

    @Override
    public void removeListener(Object key) {
        int index = ArrayUtils.indexOf(keys, key);
        if (index < 0) throw new IllegalArgumentException("No listener key: " + key);

        T[] newCallbacks = Arrays.copyOf(callbacks, callbacks.length - 1);
        Object[] newKeys = Arrays.copyOf(keys, keys.length - 1);
        if (index < callbacks.length - 1) {
            System.arraycopy(callbacks, index + 1, newCallbacks, index, newCallbacks.length - index);
            System.arraycopy(keys, index + 1, newKeys, index, newKeys.length - index);
        }

        callbacks = newCallbacks;
        keys = newKeys;
    }

    @Override
    public T[] getCallbacks() {
        return callbacks;
    }
}
