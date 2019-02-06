package br.bbchain.seguros.api

import br.bbchain.seguros.model.ItemSegurado
import br.bbchain.seguros.model.SeguroContratado
import br.bbchain.seguros.state.ItemSeguradoState
import java.time.Instant

data class ItemSeguradoDTO (
        val produto : String,
        val marca : String,
        val tipo : String,
        val valor : String,
        val seguradora: String,
        val vendedor: String?,
        val timeStamp: Instant?,
        val seguros : List<SeguroDTO>
) {
    companion object {
        fun fromItemSeguradoState(state: ItemSeguradoState): ItemSeguradoDTO {
            return ItemSeguradoDTO(
                    state.itemSegurado.produto,
                    state.itemSegurado.marca,
                    state.itemSegurado.tipo,
                    state.itemSegurado.valor.toString(),
                    state.segurador.name.toString(),
                    state.vendedor.name.toString(),
                    state.itemSegurado.timeStamp,
                    state.itemSegurado.segurosContratados.map { SeguroDTO.fromSeguroContratado(it) }
            )
        }
    }
    fun toItemSegurado(): ItemSegurado {
        return ItemSegurado(
                produto,
                marca,
                tipo,
                Instant.now(),
                valor.toInt(),
                seguros.map {
                    it.toSeguroContratado()
                    }.toSet())
    }
}

data class SeguroDTO (
       val id : String,
       val descricao : String,
       val tipo : String,
       val valor : String
){
    companion object {
        fun fromSeguroContratado(seguro: SeguroContratado) =
                SeguroDTO(
                        seguro.idExterno,
                        seguro.descricao,
                        seguro.tipo,
                        seguro.valor.toString())
    }

    fun toSeguroContratado(): SeguroContratado = SeguroContratado(
            id,
            descricao,
            tipo,
            valor.toInt())
}