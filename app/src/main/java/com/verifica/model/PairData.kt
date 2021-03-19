package com.gunar.uta.data

import java.io.Serializable

data class PairData<T>(val idDoc: String, var data: T) : Serializable {}