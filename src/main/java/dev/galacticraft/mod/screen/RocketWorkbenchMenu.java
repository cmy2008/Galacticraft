/*
 * Copyright (c) 2019-2024 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.galacticraft.mod.screen;

import dev.galacticraft.api.registry.RocketRegistries;
import dev.galacticraft.api.rocket.part.RocketPart;
import dev.galacticraft.api.rocket.recipe.RocketPartRecipe;
import dev.galacticraft.mod.content.block.entity.RocketWorkbenchBlockEntity;
import dev.galacticraft.mod.content.block.entity.RocketWorkbenchBlockEntity.RecipeSelection;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RocketWorkbenchMenu extends AbstractContainerMenu {
    public static final int SPACING = 2;

    public static final int SCREEN_CENTER_BASE_X = 88;
    public static final int SCREEN_CENTER_BASE_Y = 144;

    public final RecipeSelection cone;
    public final RecipeSelection body;
    public final RecipeSelection fins;
    public final RecipeSelection booster;
    public final RecipeSelection bottom;
    public final RecipeSelection upgrade;

    public final RecipeCollection coneRecipes;
    public final RecipeCollection bodyRecipes;
    public final RecipeCollection finsRecipes;
    public final RecipeCollection boosterRecipes;
    public final RecipeCollection bottomRecipes;
    public final RecipeCollection upgradeRecipes;

    public int additionalHeight = 0;

    public final Inventory playerInventory;

    public RocketWorkbenchMenu(int syncId, RocketWorkbenchBlockEntity workbench, Inventory playerInventory) {
        super(GCMenuTypes.ROCKET_WORKBENCH, syncId);
        this.playerInventory = playerInventory;

        this.cone = workbench.cone;
        this.body = workbench.body;
        this.fins = workbench.fins;
        this.booster = workbench.booster;
        this.bottom = workbench.bottom;
        this.upgrade = workbench.upgrade;

        this.cone.inventory.setListener(this::onSizeChange);
        this.body.inventory.setListener(this::onSizeChange);
        this.fins.inventory.setListener(this::onSizeChange);
        this.booster.inventory.setListener(this::onSizeChange);
        this.bottom.inventory.setListener(this::onSizeChange);
        this.upgrade.inventory.setListener(this::onSizeChange);

        RegistryAccess registryAccess = playerInventory.player.level().registryAccess();
        this.coneRecipes = new RecipeCollection(registryAccess.registryOrThrow(RocketRegistries.ROCKET_CONE));
        this.bodyRecipes = new RecipeCollection(registryAccess.registryOrThrow(RocketRegistries.ROCKET_BODY));
        this.finsRecipes = new RecipeCollection(registryAccess.registryOrThrow(RocketRegistries.ROCKET_FIN));
        this.boosterRecipes = new RecipeCollection(registryAccess.registryOrThrow(RocketRegistries.ROCKET_BOOSTER));
        this.bottomRecipes = new RecipeCollection(registryAccess.registryOrThrow(RocketRegistries.ROCKET_BOTTOM));
        this.upgradeRecipes = new RecipeCollection(registryAccess.registryOrThrow(RocketRegistries.ROCKET_UPGRADE));

        StackedContents contents = new StackedContents();
        playerInventory.fillStackedContents(contents);
        this.coneRecipes.calculateCraftable(contents);
        this.bodyRecipes.calculateCraftable(contents);
        this.finsRecipes.calculateCraftable(contents);
        this.boosterRecipes.calculateCraftable(contents);
        this.bottomRecipes.calculateCraftable(contents);
        this.upgradeRecipes.calculateCraftable(contents);

        this.onSizeChange();
    }

    public RocketWorkbenchMenu(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(syncId, (RocketWorkbenchBlockEntity) playerInventory.player.level().getBlockEntity(buf.readBlockPos()), playerInventory);
    }

    public static int calculateAdditionalHeight(RocketPartRecipe<?, ?> cone, RocketPartRecipe<?, ?> body, RocketPartRecipe<?, ?> fins, RocketPartRecipe<?, ?> booster, RocketPartRecipe<?, ?> bottom, RocketPartRecipe<?, ?> upgrade) {
        int rocketHeight = Math.max(126, Math.max(Math.max(
                        (bottom != null ? bottom.height() + SPACING : 0) + (body != null ? body.height() + SPACING : 0) + (cone != null ? cone.height() + SPACING : 0),
                        (booster != null ? booster.height() + SPACING : 0) + (fins != null ? fins.height() + SPACING : 0)),
                35 + (upgrade != null ? SPACING : 0) + ((int)Math.ceil(1 / 2.0)) * 19));

        return rocketHeight - 126;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.playerInventory.stillValid(player);
    }

    public void onSizeChange() {
        this.slots.clear();

        RocketPartRecipe<?, ?> bottom = this.bottom.getRecipe();
        RocketPartRecipe<?, ?> body = this.body.getRecipe();
        RocketPartRecipe<?, ?> cone = this.cone.getRecipe();
        RocketPartRecipe<?, ?> fins = this.fins.getRecipe();
        RocketPartRecipe<?, ?> booster = this.booster.getRecipe();
        RocketPartRecipe<?, ?> upgrade = this.upgrade.getRecipe();
        this.additionalHeight = calculateAdditionalHeight(cone, body, fins, booster, bottom, upgrade);

        final int[] ext = {0, 0};
        final int[] ext1 = {0, 0};
        final int[] ext2 = {0, 0};

        if (bottom != null) {
            bottom.place((i, x, y, filter) -> {
                        ext[0] = Math.min(ext[0], x - SCREEN_CENTER_BASE_X);
                        ext[1] = Math.max(ext[1], x - SCREEN_CENTER_BASE_X + 18);
                        this.slots.add(new Slot(this.bottom.inventory, i, x, y));
                    },
                    SCREEN_CENTER_BASE_X,
                    SCREEN_CENTER_BASE_X,
                    SCREEN_CENTER_BASE_Y + this.additionalHeight);
        }

        if (body != null) {
            body.place((i, x, y, filter) -> {
                        ext1[0] = Math.min(ext1[0], x - SCREEN_CENTER_BASE_X);
                        ext1[1] = Math.max(ext1[1], x - SCREEN_CENTER_BASE_X + 18);
                        this.slots.add(new Slot(this.body.inventory, i, x, y));
                    },
                    SCREEN_CENTER_BASE_X,
                    SCREEN_CENTER_BASE_X,
                    SCREEN_CENTER_BASE_Y + this.additionalHeight
                            - (bottom != null ? bottom.height() + SPACING : 0));
        }

        if (cone != null) {
            cone.place((i, x, y, filter) -> {
                        ext2[0] = Math.min(ext2[0], x - SCREEN_CENTER_BASE_X);
                        ext2[1] = Math.max(ext2[1], x - SCREEN_CENTER_BASE_X + 18);
                        this.slots.add(new Slot(this.cone.inventory, i, x, y));
                    },
                    SCREEN_CENTER_BASE_X,
                    SCREEN_CENTER_BASE_X,
                    SCREEN_CENTER_BASE_Y + this.additionalHeight
                            - (bottom != null ? bottom.height() + SPACING : 0)
                            - (body != null ? body.height() + SPACING : 0)
            );
        }

        if (fins != null) {
            if (fins.height() > (bottom != null ? bottom.height() : 0)) {
                ext[0] = Math.min(ext[0], ext1[0]);
                ext[1] = Math.max(ext[1], ext1[1]);
            }
            if (fins.height() > (bottom != null ? bottom.height() : 0) + (body != null ? body.height() : 0)) {
                ext[0] = Math.min(ext[0], ext2[0]);
                ext[1] = Math.max(ext[1], ext2[1]);
            }
            fins.place((i, x, y, filter) -> this.slots.add(new Slot(this.fins.inventory, i, x, y)),
                    SCREEN_CENTER_BASE_X + ext[0] - SPACING,
                    SCREEN_CENTER_BASE_X + ext[1] + SPACING,
                    SCREEN_CENTER_BASE_Y + this.additionalHeight
            );
        }

        if (booster != null) {
            if (booster.height() + (fins != null ? fins.height() : 0) > (bottom != null ? bottom.height() : 0)) {
                ext[0] = Math.min(ext[0], ext1[0]);
                ext[1] = Math.max(ext[1], ext1[1]);
            }
            if (booster.height() + (fins != null ? fins.height() : 0) > (bottom != null ? bottom.height() : 0) + (body != null ? body.height() : 0)) {
                ext[0] = Math.min(ext[0], ext2[0]);
                ext[1] = Math.max(ext[1], ext2[1]);
            }

            booster.place((i, x, y, filter) -> this.slots.add(new Slot(this.booster.inventory, i, x, y)),
                    SCREEN_CENTER_BASE_X + ext[0] - SPACING,
                    SCREEN_CENTER_BASE_X + ext[1] + SPACING,
                    SCREEN_CENTER_BASE_Y + this.additionalHeight
                            - (fins != null ? fins.height() + SPACING : 0)
            );

        }

        if (upgrade != null) {
            upgrade.place((i, x, y, filter) -> this.slots.add(new Slot(this.upgrade.inventory, i, x, y)),
                    11, //FIXME
                    11,
                    62 + this.additionalHeight //FIXME
            );
        }

        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(this.playerInventory, column + row * 9 + 9, column * 18 + 8, row * 18 + 167 + this.additionalHeight));
            }
        }

        for (int column = 0; column < 9; ++column) {
            this.addSlot(new Slot(this.playerInventory, column, column * 18 + 8, 225 + this.additionalHeight));
        }
    }

    public static class RecipeCollection {
        private final Registry<? extends RocketPart<?, ?>> recipes;
        private final List<Holder.Reference<? extends RocketPart<?, ?>>> uncraftable = new ArrayList<>();
        private final List<Holder.Reference<? extends RocketPart<?, ?>>> craftable = new ArrayList<>();

        public RecipeCollection(Registry<? extends RocketPart<?, ?>> recipes) {
            this.recipes = recipes;
        }

        public List<Holder.Reference<? extends RocketPart<?, ?>>> getCraftable() {
            return craftable;
        }

        public List<Holder.Reference<? extends RocketPart<?, ?>>> getUncraftable() {
            return uncraftable;
        }

        public void calculateCraftable(StackedContents contents) {
            this.craftable.clear();
            this.uncraftable.clear();

            this.recipes.holders().forEach(holder -> {
                RocketPartRecipe<?, ?> recipe = holder.value().getRecipe();
                if (recipe != null) {
                    if (contents.canCraft(recipe, null)) {
                        this.craftable.add(holder);
                    } else {
                        this.uncraftable.add(holder);
                    }
                }
            });
        }
    }
}