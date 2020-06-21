/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (C) 2014-2018 Sam Bassett (aka Lothrazar)
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
 ******************************************************************************/
package com.lothrazar.cyclic.enchant;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.lothrazar.cyclic.base.EnchantBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EnchantExcavation extends EnchantBase {

  private static final int POWER_PER_LEVEL = 7;

  public EnchantExcavation(Rarity rarityIn, EnchantmentType typeIn, EquipmentSlotType... slots) {
    super(rarityIn, typeIn, slots);
    MinecraftForge.EVENT_BUS.register(this);
  }

  @Override
  public int getMaxLevel() {
    return 5;
  }

  private int getHarvestMax(int level) {
    if (level <= 5)
      return level * POWER_PER_LEVEL;
    //reduce power if ench is past level 5
    return 5 * POWER_PER_LEVEL + level * 2;
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onBreakEvent(BreakEvent event) {
    IWorld world = event.getWorld();
    PlayerEntity player = event.getPlayer();
    if (player.swingingHand == null) {
      return;
    }
    BlockPos pos = event.getPos();
    Block block = event.getState().getBlock();
    //is this item stack enchanted with ME?
    ItemStack stackHarvestingWith = player.getHeldItem(player.swingingHand);
    int level = this.getCurrentLevelTool(stackHarvestingWith);
    if (level <= 0) {
      return;
    }
    // if I am using an axe on stone or dirt, doesn't trigger
    boolean isAnySingleOk = false;//if i am a tool valid on 2 things, and both of 2 blocks are present, we are just ok
    for (ToolType type : stackHarvestingWith.getItem().getToolTypes(stackHarvestingWith)) {
      if (block.isToolEffective(world.getBlockState(pos), type)) {
        isAnySingleOk = true;
      }
    }
    //starts at 1 for current one
    if (isAnySingleOk) {
      int harvested = this.harvestSurrounding((World) world, player, pos, block, 1, level, player.swingingHand);
      if (harvested > 0) {
        //damage but also respect the unbreaking chant  
        player.getHeldItem(player.swingingHand).attemptDamageItem(1, world.getRandom(), null);
      }
    }
  }

  /**
   * WARNING: RECURSIVE function to break all blocks connected up to the maximum total
   *
   * @param swingingHand
   */
  private int harvestSurrounding(final World world, final PlayerEntity player, final BlockPos posIn, final Block block, int totalBroken, final int level,
      Hand swingingHand) {
    if (totalBroken >= this.getHarvestMax(level)
        || player.getHeldItem(player.swingingHand).isEmpty()) {
      return totalBroken;
    }
    //    int fortuneXp = 0;//even if tool has fortune, ignore just to unbalance a bit
    Set<BlockPos> theFuture = this.getMatchingSurrounding(world, posIn, block);
    Set<BlockPos> wasHarvested = new HashSet<BlockPos>();
    for (BlockPos targetPos : theFuture) {
      BlockState targetState = world.getBlockState(targetPos);
      //check canHarvest every time -> permission or any other hooks
      if (world.isAirBlock(targetPos)
          || !player.isAllowEdit()
          || player.canHarvestBlock(targetState) == false
          || totalBroken >= this.getHarvestMax(level)
          || player.getHeldItem(player.swingingHand).isEmpty()
          || !ForgeHooks.canHarvestBlock(targetState, player, world, targetPos)) {
        continue;
      }
      if (world instanceof ServerWorld) {
        Block.spawnDrops(targetState, world, targetPos, world.getTileEntity(targetPos));
      }
      int bonusLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, player.getHeldItemMainhand());
      int silklevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player.getHeldItemMainhand());
      int exp = targetState.getExpDrop(world, targetPos, bonusLevel, silklevel);
      if (exp > 0) {
        block.dropXpOnBlockBreak(world, targetPos, exp);
      }
      world.destroyBlock(targetPos, false);
      wasHarvested.add(targetPos);
      totalBroken++;
    }
    //AFTER we harvest the close ones only THEN we branch out
    for (BlockPos targetPos : theFuture) {
      if (totalBroken >= this.getHarvestMax(level)
          || player.getHeldItem(player.swingingHand).isEmpty()) {
        break;
      }
      totalBroken += this.harvestSurrounding(world, player, targetPos, block, totalBroken, level, swingingHand);
    }
    return totalBroken;
  }

  private static final Direction[] VALUES = Direction.values();

  private Set<BlockPos> getMatchingSurrounding(World world, BlockPos start, Block blockIn) {
    Set<BlockPos> list = new HashSet<BlockPos>();
    // TODO: DIAGONAL!
    List<Direction> targetFaces = Arrays.asList(VALUES);
    Collections.shuffle(targetFaces);
    for (Direction fac : targetFaces) {
      if (world.getBlockState(start.offset(fac)).getBlock() == blockIn) {
        list.add(start.offset(fac));
      }
    }
    return list;
  }
}