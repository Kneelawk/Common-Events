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
import java.util.Objects;

import com.kneelawk.commonevents.impl.ArrayUtils;

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
    public int removeListener(Object key) {
        int index = ArrayUtils.search(keys, key, 0);
        if (index < 0) return 0;

        T[] callbacks = this.callbacks;
        Object[] keys = this.keys;
        int removed = 0;

        while (index >= 0) {
            int toRemove = 1;
            int len = keys.length;
            assert callbacks.length == len;

            // chances are, if multiple listeners get registered for the same key, they'll all get registered at once
            while (index + toRemove < len && Objects.equals(keys[index + toRemove], key)) toRemove++;

            T[] newCallbacks = Arrays.copyOf(callbacks, len - toRemove);
            Object[] newKeys = Arrays.copyOf(keys, len - toRemove);
            if (index < len - toRemove) {
                System.arraycopy(callbacks, index + toRemove, newCallbacks, index, newCallbacks.length - index);
                System.arraycopy(keys, index + toRemove, newKeys, index, newKeys.length - index);
            }

            callbacks = newCallbacks;
            keys = newKeys;

            removed += toRemove;
            index = ArrayUtils.search(keys, key, index);
        }

        this.callbacks = callbacks;
        this.keys = keys;

        return removed;
    }

    @Override
    public T[] getCallbacks() {
        return callbacks;
    }
}
