package com.bergerkiller.bukkit.common.wrappers;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;

import com.bergerkiller.bukkit.common.bases.IntVector3;
import com.bergerkiller.bukkit.common.conversion.type.HandleConversion;
import com.bergerkiller.bukkit.common.internal.CommonCapabilities;
import com.bergerkiller.bukkit.common.internal.CommonLegacyMaterials;
import com.bergerkiller.bukkit.common.internal.blocks.BlockRenderProvider;
import com.bergerkiller.bukkit.common.internal.legacy.IBlockDataToMaterialData;
import com.bergerkiller.bukkit.common.internal.legacy.MaterialDataToIBlockData;
import com.bergerkiller.bukkit.common.utils.WorldUtil;
import com.bergerkiller.generated.net.minecraft.server.AxisAlignedBBHandle;
import com.bergerkiller.generated.net.minecraft.server.BlockHandle;
import com.bergerkiller.generated.net.minecraft.server.BlockPositionHandle;
import com.bergerkiller.generated.net.minecraft.server.ExplosionHandle;
import com.bergerkiller.generated.net.minecraft.server.IBlockDataHandle;
import com.bergerkiller.generated.net.minecraft.server.IBlockStateHandle;
import com.bergerkiller.generated.net.minecraft.server.ItemStackHandle;
import com.bergerkiller.generated.net.minecraft.server.MinecraftKeyHandle;
import com.bergerkiller.generated.net.minecraft.server.RegistryBlockIDHandle;
import com.bergerkiller.generated.net.minecraft.server.RegistryIDHandle;
import com.bergerkiller.generated.net.minecraft.server.RegistryMaterialsHandle;
import com.bergerkiller.generated.net.minecraft.server.WorldHandle;
import com.bergerkiller.generated.org.bukkit.craftbukkit.util.CraftMagicNumbersHandle;

@SuppressWarnings("deprecation")
public class BlockDataImpl extends BlockData {
    private BlockHandle block;
    private IBlockDataHandle data;
    private MaterialData materialData;
    private Material type;
    private boolean hasRenderOptions;

    public static final int ID_BITS = 8;
    public static final int DATA_BITS = 4;

    public static final int REGISTRY_SIZE = (1 << 16); // 65536
    public static final int REGISTRY_MASK = (REGISTRY_SIZE - 1);

    public static final BlockDataConstant AIR;
    public static final EnumMap<Material, BlockDataConstant> BY_MATERIAL = new EnumMap<Material, BlockDataConstant>(Material.class);
    public static final BlockDataConstant[] BY_ID_AND_DATA = new BlockDataConstant[REGISTRY_SIZE];
    public static final Map<Object, BlockDataConstant> BY_BLOCK = new IdentityHashMap<Object, BlockDataConstant>();
    public static final IdentityHashMap<Object, BlockDataConstant> BY_BLOCK_DATA = new IdentityHashMap<Object, BlockDataConstant>();

    // Legacy: array of all possible Material values with all possible legacy data values
    // Index into it by taking data x 1024 | mat.ordinal()
    public static final int BY_LEGACY_MAT_DATA_SHIFT = 11; // (1<<11 == 2048)
    public static final BlockDataConstant[] BY_LEGACY_MAT_DATA = new BlockDataConstant[16 << BY_LEGACY_MAT_DATA_SHIFT];

