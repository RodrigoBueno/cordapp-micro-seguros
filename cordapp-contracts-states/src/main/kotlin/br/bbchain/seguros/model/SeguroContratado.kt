package br.bbchain.seguros.model

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class SeguroContratado(
        val idExterno: String,
        val descricao: String,
        val tipo: String,
        val valor: Int
)