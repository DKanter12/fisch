package com.fisch.menu;

import com.fisch.command.ModCommands;
import com.fisch.registry.ModMenuTypes;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FishMerchantMenu extends AbstractContainerMenu {

    private final Container container;
    private final Villager merchant;


    /*
     * ============================================================
     * CLIENT CONSTRUCTOR
     * ============================================================
     *
     * Нужен для обычного MenuType:
     *
     * new MenuType<>(FishMerchantMenu::new)
     *
     */

    public FishMerchantMenu(
            int syncId,
            Inventory playerInventory
    ) {
        this(
                syncId,
                playerInventory,
                new SimpleContainer(27),
                null
        );
    }


    /*
     * ============================================================
     * SERVER CONSTRUCTOR
     * ============================================================
     *
     * Используется при открытии меню у жителя.
     *
     */

    public FishMerchantMenu(
            int syncId,
            Inventory playerInventory,
            Container container,
            Villager merchant
    ) {
        super(
                ModMenuTypes.FISH_MERCHANT_MENU,
                syncId
        );


        checkContainerSize(
                container,
                27
        );


        this.container = container;
        this.merchant = merchant;


        container.startOpen(
                playerInventory.player
        );


        /*
         * ========================================================
         * MERCHANT INVENTORY
         * ========================================================
         */

        for (int row = 0; row < 3; row++) {

            for (int column = 0; column < 9; column++) {

                this.addSlot(
                        new Slot(
                                container,
                                column + row * 9,
                                8 + column * 18,
                                18 + row * 18
                        )
                );
            }
        }


        /*
         * ========================================================
         * PLAYER INVENTORY
         * ========================================================
         */

        for (int row = 0; row < 3; row++) {

            for (int column = 0; column < 9; column++) {

                this.addSlot(
                        new Slot(
                                playerInventory,
                                column + row * 9 + 9,
                                8 + column * 18,
                                84 + row * 18
                        )
                );
            }
        }


        /*
         * ========================================================
         * HOTBAR
         * ========================================================
         */

        for (int column = 0; column < 9; column++) {

            this.addSlot(
                    new Slot(
                            playerInventory,
                            column,
                            8 + column * 18,
                            142
                    )
            );
        }
    }


    /*
     * ============================================================
     * TOTAL PRICE
     * ============================================================
     */

    public int getTotalPrice() {

        int total = 0;


        for (int i = 0; i < 27; i++) {

            ItemStack stack =
                    this.container.getItem(i);


            if (
                    !stack.isEmpty()
                            &&
                            ModCommands.FISH_PRICES
                                    .containsKey(stack.getItem())
            ) {

                total +=
                        ModCommands.FISH_PRICES
                                .get(stack.getItem())
                                * stack.getCount();
            }
        }


        return total;
    }


    public Container getMerchantInventory() {

        return this.container;
    }


    /*
     * ============================================================
     * VALIDATION
     * ============================================================
     */

    @Override
    public boolean stillValid(
            Player player
    ) {

        boolean isCloseEnough =
                this.merchant == null
                        ||
                        (
                                this.merchant.isAlive()
                                        &&
                                        this.merchant.distanceToSqr(player)
                                                <= 64.0D
                        );


        return this.container.stillValid(player)
                && isCloseEnough;
    }


    /*
     * ============================================================
     * SHIFT-CLICK
     * ============================================================
     */

    @Override
    public ItemStack quickMoveStack(
            Player player,
            int index
    ) {

        ItemStack result =
                ItemStack.EMPTY;


        Slot slot =
                this.slots.get(index);


        if (
                slot != null
                        &&
                        slot.hasItem()
        ) {

            ItemStack stack =
                    slot.getItem();


            result =
                    stack.copy();


            if (index < 27) {

                if (
                        !this.moveItemStackTo(
                                stack,
                                27,
                                this.slots.size(),
                                true
                        )
                ) {

                    return ItemStack.EMPTY;
                }

            } else {

                if (
                        !this.moveItemStackTo(
                                stack,
                                0,
                                27,
                                false
                        )
                ) {

                    return ItemStack.EMPTY;
                }
            }


            if (stack.isEmpty()) {

                slot.setByPlayer(
                        ItemStack.EMPTY
                );

            } else {

                slot.setChanged();
            }
        }


        return result;
    }


    /*
     * ============================================================
     * CLOSE MENU
     * ============================================================
     */

    @Override
    public void removed(
            Player player
    ) {

        super.removed(player);


        this.container.stopOpen(
                player
        );


        /*
         * Отпускаем жителя
         */

        if (
                this.merchant != null
                        &&
                        !player.level().isClientSide
        ) {

            this.merchant.setTradingPlayer(
                    null
            );
        }
    }
}