    static {
        // Retrieve
        Iterable<?> REGISTRY = BlockHandle.getRegistry();

        // Fill BY_MATERIAL and BY_BLOCK mapping with all existing Block types
        AIR = new BlockDataConstant(BlockHandle.createHandle(CraftMagicNumbersHandle.getBlockFromMaterial(Material.AIR)));
        for (Object rawBlock : REGISTRY) {
            BlockHandle block = BlockHandle.createHandle(rawBlock);
            Material material = CraftMagicNumbersHandle.getMaterialFromBlock(rawBlock);
            BlockDataConstant blockConst = (material == Material.AIR) ? AIR : new BlockDataConstant(block);
            BY_BLOCK.put(rawBlock, blockConst);
            BY_MATERIAL.put(material, blockConst);
        }

        // Cache a mapping of all possible IBlockData instances
        Arrays.fill(BY_ID_AND_DATA, AIR);
        for (Object rawIBlockData : BlockHandle.REGISTRY_ID) {
            IBlockDataHandle blockData = IBlockDataHandle.createHandle(rawIBlockData);
            BlockDataConstant block_const = BY_BLOCK.get(blockData.getBlock().getRaw());
            if (block_const.getData() != rawIBlockData) {
                block_const = new BlockDataConstant(blockData);
            }
            BY_BLOCK_DATA.put(rawIBlockData, block_const);
            BY_ID_AND_DATA[block_const.getCombinedId_1_8_8()] = block_const;
        }
        BY_BLOCK_DATA.put(null, AIR);

        // Sanity check
        if (CommonLegacyMaterials.getAllMaterials().length >= (1<<BY_LEGACY_MAT_DATA_SHIFT)) {
            throw new IllegalStateException("BY_LEGACY_MAT_DATA_SHIFT is too low, can't store " +
                    CommonLegacyMaterials.getAllMaterials().length + " materials");
        }

        // Check for any missing Material enum values - store those also in the BY_MATERIAL mapping
        // This mainly applies to the legacy 1.13 enum values
        // Also store all possible values of BY_LEGACY_MAT_DATA
        Arrays.fill(BY_LEGACY_MAT_DATA, AIR);
        for (Material mat : CommonLegacyMaterials.getAllMaterials()) {
            if (!mat.isBlock()) {
                BY_MATERIAL.put(mat, AIR);
                continue;
            }

            BlockDataConstant blockConst = BY_MATERIAL.get(mat);
            if (blockConst == null) {
                if (CommonCapabilities.MATERIAL_ENUM_CHANGES && CommonLegacyMaterials.isLegacy(mat)) {
                    // Legacy Material -> IBlockData logic
                    MaterialData materialData = IBlockDataToMaterialData.createMaterialData(mat);
                    blockConst = findConstant(MaterialDataToIBlockData.getIBlockData(materialData));
                } else {
                    // Normal Material -> Block -> IBlockData logic
                    Object rawBlock = CraftMagicNumbersHandle.getBlockFromMaterial(mat);
                    blockConst = BY_BLOCK.get(rawBlock);
                    if (blockConst == null) {
                        blockConst = new BlockDataConstant(BlockHandle.createHandle(rawBlock));
                        BY_BLOCK.put(rawBlock, blockConst);
                    }
                }
                BY_MATERIAL.put(mat, blockConst);
            }

            // Only store by MaterialData information for Legacy Materials
            if (!CommonLegacyMaterials.isLegacy(mat)) {
                continue;
            }

            MaterialData materialdata = new MaterialData(mat);
            for (int data = 0; data < 16; data++) {
                // Find IBlockData from Material + Data and cache it if needed
                materialdata.setData((byte) data);
                IBlockDataHandle blockData = MaterialDataToIBlockData.getIBlockData(materialdata);
                BlockDataConstant dataBlockConst = blockConst;
                if (blockData.getRaw() != blockConst.getData()) {
                    dataBlockConst = findConstant(blockData);
                }

                // Store in lookup table
                int index = CommonLegacyMaterials.getOrdinal(mat);
                index |= (data << BY_LEGACY_MAT_DATA_SHIFT);
                BY_LEGACY_MAT_DATA[index] = dataBlockConst;
            }
        }

        // Dangerous and unpredictable: fill BY_LEGACY_MAT_DATA with Material taken using toLegacy
        if (CommonCapabilities.MATERIAL_ENUM_CHANGES) {
            for (Material mat : CommonLegacyMaterials.getAllMaterials()) {
                // Skip legacy materials
                if (CommonLegacyMaterials.isLegacy(mat)) {
                    continue;
                }

                // Find legacy Material, then copy all 16 data values from Legacy to New Material
                Material legacyType = BY_MATERIAL.get(mat).getLegacyType();
                for (int data = 0; data < 16; data++) {
                    int index_a = CommonLegacyMaterials.getOrdinal(mat);
                    index_a |= (data << BY_LEGACY_MAT_DATA_SHIFT);
                    int index_b = CommonLegacyMaterials.getOrdinal(legacyType);
                    index_b |= (data << BY_LEGACY_MAT_DATA_SHIFT);
                    BY_LEGACY_MAT_DATA[index_a] = BY_LEGACY_MAT_DATA[index_b];
                }
            }
        }
    }

    private static BlockDataConstant findConstant(IBlockDataHandle iblockdata) {
        BlockDataConstant dataBlockConst = BY_BLOCK_DATA.get(iblockdata.getRaw());
        if (dataBlockConst == null) {
            dataBlockConst = new BlockDataConstant(iblockdata);
            BY_BLOCK_DATA.put(iblockdata.getRaw(), dataBlockConst);
        }
        return dataBlockConst;
    }

