package com.eon.common.datasource.support;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * ThreadLocal 维护当前数据源栈，支持嵌套调用。
 */
public class DataSourceContextHolder {

    private final ThreadLocal<Deque<String>> holder = ThreadLocal.withInitial(ArrayDeque::new);

    public void push(String key) {
        if (key != null) {
            holder.get().push(key);
        }
    }

    public void clear() {
        holder.remove();
    }

    public Optional<String> peek() {
        return Optional.ofNullable(holder.get().peek());
    }

    public void pop() {
        Deque<String> deque = holder.get();
        if (!deque.isEmpty()) {
            deque.pop();
        }
        if (deque.isEmpty()) {
            holder.remove();
        }
    }
}
