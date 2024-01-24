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
package io.jrb.labs.common.discord4j.command

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.MessageCreateSpec
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.util.function.Supplier

data class CommandContext(
    val message: Message,
    val command: String,
    val remainder: String,
    val replyChannelSupplier: Supplier<Mono<MessageChannel>>,
    val replyScheduler: Scheduler
) {

    constructor(message: Message, command: String, remainder: String):
            this(message, command, remainder, Supplier { message.channel }, Schedulers.immediate())

    fun sendEmbed(spec: EmbedCreateSpec): Mono<CommandContext> {
        return replyChannelSupplier.get()
            .publishOn(replyScheduler)
            .flatMap { channel -> channel.createMessage(spec) }
            .then(Mono.just(this))
    }

    fun sendMessage(spec: MessageCreateSpec): Mono<CommandContext> {
        return replyChannelSupplier.get()
            .publishOn(replyScheduler)
            .flatMap { channel -> channel.createMessage(spec) }
            .then(Mono.just(this))
    }

    fun withDirectMessage(): CommandContext {
        return copy(replyChannelSupplier = { getPrivateChannel() })
    }

    fun withReplyChannel(): CommandContext {
        return copy(replyChannelSupplier = { message.channel })
    }

    fun withScheduled(scheduler: Scheduler): CommandContext {
        return copy(replyScheduler = scheduler)
    }

    private fun getPrivateChannel(): Mono<MessageChannel> {
        return Mono.justOrEmpty(message.author).flatMap(User::getPrivateChannel)
    }

}
