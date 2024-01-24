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

import discord4j.core.event.ReactiveEventAdapter
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.`object`.entity.Message
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate

class CommandListener(
    private val prefix: String
): ReactiveEventAdapter() {

    private val matchesPrefix = Predicate<Message> { m -> m.content.startsWith(prefix) }

    private val notFromBot = Predicate<Message> { m -> m.author.map { user -> !user.isBot }.orElse(false) }

    private val commandPattern = """^${prefix}\s*(\w+)\s*(.*?)$""".toRegex()

    private val noopCommand = object : Command {
        override fun name(): String = "NOOP"
        override fun apply(t: CommandContext): Publisher<CommandContext> = Mono.empty()
    }

    private val commandCatalog = ConcurrentHashMap<String, Command>()

    override fun onMessageCreate(event: MessageCreateEvent): Publisher<Any> {
        return Mono.just(event.message)
            .filter(notFromBot.and(matchesPrefix))
            .map { createCommandContext(it) }
            .flatMap { runCommand(it) }
    }

    fun on(command: Command): CommandListener {
        return on(command.name(), command)
    }

    fun on(commandName: String, command: Command): CommandListener {
        commandCatalog[commandName] = command
        return this
    }

    private fun createCommandContext(message: Message): CommandContext {
        val (command, remainder) = parseCommand(message.content)
        return CommandContext(message, command, remainder)
    }

    private fun parseCommand(content: String): Pair<String, String> {
        val commandMatch = commandPattern.find(content)!!
        val command: String = commandMatch.groups[1]?.value ?: noopCommand.name()
        val remainder = commandMatch.groups[2]?.value ?: ""
        return Pair(command, remainder)
    }

    private fun runCommand(context: CommandContext): Mono<CommandContext> {
        return Mono.from(commandCatalog.entries.stream()
            .filter { entry -> entry.key == context.command }
            .findFirst()
            .map { it.value }
            .orElse(noopCommand)
            .apply(context))
    }

}