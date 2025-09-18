package net.milkev.milkevsmultiblocklibrary.common;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name="milkevsmultiblocklibrary")
public class ModConfig implements ConfigData {
    @Comment("If the example multiblock should be enabled. This multiblock serves no actual purpose and is purely for demonstration. Default: false")
    Boolean ExampleMultiblock = false;
}
