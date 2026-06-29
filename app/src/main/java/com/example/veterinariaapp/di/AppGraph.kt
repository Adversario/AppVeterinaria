package com.example.veterinariaapp.di

import com.example.veterinaria.data.repository.RepositoryVetRepository

object AppGraph {
    val vetRepository: RepositoryVetRepository by lazy { RepositoryVetRepository() }
}