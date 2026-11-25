
package com.example.pruebaaplicacionesmoviles.core.network

import com.example.pruebaaplicacionesmoviles.BuildConfig

object ConfigBackend {

    val ORIGEN = BuildConfig.MPAZ_BACKEND_URL.trimEnd('/')

    val API = "$ORIGEN/api/v1/"
}