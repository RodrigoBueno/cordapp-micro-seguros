package br.bbchain.seguros.flow

import br.bbchain.seguros.contract.ItemSeguradoContract
import br.bbchain.seguros.model.ItemSegurado
import br.bbchain.seguros.state.ItemSeguradoState
import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.unwrap
import java.time.Instant

object SegurarItemFlow {

    @InitiatingFlow
    @StartableByRPC
    class Initiator(val itemSegurado: ItemSegurado,
                    val segurador: Party): FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            val comando = Command(ItemSeguradoContract.Commands.SegurarItem(),
                    listOf(ourIdentity, segurador).map { it.owningKey })

            val sessionSegurador = initiateFlow(segurador)

            val horaTransacao = sessionSegurador.receive<Instant>().unwrap {
                it
            }

            val itemSeguradoComTimestamp = itemSegurado.copy(timeStamp = horaTransacao)

            val output = ItemSeguradoState(itemSeguradoComTimestamp, segurador, ourIdentity)

            val txBuilder = TransactionBuilder(notary)
                    .addCommand(comando)
                    .addOutputState(output, ItemSeguradoContract::class.java.canonicalName)

            txBuilder.verify(serviceHub)

            val transacaoParcialmenteAssinada = serviceHub.signInitialTransaction(txBuilder)

            val transacaoAssinada = subFlow(CollectSignaturesFlow(transacaoParcialmenteAssinada,
                    listOf(sessionSegurador)))

            return subFlow(FinalityFlow(transacaoAssinada))
        }
    }

    @InitiatedBy(Initiator::class)
    class Responder(val otherSession: FlowSession): FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {

            val horaTransacao = Instant.now()
            otherSession.send(horaTransacao)

            return subFlow(object : SignTransactionFlow(otherSession) {
                override fun checkTransaction(stx: SignedTransaction) {
                    requireThat {
                        "Deveria ter utilizado o meu timestamp" using (
                                stx.coreTransaction.outputsOfType<ItemSeguradoState>().all {
                                    it.itemSegurado.timeStamp == horaTransacao })
                        "Eu deveria ser o segurador." using
                                stx.coreTransaction.outputsOfType<ItemSeguradoState>().all { it.segurador == ourIdentity }
                        "Deve haver apenas states de Item Segurado." using
                                (stx.coreTransaction.outputsOfType<ItemSeguradoState>().size ==
                                        stx.coreTransaction.outputs.size)
                    }
                }
            })
        }
    }

}