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

import discord4j.core.event.domain.message.MessageUpdateEvent
import io.jrb.labs.common.logging.LoggerDelegate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class MessageUpdateListener: MessageListener(), EventListener<MessageUpdateEvent> {

    private val log by LoggerDelegate()

    override fun getEventType(): Class<MessageUpdateEvent> {
        return MessageUpdateEvent::class.java
    }

    override fun handleError(error: Throwable): Mono<Void> {
        log.error("Unable to process " + getEventType().getSimpleName(), error);
        return Mono.empty()
    }

    override fun execute(event: MessageUpdateEvent): Mono<Void> {
        return Mono.just(event)
            .filter(MessageUpdateEvent::isContentChanged)
            .flatMap(MessageUpdateEvent::getMessage)
            .flatMap { filterBotMessage(it) }
            .flatMap { extractContent(it) }
            .then()
    }

}