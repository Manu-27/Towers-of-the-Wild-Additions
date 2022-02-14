/* MIT License

        Copyright (c) 2022 TelepathicGrunt

        Permission is hereby granted, free of charge, to any person obtaining a copy
        of this software and associated documentation files (the "Software"), to deal
        in the Software without restriction, including without limitation the rights
        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        copies of the Software, and to permit persons to whom the Software is
        furnished to do so, subject to the following conditions:

        The above copyright notice and this permission notice shall be included in all
        copies or substantial portions of the Software.

        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        SOFTWARE. */

package net.aurelj.totw_additions.structures;

import com.mojang.serialization.Codec;
import net.aurelj.totw_additions.TowersAdditionsMain;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.PostPlacementProcessor;
import net.minecraft.structure.StructureGeneratorFactory;
import net.minecraft.structure.StructurePiecesGenerator;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import org.apache.logging.log4j.Level;

import javax.imageio.IIOImage;
import java.util.Optional;

public class EndTowerStructure extends StructureFeature<StructurePoolFeatureConfig> {

    public EndTowerStructure(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, (context) -> {
                    if (!EndTowerStructure.canGenerate(context)) {
                        return Optional.empty();
                    } else {
                        return EndTowerStructure.createPiecesGenerator(context);
                    }
                },
                PostPlacementProcessor.EMPTY);
    }

    private static boolean canGenerate(StructureGeneratorFactory.Context<StructurePoolFeatureConfig> context) {
        BlockPos spawnXZPosition = context.chunkPos().getCenterAtY(0);

        int landHeight = context.chunkGenerator().getHeightInGround(spawnXZPosition.getX(), spawnXZPosition.getZ(), Heightmap.Type.WORLD_SURFACE_WG, context.world());

        VerticalBlockSample columnOfBlocks = context.chunkGenerator().getColumnSample(spawnXZPosition.getX(), spawnXZPosition.getZ(), context.world());

        BlockState topBlock = columnOfBlocks.getState(landHeight);

        return topBlock.getFluidState().isEmpty() && landHeight > 10; //landHeight > 100;
    }

    private static boolean isOwner(String owner, String modId) {
        ModContainer modContainer = FabricLoader.getInstance().getModContainer(modId).get();
        Person author = modContainer.getMetadata().getAuthors().iterator().next();

        return author.getName().contains(owner);
    }

    private static String waystonesString() {
        if (FabricLoader.getInstance().isModLoaded("waystones") && isOwner("BlayTheNinth", "waystones") && TowersAdditionsMain.CONFIG.endTowerWaystones)
            return "_waystones";
        if (FabricLoader.getInstance().isModLoaded("waystones") && isOwner("LordDeatHunter", "waystones") && TowersAdditionsMain.CONFIG.endTowerWaystones) {
            return "_waystones_fabric";
        } else return "";
    }


    public static Optional<StructurePiecesGenerator<StructurePoolFeatureConfig>> createPiecesGenerator(StructureGeneratorFactory.Context<StructurePoolFeatureConfig> context) {

        BlockPos blockpos = context.chunkPos().getCenterAtY(0);

            StructurePoolFeatureConfig newConfig = new StructurePoolFeatureConfig(
                    () -> context.registryManager().get(Registry.STRUCTURE_POOL_KEY)
                            .get(new Identifier(TowersAdditionsMain.MODID, "end_tower/end_tower_start" + waystonesString())),
                    10);

            StructureGeneratorFactory.Context<StructurePoolFeatureConfig> newContext = new StructureGeneratorFactory.Context<>(
                    context.chunkGenerator(),
                    context.biomeSource(),
                    context.seed(),
                    context.chunkPos(),
                    newConfig,
                    context.world(),
                    context.validBiome(),
                    context.structureManager(),
                    context.registryManager()
            );

            Optional<StructurePiecesGenerator<StructurePoolFeatureConfig>> structurePiecesGenerator =
                    StructurePoolBasedGenerator.generate(
                            newContext,
                            PoolStructurePiece::new,
                            blockpos,
                            false,
                            true);

            return structurePiecesGenerator;

    }
}