    /**
     * Used to convert a BlockData value into an immutable version.
     * This protects statically registered block objects from being mutated by plugins.
     */
    public static class BlockDataConstant extends BlockDataImpl {

        public BlockDataConstant(BlockHandle block) {
            super(block);
        }

        public BlockDataConstant(IBlockDataHandle blockData) {
            super(blockData);
        }

        @Override
        public void loadBlock(Object block) {
            throw new UnsupportedOperationException("Immutable Block Data objects can not be changed");
        }

        @Override
        public void loadBlockData(Object iBlockData) {
            throw new UnsupportedOperationException("Immutable Block Data objects can not be changed");
        }

        @Override
        public void loadMaterialData(MaterialData materialdata) {
            throw new UnsupportedOperationException("Immutable Block Data objects can not be changed");
        }
    }

    public BlockDataImpl() {
        this(AIR.getBlock());
    }

    public BlockDataImpl(IBlockDataHandle data) {
        this(data.getBlock(), data);
    }

    public BlockDataImpl(BlockHandle block) {
        this(block, block.getBlockData());
    }

    public BlockDataImpl(BlockHandle block, IBlockDataHandle data) {
        this.block = block;
        this.data = data;
        this.refreshBlock();
    }

    @Override
    public void loadBlock(Object block) {
        this.block = BlockHandle.createHandle(block);
        this.data = this.block.getBlockData();
        this.refreshBlock();
    }

    @Override
    public void loadBlockData(Object iBlockData) {
        this.data = IBlockDataHandle.createHandle(iBlockData);
        this.block = this.data.getBlock();
        this.refreshBlock();
    }

    @Override
    public void loadMaterialData(MaterialData materialdata) {
        this.data = MaterialDataToIBlockData.getIBlockData(materialdata);
        this.block = this.data.getBlock();
        this.refreshBlock();
    }

    private final void refreshBlock() {
        this.hasRenderOptions = true;
        this.type = CraftMagicNumbersHandle.getMaterialFromBlock(this.block.getRaw());
        this.materialData = IBlockDataToMaterialData.getMaterialData(this.data);
    }

    @Override
    public final BlockHandle getBlock() {
        return this.block;
    }

    @Override
    public final Object getData() {
        return this.data.getRaw();
    }

    @Override
    public final int getRawData() {
        return this.materialData.getData();
    }

    @Override
    public final org.bukkit.Material getType() {
        return this.type;
    }

    @Override
    public final org.bukkit.Material getLegacyType() {
        return this.materialData.getItemType();
    }

    @Override
    public final MaterialData getMaterialData() {
        return this.materialData;
    }

    @Override
    public final int getCombinedId() {
        return BlockHandle.getCombinedId(this.data);
    }

    @Override
    public final int getCombinedId_1_8_8() {
        if (RegistryBlockIDHandle.T.isAssignableFrom(BlockHandle.REGISTRY_ID)) {
            // >= MC 1.10.2
            return RegistryBlockIDHandle.T.getId.invoke(BlockHandle.REGISTRY_ID, this.data.getRaw());
        } else {
            // <= MC 1.8.8
            return RegistryIDHandle.T.getId.invoke(BlockHandle.REGISTRY_ID, this.data.getRaw());
        }
    }

    @Override
    public String getBlockName() {
        Object minecraftKey = RegistryMaterialsHandle.T.getKey.invoke(BlockHandle.getRegistry(), this.getBlockRaw());
        return MinecraftKeyHandle.T.name.get(minecraftKey);
    }

