package eu.pb4.sidebars.api.lines;

import eu.pb4.sidebars.api.Sidebar;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractSidebarLine implements SidebarLine {
    @Getter
    protected int value;
    @Setter
    protected Sidebar sidebar;

	public boolean setValue(int value) {
        this.value = value;
        if (this.sidebar != null) {
            this.sidebar.markDirty();
        }
        return true;
    }

}
