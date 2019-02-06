package br.bbchain.seguros.api

import br.bbchain.seguros.flow.SegurarItemFlow
import br.bbchain.seguros.state.ItemSeguradoState
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("seguro")
class SegurosAPI(val rpcOps: CordaRPCOps) {

    @PUT
    @Path("segurar_produto")
    @Consumes(MediaType.APPLICATION_JSON)
    fun segurarProduto(itemSegurado: ItemSeguradoDTO): Response {
        val seguradora = rpcOps.wellKnownPartyFromX500Name(CordaX500Name.parse(itemSegurado.seguradora)) ?:
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Seguradora não reconhecida.")
                        .build()
        val result = rpcOps.startFlowDynamic(SegurarItemFlow.Initiator::class.java,
                itemSegurado.toItemSegurado(), seguradora)
                .returnValue
                .getOrThrow()

        return Response.status(Response.Status.CREATED).entity("Transação " + result.id + " concluída.").build()
    }

    @GET
    @Path("produto_segurado")
    @Produces(MediaType.APPLICATION_JSON)
    fun getProdutosSegurados(): List<ItemSeguradoDTO> {
        return rpcOps.vaultQueryBy<ItemSeguradoState>()
                .states.map { it.state.data }
                .map { ItemSeguradoDTO.fromItemSeguradoState(it) }
    }

}