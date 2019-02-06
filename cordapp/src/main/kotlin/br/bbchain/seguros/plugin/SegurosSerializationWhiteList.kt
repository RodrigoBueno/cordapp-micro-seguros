package br.bbchain.seguros.plugin

import net.corda.core.serialization.SerializationWhitelist

class SegurosSerializationWhiteList : SerializationWhitelist{
    override val whitelist: List<Class<*>> = listOf()
}