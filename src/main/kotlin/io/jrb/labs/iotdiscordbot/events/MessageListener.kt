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
package io.jrb.labs.iotdiscordbot.events

import discord4j.core.`object`.entity.Message
import io.jrb.labs.common.logging.LoggerDelegate
import reactor.core.publisher.Mono
import java.util.function.Predicate

abstract class MessageListener {

    private val log by LoggerDelegate()

    private val notFromBot = Predicate<Message> { m -> m.author.map { user -> !user.isBot }.orElse(false) }
    private val isOnlyBotMentioned = Predicate<Message> { m ->
        m.userMentions.size == 1 &&
        m.userMentions.find { user -> user.username == "Brulenet" } != null
    }

    fun extractContent(eventMessage: Message): Mono<String> {
        return Mono.just(eventMessage.content)
            .map {
                val regex = """<@\d+>""".toRegex()
                it.replace(regex, "").trim()
            }
            .doOnNext { log.info("{}:: content={}", this.javaClass.simpleName, it) }
    }

    fun filterBotMessage(eventMessage: Message): Mono<Message> {
        return Mono.just(eventMessage)
            .filter(notFromBot.and(isOnlyBotMentioned))
            .doOnNext { log.info("{}:: content={}, message={}", this.javaClass.simpleName, it.content, it) }
    }

}