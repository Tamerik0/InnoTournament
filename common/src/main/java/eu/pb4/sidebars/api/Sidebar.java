package eu.pb4.sidebars.api;

import eu.pb4.sidebars.api.lines.ImmutableSidebarLine;
import eu.pb4.sidebars.api.lines.LineBuilder;
import eu.pb4.sidebars.api.lines.SidebarLine;
import eu.pb4.sidebars.api.lines.SimpleSidebarLine;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * Basic sidebar with all of basic functionality
 */
@SuppressWarnings({ "unused" })
public class Sidebar implements SidebarInterface {
    protected List<SidebarLine> elements = new ArrayList<>();
    protected Set<ServerGamePacketListenerImpl> players = new HashSet<>();
    protected Priority priority;
    protected Component title;
    protected boolean isDirty = false;
    protected int updateRate = 1;
    @Nullable
    protected NumberFormat defaultNumberFormat = null;

    protected boolean isActive = false;

    public Sidebar(Priority priority) {
        this.priority = priority;
        this.title = Component.empty();
    }

    public Sidebar(Component title, Priority priority) {
        this.priority = priority;
        this.title = title;
    }

    public Priority getPriority() {
        return this.priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
        if (this.isActive) {
            for (ServerGamePacketListenerImpl player : this.players) {
                SidebarUtils.updatePriorities(player, this);
            }
        }
    }

    public @Nullable NumberFormat getDefaultNumberFormat() {
        return defaultNumberFormat;
    }

    public void setDefaultNumberFormat(@Nullable NumberFormat defaultNumberFormat) {
        this.defaultNumberFormat = defaultNumberFormat;
    }

    public int getUpdateRate() {
        return this.updateRate;
    }

    public void setUpdateRate(int updateRate) {
        this.updateRate = Math.max(updateRate, 1);
    }

    public Component getTitle() {
        return this.title;
    }

    public Component getTitleFor(ServerGamePacketListenerImpl handler) {
        return this.getTitle();
    }

    public void setTitle(Component title) {
        this.title = title;
    }
    public void setLine(int value, Component text, @Nullable NumberFormat format) {
        setLine(new SimpleSidebarLine(value, text, format, this));
    }

    public void setLine(int value, Component text) {
        setLine(new SimpleSidebarLine(value, text, this.defaultNumberFormat, this));
    }

    public void setLine(SidebarLine line) {
        for (SidebarLine cLine : this.elements) {
            if (line.getValue() == cLine.getValue()) {
                line.setSidebar(this);
                this.elements.set(this.elements.indexOf(cLine), line);
                return;
            }
        }

        this.elements.add(line);
        this.isDirty = true;
    }

    public void addLines(SidebarLine... lines) {
        for (SidebarLine line : lines) {
            line.setSidebar(this);
            this.elements.add(line);
        }

        this.isDirty = true;
    }


    public void addLines(Component... texts) {
        if (this.elements.isEmpty()) {
            int lastLine = texts.length;
            for (Component t : texts) {
                this.elements.add(new SimpleSidebarLine(--lastLine, t, this.defaultNumberFormat, this));
            }
        } else {
            this.sortIfDirty();
            int lastLine = this.elements.get(this.elements.size() - 1).getValue();
            for (Component t : texts) {
                this.elements.add(new SimpleSidebarLine(--lastLine, t, this.defaultNumberFormat, this));
            }
        }
    }

    public void removeLine(SidebarLine line) {
        this.elements.remove(line);
        line.setSidebar(null);
    }

    public void removeLine(int value) {
        for (SidebarLine line : new ArrayList<>(this.elements)) {
            if (line.getValue() == value) {
                this.elements.remove(line);
                line.setSidebar(null);
            }
        }
    }

    @Nullable
    public SidebarLine getLine(int value) {
        for (SidebarLine line : this.elements) {
            if (line.getValue() == value) {
                return line;
            }
        }

        return null;
    }

    public void replaceLines(Component... texts) {
        this.clearLines();
        this.addLines(texts);
    }

    public void replaceLines(SidebarLine... lines) {
        this.clearLines();
        this.addLines(lines);
    }

    public void replaceLines(LineBuilder builder) {
        this.replaceLines(builder.getLines().toArray(new SidebarLine[0]));
    }

    public void clearLines() {
        for (SidebarLine line : this.elements) {
            line.setSidebar(null);
        }

        this.elements.clear();
    }

    public void set(Consumer<LineBuilder> consumer) {
        LineBuilder builder = new LineBuilder(this.defaultNumberFormat);
        consumer.accept(builder);
        this.replaceLines(builder);
    }

    public boolean isDirty() {
        return this.isDirty;
    }

    public void markDirty() {
        this.isDirty = true;
    }

    public List<SidebarLine> getLinesFor(ServerGamePacketListenerImpl handler) {
        this.sortIfDirty();

        return this.elements.subList(0, Math.min(14, this.elements.size()));
    }

    protected void sortIfDirty() {
        if (this.isDirty) {
            this.isDirty = false;
            this.elements.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        }
    }

    public void show() {
        if (!isActive) {
            this.isActive = true;
            for (ServerGamePacketListenerImpl player : this.players) {
                SidebarUtils.addSidebar(player, this);
            }
        }
    }

    public void hide() {
        if (this.isActive) {
            this.isActive = false;
            for (ServerGamePacketListenerImpl player : this.players) {
                SidebarUtils.removeSidebar(player, this);
            }
        }
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void addPlayer(ServerGamePacketListenerImpl handler) {
        if (this.players.add(handler)) {
            if (isActive) {
                SidebarUtils.addSidebar(handler, this);
            }
        }
    }

    public void removePlayer(ServerGamePacketListenerImpl handler) {
        if (this.players.remove(handler)) {
            if (isActive) {
                if (!handler.player.hasDisconnected()) {
                    SidebarUtils.removeSidebar(handler, this);
                }
            }
        }
    }

    public void addPlayer(ServerPlayer player) {
        this.addPlayer(player.connection);
    }

    public void removePlayer(ServerPlayer player) {
        this.removePlayer(player.connection);
    }

    public Set<ServerGamePacketListener> getPlayerHandlerSet() {
        return Collections.unmodifiableSet(this.players);
    }

    @Override
    public void disconnected(ServerGamePacketListenerImpl handler) {
        this.removePlayer(handler);
    }

    @Override
    public SidebarData getDataFor(ServerGamePacketListenerImpl handler) {
        return new SidebarData(getTitleFor(handler), getLinesFor(handler).stream().map(line->new ImmutableSidebarLine(line.getValue(),line.getText(handler), line.getNumberFormat(handler))).toList());
    }

    public enum Priority {
        LOWEST(0),
        LOW(1),
        MEDIUM(2),
        HIGH(3),
        OVERRIDE(4);

        private final int value;

        Priority(int value) {
            this.value = value;
        }

        public boolean isLowerThan(Priority priority) {
            return this.value <= priority.value;
        }
    }
}
