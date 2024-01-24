/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Jon Brule <brulejr@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.jrb.labs.iotdiscordbot.config

import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.Event
import io.jrb.labs.common.discord4j.command.CommandListener
import io.jrb.labs.common.logging.LoggerDelegate
import io.jrb.labs.iotdiscordbot.commands.EchoCommand
import io.jrb.labs.iotdiscordbot.events.EventListener
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono

@Configuration
@EnableConfigurationProperties(DiscordConfig::class)
class BotJavaConfig {

    private val log by LoggerDelegate()

    @Bean
    fun commandListener() = CommandListener(prefix="!!")
        .on("echo", EchoCommand())

    @Bean
    fun <T : Event> gatewayDiscordClient(
        discordConfig: DiscordConfig,
        eventListeners: List<EventListener<T>>,
        commandListener: CommandListener
    ): GatewayDiscordClient {
        val client = connectToDiscord(discordConfig)
//        eventListeners.forEach { listener -> registerEventListener(client, listener) }
        client.on(commandListener).subscribe()
        return client
    }

    private fun connectToDiscord(discordConfig: DiscordConfig): GatewayDiscordClient {
            val client = DiscordClient.builder(discordConfig.token)
                .build()
                .login()
                .block()
        if (client == null) {
            log.error("Unable to connect to Discord - client is null")
            throw IllegalStateException("Unable to connect to Discord - client is null");
        }
        return client
    }

    private fun <T : Event> registerEventListener(client: GatewayDiscordClient, listener: EventListener<T>) {
        log.info("Registering listener - {}", listener.javaClass.simpleName)
        client.on(listener.getEventType())
            .flatMap(listener::execute)
            .onErrorResume(listener::handleError)
            .subscribe()
    }

}