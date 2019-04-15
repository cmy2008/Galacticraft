package io.github.teamgalacticraft.galacticraft.world.biome;

import io.github.teamgalacticraft.galacticraft.Constants;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

/**
 * @author <a href="https://github.com/teamgalacticraft">TeamGalacticraft</a>
 */
public class GalacticraftBiomes {

    public static final Biome MOON = Registry.register(Registry.BIOME, new Identifier(Constants.MOD_ID, "moon"), new MoonBiome());
    public static final Biome MOON_PLAINS = Registry.register(Registry.BIOME, new Identifier(Constants.MOD_ID, "moon_plains"), new MoonPlainsBiome());
    public static final Biome MARS = Registry.register(Registry.BIOME, new Identifier(Constants.MOD_ID, "moon_plains"), new MarsBiome());

    public static void init() {
    }
}
