package br.bbchain.seguros.flow

import br.bbchain.seguros.model.ItemSegurado
import br.bbchain.seguros.model.SeguroContratado
import br.bbchain.seguros.state.ItemSeguradoState
import br.bbchain.seguros.flow.SegurarItemFlow
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals

class SegurarItemFlowTest {
    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(listOf("br.bbchain.seguros"))
        a = network.createNode()
        b = network.createNode()
        listOf(a, b).forEach {
            it.registerInitiatedFlow(SegurarItemFlow.Responder::class.java)
        }
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `deve armazenar o item segurado`() {
        val itemSegurado = ItemSegurado(
                "Teste",
                "Teste",
                "Teste",
                Instant.now(),
                1,
                setOf(
                        SeguroContratado(
                                "Teste",
                                "Teste",
                                "Teste",
                                1)))

        val future = a.startFlow(SegurarItemFlow.Initiator(itemSegurado, b.info.legalIdentities.first()))
        network.runNetwork()

        val stateCriado = future.get().coreTransaction.outputsOfType<ItemSeguradoState>().single()
        assertEquals(itemSegurado.copy(timeStamp = stateCriado.itemSegurado.timeStamp), stateCriado.itemSegurado)
        listOf(a, b).forEach {
            it.transaction {
                val stateConsultado = it.services.vaultService.queryBy<ItemSeguradoState>(
                        QueryCriteria.LinearStateQueryCriteria(
                                linearId = listOf(stateCriado.linearId))).states.single().state.data

                assertEquals(stateCriado, stateConsultado)
                assertEquals(itemSegurado.copy(timeStamp = stateConsultado.itemSegurado.timeStamp),
                        stateConsultado.itemSegurado)
            }
        }
    }
}