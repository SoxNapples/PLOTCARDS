package net.tpi.tradingcards.block;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.tpi.tradingcards.TPITradingCards;
import net.tpi.tradingcards.item.MicrophoneItem;

/** Registers the microphone block and its corresponding BlockItem. */
public final class ModBlocks {

	private ModBlocks() {
	}

	public static final Block MICROPHONE = registerBlock("microphone",
			BlockBehaviour.Properties.of()
					.noOcclusion()
					.strength(0.5F)
					.sound(SoundType.WOOD));

	public static final Item MICROPHONE_ITEM = registerBlockItem("microphone", MICROPHONE);

	private static Block registerBlock(String path, BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TPITradingCards.MOD_ID, path));
		Block block = new MicrophoneBlock(properties.setId(key));
		return Registry.register(BuiltInRegistries.BLOCK, key, block);
	}

	private static Item registerBlockItem(String path, Block block) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TPITradingCards.MOD_ID, path));
		Consumable singConsumable = Consumable.builder()
				.consumeSeconds(3.0F)
				.animation(ItemUseAnimation.EAT)
				.sound(Holder.direct(SoundEvents.EMPTY))
				.hasConsumeParticles(false)
				.build();

		// Same restriction real swords have: no per-block speed rules and canDestroyBlocksInCreative=false,
		// so left-clicking a block in creative doesn't instantly break it while holding the mic (survival
		// mining stays as slow as bare hands too, since defaultMiningSpeed matches the unarmed default).
		Tool noMiningTool = new Tool(List.of(), 1.0F, 1, false);

		// Base player attack damage is 1.0 - this ADD_VALUE modifier brings a hit up to 5.0 total.
		ItemAttributeModifiers attackDamage = ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 4.0, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
				.build();

		Item.Properties properties = new Item.Properties()
				.setId(key)
				.component(DataComponents.CONSUMABLE, singConsumable)
				.component(DataComponents.TOOL, noMiningTool)
				.attributes(attackDamage)
				.enchantable(14);
		BlockItem item = new MicrophoneItem(block, properties);
		item.registerBlocks(Item.BY_BLOCK, item);
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	public static void initialize() {
		TPITradingCards.LOGGER.info("Registered microphone block");
	}
}
