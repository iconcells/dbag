package com.plomb.plomb.core;

import java.lang.annotation.Retention;
import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME) @Qualifier
public @interface MainThread {
}