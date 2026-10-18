package com.example.pruebaaplicacionesmoviles

import android.app.Application
import com.example.pruebaaplicacionesmoviles.data.local.MpazDatabase
import com.example.pruebaaplicacionesmoviles.data.repository.MpazRepository
import com.example.pruebaaplicacionesmoviles.data.repository.RoomMpazRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MpazApp : Application() {

    lateinit var repositorio: MpazRepository
        private set

    private val alcanceApp = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val dao = MpazDatabase.obtener(this).mpazDao()
        repositorio = RoomMpazRepository(dao)
        alcanceApp.launch {
            repositorio.preparar()
        }
    }
}