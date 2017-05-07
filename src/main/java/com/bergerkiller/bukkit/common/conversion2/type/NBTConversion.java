package com.bergerkiller.bukkit.common.conversion2.type;

import java.util.List;
import java.util.Map;

import com.bergerkiller.bukkit.common.nbt.CommonTag;
import com.bergerkiller.bukkit.common.nbt.CommonTagCompound;
import com.bergerkiller.bukkit.common.nbt.CommonTagList;
import com.bergerkiller.mountiplex.conversion2.annotations.ConverterMethod;

public class NBTConversion {

    @ConverterMethod(input="net.minecraft.server.NBTBase")
    public static CommonTag toCommonTag(Object nmsNBTTagHandle) {
        return CommonTag.create(nmsNBTTagHandle);
    }

    @ConverterMethod(output="net.minecraft.server.NBTBase")
    public static Object toNBTTagHandle(CommonTag commonTag) {
        return commonTag.getHandle();
    }

    @ConverterMethod
    public static CommonTagCompound createCommonTag(Map<?, ?> mapData) {
        return (CommonTagCompound) CommonTag.createForData(mapData);
    }

    @ConverterMethod
    public static CommonTagList createCommonTag(List<?> listData) {
        return (CommonTagList) CommonTag.createForData(listData);
    }

    @ConverterMethod
    public static CommonTag createCommonTag(Byte data) {
        return CommonTag.createForData(data);
    }

    @ConverterMethod
    public static CommonTag createCommonTag(Short data) {
        return CommonTag.createForData(data);
    }

    @ConverterMethod
    public static CommonTag createCommonTag(Integer data) {
        return CommonTag.createForData(data);
    }

    @ConverterMethod
    public static CommonTag createCommonTag(Long data) {
        return CommonTag.createForData(data);
    }

    @ConverterMethod
    public static CommonTag createCommonTag(Float data) {
        return CommonTag.createForData(data);
    }

    @ConverterMethod
    public static CommonTag createCommonTag(Double data) {
        return CommonTag.createForData(data);
    }

    @ConverterMethod
    public static CommonTag createCommonTag(byte[] data) {
        return CommonTag.createForData(data);
    }

    @ConverterMethod
    public static CommonTag createCommonTag(int[] data) {
        return CommonTag.createForData(data);
    }

    @ConverterMethod
    public static CommonTag createCommonTag(String data) {
        return CommonTag.createForData(data);
    }
}
