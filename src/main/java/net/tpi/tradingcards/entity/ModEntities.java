package net.tpi.tradingcards.entity;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.tpi.tradingcards.TPITradingCards;
import net.tpi.tradingcards.config.TPIConfig;

/** Registers the band member NPCs and Captain Sox: entity types, spawn eggs, attributes and natural spawning. */
public final class ModEntities {

	private ModEntities() {
	}

	public static final EntityType<BandMemberEntity> LANDON_TEWERS = register("landon_tewers");
	public static final EntityType<BandMemberEntity> JOSH_CHILDRESS = register("josh_childress");
	public static final EntityType<BandMemberEntity> ETHAN_YODER = register("ethan_yoder");
	public static final EntityType<BandMemberEntity> MICHAEL_COOPER = register("michael_cooper");

	public static final List<EntityType<BandMemberEntity>> BAND_MEMBERS = List.of(LANDON_TEWERS, JOSH_CHILDRESS, ETHAN_YODER, MICHAEL_COOPER);

	public static final EntityType<CaptainSoxEntity> CAPTAIN_SOX = Registry.register(BuiltInRegistries.ENTITY_TYPE, key("captain_sox"),
			EntityType.Builder.of(CaptainSoxEntity::new, MobCategory.CREATURE)
					.sized(0.6F, 0.7F)
					.eyeHeight(0.35F)
					.passengerAttachments(0.5125F)
					.clientTrackingRange(8)
					.build(key("captain_sox")));

	/** Band member eggs, then Captain Sox's - used by the creative tab. */
	public static final List<Item> SPAWN_EGGS;

	static {
		List<Item> eggs = new ArrayList<>(BAND_MEMBERS.stream().map(ModEntities::registerSpawnEgg).toList());
		eggs.add(registerSpawnEgg(CAPTAIN_SOX));
		SPAWN_EGGS = List.copyOf(eggs);
	}

	private static ResourceKey<EntityType<?>> key(String name) {
		return ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(TPITradingCards.MOD_ID, name));
	}

	private static EntityType<BandMemberEntity> register(String name) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(TPITradingCards.MOD_ID, name));
		EntityType<BandMemberEntity> type = EntityType.Builder.of(BandMemberEntity::new, MobCategory.CREATURE)
				.sized(0.6F, 1.8F)
				.eyeHeight(1.62F)
				.clientTrackingRange(10)
				.build(key);
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
	}

	private static Item registerSpawnEgg(EntityType<?> type) {
		Identifier typeId = BuiltInRegistries.ENTITY_TYPE.getKey(type);
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM,
				Identifier.fromNamespaceAndPath(TPITradingCards.MOD_ID, typeId.getPath() + "_spawn_egg"));
		return Registry.register(BuiltInRegistries.ITEM, key, new SpawnEggItem(new Item.Properties().setId(key).spawnEgg(type)));
	}

	public static void initialize() {
		int weight = TPIConfig.INSTANCE.bandMemberSpawnWeight;
		for (EntityType<BandMemberEntity> type : BAND_MEMBERS) {
			FabricDefaultAttributeRegistry.register(type, BandMemberEntity.createAttributes());
			SpawnPlacements.register(type, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
					BandMemberEntity::checkBandMemberSpawnRules);
			if (weight > 0) {
				BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.CREATURE, type, weight, 1, 1);
			}
		}

		FabricDefaultAttributeRegistry.register(CAPTAIN_SOX, Cat.createAttributes());
		SpawnPlacements.register(CAPTAIN_SOX, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				Animal::checkAnimalSpawnRules);
		int soxWeight = TPIConfig.INSTANCE.captainSoxSpawnWeight;
		if (soxWeight > 0) {
			BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), MobCategory.CREATURE, CAPTAIN_SOX, soxWeight, 1, 1);
		}
		TPITradingCards.LOGGER.info("Registered {} band member NPCs (spawn weight {} each) and Captain Sox (spawn weight {})",
				BAND_MEMBERS.size(), weight, soxWeight);
	}
}
