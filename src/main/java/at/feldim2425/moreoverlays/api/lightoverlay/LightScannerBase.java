package at.feldim2425.moreoverlays.api.lightoverlay;

import at.feldim2425.moreoverlays.config.Config;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public abstract class LightScannerBase implements ILightScanner {

    protected List<Pair<BlockPos, Byte>> overlayCache = new ArrayList<>();

    @Override
    public void update(PlayerEntity player) {
        int px = (int) Math.floor(player.getPosX());
        int py = (int) Math.floor(player.getPosY());
        int pz = (int) Math.floor(player.getPosZ());

        int y1 = py - Config.light_DownRange.get();
        int y2 = py + Config.light_UpRange.get();

        overlayCache.clear();
        for (int xo = -Config.light_HRange.get(); xo <= Config.light_HRange.get(); xo++) {
            for (int zo = -Config.light_HRange.get(); zo <= Config.light_HRange.get(); zo++) {
                BlockPos pos1 = new BlockPos(px + xo, py, pz + zo);
                if (!shouldCheck(pos1, player.world)) {
                    continue;
                }
                for (int y = y1; y <= y2; y++) {
                    BlockPos pos = new BlockPos(px + xo, y, pz + zo);
                    byte mode = getSpawnModeAt(pos, player.world);
                    if (mode != 0) {
                        overlayCache.add(Pair.of(pos, mode));
                    }
                }
            }
        }
    }

    @Override
    public void clear() {
        overlayCache.clear();
    }

    @Override
    public List<Pair<BlockPos, Byte>> getLightModes() {
        return overlayCache;
    }

    public boolean shouldCheck(BlockPos pos, World world) {
        if(world.isRemote){
            return true;
        }
        if (Config.light_IgnoreSpawnList.get()) {
            return true;
        }
        Biome biome = world.getBiome(pos);
        return biome.getMobSpawnInfo().getCreatureSpawnProbability() > 0 && !biome.getMobSpawnInfo().getSpawners(EntityClassification.MONSTER).isEmpty();
    }

    public abstract byte getSpawnModeAt(BlockPos pos, World world);
}
