
/*
 * package net.phoenix.chromatic_codes;
 * 
 * 
 * // import static net.phoenix.chromatic_codes.PhoenixChromaticCodes.CHROMATIC_REGISTRATE;
 * 
 * public class TestingBlocks {
 * 
 * public static void init() {}
 * 
 * private static @NotNull BlockEntry<Block> registerSimpleBlock(String name, String id, String texture,
 * NonNullBiFunction<Block, Item.Properties, ? extends BlockItem> func) {
 * return CHROMATIC_REGISTRATE
 * .block(id, Block::new)
 * .initialProperties(() -> Blocks.IRON_BLOCK)
 * .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
 * .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
 * prov.models().cubeAll(ctx.getName(), PhoenixChromaticCodes.id("block/" + texture))))
 * .lang(name)
 * .item(func)
 * .build()
 * .register();
 * }
 * 
 * public static BlockEntry<Block> PHOENIX_ENRICHED_TRITANIUM_CASING = registerSimpleBlock(
 * "§zExtremely Heat-Stable Casing", "phoenix_enriched_tritanium_casing",
 * "phoenix_enriched_tritanium_casing", BlockItem::new);
 * 
 * 
 * 
 * 
 * }
 * 
 */
