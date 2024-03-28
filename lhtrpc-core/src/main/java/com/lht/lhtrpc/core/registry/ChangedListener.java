package com.lht.lhtrpc.core.registry;

import com.lht.lhtrpc.core.registry.Event;

/**
 * @author Leo
 * @date 2024/03/18
 */
public interface ChangedListener {
    void fire(Event data);
}
