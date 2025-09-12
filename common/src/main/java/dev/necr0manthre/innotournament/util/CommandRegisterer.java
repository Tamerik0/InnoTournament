package dev.necr0manthre.innotournament.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandRegisterer {
    public final CommandDispatcher<CommandSourceStack> dispatcher;
    private final String[] aliases;

    public CommandRegisterer(CommandDispatcher<CommandSourceStack> dispatcher, String... aliases) {
        this.dispatcher = dispatcher;
        this.aliases = Arrays.copyOf(aliases, aliases.length);
    }

    public List<LiteralCommandNode<CommandSourceStack>> register(ArgumentBuilder<CommandSourceStack, ?> argumentBuilder) {
        var nodes = new ArrayList<LiteralCommandNode<CommandSourceStack>>();
        for (var alias : aliases) {
            nodes.add(dispatcher.register(Commands.literal(alias).then(argumentBuilder)));
        }
        return nodes;
    }

    public List<LiteralCommandNode<CommandSourceStack>> register(CommandNode<CommandSourceStack> argument) {
        var nodes = new ArrayList<LiteralCommandNode<CommandSourceStack>>();
        for (var alias : aliases) {
            nodes.add(dispatcher.register(Commands.literal(alias).then(argument)));
        }
        return nodes;
    }
}
