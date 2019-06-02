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
package com.lothrazar.cyclicmagic.block.screentarget;

import com.lothrazar.cyclicmagic.block.core.BaseTESR;
import net.minecraft.block.Block;

public class ScreenTargetTESR<T extends TileEntityScreenTarget> extends BaseTESR<T> {

  public static final int SCREEN_WIDTH = 148;
  // TODO: GUI selects how much padding to use? side padding and top padding? 
  private static final int MAX_WIDTH = 16;
  public static final int MAX_TOTAL = MAX_WIDTH * 8;
  public static final float rowHeight = -0.11F;

  public ScreenTargetTESR(Block block) {
    super(block);
  }

  @Override
  public void render(TileEntityScreenTarget te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
    float zt = 0;
    int offset = te.getField(TileEntityScreenTarget.Fields.OFFSET.ordinal());
    switch (te.getCurrentFacing()) {
      case EAST:
      case WEST:
      case NORTH:
      case SOUTH:
        //zt is relative z-offset not absolute 
        zt = offset;
      break;
      case DOWN:
      case UP:
      default:
      break;
    }
    int angle = this.angleOfFace(te.getCurrentFacing());
    fixLighting(te);
    String lines[] = te.getText().split(System.lineSeparator());
    for (String line : lines) {
      // lnWidth = ((float) this.getFontRenderer().getStringWidth(line)) / ((float) SCREEN_WIDTH);
      renderTextAt(line, x, y, z, destroyStage, te.getXOffset(), te.getYOffset(), zt, angle, te.getColor(), te.getFontSize());
      y += rowHeight;
    }
  }
}
