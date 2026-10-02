package com.micarro.feature.maintenance.data

import android.content.Context
import com.micarro.feature.maintenance.presentation.DEFAULT_CATEGORIES
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalMaintenanceCategoryStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun all(): List<String> {
        val extra = prefs.getStringSet(KEY, emptySet()).orEmpty()
        return (DEFAULT_CATEGORIES + extra.sorted()).distinct()
    }

    fun add(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed in DEFAULT_CATEGORIES) return
        val next = prefs.getStringSet(KEY, emptySet()).orEmpty().toMutableSet()
        next += trimmed
        prefs.edit().putStringSet(KEY, next).apply()
    }

    companion object {
        private const val PREFS = "maintenance_categories"
        private const val KEY = "extra"
    }
}
