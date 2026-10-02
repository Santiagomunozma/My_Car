package com.micarro.feature.vehicle.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class VehicleCatalogHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private var cachedCatalog: Map<String, Map<String, List<String>>>? = null

    protected open fun readJsonFromAssets(): String {
        return try {
            context.assets.open("vehicles_colombia.json").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            val stream = javaClass.classLoader?.getResourceAsStream("vehicles_colombia.json")
                ?: javaClass.classLoader?.getResourceAsStream("assets/vehicles_colombia.json")
                ?: javaClass.classLoader?.getResourceAsStream("com/micarro/assets/vehicles_colombia.json")
                ?: Thread.currentThread().contextClassLoader?.getResourceAsStream("vehicles_colombia.json")
            stream?.bufferedReader()?.use { it.readText() } ?: "{}"
        }
    }

    private suspend fun loadCatalog(): Map<String, Map<String, List<String>>> =
        withContext(Dispatchers.IO) {
            cachedCatalog?.let { return@withContext it }

            val jsonString = readJsonFromAssets()
            val typeToken = object : TypeToken<Map<String, Map<String, List<String>>>>() {}.type
            val data: Map<String, Map<String, List<String>>> = gson.fromJson(jsonString, typeToken) ?: emptyMap()
            cachedCatalog = data
            data
        }

    open suspend fun getBrands(typeKey: String): List<String> {
        val catalog = loadCatalog()
        val brandsMap = catalog[typeKey] ?: emptyMap()
        return brandsMap.keys.sorted()
    }

    open suspend fun getLines(typeKey: String, brand: String): List<String> {
        if (brand.isBlank()) return emptyList()
        val catalog = loadCatalog()
        val brandsMap = catalog[typeKey] ?: emptyMap()
        return brandsMap[brand]?.sorted() ?: emptyList()
    }
}
