package com.fpt.h2s.utilities;

import com.fpt.h2s.models.exceptions.ApiException;

public class Generalizable<T> {
    
    private Class<T> genericClass;
    
    public final Class<T> getGenericClass() {
        if (this.genericClass == null) {
            this.genericClass =
                (Class<T>) MoreReflections
                    .genericTypeOf(this.getClass())
                    .orElseThrow(() ->
                        ApiException.failed(
                            "[DEV ALERT] Class {} is not parametrized with generic type!!! Please use extends <>.",
                            this.getClass().getName()
                        )
                    );
        }
        return this.genericClass;
    }
}