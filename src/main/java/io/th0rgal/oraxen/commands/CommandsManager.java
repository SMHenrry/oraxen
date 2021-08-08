package io.th0rgal.oraxen.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.config.Message;
import io.th0rgal.oraxen.items.ItemBuilder;
import io.th0rgal.oraxen.items.OraxenItems;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class CommandsManager {

    public void loadCommands() {
        new CommandAPICommand("oraxen")
                .withAliases("o", "oxn")
                .withPermission("oraxen.command")
                .withSubcommand(getInvCommand())
                .withSubcommand(getGiveCommand())
                .withSubcommand(getPackCommand())
                .withSubcommand((new RepairCommand()).getRepairCommand())
                .withSubcommand((new RecipesCommand()).getRecipesCommand())
                .withSubcommand((new ReloadCommand()).getReloadCommand())
                .withSubcommand((new DebugCommand()).getDebugCommand())
                .executes((sender, args) -> {
                    Message.COMMAND_HELP.send(sender);
                })
                .register();
    }

    private CommandAPICommand getPackCommand() {
        return new CommandAPICommand("pack")
                .withPermission("oraxen.command.pack")
                .withArguments(new StringArgument("action").replaceSuggestions(info -> new String[]{"send", "msg"}))
                .withArguments(new EntitySelectorArgument("target", EntitySelectorArgument.EntitySelector.MANY_PLAYERS))
                .executes((sender, args) -> {
                    final Collection<Player> targets = (Collection<Player>) args[1];
                    if (args[0].equals("msg"))
                        for (Player target : targets)
                            Message.COMMAND_JOIN_MESSAGE.send(target, "pack_url",
                                OraxenPlugin.get().getUploadManager().getHostingProvider().getPackURL());
                    else {
                        for (Player target : targets)
                            OraxenPlugin.get().getUploadManager().getSender().sendPack(target);
                    }
                });
    }

    private CommandAPICommand getInvCommand() {
        return new CommandAPICommand("inventory")
                .withAliases("inv")
                .withPermission("oraxen.command.inventory.view")
                .executes((sender, args) -> {
                    if (sender instanceof Player player)
                        OraxenPlugin.get().getInvManager().getItemsView().show(player);
                    else
                        Message.NOT_PLAYER.send(sender);
                });
    }

    private CommandAPICommand getGiveCommand() {
        return new CommandAPICommand("give")
                .withPermission("oraxen.command.give")
                .withArguments(new EntitySelectorArgument("target", EntitySelectorArgument.EntitySelector.MANY_PLAYERS))
                .withArguments(new StringArgument("item").replaceSuggestions(info -> OraxenItems.getItemNames()))
                .withArguments(new IntegerArgument("amount"))
                .executes((sender, args) -> {
                    final Collection<Player> targets = (Collection<Player>) args[0];
                    final ItemBuilder itemBuilder = OraxenItems.getItemById((String) args[1]);
                    int amount = (int) args[2];
                    final int max = itemBuilder.getMaxStackSize();
                    final int slots = amount / max + (max % amount > 0 ? 1 : 0);
                    final ItemStack[] items = itemBuilder.buildArray(slots > 36 ? (amount = max * 36) : amount);

                    for (Player target : targets)
                        target.getInventory().addItem(items);

                    if (targets.size() == 1)
                        Message.GIVE_PLAYER
                                .send(sender, "player", targets.iterator().next().getName(), "amount", String.valueOf(amount),
                                "item", OraxenItems.getIdByItem(itemBuilder));
                     else
                        Message.GIVE_PLAYERS
                                .send(sender, "count", String.valueOf(targets.size()), "amount", String.valueOf(amount),
                                        "item", OraxenItems.getIdByItem(itemBuilder));


                });
    }

}
