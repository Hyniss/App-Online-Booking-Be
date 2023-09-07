package com.fpt.h2s.utilities;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;

@UtilityClass
public class ImmutableCollectors {
    
    /**
     * Return an immutable map collector.
     * All null keys and null values will be removed from the created map.
     * @param keyMapper How key of created map will be extracted. Must not be null.
     * @param valueMapper How value of created map will be extracted. Must not be null.
     * @param mergeFunction Choose which occurrence one will be put into the map.
     */
    public static <E, K, V> Collector<E, ?, Map<K, V>> toMap(
        @NonNull final Function<? super E, ? extends K> keyMapper,
        @NonNull final Function<? super E, ? extends V> valueMapper,
        @NonNull final BinaryOperator<V> mergeFunction
    ) {
        return ImmutableCollectors.creatMapCollector(keyMapper, valueMapper, mergeFunction, HashMap::new);
    }
    
    /**
     * Return an immutable map collector.
     * All null keys and null values will be removed from the created map.
     * If there are two values with same key, this map will take the first one.
     * @param keyMapper How key of created map will be extracted. Must not be null.
     * @param valueMapper How value of created map will be extracted. Must not be null.
     */
    public static <E, K, V> Collector<E, ?, Map<K, V>> toMap(
        @NonNull final Function<? super E, ? extends K> keyMapper,
        @NonNull final Function<? super E, ? extends V> valueMapper
    ) {
        return ImmutableCollectors.toMap(keyMapper, valueMapper, (first, second) -> first);
    }
    
    /**
     * Return an immutable map collector.
     * All null keys and null values will be removed from the created map.
     * The keys of this map will be the items of the origin collection.
     * If there are two values with same key, this map will take the first one.
     * @param keyMapper How key of created map will be extracted. Must not be null.
     */
    public static <E, K> Collector<E, ?, Map<K, E>> toMapWithKey(
        @NonNull final Function<? super E, ? extends K> keyMapper
    ) {
        return ImmutableCollectors.toMap(keyMapper, value -> value);
    }
    
    /**
     * Return an immutable map collector.
     * All null keys and null values will be removed from the created map.
     * The values of this map will be the items of the origin collection.
     * @param valueMapper How value of created map will be extracted. Must not be null.
     */
    public static <E, V> Collector<E, ?, Map<E, V>> toMapWithValue(
        @NonNull final Function<? super E, ? extends V> valueMapper
    ) {
        return ImmutableCollectors.toMap(key -> key, valueMapper);
    }
    
    /**
     * Return an immutable linked map collector in which all keys of created map
     * will have the same order with the origin collection.
     * All null keys and null values will be removed from the created map.
     * @param keyMapper How key of created map will be extracted. Must not be null.
     * @param valueMapper How value of created map will be extracted. Must not be null.
     * @param mergeValueFunction Choose which occurrence one will be put into the map. <br/>
     * <strong>NOTE: You can not change the position of the key even if you chose value of the second one.</strong>
     */
    public static <E, K, V> Collector<E, ?, Map<K, V>> toLinkedMap(
        @NonNull final Function<? super E, ? extends K> keyMapper,
        @NonNull final Function<? super E, ? extends V> valueMapper,
        @NonNull final BinaryOperator<V> mergeValueFunction
    ) {
        return ImmutableCollectors.creatMapCollector(keyMapper, valueMapper, mergeValueFunction, LinkedHashMap::new);
    }
    
    /**
     * Return an immutable linked map collector in which all keys of created map
     * will have the same order with the origin collection.
     * All null keys and null values will be removed from the created map.
     * If there are two values with same key, this map will take the first one.
     * @param keyMapper How key of created map will be extracted. Must not be null.
     * @param valueMapper How value of created map will be extracted. Must not be null.
     */
    public static <E, K, V> Collector<E, ?, Map<K, V>> toLinkedMap(
        @NonNull final Function<? super E, ? extends K> keyMapper,
        @NonNull final Function<? super E, ? extends V> valueMapper
    ) {
        return ImmutableCollectors.toLinkedMap(keyMapper, valueMapper, (first, second) -> first);
    }
    
