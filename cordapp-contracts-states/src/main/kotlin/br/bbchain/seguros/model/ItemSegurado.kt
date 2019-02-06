package br.bbchain.seguros.model

import net.corda.core.serialization.CordaSerializable
import java.time.Instant

@CordaSerializable
data class ItemSegurado(
        val produto: String,
        val marca: String,
        val tipo: String,
        val timeStamp: Instant,
        val valor: Int = 0,
        val segurosContratados: Set<SeguroContratado> = setOf())