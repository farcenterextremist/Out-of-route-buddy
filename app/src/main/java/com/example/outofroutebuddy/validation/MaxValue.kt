package com.example.outofroutebuddy.validation

/**
 * Validation annotation to ensure a field has a maximum value.
 * This provides compile-time documentation and can be used for runtime validation.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class MaxValue(val value: Double) 
