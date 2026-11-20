package net.tpi.tradingcards.item;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** A music disc that also shows which release the song is from, under vanilla's "The Plot in You - Song" line. */
public class MusicDiscItem extends Item {

	private final String album;
	private final int year;

	public MusicDiscItem(Properties properties, String album, int year) {
		super(properties);
		this.album = album;
		this.year = year;
	}

	@Override
	public void appendHoverText(
			final ItemStack itemStack, final Item.TooltipContext context, final TooltipDisplay display,
			final Consumer<Component> textConsumer, final TooltipFlag tooltipFlag) {
		textConsumer.accept(Component.literal(album + " (" + year + ")").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
	}
}