    /**
     * Return an immutable linked map collector in which all keys of created map
     * will have the same order with the origin collection.
     * All null keys and null values will be removed from the created map.
     * The keys of this map will be the items of the origin collection.
     * If there are two values with same key, this map will take the first one.
     * @param keyMapper How key of created map will be extracted. Must not be null.
     */
    public static <E, K> Collector<E, ?, Map<K, E>> toLinkedMapWithKey(
        @NonNull final Function<? super E, ? extends K> keyMapper
    ) {
        return ImmutableCollectors.toLinkedMap(keyMapper, value -> value);
    }
    
    /**
     * Return an immutable linked map collector in which all keys of created map
     * will have the same order with the origin collection.
     * All null keys and null values will be removed from the created map.
     * The values of this map will be the items of the origin collection.
     * @param valueMapper How value of created map will be extracted. Must not be null.
     */
    public static <E, V> Collector<E, ?, Map<E, V>> toLinkedMapWithValue(
        @NonNull final Function<? super E, ? extends V> valueMapper
    ) {
        return ImmutableCollectors.toLinkedMap(key -> key, valueMapper);
    }
    
    private static <E, K, V> Collector<E, Object, Map<K, V>> creatMapCollector(
        final Function<? super E, ? extends K> keyMapper,
        final Function<? super E, ? extends V> valueMapper,
        final BinaryOperator<V> mergeFunction,
        final Supplier<Map<K, V>> map
    ) {
        final K nullKey = (K) new Object();
        final V nullValue = (V) new Object();
        
        return Collectors.collectingAndThen(
            Collectors.toMap(
                (element) -> {
                    if (element == null) {
                        return nullKey;
                    }
                    final K key = keyMapper.apply(element);
                    return key == null ? nullKey : key;
                },
                (element) -> {
                    if (element == null) {
                        return nullValue;
                    }
                    final V value = valueMapper.apply(element);
                    return value == null ? nullValue : value;
                },
                (first, second) -> {
                    try {
                        return mergeFunction.apply(first, second);
                    } catch (final ClassCastException exceptionWhenSecondOneIsNull) {
                        return first;
                    }
                },
                map),
            (resultMap) -> {
                resultMap.keySet().removeIf(key -> key == nullKey);
                resultMap.values().removeIf(value -> value == nullValue);
                return Collections.unmodifiableMap(resultMap);
            }
        );
    }
    
    /**
     * Return an immutable set collector in which all null elements will be removed.
     */
    public static <E> Collector<E, ?, Set<E>> toSet() {
        return Collectors.collectingAndThen(ImmutableCollectors.createCollector(HashSet::new), Collections::unmodifiableSet);
    }
    
    /**
     * Return an immutable linked set collector in which all null elements will be removed.
     */
    public static <E> Collector<E, ?, Set<E>> toLinkedSet() {
        return Collectors.collectingAndThen(ImmutableCollectors.createCollector(LinkedHashSet::new), Collections::unmodifiableSet);
    }
    
    /**
     * Return an immutable list collector in which all null elements will be removed.
     */
    public static <E> Collector<E, ?, List<E>> toList() {
        return Collectors.collectingAndThen(ImmutableCollectors.createCollector(ArrayList::new), Collections::unmodifiableList);
    }
    
    /**
     * Return an immutable linked list collector in which all null elements will be removed.
     */
    public static <E> Collector<E, ?, List<E>> toLinkedList() {
        return Collectors.collectingAndThen(ImmutableCollectors.createCollector(LinkedList::new), Collections::unmodifiableList);
    }
    
    private static <E, C extends Collection<E>> Collector<E, C, C> createCollector(final Supplier<C> supplier) {
        return Collector.of(
            supplier,
            (collection, element) -> {
                if (element != null) {
                    collection.add(element);
                }
            },
            (left, right) -> {
                if (left.size() < right.size()) {
                    right.addAll(left);
                    return right;
                } else {
                    left.addAll(right);
                    return left;
                }
            });
    }
    
    
    public static <X, Y> Collector<X, ?, List<Y>> toList(@NonNull final Function<X, Y> function) {
        return mapping(function, ImmutableCollectors.toList());
    }
    
}
