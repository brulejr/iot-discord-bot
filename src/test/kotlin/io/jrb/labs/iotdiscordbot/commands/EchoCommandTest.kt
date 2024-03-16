package io.jrb.labs.iotdiscordbot.commands

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.spec.MessageCreateSpec
import discord4j.discordjson.possible.Possible
import io.jrb.labs.common.discord4j.command.CommandContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.kotlin.*
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.test.StepVerifier
import java.util.function.Supplier

class EchoCommandTest {

    private val commandMessage: Message = mock()
    private val discordMessage: Message = mock()
    private val messageChannel: MessageChannel = mock()
    private lateinit var replyChannelSupplier: Supplier<Mono<MessageChannel>>
    private lateinit var echoCommand: EchoCommand

    @BeforeEach
    fun setup() {
        replyChannelSupplier = Supplier { Mono.just(messageChannel) }
        echoCommand = EchoCommand()
    }

    @Test
    fun testHello() {
        val context = spy(CommandContext(commandMessage, "echo", "there", replyChannelSupplier, Schedulers.immediate()))
        whenever(messageChannel.createMessage(any(MessageCreateSpec::class.java))).thenReturn(Mono.just(discordMessage))

        StepVerifier.create(echoCommand.apply(context))
                .expectNextMatches {
                    assertThat(it)
                            .hasFieldOrPropertyWithValue("command", "echo")
                            .hasFieldOrPropertyWithValue("remainder", "there")
                    true
                }
                .verifyComplete()

        val captor = argumentCaptor<MessageCreateSpec>()
        verify(messageChannel, times(1)).createMessage(captor.capture())
        assertThat(captor.firstValue.content())
                .isNotNull()
                .isEqualTo(Possible.of("echo 'there'"))
    }

    @Test
    fun testSimpleEcho() {
        val context = spy(CommandContext(commandMessage, "echo", "there", replyChannelSupplier, Schedulers.immediate()))
        whenever(messageChannel.createMessage(any(MessageCreateSpec::class.java))).thenReturn(Mono.just(discordMessage))

        StepVerifier.create(echoCommand.apply(context))
                .expectNextMatches {
                    assertThat(it)
                            .hasFieldOrPropertyWithValue("command", "echo")
                            .hasFieldOrPropertyWithValue("remainder", "there")
                    true
                }
                .verifyComplete()

        val captor = argumentCaptor<MessageCreateSpec>()
        verify(messageChannel, times(1)).createMessage(captor.capture())
        assertThat(captor.firstValue.content())
                .isNotNull()
                .isEqualTo(Possible.of("echo 'there'"))
    }
}