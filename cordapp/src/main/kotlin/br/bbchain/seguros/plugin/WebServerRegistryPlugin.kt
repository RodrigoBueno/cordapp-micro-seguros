package br.bbchain.seguros.plugin

import br.bbchain.seguros.api.SegurosAPI
import net.corda.core.messaging.CordaRPCOps
import net.corda.webserver.services.WebServerPluginRegistry
import java.util.function.Function

class WebServerRegistryPlugin : WebServerPluginRegistry {
    override val webApis: List<Function<CordaRPCOps, out Any>>
        get() = listOf(Function(::SegurosAPI))
}