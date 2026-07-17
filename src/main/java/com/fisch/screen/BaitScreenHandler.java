package com.fisch.screen;

import com.fisch.item.Bait;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BaitScreenHandler
        extends AbstractContainerMenu {

    private final Player player;

    private  final RodBaitContainer baitContainer;

    public BaitScreenHandler(
            int containerId,
            Inventory inventory
    ) {

        super(
                ModScreenHandlers.BAIT_MENU,
                containerId
        );

        this.player =
                inventory.player;

        this.baitContainer =
                new RodBaitContainer(this);

        /*
         * Единственный слот приманки
         */
        this.addSlot(
                new BaitSlot(
                        baitContainer,
                        80,
                        35
                )
        );

        addPlayerInventory(inventory);
    }

    public ItemStack getRod() {

        return player.getMainHandItem();
    }

    private void addPlayerInventory(
            Inventory inventory
    ) {

        /*
         * Основной инвентарь
         */
        for (int row = 0; row < 3; row++) {

            for (int column = 0; column < 9; column++) {

                this.addSlot(
                        new Slot(
                                inventory,
                                column + row * 9 + 9,
                                8 + column * 18,
                                84 + row * 18
                        )
                );
            }
        }

        /*
         * Хотбар
         */
        for (int column = 0; column < 9; column++) {

            this.addSlot(
                    new Slot(
                            inventory,
                            column,
                            8 + column * 18,
                            142
                    )
            );
        }
    }

    @Override
    public boolean stillValid(
            Player player
    ) {

        /*
         * Shift может быть отпущен.
         *
         * Игрок может убрать удочку.
         *
         * Меню всё равно не закроется.
         *
         * Закрытие — только ESC или кнопка закрытия.
         */
        return true;
    }

    @Override
    public ItemStack quickMoveStack(
            Player player,
            int index
    ) {

        return ItemStack.EMPTY;
    }

    public  ItemStack getBait() {
        return baitContainer.getItem(0);
    }

    public boolean hasBait() {
        return !getBait().isEmpty()
                && getBait().getItem() instanceof Bait;
    }
}