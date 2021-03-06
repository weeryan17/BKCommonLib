package com.bergerkiller.generated.net.minecraft.server;

import com.bergerkiller.mountiplex.reflection.util.StaticInitHelper;
import com.bergerkiller.mountiplex.reflection.declarations.Template;

/**
 * Instance wrapper handle for type <b>net.minecraft.server.IChatBaseComponent</b>.
 * To access members without creating a handle type, use the static {@link #T} member.
 * New handles can be created from raw instances using {@link #createHandle(Object)}.
 */
public abstract class IChatBaseComponentHandle extends Template.Handle {
    /** @See {@link IChatBaseComponentClass} */
    public static final IChatBaseComponentClass T = new IChatBaseComponentClass();
    static final StaticInitHelper _init_helper = new StaticInitHelper(IChatBaseComponentHandle.class, "net.minecraft.server.IChatBaseComponent", com.bergerkiller.bukkit.common.Common.TEMPLATE_RESOLVER);

    /* ============================================================================== */

    public static IChatBaseComponentHandle createHandle(Object handleInstance) {
        return T.createHandle(handleInstance);
    }

    /* ============================================================================== */

    /**
     * Stores class members for <b>net.minecraft.server.IChatBaseComponent</b>.
     * Methods, fields, and constructors can be used without using Handle Objects.
     */
    public static final class IChatBaseComponentClass extends Template.Class<IChatBaseComponentHandle> {
    }


    /**
     * Instance wrapper handle for type <b>net.minecraft.server.IChatBaseComponent.ChatSerializer</b>.
     * To access members without creating a handle type, use the static {@link #T} member.
     * New handles can be created from raw instances using {@link #createHandle(Object)}.
     */
    public abstract static class ChatSerializerHandle extends Template.Handle {
        /** @See {@link ChatSerializerClass} */
        public static final ChatSerializerClass T = new ChatSerializerClass();
        static final StaticInitHelper _init_helper = new StaticInitHelper(ChatSerializerHandle.class, "net.minecraft.server.IChatBaseComponent.ChatSerializer", com.bergerkiller.bukkit.common.Common.TEMPLATE_RESOLVER);

        /* ============================================================================== */

        public static ChatSerializerHandle createHandle(Object handleInstance) {
            return T.createHandle(handleInstance);
        }

        /* ============================================================================== */

        public static String chatComponentToJson(IChatBaseComponentHandle chatComponent) {
            return T.chatComponentToJson.invoke(chatComponent);
        }

        public static IChatBaseComponentHandle jsonToChatComponent(String jsonString) {
            return T.jsonToChatComponent.invoke(jsonString);
        }

        /**
         * Stores class members for <b>net.minecraft.server.IChatBaseComponent.ChatSerializer</b>.
         * Methods, fields, and constructors can be used without using Handle Objects.
         */
        public static final class ChatSerializerClass extends Template.Class<ChatSerializerHandle> {
            public final Template.StaticMethod.Converted<String> chatComponentToJson = new Template.StaticMethod.Converted<String>();
            public final Template.StaticMethod.Converted<IChatBaseComponentHandle> jsonToChatComponent = new Template.StaticMethod.Converted<IChatBaseComponentHandle>();

        }

    }

}

