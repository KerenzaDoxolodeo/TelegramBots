package org.telegram.telegrambots.bots.commandbot;


import org.telegram.telegrambots.ApiContext;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.bots.commandbot.commands.CommandRegistry;
import org.telegram.telegrambots.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.bots.commandbot.commands.ICommandRegistry;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * This class adds command functionality to the TelegramLongPollingBot
 *
 * @author Timo Schulz (Mit0x2)
 */
public abstract class TelegramLongPollingCommandBot extends TelegramLongPollingBot implements ICommandRegistry {
    private final CommandRegistry commandRegistry;
    private String botUsername;

    /**
     * Creates a TelegramLongPollingCommandBot using default options
     * Use ICommandRegistry's methods on this bot to register commands
     *
     * @param botUsername Username of the bot
     */
    public TelegramLongPollingCommandBot(String botUsername) {
        this(ApiContext.getInstance(DefaultBotOptions.class), botUsername);
    }

    /**
     * Creates a TelegramLongPollingCommandBot with custom options and allowing commands with
     * usernames
     * Use ICommandRegistry's methods on this bot to register commands
     *
     * @param options     Bot options
     * @param botUsername Username of the bot
     */
    public TelegramLongPollingCommandBot(DefaultBotOptions options, String botUsername) {
        this(options, true, botUsername);
    }

    /**
     * Creates a TelegramLongPollingCommandBot
     * Use ICommandRegistry's methods on this bot to register commands
     *
     * @param options                   Bot options
     * @param allowCommandsWithUsername true to allow commands with parameters (default),
     *                                  false otherwise
     * @param botUsername               bot username of this bot
     */
    public TelegramLongPollingCommandBot(DefaultBotOptions options, boolean allowCommandsWithUsername, String botUsername) {
        super(options);
        this.botUsername = botUsername;
        this.commandRegistry = new CommandRegistry(allowCommandsWithUsername, botUsername);
    }

    @Override
    public final void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.isCommand() && !filter(message)) {
                if (!commandRegistry.executeCommand(this, message)) {
                    //we have received a not registered command, handle it as invalid
                    processInvalidCommandUpdate(update);
                }
                return;
            }
        }
        processNonCommandUpdate(update);
    }

    /**
     * This method is called when user sends a not registered command. By default it will just call processNonCommandUpdate(),
     * override it in your implementation if you want your bot to do other things, such as sending an error message
     *
     * @param update Received update from Telegram
     */
    protected void processInvalidCommandUpdate(Update update) {
        processNonCommandUpdate(update);
    }


    /**
     * Override this function in your bot implementation to filter messages with commands
     * <p>
     * For example, if you want to prevent commands execution incoming from group chat:
     * #
     * # return !message.getChat().isGroupChat();
     * #
     *
     * @param message Received message
     * @return true if the message must be ignored by the command bot and treated as a non command message,
     * false otherwise
     * @note Default implementation doesn't filter anything
     */
    protected boolean filter(Message message) {
        return false;
    }

    @Override
    public final boolean register(IBotCommand botCommand) {
        return commandRegistry.register(botCommand);
    }

    @Override
    public final Map<IBotCommand, Boolean> registerAll(IBotCommand... botCommands) {
        return commandRegistry.registerAll(botCommands);
    }

    @Override
    public final boolean deregister(IBotCommand botCommand) {
        return commandRegistry.deregister(botCommand);
    }

    @Override
    public final Map<IBotCommand, Boolean> deregisterAll(IBotCommand... botCommands) {
        return commandRegistry.deregisterAll(botCommands);
    }

    @Override
    public final Collection<IBotCommand> getRegisteredCommands() {
        return commandRegistry.getRegisteredCommands();
    }

    @Override
    public void registerDefaultAction(BiConsumer<AbsSender, Message> defaultConsumer) {
        commandRegistry.registerDefaultAction(defaultConsumer);
    }

    @Override
    public final IBotCommand getRegisteredCommand(String commandIdentifier) {
        return commandRegistry.getRegisteredCommand(commandIdentifier);
    }

    /**
     * @return Bot username
     */
    @Override
    public final String getBotUsername() {
        return botUsername;
    }

    /**
     * Process all updates, that are not commands.
     *
     * @param update the update
     * @warning Commands that have valid syntax but are not registered on this bot,
     * won't be forwarded to this method <b>if a default action is present</b>.
     */
    public abstract void processNonCommandUpdate(Update update);
}
