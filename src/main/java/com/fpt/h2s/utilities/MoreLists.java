package com.fpt.h2s.utilities;

import java.util.List;
import java.util.function.Predicate;

public class MoreLists {
    /**
     * Find index of the first element that meet the specific condition.
     * @param condition condition to find the element.
     * @param collection list that you want to find element from.
     * @return -1 if no item met the condition. Otherwise, return index of first element that meet the condition.
     */
    public static <T> int firstIndexMatch(final Predicate<T> condition, final List<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return -1;
        }
        
        for (int index = 0; index < collection.size(); index++) {
            if (condition.test(collection.get(index))) {
                return index;
            }
        }
        
        return -1;
    }
}
