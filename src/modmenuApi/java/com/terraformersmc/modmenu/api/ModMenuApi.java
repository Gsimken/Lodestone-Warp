package com.terraformersmc.modmenu.api;

import net.minecraft.client.gui.screens.Screen;

public interface ModMenuApi {
	default ConfigScreenFactory<? extends Screen> getModConfigScreenFactory() {
		return parent -> null;
	}
}