    @Override
    public BlockRenderOptions getRenderOptions(World world, int x, int y, int z) {
        if (!this.hasRenderOptions) {
            return new BlockRenderOptions(this, "");
        }

        Object stateData;
        if (world == null) {
            //TODO: We should call updateState() with an IBlockAccess that returns all Air.
            // Right now, it will return the options of the last-modified block
            stateData = this.data.getRaw();
        } else {
            // This refreshes the state (cached) to reflect a particular Block
            stateData = BlockHandle.T.updateState.raw.invoke(
                    this.block.getRaw(),
                    this.data.getRaw(),
                    HandleConversion.toWorldHandle(world),
                    BlockPositionHandle.T.constr_x_y_z.raw.newInstance(x, y, z)
            );
        }

        // Not sure if this can happen; but we handle it!
        if (stateData == null) {
            return new BlockRenderOptions(this, new HashMap<String, String>(0));
        }

        // Serialize all tokens into String key-value pairs
        Map<IBlockStateHandle, Comparable<?>> states = IBlockDataHandle.T.getStates.invoke(stateData);
        Map<String, String> statesStr = new HashMap<String, String>(states.size());
        for (Map.Entry<IBlockStateHandle, Comparable<?>> state : states.entrySet()) {
            String key = state.getKey().getKeyToken();
            String value = state.getKey().getValueToken(state.getValue());
            statesStr.put(key, value);
        }
        BlockRenderOptions options = new BlockRenderOptions(this, statesStr);

        // Add additional options not provided by the server
        // This handles the display parameters for blocks like Water and Lava
        BlockRenderProvider renderProvider = BlockRenderProvider.get(this);
        if (renderProvider != null) {
            renderProvider.addOptions(options, world, x, y, z);
        }

        // When no options are being used, do not check for them again in the future
        // This offers performance benefits
        if (options.isEmpty()) {
            this.hasRenderOptions = false;
        }

        return options;
    }

    /* ====================================================================== */
    /* ========================= Block Properties =========================== */
    /* ====================================================================== */

    @Override
    public final ResourceKey getStepSound() {
        return ResourceKey.fromMinecraftKey(block.getSoundType().getStepSound().getName());
    }

    @Override
    public final ResourceKey getPlaceSound() {
        return ResourceKey.fromMinecraftKey(block.getSoundType().getPlaceSound().getName());
    }

    @Override
    public final ResourceKey getBreakSound() {
        return ResourceKey.fromMinecraftKey(block.getSoundType().getBreakSound().getName());
    }

    @Override
    public final int getOpacity(World world, int x, int y, int z) {
        return this.block.getOpacity(this.data, world, x, y, z);
    }

    @Override
    public final int getEmission() {
        return this.block.getEmission(this.data);
    }

    @Override
    public final boolean isOccluding() {
        return this.block.isOccluding(this.data);
    }

    @Override
    public final boolean isSuffocating() {
        return this.block.isOccluding(this.data);
    }

    @Override
    public final boolean isPowerSource() {
        return this.block.isPowerSource(this.data);
    }

    @Override
    public final AxisAlignedBBHandle getBoundingBox(Block block) {
        return this.block.getBoundingBox(this.data, WorldHandle.fromBukkit(block.getWorld()), new IntVector3(block));
    }

    @Override
    public final float getDamageResilience() {
        return this.block.getDamageResillience();
    }

    @Override
    public final boolean canSupportTop() {
        return this.block.canSupportTop(this.data);
    }

    @Override
    public final void dropNaturally(org.bukkit.World world, int x, int y, int z, float yield, int chance) {
        this.block.dropNaturally(this.data, world, new IntVector3(x, y, z), yield, chance);
    }

    @Override
    public final void ignite(org.bukkit.World world, int x, int y, int z) {
        ExplosionHandle ex = ExplosionHandle.createNew(world, null, x, y, z, 4.0f, true, true);
        this.block.ignite(world, new IntVector3(x, y, z), ex);
    }

    @Override
    public void destroy(org.bukkit.World world, int x, int y, int z, float yield) {
        dropNaturally(world, x, y, z, yield);
        WorldUtil.setBlockData(world, x, y, z, AIR);
    }

    @Override
    public void stepOn(org.bukkit.World world, IntVector3 blockPosition, org.bukkit.entity.Entity entity) {
        this.block.stepOn(world, blockPosition, entity);
    }

    @Override
    public BlockData setState(String key, Object value) {
        IBlockDataHandle updated_data = this.data.set(key, value);
        BlockData data = BlockDataImpl.BY_BLOCK_DATA.get(updated_data.getRaw());
        if (data != null) {
            return data;
        }

        // Store in map (should only rarely happen)
        BlockDataConstant c = new BlockDataImpl.BlockDataConstant(updated_data);
        BY_BLOCK_DATA.put(updated_data.getRaw(), c);
        return c;
    }

    @Override
    public <T> T getState(String key, Class<T> type) {
        return this.data.get(key, type);
    }

    @Override
    public org.bukkit.inventory.ItemStack createItem(int amount) {
        return ItemStackHandle.fromBlockData(this.data, amount).toBukkit();
    }
}
