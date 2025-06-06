package eu.pb4.sidebars.mixin;


import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundSetDisplayObjectivePacket.class)
public interface ClientboundSetDisplayObjectivePacketAccessor {
    @Mutable
    @Accessor("objectiveName")
    void setName(String name);
}
